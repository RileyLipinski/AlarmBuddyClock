import React from 'react';
import '../../App.css';
import UserProfile from '../UserProfile';

export default class Profile extends React.Component {
	constructor(props){
		super(props);
	}
	render() {
		return (
			<div>
				<UserProfile
				username={this.props.username}
				password={this.props.password}
				token={this.props.token}
				/>
			</div>
		);
	}
}