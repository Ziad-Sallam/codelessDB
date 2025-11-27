import React from "react";
import { createRoot } from "react-dom/client";
import { CssVarsProvider } from "@mui/material/styles";
import { CssBaseline } from "@mui/material";
import App from "./App";
import theme from "./theme";

createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <CssVarsProvider theme={theme}>
      <CssBaseline />
      <App />
    </CssVarsProvider>
  </React.StrictMode>
);
