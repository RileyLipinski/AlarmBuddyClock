/**
 * This React component records audio from an input mic.
 * The user can then upload the recorded audio to the server,
 * playback their audio, or reset the recorded audio.
 */
 import React from 'react';
 import {Recorder} from 'react-voice-recorder';
 import 'react-voice-recorder/dist/index.css';
 import cogoToast from 'cogo-toast';
 
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
     
     handleAudioStop(data) {
         this.setState({ audioDetails: data });
     }
 
     handleAudioUpload(file) {
         cogoToast.loading('Saving your audio...', {position: 'top-right'}).then(() => {
             cogoToast.success("Audio successfully uploaded!", {position: 'top-right'});
           });
         console.log(file);     
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
                 title={"Record Audio"}
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