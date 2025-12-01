import LogIn from './pages/login/LogIn.jsx';
import SignUp from './pages/signup/SignUp.jsx';
import OTPInput from './pages/login/otp/OTPInput.jsx';
import Reset from './pages/login/reset/Reset.jsx';
import Recovered from './pages/login/Recovered/Recovered.jsx';
import DiagramPage from './pages/diagrams/DiagramPage.jsx';
import Schema from './pages/schemaDrawing/schema.jsx';
import UserProfile from './pages/userprofile/UserProfile.jsx';

import ProtectedRoute from "./components/ProtectedRoute.jsx";

export const routes = [
  // Public routes
  {
    path: '/',
    element: <LogIn />
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
  },

  // Protected routes
  {
    path: '/diagrams',
    element: (
      <ProtectedRoute>
        <DiagramPage />
      </ProtectedRoute>
    )
  },
  {
    path: '/diagrams/:id',
    element: (
      <ProtectedRoute>
        <Schema />
      </ProtectedRoute>
    )
  },
  {
    path: '/profile',
    element: (
      <ProtectedRoute>
        <UserProfile />
      </ProtectedRoute>
    )
  }
];
