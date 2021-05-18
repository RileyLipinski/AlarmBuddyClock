/*
This react componenet deals with all of the friend 
functionality. It also displays friends and alerts.
*/
import React, {Component} from 'react';
import axios from 'axios';
import cogoToast from 'cogo-toast';
import { checkPropTypes } from 'prop-types';
import "./FriendsComp.css"; 


export default class FriendsComp extends Component{

    constructor(props){
        super(props);
        
        this.state = {
            url: 'https://alarmbuddy-312620.uc.r.appspot.com/friendsWith/' + this.props.token.username,
            url2: 'https://alarmbuddy-312620.uc.r.appspot.com/requests/' + this.props.token.username,
            url3: 'https://alarmbuddy-312620.uc.r.appspot.com/getBlockList/' + this.props.token.username,
            userName: this.props.token.username,
            friends: [],
            requests: [],
            friendsList: [],
            blocks: [],
            friendSearch: '',
            
        }

        this.handleSubmit = this.handleSubmit.bind(this);
		    this.handleChange = this.handleChange.bind(this);
        this.remFriend = this.remFriend.bind(this);
        this.addFriend = this.addFriend.bind(this);
        this.denyReq = this.denyReq.bind(this);
        this.blockReq = this.blockReq.bind(this);
        this.unblock = this.unblock.bind(this);
    }

    //Calls and initializes the users friends, Blocks and requests
    componentDidMount() {
        this.interval = setInterval(() => this.setState({ time: Date.now() }), 1000);
        axios.get(this.state.url, {
		    headers: { 'Authorization': this.props.token.token,},
	    }).then(response => {
            console.log("sound download res: ", response);
            if(response.status === 200) {
                this.state.friends = response.data;
                this.printFriends()
                
                

            }
        }).catch(error => {
            console.log("Error Getting Friends ", error);
        });
        
        
        axios.get(this.state.url2, {
		    headers: { 'Authorization': this.props.token.token,},
	    }).then(response => {
            console.log("sound download res: ", response);
            if(response.status === 200) {
                this.state.requests = response.data;
                this.printRequests()
                
                

            }
        }).catch(error => {
            console.log("Error Getting Friends ", error);
        });
        
        
        axios.get(this.state.url3, {
          headers: { 'Authorization': this.props.token.token,},
        }).then(response => {
              console.log("blocked friends", response);
              if(response.status === 201) {
                  this.state.blocks = response.data;
                  
                  
                  this.printBlocks()
                  
                  
  
              }
          }).catch(error => {
              console.log("Error Getting Friends ", error);
          });
        this.printFriends()
        this.printRequests()
        this.printBlocks()
    }

    componentWillUnmount() {
        clearInterval(this.interval);
      }

    //prints friends and allows to remove friends
    printFriends(){
        
        
        return (
            <div>
               
              {this.state.friends.map((person, index) => (
                <div className='requests'><p key={index}>{person.username2}</p>
                <button onClick = {this.remFriend} data1 = {person.username2}>Remove Friend</button>
                </div>
               
              ))}
            </div>
        )
  

      }
      //prints the blocked users that a person has
      printBlocks(){
        
        
        return (
            <div>
               
              {this.state.blocks.map((person, index) => (
                <div className='requests'><p key={index}>{person.blocked}</p>
                  <button onClick = {this.unblock} data5 = {person.blocked}>Unblock</button>
                </div>
                
              ))}
            </div>
        )
  

      }
      //prints friend requests
      printRequests(){
        return (
            <div>
             
              {this.state.requests.map((person, index) => (
                <div><p key={index}>{person.senderUsername}</p>
                <button onClick = {this.addFriend} data2 = {person.senderUsername}>Add Friend</button>
                <button onClick = {this.denyReq} data3 = {person.senderUsername}>Deny Request</button>
                <button onClick = {this.blockReq} data4 = {person.senderUsername}>Block</button>
                
                </div>
                //<form><label for="fname" key={index}><h1>{person.username2}</h2></label><button onClick = {this.remFriend(person.username2)}>Remove Friend</button></form>
              ))}
            </div>
        )
      }
      //axios delete request to remove a friend
      remFriend(event){
          let friendRemoval = event.target.getAttribute('data1');
          cogoToast.loading('Removing Friend...', {position: 'top-right'}).then(() => {
            
            axios.delete("https://alarmbuddy-312620.uc.r.appspot.com/deleteFriend/" + this.props.token.username + '/' + friendRemoval ,
			   {
				headers: { 
					'Authorization': this.props.token.token,
					
				 },
	   })
			
			.then(response => {
				
				if(response.status === 201) {
					
					cogoToast.success("Removed", {position: 'top-right'});
          this.forceUpdate();
				}	
			}).catch(error => {
				console.log("Removal Error", error);
				cogoToast.error("Removal Error", {position: 'top-right'});
			});
		});
        
		    event.preventDefault();
      }
      //adding friend function that handles adding a friend
      addFriend(event){
        let friendAdd = event.target.getAttribute('data2');
        cogoToast.loading('Accepting Friend Request...', {position: 'top-right'}).then(() => {
          
          axios.post("https://alarmbuddy-312620.uc.r.appspot.com/acceptFriendRequest/" + this.props.token.username + '/' + friendAdd ,
            '', {
              headers: { 
                  'Authorization': this.props.token.token,
                  
               },
     })
         
          .then(response => {
              
              if(response.status === 201) {
                 
                  cogoToast.success("Removed", {position: 'top-right'});
              }	
          }).catch(error => {
             
              cogoToast.error("Adding Error", {position: 'top-right'});
          });
      });
      this.setState({ state: this.state });
      event.preventDefault();
      
    }

     

    

