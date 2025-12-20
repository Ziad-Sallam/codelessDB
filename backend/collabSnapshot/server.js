import express from "express";
import * as Y from "yjs";
import Redis from "ioredis";

const app = express();
const redis = new Redis(); // defaults to localhost:6379

// Increase limit to 50mb if your diagrams get complex
app.use(express.raw({ type: "application/octet-stream", limit: "5mb" }));

/**
 * Endpoint to merge Redis stream updates into a base snapshot
 * POST /snapshot/:diagramId
 * Body: Raw Binary (Uint8Array) of the current base snapshot
 */
app.post("/snapshot/:diagramId", async (request, response) => {
	const { diagramId } = request.params;
	const diagramContent = request.body; // Buffer from express.raw
	const redisKey = `stream:${diagramId}`;

	console.log(`\n--- Snapshot Start: ${diagramId} ---`);

	const doc = new Y.Doc();

	// 1. Load the Base State
	// We wrap this in a try-catch because if the base is corrupted, 
	// applying subsequent updates will result in a broken state.
	if (Buffer.isBuffer(diagramContent) && diagramContent.length > 0) {
		try {
			Y.applyUpdate(doc, new Uint8Array(diagramContent));
			console.log(`Base state loaded: ${diagramContent.length} bytes`);
		} catch (err) {
			console.error(`CRITICAL: Base snapshot corrupted for ${diagramId}.`, err.message);
			return response.status(400).send("Base state corrupted");
		}
	} else {
		console.log("No base state provided, creating fresh doc.");
	}

	// 2. Fetch updates from Redis Stream
	// We use xrangeBuffer to ensure we get raw bytes, not UTF-8 strings
	const records = await redis.xrangeBuffer(redisKey, "-", "+");

	if ((!records || records.length === 0) && (!diagramContent || diagramContent.length === 0)) {
		console.log("Nothing to process.");
		return response.status(204).send();
	}

	console.log(`Applying ${records.length} updates from Redis...`);

	let appliedCount = 0;
	for (const [id, fields] of records) {
		// Redis streams store data as [key, value, key2, value2...]
		for (let i = 0; i < fields.length; i += 2) {
			const key = fields[i].toString();
			const value = fields[i + 1]; // This is a Buffer

			if (key === "update") {
				try {
					Y.applyUpdate(doc, new Uint8Array(value));
					appliedCount++;
				} catch (err) {
					// This is where your "Unexpected end of array" usually happens.
					// Logging the ID helps you find the bad record in Redis.
					console.error(`[Record ${id.toString()}] Update failed:`, err.message);
				}
			}
		}
	}

	// 3. Encode the new merged state
	const mergedSnapshot = Y.encodeStateAsUpdate(doc);
	console.log(`New snapshot size: ${mergedSnapshot.length} bytes`);

	// 4. Cleanup Redis 
	// ONLY delete the records we successfully read to prevent data loss
	if (records.length > 0) {
		const recordIds = records.map(r => r[0]);
		await redis.xdel(redisKey, ...recordIds);
		console.log(`Deleted ${recordIds.length} processed records from Redis.`);
	}

	// 5. Send binary response
	response.set("Content-Type", "application/octet-stream");
	console.log(`--- Snapshot Success: ${diagramId} ---`);

	// We convert the Uint8Array to a Node Buffer for Express response
	response.send(Buffer.from(mergedSnapshot));
});

const PORT = 3001;
app.listen(PORT, () => {
	console.log(`Yjs worker running on http://localhost:${PORT}`);
});