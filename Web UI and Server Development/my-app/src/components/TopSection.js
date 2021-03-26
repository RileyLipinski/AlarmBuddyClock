import React, {Component} from 'react';
import '../App.css';
import './TopSection.css';


class TopSection extends Component {
   
    constructor(props) {
        super(props);
        this.state = {
            time: '00:00:00  AM',
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

        if (today.getHours().toString().length === 1) {
            hours = "0" + today.getHours();
        }
        else {
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
        if (today.getMinutes().toString().length === 1) {
            minutes = "0" + today.getMinutes();
        }
        else {
            minutes = today.getMinutes();
        }
        if (today.getSeconds().toString().length === 1) {
            seconds = "0" + today.getSeconds();
        }
        else {
            seconds = today.getSeconds();
        }
        if(am) {
                this.setState(state => ({
                time: hours + ":" + minutes + ":" + seconds + '  AM'
                }));
        }
        else {
            this.setState(state => ({
                time: hours + ":" + minutes + ":" + seconds + '  PM'
                }));
        }
    }
    
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
    
        checkClock(){
            const audioEl = document.getElementsByClassName("audio-element")[0]
            if (this.state.time === this.state.alarm){
                audioEl.play()
                
            }
        }
    
        render() {
    
             return(
            <div className='top-container'>        
            <h1>{this.state.time}</h1>
           <h3> Current Time</h3>
           <p> Scroll down to see all that Alarm Buddy has to offer!</p>
             </div>
            );

        }
    }

export default TopSection;