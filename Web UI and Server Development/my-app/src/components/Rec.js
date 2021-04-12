/**
 * This React component records audio from an input mic.
 * The user can then upload the recorded audio to the server,
 * playback their audio, or reset the recorded audio.
 */

 import React from 'react';
 import {Recorder} from 'react-voice-recorder';
 import 'react-voice-recorder/dist/index.css';
 import cogoToast from 'cogo-toast';
 import axios from 'axios';


 class Rec extends React.Component {
     constructor(props) {
         super(props);
         this.state = {
             audioDetails: {
                 url: null,
                 blob: null,
                 chunks: null,
                 duration: {
                     h: null,
                     m: null,
                     s: null,
                 }
             }
         }
     }
    
    /**
     * Stops the current recording of audio and stores it in state
     * @param {blob} data 
     */
    handleAudioStop(data) {
        this.setState({ audioDetails: data });
    }
 
    /**
     * Upload the recorded audio blob to the database under the current user's ID
     * @param {blob} file - The audio file in blob format to be uploaded to the database
     */
    handleAudioUpload(audioFile) {
        cogoToast.loading('Saving your audio...', {position: 'top-right'}).then(() => {
            axios.post("https://alarmbuddy.wm.r.appspot.com/upload/Don", {
			headers: { 'Access-Control-Allow-Origin': '*' },
			//params: {
			    file: audioFile
			//}
		    },
		    { withCredentials: true }
            ).then(response => {
			    console.log("sound upload res: ", response);
			    if(response.data.status === 200) {
				    cogoToast.success("Audio successfully uploaded!", {position: 'top-right'});
			    }
		    }).catch(error => {
			    console.log("Sound upload error: ", error);
		    });
        });
        console.log(audioFile);     
    }
 
     /**
      * Resets the currently saved audio
      * 
      */
    handleReset() {
        const reset = {
            url: null,
            blob: null,
            chunks: null,
            duration: {
                h: null,
                m: null,
                s: null,
            }
        }
        this.setState({ audioDetails: reset });
    }
 
    render() {
        return (
            <Recorder
                record={true}
                title={'Record Audio'}
                audioURL={this.state.audioDetails.url}
                showUIAudio
                handleAudioStop={data => this.handleAudioStop(data)}
                handleOnChange={(value) => this.handleOnChange(value, 'firstname')}
                handleAudioUpload={data => this.handleAudioUpload(data)}
                handleRest={() => this.handleReset()}
            />
        );
    }
}
 
export default Rec;