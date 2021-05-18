/**
 * This React component is renders the basic alarm which is rendered within
 * the navbar. The alarm component has no visual parts, but will play the set
 * sound when the set alarm equals the current time.
 */

 import React from 'react';
 import './Clock.css';
 import axios from 'axios';
 
 class Alarm extends React.Component {
    constructor(props) {
         super(props);
         this.state = {
            time: '',
            alarm: this.props.token.alarm,
            audioId: this.props.token.audioId
        }
    }

    //This method gets the current time and sets it to state.time
    tick() {
        const today = new Date();
        let hours;
        let minutes;
        let seconds;
        hours = today.getHours();

        //add a 0 to minutes if number is single digit
        if (today.getMinutes().toString().length === 1) {
             minutes = "0" + today.getMinutes();
        } else {
             minutes = today.getMinutes();
        }
        seconds = today.getSeconds();

        this.setState(state => ({
            time: hours + ":" + minutes + ":" + seconds
        }));
     }
    
    //this method runs when the component is loaded on a page
    //It sets both the tick() and checkClock() methods to be called
    //every 1 second
    componentDidMount() {
         this.interval = setInterval(() => this.tick(), 1000);
         this.check = setInterval(() => this.checkClock(),1000)
     }
    
    //this method runs when the component is unloaded from a page.
    //It clears both of the recurring interval calls to methods made
    //previously
    componentWillUnmount() {
         clearInterval(this.interval);
         clearInterval(this.check);

     }
    
    /**
     * Checks clock and plays alarm if current time equals alarm time
     * Due to restrictions and ease of use, the sound is gotten from
     * the database at the time of it being played.
     * 
     * If no sound is specifically set, the most recent sound is used
     * as the alarm.
    */
    checkClock(){
        if (this.state.time === this.props.token.getAlarm()) {
            let soundNum = this.props.token.getAudio();
            if(soundNum === 0) {
                return;
            } else if(soundNum === "") {
                return;
            } else {
                console.log("Getting sound number: " + soundNum);
                axios.get("https://alarmbuddy-312620.uc.r.appspot.com/download/" + 
                    this.props.token.username + "/" + soundNum, {
                    responseType: 'arraybuffer',
                    headers: { 'Authorization': this.props.token.token, },
                }).then(response => {
                    console.log("sound download res: ", response);
                    if(response.status === 200) {
                        const blob = new Blob([response.data], { type: 'audio/mpeg' });

                        const url = URL.createObjectURL(blob);

                        let audio = new Audio(url);
                        audio.play();
                        URL.revokeObjectURL(blob);
                    }
                }).catch(error => {
                console.log("Sound download error: ", error);
                });
            }
        } 
    }
     
    //the render method for alarm
    render() {
        return (
            <div className="alarm">  
            </div>
        );
     }
 }
 
 export default Alarm; 