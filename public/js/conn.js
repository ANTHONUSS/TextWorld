let lastRequestTime = 0;
const throttleDelay = 100;
const ws = new WebSocket("ws://localhost:8080");

ws.onopen = () => {
    console.log("Connected!");
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
    }
};

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