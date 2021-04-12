import React, { Component } from 'react';
import axios from 'axios';
import './LoginForm.css';

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
	
	handleChange(event){
		this.setState({
			[event.target.name]: event.target.value
		})
	}
	
	handleSubmit(event){	
		axios.post("https://alarmbuddy.wm.r.appspot.com/login", {
			username: this.state.username,
			password: this.state.password
		}
		//{ withCredentials: true }
		).then(response => {
			console.log("res from login: ", response);
			if(response.status === 200) {
				//console.log(response.data.token);
				this.props.handleSuccessfulAuth(response.data);
			}	
		}).catch(error => {
			console.log("login error", error);
		});
		event.preventDefault();
	}



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