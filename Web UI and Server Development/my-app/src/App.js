
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
import Profile from './components/pages/Profile';
import axios from "axios";

export default class App extends Component {
	constructor() {
		super();
		
		this.state = {
			loggedInStatus: "NOT_LOGGED_IN",
			token: {},
			username: "",
			password: "",
			apiResponse: "",
			alarm: "",
			audioId: ""
		}
		
		this.handleLogin = this.handleLogin.bind(this);
		this.handleLogout = this.handleLogout.bind(this);
		this.setAlarm = this.setAlarm.bind(this);
		this.getAlarm = this.getAlarm.bind(this);
		this.setAudio = this.setAudio.bind(this);
		this.getAudio = this.getAudio.bind(this);
	}

	//Set alarm state
	setAlarm(time) {
		this.setState({
			alarm: time
		});
	}

	//Set audio state
	setAudio(number) {
		this.setState({
			audioId: number
		});
	}
	
	//Get state.audioID
	getAudio() {
		return this.state.audioId;
	}
	
	//Get state.alarm
	getAlarm() {
		return this.state.alarm;
	}
	
	//Test API Connection
	callAPI() {
		fetch("http://alarmbuddy.live:9000/testAPI2")
			.then(res => res.text())
			.then(res => this.setState({ apiResponse: res }));
	}
	
	//Check if user is currently logged in
	checkLoginStatus() {
		axios.post("https://alarmbuddy.wm.r.appspot.com/login", {
			username: this.state.username,
			password: this.state.password			
		}
		).then(response => {
			if(response.status === 200 && this.state.loggedInStatus === "NOT_LOGGED_IN"){
				this.setState({
					loggedInStatus: "LOGGED_IN",
					token: response.data.token,
					username: this.state.username,
					password: this.state.password
				});
			}
			console.log("logged in!", response);
		}).catch(error => {
			console.log("Check Login Error", error);
		});
	}
	
	//On website load --> connect to API
	componentWillMount() {
		this.callAPI();
	}
	
	//Update state with current user information	
	handleLogin(data, uName, pName) {
		this.setState({
			loggedInStatus: "LOGGED_IN",
			token: data.token,
			username: uName,
			password: pName
		});
	}
	
	//Remove current user information and update state
	handleLogout() {
		this.setState({
			loggedInStatus: "NOT_LOGGED_IN",
			token: {},
			username: "",
			password: ""
		});
		window.location='http://alarmbuddy.live/';
	}
	
	//return HTML
	render(){
		return (
		<div>
		  <Router>
			<Navbar
				alarm={this.state.alarm}
				getAlarm={this.getAlarm}
				getAudio={this.getAudio}
				loggedIn={this.state.loggedInStatus}
				token={this.state.token}
				username={this.state.username}
				handleLogout={this.handleLogout}
			/>
			<Switch>
			  <Route
					exact
					path={'/'}
					render={props => (
					<Home {...props}
					handleLogin={this.handleLogin}
					alarm={this.state.alarm}
					loggedInStatus={this.state.loggedInStatus} />)}
			  />		  
			  <Route
				  exact
				  path={'/friends'}
				  render={props => (
					<Friends
					{...props}
					handleLogin={this.handleLogin}
					loggedInStatus={this.state.loggedInStatus}
					alarm={this.state.alarm}
					token={this.state.token}
					username={this.state.username}
					/>
				  )}
			  />
			  <Route
				  exact
				  path={'/dashboard'}
				  render={props => (
					<Dashboard {...props}
					loggedInStatus={this.state.loggedInStatus}
					setAlarm={this.setAlarm}
					getAlarm={this.getAlarm}
					setAudio={this.setAudio}
					token={this.state.token}
					username={this.state.username}
					/>)}
			  />
			  
			  <Route
				  exact
				  path={'/sign-up'}
				  render={props => (
					<SignUp {...props}
					handleLogin={this.handleLogin}
					loggedInStatus={this.state.loggedInStatus} />)}
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
			  
			  <Route
				  exact
				  path={'/profile'}
				  render={props => (
					<Profile
					{...props}
					username={this.state.username}
					alarm={this.state.alarm}
					password={this.state.password}
					token={this.state.token}
					/>
				  )}
			  />
			  
			</Switch>
		  </Router>
		 <Footer /> 
		 
		</div>
		
		);
	}
}
