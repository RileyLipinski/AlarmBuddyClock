# Alexa Pi script.
# 4/12/2021

import sounddevice as sd
from scipy.io.wavfile import write
import json
import logging
import os
import time
import datetime
import requests
from ask_sdk_core.utils import is_intent_name, get_slot_value
import sched
import time
from flask import Flask
from flask_ask import Ask, request, session, question, statement
from playsound import playsound
import threading
import pprint
from pydub import AudioSegment
import os

token = ''

# Flask-Ask set up
app = Flask(__name__)
ask = Ask(app, "/")
logging.getLogger('flask_ask').setLevel(logging.DEBUG)

with open('config.json', 'r') as f:
    config = json.load(f)

scheduler = sched.scheduler(time.time, time.sleep)
@ask.launch
def launch():
    global token
    login_url = config['base_url'] + "/login"
    login_data = config['alarmbuddy_account']
    x = requests.post(login_url, data = login_data)
    token = x.json()['token']
    print(token)
    speech_text = 'Welcome to Alarm Buddy. Would you like to create an alarm? Or you can ask for help.'
    return question(speech_text).reprompt(speech_text).simple_card(speech_text)

@ask.intent('AlarmIntent')
def Alarm_Intent():
    # Unused 15 second Alarm
    t = Timer(15, play_alarm)
    t.start()
    return statement('Alarm set to go off in 15 seconds.')

@ask.intent('AMAZON.FallbackIntent')
def FallbackIntent():
    # A fallback Intent. If a user says something that doesn't correspond to an intent, they're sent here.
    speak_output = 'sorry I did not understand you.'
    return question(speak_output).reprompt(speak_output).simple_card('FallBack', speak_output)

@ask.intent('AMAZON.CancelIntent')
def CancelIntent():
    # A cancel intent to leave the Alarm Buddy app.
    speak_output = "Goodbye!"
    return statement(speak_output).simple_card('cancel', speak_output)

@ask.intent('AMAZON.StopIntent')
def StopIntent():
    # A stop intent to leave the Alarm Buddy app.
    speak_output = "Goodbye!"
    return statement(speak_output).simple_card('stop', speak_output)

@ask.intent('AlarmBuddy_CreateAlarm', mapping={'day': 'day', 'timeofday': 'timeofday'})
def CreateAlarmIntent(day, timeofday):
    # Creating an alarm intent. Passes in day and timeofday from Amazon Intent Slots.
    if(day is None):
        speak_output = "Sorry, you must specify a day for the alarm."
        return question(speak_output).reprompt(speak_output).simple_card('CreateAlarm_DayError', speak_output)
    elif(timeofday is None):
        speak_output = "Sorry, you must specify a time of day for the alarm."
        return question(speak_output).reprompt(speak_output).simple_card('CreateAlarm_TimeError', speak_output)
    else:
        t = time.strptime(day + " " + timeofday, "%Y-%m-%d %H:%M")
        t = time.mktime(t)
        if(t < time.time()):
            speak_output = "Sorry, you cannot create an alarm for the past."
            return question(speak_output).reprompt(speak_output).simple_card('CreateAlarm_PastError', speak_output)


        th = threading.Thread(target=scheduler.run)
        scheduler_e = scheduler.enterabs(t, 1, play_alarm, ([th])) #maybe have sound id here?
        speak_output = "You have created an alarm that will go off on " + day + " at " + timeofday  + "."
        print("in create alarm: " + speak_output)
        th.start()
        return question(speak_output).reprompt(speak_output).simple_card('CreateAlarm', speak_output)


@ask.intent('AlarmBuddy_Record')
def RecordAlarmIntent():
    speak_output = "Okay. After I say, start, speak into the microphone... start."
    th = threading.Thread(target=scheduler.run)
    scheduler_e = scheduler.enter(8, 1, record_audio, ([th]))
    th.start()
    return statement(speak_output).simple_card('Record', speak_output)

