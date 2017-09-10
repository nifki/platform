"use strict";

/**
 * Returns a 2D drawing context (a CanvasRenderingContext2D) for rendering
 * the graphics.
 * @param canvasId - the ID of the HTML canvas element to use.
 * @param width - the desired horizontal resolution in pixels.
 * @param height - the desired vertical resolution in pixels.
 */
function initCanvas(canvasId, width, height) {
    var canvas = document.getElementById(canvasId);
    if (!canvas.getContext) return null;
    var ctx = canvas.getContext("2d");
    if (!ctx) return null;
    canvas.width = width;
    canvas.height = height;
    if (typeof ctx.imageSmoothingEnabled !== "undefined") {
        ctx.imageSmoothingEnabled = false;
    } else {
        ctx.mozImageSmoothingEnabled = false;
        ctx.webkitImageSmoothingEnabled = false;
        ctx.msImageSmoothingEnabled = false;
    }
    ctx.scale(width, height);
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
        var aDepth = spriteA.v.Depth.v, bDepth = spriteB.v.Depth.v;
        if (aDepth !== bDepth) { return aDepth - bDepth; }
        var aX = spriteA.v.X.v, bX = spriteB.v.X.v;
        if (aX !== bX) { return aX - bX; }
        var aY = spriteA.v.Y.v, bY = spriteB.v.Y.v;
        if (aY !== bY) { return aY - bY; }
        var aName = spriteA.v.Picture.originalName;
        var bName = spriteB.v.Picture.originalName;
        if (aName < bName) { return -1; }
        if (bName < aName) { return 1; }
        return 0;
    }

    var ctx = state.platform.context2d;
    ctx.save();
    // Set coordinate system and draw background.
    var win = state.window.v;
    ctx.fillStyle = rgb(win.R.v, win.G.v, win.B.v);
    ctx.fillRect(0.0, 0.0, 1.0, 1.0);
    ctx.scale(1.0/win.W.v, 1.0/win.H.v);
    ctx.translate(-win.X.v, -win.Y.v);
    // Draw visible sprites.
    var spritesToDraw = Object.values(state.visibleSprites);
    spritesToDraw.sort(drawOrderCompare);
    for (var i=0; i < spritesToDraw.length; i++) {
        var v = spritesToDraw[i].v;
        var image = v.Picture.v;
        var x = v.X.v;
        var y = v.Y.v;
        var w = v.W.v;
        var h = v.H.v;
        ctx.drawImage(image, x, y, w, h);
    }
    ctx.restore();
}

/**
 * Returns a fresh object mapping spriteNum to Sprite containing only those
 * sprites for which `IsVisible` is true.
 * @param visibleSprites an object mapping spriteNum to Sprite.
 */
function removeHiddenSprites(visibleSprites) {
    var result = {};
    for (var spriteNum in visibleSprites) {
        var sprite = visibleSprites[spriteNum];
        if (sprite.v.IsVisible.v) {
            result[spriteNum] = sprite;
        }
    }
    return result;
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
            state.visibleSprites = removeHiddenSprites(state.visibleSprites);
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
    var context2d = initCanvas(canvasId, properties.w, properties.h);
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
        [
            "Rocks_rockPNG",
            "Rocks_blankPNG",
            "Rocks_leftPNG",
            "Rocks_diamondPNG",
            "Rocks_earthPNG",
            "Rocks_manPNG",
            "Rocks_rightPNG",
            "Rocks_wallPNG"
        ],
        function(images) {
            run(
                assemble(TEST_CODE),
                images,
                {"w": 384, "h": 384, "msPerFrame": 40},
                "game"
            );
        }
    );
}

window.addEventListener("DOMContentLoaded", onload, false);
