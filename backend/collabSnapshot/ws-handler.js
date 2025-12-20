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

const messageSync = 0
const messageAwareness = 1

/**
 * Sets up the Awareness Broadcast logic.
 * This should be called ONCE per Room (not per connection).
 * * @param {Y.Doc} doc - The Yjs document
 * @param {Set<WebSocket>} conns - The set of all active connections for this room
 */
export const setupAwarenessBroadcasting = (doc, conns) => {
    if (doc.awareness) {
        doc.awareness.on('update', ({ added, updated, removed }, origin) => {
            const changedClients = added.concat(updated).concat(removed)
            const encoder = encoding.createEncoder()
            encoding.writeVarUint(encoder, messageAwareness)
            encoding.writeVarUint8Array(
                encoder, 
                awarenessProtocol.encodeAwarenessUpdate(doc.awareness, changedClients)
            )
            const buff = encoding.toUint8Array(encoder)

            conns.forEach((conn) => {
                // Broadcast to everyone EXCEPT the origin of the update
                if (conn !== origin && (conn.readyState === wsReadyStateOpen || conn.readyState === wsReadyStateConnecting)) {
                    send(conn, buff)
                }
            })
        })
    }
}

/**
 * Connects a WebSocket to a specific Yjs Doc.
 */
export const setupWSConnection = (conn, doc) => {
  conn.binaryType = 'arraybuffer'
  
  // 1. Send SyncStep1
  const encoder = encoding.createEncoder()
  encoding.writeVarUint(encoder, messageSync)
  syncProtocol.writeSyncStep1(encoder, doc)
  send(conn, encoding.toUint8Array(encoder))

  // 2. Send Awareness states (Initial State)
  const awareness = doc.awareness
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
            // Apply the update. 'conn' is passed as the 'origin' so we can filter it out in the broadcast loop above.
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

  // 5. Sync Protocol Update Handler
  const updateHandler = (update, origin) => {
    const encoder = encoding.createEncoder()
    encoding.writeVarUint(encoder, messageSync)
    syncProtocol.writeUpdate(encoder, update)
    send(conn, encoding.toUint8Array(encoder))
  }

  doc.on('update', updateHandler)

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