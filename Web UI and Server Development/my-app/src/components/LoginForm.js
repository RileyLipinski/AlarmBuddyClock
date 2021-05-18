import React, { Component } from 'react';
import axios from 'axios';
import './LoginForm.css';
import cogoToast from 'cogo-toast';

//Login Form utilized in logging a user into our state management system
export default class LoginForm extends Component {

	constructor(props){
		super(props);
		
		this.state = {
			username: "",
			password: "",
		}		
		
		this.handleSubmit = this.handleSubmit.bind(this);
		this.handleChange = this.handleChange.bind(this);
	}
	
	//On user entry update the current state
	handleChange(event){
		this.setState({
			[event.target.name]: event.target.value
		})
	}
	
	//On user form submission update the state and post to the database
	handleSubmit(event){
		cogoToast.loading('Logging in...', {position: 'top-right'}).then(() => {	
			axios.post("https://alarmbuddy-312620.uc.r.appspot.com/login", {
				username: this.state.username,
				password: this.state.password
			}
			//{ withCredentials: true }
			).then(response => {
				console.log("res from login: ", response);
				if(response.status === 200) {
					//console.log(response.data.token);
					cogoToast.success("Logged in!", {position: 'top-right'});
					this.props.handleSuccessfulAuth(response.data,
					this.state.username,
					this.state.password);
				}	
			}).catch(error => {
				console.log("login error", error);
				cogoToast.error("Error Logging in", {position: 'top-right'});
			});
		});
		event.preventDefault();
		
	}

	//return HTML
	render() {
		return (<div className='pad'>
			<form onSubmit={this.handleSubmit}>
				<h1> Login </h1>
				
				<label>
				Username: 
				<input 
					type="username"
					name="username"
					placeholder="Username"
					value={this.state.username}
					onChange={this.handleChange}
					required
				/>
				</label>

				<label>
				Password:
				<input 
					type="password"
					name="password"
					placeholder="Password"
					value={this.state.password}
					onChange={this.handleChange}
					required
				/>
				</label>
				<button type="submit">Login</button>
			</form>
		</div>);
	}	
}