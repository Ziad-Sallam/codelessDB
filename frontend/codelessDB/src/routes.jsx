import Register from './pages/auth/Register';
import LogIn from './pages/auth/LogIn';
import DiagramPage from './pages/diagrams/DiagramPage.jsx';
import Schema from './pages/schemaDrawing/schema.jsx';
import UserProfile from './pages/userprofile/UserProfile.jsx';
import Discover from './pages/discover/Discover.jsx';
import SchemaPreview from './pages/schemaPreviewing/SchemaPreview.jsx';
import CreateSchema from './pages/schemaPublishing/CreateSchema.jsx';
import DesignerProfile from './pages/designerprofile/DesignerProfile.jsx';
import { ProtectedRoute, PublicRoute } from "./components/Routing.jsx";

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
    path: "/discover",
    element: (
      <ProtectedRoute>
        <Discover />
      </ProtectedRoute>
    ),
  },
  {
    path: "/schema/preview/:id",
    element: (
      <ProtectedRoute>
        <SchemaPreview />
      </ProtectedRoute>
    ),
  },
  {
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
];
