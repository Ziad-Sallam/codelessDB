import Register from './pages/auth/Register';
import LogIn from './pages/auth/LogIn';
import DiagramPage from './pages/diagrams/DiagramPage.jsx';
import Schema from './pages/schemaDrawing/schema.jsx';
import UserProfile from './pages/userprofile/UserProfile.jsx';
import CannedQueries from './pages/cannedquery/CannedQueries.jsx';
import ServersPage from './pages/servers/ServersPage.jsx';

import { ProtectedRoute, PublicRoute } from "./components/Routing.jsx";
import TopBar from './components/TopBar.jsx';
import LeftPanel from './components/LeftPanel';
import DatabaseConfiguration from './pages/databaseConfiguration/DatabaseManager.js';
import QueryRunner from './pages/databaseManager/QueryRunner.js';

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
    path: "/canned-queries",
    element: (
      <ProtectedRoute>
        <CannedQueries />
      </ProtectedRoute>
    ),
  },
  {
    path: "/servers",
    element: (
      <ProtectedRoute>
        <ServersPage />
      </ProtectedRoute>
    ),
  },

  {
    path: "/database-configuration",
    element: (
      <ProtectedRoute>
        <DatabaseConfiguration />
      </ProtectedRoute>
    ),
  },
  {
    path: "/database-manager/:id",
    element: (
      <ProtectedRoute>
        <QueryRunner />
      </ProtectedRoute>
    ),
  },
];
