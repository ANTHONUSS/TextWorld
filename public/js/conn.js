const ws = new WebSocket("ws://localhost:8080");

ws.onopen = () => {
    console.log("Connected!");
}

ws.onmessage = (event) => {
    console.log("Messages received: " + event.data);
};

function sendData(data) {
    ws.send(data);
    console.log("Sending data to server...");
}