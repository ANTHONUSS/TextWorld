/* -------------------- INITIALISATION / VARIABLES -------------------- */
const canvas = document.getElementById("world");
const ctx = canvas.getContext("2d");

const CELL_WIDTH = 10;
const CELL_HEIGHT = 14;

let cameraX =0;
let cameraY =0;
let zoom = 1;

let isDragging = false;
let dragStart = { x: 0, y: 0 };

let cellCursorX = 0;
let cellCursorY = 0;
let lineStartX = 0;
let lineStartY = 0;

canvas.width = window.innerWidth;
canvas.height = window.innerHeight;