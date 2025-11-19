import React from "react";
import {
	Drawer,
	Box,
	Avatar,
	Typography,
	Divider,
	List,
	ListItemButton,
	ListItemIcon,
	ListItemText,
} from "@mui/material";
import FolderIcon from "@mui/icons-material/Folder";
import AccessTimeIcon from "@mui/icons-material/AccessTime";

const LEFT_BG = "#1E1E2F";

export default function LeftPanel({ leftNav, setLeftNav }) {
	return (
		<Drawer
			variant="permanent"
			sx={{
				width: 240,
				[`& .MuiDrawer-paper`]: {
					width: 240,
					boxSizing: "border-box",
					p: 2,
					bgcolor: LEFT_BG,
					color: "white",
					borderRight: "0",
				},
			}}
		>
			<Box sx={{ display: "flex", gap: 2, alignItems: "center", mb: 2 }}>
				<Avatar sx={{ bgcolor: "white", color: LEFT_BG }}>A</Avatar>
				<Box>
					<Typography variant="subtitle1">Ahmed Ragy</Typography>
					<Typography variant="caption" sx={{ opacity: 0.85 }}>
						Personal workspace
					</Typography>
				</Box>
			</Box>

			<Divider sx={{ borderColor: "rgba(255,255,255,0.08)", my: 1 }} />

			<List>
				<ListItemButton
					selected={leftNav === "recent"}
					onClick={() => setLeftNav("recent")}
					sx={{ borderRadius: 1 }}
				>
					<ListItemIcon sx={{ color: "white" }}>
						<AccessTimeIcon />
					</ListItemIcon>
					<ListItemText primary="Recents" primaryTypographyProps={{ fontWeight: 600 }} />
				</ListItemButton>
				<ListItemButton
					selected={leftNav === "all"}
					onClick={() => setLeftNav("all")}
					sx={{ borderRadius: 1, mb: 1 }}
				>
					<ListItemIcon sx={{ color: "white" }}>
						<FolderIcon />
					</ListItemIcon>
					<ListItemText primary="All diagrams" primaryTypographyProps={{ fontWeight: 600 }} />
				</ListItemButton>


			</List>

			<Box sx={{ flexGrow: 1 }} />

			<Divider sx={{ borderColor: "rgba(255,255,255,0.06)", my: 1 }} />
			<Typography variant="caption" sx={{ color: "rgba(255,255,255,0.7)" }}>
				Tip: Click a diagram to open it
			</Typography>
		</Drawer>
	);
}