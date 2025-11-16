import { useState, createContext } from 'react';
import './App.css';
import LogIn from './pages/login/LogIn.jsx';
// import SignUp from './pages/signup/SignUp.jsx';
import {OTPInput} from './pages/login/otp/OTPInput.jsx';
// import Reset from './pages/reset/Reset.jsx';
import { RouterProvider, createBrowserRouter } from 'react-router-dom';

export const RecoveryContext = createContext();

function App() {
  const [email, setEmail] = useState("");
  const [otp, setOTP] = useState("");
  const router = createBrowserRouter([
    {
      path: '/login',
      element: <LogIn />
    },
    // {
    //   path: '/signup',
    //   element: <SignUp />
    // },
    {
      path: '/otp',
      element: <OTPInput />
    }
    // ,
    // {
    //   path: '/reset',
    //   element: <Reset />
    // }
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
