/**
 * This program accesses an already made React component
 * to accomplish file upload from the computer.
 * Only audio files are allowed to be uploaded
 */


 import axios from 'axios';
 
 import React,{Component} from 'react';
 import MicRecorder from 'mic-recorder-to-mp3';
 import cogoToast from 'cogo-toast';
 
 const Mp3Recorder = new MicRecorder({ bitRate: 128 });


 class App extends Component {
    constructor(props){
		super(props)
	
	 	this.state = {
	   		// Initially, no file is selected
			selectedFile: null,
			isRecording: false,
  			blobURL: '',
  			isBlocked: false,
			mp3Sound: null,
	 	};
		 this.onFileUpload = this.onFileUpload.bind(this);
		 this.start = this.start.bind(this);
		 
	}
	 // On file select (from the pop up)
	 onFileChange = event => {
	 
	   // Update the state
	   this.setState({ selectedFile: event.target.files[0] });
	   
	 
	 };
	 
	 // On file upload (click the upload button)
	 onFileUpload(event){
	   const formData = new FormData();
	 
	   // Update the formData object
	   formData.append(
		 'file',
		 this.state.selectedFile
	   );
	   // Details of the uploaded file
	   console.log(this.state.selectedFile);
	   console.log(formData);
	   console.log('username ' + this.props.token)
	   cogoToast.loading('Saving your audio...', {position: 'top-right'}).then(() => {
			axios.post("https://alarmbuddy-312620.uc.r.appspot.com/upload/" + 
			this.props.token.username, formData, {
				headers: { 
					'Authorization': this.props.token.token,
					'content-type': 'audio/mpeg',
				},
	   		}).then(response => {
			    console.log("sound upload res: ", response);
			    if(response.status === 201) {
				    cogoToast.success("Audio successfully uploaded!", {position: 'top-right'});
			    }
		    }).catch(error => {
			    console.log("Sound upload error: ", error);
		    });
		});
	   	event.preventDefault();
	 };

	 //This displayes the file details on the website
	 fileData = () => {
	 
	   if (this.state.selectedFile) {
		 return (
		   <div>
				<h2>File Details:</h2>
				<p>File Name: {this.state.selectedFile.name}</p>
				<p>File Type: {this.state.selectedFile.type}</p>
				<p>
			   		Last Modified:{" "}
			   		{this.state.selectedFile.lastModifiedDate.toDateString()}
			   	</p>
		   </div>
		 );
	   } else {
		 return (
		   <div>
			 	<br />
				<h4>Choose before Pressing the Upload button</h4>
		   </div>
		 );
	   }
	 };
	 //This handles the the start of recording
	 start(event){
		if (this.state.isBlocked) {
		  console.log('Permission Denied');
		} else {
		  Mp3Recorder
			.start()
			.then(() => {
			  this.setState({ isRecording: true });
			}).catch((e) => console.error(e));
		}
		cogoToast.info('Recording started!', {position: 'top-right'})
		event.preventDefault();
	  };
	  //this stops the recording
	  stop = () => {
		Mp3Recorder
		  .stop()
		  .getMp3()
		  .then(([buffer, blob]) => {
			const blobURL = URL.createObjectURL(blob)
			this.setState({ blobURL, isRecording: false });
		  }).catch((e) => console.log(e));
		  cogoToast.success('Audio ready for download', {position: 'top-right'})
	  };

	  


	 //renders the page
	 render() {
	   return (
		 <div>	
			<div style={{'textAlign' : 'center'}}>
            	<label for='rec' style={{'fontWeight': 'bold', color : 'black', 'fontSize' : '1.5rem'}}>Record a new alarm:</label>
				<br></br>
				<br></br>
				<audio id='rec' src={this.state.blobURL} controls="controls" />
				<button onClick={this.start} disabled={this.state.isRecording} >
  					Start recording
				</button>
				<button onClick={this.stop} disabled={!this.state.isRecording}>
 				 	Stop recording
				</button>
			</div>

			<h3 style={{'textAlign' : 'center', 'fontSize' : '1.5rem', 'marginTop' : '3rem'}}>
				Select a file to upload.
			</h3>
			<div style={{'textAlign' : 'center', 'fontSize' : '0.7rem'}}>
				<input type="file" onChange={this.onFileChange} accept="audio/*" />
				{this.fileData()}
				<button onClick={this.onFileUpload}>
					Upload!
				</button>
			</div>
		 </div>
	   );
	 }
   }
  
   export default App;
