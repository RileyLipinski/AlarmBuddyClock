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
var FileType = require('file-type');
// package for reading files from requests
var multer  = require('multer');

// set temp file destination for sound files to /uploads and set sound file size to 30MB
var uploadSound = multer({ 
  dest: 'uploads/',
  limits: { fileSize: 30 * 1024 * 1024}
}).single('file');

// set temp file destination for image files to /uploads and set image file size to 30MB
var uploadImage = multer({ 
  dest: 'uploads/',
  limits: { fileSize: 30 * 1024 * 1024}
}).single('file');

// package for cross origion resource sharing
var cors = require('cors');

// use for reading and writing JSON objects
app.use(express.json());
app.use(express.urlencoded({
  extended: true
}));

// set origin to come from anywhere
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
          // select all usernames from the database that the username provided is friends with
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
app.route('/download/:username/:soundID').get(function(req,res,next) {

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
        // query soundOwnership table to see if requester owns access to sound
        connection.query("SELECT * FROM alarmbuddy.soundOwnership WHERE username = ? AND soundID = ?", [req.params.username, req.params.soundID], function(error, results, field){
          if(error) {
            // respond with error if insert failed
            res.status(500).send('ERROR: database query error.');
          }
          // checks if database responds with results or not 
          if (!(JSON.stringify(results) == JSON.stringify([]))){
            // select sound name and file based off the soundID provided 
            connection.query("SELECT soundName, soundFile FROM alarmbuddy.soundFile INNER JOIN alarmbuddy.soundInfo ON alarmbuddy.soundFile.soundID = alarmbuddy.soundInfo.soundID WHERE alarmbuddy.soundFile.soundID = ?", req.params.soundID, function(error, results, fields){
              if(error) {
                // respond with error if insert failed
                res.status(500).send('ERROR: database query error.');
              }
              // write the sound file to the tmp folder
              fs.writeFile('/tmp/' + results[0].soundName, results[0].soundFile, function (error) {
                if (error){
                  // respond with error if writing to file failed
                  res.status(500).send('ERROR: write to file error.');
                }
                // respond with written file
                res.sendFile('/tmp/' + results[0].soundName, (error) => {
                  if (error){
                    // respond with error if sending file failed
                    res.status(500).send('ERROR: could not send file.');
                  }
                  // delete the sound file from the temp folder
                  fs.unlinkSync('/tmp/' + results[0].soundName);
                });
              });
            });
          } else {
            // respond with error if user does not have access to file
            res.status(500).send('ERROR: no access to audio file or file does not exist.');
          }
        });
      } else { 
        // extracted token username did not match provided username from request so send error
        res.status(401).send('ERROR: Access to provided user denied.');
      }
    });
  }
});

// handler for uploading sound file to database
// handler for updloading sound file to database
app.post('/upload/:username', function (req, res, next) {
  uploadSound(req, res, function (err) {
    if (err instanceof multer.MulterError) {
      // respond with error if file size to large -> size defined above
      res.status(401).send('ERROR: file to large.')
    } else if (err) {
      // respond with error if file failed to upload
      res.status(401).send('ERROR: file upload error.')
    } else {
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
            // assign file description
            var soundDescription = req.body.soundDescription;

            // create the mp3 file
            var mp3 = fs.readFileSync(req.file.path);

            (async () => {
              const stream = fs.createReadStream(req.file.path);

              const fileType = await FileType.fromStream(stream);

              // check if audio file is mp3
              if (fileType.mime == "audio/mpeg" && fileType.ext == "mp3" && req.file.mimetype == "application/octet-stream"){
                // insert sound sound name and sound description into the soundInfo table
                connection.query("INSERT INTO alarmbuddy.soundInfo (soundName, soundDescription) VALUES (?, ?)", [soundName, soundDescription], function(error, result, field){
                  if(error) {
                    // respond with error if insert failed
                    res.status(500).send('ERROR: database query error.');
                  }else{
                    
                    // assign soundID from above query to var
                    var soundID = result.insertId;
                    // create soundfile entry using the results from previous query and the mp3 file
                    var soundFileEntry = [
                      [soundID, mp3]
                    ];
                    // insert the soundID and sound file into the soundFile table
                    connection.query("INSERT INTO alarmbuddy.soundFile (soundID, soundFile) VALUES ?", [soundFileEntry], function(error, result, field){
                      if(error) {
                        console.log("here");
                        // respond with error if insert failed
                        res.status(500).send('ERROR: database query error.');
                      }
                      
                      // delete the mp3 file from temp storage
                      fs.unlinkSync(req.file.path);
                      
                      // create ownership entry using the username and soundID
                      var ownershipEntry = [
                        [username, soundID]
                      ]
                      // insert ownership entry into soundOwnership table
                      connection.query("INSERT INTO alarmbuddy.soundOwnership (username, soundID) VALUES ?", [ownershipEntry], function(error, result,field) {
                        if(error) {
                          // respond with error if insert failed
                          res.status(500).send('ERROR: database query error.');
                        }
                        // respond with valid upload to database
                        res.status(201).send('database updated sucessfully');
                      });
                    });
                  }
                })
              } else {
                // delete the file because it wasn't an mp3 file
                fs.unlinkSync(req.file.path);
                res.status(401).send('ERROR: file type not supported.')
              }
            })();
          } else {
            // extracted token username did not match provided username from request so send error
            res.status(401).send('ERROR: Access to provided user denied.');
          }
        });
      }
    }
  });
});


