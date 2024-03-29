# Alexa Pi script.
# 4/12/2021

import sounddevice as sd
from scipy.io.wavfile import write
import json
import logging
import os
import time
import requests
from ask_sdk_core.utils import is_intent_name, get_slot_value
import sched
import time
from flask import Flask
from flask_ask import Ask, request, session, question, statement
from playsound import playsound
import threading
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
    if(x.status_code != 200):
        speech_text = 'Sorry, I could not log into Alarm Buddy. Please try again later.'
        return statement(speech_text).simple_card(speech_text)
    token = x.json()['token']
    speech_text = 'Welcome to Alarm Buddy. Would you like to create an alarm? Or you can ask for help.'
    return question(speech_text).reprompt(speech_text).simple_card(speech_text)

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
        th.start()
        return question(speak_output).reprompt(speak_output).simple_card('CreateAlarm', speak_output)


@ask.intent('AlarmBuddy_Record')
def RecordAlarmIntent():
    speak_output = "Okay. After I say, start, speak into the microphone... start."
    th = threading.Thread(target=scheduler.run)
    scheduler_e = scheduler.enter(7, 1, record_audio, ([th]))
    th.start()
    return statement(speak_output).simple_card('Record', speak_output)

@ask.intent('AlarmBuddy_GetFriends')
def GetFriendIntent():
    speak_output = 'Your current friends are... '
    friends_url = config['base_url'] + '/friendsWith/' + config['alarmbuddy_account']['username']
    friends_header = {'Authorization': token}
    f = requests.get(friends_url, headers=friends_header)
    if(f.status_code != 200):
        speak_output = "Sorry, I could not get your friends list at this time. Please try again later."
        return question(speak_output).simple_card('getFriendsError', speak_output)
    friends_list = json.loads(f.content)
    if(len(friends_list) <= 0):
        speak_output = "You have no friends on your account."
        return question(speak_output).simple_card('getFriendsNone', speak_output)
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
    if(f.status_code != 200):
        speak_output = "Sorry, I could not get your sounds list at this time. Please try again later."
        return question(speak_output).simple_card('getSoundsError', speak_output)
    sounds_list = json.loads(f.content)
    if(len(sounds_list) <= 0):
        speak_output = "You have no sounds on your account."
        return question(speak_output).simple_card('getSoundsNone', speak_output)
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
    if(f.status_code != 200):
        speak_output = "Sorry, I could not get your friend requests at this time. Please try again later."
        return question(speak_output).simple_card('getFriendRequestsError', speak_output)
    requests_list = json.loads(f.content)
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
    print(f.content)
    if(f.status_code != 201):
        speak_output = "Sorry, I could not get your block list at this time. Please try again later."
        return question(speak_output).simple_card('getBlockListError', speak_output)
    block_list = json.loads(f.content)
    if(len(block_list) <= 0):
        speak_output = 'You currently have nobody on your block list.'
        return question(speak_output).simple_card('getBlockList', speak_output)
    for block in block_list:
        speak_output = speak_output + block['blocked'] + ", "
    speak_output = speak_output[:-2] + "."
    return question(speak_output).simple_card('getBlockList', speak_output)

@ask.intent('AlarmBuddy_SendSounds', mapping={'friend_uname' : 'friend_uname', 'sound_id' : 'sound_id'})
def SendSoundIntent(friend_uname, sound_id):
    friend_uname = friend_uname.replace(" ", "")
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

@ask.intent('AlarmBuddy_BlockUser', mapping={'block_uname' : 'block_uname'})
def BlockUser(block_uname):
    if(block_uname is None):
        speak_output = "Sorry, you must specify a username to block."
        return question(speak_output).reprompt(speak_output).simple_card('BlockUser_BlockUsernameIsNone', speak_output)
    #Attempt to block user
    block_uname = block_uname.replace(" ", "")
    header = {"Authorization": token}
    block_user_url = config['base_url'] + '/blockUser/' + config['alarmbuddy_account']['username'] + '/' + block_uname
    response = requests.post(block_user_url, headers=header)
    if(response.status_code == 201): 
        speak_output = 'Okay. The user with the username ' + block_uname + ' has been blocked.'
        return question(speak_output).reprompt(speak_output).simple_card('BlockUser_BlockUsername', speak_output)
    else:
        speak_output = 'Sorry. Failed to block user with the username ' + block_uname
        return question(speak_output).reprompt(speak_output).simple_card('BlockUser_BlockUsernameIsInvalid', speak_output)

