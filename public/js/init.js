/* -------------------- INITIALISATION / VARIABLES -------------------- */
const canvas = document.getElementById("world");
const ctx = canvas.getContext("2d");
canvas.setAttribute("data-theme", "light");

let bgColor;
let accentColor;
let textColor;

function updateColors() {
    const styles = getComputedStyle(document.documentElement);
    bgColor = styles.getPropertyValue('--bg-color').trim();
    accentColor = styles.getPropertyValue('--accent-color').trim();
    textColor = styles.getPropertyValue('--text-color').trim();
}

function applySavedTheme() {
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
    canvas.setAttribute('data-theme', savedTheme);
    updateColors();
}
applySavedTheme();

const loaderTimeout = document.getElementById("loader-timeout");
setTimeout(() => {
    if (loaderTimeout) {
        loaderTimeout.style.marginBottom = "40px";
    }
}, 5000);

function hideLoader() {
    const loader = document.getElementById("loader");
    if (loader) {
        setTimeout(() => {
            loader.style.opacity = "0";
            setTimeout(() => {
                loader.style.display = "none";
            }, 300);
        }, 300);
    }
}


const themeToggle = document.getElementById("theme-toggle");

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