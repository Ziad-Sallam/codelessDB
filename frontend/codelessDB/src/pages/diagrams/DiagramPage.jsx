/* eslint-disable react-hooks/exhaustive-deps */
import {
	Box,
	Button,
	CircularProgress,
	Grid,
	Pagination,
	Popover,
	Stack,
	TextField,
	Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import LeftPanel from "../../components/LeftPanel.jsx";
import TopBar from "../../components/TopBar.jsx";
import DiagramCard from "./DiagramCard.jsx";
import { createDiagram, fetchDiagrams } from "./fetch.js";

import { useNotification } from "../../components/NotificationContext";


const ITEMS_PER_PAGE = 8;

export default function DiagramPage() {
	const { showSuccess, showError } = useNotification();

	const [diagrams, setDiagrams] = useState([]);
	const [leftNav, setLeftNav] = useState("all");
	const [search, setSearch] = useState("");
	const [page, setPage] = useState(1);
	const [loading, setLoading] = useState(false);
	const [totalPages, setTotalPages] = useState(0);
	const [totalElements, setTotalElements] = useState(0);

	const [filterAnchor, setFilterAnchor] = useState(null);
	const [dateFrom, setDateFrom] = useState("");
	const [dateTo, setDateTo] = useState("");

	const navigate = useNavigate();

	const loadDiagrams = async (pageNumber = page) => {
		setLoading(true);
		try {
			const resp = await fetchDiagrams(pageNumber - 1, ITEMS_PER_PAGE, { search, dateFrom, dateTo });
			setDiagrams(resp.content || []);
			setTotalPages(resp.totalPages || 0);
			setTotalElements(resp.totalElements || 0);

		} catch (err) {
			setDiagrams([]);
			setTotalPages(0);
			setTotalElements(0);
			showError(err.message);

		} finally {
			setLoading(false);
		}
	};

	useEffect(() => {
		loadDiagrams(page);
	}, [page, search, dateFrom, dateTo]);

	useEffect(() => {
		setPage(1);
	}, [search, dateFrom, dateTo]);

	const handlePageChange = (event, value) => {
		setPage(value);
		window.scrollTo({ top: 0, behavior: "smooth" });
	};

	const openFilter = (e) => setFilterAnchor(e.currentTarget);
	const closeFilter = () => setFilterAnchor(null);
	const clearFilter = () => {
		setDateFrom("");
		setDateTo("");
		closeFilter();
	};

	const applyFilter = () => {
		closeFilter();
	};

	const handleOpenDiagram = (d) => {
		if (d?.diagramId) navigate(`/diagrams/${d.diagramId}`);
	};

	function handleUpdateDiagram(newDiagram) {
		setDiagrams((prev) =>
			prev.map((item) => (item.diagramId === newDiagram.diagramId ? { ...item, ...newDiagram } : item))
		);
	}

	function handleDeleteDiagram(deletedDiagram) {
		setDiagrams((prev) => prev.filter((d) => d.diagramId !== deletedDiagram.diagramId));
	}

	const handleCreateDiagram = async () => {
		try {
			const newDiagram = await createDiagram();
			setDiagrams((ds) => [newDiagram, ...ds]);
			showSuccess("Diagram created");

		} catch (error) {
			showError(error.message);
		}
	};

	return (
		<Box
			sx={{
				display: "flex",
				minHeight: "100vh",
				width: "100%",
				bgcolor: "background.light",
				px: 4,
				py: 2,
			}}
		>
			<LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />

			<Box component="main" sx={{ flexGrow: 1 }}>
				<TopBar search={search} setSearch={setSearch} onFilterOpen={openFilter} />

				<Popover
					open={Boolean(filterAnchor)}
					anchorEl={filterAnchor}
					onClose={closeFilter}
					anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
					transformOrigin={{ vertical: "top", horizontal: "right" }}
				>
					<Box sx={{ p: 2, width: 300 }}>
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

				<Box sx={{ p: 3 }}>
					<Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 3 }}>
						<Box>
							<Typography variant="h5">Your diagrams</Typography>
							<Typography variant="body2" color="text.secondary">
								Organize your designs and share with collaborators
								{totalElements > 0 && ` · ${totalElements} total`}
							</Typography>
						</Box>

						<Button variant="contained" sx={{ transform: "translateX(6px)" }} onClick={handleCreateDiagram} disabled={loading}>
							New diagram
						</Button>
					</Box>

					{loading ? (
						<Box sx={{ display: "flex", justifyContent: "center", p: 6 }}>
							<CircularProgress />
						</Box>
					) : (
						<>
							<Grid container spacing={3}>
								{diagrams.map((diagram) => (
									<Grid key={diagram.diagramId}>
										<DiagramCard
											d={diagram}
											onOpen={handleOpenDiagram}
											onUpdate={handleUpdateDiagram}
											onDelete={handleDeleteDiagram}
										/>
									</Grid>
								))}

								{diagrams.length === 0 && (
									<Grid item xs={12}>
										<Box sx={{ p: 6, textAlign: "center", bgcolor: "white", borderRadius: 2, boxShadow: 1 }}>
											<Typography variant="h6">No diagrams found</Typography>
											<Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
												Try clearing filters or creating a new diagram.
											</Typography>
										</Box>
									</Grid>
								)}
							</Grid>

							{diagrams.length > 0 && totalPages > 1 && (
								<Box sx={{ display: "flex", justifyContent: "center", mt: 4 }}>
									<Pagination count={totalPages} page={page} onChange={handlePageChange} color="primary" size="large" />
								</Box>
							)}
						</>
					)}
				</Box>
			</Box>
		</Box>
	);
}
