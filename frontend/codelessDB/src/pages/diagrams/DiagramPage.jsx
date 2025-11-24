/* eslint-disable react-hooks/set-state-in-effect */
import React, { useEffect, useMemo, useState } from "react";
import {
	Box,
	Button,
	Grid,
	Popover,
	Stack,
	TextField,
	Typography,
} from "@mui/material";

import LeftPanel from "../../components/LeftPanel.jsx";
import TopBar from "../../components/TopBar.jsx";
import DiagramCard from "./DiagramCard.jsx";
import { useNavigate } from "react-router-dom";
import { createDiagram } from "./fetch.js";

export default function DiagramPage() {
	// State
	const [diagrams, setDiagrams] = useState([]);
	const [leftNav, setLeftNav] = useState("all");
	const [search, setSearch] = useState("");

	// Filter popover
	const [filterAnchor, setFilterAnchor] = useState(null);
	const [dateFrom, setDateFrom] = useState("");
	const [dateTo, setDateTo] = useState("");

	const navigate = useNavigate();

	/** -------------------------------------
	 * Mock initial load (replace with backend)
	 * -------------------------------------- */
	useEffect(() => {
		setDiagrams([
			{ id: "d1", name: "User Auth Flow", createdAt: "2025-10-18", modifiedAt: "2025-11-10", thumbnail: "https://picsum.photos/seed/auth/800/450" },
			{ id: "d2", name: "Checkout Flow", createdAt: "2025-08-05", modifiedAt: "2025-11-16", thumbnail: "https://picsum.photos/seed/checkout/800/450" },
			{ id: "d3", name: "Project Architecture", createdAt: "2024-12-02", modifiedAt: "2025-10-20", thumbnail: "" },
			{ id: "d4", name: "Onboarding", createdAt: "2025-07-01", modifiedAt: "2025-09-10", thumbnail: "https://picsum.photos/seed/onb/800/450" },
		]);
	}, []);

	/** -------------------------------------
	 * Filtering + Sorting
	 * -------------------------------------- */
	const filtered = useMemo(() => {
		const term = search.trim().toLowerCase();

		return diagrams
			// name search
			.filter((d) => (term ? d.name.toLowerCase().includes(term) : true))

			// date range
			.filter((d) => {
				if (!dateFrom && !dateTo) return true;

				const created = new Date(d.createdAt);

				if (dateFrom && created < new Date(`${dateFrom}T00:00:00`)) return false;
				if (dateTo && created > new Date(`${dateTo}T23:59:59`)) return false;

				return true;
			})

			// last modified first
			.sort((a, b) => new Date(b.modifiedAt) - new Date(a.modifiedAt));
	}, [diagrams, search, dateFrom, dateTo]);

	/** -------------------------------------
	 * Popover Handlers
	 * -------------------------------------- */
	const openFilter = (e) => setFilterAnchor(e.currentTarget);
	const closeFilter = () => setFilterAnchor(null);
	const clearFilter = () => {
		setDateFrom("");
		setDateTo("");
		closeFilter();
	};

	const applyFilter = () => closeFilter();

	const handleOpenDiagram = (d) => {
		navigate(`/diagrams/${d.id}`);
	};

	const handleCreateDiagram = async () => {
		try {
			const newDiagram = await createDiagram();
			setDiagrams((ds) => [newDiagram, ...ds]);

		} catch (error) {
			console.error("Error creating diagram:", error);
			alert("Failed to create diagram");
		}
	};

	return (
		<Box
			sx={{
				display: "flex",
				minHeight: "100vh",
				width: "100%",
				bgcolor: "background.light",
				padding: 1,
				paddingRight: 6,
				paddingLeft: 4
			}}
		>
			{/* Left Navigation Panel */}
			<LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />

			{/* Main Content */}
			<Box component="main" sx={{ flexGrow: 1 }}>
				<TopBar search={search} setSearch={setSearch} onFilterOpen={openFilter} />

				{/* Filter Popover */}
				<Popover
					open={Boolean(filterAnchor)}
					anchorEl={filterAnchor}
					onClose={closeFilter}
					anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
					transformOrigin={{ vertical: "top", horizontal: "right" }}
				>
					<Box sx={{ p: 2, width: 300, height: 'auto' }}>
						<Typography variant="subtitle1" sx={{ mb: 1 }}>
							Filter diagrams
						</Typography>

						<Stack spacing={1}>
							<TextField
								label="Created from"
								type="date"
								InputLabelProps={{ shrink: true }}
								value={dateFrom}
								onChange={(e) => setDateFrom(e.target.value)}
							/>
							<TextField
								label="Created to"
								type="date"
								InputLabelProps={{ shrink: true }}
								value={dateTo}
								onChange={(e) => setDateTo(e.target.value)}
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

				{/* Page Content */}
				<Box sx={{ p: 3 }}>
					{/* Header */}
					<Box
						sx={{
							display: "flex",
							justifyContent: "space-between",
							alignItems: "center",
							mb: 3,
						}}
					>
						<Box>
							<Typography variant="h5">Your diagrams</Typography>
							<Typography variant="body2" color="text.secondary">
								Organize your designs and share with collaborators
							</Typography>
						</Box>

						<Button
							variant="contained"
							sx={{ transform: "translateX(6px)" }}
							onClick={handleCreateDiagram}
						>
							New diagram
						</Button>

					</Box>

					{/* Grid of Diagrams */}
					<Grid container spacing={3}>
						{filtered.map((diagram) => (
							<Grid item key={diagram.id} xs={12} sm={6} md={4} lg={3}>
								<DiagramCard d={diagram} onOpen={handleOpenDiagram} />
							</Grid>
						))}

						{filtered.length === 0 && (
							<Grid item xs={12}>
								<Box
									sx={{
										p: 6,
										textAlign: "center",
										bgcolor: "white",
										borderRadius: 2,
										boxShadow: 1,
									}}
								>
									<Typography variant="h6">No diagrams found</Typography>
									<Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
										Try clearing filters or creating a new diagram.
									</Typography>
								</Box>
							</Grid>
						)}
					</Grid>
				</Box>
			</Box>
		</Box>
	);
}
