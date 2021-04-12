/**
 * This class renders the friends page of the website which
 * currently contains a popup for audio recording and file uploading
 */

import React from 'react';
import '../../App.css';
import Popup from '../Popup';  
import FileUpload from '../FileUpload';

export default class Friends extends React.Component {
	constructor(props) {
	super(props);  
	this.state = {
 	   seen: false,
	token: this.props
  };
}

  togglePop = () => {
    this.setState({
      seen: !this.state.seen
    });
  };

  render() {
    return (
      <div>
        <button className='btn' onClick={this.togglePop}>Record audio!</button>
        {this.state.seen ? <Popup toggle={this.togglePop} /> : null}
        <FileUpload token= {this.state.token} />
      </div>
    );
  }
}

  
