// Load Google App Engine secret .env variables
require('dotenv').config()
var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var connection = require('./database');
var bcrypt = require('bcryptjs');
var fs = require('fs'); 

// JSON webtoken package
var jwt = require('jsonwebtoken');
// config for jsonwebtoken
var config = require('./config');
//Cors package
var cors = require('cors');
var multer  = require('multer');
var upload = multer({ dest: 'uploads/' });

// use for reading and writing JSON objects
app.use(express.json());
app.use(express.urlencoded({
  extended: true
}));


const corsOptions = {
	origin: "*"
};


app.use(cors(corsOptions));

// handler for getting user information
app.route('/users/:username').get(function(req, res, next) {

    // extract token from reqest header
    var token = req.headers.authorization;
    // check if token was provided in the request
    if (!token){
      res.status(401).send({ auth: false, message: 'No token provided.' });
    } else {
      // verify that token provided is a valid token
      jwt.verify(token, config.secret, function(error, decoded) {
        // respond with error that token could not be authenticated
        if (error){
          res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
        }
        // check if extracted username from token matches the username provided in the request
        if (decoded.id == req.params.username){
          // query the database and get all information related to the username provided
          connection.query("SELECT * FROM alarmbuddy.users WHERE username = ?", req.params.username, function(error, results, fields) {
              // respond with error if database query failed
              if (error){
                res.status(500).send('ERROR: database query error.');
              }
              // respond with database query results
              res.json(results);
            }
          );
        } else {
          // extracted token username did not match provided username from request so send error
          res.status(401).send('ERROR: Access to provided user denied.');
        }

      });
    }
});

// handler for registering a new user
app.post('/register', (req, res)=>{

  // extract username parameter from request
  var chosenUsername = req.body.username;

  // query the database to check if the username provided in the request matches a username already stored in the database
  connection.query("SELECT username FROM alarmbuddy.users WHERE username = ?", chosenUsername, function(error, results, fields) {
      // respond with error if database query fails
      if (error){
        res.status(500).send('ERROR: database query error.');
      }
      // check if the results from teh database query are empty meaning it didn't find a matching username
      if (JSON.stringify(results) == JSON.stringify([])){
        var passwordUnhashed = req.body.password;
        // generate a salt for the password
        var salt = bcrypt.genSaltSync(10);
        // encrypt the password with the salt
        var passwordHashed = bcrypt.hashSync(passwordUnhashed,salt);


        var firstName = req.body.firstName;
        var lastName = req.body.lastName;
        var email = req.body.email;
        var phoneNumber = req.body.phoneNumber;

        // create a time stamp for when the user registered
        let ts = Date.now();
        let date_ob = new Date(ts);
        let date = date_ob.getDate();
        let month = date_ob.getMonth() + 1;
        let year = date_ob.getFullYear();

        var creationDateTimestamp = year + "-" + month + "-" + date;

        var birthdate = req.body.birthDate;

        
        // array of required values to be put into the users table
        var userEntry = [
          [chosenUsername, passwordHashed, firstName, lastName, email, phoneNumber, creationDateTimestamp, birthdate]
        ];

        var missingInfo = false;
        // check that no required information for creating a user was missing
        for(let i = 0; i < userEntry[0].length; i++){
          if (userEntry[0][i] == null){
            missingInfo = true;
          }
        }

        if (missingInfo == false){
          // insert user data into users table
          connection.query("INSERT INTO alarmbuddy.users (username, password, first_Name, last_Name, email, phone_Number, creation_Date, birth_Date) VALUES ?", [userEntry], function(error, result, field){
            if (error){
              // respond with error if the insert fails
              res.status(500).send('ERROR: database query error.');
            }
            // generate the users token
            var token = jwt.sign({ id: chosenUsername }, config.secret, {
              expiresIn: 86400 // expires in 24 hours
            });
            // respond to the request with the user generated token
            res.status(200).send({ auth: true, token: token });
          }); 
        } else {
          // respond with error if some parameters were missing
          res.status(418).send('ERROR: an entry was null');
        }

      } else {
        // respond with error if the username already exists in the database
        res.status(418).send('ERROR: username already in use');
      }

    }
  );
});


