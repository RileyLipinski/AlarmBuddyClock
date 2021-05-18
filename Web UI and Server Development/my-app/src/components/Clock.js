/**
 * This React component is renders the basic clock and alarm
 */

import React from 'react';
import './Clock.css';
import Alarm from './Alarm';

class Clock extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            time: '',
            alarm: '', 
        }
        this.setAlarm = this.setAlarm.bind(this);
    }

    tick() {
        const today = new Date();
        let hours;
        let minutes;
        let seconds;
        let am = true;
        
        //The following if statements convert the hours from military
        //time to standard time
        if (today.getHours() === 0){
            hours = "12";
        } else if (today.getHours().toString().length === 1) {
            hours = "0" + today.getHours();
        } else {
            hours = today.getHours();
        }
        if (today.getHours() > 12) {
            hours = "0" + today.getHours() % 12;
            am = false;
        }
        if (today.getHours() === 12) {
            hours = today.getHours();
            am = false;
        }
        //add a 0 to minutes if number is single digit
        if (today.getMinutes().toString().length === 1) {
            minutes = "0" + today.getMinutes();
        } else {
            minutes = today.getMinutes();
        }
        //add a 0 to seconds if number is single digit
        if (today.getSeconds().toString().length === 1) {
            seconds = "0" + today.getSeconds();
        } else {
            seconds = today.getSeconds();
        }
        //add AM or PM to end of time depending on time
        if(am) {
            this.setState(state => ({
                time: hours + ":" + minutes + ":" + seconds + '  AM'
            }));
        } else {
            this.setState(state => ({
                time: hours + ":" + minutes + ":" + seconds + '  PM'
            }));
        }
    }
    
    //Sets a new alarm event
    setAlarm(event){
        const newAlarm = event.target.value + ':0'
        console.log("Alarm set for " + newAlarm);
        this.setState(state => ({
            alarm: newAlarm,
        }));
        this.props.token.setAlarm(newAlarm);  
        event.preventDefault();
    }
    
    componentDidMount() {
        this.interval = setInterval(() => this.tick(), 1000);

    }
        
    componentWillUnmount() {
        clearInterval(this.interval);
        clearInterval(this.check);
    }
    
    /**
     * Checks clock and plays alarm if current time equals alarm time
     */
    render() {

        return (
            <div className="MainClock">
		        <h1>Set Your Alarms Here</h1>
                <p>Current Time: {this.state.time}</p>   
                <input type="time" onChange={this.setAlarm}></input> 
               
                <audio className="audio-element">
                    <source src="https://assets.coderrocketfuel.com/pomodoro-times-up.mp3"></source>
                </audio>

            </div>
        );
    }
}

export default Clock; 