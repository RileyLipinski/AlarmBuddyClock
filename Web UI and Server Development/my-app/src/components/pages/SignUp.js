/**
 * This class renders the signup page of the website which
 * contains the SignupBox class
 */

import React, { Component } from "react";
import '../../App.css';
import RegistrationForm from '../RegistrationForm';

export default class SignUp extends Component {
	constructor(props){
		super(props);
		
		this.handleSuccessfulAuth = this.handleSuccessfulAuth.bind(this);
	}
	
	handleSuccessfulAuth(data) {
		this.props.handleLogin(data);
		this.props.history.push("/dashboard");
	}
	
    render() {
        return (
			<div>
				<h1>Status: {this.props.loggedInStatus}</h1>
				<RegistrationForm handleSuccessfulAuth={this.handleSuccessfulAuth}/>
            </div>          
        );
    }
}
