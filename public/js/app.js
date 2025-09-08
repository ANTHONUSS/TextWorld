/* -------------------- CURSEUR & FENÊTRE -------------------- */
function ensureCursorInView() {
    const margin = 20;
    const screenCursorX = (cellCursorX * CELL_WIDTH - cameraX) * zoom;
    const screenCursorY = (cellCursorY * CELL_HEIGHT - cameraY) * zoom;

    if (screenCursorX < margin) {
        cameraX = cellCursorX * CELL_WIDTH - margin / zoom;
    } else if (screenCursorX > canvas.width - margin) {
        cameraX = cellCursorX * CELL_WIDTH - (canvas.width - margin) / zoom;
    }

    if (screenCursorY < margin) {
        cameraY = cellCursorY * CELL_HEIGHT - margin / zoom;
    } else if (screenCursorY > canvas.height - margin) {
        cameraY = cellCursorY * CELL_HEIGHT - (canvas.height - margin) / zoom;
    }
}

/* -------------------- TEXTE -------------------- */
function updateLineStart() {
    lineStartX = cellCursorX;
    lineStartY = cellCursorY;
}

window.addEventListener("keydown", (e) => {
    if (e.ctrlKey) return;

    if (e.key.length === 1) {
        e.preventDefault();

        const char = e.key;
        setCell(cellCursorX, cellCursorY, char);
        sendUpdateCell(cellCursorX, cellCursorY, char);
        cellCursorX += 1;
    }

    if (e.key === "Space") {
        e.preventDefault();

        deleteCell(cellCursorX, cellCursorY);
        sendUpdateCell(cellCursorX, cellCursorY, ' ');
        cellCursorX += 1;
    }

    if (e.key === "Backspace") {
        e.preventDefault();

        deleteCell(cellCursorX-1, cellCursorY);
        sendUpdateCell(cellCursorX-1, cellCursorY, ' ');
        cellCursorX -= 1;
    }

    if (e.key === "Enter") {
        cellCursorY += 1;
        cellCursorX = lineStartX;
    }

    ensureCursorInView();
    draw();
});

window.addEventListener("paste", (e) => {
    const text = e.clipboardData.getData("text/plain");
    for (const char of text) {
        if (char === "\n") {
            cellCursorX = lineStartX;
            cellCursorY += 1;
        } else {
            setCell(cellCursorX, cellCursorY, char);
        }
        cellCursorX += 1;
    }

    lineStartX = cellCursorX;
    lineStartY = cellCursorY;
    draw();
});

/* -------------------- MAIN -------------------- */
draw();
sendRequestZone(cameraX, cameraY, canvas.width, canvas.height);