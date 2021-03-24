require('dotenv').config()
var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var connection = require('./database');
var bcrypt = require('bcryptjs');
var fs = require('fs'); 
var multer  = require('multer');
var upload = multer({ dest: 'uploads/' })
app.use(express.json());
app.use(express.urlencoded({
  extended: true
}));

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

    var chosenUsername = req.body.username;
  
    connection.query("SELECT username FROM alarmbuddy.users WHERE username = ?", chosenUsername, function(error, results, fields) {
        if (error) throw error;
        if (JSON.stringify(results) == JSON.stringify([])){
          var passwordUnhashed = req.body.password;
          var salt = bcrypt.genSaltSync(10);
          var passwordHashed = bcrypt.hashSync(passwordUnhashed,salt);
  
  
          var firstName = req.body.firstName;
          var lastName = req.body.lastName;
          var email = req.body.email;
          var phoneNumber = req.body.phoneNumber;
  
          let ts = Date.now();
          let date_ob = new Date(ts);
          let date = date_ob.getDate();
          let month = date_ob.getMonth() + 1;
          let year = date_ob.getFullYear();
  
          var creationDateTimestamp = year + "-" + month + "-" + date;
  
          var birthdate = req.body.birthDate;
  
          var userEntry = [
            [chosenUsername, passwordHashed, firstName, lastName, email, phoneNumber, creationDateTimestamp, birthdate]
          ]
  
          var missingInfo = false;
  
          for(let i = 0; i < userEntry[0].length; i++){
            if (userEntry[0][i] == null){
              missingInfo = true;
            }
          }
  
          if (missingInfo == false){
            connection.query("INSERT INTO alarmbuddy.users (username, password, first_Name, last_Name, email, phone_Number, creation_Date, birth_Date) VALUES ?", [userEntry], function(error, result, field){
              if (error) throw error;
              res.status(201).send('database updated sucessfully');
            }); 
          } else {
            console.log('ERROR: an entry was null');
            res.status(418).send('ERROR: an entry was null');
          }
  
        } else {
          console.log('ERROR: username already in use');
          res.status(418).send('ERROR: username already in use');
        }
  
      }
    );
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


app.post('/upload/:userID', upload.single('file'), function (req, res, next) {
  var userID = req.params.userID;

  var soundName = req.file.originalname;
  var mp3 = fs.readFileSync(req.file.path);

  var soundInfoEntry = [
    [userID,soundName]
  ];


  if (req.file.mimetype == "application/octet-stream"){
    connection.query("INSERT INTO alarmbuddy.soundInfo (soundOwner, soundName) VALUES ?", [soundInfoEntry], function(error, result, field){
      if(error) {
        throw error;
      }else{
        var soundFileEntry = [
          [result.insertId, mp3]
        ];
        connection.query("INSERT INTO alarmbuddy.soundFile (soundID, soundFile) VALUES ?", [soundFileEntry], function(error, result, field){
          fs.unlinkSync(req.file.path);
          res.status(201).send('database updated sucessfully');
        });
      }
    })
  } else {
    //not a mp3 file
    fs.unlinkSync(req.file.path);
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