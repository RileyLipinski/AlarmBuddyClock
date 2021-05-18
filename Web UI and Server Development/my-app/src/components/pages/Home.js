/**
 * This function renders the home page for the website
 * which displays the alarm clock with the cards section
 * below it
 */

import React from 'react';

//import RegistrationForm from '../RegistrationForm';
import Cards from '../Cards';
import TopSection from '../TopSection'; 



function Home() {

 

  return (
    <>
	<TopSection />
	       <Cards />
	
    </>
  );
}

export default Home;
