/**
 * This class renders the friends page of the website 
 */
 import React, {Component} from 'react';
import '../../App.css';
import FriendsComp from '../FriendsComp';



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
        <FriendsComp  token = {this.state.token}/>
             </div>
    );
  }
}

  
