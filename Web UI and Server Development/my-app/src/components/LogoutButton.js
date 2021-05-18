import React, { Component } from 'react';

//Logout button component that is utilized in clearing the current user state
export default class LogoutButton extends Component{
	constructor(props){
		super(props);
		
		this.logoutUser = this.logoutUser.bind(this);
	}
	
	//App.js --> hangleLogout() prop
	logoutUser() {
		this.props.handleLogout();
	}
	
	//return HTML
	render(){
		return(
			<button onClick={this.logoutUser}>Logout</button>
		);
	}
}