// handler for grabbing list of sounds from database that the user owns
app.route('/sounds/:username').get(function(req,res,next){

    // extract token from request header
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
          // select the row from soundOwnership table based off the username provided in the request
          connection.query("SELECT * FROM alarmbuddy.soundOwnership INNER JOIN alarmbuddy.soundInfo ON alarmbuddy.soundOwnership.soundID = alarmbuddy.soundInfo.soundID WHERE username = ?", req.params.username, function(error, results, fields) {
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
app.route('/deleteSound/:username/:soundID').delete(function(req,res,next) {

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
          // select row from soundOwnership table based off of the username and soundID provided
          connection.query("SELECT * FROM alarmbuddy.soundOwnership WHERE username = ? AND soundID = ?", [req.params.username, req.params.soundID], function(error, results, field){
            if(error) {
              // respond with error if insert failed
              res.status(500).send('ERROR: database query error.');
            }
            // check if user has access to the sound file based off results from above query
            if (!(JSON.stringify(results) == JSON.stringify([]))){
              // select the amount of people that own the sound file based off the soundID
              connection.query("SELECT COUNT(soundID) AS numberOfOwners FROM alarmbuddy.soundOwnership WHERE soundID = ?", [req.params.soundName, req.params.soundID], function(error, results, field){
                if(error) {
                  // respond with error if insert failed
                  res.status(500).send('ERROR: database query error.');
                }
                // if there are more than 1 owner to the sound file
                if (results[0].numberOfOwners > 1){
                  // delete the users access to the file in the soundOwnership table
                  connection.query("DELETE FROM alarmbuddy.soundOwnership WHERE username = ? AND soundID = ?", [req.params.username, req.params.soundName, req.params.soundID], function(error, results, field){
                    if(error) {
                      // respond with error if insert failed
                      res.status(500).send('ERROR: database query error.');
                    }
                    // respond with delete successful
                    res.status(201).send('Alarm deleted successfully.');
                  });
                } else {
                  // there is only 1 owner of the sound file
                  // this delete query cascades on delete into the soundOwnership and soundFile table getting rid of the sound file and all information about it in database
                  connection.query("DELETE FROM alarmbuddy.soundInfo WHERE soundID = ?", req.params.soundID, function(error, results, field){
                    if(error) {
                      // respond with error if insert failed
                      res.status(500).send('ERROR: database query error.');
                    }
                    // respond with delete successful
                    res.status(201).send('Alarm deleted successfully.');
                  });
                }
              });
            } else {
              // respond with error that the user doesn't have access to sound file
              res.status(500).send('ERROR: no access to audio file or file does not exist.');
            }
          });
        }
      });
    }
});


// handler for updloading profile picture to the database
app.post('/setProfilePicture/:username', function (req, res, next) {
  uploadImage(req, res, function (err) {
    if (err instanceof multer.MulterError) {
      // respond with error if file size to large -> size defined above
      res.status(401).send('ERROR: file to large.');
    } else if (err) {
      // respond with error if file failed to upload
      res.status(401).send('ERROR: file upload error.')
    } else {
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
            
            // create the image file and assign to var
            var image = fs.readFileSync(req.file.path);

            // check if the file type is a png/jpeg file
            if (req.file.mimetype == "image/jpeg" || req.file.mimetype == "image/png"){
              var imageType;
              if (req.file.mimetype == "image/jpeg"){
                imageType = "jpeg";
              } else {
                imageType = "png";
              }
              // inserts profile picture if user doesn't have one or replaces profile picture if one already exists
              connection.query("REPLACE INTO alarmbuddy.profilePictures SET username = ?, profile_Photo = ?, image_Type = ?", [username, image, imageType], function(error, result, field){
                if(error) {
                  // respond with error if insert failed
                  res.status(500).send('ERROR: database query error.');
                }
                // delete the file from temporary storage
                fs.unlinkSync(req.file.path);
                // respond with profile pricture upload success
                res.status(201).send('profile picture successfully uploaded.');
              });
            } else {
              // delete the file because it wasn't a png/jpeg file
              fs.unlinkSync(req.file.path);
            }
          } else {
            // extracted token username did not match provided username from request so send error
            fs.unlinkSync(req.file.path);
            res.status(401).send('ERROR: Access to provided user denied.');
          }
        });
      } 
    }   
  });
});


