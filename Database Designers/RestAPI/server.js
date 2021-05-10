// Load Google App Engine secret .env variables
require('dotenv').config()
var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var connection = require('./database');
var bcrypt = require('bcryptjs');
var fs = require('fs'); 

// package for getting the file type of a file based on the files contents
var FileType = require('file-type');
// JSON webtoken package
var jwt = require('jsonwebtoken');
// config for jsonwebtoken
var config = require('./config');
// package for reading files from requests
var multer  = require('multer');

// set temp file destination for sound files to /uploads and set sound file size to 100MB
var uploadSound = multer({ 
  dest: 'uploads/',
  limits: { fileSize: 100 * 1024 * 1024}
}).single('file');
// set temp file destination for image files to /uploads and set image file size to 30MB
var uploadImage = multer({ 
  dest: 'uploads/',
  limits: { fileSize: 30 * 1024 * 1024}
}).single('file');

// package for cross origion resource sharing
var cors = require('cors');

// used for reading and writing JSON objects
app.use(express.json());
app.use(express.urlencoded({
  extended: true
}));

// set connection origin to come from anywhere
const corsOptions = {
	origin: "*"
};
app.use(cors(corsOptions));


// - - - - - - - - - - - - - - - - - - - - - - - USER RELATED FUNCTIONS - - - - - - - - - - - - - - - - - - - - - - - //


// handler for getting user information
app.route('/users/:username').get(function(req, res, next) {
    // extract token from reqest header
    var token = req.headers.authorization;
    // check if token was provided in the request
    if (!token){
      res.status(401).send({ auth: false, message: 'no token provided' });
    } else {
      // verify that token provided is a valid token
      jwt.verify(token, config.secret, function(error, decoded) {
        // respond with error that token could not be authenticated
        if (error){
          res.status(500).send({ auth: false, message: 'failed to authenticate token' });
        }
        // check if extracted username from token matches the username provided in the request
        if (decoded.id == req.params.username){
          // query the database and get all information related to the username provided
          connection.query("SELECT * FROM alarmbuddy.users WHERE username = ?", req.params.username, function(error, results, fields) {
              // respond with error if database query failed
              if (error){
                res.status(500).send('ERROR: database query error');
              }
              // respond with database query results
              res.json(results);
            }
          );
        } else {
          // extracted token username did not match provided username from request so send error
          res.status(403).send('ERROR: access to provided user denied');
        }
      });
    }
});

// handler for registering a new user
app.post('/register', (req, res)=>{
  // check if username, email, or phone number field was sent in the request
  // will use these three fields to varify no other user has signed up with these credentials already
  if (req.body.username == null || req.body.email == null || req.body.phoneNumber == null) {
    // respond with an error that some required user information was missing in request
    res.status(400).send('ERROR: missing information for registration');
  } else {
    // put these fields from the request into variables
    var chosenUsername = req.body.username;
    var email = req.body.email;
    var phoneNumber = req.body.phoneNumber;
    // query the database to check if the username, email, or phone number provided in the request have been used already by other users already in the database
    connection.query("SELECT username FROM alarmbuddy.users WHERE username = ? OR email = ? OR phone_Number = ?", [chosenUsername, email, phoneNumber], function(error, results, fields) {
        // respond with error if database query fails
        if (error){
          res.status(500).send('ERROR: database query error');
        }
        // check if the results from the database query are empty meaning it didn't find any users that have previously used these credentials
        if (JSON.stringify(results) == JSON.stringify([])){
          // assign password field from request to variable
          var passwordUnhashed = req.body.password;
          // generate a salt for the password
          var salt = bcrypt.genSaltSync(10);
          // encrypt the password with the salt
          var passwordHashed = bcrypt.hashSync(passwordUnhashed,salt);
          // assigning other required data fields from request
          var firstName = req.body.firstName;
          var lastName = req.body.lastName;
          // create a time stamp for when the user registered
          let ts = Date.now();
          let date_ob = new Date(ts);
          let date = date_ob.getDate();
          let month = date_ob.getMonth() + 1;
          let year = date_ob.getFullYear();
          // build the timestamp string using variables above
          var creationDateTimestamp = year + "-" + month + "-" + date;
          var birthdate = req.body.birthDate;
          // array of required values that will be inserted into the users table in the database
          var userEntry = [
            [chosenUsername, passwordHashed, firstName, lastName, email, phoneNumber, creationDateTimestamp, birthdate]
          ];
          // set missingInfo to false;
          var missingInfo = false;
          // check that no required information for creating a user was missing
          for(let i = 0; i < userEntry[0].length; i++){
            //if an entry in the user entry is null then set missingInfo to true
            if (userEntry[0][i] == null){
              missingInfo = true;
            }
          }
          // if there was no missing info, continue...
          if (missingInfo == false){
            // insert user data into users table
            connection.query("INSERT INTO alarmbuddy.users (username, password, first_Name, last_Name, email, phone_Number, creation_Date, birth_Date) VALUES ?", [userEntry], function(error, result, field){
              if (error){
                // respond with error if the insert fails
                res.status(500).send('ERROR: database query error');
              }
              // generate the users token
              var token = jwt.sign({ id: chosenUsername }, config.secret, {
                expiresIn: 86400 // expires in 24 hours
              });
              // respond with the user generated token
              res.status(200).send({ auth: true, token: token });
            }); 
          } else {
            // respond with error if some required data fields were missing
            res.status(418).send('ERROR: an entry was null');
          }
        } else {
          // respond with error if the username, email, or phone number is already in use by another user
          res.status(418).send('ERROR: username, email, or phone number already in use');
        }
      }
    );
  }
});

