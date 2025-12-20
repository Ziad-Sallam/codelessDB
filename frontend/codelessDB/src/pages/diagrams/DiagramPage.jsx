
import {
	Alert,
	Box, Button, CircularProgress, Pagination, Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import LeftPanel from "../../components/LeftPanel.jsx";
import TopBar from "../../components/TopBar.jsx";
import DiagramCard from "./DiagramCard.jsx";
import { createDiagram, fetchDiagrams } from "./fetch.js";

import { useNotification } from "../../components/NotificationContext";

// Professional error handling utilities
const ErrorHandler = {
	isNetworkError: (err) => !err?.response || err?.message?.includes("Network"),
	isUnauthorized: (err) => err?.response?.status === 401 || err?.response?.status === 403,
	isNotFound: (err) => err?.response?.status === 404,
	getErrorMessage: (err) => {
		if (!err) return "An unexpected error occurred";
		if (ErrorHandler.isNetworkError(err)) return "Network error. Please check your connection.";
		if (ErrorHandler.isUnauthorized(err)) return "You don't have permission to perform this action.";
		if (ErrorHandler.isNotFound(err)) return "The requested resource was not found.";
		return err?.message || "Failed to complete the action";
	},
	logError: (context, err) => {
		console.error(`[${context}]`, {
			message: err?.message,
			status: err?.response?.status,
			data: err?.response?.data,
			stack: err?.stack,
			timestamp: new Date().toISOString(),
		});
	},
};

const ITEMS_PER_PAGE = 12;

export default function DiagramPage() {
	const { showSuccess, showError } = useNotification();

	const [diagrams, setDiagrams] = useState([]);
	const [leftNav, setLeftNav] = useState("all");
	const [page, setPage] = useState(1);
	const [loading, setLoading] = useState(false);
	const [totalPages, setTotalPages] = useState(0);
	const [totalElements, setTotalElements] = useState(0);
	const [error, setError] = useState(null);
	const [retryCount, setRetryCount] = useState(0);

	const navigate = useNavigate();

	// loadDiagrams: pageNumber is 1-based here
	const loadDiagrams = async (pageNumber = page) => {
		setLoading(true);
		setError(null);
		try {
			// convert to 0-based for backend
			const resp = await fetchDiagrams(pageNumber - 1, ITEMS_PER_PAGE);
			
			if (!resp?.content) {
				throw new Error("Invalid response structure from server");
			}
			
			setDiagrams(resp.content);
			setTotalPages(resp.totalPages || 0);
			setTotalElements(resp.totalElements || 0);
			setRetryCount(0);

		} catch (err) {
			ErrorHandler.logError("LoadDiagrams", err);
			const errorMsg = ErrorHandler.getErrorMessage(err);
			setError(errorMsg);
			setDiagrams([]);
			setTotalPages(0);
			setTotalElements(0);
			showError?.(errorMsg);

		} finally {
			setLoading(false);
		}
	};

	// Retry loading with exponential backoff
	const handleRetry = async () => {
		if (retryCount >= 3) {
			showError?.("Maximum retry attempts reached. Please refresh the page.");
			return;
		}
		setRetryCount(prev => prev + 1);
		setError(null);
		await loadDiagrams(page);
	};

	useEffect(() => {
		loadDiagrams(1);
	}, []);


	const handlePageChange = (event, value) => {
		setPage(value);
		window.scrollTo({ top: 0, behavior: "smooth" });
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
			if (!newDiagram?.diagramId) {
				throw new Error("Failed to create diagram: Invalid response");
			}
			setDiagrams((ds) => [newDiagram, ...ds]);
			setError(null);
			showSuccess?.("Diagram created successfully");
		} catch (error) {
			ErrorHandler.logError("CreateDiagram", error);
			const errorMsg = ErrorHandler.getErrorMessage(error);
			setError(errorMsg);
			showError?.(errorMsg);
		}
	};

	const onSearchResults = (resp) => {
		setDiagrams(resp?.content || []);
		setTotalPages(resp?.totalPages || 0);
		setTotalElements(resp?.totalElements || 0);

		// setPage(1);
	};

	return (
		<Box
			sx={{
				display: "flex",
				minHeight: "100vh",
				width: "100%",
				minWidth: "400px",
				bgcolor: "background.light",
				px: { xs: 1, sm: 2, md: 4 },
				pr: { xs: 1, sm: 2, md: 6 },
				py: 2,
			}}
		>
			<LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />

			<Box component="main" sx={{ flexGrow: 1 }}>
				<TopBar onSearchResults={onSearchResults} pageSize={ITEMS_PER_PAGE} page={page} loadDiagrams={loadDiagrams} />


				<Box sx={{ p: 3 }}>
					{error && (
						<Alert
							severity="error"
							onClose={() => setError(null)}
							sx={{ mb: 3, display: "flex", alignItems: "center", justifyContent: "space-between" }}
							action={
								<Button
									color="inherit"
									size="small"
									onClick={handleRetry}
								>
									Retry
								</Button>
							}
						>
							{error}
						</Alert>
					)}

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
							<Box
								sx={{
									display: "grid",
									gridTemplateColumns: "repeat(auto-fill, minmax(350px, 1fr))",
									gap: 3,
								}}
							>
								{diagrams.map((diagram) => (
									<DiagramCard
										key={diagram.diagramId}
										d={diagram}
										onOpen={handleOpenDiagram}
										onUpdate={handleUpdateDiagram}
										onDelete={handleDeleteDiagram}
									/>
								))}
							</Box>

							{diagrams.length === 0 && (
								<Box sx={{ p: 6, textAlign: "center", bgcolor: "white", borderRadius: 2, boxShadow: 1 }}>
									<Typography variant="h6">No diagrams found</Typography>
									<Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
										Try clearing filters or creating a new diagram.
									</Typography>
								</Box>
							)}

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