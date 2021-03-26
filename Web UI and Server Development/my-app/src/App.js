
import React from 'react';

import Navbar from './components/Navbar';
import './App.css';
import Home from './components/pages/Home';
import { BrowserRouter as Router, Switch, Route } from 'react-router-dom';
import Footer from './components/Footer';

import Friends from './components/pages/Friends';
import UserHome from './components/pages/UserHome';
import SignUp from './components/pages/SignUp';
import Login from './components/pages/Login'; 


function App() {
  return (
    <>
      <Router>
        <Navbar />
        <Switch>
          <Route path='/' exact component={Home} />
          <Route path='/friends' component={Friends} />
          <Route path='/userhome' component={UserHome} />
          <Route path='/sign-up' component={SignUp} />
          <Route path='/login' component={Login} />
        </Switch>
      </Router>
    </>
  );
}

export default App;