// handler for getting a users friends list
app.route('/friendsWith/:username').get(function(req, res, next) {

  // extract token from reqest header
  var token = req.headers.authorization;
    // check if token was provided in the request
    if (!token){
      res.status(401).send({ auth: false, message: 'No token provided.' });
    } else {
      // verify that token provided is a valid token
      jwt.verify(token, config.secret, function(error, decoded) {
        // respond with error that token could not be authenticated
        if (error) {
          res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
        }
        // check if extracted username from token matches the username provided in the request
        if (decoded.id == req.params.username){
          // selct all usernames from the database that the username provided is friends with
          connection.query("SELECT username2 FROM alarmbuddy.friendsWith WHERE username1 = ? UNION SELECT username1 FROM alarmbuddy.friendsWith WHERE username2 = ?", [req.params.username, req.params.username], function(error, results, fields) {
              if (error){
                // respond with error if selection fails
                res.status(500).send('ERROR: database query error.');
              }
              // resond with the results of the query
              res.json(results);
            }
          );
        } else { 
          // extracted token username did not match provided username from request so send error 
          res.status(401).send('ERROR: Access to provided user denied.')
        };

      });
    }
});

// handler for user login
app.post('/login', (req,res) => {

  // assign provided username to var
  var submittedUsername = req.body.username;
  // assign provided password to var
  var passwordUnhashed = req.body.password;

  // select the password from the database that corresponds to the username provided in the request
  connection.query("SELECT password FROM alarmbuddy.users WHERE username = ?", [submittedUsername], function(error, result, field){
    if (error){
      // respond with error if selection fails
      res.status(500).send('ERROR: database query error.');
    }

    // compare password provided to the password stored in the database
    var passwordIsValid = bcrypt.compareSync(passwordUnhashed, result[0].password);

    if (!passwordIsValid){
      // respond with false authentication if the passwords did not match
      res.status(401).send({ auth: false, token: null });
    } else {
      // generate the users token
      var token = jwt.sign({ id: submittedUsername }, config.secret, {
        expiresIn: 86400 // expires in 24 hours
      });
      // respond with the user generated token
      res.status(200).send({ auth: true, token: token });
    }
  }); 
})

// handler for downloading sound file from database
app.route('/download/:username/:soundName').get(function(req,res,next) {

  // extract token from reqest header
  var token = req.headers.authorization;
    // check if token was provided in the request
    if (!token){
      res.status(401).send({ auth: false, message: 'No token provided.' });
    } else {
      // verify that token provided is a valid token
      jwt.verify(token, config.secret, function(error, decoded) {
        // respond with error that token could not be authenticated
        if (error) {
          res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
        }
        // check if extracted username from token matches the username provided in the request
        if (decoded.id == req.params.username){
          // select the soundID from the soundInfo table based off the sound name and owner provided in the request
          connection.query("SELECT soundID FROM alarmbuddy.soundInfo WHERE soundName = ? AND soundOwner = ?", [req.params.soundName, req.params.username], function(error, results, fields) {
            if (error){
              // respond with error if database query failed
              res.status(500).send('ERROR: database query error.');
            }
              // select the sound file from the soundFile table based off the soundID from the previous query
              connection.query("SELECT soundFile FROM alarmbuddy.soundFile WHERE soundID = ?", results[0].soundID, function(error, results, fields){
                // write the sound file to the tmp folder
                fs.writeFile('/tmp/' + req.params.soundName, results[0].soundFile, function (error) {
                  if (error){
                    // respond with error if writing to file failed
                    res.status(500).send('ERROR: write to file error.');
                  }
                  // respond with written file
                  res.sendFile('/tmp/' + req.params.soundName, (error) => {
                    // delete the sound file from the temp folder
                    fs.unlinkSync('/tmp/' + req.params.soundName);
                  });
                });
              });
          });
        } else { 
          // extracted token username did not match provided username from request so send error
          res.status(401).send('ERROR: Access to provided user denied.');
        }
      });
    }
});