@ask.intent('AlarmBuddy_UnblockUser', mapping={'unblock_uname' : 'unblock_uname'})
def UnblockUser(unblock_uname):
    if(unblock_uname is None):
        speak_output = "Sorry, you must specify a username to unblock."
        return question(speak_output).reprompt(speak_output).simple_card('UnblockUser_UnblockUsernameIsNone', speak_output)
    #Attempt to unblock user
    unblock_uname = unblock_uname.replace(" ", "")
    header = {"Authorization": token}
    unblock_user_url = config['base_url'] + '/unblockUser/' +  config['alarmbuddy_account']['username'] + '/' + unblock_uname
    response = requests.post(unblock_user_url, headers=header)
    if(response.status_code == 201): 
        speak_output = 'Okay. The user with the username ' + unblock_uname + ' has been unblocked.'
        return question(speak_output).reprompt(speak_output).simple_card('UnBlockUser_BlockUsername', speak_output)
    else:
        speak_output = 'Sorry. Failed to unblock user with the username ' + unblock_uname
        return question(speak_output).reprompt(speak_output).simple_card('UnBlockUser_BlockUsernameIsInvalid', speak_output)

@ask.intent('AlarmBuddy_DeleteFriend', mapping={'friend_uname' : 'friend_uname'})
def DeleteFriend(friend_uname):
    if(friend_uname is None):
        speak_output = "Sorry, you must specify a friend to delete."
        return question(speak_output).reprompt(speak_output).simple_card('DeleteFriend_FriendIsNone', speak_output)
    
    #get list of friends
    friend_uname = friend_uname.replace(' ', '')
    header = {"Authorization": token}
    friends_list_url = config['base_url'] + '/friendsWith/' + config['alarmbuddy_account']['username']
    friends_list = requests.get(friends_list_url, headers=header).json()
    #check that recipient is a friend
    friend_found = False
    for friend in friends_list:
        if friend['username2'] == friend_uname:
            friend_found = True
    if(not friend_found):
        speak_output = "You already weren't friends with " + friend_uname + "."
        return question(speak_output).reprompt(speak_output).simple_card('DeleteFriend_AlreadyNotFriends', speak_output)

    #Attempt to delete friend
    delete_friend_url = config['base_url'] + '/deleteFriend/' + config['alarmbuddy_account']['username'] + '/' + friend_uname
    response = requests.delete(delete_friend_url, headers=header)

    if(response.status_code == 201): 
        speak_output = 'Okay. Your friend ' + friend_uname + ' has been deleted.'
        return question(speak_output).reprompt(speak_output).simple_card('DeleteFriend_Success', speak_output)
    else:
        speak_output = 'Sorry. Failed to delete your friend with the name ' + friend_uname
        return question(speak_output).reprompt(speak_output).simple_card('DeleteFriend_Invalid', speak_output)

@ask.intent('AlarmBuddy_SendFriendRequest', mapping={'receiver_uname' : 'receiver_uname'})
def SendFriendRequest(receiver_uname):
    if(receiver_uname is None):
        speak_output = "Sorry, you must specify a username to send a friend request to."
        return question(speak_output).reprompt(speak_output).simple_card('AcceptFriendRequest_ReceiverIsNone', speak_output)
    
    receiver_uname = receiver_uname.replace(" ", "")
    #Check if you are already friends
    #get list of friends
    header = {"Authorization": token}
    friends_list_url =  config['base_url'] + '/friendsWith/' + config['alarmbuddy_account']['username']
    friends_list = requests.get(friends_list_url, headers=header).json()
    #check that recipient is not friend
    friend_found = False
    for friend in friends_list:
        if friend['username2'] == receiver_uname:
            friend_found = True
    if(friend_found):
        speak_output = "Sorry, you are already friends with " + receiver_uname + "."
        return question(speak_output).reprompt(speak_output).simple_card('SendFriendRequest_AlreadyFriends', speak_output)

    #Attempt to send friend request
    send_request_url = config['base_url'] +  '/sendRequest/' + config['alarmbuddy_account']['username'] + '/' + receiver_uname
    response = requests.post(send_request_url, headers=header)

    if(response.status_code == 201): 
        speak_output = 'Okay. Friend request has been sent to ' + receiver_uname
        return question(speak_output).reprompt(speak_output).simple_card('SendFriend_Success', speak_output)
    else:
        speak_output = 'Sorry. Failed to send the friend request to ' + receiver_uname
        return question(speak_output).reprompt(speak_output).simple_card('SendFriend_Success', speak_output)

