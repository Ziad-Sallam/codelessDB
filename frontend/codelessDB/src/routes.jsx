import Register from './pages/auth/Register';
import LogIn from './pages/auth/LogIn';
import DiagramPage from './pages/diagrams/DiagramPage.jsx';
import Schema from './pages/schemaDrawing/schema.jsx';
import UserProfile from './pages/userprofile/UserProfile.jsx';

import {ProtectedRoute, PublicRoute} from "./components/Routing.jsx";
import DatabaseManager from './pages/databaseConfiguration/DatabaseManager.js';
import TopBar from './components/TopBar.jsx';

export const routes = [
  {
    path: "/password-reset",
    element: <Register />, 
  },
  {
    path: "/login",
    element: (
      <PublicRoute>
        <LogIn />
      </PublicRoute>
    ),
  },
  {
    path: "/register",
    element: (
      <PublicRoute>
        <Register />
      </PublicRoute>
    ),
  },
  {
    path: "/",
    element: (
      <ProtectedRoute>
        <DiagramPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/diagrams",
    element: (
      <ProtectedRoute>
        <DiagramPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/diagrams/:id",
    element: (
      <ProtectedRoute>
        <Schema />
      </ProtectedRoute>
    ),
  },
  {
    path: "/profile",
    element: (
      <ProtectedRoute>
        <UserProfile />
      </ProtectedRoute>
    ),
  },
  {
    path: "/database-configuration",
    element: (
      <ProtectedRoute>
        <DatabaseManager />
      </ProtectedRoute>
    ),
  },
];
