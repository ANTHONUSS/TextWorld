/* -------------------- CURSEUR & FENÊTRE -------------------- */
function ensureCursorInView() {
    const margin = 20;
    const screenCursorX = cellCursorX * CELL_WIDTH * zoom - cameraX;
    const screenCursorY = cellCursorY * CELL_HEIGHT * zoom - cameraY;

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
    if (e.key.length === 1) {
        const char = e.key;
        setCell(cellCursorX, cellCursorY, char);
        cellCursorX += 1;
    }

    if (e.key === "Space") {
        deleteCell(cellCursorX, cellCursorY);
        cellCursorX += 1;
    }

    if (e.key === "Backspace") {
        e.preventDefault();
        deleteCell(cellCursorX-1, cellCursorY);
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