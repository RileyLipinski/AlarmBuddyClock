
import React, { Component } from 'react';
import Navbar from './components/Navbar';
import './App.css';
import Home from './components/pages/Home';
import { BrowserRouter as Router, Switch, Route } from 'react-router-dom';
import Footer from './components/Footer';
import Friends from './components/pages/Friends';
import Dashboard from './components/pages/Dashboard';
import SignUp from './components/pages/SignUp';
import Login from './components/pages/Login'; 

export default class App extends Component {
	constructor() {
		super();
		
		this.state = {
			loggedInStatus: "NOT_LOGGED_IN",
			token: ""
		}
		
		this.handleLogin = this.handleLogin.bind(this);
	}
	
	handleLogin(data) {
		this.setState({
			loggedInStatus: "LOGGED_IN",
			token: data.token
		});
	}
	
	render(){
		return (
		<div>
		  <Router>
			<Navbar />
			<Switch>
			  <Route
					exact
					path={'/'}
					render={props => (
					<Home {...props} handleLogin={this.handleLogin} loggedInStatus={this.state.loggedInStatus} />)}
			  />
			  
			  <Route
				  exact
				  path={'/friends'}
				  render={props => (
					<Friends
					{...props}
					handleLogin={this.handleLogin}
					loggedInStatus={this.state.loggedInStatus}
					token={this.state.token}
					/>
				  )}
			  />
				
			  <Route
				  exact
				  path={'/dashboard'}
				  render={props => (
					<Dashboard {...props} loggedInStatus={this.state.loggedInStatus} />)}
			  />
			  
			  <Route
				  exact
				  path={'/sign-up'}
				  render={props => (
					<SignUp {...props} handleLogin={this.handleLogin} loggedInStatus={this.state.loggedInStatus} />)}
			  />
			  
			  <Route
				  exact
				  path={'/login'}
				  render={props => (
					<Login
					{...props}
					handleLogin={this.handleLogin}
					loggedInStatus={this.state.loggedInStatus} />
				  )}
			  />
			  
			</Switch>
		  </Router>
		 <Footer /> 

		</div>
		);
	}
}
