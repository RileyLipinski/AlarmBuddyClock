/**
 * This function renders the home page for the website
 * which displays the alarm clock with the cards section
 * below it
 */

import React from 'react';

import TopSection from '../TopSection';
//import RegistrationForm from '../RegistrationForm';
import Cards from '../Cards';


function Home() {
  return (
    <>
      <TopSection />
      <Cards />
    </>
  );
}

export default Home;

/*
export default class Home extends Component {
	constructor(props){
		super(props);
		
		this.handleSuccessfulAuth = this.handleSuccessfulAuth.bind(this);
	}
	
	handleSuccessfulAuth(data) {
		this.props.handleLogin(data);
		this.props.history.push("/Dashboard");
	}
	
	render() {
		return (
			<>
				<h1>Status: {this.props.loggedInStatus}</h1>
				<RegistrationForm handleSuccessfulAuth={this.handleSuccessfulAuth} />
				<TopSection />			
			</>
		);
	}
}

*/ 