import React, { useState, useEffect } from "react";
import {
	AppBar, Toolbar, IconButton, Avatar,
	Typography, Paper, InputBase, Box, Link,
	Popover, Stack, TextField, Button,
} from "@mui/material";

import CloseIcon from "@mui/icons-material/Close";
import SearchIcon from "@mui/icons-material/Search";
import FilterListIcon from "@mui/icons-material/FilterList";
import { useNavigate } from "react-router-dom";
import { getInitials } from "../pages/diagrams/Contributors";
import { useNotification } from "./NotificationContext";

import { getUserInfo, searchDiagrams } from "../pages/diagrams/fetch";

const INITIAL_DATE = "2025-01-01";

const FINAL_DATE = (() => {
	const d = new Date();
	const yyyy = d.getFullYear();
	const mm = String(d.getMonth() + 1).padStart(2, "0");
	const dd = String(d.getDate()).padStart(2, "0");
	return `${yyyy}-${mm}-${dd}`;
})();

export default function TopBar(props) {
	const { showError } = useNotification();

	const { onSearchResults, pageSize = 10, page, loadDiagrams } = props;
	const navigate = useNavigate();

	const [username, setUserName] = useState("");
	const [userImage, setUserImage] = useState("");
	const [filterAnchor, setFilterAnchor] = useState(null);

	const [search, setSearch] = useState(null);
	const [dateFrom, setDateFrom] = useState(null);
	const [dateTo, setDateTo] = useState(null);

	useEffect(() => {
		const fetchUser = async () => {
			try {
				const user = await getUserInfo();
				setUserName(user.username);
				setUserImage(user.picture);
			} catch (error) {
				showError(error);
			}
		};

		fetchUser();
	}, []);

	useEffect(() => {
		if (search !== null || (dateFrom !== null && dateTo !== null)) {
			handleSearch(page);

		} else {
			loadDiagrams(page);
		}
	}, [page]);

	const handleSearch = async (idx) => {
		try {
			if (search === "") {
				const resp = await searchDiagrams(idx, pageSize, null, dateFrom, dateTo);
				onSearchResults && onSearchResults(resp);
			} else {
				const resp = await searchDiagrams(idx, pageSize, search, dateFrom, dateTo);
				onSearchResults && onSearchResults(resp);
			}

		} catch (error) {
			showError(error);
		}
	};

	const openFilter = (e) => setFilterAnchor(e.currentTarget);
	const closeFilter = () => setFilterAnchor(null);

	const applyFilter = async () => {
		closeFilter();
		await handleSearch(0);
	};

	const clearFilter = async () => {
		setDateFrom(null);
		setDateTo(null);
		closeFilter();

		await handleSearch(0);
	};

	const handleKeyPress = (e) => {
		if (e.key === "Enter") {
			handleSearch(0);
		}
	};

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
				{/* CENTER — search bar */}
				<Paper
					elevation={2}
					sx={{
						display: "flex",
						alignItems: "center",
						px: 2,
						py: 0.8,
						borderRadius: 7,
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
					<InputBase
						value={search || ""}
						onChange={(e) => setSearch(e.target.value)}
						onKeyPress={handleKeyPress}
						placeholder="Search diagrams..."
						sx={{ flex: 1, fontSize: 15, paddingLeft: 2 }}
					/>

					<SearchIcon
						sx={{
							mr: 1.4,
							color: "text.secondary",
							fontSize: 22,
							cursor: "pointer",
						}}
						onClick={() => handleSearch(0)}
					/>

					<IconButton onClick={openFilter}>
						<FilterListIcon />
					</IconButton>
				</Paper>

				<Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
					<Link href="/profile" style={{ textDecoration: "none" }}>
						<Typography
							variant="body2"
							sx={{
								width: "auto",
								fontSize: 23,
								marginRight: 2,
								display: { sm: "block" },
								cursor: "pointer",
								fontWeight: 540,
								color: "background.dark",
							}}
						>
							{username}
						</Typography>
					</Link>

					<Avatar
						onClick={() => navigate("/profile")}
						src={userImage || undefined}
						alt={username}
						sx={{
							bgcolor: userImage ? undefined : "primary.main",
							cursor: "pointer",
							transition: "0.2s",
							"&:hover": {
								transform: "scale(1.07)",
								boxShadow: 3,
							},
						}}
					>
						{(!userImage || userImage.trim() === "") && getInitials(username)}
					</Avatar>
				</Box>
			</Toolbar>


			<Popover
				open={Boolean(filterAnchor)}
				anchorEl={filterAnchor}
				onClose={closeFilter}
				anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
				transformOrigin={{ vertical: "top", horizontal: "right" }}
			>
				<Box sx={{ p: 2, width: 300, gap: 10 }}>
					{/* Header with title + X button */}
					<Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
						<Typography variant="subtitle1">Filter diagrams</Typography>
						<IconButton size="small" onClick={closeFilter}>
							<CloseIcon fontSize="small" />
						</IconButton>
					</Box>

					<Stack spacing={2}>
						<TextField
							label="Created from"
							type="date"
							InputLabelProps={{ shrink: true }}
							value={dateFrom || INITIAL_DATE}
							onChange={(e) => setDateFrom(e.target.value)}
							inputProps={{ pattern: "\\d{4}-\\d{2}-\\d{2}" }}
						/>
						<TextField
							label="Created to"
							type="date"
							InputLabelProps={{ shrink: true }}
							value={dateTo || FINAL_DATE}
							onChange={(e) => setDateTo(e.target.value)}
							inputProps={{ pattern: "\\d{4}-\\d{2}-\\d{2}" }}
						/>

						<Box sx={{ display: "flex", gap: 1, justifyContent: "flex-end", pt: 1 }}>
							<Button onClick={clearFilter}>Clear</Button>
							<Button variant="contained" onClick={applyFilter}>
								Apply
							</Button>
						</Box>
					</Stack>
				</Box>
			</Popover>

		</AppBar>
	);
}