import React from 'react';
import './Clock.css';



class Clock extends React.Component {
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
                time: hours + ":" + minutes + ":" + seconds + ''
                }));
        }
        else {
            this.setState(state => ({
                time: hours + ":" + minutes + ":" + seconds + '  PM'
                }));
        }
    }

    setAlarm(event) {
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

    checkClock() {
        const audioEl = document.getElementsByClassName("audio-element")[0]
        if (this.state.time === this.state.alarm) {
            audioEl.play()
            
        }
    }

    render() {

        return (
            <div className="Main Clock">
                <div id="clock">{this.state.time}</div>
                <div>Set Alarm:</div>
                <form><input type="time" onChange={this.setAlarm}></input></form>
                <audio className="audio-element">
                    <source src="https://assets.coderrocketfuel.com/pomodoro-times-up.mp3"></source>
                </audio>
            </div>
        );
    }
}

export default Clock; 