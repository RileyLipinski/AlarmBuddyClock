/**
 * This function renders the user page for the website
 * which displays the alarm clock currently
 */

import React from 'react';
import '../../App.css';
import Clock from '../Clock';


const Dashboard = props => {
	return (
    <>
		<h1>Status: {props.loggedInStatus}</h1>
		<Clock />
    </>
	);
}

export default Dashboard;

