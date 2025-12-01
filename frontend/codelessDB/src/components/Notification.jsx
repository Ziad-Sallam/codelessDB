import React, { useEffect, useState } from "react";
import { Snackbar, Alert } from "@mui/material";

export default function Notification({ message, severity = "info", duration = 3000, timestamp }) {
	const [open, setOpen] = useState(false);

	useEffect(() => {
		if (message) {
			setOpen(true);
			const timer = setTimeout(() => setOpen(false), duration);
			return () => clearTimeout(timer);
		}
	}, [message, duration, timestamp]);

	const handleClose = (event, reason) => {
		if (reason === "clickaway") return;
		setOpen(false);
	};

	return (
		<Snackbar
			open={open}
			anchorOrigin={{ vertical: "top", horizontal: "center" }}
			onClose={handleClose}
		>
			<Alert onClose={handleClose} severity={severity} sx={{ width: "100%" }}>
				{message}
			</Alert>
		</Snackbar>
	);
}
