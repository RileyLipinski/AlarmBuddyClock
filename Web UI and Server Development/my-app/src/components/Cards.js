import React from 'react';
import './Cards.css';

const Card = (props) => (
  <div className="card">
    <img src={ props.img } 
      alt={ props.alt || 'Image' } />
    <div className="card-content">
      <h2>{ props.title }</h2>
      <p>{ props.content }</p>
    </div>
  </div>
);

const CardContainer = (props) => (
  <div className="cards-container"> {
      props.cards.map((card) => (
        <Card title={ card.title }
          content={ card.content }
          img={ card.img }
        />
      ))
    }
  </div>
);

class Cards extends React.Component {
  render () {
    const cardsData = [
      {id: 1, title: 'Friends', content: 'Add your friends to your list of people!', img: 'images/smilefaces.jpg'},
      {id: 2, title: 'Options', content: 'Customize all of your alarms with various jingles!', img: 'images/jingles.jpg'},
      {id: 3, title: 'Alert', content: 'Wake up to a suprise alarm your friends send to you!', img: 'images/wakeup.jpg'},
      {id: 4, title: 'Account', content: 'Manage your alarms', img: 'images/alarms.jpg'},
      {id: 5, title: 'Account', content: 'Manage your profile', img: 'images/profile.jpg'},
    ]
    
    return(
      <div className="container">
        <h1>Alarm Buddy Features </h1>
        <CardContainer cards={ cardsData } />
      </div>
    );
  }
}
export default Cards;