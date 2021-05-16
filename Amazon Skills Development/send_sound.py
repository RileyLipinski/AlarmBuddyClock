@ask.intent('AlarmBuddy_SendSound', mapping={'friend_uname' : 'friend_uname', 'sound_id' : 'sound_id'})
def SendSoundIntent(friend_uname, sound_id):
    if(friend_uname is None):
        speak_output = "Sorry, you must specify a username to send a sound to."
        return question(speak_output).reprompt(speak_output).simple_card('AddFriend_UnameError', speak_output)
    if(sound_id is None):
        speak_output = "Sorry, you must specify a recorded alarm to send."
        return question(speak_output).reprompt(speak_output).simple_card('SendSound_SoundIdError', speak_output)

    #get list of friends
    friends_list_url = config['base_url'] + '/friendsWith/' + config['alarmbuddy_account']['username']
    friends_list = requests.get(friends_list_url, headers=header).json()

    #check that recipient is a friend
    friend_found = False
    for friend in friends_list:
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
        if sound['soundID'] == sound_id:
            sound_to_send = sound

    if sound_to_send is None:
        speak_output = "Sorry, an alarm sound with that name cannot be found. Have you recorded it?"
        return question(speak_output).reprompt(speak_output).simple_card('SendSound_SoundNotFoundError', speak_output)

    #Send the sound.
    send_sound_url = config['base_url'] + '/shareSound/' + config['alarmbuddy_account']['username'] + '/' + friend_uname + '/' + str(sound_to_send['soundID'])
    u = requests.post(send_sound_url, headers=header)
    if(u.status_code != 201):
        speak_output = "Something went wrong. We couldn't send the sound to your friend."
        return question(speak_output).reprompt(speak_output).simple_card('SendSound_Error', speak_output)

    return statement('Okay. ' + sound_to_send['soundName'] + ' has been sent to ' + friend_uname)