      handleChange(event){
		this.setState({
			[event.target.name]: event.target.value
		})
	}
  //function to deny a friend request
  denyReq(event){
    let friendAdd = event.target.getAttribute('data3');
    cogoToast.loading('Denying Friend Request...', {position: 'top-right'}).then(() => {
      
      axios.post("https://alarmbuddy-312620.uc.r.appspot.com/denyFriendRequest/" + this.props.token.username + '/' + friendAdd ,
        '', {
          headers: { 
              'Authorization': this.props.token.token,
              
           },
      })
      //{ withCredentials: true }
      .then(response => {
          
          if(response.status === 201) {
              //console.log(response.data.token);
              cogoToast.success("Denied", {position: 'top-right'});
          }	
      }).catch(error => {
          console.log("Error Denying", error);
          cogoToast.error("Denying Error", {position: 'top-right'});
      });
  });
  
  event.preventDefault();
  
  
}

//function to handle blocking a user
blockReq(event){
  let friendAdd = event.target.getAttribute('data4');
  cogoToast.loading('Blocking User...', {position: 'top-right'}).then(() => {
    
    axios.post("https://alarmbuddy-312620.uc.r.appspot.com/blockUser/" + this.props.token.username + '/' + friendAdd ,
      '', {
        headers: { 
            'Authorization': this.props.token.token,   
        },
    }).then(response => {
        if(response.status === 201) {
            //console.log(response.data.token);
            cogoToast.success("Blocked", {position: 'top-right'});
            axios.post("https://alarmbuddy-312620.uc.r.appspot.com/denyFriendRequest/" + this.props.token.username + '/' + friendAdd ,
            '', {
              headers: { 
                  'Authorization': this.props.token.token,         
              },
     })
        }	
    }).catch(error => {
        console.log("Adding Error", error);
        cogoToast.error("Error blocking", {position: 'top-right'});
    });
    this.setState({ state: this.state });
});
this.setState({ state: this.state });
event.preventDefault();

}
    //function to handle unblocking a user
    unblock(event){
      let friendAdd = event.target.getAttribute('data5');
      cogoToast.loading('Unblocking User...', {position: 'top-right'}).then(() => {
      
      axios.post("https://alarmbuddy-312620.uc.r.appspot.com/unblockUser/" + this.props.token.username + '/' + friendAdd ,
        '', {
        headers: { 
            'Authorization': this.props.token.token,
              
          },
      }).then(response => {
          
          if(response.status === 201) {
              
              cogoToast.success("Unblocked", {position: 'top-right'});
          }	
      }).catch(error => {
          console.log("Unblocking Error", error);
          cogoToast.error("Unblocking Error", {position: 'top-right'});
      });
  });
  this.setState({ state: this.state });
  event.preventDefault();

    }
    //axios post request to send a friend request
    handleSubmit(event){
        cogoToast.loading('Adding Friend...', {position: 'top-right'}).then(() => {
            console.log(this.props.token.token);
            axios.post("https://alarmbuddy-312620.uc.r.appspot.com/sendRequest/" + this.props.token.username + '/' + this.state.friendSearch ,
			  '', {
				headers: { 
					'Authorization': this.props.token.token,
					
				 },
	   }).then(response => {
				if(response.status === 201) {
					//console.log(response.data.token);
					cogoToast.success("Added", {position: 'top-right'});
				}	
			}).catch(error => {
				console.log("Adding Error", error);
				cogoToast.error("Adding Error", {position: 'top-right'});
			});
		});
		event.preventDefault();
    }
    //render method 
    render (){
        return(
   
           <div >
                 
                <div>
                <form class="colform" onSubmit={this.handleSubmit}>
				<h1> Add Friends </h1>
				
				<label>
				Search for friends:
				<input 
					type="friendSearch"
					name="friendSearch"
					placeholder="Add Friend..."
					value={this.state.friendSearch}
					onChange={this.handleChange}
					required
				/>
				</label>
                <button type="submit">Add</button>
                
			</form>
	<form class="colform">
	<h1> Your Friends </h1>

            {this.printFriends()}
	</form>
	<form class="colform">
	<h1> Friend Requests </h1>

            {this.printRequests()}
	</form>
  <form class="colform">
	<h1> Blocked Users </h1>

            {this.printBlocks()}
	</form>
            </div>
                
           </div>
        )
    }
}