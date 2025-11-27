/* eslint-disable react-hooks/set-state-in-effect */
import React, { useEffect, useMemo, useState } from "react";
import {
	Avatar,
	Box,
	Button,
	Card,
	CardContent,
	CardMedia,
	Divider,
	Grid,
	Popover,
	Stack,
	TextField,
	Typography,
} from "@mui/material";
import { createTheme, ThemeProvider } from "@mui/material/styles";
import InsertPhotoIcon from "@mui/icons-material/InsertPhoto";
import LeftPanel from "./LeftPanel.jsx";
import TopBar from "./TopBar.jsx";
import DiagramCard from "./DiagramCard.jsx";

// ------------------------
// Styles & theme
// ------------------------
const APP_PRIMARY = "#0b3d91"; // deep blue
const LEFT_BG = "#082043"; // left panel dark blue

const theme = createTheme({
	palette: {
		primary: { main: APP_PRIMARY },
		background: { default: "#f6f8fb", paper: "#ffffff" },
		text: { primary: "#0f172a" },
	},
	typography: { fontFamily: "Inter, Roboto, Arial, sans-serif" },
});

// ------------------------
// Main DiagramPage
// ------------------------
export default function DiagramPage() {
	const [diagrams, setDiagrams] = useState([]);
	const [leftNav, setLeftNav] = useState("all");
	const [search, setSearch] = useState("");

	// filter popover
	const [filterAnchor, setFilterAnchor] = useState(null);
	const [dateFrom, setDateFrom] = useState("");
	const [dateTo, setDateTo] = useState("");

	// notifications popover
	const [notifAnchor, setNotifAnchor] = useState(null);
	const [notifications, setNotifications] = useState([
		{ id: 1, title: "Comment on Checkout Flow", body: "Looks good — see line 3", unread: true, date: "2025-11-15" },
		{ id: 2, title: "New collaborator added", body: "You were added to Project X", unread: false, date: "2025-11-01" },
	]);

	useEffect(() => {
		// Replace this with real backend fetch
		setDiagrams([
			{ id: "d1", name: "User Auth Flow", createdAt: "2025-10-18", modifiedAt: "2025-11-10", thumb: "https://picsum.photos/seed/auth/800/450" },
			{ id: "d2", name: "Checkout Flow", createdAt: "2025-08-05", modifiedAt: "2025-11-16", thumb: "https://picsum.photos/seed/checkout/800/450" },
			{ id: "d3", name: "Project Architecture", createdAt: "2024-12-02", modifiedAt: "2025-10-20", thumb: "" },
			{ id: "d4", name: "Onboarding", createdAt: "2025-07-01", modifiedAt: "2025-09-10", thumb: "https://picsum.photos/seed/onb/800/450" },
		]);
	}, []);

	// Filtering logic
	const filtered = useMemo(() => {
		const term = search.trim().toLowerCase();
		return diagrams
			.filter((d) => (term ? d.name.toLowerCase().includes(term) : true))
			.filter((d) => {
				if (!dateFrom && !dateTo) return true;
				const created = new Date(d.createdAt);
				if (dateFrom) {
					const f = new Date(dateFrom + "T00:00:00");
					if (created < f) return false;
				}
				if (dateTo) {
					const t = new Date(dateTo + "T23:59:59");
					if (created > t) return false;
				}
				return true;
			})
			.sort((a, b) => new Date(b.modifiedAt) - new Date(a.modifiedAt));
	}, [diagrams, search, dateFrom, dateTo]);

	const recent = filtered.slice(0, 6);

	// handlers
	const openFilter = (e) => setFilterAnchor(e.currentTarget);
	const closeFilter = () => setFilterAnchor(null);
	const applyFilter = () => {
		closeFilter();
	};
	const clearFilter = () => { setDateFrom(""); setDateTo(""); };

	const openNotif = (e) => setNotifAnchor(e.currentTarget);
	const closeNotif = () => setNotifAnchor(null);
	const markAllRead = () => setNotifications((s) => s.map((n) => ({ ...n, unread: false })));

	const handleOpenDiagram = (d) => {
		// placeholder: replace with routing / navigation
		alert(`Open diagram: ${d.name}`);
	};

	return (
		<ThemeProvider theme={theme}>
			<Box sx={{ display: "flex", minHeight: "100vh", bgcolor: "background.default" }}>
				<LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />

				<Box component="main" sx={{ flexGrow: 1 }}>
					<TopBar
						search={search}
						setSearch={setSearch}
						notifications={notifications}
						onFilterOpen={openFilter}
						onNotifOpen={openNotif}
					/>

					{/* Filter Popover */}
					<Popover open={Boolean(filterAnchor)} anchorEl={filterAnchor} onClose={closeFilter} anchorOrigin={{ vertical: "bottom", horizontal: "right" }} transformOrigin={{ vertical: "top", horizontal: "right" }}>
						<Box sx={{ p: 2, width: 300 }}>
							<Typography variant="subtitle1" sx={{ mb: 1 }}>Filter diagrams</Typography>
							<Stack spacing={1}>
								<TextField label="Created from" type="date" InputLabelProps={{ shrink: true }} value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} />
								<TextField label="Created to" type="date" InputLabelProps={{ shrink: true }} value={dateTo} onChange={(e) => setDateTo(e.target.value)} />
								<Box sx={{ display: "flex", gap: 1, justifyContent: "flex-end", pt: 1 }}>
									<Button onClick={() => { clearFilter(); closeFilter(); }}>Clear</Button>
									<Button variant="contained" onClick={() => { applyFilter(); }}>Apply</Button>
								</Box>
							</Stack>
						</Box>
					</Popover>

					{/* Notifications Popover */}
					<Popover open={Boolean(notifAnchor)} anchorEl={notifAnchor} onClose={closeNotif} anchorOrigin={{ vertical: "bottom", horizontal: "right" }} transformOrigin={{ vertical: "top", horizontal: "right" }}>
						<Box sx={{ p: 1, width: 340 }}>
							<Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", px: 1 }}>
								<Typography variant="subtitle1">Notifications</Typography>
								<Button size="small" onClick={markAllRead}>Mark all read</Button>
							</Box>

							<Divider sx={{ my: 1 }} />

							{notifications.map((n) => (
								<Box key={n.id} sx={{ px: 1, py: 1, display: "flex", gap: 1, alignItems: "flex-start", bgcolor: n.unread ? "rgba(11,61,145,0.04)" : "transparent", borderRadius: 1 }}>
									<Avatar sx={{ bgcolor: n.unread ? APP_PRIMARY : "#d6dde8", width: 34, height: 34 }}>{n.unread ? "!" : ""}</Avatar>
									<Box sx={{ flex: 1 }}>
										<Typography variant="body2" sx={{ fontWeight: 600 }}>{n.title}</Typography>
										<Typography variant="caption" color="text.secondary">{n.body}</Typography>
									</Box>
									<Typography variant="caption" color="text.secondary">{new Date(n.date).toLocaleDateString()}</Typography>
								</Box>
							))}

							{notifications.length === 0 && (
								<Box sx={{ py: 4, textAlign: "center" }}>
									<Typography variant="body2" color="text.secondary">No notifications</Typography>
								</Box>
							)}
						</Box>
					</Popover>

					{/* Page header */}
					<Box sx={{ p: 3 }}>
						<Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 3 }}>
							<Box>
								<Typography variant="h5">Your diagrams</Typography>
								<Typography variant="body2" color="text.secondary">Organize your designs and share with collaborators</Typography>
							</Box>

							<Box sx={{ display: "flex", gap: 1 }}>
								<Button variant="outlined">New diagram</Button>
								<Button variant="contained">Import</Button>
							</Box>
						</Box>

						{/* Recent row */}
						<Box sx={{ mb: 3 }}>
							<Typography variant="subtitle2" sx={{ mb: 1 }}>Recent</Typography>
							<Grid container spacing={2}>
								{recent.map((d) => (
									<Grid item key={d.id} xs={12} sm={6} md={4} lg={2}>
										<Card sx={{ height: 220, display: "flex", flexDirection: "column" }}>
											{d.thumb ? <CardMedia component="img" height="120" image={d.thumb} /> : <Box sx={{ height: 120, display: "flex", alignItems: "center", justifyContent: "center", bgcolor: "#f3f5f7" }}> <InsertPhotoIcon sx={{ fontSize: 36, color: "#9aa4b2" }} /> </Box>}
											<CardContent>
												<Typography noWrap variant="subtitle1">{d.name}</Typography>
												<Typography variant="caption" color="text.secondary">Modified {new Date(d.modifiedAt).toLocaleDateString()}</Typography>
											</CardContent>
										</Card>
									</Grid>
								))}
							</Grid>
						</Box>

						{/* Main grid */}
						<Grid container spacing={3}>
							{filtered.map((d) => (
								<Grid item key={d.id} xs={12} sm={6} md={4} lg={3}>
									<DiagramCard d={d} onOpen={handleOpenDiagram} />
								</Grid>
							))}

							{filtered.length === 0 && (
								<Grid item xs={12}>
									<Box sx={{ p: 6, textAlign: "center", bgcolor: "white", borderRadius: 2, boxShadow: 1 }}>
										<Typography variant="h6">No diagrams found</Typography>
										<Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>Try clearing filters or creating a new diagram.</Typography>
									</Box>
								</Grid>
							)}
						</Grid>
					</Box>
				</Box>
			</Box>
		</ThemeProvider>
	);
}
