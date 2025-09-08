const ws = new WebSocket("ws://localhost:8080");

ws.onopen = () => {
    console.log("Connected!");
}

ws.onmessage = (event) => {
    const data = JSON.parse(event.data);

    switch(data.type) {
        case "zone_data":
            console.log("Zone data received:", data);

            for(let i = 0; i < data.cells.length; i++) {
                const cell = data.cells[i];
                setCell(cell.x, cell.y, cell.char);
            }

            console.log("Cells updated");

            break;

        case "cell_update":
            console.log("Cell update received:", data);

            setCell(data.x, data.y, data.char);

            break;
    }

};


function sendRequestZone(x, y, w, h) {
    const request = {
        type: "request_zone",
        x: x,
        y: y,
        width: w,
        height: h
    };

    ws.send(JSON.stringify(request));
    console.log("Zone request sent:", request);
}

function sendUpdateCell(x, y, c) {
    const update = {
        type: "cell_update",
        x: x,
        y: y,
        char: c
    }

    ws.send(JSON.stringify(update));
    console.log("Cell update sent:", update);
}