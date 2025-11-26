import React, { useEffect, useState } from "react";
import { Snackbar, Alert } from "@mui/material";

// const POP_SOUND_URL = "https://www.soundjay.com/buttons/sounds/button-3.mp3";

export default function Notification({ message, severity = "info", duration = 3000 }) {
	const [open, setOpen] = useState(false);

	useEffect(() => {
		if (message) {
			// Play pop sound
			// const audio = new Audio(POP_SOUND_URL);
			// audio.play().catch(() => { });

			setOpen(true);
			const timer = setTimeout(() => setOpen(false), duration);
			return () => clearTimeout(timer);
		}
	}, [message, duration]);

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
