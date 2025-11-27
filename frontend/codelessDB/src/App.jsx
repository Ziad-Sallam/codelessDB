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
      const token = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoibm91ciBha3JhbSIsInN1YiI6IjEiLCJpYXQiOjE3NjQxODI2NjMsImV4cCI6MTc2NDI2OTA2M30.V-zYvxeG5-VQa4bju8xJ2huQHIKPU_n0vSRDUMOqEWPeEvsmE9IT2XOxfvtrhuzB";
      // const token = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VySWQiOjIsInVzZXJuYW1lIjoiYWhtZWQgcmFneSIsInN1YiI6IjIiLCJpYXQiOjE3NjQxODMwMTEsImV4cCI6MTc2NDI2OTQxMX0.kzXIP5dJ3gqYZg_Xr_5zcj0Ogbom46K5HFE02NqsNE_erBNNMaW7e8WEkxOD6O-k";
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