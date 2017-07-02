"use strict";

/** Returns a 2D drawing context (a CanvasRenderingContext2D) for rendering
 * the graphics. */
function initCanvas(canvasId) {
    var canvas = document.getElementById(canvasId);
    if (!canvas.getContext) return null;
    var ctx = canvas.getContext("2d");
    if (!ctx) return null;
    ctx.mozImageSmoothingEnabled = false;
    ctx.webkitImageSmoothingEnabled = false;
    ctx.msImageSmoothingEnabled = false;
    ctx.imageSmoothingEnabled = false;
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

function render(platform, state) {
    var ctx = platform.context2d;
    ctx.save();
    var win = state.window;
    ctx.fillStyle = rgb(win.r, win.g, win.b);
    ctx.fillRect(0.0, 0.0, 1.0, 1.0);
    ctx.scale(1.0/win.w, 1.0/win.h);
    ctx.translate(-win.x, -win.y);
    for (var i=0; i < state.images.length; i++) {
        ctx.drawImage(state.images[i], 32*i, 32*i);
    }
    ctx.restore();
}

function frame(platform, state) {
    // Run the interpreter here.
    var terminated = false;
    try {
        state.window.r += 0.01;
        if (state.window.r >= 0.8) {
            throw "END";
        }
    } catch (e) {
        // All exceptions are fatal. Stop the timer.
        clearInterval(platform.intervalId);
        if (e === "END") {
            return;
        }
        console.log(e);
        throw e;
    }
    // Draw a frame and wait to be called again.
    render(platform, state);
}

function run(instructions, images, properties, canvasId) {
    var context2d = initCanvas(canvasId);
    if (!context2d) {
        throw "2D canvas is not available";
    }
    var platform = {
        "context2d": context2d,
        "intervalId": null
    };
    var state = {
        "images": images,
        "window": {
            "r": 0, "g": 0, "b": 0,
            "x": 0, "y": 0, "w": properties.w, "h": properties.h
        }
    };
    platform.intervalId = setInterval(
        frame,
        properties.msPerFrame,
        platform,
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
                assemble("END"),
                images,
                {"w": 256, "h": 256, "msPerFrame": 40},
                "game",
            );
        }
    );
}

window.addEventListener("DOMContentLoaded", onload, false);
