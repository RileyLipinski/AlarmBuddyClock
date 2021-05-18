/**
 * This program access an already made react component to display 
 * a react carousel. 
 *
 */



import React, {Component} from 'react';
import './Cards.css';
import ReactCardCarousel from 'react-card-carousel';

class Cards extends Component {
  //image place 
  static get CONTAINER_STYLE() {
    return {
      position: "relative",
      height: "100vh",
      width: "100%",
      display: "flex",
      flex: 1,
      justifyContent: "center",
      alignItems: "middle"
    };
  }


 //displays the top and bottom blue sections of card
  static get CARD_STYLE() {
    return {
	
      height: "auto",
      width: "auto",
      paddingTop: "2rem",
	paddingBottom: "2rem",
      textAlign: "center",
      background: "#52C0F5",
      color: "#FFF",
      fontFamily: "sans-serif",
      fontSize: "1.5rem",
     
      borderRadius: "10px",
      boxSizing: "border-box"    
    };
  } 
 
  render() {
    return (
 <div style={Cards.CONTAINER_STYLE}>
      <ReactCardCarousel autoplay={ true } autoplay_speed={ 3000 }>
	<div style={ Cards.CARD_STYLE }>
	<h1> Friends </h1> 
	<p> Send custom alarms to your friends! </p> 
               <img src= "images/smilefaces.jpg" alt ="smiley" /> 
        </div>
        <div style={ Cards.CARD_STYLE }>
	<h1> Sounds </h1>
	<p> Create custom alarm sounds! </p>
              <img src= 'images/jingles.jpg' alt ="tunes" />
        
        </div>
        <div style={ Cards.CARD_STYLE }>
	<h1> Alert </h1>
	<p> Set alarms for anytime of the day! </p> 
           <img src= 'images/wakeup.jpg' alt ="wakeup" />
        
        </div>
        <div style={ Cards.CARD_STYLE }>
 	<h1> Account </h1>
	<p> Manage all of your alarms </p>
        <img src= 'images/alarms.jpg' alt ="manage" />
        
        </div>
        <div style={ Cards.CARD_STYLE }>
	<h1> Profile </h1>
	<p> See your list of friends </p> 
             <img src= 'images/profile.jpg' alt ="profile" />
       
        </div>
      </ReactCardCarousel>
  </div>
    );
  }
}
export default Cards; 