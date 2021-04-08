require('dotenv').config()
var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var connection = require('./database');
var bcrypt = require('bcryptjs');
var fs = require('fs'); 

var jwt = require('jsonwebtoken');
var config = require('./config');

var multer  = require('multer');
var upload = multer({ dest: 'uploads/' })
app.use(express.json());
app.use(express.urlencoded({
  extended: true
}));

app.route('/users/:username')
//this just take in the UserId and returns all their information from the GoogleDB
  .get(function(req, res, next) {

    var token = req.headers.authorization;
    if (!token){
      res.status(401).send({ auth: false, message: 'No token provided.' });
    } else {
      jwt.verify(token, config.secret, function(err, decoded) {
        if (err) return res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });

        if (decoded.id == req.params.username){
          connection.query(
            "SELECT * FROM alarmbuddy.users WHERE username = ?", req.params.username,
            function(error, results, fields) {
              if (error) throw error;
              res.json(results);
            }
          );
        } else { 
          res.status(401).send('ERROR: Access to provided user denied.')
        }

      });
    }
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
            // create a token
            var token = jwt.sign({ id: chosenUsername }, config.secret, {
              expiresIn: 86400 // expires in 24 hours
            });
            res.status(200).send({ auth: true, token: token });
            //res.status(201).send('database updated sucessfully');
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



app.route('/friendsWith/:username')
//this just take in the UserId and returns all their information from the GoogleDB
.get(function(req, res, next) {


  var token = req.headers.authorization;
    if (!token){
      res.status(401).send({ auth: false, message: 'No token provided.' });
    } else {
      jwt.verify(token, config.secret, function(err, decoded) {
        if (err) return res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
        if (decoded.id == req.params.username){
          connection.query(
            "SELECT username2 FROM alarmbuddy.friendsWith WHERE username1 = ?", req.params.username,
            function(error, results, fields) {
              if (error) throw error;
              res.json(results);
            }
          );
        } else { 
          res.status(401).send('ERROR: Access to provided user denied.')
        }

      });
    }
});


app.post('/login', (req,res)=>{
  var submittedUsername = req.body.username;
  //This should be cleartext?
  var passwordUnhashed = req.body.password;
  //maybe I need to async the hash part...

  connection.query("SELECT password FROM alarmbuddy.users WHERE username = ?", [submittedUsername], function(error, result, field){
    if (error) throw error;
    var passwordIsValid = bcrypt.compareSync(passwordUnhashed, result[0].password);

    if (!passwordIsValid){
      res.status(401).send({ auth: false, token: null });
    } else {
      var token = jwt.sign({ id: submittedUsername }, config.secret, {
        expiresIn: 86400 // expires in 24 hours
      });
      res.status(200).send({ auth: true, token: token });
    }
  }); 
})


app.route('/download/:username/:soundName').get(function(req,res,next) {

  var token = req.headers.authorization;
    if (!token){
      res.status(401).send({ auth: false, message: 'No token provided.' });
    } else {
      jwt.verify(token, config.secret, function(err, decoded) {
        if (err) return res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
        if (decoded.id == req.params.username){
          connection.query("SELECT soundID FROM alarmbuddy.soundInfo WHERE soundName = ? AND soundOwner = ?", [req.params.soundName, req.params.username], function(error, results, fields) {
            if (error) throw error;
              connection.query("SELECT soundFile FROM alarmbuddy.soundFile WHERE soundID = ?", results[0].soundID, function(error, results, fields){
                fs.writeFile('/tmp/' + req.params.soundName, results[0].soundFile, function (err) {
                  if (err) throw err;
                  res.sendFile('/tmp/' + req.params.soundName, (err) => {
                    fs.unlinkSync('/tmp/' + req.params.soundName);
                  });
                });
              });
          });
        } else { 
          res.status(401).send('ERROR: Access to provided user denied.')
        }
      });
    }
});


app.post('/upload/:username', upload.single('file'), function (req, res, next) {

  var token = req.headers.authorization;

  if (!token){
    res.status(401).send({ auth: false, message: 'No token provided.' });
  } else {
    jwt.verify(token, config.secret, function(err, decoded) {
      if (err) return res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
      if (decoded.id == req.params.username){
        var username = req.params.username;

        var soundName = req.file.originalname;
        var mp3 = fs.readFileSync(req.file.path);

        var soundInfoEntry = [
          [username,soundName]
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
      } else { 
        res.status(401).send('ERROR: Access to provided user denied.')
      }
    });
  }
});


//This is for grabbing sounds for a specific user (needs to be stress tested)

app.route('/sounds/:username').get(function(req,res,next){

    var token = req.headers.authorization;
    if (!token){
      res.status(401).send({ auth: false, message: 'No token provided.' });
    } else {
      jwt.verify(token, config.secret, function(err, decoded) {
        if (err) return res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
        if (decoded.id == req.params.username){
          connection.query("SELECT soundName FROM alarmbuddy.soundInfo WHERE soundOwner = ?", req.params.username, function(error, results, fields) {
            if (error) throw error;
            res.json(results);
          });
        } else { 
          res.status(401).send('ERROR: Access to provided user denied.')
        }
      });
    }
});


app.get('/status', (req, res) => res.send('Working!'));

//Port 8080 for Google App Engine
//port 3000 i guess if youre doing local host 
app.set('port', process.env.PORT || 8080);
app.listen(8080);