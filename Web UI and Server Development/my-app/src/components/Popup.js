import React, { Component } from 'react';
import Rec from './Rec';
import './Popup.css';

export default class Popup extends Component {
  handleClick = () => {
   this.props.toggle();
};

render() {
  return (
    <div className='modal'>
      <div className='modal_content'>
          <Rec />
      </div>
   </div>
  );
 }
}