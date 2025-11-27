import { useState, createContext } from 'react';
import './App.css';
import LogIn from './pages/login/LogIn.jsx';
import SignUp from './pages/signup/SignUp.jsx';
import OTPInput from './pages/login/otp/OTPInput.jsx';
import Reset from './pages/login/reset/Reset.jsx';
import Recovered from './pages/login/recovered/Recovered.jsx';
import Home from './pages/Home.jsx';
import { RouterProvider, createBrowserRouter } from 'react-router-dom';
import Schema from './pages/schemaDrawing/schema.jsx';

export const RecoveryContext = createContext();

function App() {
  const [email, setEmail] = useState("");
  const [otp, setOTP] = useState("");

  const router = createBrowserRouter([
    {
      path: '/',
      element: <Home />
    },
    {
      path: '/diagram/:id',
      element: <Schema />
    },
    {
      path: '/login',
      element: <LogIn />
    },
    {
      path: '/signup',
      element: <SignUp />
    },
    {
      path: '/otp',
      element: <OTPInput />
    },
    {
      path: '/reset',
      element: <Reset />
    },
    {
      path: '/recovered',
      element: <Recovered />
    }
  ]);
  
  return (
    <RecoveryContext.Provider value={{ email, setEmail, otp, setOTP }}>
      <div className="App">
        <RouterProvider router={router} />
      </div>
    </RecoveryContext.Provider>
  );
}

export default App;