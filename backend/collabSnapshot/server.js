import express from "express";
import Redis from "ioredis";
import * as decoding from "lib0/decoding";
import * as Y from "yjs";

const app = express();
const redis = new Redis();

app.use(express.raw({
	type: "application/octet-stream",
	limit: "10mb"
}));

app.post("/snapshot/:diagramId", async (req, res) => {
	const { diagramId } = req.params;
	const redisKey = `stream:${diagramId}`;

	console.log(`\n--- Snapshot Start: ${diagramId} ---`);

	const doc = new Y.Doc();

	// Always normalize body
	const baseBuffer =
		Buffer.isBuffer(req.body) && req.body.length > 0
			? req.body
			: null;

	// Load base snapshot (optional)
	if (baseBuffer) {
		try {
			Y.applyUpdate(doc, new Uint8Array(baseBuffer));
			console.log(`Base snapshot loaded (${baseBuffer.length} bytes)`);
		} catch (err) {
			console.error("Base snapshot corrupted:", err.message);
			return res.status(400).send("Base snapshot corrupted");
		}
	} else {
		console.log("No base snapshot provided (new diagram or empty state)");
	}

	// Read Redis updates
	const records = await redis.xrangeBuffer(redisKey, "-", "+");

	if (!records || records.length === 0) {
		console.log("No Redis updates found");

		// Return snapshot of current doc (empty or base-only)
		const snapshot = Y.encodeStateAsUpdate(doc);
		res.set("Content-Type", "application/octet-stream");
		return res.send(Buffer.from(snapshot));
	}

	console.log(`Applying ${records.length} Redis updates`);

	const appliedRecordIds = [];
	let appliedCount = 0;

	for (const [id, fields] of records) {
		// let appliedThisRecord = false;

		for (let i = 0; i < fields.length; i += 2) {
			const key = fields[i].toString();
			const value = fields[i + 1];

			if (key !== "update") continue;

			try {
				const buffer = new Uint8Array(value);

				// If the buffer is empty, skip
				if (buffer.length === 0) continue;

				// Try to decode as y-protocol message
				// Ref: https://github.com/yjs/y-protocols/blob/master/PROTOCOL.md
				const decoder = decoding.createDecoder(buffer);
				const messageType = decoding.readVarUint(decoder);

				// 0: Sync, 1: Awareness
				if (messageType === 0) { // Sync Protocol
					const syncMessageType = decoding.readVarUint(decoder);

					console.log(`[${id}] Sync Msg Type: ${syncMessageType}`);

					// 0: SyncStep1, 1: SyncStep2, 2: Update
					if (syncMessageType === 0) {
						// SyncStep1: Just a request for state, contains state vector. Ignore.
						console.log(`[${id}] Ignoring SyncStep1`);
						continue;
					} else if (syncMessageType === 1 || syncMessageType === 2) {
						// SyncStep2 or Update: Contains document update
						const update = decoding.readVarUint8Array(decoder);
						console.log(`[${id}] Applying Update (Type ${syncMessageType}), size: ${update.length}`);
						Y.applyUpdate(doc, update);
						// appliedThisRecord = true;
						appliedCount++;
					}

				} else if (messageType === 1) { // Awareness Protocol
					console.log(`[${id}] Ignoring Awareness Msg`);
					// Ignore awareness updates for snapshotting
					continue;

				} else {
					console.log(`[${id}] Unknown Msg Type: ${messageType}. Applying raw fallack.`);
					// Fallback: Try applying as raw update if it doesn't look like protocol
					try {
						Y.applyUpdate(doc, buffer);
						// appliedThisRecord = true;
						appliedCount++;
					} catch (subErr) {
						console.error(`[${id.toString()}] Update failed:`, err.message);
					}
				}

			} catch (err) {
				// If protocol parsing fials, try raw update as last resort before failing
				try {
					Y.applyUpdate(doc, new Uint8Array(value));
					// appliedThisRecord = true;
					appliedCount++;
				} catch (subErr) {
					console.error(`[${id.toString()}] Update failed:`, err.message);
				}
			}
		}

		appliedRecordIds.push(id);
		// if (appliedThisRecord) {
		// }
	}

	console.log(`Applied ${appliedCount} updates`);

	// Debug: Check what's actually in the doc
	const nodes = doc.getMap("nodes").toJSON();
	const edges = doc.getMap("edges").toJSON();
	const meta = doc.getMap("meta").toJSON();

	console.log("Server Doc State - Nodes count:", Object.keys(nodes).length);
	console.log("Server Doc State - Edges count:", Object.keys(edges).length);
	console.log("Server Doc State - Meta:", JSON.stringify(meta));

	// Check for pending updates (Data Loss Vector)
	if (doc.store.pendingStructs) {
		console.log("!!! PENDING STRUCTS FOUND !!!");
		console.log("Missing:", JSON.stringify(doc.store.pendingStructs.missing));
		// console.log("Pending:", doc.store.pendingStructs);
	}
	if (doc.store.pendingStack && doc.store.pendingStack.length > 0) {
		console.log("!!! PENDING STACK FOUND !!! Length:", doc.store.pendingStack.length);
	}

	if (Object.keys(nodes).length > 0) {
		console.log("Sample Node:", Object.values(nodes)[0]);
	}

	// Encode merged snapshot
	const mergedSnapshot = Y.encodeStateAsUpdate(doc);
	console.log(`Merged snapshot size: ${mergedSnapshot.length} bytes`);

	// Delete only successfully applied records
	// if (appliedRecordIds.length > 0) {
	// 	await redis.xdel(redisKey, [...appliedRecordIds]);
	// 	console.log(`Deleted ${appliedRecordIds.length} Redis records`);
	// }

	res.set("Content-Type", "application/octet-stream");
	console.log(`--- Snapshot Success: ${diagramId} ---`);

	res.send(Buffer.from(mergedSnapshot));
});

app.listen(3001, () => {
	console.log("Yjs snapshot worker running on http://localhost:3001");
});
