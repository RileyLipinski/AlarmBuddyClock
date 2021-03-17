require('dotenv').config()
var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var connection = require('./database');
var bcrypt = require('bcryptjs');

app.route('/users/:userId')
//this just take in the UserId and returns all their information from the GoogleDB
  .get(function(req, res, next) {
    connection.query(
      "SELECT * FROM alarmbuddy.users WHERE username = ?", req.params.userId,
      function(error, results, fields) {
        if (error) throw error;
        res.json(results);
      }
    );
  });


app.put('/newUser', (req, res)=>{
//Need to be able to pull all the different params passed into this one
  var chosenUsername = req.body.username;
  //This should be cleartext?
  var passwordUnhashed = req.body.password;
  var firstName = req.body.firstName;
  var lastName = req.body.lastName;
  var email = req.body.email;
  var phoneNumber = req.body.phoneNumber;
  var creationDateTimestamp = Date.now;
  var birthdate = req.body.birthDate;
  //need to check to make sure username is unique, check for users with that username, and if there is one, fail the whole process.
  //connection.query()

  var salt = chosenUsername.substring(2,4);
  var hash = bcrypt.hashSync(passwordUnhashed,salt);
  console.log(hash);
  
  //if username is unique
  //also need to make sure that their username hits minimum requirements..

  //HASH PASSWORD HERE


  //"INSERT INTO users (username, password, first_Name, last_Name, email, phone_Number,creation_Date, birth_Date
  // VALUES
  // (?,?,?,?,?,?,?,?)", [chosenUsername,HASHED PASSWORD HERE,firstName,lastName,email,phoneNumber,creationDateTimestamp,birthdate]
  

});


app.get('/friendsWith', (req, res)=>{

  var primaryUser = req.body.primaryUser;
  connection.query("SELECT friendsWithID FROM alarmbuddy.friendsWith WHERE userID = ?", primaryUser),
    function(error, results, fields){
      if (error) throw error;
      res.json(results);
    };
});




//This is for grabbing sounds for a specific user (needs to be stress tested)

app.route('/sounds/:userId')
  .get(function(req,res,next){
    connection.query(
      "SELECT * FROM alarmbuddy.sounds WHERE soundOwner = ?", req.params.userId,
      function(error,results,fields){
        if (error) throw error;
        res.json(results);
      }
    );
  });


app.get('/status', (req, res) => res.send('Working!'));

//Port 8080 for Google App Engine
app.set('port', process.env.PORT || 3000);
app.listen(3000);