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

import StorageIcon from "@mui/icons-material/Storage";
import SchemaIcon from '@mui/icons-material/Schema';
import ListAltIcon from '@mui/icons-material/ListAlt';

import { useNavigate } from "react-router-dom";


export default function LeftPanel({ leftNav, setLeftNav }) {
	const navigate = useNavigate();

	return (
		<Drawer
			variant="permanent"
			sx={{
				width: 280,
				[`& .MuiDrawer-paper`]: {
					width: 280,
					boxSizing: "border-box",
					p: 2,
					background:  (theme) => theme.palette.background.gradient,
					color: "white",
					boxShadow: "0 5px 22px rgba(0,0,0,0.2)", // simple shadow
				},
			}}
		>
			<Box sx={{ display: "flex", gap: 2, alignItems: "center", mb: 2 }}>
				<Box>
					<Typography variant="h6" style={{ fontWeight: "600", fontSize: "28px", fontFamily: 'Poppins', color: "#cadfffff" }}>CodelessDB</Typography>
					<Typography variant="caption" sx={{ opacity: 1, fontSize: "13px" }}>
						Shared workspace
					</Typography>
				</Box>
			</Box>

			<Divider sx={{ borderColor: "rgba(255,255,255,0.08)", my: 1 }} />

			<List>

				<ListItemButton
					selected={leftNav === "all"}
					onClick={() => navigate('/discover')}
					sx={{ borderRadius: 1, mb: 1 }}
				>
					<ListItemIcon sx={{ color: "white" }}>
						<SchemaIcon />
					</ListItemIcon>
					<ListItemText primary="Discover" />
				</ListItemButton>

				<ListItemButton
					selected={leftNav === "all"}
					onClick={() => navigate('/diagrams')}
					sx={{ borderRadius: 1, mb: 1 }}
				>
					<ListItemIcon sx={{ color: "white" }}>
						<SchemaIcon />
					</ListItemIcon>
					<ListItemText primary="Your diagrams" />
				</ListItemButton>

				<ListItemButton
					selected={leftNav === "all"}
					onClick={() => navigate('/servers')}
					sx={{ borderRadius: 1, mb: 1 }}
				>
					<ListItemIcon sx={{ color: "white" }}>
						<StorageIcon />
					</ListItemIcon>
					<ListItemText primary="Servers" />
				</ListItemButton>

				<ListItemButton
					selected={leftNav === "all"}
					onClick={() => navigate('/canned-queries')}
					sx={{ borderRadius: 1, mb: 1 }}
				>
					<ListItemIcon sx={{ color: "white" }}>
						<ListAltIcon />
					</ListItemIcon>
					<ListItemText primary="Canned Queries" />
				</ListItemButton>

			</List>

			<Box sx={{ flexGrow: 1 }} />

			<Divider sx={{ borderColor: "rgba(255,255,255,0.06)", my: 1 }} />
		</Drawer>
	);
}