/*
This is the component that handles getting the users audio files
that they have stored on the database. Also handles the sharing the 
sounds with another user.
*/
import axios from 'axios';
import React,{Component} from 'react';
import cogoToast from 'cogo-toast';

class FileDownload extends Component {
    constructor(props) {
        super(props);

        this.state = {
            friends: [],
        }

        this.soundInfo = null;
        this.playAudio = this.playAudio.bind(this);
        this.sentBy = this.sentBy.bind(this);
        this.setSound = this.setSound.bind(this);
        this.shareSound = this.shareSound.bind(this);
    }
    //gets the audio list for the user logged in
    getAudioList(){
        const optList = document.getElementById("soundList");
        let soundIDList;
        axios.get("https://alarmbuddy-312620.uc.r.appspot.com/sounds/" + this.props.token.username, {
		    headers: { 'Authorization': this.props.token.token, },
	    }).then(response => {
            console.log("sound list res: ", response);
            if(response.status === 200) {
                soundIDList = response.data;
                if(soundIDList.length == null) {
                    cogoToast.error("Error getting owned sounds", {position: 'top-right'});
                }
                else if(soundIDList.length === 0) {
                    let option = document.createElement("OPTION");
                    option.innerHTML = "No sounds";
                    option.value = 0;
                    optList.options.add(option);
                }
                else {
                    this.soundInfo = soundIDList;
                    for(let i = 0; i < soundIDList.length; i++) {
                        let option = document.createElement("OPTION");
                        option.innerHTML = soundIDList[i].soundName;
                        option.value = soundIDList[i].soundID;
                        optList.options.add(option);
                        this.props.token.setAudio(soundIDList[i].soundID);
                    }
                }
            }
        }).catch(error => {
            console.log("Sound list error: ", error);
        });

    };

    
    //plays the selected audio
    playAudio(event){
        let e = document.getElementById("soundList");
        let soundNum = e.value;
        if(soundNum == 0) {
            return;
        }
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
        event.preventDefault();
    }
    //function that checks and displays who sent the sound to the user.
    sentBy(event){
        let e = document.getElementById("soundList");
        if(e == null) {
            e = document.getElementById("shared");
            e.innerHTML = "There are no sounds!";
            return;
        }
        let soundNum = e.value;
        let shared = "";
        for(let i = 0; i < this.soundInfo.length; i++) {
            if(this.soundInfo[i].soundID === soundNum) {
                shared = this.soundInfo[i].sharedBy;
                break;
            }
        }
        e = document.getElementById("shared");
        if(shared.length <= 0) {
            e.innerHTML = "This sound was uploaded by you!";
        } else {
            e.innerHTML = "This sound was shared with you by user: " + shared;
        }
        event.preventDefault();
    }
    //this function sets the current sound
    setSound(event) {
        let e = document.getElementById("soundList");
        if(e == null) {
            e = document.getElementById("shared");
            e.innerHTML = "There are no sounds!";
            return;
        }
        let soundNum = e.value;
        this.props.token.setAudio(e.value);
        event.preventDefault();
    }
    //this function gets the user's friends to share files to them later
    getFriendList(){
        this.interval = setInterval(() => this.setState({ time: Date.now() }), 1000);
        axios.get('https://alarmbuddy-312620.uc.r.appspot.com/friendsWith/' + this.props.token.username, {
		    headers: { 'Authorization': this.props.token.token,},
	    }).then(response => {
            console.log("sound download res: ", response);
            if(response.status === 200) {
                this.state.friends = response.data;
                this.printFriends()
                
                

            }
        }).catch(error => {
            console.log("Error Getting Friends ", error);
        });
    }
    //this function shares the selected sound with the desired friend
    shareSound(event){
        let friendShare = event.target.getAttribute('data1');
        let e = document.getElementById("soundList");
        let soundNum = e.value;
        if(soundNum == 0) {
            return;
        }
        cogoToast.loading('Sending Sound...', {position: 'top-right'}).then(() => {
          
          axios.post("https://alarmbuddy-312620.uc.r.appspot.com/shareSound/" + this.props.token.username + '/' + friendShare + '/' +  soundNum,
            '', {
              headers: { 
                  'Authorization': this.props.token.token,
                  
               },
     })
        
          .then(response => {
              
              if(response.status === 204) {
                  
                  cogoToast.success("Sent", {position: 'top-right'});
              }	
          }).catch(error => {
              console.log("Sending Error", error);
              cogoToast.error("sending Error", {position: 'top-right'});
          });
      });
      this.setState({ state: this.state });

        event.preventDefault();
    }
    //prints the users friends that they can share sounds with
    printFriends(){
        
        
        return (
            <div>
               
              {this.state.friends.map((person, index) => (
                <div className='requests'><p key={index}>{person.username2}</p>
                <button onClick = {this.shareSound} data1 = {person.username2}>Share Sound</button>
                </div>
               
              ))}
            </div>
        )
  

      }
    //What gets called immedietly on load
    componentDidMount() {
        this.getAudioList();
        this.getFriendList();
    }
    //render function
    render() {
        return (
          <div>
                <div>
                    <button onClick={this.playAudio} type="button"> 
                        PLAY!
                    </button>
                    <button onClick={this.sentBy} type="button">
                        Who sent me this sound?
                    </button>
                    <button onClick={this.setSound} type="button">
                        Set this as my alarm!
                    </button>
                    
	                
                        <h2>Friends:</h2>
                        {this.printFriends()}
	            

                </div>
            </div>
        );
      }
}

export default FileDownload;