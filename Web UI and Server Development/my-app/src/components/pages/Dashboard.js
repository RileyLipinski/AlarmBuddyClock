/**
 * This function renders the user page for the website
 * which displays the alarm clock and a popup for audio recording and file uploading currently
 */

import React from 'react';
//import '../../App.css';
import Clock from '../Clock'; 
import FileUpload from '../FileUpload';
import FileDownload from '../FileDownload';


export default class Dashboard extends React.Component {
	constructor(props) {
	super(props);  
	this.state = {
	    token: this.props
  };
}

  render() {
    return (
      <div>

        <form>
          <Clock token = {this.state.token}/>
          <FileUpload token= {this.state.token} />
	        <div style={{'textAlign' : 'center', 'marginTop' : '3rem'}}>
            <label for='sounds' style={{'fontWeight': 'bold', color : 'black', 'fontSize' : '1.5rem'}}>Your Sounds:</label>
        	  <div class='sounds'>
          		<select id='soundList'></select>
        	  </div>
        	  <p id='shared'></p>
	        </div>
        <FileDownload token = {this.state.token} />
	    </form> 
      </div>
    );
  }
}