@ask.intent('AlarmBuddy_CancelFriendRequest', mapping={'receiver_uname' : 'receiver_uname'})
def CancelFriendRequest(receiver_uname):
    if(receiver_uname is None):
        speak_output = "Sorry, you must specify a username to cancel a friend request for."
        return question(speak_output).reprompt(speak_output).simple_card('CancelFriendRequest_ReceiverIsNone', speak_output)

    receiver_uname = receiver_uname.replace(" ", "")
    #Attempt to cancel friend request
    header = {"Authorization": token}
    cancel_request_url = config['base_url'] + '/cancelFriendRequest/' + config['alarmbuddy_account']['username'] + '/' + receiver_uname
    response = requests.post(cancel_request_url, headers=header)

    if(response.status_code == 201): 
        speak_output = 'Okay. Friend request to ' + receiver_uname + ' has been cancelled.'
        return question(speak_output).reprompt(speak_output).simple_card('CancelFriend_Success', speak_output)
    else:
        speak_output = 'Sorry. Failed to cancel the friend request to ' + receiver_uname + '.'
        return question(speak_output).reprompt(speak_output).simple_card('CancelFriend_Invalid', speak_output)

@ask.intent('AlarmBuddy_DenyFriendRequest', mapping={'sender_uname' : 'sender_uname'})
def DenyFriendRequest(sender_uname):
    if(sender_uname is None):
        speak_output = "Sorry, you must specify a username to send a friend request to."
        return question(speak_output).reprompt(speak_output).simple_card('DenyFriendRequest_SenderIsNone', speak_output)

    #Get friend requests 
    sender_uname = sender_uname.replace(" ", "")
    header = {"Authorization": token}
    request_list_url = config['base_url'] + '/requests/' + config['alarmbuddy_account']['username']
    request_list = requests.get(request_list_url, headers=header).json()

    #Verify that request exists
    request_found = False
    for request in request_list:
        if request['senderUsername'] == sender_uname:
            request_found = True
    if(not request_found):
        speak_output = "Sorry, no friend request was found under the username " + sender_uname + "."
        return question(speak_output).reprompt(speak_output).simple_card('DenyFriendRequest_RequestNotFound', speak_output)

    #Deny friend request
    denyRequest_url = config['base_url'] + '/denyFriendRequest/' + config['alarmbuddy_account']['username'] + '/' + sender_uname
    response = requests.post(denyRequest_url, headers = {'Authorization' : token})

    if(response.status_code == 201): 
        speak_output = 'Okay. Friend request from ' + sender_uname + ' + has been denied.'
        return question(speak_output).reprompt(speak_output).simple_card('DenyFriend_Success', speak_output)
    else:
        speak_output = 'Sorry. Failed to deny the friend request from ' + sender_uname
        return question(speak_output).reprompt(speak_output).simple_card('DenyFriend_Invalid', speak_output)

