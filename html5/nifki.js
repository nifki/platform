"use strict";

/** Returns a 2D drawing context (a CanvasRenderingContext2D) for rendering
 * the graphics. */
function initCanvas(canvasId) {
    var canvas = document.getElementById(canvasId);
    if (!canvas.getContext) return null;
    var ctx = canvas.getContext("2d");
    if (!ctx) return null;
    if (typeof ctx.imageSmoothingEnabled !== "undefined") {
        ctx.imageSmoothingEnabled = false;
    } else {
        ctx.mozImageSmoothingEnabled = false;
        ctx.webkitImageSmoothingEnabled = false;
        ctx.msImageSmoothingEnabled = false;
    }
    ctx.scale(canvas.width, canvas.height);
    return ctx;
}

function rgb(r, g, b) {
    var R=255*r, G=255*g, B=255*b;
    if (R < 0) R = 0; else if (R > 255) R = 255;
    if (G < 0) G = 0; else if (G > 255) G = 255;
    if (B < 0) B = 0; else if (B > 255) B = 255;
    return "rgb(" + R + "," + G + "," + B + ")";
}

function render(state) {
    var ctx = state.platform.context2d;
    ctx.save();
    var win = state.window;
    ctx.fillStyle = rgb(win.R.v, win.G.v, win.B.v);
    ctx.fillRect(0.0, 0.0, 1.0, 1.0);
    ctx.scale(1.0/win.W.v, 1.0/win.H.v);
    ctx.translate(-win.X.v, -win.Y.v);
    for (var i=0; i < state.images.length; i++) {
        ctx.drawImage(state.images[i], 32*i, 32*i);
    }
    ctx.restore();
}

function doFrame(state) {
    var instructions = state.instructions;
    // Run the interpreter here.
    try {
        while (true) {
            instructions[state.frame.pc++](state);
        }
    } catch (e) {
        // All exceptions are fatal. Stop the timer.
        clearInterval(state.platform.intervalId);
        if (e === "END") {
            return;
        } else if (e === "WAIT") {
            // Draw a frame and wait to be called again.
            render(state);
            return;
        }
        console.log(e);
        throw e;
    }
}

function run(code, images, properties, canvasId) {
    var context2d = initCanvas(canvasId);
    if (!context2d) {
        throw "2D canvas is not available";
    }
    var state = {
        "instructions": code.instructions,
        "globals": code.globalValues,
        "images": images,
        "platform": {
            "context2d": context2d,
            "intervalId": null
        },
        "window": newObject("WINDOW", {
            "X": newNumber(0),
            "Y": newNumber(0),
            "W": newNumber(properties.w),
            "H": newNumber(properties.h),
            "R": newNumber(0),
            "G": newNumber(0),
            "B": newNumber(0)
            // "IsVisible": TODO
        }),
        "frame": newStackFrame(code.main, null)
    };
    state.platform.intervalId = setInterval(
        doFrame,
        properties.msPerFrame,
        state
    );
}

function loadImagesThen(imageFilenames, callback) {
    var counter = imageFilenames.length;
    var images = [];

    function count() {
        counter -= 1;
        if (counter == 0) {
            callback(images);
        }
    }

    for (var i=0; i < imageFilenames.length; i++) {
        var image = new Image();
        image.onload = count;
        image.src = imageFilenames[i];
        images.push(image);
    }
}

function onload() {
    loadImagesThen(
        ["man.png", "boulder.png"],
        function(images) {
            run(
                assemble(TEST_CODE),
                images,
                {"w": 256, "h": 256, "msPerFrame": 40},
                "game"
            );
        }
    );
}

window.addEventListener("DOMContentLoaded", onload, false);
