import express from "express";
import Redis from "ioredis";
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
		let appliedThisRecord = false;

		for (let i = 0; i < fields.length; i += 2) {
			const key = fields[i].toString();
			const value = fields[i + 1];

			if (key !== "update") continue;

			try {
				Y.applyUpdate(doc, new Uint8Array(value));
				appliedThisRecord = true;
				appliedCount++;
			} catch (err) {
				console.error(`[${id.toString()}] Update failed:`, err.message);
			}
		}

		if (appliedThisRecord) {
			appliedRecordIds.push(id);
		}
	}

	console.log(`Applied ${appliedCount} updates`);

	// Encode merged snapshot
	const mergedSnapshot = Y.encodeStateAsUpdate(doc);
	console.log(`Merged snapshot size: ${mergedSnapshot.length} bytes`);

	// Delete only successfully applied records
	if (appliedRecordIds.length > 0) {
		await redis.xdel(redisKey, ...appliedRecordIds);
		console.log(`Deleted ${appliedRecordIds.length} Redis records`);
	}

	res.set("Content-Type", "application/octet-stream");
	console.log(`--- Snapshot Success: ${diagramId} ---`);

	res.send(Buffer.from(mergedSnapshot));
});

app.listen(3001, () => {
	console.log("Yjs snapshot worker running on http://localhost:3001");
});
