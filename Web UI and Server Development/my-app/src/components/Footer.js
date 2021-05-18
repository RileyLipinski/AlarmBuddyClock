/**
 * A simple React component that renders the footer information shown on each page of the website
 */

import React from 'react';
import './Footer.css';

function Footer() {
	
	//return HTML
	return (
  		<div className = "footer">
			<p> Copyright 2021 Students of  <a href="https://www.stthomas.edu/"> University of St. Thomas </a>CISC 480-D01, Spring 2021 </p>
			<p> <a href="https://opensource.org/licenses/MIT"> Licensed under MIT License </a> </p>
		</div>
	);
}

export default Footer;