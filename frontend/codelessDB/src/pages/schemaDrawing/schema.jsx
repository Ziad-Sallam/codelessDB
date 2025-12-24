import { useCallback, useEffect, useState } from "react";
import { useParams, useBlocker, useNavigate } from "react-router-dom";
import {
	Background,
	Controls,
	MiniMap,
	ReactFlow,
	ReactFlowProvider,
	useReactFlow,
	useViewport,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { toPng } from "html-to-image";
import { useNotification } from "../../components/NotificationContext";
import ShareWindow from "../../components/ShareWindow.jsx";
import CodeEditor from "./code-editor/CodeEditor.jsx";
import Toolbar from "./ConnectionControls.jsx";
import Cursor from "./collab/Cursor.jsx";
import ActiveUsers from "./collab/ActiveUsers.jsx";
import {
	CollaborationProvider,
	useCollaboration,
} from "./collab/CollaborationContext.jsx";
import applyRelationLogic from "./connectingLogic/ConnectingLogic";
import {
	fetchDiagramMetadata,
	fetchDiagramSnapshot,
	updateDiagramMetadata,
} from "./fetch.js";
import { nodeTypes, edgeTypes } from "./index";
import DiagramNotFound from "../notFound/DiagramNotFound.jsx";
import "./Schema.css";
import { renameDiagram } from "../diagrams/fetch.js";
import { uploadToCloudinary } from "../../uploadToCloudinary.js";
import LoadingPage from "../../components/LoadingPage.jsx";


const SchemaContent = () => {
	const {
		ydoc,
		nodes,
		edges,
		schemaName,
		updateSchemaName,
		cursors,
		updateCursor,
		onNodesChange,
		onEdgesChange,
		addNodeYjs,
		addEdgeYjs,
		updateNodeData,
		applySnapshot,
		undo,
		redo
	} = useCollaboration();

	const navigate = useNavigate();
	const { roomId } = useParams();
	const { showSuccess, showError, showWarning } = useNotification();

	const [selectedRelationType, setSelectedRelationType] = useState("1:N");
	const [isSqlPanelOpen, setIsSqlPanelOpen] = useState(false);
	const [isReadOnly, setIsReadOnly] = useState(false);
	const [reactFlowInstance, setReactFlowInstance] = useState(null);
	const [shareOpen, setShareOpen] = useState(false);
	const [diagramExistFlag, setDiagramExistFlag] = useState(true);

	const [isLoading, setIsLoading] = useState(true);

	const { screenToFlowPosition } = useReactFlow();

	const onMouseMove = useCallback(
		(e) => {
			// Convert pixel coordinates (e.clientX) to World coordinates (Flow X)
			// This handles Zoom and Pan automatically.
			const position = screenToFlowPosition({
				x: e.clientX,
				y: e.clientY,
			});

			// Broadcast the World Position
			updateCursor(position.x, position.y);
		},
		[screenToFlowPosition, updateCursor]
	);

	const loadDigram = async () => {
		try {
			setIsLoading(true);
			// 1. Fetch Snapshot (Binary)
			const snapshotBuffer = await fetchDiagramSnapshot(roomId);
			applySnapshot(new Uint8Array(snapshotBuffer));

			// 2. Fetch Metadata (JSON)
			const meta = await fetchDiagramMetadata(roomId);

			updateSchemaName(meta.diagramName);
			setIsReadOnly(meta.role === "READER");
			setDiagramExistFlag(true);

		} catch (err) {

			setDiagramExistFlag(false);

		} finally {
			setIsLoading(false);
		}
	};

	useEffect(() => {
		loadDigram();
	}, []);

	useEffect(() => {
		const handleKeyDown = (e) => {

			if (e.ctrlKey || e.metaKey) {
				if (e.key === "z") {
					e.preventDefault();
					if (e.shiftKey) {
						redo();
					} else {
						undo();
					}
				} else if (e.key === "y") {
					e.preventDefault();
					redo(); // Ctrl + Y
				}
			}
		};

		window.addEventListener("keydown", handleKeyDown);
		return () => window.removeEventListener("keydown", handleKeyDown);
	}, [undo, redo]);

	const takeThumbnail = async () => {
		const viewport = document.querySelector(".react-flow__viewport");

		await reactFlowInstance.fitView({ padding: 50 });

		if (!viewport) return;

		try {
			const thumbnail = await toPng(viewport, {
				backgroundColor: "#ffffff",
				quality: 1,
			});

			return thumbnail;
		} catch (err) {
			console.log("Error exporting:", err);
		}
	};

	const onConnect = useCallback(
		(params) => {
			if (!params || !params.source || !params.target) return;

			applyRelationLogic(
				params.source,
				params.target,
				selectedRelationType,
				nodes,
				{ updateNodeData, addNodeYjs, addEdgeYjs }
			);

			const typeKey =
				{
					"1:1": "oneToOne",
					"1:N": "oneToMany",
					"N:1": "manyToOne",
					"M:N": "manyToMany",
				}[selectedRelationType] || "oneToMany";

			const newEdge = {
				source: params.source,
				target: params.target,
				id: `e_${params.source}_${params.target}_${Date.now()}`,
				type: typeKey,
				data: { type: selectedRelationType },
			};
			addEdgeYjs(newEdge);
		},
		[selectedRelationType, addEdgeYjs, nodes, updateNodeData, addNodeYjs]
	);

	const addNode = () => {
		const id = `${nodes.length + 1}_${Date.now()}`;
		const newNode = {
			id,
			type: "Defult-Node",
			data: {
				tableName: `Entity_${nodes.length + 1}`,
				columns: [
					{
						id: `attr1_${id}`,
						name: "id",
						dataType: "INT",
						constraints: { PRIMARY_KEY: true },
					},
				],
			},
			position: { x: Math.random() * 400, y: Math.random() * 400 },
		};
		addNodeYjs(newNode);
	};

	function handleShareClick() {
		setShareOpen(true);
	}

	async function rename(newName) {
		try {
			newName = newName.trim();
			await renameDiagram(roomId, newName);
			showSuccess("Name Changed Successfuly")
		} catch {
			showError("Error")
		}
	}

	const handleSaveAndExit = async () => {
		setIsLoading(true);
		try {
			const screenShot = await takeThumbnail();
			if (screenShot) {
				const screenShotUrl = await uploadToCloudinary(screenShot, roomId);
				await updateDiagramMetadata(roomId, { thumbnail: screenShotUrl });
			}
		} catch (error) {
			console.error("Auto-save failed:", error);
		}
	};

	const blocker = useBlocker(
		({ currentLocation, nextLocation }) =>
			currentLocation.pathname !== nextLocation.pathname
	);

	useEffect(() => {
		if (blocker.state === "blocked") {
			handleSaveAndExit().then(() => {
				blocker.proceed();
			});
		}
	}, [blocker]);

	async function handleExitClick() {
		navigate("/diagrams");
	}


	if (!diagramExistFlag) {
		return <DiagramNotFound />;
	}

	return (
		<div className="drawing-container" onMouseMove={onMouseMove}>
			{isLoading && <LoadingPage />}
			{isSqlPanelOpen && (
				<CodeEditor
					onClose={() => setIsSqlPanelOpen(false)}
					diagramId={roomId}
					schemaName={schemaName}
					nodes={nodes}
				/>
			)}
			<button className="exit-button" onClick={handleExitClick}>
				↩
			</button>
			<input
				className="schema-name"
				placeholder="Database Name"
				value={schemaName}
				onChange={(e) => updateSchemaName(e.target.value)}
				onBlur={(e) => rename(e.target.value)}
				disabled={isReadOnly}
			/>
			<div className="active-users">
				<ActiveUsers />
				<button className="add-user" onClick={handleShareClick}>+</button>
			</div>
			<div className="share-window">
				<ShareWindow diagramId={roomId} shareOpen={shareOpen} setShareOpen={setShareOpen} />
			</div>

			<div className="drawing-canva">
				<ReactFlow
					onInit={(instance) => setReactFlowInstance(instance)}
					nodes={nodes}
					edges={edges}
					onNodesChange={onNodesChange}
					onEdgesChange={onEdgesChange}
					onConnect={onConnect}
					nodeTypes={nodeTypes}
					edgeTypes={edgeTypes}
					fitView
					connectionMode="loose"
					nodesDraggable={!isReadOnly}
					nodesConnectable={!isReadOnly}
					elementsSelectable={!isReadOnly}
					zoomOnDoubleClick={!isReadOnly}
					defaultEdgeOptions={{
						style: { strokeWidth: 2, stroke: "#94a3b8" },
					}}
					//new props
					proOptions={{ hideAttribution: true }}
					nodeDragThreshold={2}
					onlyRenderVisibleElements={true}
				>
					<MiniMap
						style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
						nodeColor="#cbd5e1"
						maskColor="rgba(241, 245, 249, 0.6)"
					/>
					{!isReadOnly && (
						<Controls
							style={{
								borderRadius: 8,
								overflow: "hidden",
								border: "none",
								boxShadow: "0 4px 6px -1px rgba(0,0,0,0.1)",
							}}
						/>
					)}
					<Background color="#5f5f5fff" gap={20} size={1} variant="dots" />
					<CursorLayer>
						{cursors.map((cursor) => (
							<Cursor
								key={cursor.id}
								x={cursor.x}
								y={cursor.y}
								color={cursor.color}
								name={cursor.name}
							/>
						))}
					</CursorLayer>
				</ReactFlow>
			</div>

			{!isSqlPanelOpen && !isReadOnly && (
				<div className="toolbar-container">
					<Toolbar
						addNode={addNode}
						selectedRelationType={selectedRelationType}
						setSelectedRelationType={setSelectedRelationType}
						undo={undo}
						redo={redo}
					/>
				</div>

			)}
			<button className="generate" onClick={() => setIsSqlPanelOpen(true)}>
				SQL Editor
			</button>
		</div>
	);
};


export default function Schema() {
	const { roomId } = useParams();
	return (
		<ReactFlowProvider>
			<CollaborationProvider roomId={roomId} key={roomId}>
				<SchemaContent />
			</CollaborationProvider>
		</ReactFlowProvider>
	);
}

const CursorLayer = ({ children }) => {
	const { x, y, zoom } = useViewport();

	return (
		<div
			style={{
				position: "absolute",
				top: 0,
				left: 0,
				pointerEvents: "none",
				zIndex: 1000,
				transform: `translate(${x}px, ${y}px) scale(${zoom})`,
				transformOrigin: "0 0",
				width: "100%",
				height: "100%",
			}}
		>
			{children}
		</div>
	);
};
