import { useState } from 'react'
import './App.css'
import LogIn from './pages/login/LogIn.jsx'
import SignUp from './pages/signup/SignUp.jsx'
import { RouterProvider,createBrowserRouter } from 'react-router-dom'

function App() {
  const router = createBrowserRouter([
    {
      path:'/login',
      element:<LogIn/>
    },
    {
      path:'/signup',
      element:<SignUp/>
    }
  ])

  return (
    <div className="App">
    <RouterProvider router={router}/>
    </div>
  )
}

export default App