// handler for updloading sound file to database
app.post('/upload/:username', upload.single('file'), function (req, res, next) {

  // extract token from reqest header
  var token = req.headers.authorization;
  // check if token was provided in the request
  if (!token){
    res.status(401).send({ auth: false, message: 'No token provided.' });
  } else {
    // verify that token provided is a valid token
    jwt.verify(token, config.secret, function(error, decoded) {
      // respond with error that token could not be authenticated
      if (error) {
        res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
      }
      // check if extracted username from token matches the username provided in the request
      if (decoded.id == req.params.username){
        // assign provided username to var
        var username = req.params.username;
        // assign sound name from the uploaded file sound name
        var soundName = req.file.originalname;
        // create the mp3 file
        var mp3 = fs.readFileSync(req.file.path);
        // create sound info entry
        var soundInfoEntry = [
          [username,soundName]
        ];
        // check if the file type is a media file
        if (req.file.mimetype == "application/octet-stream"){
          // insert sound owner and sound name into the soundInfo table
          connection.query("INSERT INTO alarmbuddy.soundInfo (soundOwner, soundName) VALUES ?", [soundInfoEntry], function(error, result, field){
            if(error) {
              // respond with error if insert failed
              res.status(500).send('ERROR: database query error.');
            }else{
              // create soundfile entry using the results from previous query and the mp3 file
              var soundFileEntry = [
                [result.insertId, mp3]
              ];
              // insert the soundID and sound file into the soundFile table
              connection.query("INSERT INTO alarmbuddy.soundFile (soundID, soundFile) VALUES ?", [soundFileEntry], function(error, result, field){
                if(error) {
                  // respond with error if insert failed
                  res.status(500).send('ERROR: database query error.');
                }
                // delete the mp3 file from temp storage
                fs.unlinkSync(req.file.path);
                // respond with valid upload to database
                res.status(201).send('database updated sucessfully');
              });
            }
          })
        } else {
          // delete the file because it wasn't an mp3 file
          fs.unlinkSync(req.file.path);
        }
      } else {
        // extracted token username did not match provided username from request so send error
        res.status(401).send('ERROR: Access to provided user denied.');
      }
    });
  }
});


// handler for grabbing list of sounds from database that the user owns
app.route('/sounds/:username').get(function(req,res,next){

    // extract token from reqest header
    var token = req.headers.authorization;
    // check if token was provided in the request
    if (!token){
      res.status(401).send({ auth: false, message: 'No token provided.' });
    } else {
      // verify that token provided is a valid token
      jwt.verify(token, config.secret, function(error, decoded) {
        // respond with error that token could not be authenticated
        if (error) {
          res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
        }
        // check if extracted username from token matches the username provided in the request
        if (decoded.id == req.params.username){
          // select the list of soundNames from soundInfo table based off the username provided in the request
          connection.query("SELECT soundName FROM alarmbuddy.soundInfo WHERE soundOwner = ?", req.params.username, function(error, results, fields) {
            if (error) {
              // respond with error if database query failed
              res.status(500).send('ERROR: database query error.');
            }
            // resond with the results of the query
            res.json(results);
          });
        } else {
          // extracted token username did not match provided username from request so send error
          res.status(401).send('ERROR: Access to provided user denied.');
        }
      });
    }
});


// handler for deleting a sound
app.route('/deleteAlarm/:username/:soundName').get(function(req,res,next) {

  // extract token from reqest header
  var token = req.headers.authorization;
    // check if token was provided in the request
    if (!token){
     // res.status(401).send({ auth: false, message: 'No token provided.' });
    } else {
      // verify that token provided is a valid token
      jwt.verify(token, config.secret, function(error, decoded) {
        // respond with error that token could not be authenticated
        if (error) {
          res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
        }
        // check if extracted username from token matches the username provided in the request
        if (decoded.id == req.params.username){

          connection.query("DELETE FROM alarmbuddy.soundInfo WHERE soundName = ? AND soundOwner = ?", [req.params.soundName, req.params.username], function(error, results, fields) {
            if (error){
              // respond with error if database query failed
              res.status(500).send('ERROR: database query error.');
            }
            res.status(200).send('Successfully deleted sound.')
          });
        } else { 
          // extracted token username did not match provided username from request so send error
          res.status(401).send('ERROR: Access to provided user denied.');
        }
      });
    }
});


// handler for checking if the rest API is online
app.get('/status', (req, res) => res.send('Working!'));

//Port 8080 for Google App Engine
//port 3000 i guess if youre doing local host 
app.set('port', process.env.PORT || 8080);
app.listen(8080);