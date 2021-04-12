import React, { Component } from 'react';
import curl from 'curl';

export default class RegistrationForm extends Component {
	render() {
		/*
		axios.get('https://alarmbuddy.wm.r.appspot.com/users/don', {
			headers: {
				'Authorization': 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6ImpvaG5ueSIsImlhdCI6MTYxNzg1MDMyMiwiZXhwIjoxNjE3OTM2NzIyfQ.PTifpWEtSLYx7T0Jow68_duzyNlVezF__I2NVCuK3uf'
			  }
		}).then(function (response) {
			console.log(response);
		})
		*/
		curl.get('https://alarmbuddy.wm.r.appspot.com/status',
			(function (error, response) {
				console.log(response);
				console.log(error);
			}));
	
		return (
		
			<div></div>
		)
	}
}