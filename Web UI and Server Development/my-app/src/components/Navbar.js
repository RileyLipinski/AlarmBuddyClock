/**
 * This React component renders the navbar on top of the webpages
 * that contains links to all the different webpages
 */

import React, { Component } from 'react';
import { Link } from 'react-router-dom';
import './Navbar.css';
import LogoutButton from './LogoutButton';
import Alarm from './Alarm';

export default class Navbar extends Component {
	
	//Return HTML
	render(){
		if(this.props.loggedIn === "LOGGED_IN"){
			return (
			<>
			  <nav className='navbar'>
				<div className='navbar-container'>
				  <Link to='/' className='navbar-logo'>
					AlarmBuddy 
				  </Link>
				  <div className='menu-icon'>
					<i className='fas fa-times fas fa-bars' />
				  </div>
				  
				  <ul className='nav-menu active nav-menu'>
					<li className='nav-item'>
					  <Link to='/' className='nav-links'>
						Home  
					  </Link>
					</li>

					<li className='nav-item'>
					  <Link to='/friends' className='nav-links'>
						Friends
					  </Link>
					</li>

					<li className='nav-item'>
					  <Link to='/dashboard' className='nav-links'>
						Dashboard
					  </Link>
					</li>
					
					<li className='nav-item'>
					  <Link to='/profile' className='nav-links'>
						Profile
					  </Link>
					</li>
					
					<li className='nav-item'>
						<LogoutButton
							handleLogout={this.props.handleLogout}
						/>
					</li>
				  </ul>
				  
				</div>
			  </nav>
			  <Alarm token = {this.props}/>
			</>
			);
		}
		else{
			return (
			<>
			  <nav className='navbar'>
				<div className='navbar-container'>
				  <Link to='/' className='navbar-logo'>
					AlarmBuddy 
				  </Link>
				  <div className='menu-icon'>
					<i className='fas fa-times fas fa-bars' />
				  </div>
				  
				  <ul className='nav-menu active nav-menu'>
					<li className='nav-item'>
					  <Link to='/sign-up' className='nav-links'>
						Sign Up
					  </Link>
					</li>

					<li className='nav-item'>
					  <Link to='/login' className='nav-links'>
						Login
					  </Link>
					</li>
					
				  </ul>
				  
				</div>
			  </nav>
			</>
			);
		}
	}	
}
