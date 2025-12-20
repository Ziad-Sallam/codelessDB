// ws-handler.js
import * as Y from 'yjs'
import * as syncProtocol from 'y-protocols/sync'
import * as awarenessProtocol from 'y-protocols/awareness'
import * as encoding from 'lib0/encoding'
import * as decoding from 'lib0/decoding'

const wsReadyStateConnecting = 0
const wsReadyStateOpen = 1
const wsReadyStateClosing = 2
const wsReadyStateClosed = 3

// Standard Yjs Message Types
const messageSync = 0
const messageAwareness = 1

/**
 * Connects a WebSocket to a specific Yjs Doc.
 * @param {WebSocket} conn 
 * @param {Y.Doc} doc 
 */
export const setupWSConnection = (conn, doc) => {
  conn.binaryType = 'arraybuffer'
  
  // 1. Send SyncStep1 (Ask client for their state)
  const encoder = encoding.createEncoder()
  encoding.writeVarUint(encoder, messageSync)
  syncProtocol.writeSyncStep1(encoder, doc)
  send(conn, encoding.toUint8Array(encoder))

  // 2. Send Awareness states
  const awareness = doc.awareness // Assuming you attached awareness to doc if needed
  if (awareness) {
    const awarenessStates = awareness.getStates()
    if (awarenessStates.size > 0) {
      const encoder = encoding.createEncoder()
      encoding.writeVarUint(encoder, messageAwareness)
      encoding.writeVarUint8Array(encoder, awarenessProtocol.encodeAwarenessUpdate(awareness, Array.from(awarenessStates.keys())))
      send(conn, encoding.toUint8Array(encoder))
    }
  }

  // 3. Listen for updates from this client
  conn.on('message', (message) => {
    try {
      const encoder = encoding.createEncoder()
      const decoder = decoding.createDecoder(new Uint8Array(message))
      const messageType = decoding.readVarUint(decoder)

      switch (messageType) {
        case messageSync:
          encoding.writeVarUint(encoder, messageSync)
          syncProtocol.readSyncMessage(decoder, encoder, doc, null)
          if (encoding.length(encoder) > 1) {
            send(conn, encoding.toUint8Array(encoder))
          }
          break
        case messageAwareness: {
          if (doc.awareness) {
            awarenessProtocol.applyAwarenessUpdate(doc.awareness, decoding.readVarUint8Array(decoder), conn)
          }
          break
        }
      }
    } catch (err) {
      console.error(err)
      doc.emit('error', [err])
    }
  })

  // 4. Handle Disconnect
  conn.on('close', () => {
    if (doc.awareness) {
      awarenessProtocol.removeAwarenessStates(doc.awareness, [doc.awareness.clientID], null)
    }
  })

  // 5. Subscribe to document updates (broadcast to this client)
  const updateHandler = (update, origin) => {
    // Ignore updates originating from this same connection to prevent loops
    // (In standard Yjs server, we often rely on origin checks, but basic broadcasting works too)
    const encoder = encoding.createEncoder()
    encoding.writeVarUint(encoder, messageSync)
    syncProtocol.writeUpdate(encoder, update)
    send(conn, encoding.toUint8Array(encoder))
  }

  doc.on('update', updateHandler)

  // Cleanup when connection closes
  conn.on('close', () => {
    doc.off('update', updateHandler)
  })
}

const send = (conn, m) => {
  if (conn.readyState !== wsReadyStateConnecting && conn.readyState !== wsReadyStateOpen) {
    conn.close()
  }
  try {
    conn.send(m, (err) => { err != null && conn.close() })
  } catch (e) {
    conn.close()
  }
}