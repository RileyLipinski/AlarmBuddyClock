import React from 'react';
import './Cards.css';
import CardItem from './CardItem';

function Cards() {
  return (
    <div className='cards'>
      <h1>Lets Set Alarms!</h1>
      <div className='cards__container'>
        <div className='cards__wrapper'>
          <ul className='cards__items'>
            <CardItem
              src='images/smilefaces.jpg'
              text='Add friends to your list of people!'
              label='Community'
              path='/Login'
            />
            <CardItem
              src='images/jingles.jpg'
              text='Customize your alarms with various jingles!'
              label='Create'
              path='/Login'
            />
          </ul>
          <ul className='cards__items'>
            <CardItem
              src='images/profile.jpg'
              text='Manage your profile'
              label='Profile'
              path='/Login'
            />
            <CardItem
              src='images/alarms.jpg'
              text='Manage your alarms'
              label='Alert'
              path='/Login'
            />
            <CardItem
              src='images/wakeup.jpg'
              text='Wake up to a suprise with alarm sounds that your friends send you! '
              label='Adrenaline'
              path='/Login'
            />
          </ul>
        </div>
      </div>
    </div>
  );
}

export default Cards;