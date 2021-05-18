import React from 'react';
import './UserProfile.css';
import axios from 'axios';

//User Profile Component for /profile page
export default class UserProfile extends React.Component {
	constructor(props){
		super(props)
		
		this.state = {
			birth_Date: "",
			creation_Date: "",
			email: "",
			first_Name: "",
			last_Name: "",
			password: "",
			phone_Number: "",
			username: "",
			Image: "",
			selectedFile: null
		}
		
		this.getUserInformation = this.getUserInformation.bind(this);
		this.getProfileImage = this.getProfileImage.bind(this);
		this.updateProfile = this.updateProfile.bind(this);
		this.imageEncode = this.imageEncode.bind(this);
		this.changeProfilePic = this.changeProfilePic.bind(this);
	}
	
	//Change the selectedFile state to an input file
	onFileChange = event => {	 
	   // Update the state
	   this.setState({selectedFile: event.target.files[0] }); 
	};
	
	//Get current user specific information
	getUserInformation(){
		axios.get("https://alarmbuddy-312620.uc.r.appspot.com/users/"
			+ this.props.username, {
			headers: { 
				'Authorization': this.props.token,
			}
		}
			).then(response => {

				if(response.status === 200) {
					this.updateProfile(response.data[0]);
									}	
			}).catch(error => {
				console.log("Profile Load Error", error);
		});
	}
	
	//Update the userProfile State
	updateProfile(data){
		this.setState({
			birth_Date: data.birth_Date,
			creation_Date: data.creation_Date,
			email: data.email,
			first_Name: data.first_Name,
			last_Name: data.last_Name,
			password: data.password,
			phone_Number: data.phone_Number,
			username: data.username
		});
	}
	
	//Turn an arrayBuffer into a base64 image
	imageEncode(arrayBuffer){
		let u8 = new Uint8Array(arrayBuffer)
		let b64encoded = btoa([].reduce.call(new Uint8Array(arrayBuffer),function(p,c){return p+String.fromCharCode(c)},''))
		let mimetype="image/jpeg"
		return "data:"+mimetype+";base64,"+b64encoded
	}
	
	//Get current user profile image
	getProfileImage(){
		axios.get("https://alarmbuddy-312620.uc.r.appspot.com/getProfilePicture"
			+ "/" + this.props.username + "/" + this.props.username, {
			responseType: 'arraybuffer',
			headers: { 			
				'Authorization': this.props.token,
			}
		}
			).then(response => {

				if(response.status === 200) {
					this.setState({
						Image: this.imageEncode(response.data)
					})
				}	
			}).catch(error => {
				console.log("Profile Image Load Error", error);
		});
	}
	
	//Change the current users profile image
	changeProfilePic(event){
		const formData = new FormData();

		// Update the formData object
		formData.append(
			'file',
			this.state.selectedFile
		);
		
		axios.post("https://alarmbuddy-312620.uc.r.appspot.com/setProfilePicture" + 
			"/" + this.props.username, formData, {
				headers: { 
					'Authorization': this.props.token,
					'content-type': 'Image/*'
				}
			}).then(response => {
				console.log("Image upload res: ", response);
			    if(response.status === 201) {
				    console.log("Image successfully uploaded!", {position: 'top-right'});
			    }
		    }).catch(error => {
			    console.log("Image upload error: ", error);
		    });
			event.preventDefault();
	}
	
	//On page load --> Call these functions
	componentDidMount() {
		this.getUserInformation();
		this.getProfileImage();
	}
	
	//Return HTML
	render() {
		return (

			<div className='center'>
				<meta charset="utf-8"/>
			 	<form class='ProfileBox'>
					<h1>User Profile</h1>
					
					<div class='ProfileContent'>
						<section class='ProfileImg'>
							<h1>PROFILE PICTURE</h1>  
							<img src={this.state.Image} alt="Profile Picture"/>
							<div class='ProfileChange'>
								<input type="file" onChange={this.onFileChange} accept="image/*" />
								<button onClick={this.changeProfilePic}>
									Change Profile
								</button>
							</div>
						</section> 	

						<section class='userInformation'>
							<h1>USER INFORMATION</h1>
							<p>First Name: {this.state.first_Name}</p>
							<p>Last Name: {this.state.last_Name}</p>
							<p>Username: {this.state.username}</p>
							<p>Phone Number: {this.state.phone_Number}</p>
							<p>Email: {this.state.email}</p>
							<p>Creation Date: {this.state.creation_Date}</p>

						</section>
					</div>
				</form>
			</div>
		);
	}
}