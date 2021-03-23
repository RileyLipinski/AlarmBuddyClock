require('dotenv').config()
var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var connection = require('./database');
var bcrypt = require('bcryptjs');
var fs = require('fs'); 
var multer  = require('multer');
var upload = multer({ dest: 'uploads/' })

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


app.post('/newUser', (req, res)=>{
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
  //  res.status(500).send('error:username already used');
  //if username is unique
  //also need to make sure that their username hits minimum requirements..

  //HASH PASSWORD HERE


  //"INSERT INTO users (username, password, first_Name, last_Name, email, phone_Number,creation_Date, birth_Date
  // VALUES
  // (?,?,?,?,?,?,?,?)", [chosenUsername,HASHED PASSWORD HERE,firstName,lastName,email,phoneNumber,creationDateTimestamp,birthdate]
  

});



app.route('/friendWith/:userId')
//this just take in the UserId and returns all their information from the GoogleDB
  .get(function(req, res, next) {
    connection.query(
      "SELECT friendsWithID FROM alarmbuddy.friendsWith WHERE userID = ?", req.params.userId,
      function(error, results, fields) {
        if (error) throw error;
        res.json(results);
      }
    );
  });


app.get('/passwordAuthentication', (req,res)=>{
  var submittedUsername = req.body.username;
  //This should be cleartext?
  var passwordUnhashed = req.body.password;
  //maybe I need to async the hash part...
  var salt = submittedUsername.substring(2,4);
  var hash = bcrypt.hashSync(passwordUnhashed,salt);
  var queryResults;
  //this is pulling the password from that user
  connection.query("SELECT password FROM alarmbuddy.users WHERE username = ?", submittedUsername),
  function(error, results, fields){
    if(error) throw error;
    queryResults = results;
  }
  //need to have check here in case if results are blank meaning user doesnt exist
  if(hash == queryResults){
    res.status(200).send("User authenticated sucessfully");
    //Probably shoudl send an auth token here
  }else{
    res.status(403).send("Incorrect Username Or Password");
  }
  //unsure if there needs to be some different way of saying if correct or not.


})
//This is the old one table sound file
// app.post('/upload/:userID', upload.single('file'), function (req, res, next) {
//   var userID = req.params.userID;

//   var soundName = req.file.originalname;
//   var img = fs.readFileSync(req.file.path);

//   console.log(req.file);

//   var soundEntry = [
//     [userID,soundName,img]
//   ];

//   if (req.file.mimetype == "application/octet-stream"){
//     connection.query("INSERT INTO alarmbuddy.sounds (soundOwner, soundName, soundFile) VALUES ?", [soundEntry], function(error, results, field){
//       if(error) {
//         throw error;
//       }else{
//         fs.unlinkSync(req.file.path);
//         res.status(201).send('database updated sucessfully');
//       }
//     })
//   } else {
//     //not a mp3 file
//     fs.unlinkSync(req.file.path);
//   }

// })


app.route('/download/:soundName').get(function(req,res,next) {

  connection.query("SELECT soundFile FROM alarmbuddy.sounds WHERE soundName = ?", req.params.soundName, function(error, results, fields) {
    if (error) throw error;
      fs.writeFile('/tmp/' + req.params.soundName, results[0].soundFile, function (err) {
        if (err) throw err;
        res.sendFile('/tmp/' + req.params.soundName, (err) => {
          fs.unlinkSync('/tmp/' + req.params.soundName);
        });
      });
  });
});


app.post('/uploadSound', (req, res) =>{
  var userID = req.params.userID;
  var soundName = req.params.soundName;
  var soundFile = req.params.soundFile;

  //multer or  express-fileupload here



  //May need to do something with the results?
  connection.query("INSERT INTO sounds (soundOwner, soundName, soundFile VALUES (?,?,?)", [userID,soundName,soundFile]), function(error, results, field){
    if(error) {
      throw error;
    }else{
      res.status(201).send('database updated sucessfully');
    }
  }
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
//port 3000 i guess if youre doing local host 
app.set('port', process.env.PORT || 8080);
app.listen(8080);