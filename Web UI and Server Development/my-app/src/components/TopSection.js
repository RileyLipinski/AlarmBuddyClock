/**
 * This component renders the homepage alarm clock with current time
 */

import React, {Component} from 'react';
import '../App.css';
import './TopSection.css';


class TopSection extends Component {
   
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
            hours = "0" + today.getHours();
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
        event.preventDefault();
        const newAlarm = event.target.value + ':0'
        console.log(newAlarm);
        this.setState(state => ({
            alarm: newAlarm,
        }));
    }
    
    componentDidMount() {
        this.interval = setInterval(() => this.tick(), 1000);
        this.check = setInterval(() => this.checkClock(),1000)
    }
        
    componentWillUnmount() {
        clearInterval(this.interval);
        clearInterval(this.check);
    }
    
    /**
     * Checks clock and plays alarm if current time equals alarm time
     */
    checkClock(){
        const audioEl = document.getElementsByClassName("audio-element")[0];
        if (this.state.time === this.state.alarm) {
            audioEl.play()  
        }
    }
    
    render() {
        return(
            <div className='top-container'>        
        <h3>Current Time: {this.state.time}</h3>       
   <p> Check out all that Alarm Buddy has to offer!</p>
                            
            </div>
        );
    }
}

export default TopSection;