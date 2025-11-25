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
	Pagination,
	CircularProgress,
} from "@mui/material";

import LeftPanel from "../../components/LeftPanel.jsx";
import TopBar from "../../components/TopBar.jsx";
import DiagramCard from "./DiagramCard.jsx";
import { useNavigate } from "react-router-dom";
import { createDiagram, fetchDiagrams } from "./fetch.js";

const ITEMS_PER_PAGE = 8;

export default function DiagramPage() {
	// State
	const [diagrams, setDiagrams] = useState([]);
	const [leftNav, setLeftNav] = useState("all");
	const [search, setSearch] = useState("");
	const [page, setPage] = useState(1);
	const [loading, setLoading] = useState(false);
	const [totalPages, setTotalPages] = useState(0);
	const [totalElements, setTotalElements] = useState(0);

	// Filter popover
	const [filterAnchor, setFilterAnchor] = useState(null);
	const [dateFrom, setDateFrom] = useState("");
	const [dateTo, setDateTo] = useState("");

	const navigate = useNavigate();

	/** -------------------------------------
	 * Fetch diagrams from backend
	 * -------------------------------------- */
	const loadDiagrams = async () => {
		setLoading(true);
		try {
			const response = await fetchDiagrams();
			// const response = await fetchDiagrams({
			// 	page: page - 1, // Backend typically uses 0-based indexing
			// 	size: ITEMS_PER_PAGE,
			// 	search,
			// 	dateFrom,
			// 	dateTo,
			// });
			console.log(response)
			setDiagrams(response.content || []);
			setTotalPages(response.totalPages || 0);
			setTotalElements(response.totalElements || 0);
		
		} catch (error) {
			console.error("Error fetching diagrams:", error);
			setDiagrams([]);
		
		} finally {
			setLoading(false);
		}
	};

	// Load diagrams on mount and when filters/page change
	useEffect(() => {
		loadDiagrams();
	}, [page, search, dateFrom, dateTo]);
	
	useEffect(() => {
		loadDiagrams();
	}, []);

	// Reset to page 1 when filters change
	useEffect(() => {
		if (page !== 1) {
			setPage(1);
		}
	}, [search, dateFrom, dateTo]);

	const handlePageChange = (event, value) => {
		setPage(value);
		// Scroll to top when page changes
		window.scrollTo({ top: 0, behavior: 'smooth' });
	};

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
		navigate(`/diagrams/${d.diagramId}`);
	};

	const handleCreateDiagram = async () => {
		try {
			const newDiagram = await createDiagram();
			console.log(newDiagram);
			setDiagrams((ds) => [newDiagram, ...ds]);
			// Reload diagrams to get updated list
			// await loadDiagrams();
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
								{totalElements > 0 && ` · ${totalElements} total`}
							</Typography>
						</Box>

						<Button
							variant="contained"
							sx={{ transform: "translateX(6px)" }}
							onClick={handleCreateDiagram}
							disabled={loading}
						>
							New diagram
						</Button>

					</Box>

					{/* Loading State */}
					{loading && (
						<Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
							<CircularProgress />
						</Box>
					)}

					{/* Grid of Diagrams */}
					{!loading && (
						<Grid container spacing={3}>
							{diagrams.map((diagram) => (
								<Grid item key={diagram.diagramId} xs={12} sm={6} md={4} lg={3}>
									<DiagramCard d={diagram} onOpen={handleOpenDiagram} />
								</Grid>
							))}

							{diagrams.length === 0 && (
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
					)}

					{/* Pagination */}
					{!loading && diagrams.length > 0 && totalPages > 1 && (
						<Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
							<Pagination
								count={totalPages}
								page={page}
								onChange={handlePageChange}
								color="primary"
								size="large"
								showFirstButton
								showLastButton
							/>
						</Box>
					)}
				</Box>
			</Box>
		</Box>
	);
}