import React from "react";
import {
	AppBar,
	Toolbar,
	IconButton,
	Avatar,
	Typography,
	Paper,
	InputBase,
	Box,
} from "@mui/material";

import SearchIcon from "@mui/icons-material/Search";
import FilterListIcon from "@mui/icons-material/FilterList";
import { useNavigate } from "react-router-dom";

export default function TopBar(props) {
	const { search, setSearch, onFilterOpen } = props;
	const navigate = useNavigate();

	return (
		<AppBar
			position="static"
			color="transparent"
			elevation={0}
			sx={{
				borderBottom: "1px solid rgba(0,0,0,0.06)",
				bgcolor: "background.light",
			}}
		>
			<Toolbar sx={{ display: "flex", justifyContent: "space-between", py: 1 }}>

				{/* LEFT — empty spacer (for perfect centering) */}
				{/* <Box sx={{ width: 120 }} /> */}

				{/* CENTER — search bar */}
				<Paper
					elevation={2}
					sx={{
						display: "flex",
						alignItems: "center",
						px: 2,
						py: 0.8,
						borderRadius: 4,
						flex: 1,
						maxWidth: 650,
						bgcolor: "background.paper",
						transition: "box-shadow 0.2s ease, transform 0.2s ease",
						"&:hover": {
							boxShadow: 6,
							transform: "translateY(-1px)",
						},
					}}
				>
					<SearchIcon sx={{ mr: 1.4, color: "text.secondary", fontSize: 22 }} />
					<InputBase
						value={search}
						onChange={(e) => setSearch(e.target.value)}
						placeholder="Search diagrams..."
						sx={{ flex: 1, fontSize: 15 }}
					/>

				</Paper>

				<IconButton onClick={onFilterOpen}>
					<FilterListIcon />
				</IconButton>

				<Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
					<Typography variant="body2" sx={{ width: 'auto', display: { sm: "block" } }}>
						Ahmed Ragy
					</Typography>

					<Avatar
						onClick={() => navigate("/profile")}
						sx={{
							bgcolor: "primary.main",
							cursor: "pointer",
							transition: "0.2s",
							"&:hover": {
								transform: "scale(1.07)",
								boxShadow: 3,
							},
						}}
					>
						AR
					</Avatar>
				</Box>
			</Toolbar>
		</AppBar>
	);
}
