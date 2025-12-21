
import * as decoding from "lib0/decoding";
import * as encoding from "lib0/encoding";
import * as Y from "yjs";

// Mocking the logic added to server.js
function applyUpdateLogic(doc, value) {
	let appliedThisRecord = false;
	let appliedCount = 0;
	const id = "test-id";

	try {
		const buffer = new Uint8Array(value);

		// If the buffer is empty, skip
		if (buffer.length === 0) return { appliedThisRecord, appliedCount };

		// Try to decode as y-protocol message
		const decoder = decoding.createDecoder(buffer);
		const messageType = decoding.readVarUint(decoder);

		// 0: Sync, 1: Awareness
		if (messageType === 0) { // Sync Protocol
			const syncMessageType = decoding.readVarUint(decoder);

			// 0: SyncStep1, 1: SyncStep2, 2: Update
			if (syncMessageType === 0) {
				console.log("Ignored SyncStep1");
			} else if (syncMessageType === 1 || syncMessageType === 2) {
				// SyncStep2 or Update: Contains document update
				const update = decoding.readVarUint8Array(decoder);
				Y.applyUpdate(doc, update);
				appliedThisRecord = true;
				appliedCount++;
				console.log("Applied SyncStep2/Update");
			}
		} else if (messageType === 1) { // Awareness Protocol
			console.log("Ignored Awareness");
		} else {
			// Fallback
			Y.applyUpdate(doc, buffer);
			appliedThisRecord = true;
			appliedCount++;
			console.log("Applied Raw Update (Fallback)");
		}

	} catch (err) {
		// Fallback
		try {
			Y.applyUpdate(doc, new Uint8Array(value));
			appliedThisRecord = true;
			appliedCount++;
			console.log("Applied Raw Update (Catch Fallback)");
		} catch (subErr) {
			console.error(`[${id.toString()}] Update failed:`, err.message);
		}
	}
	return { appliedThisRecord, appliedCount };
}

// Test Case 1: SyncStep1 (Should be ignored)
const encoder1 = encoding.createEncoder();
encoding.writeVarUint(encoder1, 0); // Sync Protocol
encoding.writeVarUint(encoder1, 0); // SyncStep1
encoding.writeVarUint8Array(encoder1, new Uint8Array([0])); // Mock State Vector
const syncStep1Msg = encoding.toUint8Array(encoder1);

// Test Case 2: SyncStep2 (Should be applied)
const docSource = new Y.Doc();
docSource.getText("test").insert(0, "Hello World");
const update = Y.encodeStateAsUpdate(docSource);

const encoder2 = encoding.createEncoder();
encoding.writeVarUint(encoder2, 0); // Sync Protocol
encoding.writeVarUint(encoder2, 1); // SyncStep2
encoding.writeVarUint8Array(encoder2, update);
const syncStep2Msg = encoding.toUint8Array(encoder2);

// Test Case 3: Update (Should be applied)
const encoder3 = encoding.createEncoder();
encoding.writeVarUint(encoder3, 0); // Sync Protocol
encoding.writeVarUint(encoder3, 2); // Update
encoding.writeVarUint8Array(encoder3, update);
const updateMsg = encoding.toUint8Array(encoder3);

// Test Case 4: Awareness (Should be ignored)
const encoder4 = encoding.createEncoder();
encoding.writeVarUint(encoder4, 1); // Awareness Protocol
// encoding.writeVarUint8Array(encoder4, new Uint8Array([0])); // Mock Awareness Data
const awarenessMsg = encoding.toUint8Array(encoder4);


// Run Tests
const docDest = new Y.Doc();

console.log("--- Test 1: SyncStep1 ---");
applyUpdateLogic(docDest, syncStep1Msg);

console.log("\n--- Test 2: SyncStep2 ---");
applyUpdateLogic(docDest, syncStep2Msg);
console.log("Doc content after Test 2:", docDest.getText("test").toString());

console.log("\n--- Test 3: Update ---");
const docDest2 = new Y.Doc();
applyUpdateLogic(docDest2, updateMsg);
console.log("Doc content after Test 3:", docDest2.getText("test").toString());

console.log("\n--- Test 4: Awareness ---");
applyUpdateLogic(docDest, awarenessMsg);

