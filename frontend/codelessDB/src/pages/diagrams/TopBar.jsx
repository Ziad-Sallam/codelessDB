import React from "react";
import {
	AppBar,
	Toolbar,
	IconButton,
	Badge,
	Avatar,
	Typography,
	Box,
} from "@mui/material";
import NotificationsIcon from "@mui/icons-material/Notifications";

const APP_PRIMARY = "#4C84FF";

export default function TopBar({ notifications, onNotifOpen }) {
	return (
		<AppBar position="static" color="transparent" elevation={0} sx={{ borderBottom: "1px solid rgba(0,0,0,0.06)" }}>
			<Toolbar sx={{ gap: 2, justifyContent: "flex-end" }}>
				<IconButton aria-label="notifications" onClick={onNotifOpen}>
					<Badge badgeContent={notifications?.filter((n) => n.unread).length || 0} color="error">
						<NotificationsIcon />
					</Badge>
				</IconButton>

				<Box sx={{ display: "flex", alignItems: "center", gap: 1, ml: 1 }}>
					<Typography variant="body2" sx={{ minWidth: 90, textAlign: "right" }}>
						Abdelrhman khaled
					</Typography>
					<Avatar sx={{ bgcolor: APP_PRIMARY }}>Ak</Avatar>
				</Box>
			</Toolbar>
		</AppBar>
	);
}