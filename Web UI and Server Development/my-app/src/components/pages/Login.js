/**
 * This class renders the login page of the website which
 * contains the LoginBox class
 */

import React, {Component} from 'react';
import '../../App.css';
import LoginForm from '../LoginForm';

export default class Login extends Component { 
	constructor(props){
		super(props);
		
		this.handleSuccessfulAuth = this.handleSuccessfulAuth.bind(this);
	}
	
	handleSuccessfulAuth(data) {
		this.props.handleLogin(data);
		this.props.history.push('/dashboard');
	}
	
    render() {
      return (
		<div>
			<LoginForm handleSuccessfulAuth={this.handleSuccessfulAuth} />
		</div>
      );
    }
}


