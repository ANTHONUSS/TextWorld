/* -------------------- TEXTE -------------------- */
function draw() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = "white";
    ctx.font = `${CELL_HEIGHT * zoom}px monospace`;
    ctx.textBaseline = "top";

    /* ----- TEXTE ----- */
    const startCol = Math.floor(cameraX / CELL_WIDTH);
    const endCol = Math.ceil((cameraX + canvas.width / zoom) / CELL_WIDTH);
    const startRow = Math.floor(cameraY / CELL_HEIGHT);
    const endRow = Math.ceil((cameraY + canvas.height / zoom) / CELL_HEIGHT);

    for (let row = startRow; row < endRow; row++) {
        for (let col = startCol; col < endCol; col++) {
            const cell = getCell(col, row);
            if (cell) {
                const ch = cell.getChar();
                const screenX = (cell.x * CELL_WIDTH - cameraX) * zoom;
                const screenY = (cell.y * CELL_HEIGHT - cameraY) * zoom;
                ctx.fillText(ch, screenX, screenY);
            }
        }
    }

    /* ----- CURSEUR ----- */
    const baseW = zoom, baseH = CELL_HEIGHT * zoom;
    const w = baseW, h = baseH;
    const sx = (cellCursorX * CELL_WIDTH - cameraX) * zoom - (w - baseW) / 2;
    const sy = (cellCursorY * CELL_HEIGHT - cameraY) * zoom - (h - baseH) / 2;

    ctx.fillStyle = "rgb(11,239,214)";
    ctx.fillRect(sx, sy, w, h);

    updateCoordsDisplay();
}

/* -------------------- COORDONNÉES -------------------- */
function updateCoordsDisplay() {
    document.getElementById("coords").textContent = `X: ${cellCursorX} | Y: ${cellCursorY}`;
}

/* -------------------- DÉPLACEMENT / SELECTION -------------------- */
canvas.addEventListener("click", (e) => {
    const rect = canvas.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;
    const worldX = (mouseX / zoom) + cameraX;
    const worldY = (mouseY / zoom) + cameraY;
    cellCursorX = Math.floor(worldX / CELL_WIDTH);
    cellCursorY = Math.floor(worldY / CELL_HEIGHT);

    draw();
    updateLineStart();
});

canvas.addEventListener("mousedown", (e) => {
    isDragging = true;

    dragStart.x = e.clientX;
    dragStart.y = e.clientY;
});
canvas.addEventListener("mouseup", () => {
    isDragging = false;

    canvas.style.cursor = "default";
});
canvas.addEventListener("mousemove", (e) => {
    if (isDragging) {
        const dx = e.clientX - dragStart.x;
        const dy = e.clientY - dragStart.y;
        cameraX -= dx / zoom;
        cameraY -= dy / zoom;
        dragStart.x = e.clientX;
        dragStart.y = e.clientY;

        draw();

        sendRequestZone();

        canvas.style.cursor = "move";
    }
});

canvas.addEventListener("wheel", (e) => {
    e.preventDefault();

    const zoomFactor = 1.1;
    const oldZoom = zoom;
    const rect = canvas.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;
    const worldX = cameraX + mouseX / oldZoom;
    const worldY = cameraY + mouseY / oldZoom;

    if (e.deltaY < 0 && zoom < 2.5) zoom *= zoomFactor;
    else if (e.deltaY > 0 && zoom > 0.5) zoom /= zoomFactor;

    cameraX = worldX - mouseX / zoom;
    cameraY = worldY - mouseY / zoom;

    sendRequestZone();

    draw();
}, {passive: false});

window.addEventListener("resize", () => {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    sendRequestZone();
    draw();
});

window.addEventListener("keydown", (e) => {
    if (e.key === "ArrowLeft") changeArrow(true, -1);
    if (e.key === "ArrowRight") changeArrow(true, 1);
    if (e.key === "ArrowUp") changeArrow(false, -1);
    if (e.key === "ArrowDown") changeArrow(false, 1);
});

function changeArrow(way, value) {
    if (way) cellCursorX += value;
    else cellCursorY += value;

    updateLineStart();
    ensureCursorInView();
    draw();
}

window.addEventListener("wheel", (e) => {
    if (e.ctrlKey) e.preventDefault();
}, {passive: false});

window.addEventListener("keydown", (e) => {
    if (e.ctrlKey && (e.key === '+' || e.key === '-' || e.key === '=')) {
        e.preventDefault();
    }
});


canvas.addEventListener("touchstart", (e) => {
    if (e.touches.length === 1) {
        isDragging = true;
        dragStart.x = e.touches[0].clientX;
        dragStart.y = e.touches[0].clientY;
    }
});

canvas.addEventListener("touchmove", (e) => {
    if (isDragging && e.touches.length === 1) {
        const dx = e.touches[0].clientX - dragStart.x;
        const dy = e.touches[0].clientY - dragStart.y;
        cameraX -= dx / zoom;
        cameraY -= dy / zoom;
        dragStart.x = e.touches[0].clientX;
        dragStart.y = e.touches[0].clientY;

        draw();
        sendRequestZone();
    }
});

canvas.addEventListener("touchend", () => {
    isDragging = false;
});

let initialPinchDistance = 0;

canvas.addEventListener("touchstart", (e) => {
    if (e.touches.length === 2) {
        const dx = e.touches[0].clientX - e.touches[1].clientX;
        const dy = e.touches[0].clientY - e.touches[1].clientY;
        initialPinchDistance = Math.sqrt(dx * dx + dy * dy);
    }
});

canvas.addEventListener("touchmove", (e) => {
    if (e.touches.length === 2) {
        const dx = e.touches[0].clientX - e.touches[1].clientX;
        const dy = e.touches[0].clientY - e.touches[1].clientY;
        const currentPinchDistance = Math.sqrt(dx * dx + dy * dy);

        const zoomFactor = currentPinchDistance / initialPinchDistance;
        const oldZoom = zoom;

        if (zoom <= 2.5 && zoom >= 0.5) {
            zoom *= zoomFactor;
        }
        if (zoom > 2.5) zoom = 2.5;
        if (zoom < 0.5) zoom = 0.5;


        const rect = canvas.getBoundingClientRect();
        const centerX = (e.touches[0].clientX + e.touches[1].clientX) / 2 - rect.left;
        const centerY = (e.touches[0].clientY + e.touches[1].clientY) / 2 - rect.top;
        const worldX = cameraX + centerX / oldZoom;
        const worldY = cameraY + centerY / oldZoom;

        cameraX = worldX - centerX / zoom;
        cameraY = worldY - centerY / zoom;

        initialPinchDistance = currentPinchDistance;

        draw();
        sendRequestZone();
    }
});