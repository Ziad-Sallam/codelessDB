import React from "react";
import {
	AppBar,
	Toolbar,
	IconButton,
	Badge,
	Avatar,
	Typography,
	Paper,
	InputBase,
	Box,
} from "@mui/material";
import MenuIcon from "@mui/icons-material/Menu";
import SearchIcon from "@mui/icons-material/Search";
import FilterListIcon from "@mui/icons-material/FilterList";
import NotificationsIcon from "@mui/icons-material/Notifications";

const APP_PRIMARY = "#4C84FF";

export default function TopBar({ search, setSearch, notifications, onFilterOpen, onNotifOpen }) {
	return (
		<AppBar position="static" color="transparent" elevation={0} sx={{ borderBottom: "1px solid rgba(0,0,0,0.06)" }}>
			<Toolbar sx={{ gap: 2 }}>
				{/* <IconButton disableRipple>
					<MenuIcon />
				</IconButton> */}

				<Paper
					elevation={1}
					sx={{ display: "flex", alignItems: "center", px: 2, py: 0.25, borderRadius: 3, flex: 1, maxWidth: 820 }}
				>
					<SearchIcon sx={{ mr: 1, color: "text.secondary" }} />
					<InputBase value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search diagrams by name" sx={{ flex: 1 }} />
				</Paper>

				<IconButton aria-label="filter" onClick={onFilterOpen}>
					<FilterListIcon />
				</IconButton>

				<IconButton aria-label="notifications" onClick={onNotifOpen}>
					<Badge badgeContent={notifications.filter((n) => n.unread).length} color="error">
						<NotificationsIcon />
					</Badge>
				</IconButton>

				<Box sx={{ display: "flex", alignItems: "center", gap: 1, ml: 1 }}>
					<Typography variant="body2" sx={{ minWidth: 90, textAlign: "right" }}>
						Ahmed Ragy
					</Typography>
					<Avatar sx={{ bgcolor: APP_PRIMARY }}>AR</Avatar>
				</Box>
			</Toolbar>
		</AppBar>
	);
}
