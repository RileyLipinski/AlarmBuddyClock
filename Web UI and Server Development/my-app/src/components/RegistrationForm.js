



import React, { Component } from 'react';
import axios from 'axios';
import './RegistrationForm.css';
import cogoToast from 'cogo-toast';

//Registration Form Component for /sign-up page
export default class RegistrationForm extends Component {
	constructor(props) {
		super(props);
		
		this.state = {
			username: "",
			password: "",
			secondPassword: "",
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
	
	//On user category input change the state
	handleChange(event) {
		this.setState({
			[event.target.name]: event.target.value
		})
	}
	
	//On user submit button press initiate the post request
	handleSubmit(event) {
		//check for valid inputs
		let pass = true;
		let reason = "";

		//check username
		let valid = /^[A-Z|a-z|_|0-9]+$/;
		if(!this.state.username.match(valid)) {
			pass = false;
			reason = "Usernames can only contain _ and alphanumeric characters";
		}
		if(this.state.username.length < 5 || this.state.username.length > 20) {
			pass = false;
			reason = "Username length must be between 5 and 20 characters";
		}

		//check if password and re-entered password are the same
		if(this.state.password.localeCompare(this.state.secondPassword) !== 0) {
			pass = false;
			event.preventDefault();
			reason = "Password and Confirm Password do not match";
		}

		//check password
		valid = /^[<->|"|&]+$/;
		if(this.state.password.match(valid)) {
			pass = false;
			reason = "Passwords cannot contain <, =, >, &, or double quotations";
		}
		valid = /[A-Z]/;
		if(!valid.test(this.state.password)) {
			pass = false;
			reason = "Passwords must contain both upper and lower-case letters, a number, and a special character";
		}
		valid = /[a-z]/;
		if(!valid.test(this.state.password)) {
			pass = false;
			reason = "Passwords must contain both upper and lower-case letters, a number, and a special character";
		}
		valid = /[0-9]/;
		if(!valid.test(this.state.password)) {
			pass = false;
			reason = "Passwords must contain both upper and lower-case letters, a number, and a special character";
		}
		valid = /[!-/|:-@|{-~]/;
		if(!valid.test(this.state.password)) {
			pass = false;
			reason = "Passwords must contain both upper and lower-case letters, a number, and a special character";
		}
		if(this.state.password.length < 8 || this.state.password.length > 20) {
			pass = false;
			reason = "Password length must be between 8 and 20 characters";
		}

		//check first name
		valid = /^[\w|-|']+$/;
		if(!valid.test(this.state.firstName)) {
			pass = false;
			reason = "First names can only contain letters, hyphens, and apostrophes";
		}

		//check last name
		valid = /^[\w|-|']+$/;
		if(!valid.test(this.state.lastName)) {
			pass = false;
			reason = "Last names can only contain letters, hyphens, and apostrophes";
		}

		//check birth date (needs to be over 18)
		let today = new Date();
		let dd = String(today.getDate()).padStart(2, '0');
		let mm = String(today.getMonth() + 1).padStart(2, '0'); //January is 0!
		let yyyy = today.getFullYear();
		let birth;
		let temp = "";

		if(this.state.birthDate.includes("/")) {
			birth = this.state.birthDate.split("/");
			const ageYear = parseInt(yyyy, 10) - parseInt(birth[2], 10);
			if(ageYear <= 18) {
				pass = false;
				reason = "You have to be over the age of 18 to use AlarmBuddy";
				console.log(parseInt(mm, 10));
				if(parseInt(mm, 10) === parseInt(birth[0], 10) && ageYear === 18) {
					if(parseInt(dd, 10) >= parseInt(birth[1], 10)) {
						pass = true;
						reason = "";
					}
				} else if(parseInt(mm, 10) > parseInt(birth[0], 10) && ageYear === 18) {
					pass = true;
					reason = "";
				}	
			}
			temp = birth[2] + "-" + birth[0] + "-" + birth[1];
		} else if(this.state.birthDate.includes("-")) {
			birth = this.state.birthDate.split("-");
			const ageYear = parseInt(yyyy, 10) - parseInt(birth[2], 10);
			if(ageYear <= 18) {
				pass = false;
				reason = "You have to be over the age of 18 to use AlarmBuddy";
				if(parseInt(mm, 10) === parseInt(birth[0], 10) && ageYear === 18) {
					if(parseInt(dd, 10) >= parseInt(birth[1], 10)) {
						pass = true;
						reason = "";
					}
				} else if(parseInt(mm, 10) > parseInt(birth[0], 10) && ageYear === 18) {
					pass = true;
					reason = "";
				}	
			}
			temp = birth[2] + "-" + birth[0] + "-" + birth[1];
		} else {
			pass = false;
			reason = "Birth date must be in one of the two provided forms: mm/dd/yyyy or mm-dd-yyyy"
		}
		
		console.log(temp);
		if(pass) {
			axios.post("https://alarmbuddy-312620.uc.r.appspot.com/register", {
				username: this.state.username,
				password: this.state.password,
				firstName: this.state.firstName,
				lastName: this.state.lastName,
				email: this.state.email,
				phoneNumber: this.state.phoneNumber,
				birthDate: temp
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
		}

		if(!pass) {
			cogoToast.error(reason, {position: 'top-center', hideAfter: 8, heading: 'Registration Error:'});
		}
		event.preventDefault();
	}
		
	//Return HTML
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
					Confirm Password:
					<input 
						type="password"
						name="secondPassword"
						placeholder="Re-enter Password"
						value={this.state.secondPassword}
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
						placeholder="Birth Date (I.E. 10-27-1998 or 10/27/1998)"
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