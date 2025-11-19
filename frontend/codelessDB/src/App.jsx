/* eslint-disable react-refresh/only-export-components */
import { useState, createContext } from 'react';
import './App.css';
import { RouterProvider, createBrowserRouter } from 'react-router-dom';

import { routes } from './routes.jsx';

export const RecoveryContext = createContext();

function App() {
   const [email, setEmail] = useState("");
   const [otp, setOTP] = useState("");
   const router = createBrowserRouter(routes);
   
   return (
      <RecoveryContext.Provider value={{ email, setEmail, otp, setOTP }}>
         <div className="App">
            <RouterProvider router={router} />
         </div>
      </RecoveryContext.Provider>
   );
}

export default App;
