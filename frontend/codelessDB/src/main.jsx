import { createRoot } from "react-dom/client";
import { CssVarsProvider } from "@mui/material/styles";
import { CssBaseline } from "@mui/material";
import App from "./App";
import theme from "./theme";
import { AuthProvider } from "./components/AuthProvider.jsx";

createRoot(document.getElementById("root")).render(
  <CssVarsProvider theme={theme}>
    <CssBaseline />
    <AuthProvider>
      <App />
    </AuthProvider>
  </CssVarsProvider>
);