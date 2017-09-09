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
    var R = Math.round(255*r), G = Math.round(255*g), B = Math.round(255*b);
    if (R < 0) R = 0; else if (R > 255) R = 255;
    if (G < 0) G = 0; else if (G > 255) G = 255;
    if (B < 0) B = 0; else if (B > 255) B = 255;
    return "rgb(" + R + "," + G + "," + B + ")";
}

function render(state) {
    function drawOrderCompare(spriteA, spriteB) {
        if (spriteA.objNum === spriteB.objNum) { return 0; }
        var aDepth = spriteA.v.Depth.v, bDepth = sprite.v.Depth.v;
        if (aDepth !== bDepth) { return aDepth - bDepth; }
        var aX = spriteA.v.X.v, bX = sprite.v.X.v;
        if (aX !== bX) { return aX - bX; }
        var aY = spriteA.v.Y.v, bY = sprite.v.Y.v;
        if (aY !== bY) { return aY - bY; }
        var aName = spriteA.v.Picture.originalName;
        var bName = spriteB.v.Picture.originalName;
        if (aName < bName) { return -1; }
        if (bName < aName) { return 1; }
        return 0;
    }
    // TODO: Preserve the context across frames?
    // Don't touch it if the window is unchanged?
    var ctx = state.platform.context2d;
    ctx.save();
    var win = state.window.v;
    ctx.fillStyle = rgb(win.R.v, win.G.v, win.B.v);
    ctx.fillRect(0.0, 0.0, 1.0, 1.0);
    ctx.scale(1.0/win.W.v, 1.0/win.H.v);
    ctx.translate(-win.X.v, -win.Y.v);
    var spritesToDraw = [];
    for (var spriteNum in state.visibleSprites) {
        var sprite = state.visibleSprites[spriteNum];
        if (sprite.v.IsVisible.v) {
            spritesToDraw.push(sprite);
        }
    }
    spritesToDraw.sort(drawOrderCompare);
    for (var i=0; i < spritesToDraw.length; i++) {
        var sprite = spritesToDraw[i];
        var image = sprite.v.Picture.v;
        var x = sprite.v.X.v;
        var y = sprite.v.Y.v;
        var w = sprite.v.W.v;
        var h = sprite.v.H.v;
        ctx.drawImage(image, x, y, w, h);
    }
    var spriteNums = Object.keys(state.visibleSprites);
    for (var i=0; i < spriteNums.length; i++) {
        var spriteNum = spriteNums[i];
        var sprite = state.visibleSprites[spriteNum];
        if (!sprite.v.IsVisible.v) {
            delete state.visibleSprites[spriteNum];
        }
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
        if (e === "WAIT") {
            // Draw a frame and wait to be called again.
            render(state);
            return;
        }
        // All other exceptions are fatal. Stop the timer.
        clearInterval(state.platform.intervalId);
        if (e === "END") {
            // Normal exit.
            return;
        }
        // Show the error to the user.
        console.log(e, "at PC", state.frame.pc-1, instructions[state.frame.pc-1]);
        throw e;
    }
}

function pictureNameFromFilename(filename) {
    var parts = filename.split('/');
    // TODO: Correct variable naming.
    var pictureName = parts[parts.length - 1].replace('.png', 'PNG');
    return pictureName;
}

function run(code, images, properties, canvasId) {
    var context2d = initCanvas(canvasId);
    if (!context2d) {
        throw "2D canvas is not available";
    }
    var globalValues = code.globalValues.slice();
    var globalNames = code.globalNames.slice();
    for (var i=0; i < images.length; i++) {
        var name = pictureNameFromFilename(images[i].src);
        var picture = newPicture(images[i], name);
        if (name in code.globalMappings) {
            var index = globalNames.indexOf(name);
            if (index < 0) {
                throw "Assertion failed";
            }
            globalValues[index] = picture;
        } else {
            globalValues.push(picture);
            globalNames.push(name);
        }
    }
    var state = {
        "instructions": code.instructions,
        "globals": globalValues,
        "globalNames": globalNames,
        "platform": {
            "context2d": context2d,
            "intervalId": null
        },
        "visibleSprites": {},
        "window": newObject("WINDOW", {
            "X": newNumber(0),
            "Y": newNumber(0),
            "W": newNumber(properties.w),
            "H": newNumber(properties.h),
            "R": newNumber(0),
            "G": newNumber(0),
            "B": newNumber(0),
            "IsVisible": VALUE_TRUE
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
