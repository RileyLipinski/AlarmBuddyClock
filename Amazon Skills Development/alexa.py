# Alexa Pi script.
# 4/12/2021

import logging
import os
import time
import datetime
import requests
from ask_sdk_core.utils import is_intent_name, get_slot_value
#from threading import Timer
import sched
import time
from flask import Flask
from flask_ask import Ask, request, session, question, statement
from playsound import playsound
import threading


# Flask-Ask set up
app = Flask(__name__)
ask = Ask(app, "/")
logging.getLogger('flask_ask').setLevel(logging.DEBUG)


@ask.launch
def launch():
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

        scheduler = sched.scheduler(time.time, time.sleep)
        th = threading.Thread(target=scheduler.run)
        scheduler_e = scheduler.enterabs(t, 1, play_alarm, ([th]))
        speak_output = "You have created an alarm that will go off on " + day + " at " + timeofday  + "."
        print("in create alarm: " + speak_output)
        th.start()
        return question(speak_output).reprompt(speak_output).simple_card('CreateAlarm', speak_output)

def play_alarm(thread):
    # Function that is called at the time specified by the Create Alarm Intent
    print("in play alarm")
    response = requests.get('https://alarmbuddy.wm.r.appspot.com/download/johnny/erokia.wav', headers={'Authorization': 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6ImpvaG5ueSIsImlhdCI6MTYxODE5MDQyNCwiZXhwIjoxNjE4Mjc2ODI0fQ.8aDiyi_SSNgUQuy4IRnOGX2BBZz8IiySPPeE9tN5Qu8'})
    #print(response.json())
    open('erokia.wav', 'wb').write(response.content)
    playsound('/home/pi/ngrok/alexa/erokia.wav')


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
