/* eslint-disable react-refresh/only-export-components */
import { useState, createContext, useEffect } from 'react';
import { RouterProvider, createBrowserRouter } from 'react-router-dom';
import Notification from './components/Notification.jsx';
import { routes } from './routes.jsx';
import { NotificationProvider } from './components/NotificationContext.jsx';

export const RecoveryContext = createContext();

function App() {
   const [email, setEmail] = useState("");
   const [otp, setOTP] = useState("");
   const router = createBrowserRouter(routes);
   const [msg, setMsg] = useState("");
   
   useEffect(() => {
      // setMsg("test Notification")
      const token = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWhtZWRfcmFneTMiLCJzdWIiOiIxIiwiaWF0IjoxNzY0MTIzMTkzLCJleHAiOjE3NjQyMDk1OTN9.zVF12w7GXDq7pBqTMM8MGURODb67qRTOrV4sE_-PbCJWI9KUomeCDY5a1ED7_onA";
      localStorage.setItem("authToken" , token)
   }, []);

   return (
      <RecoveryContext.Provider value={{ email, setEmail, otp, setOTP }}>
         <NotificationProvider>
            <RouterProvider router={router} />
         </NotificationProvider>
      </RecoveryContext.Provider>
   );
}

export default App;