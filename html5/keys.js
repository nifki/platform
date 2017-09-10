"use strict";

// See https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/keyCode
// As noted there, this doesn't really work in general.
var KEY_CODES = {
    // TODO (in Java too): Function keys.
    "Escape": 0x1B,
    "PrintScreen": 0x2C,  // I can't test this, the OS grabs it first.
    "ScrollLock": 0x91,
    "Break": 0x13,  // ="Pause"
    // Typewriter first row.
    "BackQuote": 0xC0,
    "Minus": 0xAD,
    "Equals": 0x3D,
    "BackSpace": 0x08,
    // Typewriter second row.
    "Tab": 0x09,
    "OpenBracket": 0xDB,  // "["/"{"
    "CloseBracket": 0xDD,  // "]"/"}"
    "LeftBrace": 0,  // My keyboard doesn't have this.
    "RightBrace": 0,  // My keyboard doesn't have this.
    "Enter": 0x0D,
    // Typewriter third row.
    "CapsLock": 0x14,
    "Semicolon": 0x3B,
    "Quote": 0xDE,
    "Hash": 0xA3,
    // Typewriter fourth row.
    "Shift": 0x10,
    "BackSlash": 0xDC,
    "Comma": 0xBC,
    "FullStop": 0xBE,
    "Slash": 0xBF,
    // Typewriter fifth row.
    "Control": 0x11,
    "Alt": 0x12,
    "Space": 0x20,
    // Navigation keys.
    "Insert": 0x2D,
    "Home": 0x24,
    "PageUp": 0x21,
    "Delete": 0x2E,
    "End": 0x23,
    "PageDown": 0x22,
    // Arrow keys.
    "DownArrow": 0x28,
    "LeftArrow": 0x25,
    "RightArrow": 0x27,
    "UpArrow": 0x26
    // TODO (in Java too): Numpad.
};

(function() {
    for (var i=0; i < 10; i++) {
        KEY_CODES["Number" + i] = 0x30 + i;
    }
    for (var j=0; j < 26; j++) {
        KEY_CODES["Letter" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"[j]] = 0x41 + j;
    }
})();

var KEY_NAMES = Object.getOwnPropertyNames(KEY_CODES);
KEY_NAMES.sort();

// Make an array of Nifki values representing the key names in sorted order.
var KEY_NAME_VALUES = (function() {
    var result = [];
    for (var i=0; i < KEY_NAMES.length; i++) {
        result.push(newString(KEY_NAMES[i]));
    }
    return result;
})();

var getKeys = (function() {
    function makeKeyStateTable(values) {
        return {"keys": KEY_NAME_VALUES, "values": values};
    }

    var keyStates = [];
    for (var key in KEY_NAMES) {
        keyStates.push(VALUE_FALSE);
    }

    function onKeyEvent(event) {
        if (event.defaultPrevented) {
            return;
        }

        if (typeof event.keyCode === "undefined") {
            // We can only handle the "keyCode" attribute for now.
            return;
        }

        var newKeyStates = [];
        for (var i=0; i < KEY_NAMES.length; i++) {
            var key = KEY_NAMES[i];
            var state = keyStates[i];
            if (event.keyCode === KEY_CODES[key]) {
                state = event.type === "keydown" ? VALUE_TRUE : VALUE_FALSE;
                event.preventDefault();  // Consume this key press.
            }
            newKeyStates.push(state);
        }
        keyStates = newKeyStates;
    }

    var canvas = document.getElementById("game"); // TEMPORARY.
    if (typeof canvas.tabIndex !== "number" || canvas.tabIndex < 0) {
        canvas.tabIndex = 0;
    }
    canvas.addEventListener("keydown", onKeyEvent, true);
    canvas.addEventListener("keyup", onKeyEvent, true);

    function getKeys() {
        return makeKeyStateTable(keyStates);
    }
    return getKeys;
})();
