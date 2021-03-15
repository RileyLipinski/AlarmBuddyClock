require('dotenv').config()
var express = require('express');
var app = express();
var bodyParser = require('body-parser');
const connection = require('./database');

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

app.get('/status', (req, res) => res.send('Working!'));

//Port 8080 for Google App Engine
app.set('port', process.env.PORT || 8080);
app.listen(8080);