import Register from './pages/auth/Register';
import LogIn from './pages/auth/LogIn';
import DiagramPage from './pages/diagrams/DiagramPage.jsx';
import Schema from './pages/schemaDrawing/schema.jsx';
import UserProfile from './pages/userprofile/UserProfile.jsx';
import CannedQueries from './pages/cannedquery/CannedQueries.jsx';
import ServersPage from './pages/servers/ServersPage.jsx';

import { ProtectedRoute, PublicRoute } from "./components/Routing.jsx";
import DatabaseConfiguration from './pages/databaseConfiguration/DatabaseManager.js';
import QueryRunner from './pages/databaseManager/QueryRunner.js';
import Discover from './pages/discover/Discover.jsx';
import SchemaPreview from './pages/schemaPreviewing/SchemaPreview.jsx';
import CreateSchema from './pages/schemaPublishing/CreateSchema.jsx';
import DesignerProfile from './pages/designerprofile/DesignerProfile.jsx';
import PageNotFound from './pages/notFound/PageNotFound.jsx';


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
    path: "/diagrams/:roomId",
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
    )
  }, {
    path: "/discover",
    element: (
      <ProtectedRoute>
        <Discover />
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
    )
  }, {
    path: "/schema/preview/:id",
    element: (
      <ProtectedRoute>
        <SchemaPreview />
      </ProtectedRoute>
    ),
  },
  {
    path: "/database-manager/:id",
    element: (
      <ProtectedRoute>
        <QueryRunner />
      </ProtectedRoute>
    )
  }, {
    path: "/schema/create",
    element: (
      <ProtectedRoute>
        <CreateSchema />
      </ProtectedRoute>
    ),
  },
  {
    path: "/designer/:username",
    element: (
      <ProtectedRoute>
        <DesignerProfile />
      </ProtectedRoute>
    ),
  },
  {
    path: "/*",
    element: (
      <PageNotFound />
    ),
  },
];
