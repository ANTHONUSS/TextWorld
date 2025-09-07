// javascript
class Cell {
    x;
    y;
    c;

    /**
     * @param {number} x - colonne
     * @param {number} y - ligne
     * @param {string} c - caractère
     */
    constructor(x = 0, y = 0, c = ' ') {
        this.x = x;
        this.y = y;
        this.c = c;
    }

    getChar() { return this.c; }
}

let cells = new Map();
function key(col, row) { return `${col},${row}`; }

function getCell(col, row) {
    const k = key(col, row);
    let cell = cells.get(k);
    return cell ?? null;
}

function setCell(col, row, ch) {
    const cell = new Cell(col, row, ch);
    const k  = key(col, row);
    cells.set(k, cell);
}

function deleteCell(col, row) {
    const k = key(col, row);
    cells.delete(k);
}