@ask.intent('AlarmBuddy_GetFriends')
def GetFriendIntent():
    speak_output = 'Your current friends are... '
    friends_url = config['base_url'] + '/friendsWith/' + config['alarmbuddy_account']['username']
    friends_header = {'Authorization': token}
    f = requests.get(friends_url, headers=friends_header)
    friends_list = json.loads(f.content)

    #friends_list = [{'username2': 'amaz0n'}, {'username2': 'Don2'}, {'username2': 'jjj123769'}, {'username2': 'Johnny'}, {'username2': 'Twiggy1'}, {'username2': 'Honk_Supreme'}, {'username2': 'brianna4'}, {'username2': 'woah1'}]
    for i in range(6):
        if(i < len(friends_list)):
            speak_output = speak_output + friends_list[i]['username2'] + ", "
    speak_output = speak_output[:-2] + "."
    if(len(friends_list) > 6):
        speak_output = speak_output + " To see more friends, please go to the Alarmbuddy website, or the Alarmbuddy mobile app."
    return question(speak_output).simple_card('getFriends', speak_output)

@ask.intent('AlarmBuddy_GetSounds')
def GetSoundsIntent():
    speak_output = 'The sounds on your account are... '
    sounds_url = config['base_url'] + '/sounds/' + config['alarmbuddy_account']['username']
    sounds_header = {'Authorization': token}
    f = requests.get(sounds_url, headers=sounds_header)
    sounds_list = json.loads(f.content)

    #friends_list = [{'username2': 'amaz0n'}, {'username2': 'Don2'}, {'username2': 'jjj123769'}, {'username2': 'Johnny'}, {'username2': 'Twiggy1'}, {'username2': 'Honk_Supreme'}, {'username2': 'brianna4'}, {'username2': 'woah1'}]
    for sound in sounds_list:
        speak_output = speak_output + sound['soundName'] + ' with i.d. ' + str(sound['soundID']) + ', '
    speak_output = speak_output[:-2] + "."
    return question(speak_output).simple_card('getSounds', speak_output)

@ask.intent('AlarmBuddy_GetFriendRequests')
def GetFriendRequestsIntent():
    speak_output = 'Your current requests are... '
    requests_url = config['base_url'] + '/requests/' + config['alarmbuddy_account']['username']
    requests_header = {'Authorization': token}
    f = requests.get(requests_url, headers=requests_header)
    requests_list = json.loads(f.content)
    print(requests_list)
    if(len(requests_list) <= 0):
        speak_output = 'You currently have no incoming friend requests.'
        return question(speak_output).simple_card('getFriendRequests', speak_output)
    for request in requests_list:
        speak_output = speak_output + request['senderUsername'] + ", "
    speak_output = speak_output[:-2] + "."
    return question(speak_output).simple_card('getFriendRequests', speak_output)

@ask.intent('AlarmBuddy_GetBlockList')
def GetBlockListIntent():
    speak_output = 'Your current blocked accounts are... '
    getblock_url = config['base_url'] + '/getBlockList/' + config['alarmbuddy_account']['username']
    getblock_header = {'Authorization': token}
    f = requests.get(getblock_url, headers=getblock_header)
    block_list = json.loads(f.content)
    print(block_list)
    if(len(block_list) <= 0):
        speak_output = 'You currently have nobody on your block list.'
        return question(speak_output).simple_card('getBlockList', speak_output)
    for block in block_list:
        speak_output = speak_output + block['blocked'] + ", "
    speak_output = speak_output[:-2] + "."
    return question(speak_output).simple_card('getBlockList', speak_output)

