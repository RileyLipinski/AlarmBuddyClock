import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';



class Clock extends React.Component {
    constructor(props) {
        super(props);
        var today = new Date();
        this.state = {
            time: today.getHours() + ":" + today.getMinutes() + ":" + today.getSeconds(),
        }
    }

    tick() {
        var today = new Date();
        this.setState(state => ({
          time: today.getHours() + ":" + today.getMinutes() + ":" + today.getSeconds()
        }));
    }

    componentDidMount() {
        this.interval = setInterval(() => this.tick(), 1000);
    }
    
    componentWillUnmount() {
        clearInterval(this.interval);
    }

    render() {
        return (
            <div className="Main Clock">
                <div><Alarm timeData = {this.state.time}/></div>
                <div>{this.state.time}</div>
            </div>
            
        );
    }
}

class Alarm extends React.Component {
    constructor(props) {
        super(props); 
        this.state = {
            time: this.props.timeData,
            text: ''
        }
    }

    render() {
        return(
            <div>
            hours
            <br></br>
            <input id="hours" />
            <br></br>
            minutes
            <br></br>
            <input id = "minutes"/> 
            <button>
                Set Alarm 
            </button>
            </div>
        );
    }
}



ReactDOM.render(
    <Clock />,

    document.getElementById('root')
    
  );