app.route('/getProfilePicture/:username').get(function(req,res,next){
  var token = req.headers.authorization;
  if (!token){
    res.status(401).send({ auth: false, message: 'No token provided.' });
  }else {
    jwt.verify(token, config.secret, function(error, decoded) {
      if (error){
        res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
      }
      // check if extracted username from token matches the username provided in the request
      if (decoded.id == req.params.username){
        connection.query("SELECT profile_Photo, image_Type FROM alarmbuddy.profilePictures WHERE username = ?", [req.params.username], function(error, results, field){
          if(error) {
            // respond with error if insert failed
            res.status(500).send('ERROR: database query error.');
          }
          // write the sound file to the tmp folder

          var pathToImage = "/tmp/" + req.params.username + "_profilePhoto." + results[0].image_Type;
          fs.writeFile(pathToImage, results[0].profile_Photo, function (error) {
            if (error){
              // respond with error if writing to file failed
              res.status(500).send('ERROR: write to file error.');
            }
            // respond with written file
            res.sendFile(pathToImage, (error) => {
              if (error){
                // respond with error if sending file failed
                res.status(500).send('ERROR: could not send file.');
              }
              // delete the sound file from the temp folder
              fs.unlinkSync(pathToImage);
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



// handler for sharing sounds after they have been uploaded to the database
app.route('/shareSound/:sender/:receiver/:soundID').post(function(req,res,next){

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
      if (decoded.id == req.params.sender){
        // query the soundOwnership table to see if user has access to the file they want to send
        connection.query("SELECT * FROM alarmbuddy.soundOwnership WHERE username = ? AND soundID = ?", [req.params.sender, req.params.soundID], function(error, results, field){
          if(error) {
            // respond with error if insert failed
            res.status(500).send('ERROR: database query error.');
          }
          // check if the query above responded with a row from the soundOwnership table or not
          if (!(JSON.stringify(results) == JSON.stringify([]))){
            // create a new entry in the soundOwnership table for the receiver of the sound being sent
            connection.query("REPLACE INTO alarmbuddy.soundOwnership SET username = ?, soundID = ?", [req.params.receiver, req.params.soundID], function(error, result, field){
              if(error) {
                // respond with error if insert/replace failed
                res.status(500).send('ERROR: database query error.');
              } else {
                // respomd with success that sound was shared successfully
                res.status(201).send("Shared sound successfully.");
              }
            });
          } else {
            // respond with error since user doesn't have access to the file they are trying to send
            res.status(500).send('ERROR: no access to audio file or file does not exist.');
          }
        });
      } else {
        // extracted token username did not match provided username from request so send error
        res.status(401).send('ERROR: Access to provided user denied.');
      }
    });
  }
});

//handler for getting incoming friend requests
app.route('/requests/:username').get(function(req,res,next){
  //this gives the requests for the username provided
  var token = req.headers.authorization;
  if (!token){
    res.status(401).send({ auth: false, message: 'No token provided.' });
  }else{
    jwt.verify(token, config.secret, function(error, decoded) {
      if (error){
        res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
      }
      // check if extracted username from token matches the username provided in the request
      if (decoded.id == req.params.username){
        //do work here
        connection.query('SELECT * FROM alarmbuddy.friendRequests WHERE recipientUsername = ?', req.params.username, function(error, results, fields) {
          if (error){
            // respond with error if database query failed
            res.status(500).send('ERROR: database query error.');
          }
          res.json(results); 
        });
      }else { 
          // extracted token username did not match provided username from request so send error
          res.status(401).send('ERROR: Access to provided user denied.');
      }
    });
  }
});


// handler for sending friend requests
app.route('/sendRequest/:sender/:receiver').post(function(req,res,next){
  var token = req.headers.authorization;
  if (!token){
    res.status(401).send({ auth: false, message: 'No token provided.' });
  }else{
    jwt.verify(token, config.secret, function(error, decoded) {
      if (error){
        res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
      }
      if (decoded.id == req.params.sender){
        connection.query("REPLACE INTO alarmbuddy.friendRequests SET senderUsername = ?, recipientUsername = ?", [req.params.sender, req.params.receiver], function(error, results, fields) {
          if (error){
            // respond with error if database query failed
            res.status(500).send('ERROR: database query error.');
          }
          res.status(201).send('friend request sent successfully.');
        });
      } else { 
          // extracted token username did not match provided username from request so send error
          res.status(401).send('ERROR: Access to provided user denied.');
      }
    });
  }
});


app.route('/acceptFriendRequest/:receiver/:sender').post(function(req,res,next){
  var token = req.headers.authorization;
  if (!token){
    res.status(401).send({ auth: false, message: 'No token provided.' });
  }else{
    jwt.verify(token, config.secret, function(error, decoded) {
      if (error){
        res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
      }
      if (decoded.id == req.params.receiver){
        connection.query("SELECT * FROM alarmbuddy.friendRequests WHERE senderUsername = ? AND recipientUsername = ?", [req.params.sender, req.params.receiver], function(error, results, fields) {
          if (error){
            // respond with error if database query failed
            res.status(500).send('ERROR: database query error.');
          }
          if (!(JSON.stringify(results) == JSON.stringify([]))){

            var friendsEntry = [
              [req.params.sender, req.params.receiver]
            ]
            connection.query("INSERT INTO alarmbuddy.friendsWith (username1, username2) VALUES ?", [friendsEntry], function(error, results, fields) {
              if (error){
                // respond with error if database query failed
                res.status(500).send('ERROR: database query error.');
              }
              connection.query("DELETE FROM alarmbuddy.friendRequests WHERE senderUsername = ? AND recipientUsername = ?", [req.params.sender, req.params.receiver], function(error, results, fields) {
                if (error){
                  // respond with error if database query failed
                  res.status(500).send('ERROR: database query error.');
                }
                res.status(201).send('friend request accepted successfully.');
              });
            });
          } else {
            res.status(500).send('ERROR: friend request does not exist.');
          }
        });
      } else { 
        // extracted token username did not match provided username from request so send error
        res.status(401).send('ERROR: Access to provided user denied.');
    }
    });
  }
});


// handler for canceling a sent friend request
// the sender cancels the request
app.route('/cancelFriendRequest/:sender/:receiver').post(function(req,res,next){
  var token = req.headers.authorization;
  if (!token){
    res.status(401).send({ auth: false, message: 'No token provided.' });
  }else{
    jwt.verify(token, config.secret, function(error, decoded) {
      if (error){
        res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
      }
      if (decoded.id == req.params.sender){
        connection.query("DELETE FROM alarmbuddy.friendRequests WHERE senderUsername = ? AND recipientUsername = ?", [req.params.sender, req.params.receiver], function(error, results, fields) {
          if (error){
            // respond with error if database query failed
            res.status(500).send('ERROR: database query error.');
          }
          if (results.affectedRows == 0){
            res.status(500).send('ERROR: friend request does not exist.');
          } else {
            res.status(201).send('request canceled successfully');
          }
        });
      } else {
        res.status(500).send('ERROR: friend request does not exist.');
      }
    });
  }
});

//handler for denying a friend request
// the receiver cancels the request
app.route('/denyFriendRequest/:receiver/:sender').post(function(req,res,next){
  var token = req.headers.authorization;
  if (!token){
    res.status(401).send({ auth: false, message: 'No token provided.' });
  }else{
    jwt.verify(token, config.secret, function(error, decoded) {
      if (error){
        res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
      }
      if (decoded.id == req.params.receiver){
        connection.query("DELETE FROM alarmbuddy.friendRequests WHERE senderUsername = ? AND recipientUsername = ?", [req.params.sender, req.params.receiver], function(error, results, fields) {
          if (error){
            // respond with error if database query failed
            res.status(500).send('ERROR: database query error.');
          }
          if (results.affectedRows == 0){
            res.status(500).send('ERROR: friend request does not exist.');
          } else {
            res.status(201).send('friend request denied successfully');
          }
        });
      } else {
        res.status(500).send('ERROR: friend request does not exist.');
      }
    });
  }
});


//handler for deleting a friend from friends list
app.route('/deleteFriend/:username/:friend').post(function(req,res,next){
  var token = req.headers.authorization;
  if (!token){
    res.status(401).send({ auth: false, message: 'No token provided.' });
  }else{
    jwt.verify(token, config.secret, function(error, decoded) {
      if (error){
        res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
      }
      if (decoded.id == req.params.username){
        connection.query("DELETE FROM alarmbuddy.friendsWith WHERE (username1 = ? AND username2 = ?) OR (username2 = ? AND username1 = ?)", [req.params.username, req.params.friend, req.params.friend, req.params.username], function(error, results, fields) {
          if (error){
            // respond with error if database query failed
            res.status(500).send('ERROR: database query error.');
          }
          if (results.affectedRows == 0){
            res.status(500).send('ERROR: friend request does not exist.');
          } else {
            res.status(201).send('removed friend successfully.');
          }
        });
      } else {
        res.status(500).send('ERROR: friend request does not exist.');
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