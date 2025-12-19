import express from "express";
import * as Y from "yjs";
import Redis from "ioredis";

const app = express();
const redis = new Redis(); // defaults to localhost:6379

const docs = new Map();

function getDoc(diagramId) {
	if (!docs.has(diagramId)) {
		docs.set(diagramId, new Y.Doc());
	}
	return docs.get(diagramId);
}

app.post("/snapshot/:diagramId", async (request, response) => {
	const { diagramId } = request.params;
	const redisKey = `stream:${diagramId}`;
	console.log(`Taking snapshot for diagramId ${diagramId} from Redis stream ${redisKey}`);

	// Fetch all records in the stream
	// XRANGE streamKey - +
	const records = await redis.xrangeBuffer(redisKey, "-", "+");

	if (!records || records.length === 0) {
		return response.status(204).send(); // nothing to snapshot
	}

	const doc = getDoc(diagramId);
	console.log(`Applying ${records.length} updates to Y.Doc`);

	for (const [id, fields] of records) {
		// fields: [key1, value1, ...]
		for (let i = 0; i < fields.length; i += 2) {
			const key = fields[i].toString();      // "update"
			const value = fields[i + 1];           // Buffer from XRANGEBuffer
			if (key === "update") {
				// Convert Buffer to Uint8Array for Yjs
				Y.applyUpdate(doc, new Uint8Array(value));
			}
		}
	}

	const snapshot = Y.encodeStateAsUpdate(doc);
	console.log(`Snapshot size: ${snapshot.length} bytes`);

	// Delete merged stream entries (XDEL)
	const recordIds = records.map(r => r[0].toString());
	if (recordIds.length > 0) {
		await redis.xdel(redisKey, ...recordIds);
	}

	response.set("Content-Type", "application/octet-stream");
	console.log(`Sending snapshot response for diagramId ${diagramId}`);
	response.send(Buffer.from(snapshot));
});

app.listen(3001, () => {
	console.log("Yjs snapshot worker running on port 3001");
});
