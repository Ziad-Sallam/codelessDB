/* eslint-disable react-refresh/only-export-components */
import { useState, createContext, useEffect } from 'react';
import { RouterProvider, createBrowserRouter } from 'react-router-dom';
import Notification from './components/Notification.jsx';
import { routes } from './routes.jsx';

export const RecoveryContext = createContext();

function App() {
   const [email, setEmail] = useState("");
   const [otp, setOTP] = useState("");
   const router = createBrowserRouter(routes);
   const [msg, setMsg] = useState("");
   
   useEffect(() => {
      setMsg("test Notification")
   }, []);

   return (
      <RecoveryContext.Provider value={{ email, setEmail, otp, setOTP }}>
         <RouterProvider router={router} />
         <Notification message={msg} severity="success" duration={5000} />
      </RecoveryContext.Provider>
   );
}

export default App;