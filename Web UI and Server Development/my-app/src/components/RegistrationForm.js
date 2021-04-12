



import React, { Component } from 'react';
import axios from 'axios';
import './RegistrationForm.css';

export default class RegistrationForm extends Component {
	constructor(props){
		super(props);
		
		this.state = {
			username: "",
			password: "",
			firstName: "",
			lastName: "",
			email: "",
			phoneNumber: "",
			birthDate: "",
			registrationErrors: ""
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
		axios.post("https://alarmbuddy.wm.r.appspot.com/register", {
			username: this.state.username,
			password: this.state.password,
			firstName: this.state.firstName,
			lastName: this.state.lastName,
			email: this.state.email,
			phoneNumber: this.state.phoneNumber,
			birthDate: this.state.birthDate
		}
		//{ withCredentials: true }
		).then(response => {
			console.log("registration res", response);
			if(response.status === 200) {
				this.props.handleSuccessfulAuth(response.data);
			}
		}).catch(error => {
			console.log("registration error", error);
		});
				
		event.preventDefault();
	}
		
	render() {
		return (
			<div className='center'>
				<form onSubmit={this.handleSubmit}>
					<h1> Create Account</h1>
					
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

					<label>
					First Name:
					<input 
						type="firstName"
						name="firstName"
						placeholder="First Name"
						value={this.state.firstName}
						onChange={this.handleChange}
						required
					/>
					</label>

					<label>
					Last Name:
					<input 
						type="lastName"
						name="lastName"
						placeholder="Last Name"
						value={this.state.lastName}
						onChange={this.handleChange}
						required
					/>
					</label>

					<label>
					Email:
					<input 
						type="email"
						name="email"
						placeholder="Email"
						value={this.state.email}
						onChange={this.handleChange}
						required
					/>
					</label>


					<label>
					Phone Number:
					<input 
						type="phoneNumber"
						name="phoneNumber"
						placeholder="Phone Number"
						value={this.state.phoneNumber}
						onChange={this.handleChange}
						required
					/>
					</label>
					
					<label>
					Birthdate:
					<input 
						type="birthDate"
						name="birthDate"
						placeholder="Birth Date (I.E. 1998-10-27)"
						value={this.state.birthDate}
						onChange={this.handleChange}
						required
					/>
					</label>
					<button type="submit">Register</button>
				</form>
			</div>
		);
	}	
}