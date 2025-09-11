/* -------------------- WEBSOCKET CONNECTION -------------------- */
let ws;
let heartbeatTimeout;
function connect() {
    ws = new WebSocket("ws://localhost:30000/ws");
    // ws = new WebSocket("wss://textworld.anthonus.fr/ws");

    ws.onopen = () => {
        console.log("Connected!");
        hideOfflinePopup();
        sendPing(); // Start heartbeat
        sendRequestZone();
    }

    ws.onmessage = (event) => {
        const data = JSON.parse(event.data);

        switch(data.type) {
            case "zone_data":
                console.log("Zone data received:", data);
                handleZoneData(data);
                break;

            case "cell_update":
                console.log("Cell update received:", data);
                handleCellUpdate(data);
                break;

            case "cell_update_block":
                console.log("Cell block update received:", data);
                handleCellBlockUpdate(data);
                break;

            case "pong":
                console.log("Pong received:", data);
                resetHeartbeat();
                break;

            default:
                console.log("Unknown message type:", data.type);
                break;

        }
    };

    ws.onclose = () => {
        showOfflinePopup();
        console.log("Disconnected! Reconnecting...");
        connect();
    }

    ws.onerror = (event) => {
        console.error("WebSocket error observed:", event);
        console.error("Closing connection.");
        ws.close();
    }
}

function sendPing() {
    if (ws.readyState === WebSocket.OPEN) {

        const ping = { type: "ping" };
        ws.send(JSON.stringify(ping));
        console.log("Ping sent: ", ping);

        heartbeatTimeout = setTimeout(() => {
            console.log("No pong received, closing connection.");
            ws.close();
        }, 10_000);

    }
    setTimeout(sendPing, 30_000);
}

function resetHeartbeat() {
    clearTimeout(heartbeatTimeout);
}

console.log('Connecting...');
connect();

/* -------------------- HANDLE DATA -------------------- */

function handleZoneData(data) {
    const { x, y, w, h, chars: cellArray } = data;
    cellArray.forEach(cell => {
        const { x: cx, y: cy, c } = cell;
        setCell(cx, cy, c);
    });
    draw();
}

function handleCellUpdate(data) {
    const { x, y, c } = data;
    if (c === ' ') {
        deleteCell(x, y);
    } else {
        setCell(x, y, c);
    }
    draw();
}

function handleCellBlockUpdate(data) {
    const { chars: cellArray } = data;
    cellArray.forEach(cell => {
        const { x: cx, y: cy, c } = cell;
        if (c === ' ') {
            deleteCell(cx, cy);
        } else {
            setCell(cx, cy, c);
        }
    });
    draw();
}

/* -------------------- SEND REQUESTS -------------------- */

let lastRequestTime = 0;
const throttleDelay = 100;
function sendRequestZone() {
    const x = Math.floor(cameraX / CELL_WIDTH);
    const endCol = Math.ceil((cameraX + canvas.width / zoom) / CELL_WIDTH);
    const y = Math.floor(cameraY / CELL_HEIGHT);
    const endRow = Math.ceil((cameraY + canvas.height / zoom) / CELL_HEIGHT);
    const w = endCol - x;
    const h = endRow - y;

    const now = Date.now();
    if (now - lastRequestTime < throttleDelay) return; // Ignore si trop tôt

    lastRequestTime = now;

    if (ws.readyState !== WebSocket.OPEN) return;

    const request = {
        type: "request_zone",
        x: x,
        y: y,
        w: w,
        h: h
    };

    ws.send(JSON.stringify(request));
    console.log("Zone request sent:", request);
}

function sendUpdateCell(x, y, c) {
    if (ws.readyState !== WebSocket.OPEN) return;

    const update = {
        type: "cell_update",
        x: x,
        y: y,
        c: c
    }

    ws.send(JSON.stringify(update));
    console.log("Cell update sent:", update);
}

function sendUpdateCellBlock(cells) {
    if (ws.readyState !== WebSocket.OPEN) return;

    const chars = cells.map(cell => ({
        x: cell.x,
        y: cell.y,
        c: cell.c
    }));

    const update = {
        type: "cell_update_block",
        chars: chars
    }

    ws.send(JSON.stringify(update));
    console.log("Cell block update sent:", update);
}

/* -------------------- POPUP -------------------- */
function showOfflinePopup() {
    document.getElementById("offline").style.transform = "translateY(0px)";
}

function hideOfflinePopup() {
    document.getElementById("offline").style.transform = "translateY(100px)";
}