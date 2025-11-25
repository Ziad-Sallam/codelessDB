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
      // setMsg("test Notification")
      const token = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWhtZWRfcmFneTMiLCJzdWIiOiIxIiwiaWF0IjoxNzY0MDM0OTAyLCJleHAiOjE3NjQxMjEzMDJ9.3Gr4XfnK9Wy1qpiyawmECHh1AD2n4BZWjsex7c9h-Bz4Qf3r1qRkKvbCW8f0omtY";
      localStorage.setItem("authToken" , token)
   }, []);

   return (
      <RecoveryContext.Provider value={{ email, setEmail, otp, setOTP }}>
         <RouterProvider router={router} />
         <Notification message={msg} severity="success" duration={5000} />
      </RecoveryContext.Provider>
   );
}

export default App;