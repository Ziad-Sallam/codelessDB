import { createTheme } from "@mui/material/styles";

const theme = createTheme({
	palette: {
		primary: {
			main: "#0b3d91",
			light: "#4C84FF",
			dark: "#06275F",
		},
		secondary: {
			main: "#FF8C00",
		},
		background: {
			light: "#f6f8fb",
			dark: "#082043",
			gradient: "linear-gradient(180deg, #0a2751ff 0%, #05183aff 100%)",
		},
		text: {
			primary: "#0f172a",
			secondary: "#64748b",
		},
	},
	typography: {
		fontFamily: "Inter, Roboto, Arial, sans-serif",
	},
});

export default theme;