@ask.intent('AlarmBuddy_SendSounds', mapping={'friend_uname' : 'friend_uname', 'sound_id' : 'sound_id'})
def SendSoundIntent(friend_uname, sound_id):
    print(sound_id)
    if(friend_uname is None):
        speak_output = "Sorry, you must specify a username to send a sound to."
        return question(speak_output).reprompt(speak_output).simple_card('AddFriend_UnameError', speak_output)
    if(sound_id is None):
        speak_output = "Sorry, you must specify a recorded sound i.d. to send."
        return question(speak_output).reprompt(speak_output).simple_card('SendSound_SoundIdError', speak_output)

    #get list of friends
    header = {"Authorization": token}
    friends_list_url = config['base_url'] + '/friendsWith/' + config['alarmbuddy_account']['username']
    friends_list = requests.get(friends_list_url, headers=header).json()

    #check that recipient is a friend
    friend_found = False
    for friend in friends_list:
        print('in friend')
        print(friend)
        if friend['username2'] == friend_uname:
            friend_found = True
    if(not friend_found):
        speak_output = "Sorry, you must be friends with someone to send them an alarm."
        return question(speak_output).reprompt(speak_output).simple_card('SendSound_NotFriendError', speak_output)
        
    #get list of sounds
    sound_list_url = config['base_url'] + '/sounds/' + config['alarmbuddy_account']['username']
    sound_list = requests.get(sound_list_url, headers=header).json()

    #find requested sound
    sound_to_send = None
    for sound in sound_list:
        print('in sound')
        print(sound)
        if str(sound['soundID']) == str(sound_id):
            sound_to_send = sound

    if sound_to_send is None:
        speak_output = "Sorry, an alarm sound with that i.d. cannot be found. Have you recorded it?"
        return question(speak_output).reprompt(speak_output).simple_card('SendSound_SoundNotFoundError', speak_output)

    #Send the sound.
    send_sound_url = config['base_url'] + '/shareSound/' + config['alarmbuddy_account']['username'] + '/' + friend_uname + '/' + str(sound_to_send['soundID'])
    u = requests.post(send_sound_url, headers=header)
    if(u.status_code != 201):
        speak_output = "Something went wrong. We couldn't send the sound to your friend."
        return question(speak_output).reprompt(speak_output).simple_card('SendSound_Error', speak_output)

    return statement('Okay. ' + sound_to_send['soundName'] + ' has been sent to ' + friend_uname)

def play_alarm(thread):
    # Function that is called at the time specified by the Create Alarm Intent
    print("in play alarm")
    download_url = config['base_url'] + '/download/' + config['alarmbuddy_account']['username'] + '/'
    response = requests.get(download_url, headers={'Authorization': token})
    #if fails to download sound, replace sound with default.
    #print(response.json())
    open('downloadedsound.mp3', 'wb').write(response.content)
    sound_path = os.getcwd() + '/downloadedsound.mp3'
    playsound(sound_path)

def record_audio(thread):
    print('in record_audio')
    fs = 16000  # Sample rate
    seconds = 20  # Duration of recording
    print(sd.query_devices())

    mydevice = 4

    myrecording = sd.rec(int(seconds * fs), samplerate=fs, channels=2, device=mydevice)
    sd.wait()  # Wait until recording is finished
    write('output.wav', fs, myrecording)  # Save as WAV file 


    print('start conversion')
    sound = AudioSegment.from_wav('output.wav')

    sound.export('amazon.mp3', format='mp3')
    upload_file('amazon.mp3')

def upload_file(filename):
    upload_url = config['base_url'] + '/upload/' + config['alarmbuddy_account']['username']
    upload_header = {'authorization': token}
    file_data = {'file': (filename, open(filename, 'rb'), 'audio/mpeg')}
    info_data = {'soundDescription': 'Amazon Team Alexa MP3 Upload'}
    u = requests.post(upload_url, headers=upload_header, files=file_data, data=info_data)

    #put a check. If fails to upload, do something?
    print(u)
    print(u.content)


@ask.intent('AMAZON.HelpIntent')
def help():
    # Intent designed to help the user use the application.
    speech_text = 'You can create an alarm by saying the following: Create an alarm for date at time. For example, create an alarm for tomorrow at eight p.m. If you want to leave Alarm Buddy, simply say cancel or stop.'
    return question(speech_text).reprompt(speech_text).simple_card('Help', speech_text)


@ask.session_ended
def session_ended():
    return "{}", 200


if __name__ == '__main__':
    if 'ASK_VERIFY_REQUESTS' in os.environ:
        verify = str(os.environ.get('ASK_VERIFY_REQUESTS', '')).lower()
        if verify == 'false':
            app.config['ASK_VERIFY_REQUESTS'] = False
    app.run(debug=True)