// handler for user login
app.post('/login', (req,res) => {
  // assign username field from request to variable
  var submittedUsername = req.body.username;
  // assign password field from request to variable
  var passwordUnhashed = req.body.password;
  // select the password from the database that corresponds to the username provided in the request
  connection.query("SELECT password FROM alarmbuddy.users WHERE username = ?", [submittedUsername], function(error, result, field){
    if (error){
      // respond with error if query fails
      res.status(500).send('ERROR: database query error');
    }
    // compare password provided from request with the password stored in the database for user provided in request
    var passwordIsValid = bcrypt.compareSync(passwordUnhashed, result[0].password);
    // if password doesn't match...
    if (!passwordIsValid){
      // respond with false authentication
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
});

// - - - - - - - - - - - - - - - - - - - - - - - USER PROFILE PHOTOS - - - - - - - - - - - - - - - - - - - - - - - //

// handler for updloading profile picture to the database
app.post('/setProfilePicture/:username', function (req, res, next) {
  uploadImage(req, res, function (err) {
    if (err instanceof multer.MulterError) {
      // respond with error if file cannot be interpreted
      res.status(400).send('ERROR: file not being passed correctly');
    } else if (err) {
      // respond with error if file failed to upload
      res.status(401).send('ERROR: file upload error')
    } else if (req.file == null) {
      // respond with error if the request doesn't contain a file
      res.status(500).send('ERROR: no file received'); 
    } else {
      // extract token from reqest header
      var token = req.headers.authorization;
      // check if token was provided in the request
      if (!token){
        res.status(401).send({ auth: false, message: 'no token provided' });
      } else {
        // verify that token provided is a valid token
        jwt.verify(token, config.secret, function(error, decoded) {
          // respond with error that token could not be authenticated
          if (error) {
            res.status(500).send({ auth: false, message: 'failed to authenticate token' });
          }
          // check if extracted username from token matches the username provided in the request
          if (decoded.id == req.params.username){
            // assign username field to variable
            var username = req.params.username;
            // create the image file and assign to var
            var image = fs.readFileSync(req.file.path);
            // check if the file type is a png or jpeg file
            if (req.file.mimetype == "image/jpeg" || req.file.mimetype == "image/png"){
              // create a variable that will hold the image type/extension
              var imageType;
              // correctly assign the image type variable to the correct extension based off the image file mimetype
              if (req.file.mimetype == "image/jpeg"){
                imageType = "jpeg";
              } else {
                imageType = "png";
              }
              // inserts profile picture if user doesn't have one or replaces profile picture if one already exists for the user in the database
              connection.query("REPLACE INTO alarmbuddy.profilePictures SET username = ?, profile_Photo = ?, image_Type = ?", [username, image, imageType], function(error, result, field){
                if(error) {
                  // respond with error if insert/replace failed
                  res.status(500).send('ERROR: database query error');
                }
                // delete the file from temporary storage
                fs.unlinkSync(req.file.path);
                // respond with profile pricture upload success
                res.status(201).send('profile picture successfully uploaded');
              });
            } else {
              // delete the file because it wasn't a png/jpeg file
              fs.unlinkSync(req.file.path);
              // respond with file not supported
              res.status(415).send('ERROR: file type not supported');
            }
          } else {
            // delete the file because the user sending request couldn't be successfully authenticated
            fs.unlinkSync(req.file.path);
            // extracted token username did not match provided username from request so respond with error
            res.status(403).send('ERROR: access to provided user denied');
          }
        });
      } 
    }   
  });
});

// handler for getting a users profile picture
app.route('/getProfilePicture/:username/:usernamepfp').get(function(req,res,next){
  // extract token from reqest header
  var token = req.headers.authorization;
  // check if token was provided in the request
  if (!token){
    res.status(401).send({ auth: false, message: 'no token provided' });
  }else {
    // verify that token provided is a valid token
    jwt.verify(token, config.secret, function(error, decoded) {
      // respond with error that token could not be authenticated
      if (error){
        res.status(500).send({ auth: false, message: 'failed to authenticate token' });
      }
      // check if extracted username from token matches the username provided in the request
      if (decoded.id == req.params.username){
        connection.query("SELECT * FROM alarmbuddy.userBlockList WHERE blockedBy = ? AND blocked = ?", [req.params.usernamepfp, req.params.username], function(error, results, fields){
          if (error){
            res.status(500).send('ERROR: database query error');
          }
          if ((JSON.stringify(results) == JSON.stringify([]))){
            // select the profile photo of the user from the profilePictures table
            connection.query("SELECT profile_Photo, image_Type FROM alarmbuddy.profilePictures WHERE username = ?", [req.params.usernamepfp], function(error, results, field){
              if(error) {
                // respond with error if query failed
                res.status(500).send('ERROR: database query error');
              }
              // assign the path to the image to a variable
              var pathToImage = "/tmp/" + req.params.username + "_profilePhoto." + results[0].image_Type;
              // write the sound file to the tmp folder
              fs.writeFile(pathToImage, results[0].profile_Photo, function (error) {
                if (error){
                  // respond with error if writing to file failed
                  res.status(500).send('ERROR: write to file error');
                }
                // respond with written file
                res.sendFile(pathToImage, (error) => {
                  if (error){
                    // respond with error if sending file failed
                    res.status(500).send('ERROR: could not send file');
                  }
                  // delete the sound file from the temp folder
                  fs.unlinkSync(pathToImage);
                });
              });
            });
          } else {
            res.status(403).send('ERROR: cannot get users pfp becasue user has blocked you');
          }
        });
      } else { 
        // extracted token username did not match provided username from request so send error
        res.status(403).send('ERROR: access to provided user denied');
      }
    });
  }
});

// - - - - - - - - - - - - - - - - - - - - - - - FRIEND RELATED FUNCTIONS - - - - - - - - - - - - - - - - - - - - - - - //

// handler for sending friend requests
app.route('/sendRequest/:sender/:receiver').post(function(req,res,next){
  // extract token from reqest header
  var token = req.headers.authorization;
  // check if token was provided in the request
  if (!token){
    res.status(401).send({ auth: false, message: 'no token provided' });
  }else{
    // verify that token provided is a valid token
    jwt.verify(token, config.secret, function(error, decoded) {
      // respond with error that token could not be authenticated
      if (error){
        res.status(500).send({ auth: false, message: 'failed to authenticate token' });
      }
      // check if extracted username from token matches the sender username provided in the request
      if (decoded.id == req.params.sender){
        connection.query("SELECT * FROM alarmbuddy.userBlockList WHERE blockedBy = ? AND blocked = ?", [req.params.receiver, req.params.sender], function(error, results, fields){
          if (error){
            res.status(500).send('ERROR: database query error');
          }
          if ((JSON.stringify(results) == JSON.stringify([]))){
            // replace/insert into friendRequests table the sender and reciever usernames provided in the request
            connection.query("REPLACE INTO alarmbuddy.friendRequests SET senderUsername = ?, recipientUsername = ?", [req.params.sender, req.params.receiver], function(error, results, fields) {
              if (error){
                // respond with error if database query failed
                res.status(500).send('ERROR: database query error');
              }
              // respond with success that friend request was sent successfully
              res.status(201).send('friend request sent successfully');
            });
          } else {
            res.status(403).send('ERROR: cannot send user a friend request because user has blocked you');
          }
        });
      } else { 
          // extracted token username did not match provided username from request so send error
          res.status(403).send('ERROR: access to provided user denied');
      }
    });
  }
});

// handler for accepting incoming friend requests
app.route('/acceptFriendRequest/:receiver/:sender').post(function(req,res,next){
  // extract token from reqest header
  var token = req.headers.authorization;
  // check if token was provided in the request
  if (!token){
    res.status(401).send({ auth: false, message: 'no token provided' });
  }else{
    // verify that token provided is a valid token
    jwt.verify(token, config.secret, function(error, decoded) {
      // respond with error that token could not be authenticated
      if (error){
        res.status(500).send({ auth: false, message: 'failed to authenticate token' });
      }
      // check if extracted username from token matches the receiver username provided in the request
      if (decoded.id == req.params.receiver){
        connection.query("SELECT * FROM alarmbuddy.userBlockList WHERE blockedBy = ? AND blocked = ?", [req.params.receiver, req.params.sender], function(error, results, fields){
          if (error){
            res.status(500).send('ERROR: database query error');
          }
          if ((JSON.stringify(results) == JSON.stringify([]))){
            // look for the row in the friend requests table with the sender and recipient to verify the friend request exists
            connection.query("SELECT * FROM alarmbuddy.friendRequests WHERE senderUsername = ? AND recipientUsername = ?", [req.params.sender, req.params.receiver], function(error, results, fields) {
              if (error){
                // respond with error if database query failed
                res.status(500).send('ERROR: database query error');
              }
              // check if the query above responded with a row from the friendRequests table
              if (!(JSON.stringify(results) == JSON.stringify([]))){
                // create friends entry variable
                var friendsEntry = [
                  [req.params.sender, req.params.receiver]
                ]
                // insert into the friends with table the reciever and sender username into the friendsWith table
                connection.query("INSERT INTO alarmbuddy.friendsWith (username1, username2) VALUES ?", [friendsEntry], function(error, results, fields) {
                  if (error){
                    // respond with error if database query failed
                    res.status(500).send('ERROR: database query error');
                  }
                  // delete the requests from the friend requests table because the request has been successfully accepted
                  connection.query("DELETE FROM alarmbuddy.friendRequests WHERE senderUsername = ? AND recipientUsername = ?", [req.params.sender, req.params.receiver], function(error, results, fields) {
                    if (error){
                      // respond with error if database query failed
                      res.status(500).send('ERROR: database query error');
                    }
                    // respond with friend request accepted successfully
                    res.status(201).send('friend request accepted successfully');
                  });
                });
              } else {
                // respond with error if the friend request could not be found in the database
                res.status(404).send('ERROR: friend request does not exist');
              }
            });
          } else {
            res.status(403).send('ERROR: cannot accept friend request because you have the user blocked');
          }
        });
      } else { 
        // extracted token username did not match provided receiver username from request so send error
        res.status(403).send('ERROR: access to provided user denied');
    }
    });
  }
});

// handler for canceling a sent friend request
// *NOTE* : only the sender can cancel the request
app.route('/cancelFriendRequest/:sender/:receiver').post(function(req,res,next){
  // extract token from reqest header
  var token = req.headers.authorization;
  // check if token was provided in the request
  if (!token){
    res.status(401).send({ auth: false, message: 'no token provided' });
  }else{
    // verify that token provided is a valid token
    jwt.verify(token, config.secret, function(error, decoded) {
      // respond with error that token could not be authenticated
      if (error){
        res.status(500).send({ auth: false, message: 'failed to authenticate token' });
      }
      // check if extracted username from token matches the sender username provided in the request
      if (decoded.id == req.params.sender){
        connection.query("DELETE FROM alarmbuddy.friendRequests WHERE senderUsername = ? AND recipientUsername = ?", [req.params.sender, req.params.receiver], function(error, results, fields) {
          if (error){
            // respond with error if database query failed
            res.status(500).send('ERROR: database query error');
          }
          // check if any row was actually deleted from the delete query above
          if (results.affectedRows == 0){
            // respond with error that the friend request does not exist
            res.status(404).send('ERROR: friend request does not exist');
          } else {
            // respond with request canceled successfully
            res.status(201).send('request canceled successfully');
          }
        });
      } else {
        // extracted token username did not match provided sender username from request so send error
        res.status(403).send('ERROR: access to provided user denied');
      }
    });
  }
});

//handler for denying a friend request
// *NOTE* : only the receiver can deny the request
app.route('/denyFriendRequest/:receiver/:sender').post(function(req,res,next){
  // extract token from reqest header
  var token = req.headers.authorization;
  // check if token was provided in the request
  if (!token){
    res.status(401).send({ auth: false, message: 'no token provided' });
  }else{
    // verify that token provided is a valid token
    jwt.verify(token, config.secret, function(error, decoded) {
      // respond with error that token could not be authenticated
      if (error){
        res.status(500).send({ auth: false, message: 'failed to authenticate token' });
      }
      // check if extracted username from token matches the receiver username provided in the request
      if (decoded.id == req.params.receiver){
        // delete the request from the friendRequest table where the sender and recipient username match a row in the table
        connection.query("DELETE FROM alarmbuddy.friendRequests WHERE senderUsername = ? AND recipientUsername = ?", [req.params.sender, req.params.receiver], function(error, results, fields) {
          if (error){
            // respond with error if database query failed
            res.status(500).send('ERROR: database delete error');
          }
          // check if any row was actually deleted from the delete query above
          if (results.affectedRows == 0){
            // respond with error that the friend request does not exist
            res.status(404).send('ERROR: friend request does not exist');
          } else {
            // respond with request canceled successfully
            res.status(201).send('friend request denied successfully');
          }
        });
      } else {
        // extracted token username did not match provided receiver username from request so send error
        res.status(403).send('ERROR: access to provided user denied');
      }
    });
  }
});

// handler for getting incoming friend requests
app.route('/requests/:username').get(function(req,res,next){
  // extract token from reqest header
  var token = req.headers.authorization;
  // check if token was provided in the request
  if (!token){
    res.status(401).send({ auth: false, message: 'no token provided' });
  } else {
    // verify that token provided is a valid token
    jwt.verify(token, config.secret, function(error, decoded) {
      // respond with error that token could not be authenticated
      if (error){
        res.status(500).send({ auth: false, message: 'failed to authenticate token' });
      }
      // check if extracted username from token matches the username provided in the request
      if (decoded.id == req.params.username){
        // select all rows where the recipientUsername in the friendRequests table matches the username of the user requesting
        connection.query('SELECT * FROM alarmbuddy.friendRequests WHERE recipientUsername = ?', req.params.username, function(error, results, fields) {
          if (error){
            // respond with error if database query failed
            res.status(500).send('ERROR: database query error');
          }
          // respond with list of incoming friends requests
          res.json(results); 
        });
      }else { 
          // extracted token username did not match provided username from request so send error
          res.status(403).send('ERROR: access to provided user denied');
      }
    });
  }
});

// handler for deleting a friend from friends list
app.route('/deleteFriend/:username/:friend').delete(function(req,res,next){
  // extract token from reqest header
  var token = req.headers.authorization;
  // check if token was provided in the request
  if (!token){
    res.status(401).send({ auth: false, message: 'no token provided' });
  }else{
    // verify that token provided is a valid token
    jwt.verify(token, config.secret, function(error, decoded) {
      // respond with error that token could not be authenticated
      if (error){
        res.status(500).send({ auth: false, message: 'failed to authenticate token' });
      }
      // check if extracted username from token matches the username provided in the request
      if (decoded.id == req.params.username){
        // delete the row from the friendsWith table where the two usernames provided in the request match up
        connection.query("DELETE FROM alarmbuddy.friendsWith WHERE (username2 = ? AND username1 = ?) OR (username1 = ? AND username2 = ?)", [req.params.username, req.params.friend, req.params.username, req.params.friend], function(error, results, fields) {
          if (error){
            // respond with error if database delete failed
            res.status(500).send('ERROR: database query error');
          }
          // check if any row was actually deleted from the delete query above
          if (results.affectedRows == 0){
            // respond with error that the friend request does not exist
            res.status(404).send('ERROR: no friendship with user.');
          } else {
            // respond with request canceled successfully
            res.status(201).send('removed friend successfully');
          }
        });
      } else {
        // extracted token username did not match provided sender username from request so send error
        res.status(403).send('ERROR: access to provided user denied');
      }
    });
  }
});

// handler for getting a user's friends list
app.route('/friendsWith/:username').get(function(req, res, next) {
  // extract token from reqest header
  var token = req.headers.authorization;
    // check if token was provided in the request
    if (!token){
      res.status(401).send({ auth: false, message: 'no token provided' });
    } else {
      // verify that token provided is a valid token
      jwt.verify(token, config.secret, function(error, decoded) {
        // respond with error that token could not be authenticated
        if (error) {
          res.status(500).send({ auth: false, message: 'failed to authenticate token' });
        }
        // check if extracted username from token matches the username provided in the request
        if (decoded.id == req.params.username){
          // select all usernames from the database that the username provided is friends with
          connection.query("SELECT username2 FROM alarmbuddy.friendsWith WHERE username1 = ? UNION SELECT username1 FROM alarmbuddy.friendsWith WHERE username2 = ?", [req.params.username, req.params.username], function(error, results, fields) {
              if (error){
                // respond with error if query fails
                res.status(500).send('ERROR: database query error');
              }
              // resond with list of freinds of user making the request
              res.json(results);
            }
          );
        } else { 
          // extracted token username did not match provided username from request so send error 
          res.status(403).send('ERROR: access to provided user denied')
        };
      });
    }
});

// handler for blocking a user
app.route('/blockUser/:username/:userToBlock').post(function(req, res, next) {
  // extract token from reqest header
  var token = req.headers.authorization;
    // check if token was provided in the request
    if (!token){
      res.status(401).send({ auth: false, message: 'no token provided' });
    } else {
      // verify that token provided is a valid token
      jwt.verify(token, config.secret, function(error, decoded) {
        // respond with error that token could not be authenticated
        if (error) {
          res.status(500).send({ auth: false, message: 'failed to authenticate token' });
        }
        // check if extracted username from token matches the username provided in the request
        if (decoded.id == req.params.username){
          // insert/replace into userBlockList table a row with the blockedBy username and blocked username
          connection.query("REPLACE INTO alarmbuddy.userBlockList SET blockedBy = ?, blocked = ?", [req.params.username, req.params.userToBlock], function(error, results, fields) {
              if (error){
                // respond with error if insert/replace fails
                res.status(500).send('ERROR: database query error');
              }
              // respond with success that user was blocked
              res.status(201).send('successfully blocked user');
            }
          );
        } else { 
          // extracted token username did not match provided username from request so send error 
          res.status(403).send('ERROR: access to provided user denied')
        };
      });
    }
});

// hanlder for unblocking a user
app.route('/unblockUser/:username/:userToUnblock').post(function(req, res, next) {
  // extract token from reqest header
  var token = req.headers.authorization;
    // check if token was provided in the request
    if (!token){
      res.status(401).send({ auth: false, message: 'no token provided' });
    } else {
      // verify that token provided is a valid token
      jwt.verify(token, config.secret, function(error, decoded) {
        // respond with error that token could not be authenticated
        if (error) {
          res.status(500).send({ auth: false, message: 'failed to authenticate token' });
        }
        // check if extracted username from token matches the username provided in the request
        if (decoded.id == req.params.username){
          // delete row from userBlockList table where blockedBy username and blocked username match fields provided in the request
          connection.query("DELETE FROM alarmbuddy.userBlockList WHERE blockedBy = ? AND blocked = ?", [req.params.username, req.params.userToUnblock], function(error, results, fields) {
              if (error){
                // respond wih error if delete fails
                res.status(500).send('ERROR: database query error');
              }
              // if no row can be found and deleted...
              if (results.affectedRows == 0){
                // respond with error that user not blocked
                res.status(500).send('ERROR: user not blocked');
              } else {
                // respond with success that user was successfully unblocked
                res.status(201).send('successfully unblocked user');
              }
            }
          );
        } else {
          // extracted token username did not match provided username from request so send error
          res.status(403).send('ERROR: access to provided user denied');
        };
      });
    }
});

// - - - - - - - - - - - - - - - - - - - - - - - SOUND RELATED FUNCTIONS - - - - - - - - - - - - - - - - - - - - - - - //

// handler for updloading sound file to database
app.post('/upload/:username', function (req, res, next) {
  uploadSound(req, res, function (err) {
    if (err instanceof multer.MulterError) {
      // respond with error if file cannot be interpreted
      res.status(400).send('ERROR: file not being passed correctly')
    } else if (err) {
      // respond with error if file failed to upload
      res.status(401).send('ERROR: file upload error');
    } else if (req.file == null) {
      // respond with error if the request doesn't contain a file
      res.status(500).send('ERROR: no file received');
    } else {
      // extract token from reqest header
      var token = req.headers.authorization;
      // check if token was provided in the request
      if (!token){
        res.status(401).send({ auth: false, message: 'no token provided' });
      } else {
        // verify that token provided is a valid token
        jwt.verify(token, config.secret, function(error, decoded) {
          // respond with error that token could not be authenticated
          if (error) {
            res.status(500).send({ auth: false, message: 'failed to authenticate token' });
          }
          // check if extracted username from token matches the username provided in the request
          if (decoded.id == req.params.username){
            // assign username field from request to variable
            var username = req.params.username;
            // assign sound name to variable 
            var soundName = req.file.originalname;
            // assign file description
            // *NOTE* : sound description is not required
            var soundDescription = req.body.soundDescription;
            // create the mp3 file from the file sent in the request
            var mp3 = fs.readFileSync(req.file.path);
            (async () => {
              // set the file stream from the file path which is located in the uploads folder
              const stream = fs.createReadStream(req.file.path);
              // extract the file type from the file sent in request by analyzing the file contents
              const fileType = await FileType.fromStream(stream);

              // check if audio file is mp3 with a mimetype of application/octet-strean or audio/mpeg
              if (fileType.mime == "audio/mpeg" && fileType.ext == "mp3" && (req.file.mimetype == "application/octet-stream" || req.file.mimetype == "audio/mpeg")){
                // insert sound sound name and sound description into the soundInfo table
                connection.query("INSERT INTO alarmbuddy.soundInfo (soundName, soundDescription) VALUES (?, ?)", [soundName, soundDescription], function(error, result, field){
                  if(error) {
                    // respond with error if insert failed
                    res.status(500).send('ERROR: database query error');
                  }else{
                    // assign sound ID, aquired from the query above, to a variable
                    var soundID = result.insertId;
                    // create soundfile entry using the sound ID and the mp3 file
                    var soundFileEntry = [
                      [soundID, mp3]
                    ];
                    // insert the sound file entry into the soundFile table
                    connection.query("INSERT INTO alarmbuddy.soundFile (soundID, soundFile) VALUES ?", [soundFileEntry], function(error, result, field){
                      if(error) {
                        // respond with error if insert failed
                        res.status(500).send('ERROR: database query error');
                      }
                      // delete the mp3 file from temp storage
                      fs.unlinkSync(req.file.path);
                      // create ownership entry using the username and soundID
                      var ownershipEntry = [
                        [username, soundID, username]
                      ]
                      // insert ownership entry into soundOwnership table
                      connection.query("INSERT INTO alarmbuddy.soundOwnership (username, soundID, sharedBy) VALUES ?", [ownershipEntry], function(error, result,field) {
                        if(error) {
                          // respond with error if insert failed
                          res.status(500).send('ERROR: database query error');
                        }
                        // respond with valid upload to database
                        res.status(201).send('database updated sucessfully');
                      });
                    });
                  }
                })
              } else {
                // delete the file from uploads folder because it wasn't identified as an mp3 file
                fs.unlinkSync(req.file.path);
                res.status(415).send('ERROR: file type not supported');
              }
            })();
          } else {
            // extracted token username did not match provided username from request so send error
            res.status(403).send('ERROR: access to provided user denied');
          }
        });
      }
    }
  });
});

// handler for downloading sound file from database
app.route('/download/:username/:soundID').get(function(req,res,next) {
  // extract token from reqest header
  var token = req.headers.authorization;
  // check if token was provided in the request
  if (!token){
    res.status(401).send({ auth: false, message: 'no token provided' });
  } else {
    // verify that token provided is a valid token
    jwt.verify(token, config.secret, function(error, decoded) {
      // respond with error that token could not be authenticated
      if (error) {
        res.status(500).send({ auth: false, message: 'failed to authenticate token' });
      }
      // check if extracted username from token matches the username provided in the request
      if (decoded.id == req.params.username){
        // query soundOwnership table to see if user sending request has ownership access to sound
        connection.query("SELECT * FROM alarmbuddy.soundOwnership WHERE username = ? AND soundID = ?", [req.params.username, req.params.soundID], function(error, results, field){
          if(error) {
            // respond with error if query failed
            res.status(500).send('ERROR: database query error');
          }
          // checks if database responds with results or not 
          if (!(JSON.stringify(results) == JSON.stringify([]))){
            // select sound name and sound file based off the soundID provided in the request
            connection.query("SELECT soundName, soundFile FROM alarmbuddy.soundFile INNER JOIN alarmbuddy.soundInfo ON alarmbuddy.soundFile.soundID = alarmbuddy.soundInfo.soundID WHERE alarmbuddy.soundFile.soundID = ?", req.params.soundID, function(error, results, fields){
              if(error) {
                // respond with error if query failed
                res.status(500).send('ERROR: database query error');
              }
              // write the sound file to the tmp folder
              // *NOTE* : required for Google App Engine
              fs.writeFile('/tmp/' + results[0].soundName, results[0].soundFile, function (error) {
                if (error){
                  // respond with error if writing to file failed
                  res.status(500).send('ERROR: write to file error');
                }
                // respond with written file
                res.sendFile('/tmp/' + results[0].soundName, (error) => {
                  if (error){
                    // respond with error if sending file failed
                    res.status(500).send('ERROR: could not send file');
                  }
                  // delete the sound file from the temp folder
                  // no longer needed in tmp storage once the user recieves the file
                  fs.unlinkSync('/tmp/' + results[0].soundName);
                });
              });
            });
          } else {
            // respond with error if user does not have access to sound file or if the sound file doesn't exist in the database 
            res.status(404).send('ERROR: no access to audio file or file does not exist');
          }
        });
      } else { 
        // extracted token username did not match provided username from request so send error
        res.status(403).send('ERROR: access to provided user denied');
      }
    });
  }
});

// handler for deleting a sound
app.route('/deleteSound/:username/:soundID').delete(function(req,res,next) {
  // extract token from reqest header
  var token = req.headers.authorization;
    // check if token was provided in the request
    if (!token){
      res.status(401).send({ auth: false, message: 'no token provided' });
    } else {
      // verify that token provided is a valid token
      jwt.verify(token, config.secret, function(error, decoded) {
        // respond with error that token could not be authenticated
        if (error) {
          res.status(500).send({ auth: false, message: 'failed to authenticate token' });
        }
        // check if extracted username from token matches the username provided in the request
        if (decoded.id == req.params.username){
          // select row from soundOwnership table based off of the username and soundID provided
          connection.query("SELECT * FROM alarmbuddy.soundOwnership WHERE username = ? AND soundID = ?", [req.params.username, req.params.soundID], function(error, results, field){
            if(error) {
              // respond with error if query failed
              res.status(500).send('ERROR: database query error');
            }
            // check if user has access to the sound file based off results from above query
            if (!(JSON.stringify(results) == JSON.stringify([]))){
              // select the amount of people that own the sound file based off the soundID
              connection.query("SELECT COUNT(soundID) AS numberOfOwners FROM alarmbuddy.soundOwnership WHERE soundID = ?", [req.params.soundName, req.params.soundID], function(error, results, field){
                if(error) {
                  // respond with error if query failed
                  res.status(500).send('ERROR: database query error');
                }
                // if more than one person has ownership to the sound file...
                if (results[0].numberOfOwners > 1){
                  // delete the users access to the file in the soundOwnership table
                  connection.query("DELETE FROM alarmbuddy.soundOwnership WHERE username = ? AND soundID = ?", [req.params.username, req.params.soundName, req.params.soundID], function(error, results, field){
                    if(error) {
                      // respond with error if delete failed
                      res.status(500).send('ERROR: database query error');
                    }
                    // respond with delete successful
                    res.status(201).send('sound deleted successfully');
                  });
                } else {
                  // there is only 1 owner of the sound file
                  // this delete query cascades on delete into the soundOwnership and soundFile table getting rid of the sound file and all information about it in database
                  connection.query("DELETE FROM alarmbuddy.soundInfo WHERE soundID = ?", req.params.soundID, function(error, results, field){
                    if(error) {
                      // respond with error if delete failed
                      res.status(500).send('ERROR: database query error');
                    }
                    // respond with delete successful
                    res.status(201).send('sound deleted successfully');
                  });
                }
              });
            } else {
              // respond with error that the user doesn't have access to sound file
              res.status(500).send('ERROR: no access to audio file or file does not exist');
            }
          });
        } else {
          // extracted token username did not match provided username from request so send error
          res.status(403).send('ERROR: access to provided user denied');
        }
      });
    }
});

// handler for grabbing list of sounds from database that the user owns
app.route('/sounds/:username').get(function(req,res,next){
    // extract token from request header
    var token = req.headers.authorization;
    // check if token was provided in the request
    if (!token){
      res.status(401).send({ auth: false, message: 'no token provided' });
    } else {
      // verify that token provided is a valid token
      jwt.verify(token, config.secret, function(error, decoded) {
        // respond with error that token could not be authenticated
        if (error) {
          res.status(500).send({ auth: false, message: 'failed to authenticate token' });
        }
        // check if extracted username from token matches the username provided in the request
        if (decoded.id == req.params.username){
          // select rows from soundOwnership table based off the username provided in the request
          connection.query("SELECT * FROM alarmbuddy.soundOwnership INNER JOIN alarmbuddy.soundInfo ON alarmbuddy.soundOwnership.soundID = alarmbuddy.soundInfo.soundID WHERE username = ?", req.params.username, function(error, results, fields) {
            if (error) {
              // respond with error if database query failed
              res.status(500).send('ERROR: database query error');
            }
            // resond with list of songs that the user owns
            res.json(results);
          });
        } else {
          // extracted token username did not match provided username from request so send error
          res.status(403).send('ERROR: access to provided user denied');
        }
      });
    }
});

// handler for sharing sounds after they have been uploaded to the database
app.route('/shareSound/:sender/:receiver/:soundID').post(function(req,res,next){
  // extract token from reqest header
  var token = req.headers.authorization;
  // check if token was provided in the request
  if (!token){
    res.status(401).send({ auth: false, message: 'no token provided' });
  } else {
    // verify that token provided is a valid token
    jwt.verify(token, config.secret, function(error, decoded) {
      // respond with error that token could not be authenticated
      if (error) {
        res.status(500).send({ auth: false, message: 'failed to authenticate token' });
      }
      // check if extracted username from token matches the username provided in the request
      if (decoded.id == req.params.sender){
        connection.query("SELECT * FROM alarmbuddy.userBlockList WHERE blockedBy = ? AND blocked = ?", [req.params.receiver, req.params.sender], function(error, results, fields){
          if (error){
            res.status(500).send('ERROR: database query error');
          }
          if ((JSON.stringify(results) == JSON.stringify([]))){
            // query the soundOwnership table to see if user has access to the file they want to share with another user
            connection.query("SELECT * FROM alarmbuddy.soundOwnership WHERE username = ? AND soundID = ?", [req.params.sender, req.params.soundID], function(error, results, field){
              if(error) {
                // respond with error if query failed
                res.status(500).send('ERROR: database query error');
              }
              // check if the query above responded with a row from the soundOwnership table
              if (!(JSON.stringify(results) == JSON.stringify([]))){
                connection.query("SELECT * FROM alarmbuddy.soundOwnership WHERE username = ? AND soundID = ?", [req.params.receiver, req.params.soundID], function(error, results, field){
                  if(error) {
                    // respond with error if query failed
                    res.status(500).send('ERROR: database query error');
                  }
                  // check that query above responded with nothing since we want to make sure the receiver doesn't already have access
                  if (JSON.stringify(results) == JSON.stringify([])){
                    // create a new entry in the soundOwnership table for the receiver of the sound being shared
                    connection.query("REPLACE INTO alarmbuddy.soundOwnership SET username = ?, soundID = ?, sharedBy = ?", [req.params.receiver, req.params.soundID, req.params.sender], function(error, result, field){
                      if(error) {
                        // respond with error if insert/replace failed
                        res.status(500).send('ERROR: database query error');
                      } else {
                        // respond with success that sound was shared successfully
                        res.status(201).send("Shared sound successfully");
                      }
                    });
                  } else {
                    // respond with error that user already owns the sound
                    res.status(409).send('ERROR: user already owns the sound');
                  }
                });
              } else {
                // respond with error since user doesn't have access to the file they are trying to send
                res.status(404).send('ERROR: no access to audio file or file does not exist');
              }
            });
          } else {
            res.status(403).send('ERROR: cannot share sound with user because user has blocked you');
          }
        });
      } else {
        // extracted token username did not match provided username from request so send error
        res.status(403).send('ERROR: access to provided user denied');
      }
    });
  }
});

// handler for checking if the rest API is online
app.get('/status', (req, res) => res.send('working!'));

  //Port 8080 for Google App Engine
  //port 3000 i guess if youre doing local host 
  app.set('port', process.env.PORT || 8080);
  app.listen(8080);