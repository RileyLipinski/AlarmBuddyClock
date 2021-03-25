import React from 'react';
import '../../App.css';
import Popup from '../Popup';  
import FileUpload from '../FileUpload';

export default class Friends extends React.Component {
  state = {
   seen: false
   };
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
      <FileUpload />
   </div>
   
  );
 }
}

  