@ask.intent('AlarmBuddy_AcceptFriendRequest', mapping={'sender_uname' : 'sender_uname'})
def AcceptFriendRequest(sender_uname):
    if(sender_uname is None):
        speak_output = "Sorry, you must specify a username to accept a friend request from."
        return question(speak_output).reprompt(speak_output).simple_card('AcceptFriendRequest_SenderIsNone', speak_output)

    sender_uname = sender_uname.replace(" ", "")
    #get list of friend requests
    header = {"Authorization": token}
    friendRequest_url = config['base_url'] + '/requests/' + config['alarmbuddy_account']['username']
    request_list = requests.get(friendRequest_url, headers=header).json()

    #find friend request in list
    request_found = False
    for request in request_list:
        if request['senderUsername'] == sender_uname:
            request_found = True
    if(not request_found):
        speak_output = "Sorry, no friend request was found under the username " + sender_uname + "."
        return question(speak_output).reprompt(speak_output).simple_card('AcceptFriendRequest_RequestNotFound', speak_output)

    #Attempt to accept friend request
    acceptRequest_url = config['base_url'] + '/acceptFriendRequest/' + config['alarmbuddy_account']['username'] + '/' + sender_uname
    response = requests.post(acceptRequest_url, headers = {'Authorization' : token})
    
    #Error if already friends
    if(response.status_code == 403):
        speak_output = "Sorry, you are already friends with this user."
        return question(speak_output).reprompt(speak_output).simple_card('AcceptFriendRequest_SenderIsAlreadyFriend', speak_output)

    if(response.status_code == 201): 
        speak_output = 'Okay. Friend request has been accepted from ' + sender_uname
        return question(speak_output).reprompt(speak_output).simple_card('AcceptFriendRequest_Success', speak_output)
    else:
        speak_output = 'Sorry. Failed to accept the friend request from ' + sender_uname
        return question(speak_output).reprompt(speak_output).simple_card('AcceptFriendRequest_Invalid', speak_output)


@ask.intent('AMAZON.HelpIntent')
def help():
    # Intent designed to help the user use the application.
    speech_text = """You can create an alarm by saying the following: Create an alarm for date at time. 
    For example, create an alarm for tomorrow at eight p.m. 
    If you want to leave Alarm Buddy, simply say cancel or stop.
    If you want to record a sound, you can say: record a sound.
    You can send a friend request by saying: send friend request to bob. You can also delete friends by saying: delete friend bob.
    If you want to accept or deny a friend request, say: accept friend request from bob, or, deny friend request from bob.
    If you want to cancel a friend request you sent, say: cancel my friend request to bob.
    You can send a friend a sound by saying: send sound 123 to bob, where 123 is a sound i.d. . to figure out the i.d., you can say: get my sounds list.
    You can also get your friend requests by saying: what are my friend requests? You can also get your friends list by saying: tell me my alarm buddy friends.
    You can see who you have blocked by saying: who do I have blocked?
    If you want to block a user, say: block user bob. If you want to unblock a user, say: unblock user bob."""
    return question(speech_text).reprompt(speech_text).simple_card('Help', speech_text)

def record_audio(thread):
    fs = 16000  # Sample rate
    seconds = 10  # Duration of recording

    mydevice = 4

    myrecording = sd.rec(int(seconds * fs), samplerate=fs, channels=2, device=mydevice)
    sd.wait()  # Wait until recording is finished
    write('output.wav', fs, myrecording)  # Save as WAV file 


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
    if(u.status_code != 201):
        print("ERROR: file not uploaded.")
    else:
        print("file successfully uploaded to database from Alexa Pi.")

def play_alarm(thread):
    # Function that is called at the time specified by the Create Alarm Intent
    sounds_url = config['base_url'] + '/sounds/' + config['alarmbuddy_account']['username']
    sounds_header = {'Authorization': token}
    f = requests.get(sounds_url, headers=sounds_header)
    sounds_list = json.loads(f.content)
    max_soundID = -1
    for item in sounds_list:
        if(max_soundID < item['soundID']):
            max_soundID = item['soundID']
    download_url = config['base_url'] + '/download/' + config['alarmbuddy_account']['username'] + '/' + str(max_soundID)
    response = requests.get(download_url, headers={'Authorization': token})
    #if fails to download sound, replace sound with default.
    if(response.status_code != 200):
        sound_path = os.getcwd() + '/alarm_buddy.mp3'
    else:
        open('downloadedsound.mp3', 'wb').write(response.content)
        sound_path = os.getcwd() + '/downloadedsound.mp3'
    print('playing sound at ' + sound_path)
    playsound(sound_path)

@ask.session_ended
def session_ended():
    return "{}", 200


if __name__ == '__main__':
    if 'ASK_VERIFY_REQUESTS' in os.environ:
        verify = str(os.environ.get('ASK_VERIFY_REQUESTS', '')).lower()
        if verify == 'false':
            app.config['ASK_VERIFY_REQUESTS'] = False
    app.run(debug=True)