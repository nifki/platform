"use strict";

var ctx = null;
{
    var canvas = document.getElementById('game');
    if (canvas.getContext) {
        ctx = canvas.getContext('2d');
        ctx.mozImageSmoothingEnabled = false;
        ctx.webkitImageSmoothingEnabled = false;
        ctx.msImageSmoothingEnabled = false;
        ctx.imageSmoothingEnabled = false;
        ctx.scale(canvas.width, canvas.height);
    }
}

function rgb(r, g, b) {
    var R=255*r, G=255*g, B=255*b;
    if (R < 0) R = 0; else if (R > 255) R = 255;
    if (G < 0) G = 0; else if (G > 255) G = 255;
    if (B < 0) B = 0; else if (B > 255) B = 255;
    return "rgb(" + R + "," + G + "," + B + ")";
}

function render(state) {
    ctx.save()
    var win = state.window;
    ctx.fillStyle = rgb(win.r, win.g, win.b);
    ctx.fillRect(0.0, 0.0, 1.0, 1.0);
    ctx.scale(1.0/win.w, 1.0/win.h);
    ctx.translate(-win.x, -win.y);
    for (var i=0; i < state.images.length; i++) {
        ctx.drawImage(state.images[i], 32*i, 32*i);
    }
    ctx.restore()
}

function frame(state) {
    console.log("frame()");
    // Run the interpreter here.
    state.window.r += 0.01;
    var terminated = state.window.r >= 0.8;
    if (terminated) {
        // Nothing else to do. Stop the timer.
        clearInterval(intervalId);
    } else {
        // Draw a frame and wait to be called again.
        render(state);
    }
}

var intervalId = null;

function run(images, properties) {
    console.log("run()");
    var state = {
        "images": images,
        "window": {
            "r": 0, "g": 0, "b": 0,
            "x": 0, "y": 0, "w": properties.w, "h": properties.h
        }
    };
    if (ctx !== null) {
        intervalId = setInterval(frame, properties.msPerFrame, state)
        frame(state);
    }
}

function loadImagesThen(image_filenames, run) {
    var counter = image_filenames.length;
    var images = [];

    function count() {
        counter -= 1;
        if (counter == 0) {
            run(images);
        }
    }

    for (var i=0; i<image_filenames.length; i++) {
        var image = new Image();
        image.onload = count;
        image.src = image_filenames[i];
        image.className = "pixelated";
        images.push(image);
    }
}

function onload() {
    var properties = {
        w: 256, h: 256,
        msPerFrame: 40
    }
    loadImagesThen(
        ["man.png", "boulder.png"],
        function(images) { run(images, properties); }
    );
}

window.addEventListener("DOMContentLoaded", onload, false);
