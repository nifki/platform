"use strict";

/**
 * An "instruction" is a Javascript function that takes an interpreter state,
 * mutates it in place, and returns nothing. It also has public fields:
 *   - pops - the number of stack items consumed.
 *   - pushes - the number of stack items created.
 */

function decodeString(source) {
    // FIXME
    return source;
}


var assemble = function() {
    var STOP_WORDS = {
        "THEN": null,
        "WHILE": null,
        "NEXT": null,
        "ELSE": null
    };

    function makeOp(func, pops, pushes) {
        func.pops = pops;
        func.pushes = pushes;
        return func;
    }

    var OPS = {
        "END": makeOp(
            function END(state) {
                throw "END";
            },
            0,
            0
        )
    };

    /**
     * @param {object} value - the value to push on the stack (see
     * "value.txt").
     */
    function CONSTANT(value) {
        return makeOp(
            function CONSTANT(state) {
                // TODO
            },
            0,
            1
        );
    }

    function LOAD(index, name) {
        return makeOp(
            function LOAD(state) {
                // TODO
            },
            0,
            1
        );
    }

    function STORE(index, name) {
        return makeOp(
            function STORE(state) {
                // TODO
            },
            1,
            0
        );
    }

    function LLOAD(index, name) {
        return makeOp(
            function LLOAD(state) {
                // TODO
            },
            0,
            1
        );
    }

    function LSTORE(index, name) {
        return makeOp(
            function LSTORE(state) {
                // TODO
            },
            1,
            0
        );
    }

    function SET(name) {
        return makeOp(
            function SET(state) {
                // TODO
            },
            2,
            0
        );
    }

    function assemble(source) {
        console.log("assemble()");

        var lineNum = 1;

        function syntaxException(message) {
            return "SyntaxException: " + message + " at line " + lineNum;
        }

        /** Matches and classifies a single instruction (or newline or
         * comment). Everything except whitespace will be matched.
         * Note: Javascript RegExps have state; we need a fresh one.
         */
        var wordRegExp = new RegExp(
            "(\n|#[^\n]*\n?)" + "|" +
            "\"([^\"]*)(\"?)" + "|" +
            "([0-9][^\t\n\r ]*)" + "|" +
            "([^\(\t\n\r ]+)\(([^\)\t\n\r ]*)(\)?)" + "|" +
            "[^\t\n\r ]+",
            "g"
        );
        var wordMatch; // Next match object from `wordRegExp`.
        var word; // Equal to `wordMatch[0]`, except when null.

        /** Moves on to the next instruction.
         */
        function next() {
            while (true) {
                var m = wordRegExp.exec(source);
                if (m !== null) {
                    if (typeof m[1] !== "undefined") {
                        // Newline and/or comment.
                        lineNum++;
                        continue;
                    }
                    word = m[0];
                } else {
                    word = null;
                }
                wordMatch = m;
                console.log("next()");
                return;
            }
        }

        next(); // Initialise `word` and `wordMatch()`.

        var globalMappings = {};
        var globalValues = [];

        function getGlobalIndex(name) {
            if (!(name in globalMappings)) {
                globalMappings[name] = globalValues.length;
                globalValues.push(null);
            }
            return globalMappings[name];
        }

        var instructions = [];
        var instructionsFilled = 0;

        /** Appends a placeholder to `instructions` and returns a
         * function that replaces it with the passed instruction.
         */
        function allocate() {
            var slotIndex = instructions.length;
            instructions.push(null);
            return function(instruction) {
                if (instructions[slotIndex] !== null) {
                    throw syntaxException(
                        "This slot has already been filled");
                }
                instructions[slotIndex] = instruction;
                instructionsFilled++;
            };
        }

        /** Fills in the next instruction.
         * Equivalent to 'allocate()(instruction)'.
         */
        function append(instruction) {
            allocate()(instruction);
        }

        function parseFunctionBody(entrySP, exitSP, originalName) {
            console.log("parseFunctionBody(" + originalName + ")");

            var localMappings = {};
            var numLocals = 0;

            function getLocalIndex(name) {
                if (!(name in localMappings)) {
                    localMappings[name] = numLocals;
                    numLocals++;
                }
                return localMappings[name];
            }

            function parseBlock(numLoops) {
                var sp = entrySP;
                function appendAndUpdateSP(instruction) {
                    sp -= instruction.pops;
                    if (sp < 0) {
                        throw syntaxException("Stack underflow");
                    }
                    sp += instruction.pushes;
                    append(instruction);
                }
                while (
                    word !== null &&
                    !(word in STOP_WORDS) &&
                    !word.startsWith("DEF(")
                ) {
                    console.log(word);
                    var instruction = null;
                    // TODO: "IF", "LOOP", "FOR", "BREAK", "RETURN", "ERROR".
                    if (typeof wordMatch[2] !== "undefined") {
                        // String literal.
                        if (typeof wordMatch[3] === "undefined") {
                            throw syntaxException(
                                "Unclosed string literal: " + wordMatch[0]);
                        }
                        appendAndUpdateSP(CONSTANT({
                            "type": "string",
                            "value": decodeString(wordMatch[2])
                        }));
                    } else if (typeof wordMatch[4] !== "undefined") {
                        // Number literal.
                        var value = +wordMatch[4];
                        if (value !== value) {
                            throw syntaxException("Not a number");
                        }
                        appendAndUpdateSP(CONSTANT({
                            "type": "number",
                            "value": value
                        }));
                    } else if (typeof wordMatch[5] !== "undefined") {
                        if (typeof wordMatch[7] === "undefined") {
                            throw syntaxException(
                                "Missing ')' in " + word);
                        }
                        var op = wordMatch[5];
                        var name = wordMatch[6];
                        var index;
                        if (op === "LOAD") {
                            index = getGlobalIndex(name);
                            appendAndUpdateSP(LOAD(index, name));
                        } else if (op === "STORE") {
                            index = getGlobalIndex(name);
                            appendAndUpdateSP(STORE(index, name));
                        } else if (op === "LLOAD") {
                            index = getLocalIndex(name);
                            appendAndUpdateSP(LLOAD(index, name));
                        } else if (op === "LSTORE") {
                            index = getLocalIndex(name);
                            appendAndUpdateSP(LSTORE(index, name));
                        } else if (op === "SET") {
                            appendAndUpdateSP(SET(name));
                        } else {
                            throw syntaxException(
                                "Unknown instruction: " + word);
                        }
                    } else if (word === ";") {
                        if (sp !== 0) {
                            throw syntaxException(
                                "Stack should be empty before executing ;");
                        }
                    } else {
                        if (!(word in OPS)) {
                            throw syntaxException(
                                "Unknown instruction: " + word);
                        }
                        appendAndUpdateSP(OPS[word]);
                    }
                    next();
                }
                if (sp != exitSP) {
                    throw syntaxException(
                        "Stack contains " + sp +
                        " items; should be " + exitSP
                    );
                }
            }

            if (instructionsFilled !== instructions.length) {
                throw syntaxException(
                    "Used != Filled");
            }
            var startPC = instructions.length;
            parseBlock(0);
            if (instructionsFilled !== instructions.length) {
                throw syntaxException(
                    "Used != Filled");
            }
            var endPC = instructions.length;
            // TODO: Do we need to compute `stackLen`?
            // Return a function value (see "value.txt").
            return {
                "type": "function",
                "startPC": startPC,
                "numLocals": numLocals,
                "originalName": originalName
            };
        }

        // Parse the main program.
        var main = parseFunctionBody(0, 0, "<main>");
        append(OPS.END);
        // Parse the function definitions.
        while (word !== null) {
            console.log(word);
            if (typeof wordMatch[5] === "undefined" ||
                wordMatch[5] !== "DEF"
            ) {
                throw syntaxException(
                    "Expected DEF(name)");
            }
            if (typeof wordMatch[7] === "undefined") {
                throw syntaxException(
                    "Missing ')' in " + word);
            }
            var name = wordMatch[6];
            next();
            var value = parseFunctionBody(1, -1, name);
            // Was `defineFunction()` in Java:
            var index = getGlobalIndex(name);
            globalValues[index] = value;
        }

        return { // TODO: Add more.
            "main": main
        };
    }

    return assemble;
}();

assemble(
    '# A maze game.\n' +
    '\n' +
    'WINDOW 4 SET(X)\n' +
    'WINDOW 4 SET(Y)\n' +
    'WINDOW 8 SET(W)\n' +
    'WINDOW 8 SET(H)\n' +
    '\n' +
    '# Define the mapping of characters to sprite names.\n' +
    'TABLE\n' +
    '  " " "examples/blank.png" PUT\n' +
    '  "A" "examples/blank.png" PUT # This marks the start position.\n' +
    '  ":" "examples/earth.png" PUT\n' +
    '  "+" "examples/diamond.png" PUT\n' +
    '  "O" "examples/boulder.png" PUT\n' +
    '  "#" "examples/wall.png" PUT\n' +
    '  "<" "examples/left.png" PUT\n' +
    '  ">" "examples/right.png" PUT\n' +
    'STORE(_spriteNames) ;\n' +
    '\n' +
    '# Define some useful constants.\n' +
    'LOAD(_spriteNames) " " GET STORE(_blank) ;\n' +
    'LOAD(_spriteNames) "+" GET STORE(_diamond) ;\n' +
    'LOAD(_spriteNames) "O" GET STORE(_boulder) ;\n' +
    'TABLE\n' +
    '  LOAD(_spriteNames) " " GET FALSE PUT\n' +
    '  LOAD(_spriteNames) ":" GET FALSE PUT\n' +
    '  LOAD(_spriteNames) "+" GET TRUE PUT\n' +
    '  LOAD(_spriteNames) "O" GET TRUE PUT\n' +
    '  LOAD(_spriteNames) "#" GET FALSE PUT\n' +
    '  LOAD(_spriteNames) "<" GET TRUE PUT\n' +
    '  LOAD(_spriteNames) ">" GET FALSE PUT\n' +
    'STORE(_left) ;\n' +
    'TABLE\n' +
    '  LOAD(_spriteNames) " " GET FALSE PUT\n' +
    '  LOAD(_spriteNames) ":" GET FALSE PUT\n' +
    '  LOAD(_spriteNames) "+" GET TRUE PUT\n' +
    '  LOAD(_spriteNames) "O" GET TRUE PUT\n' +
    '  LOAD(_spriteNames) "#" GET FALSE PUT\n' +
    '  LOAD(_spriteNames) "<" GET FALSE PUT\n' +
    '  LOAD(_spriteNames) ">" GET TRUE PUT\n' +
    'STORE(_right) ;\n' +
    'TABLE\n' +
    '  LOAD(_spriteNames) " " GET FALSE PUT\n' +
    '  LOAD(_spriteNames) ":" GET FALSE PUT\n' +
    '  LOAD(_spriteNames) "+" GET FALSE PUT\n' +
    '  LOAD(_spriteNames) "O" GET FALSE PUT\n' +
    '  LOAD(_spriteNames) "#" GET TRUE PUT\n' +
    '  LOAD(_spriteNames) "<" GET TRUE PUT\n' +
    '  LOAD(_spriteNames) ">" GET TRUE PUT\n' +
    'STORE(_wall) ;\n' +
    '\n' +
    '# Define the map.\n' +
    'TABLE\n' +
    '   0 "################" PUT\n' +
    '   1 "#AOO: O:O :OO+O#" PUT\n' +
    '   2 "#+:#<>+<>+<>+O+#" PUT\n' +
    '   3 "#+#+OO>OO>OO>+O#" PUT\n' +
    '   4 "#+#OO<O++#++#O+#" PUT\n' +
    '   5 "#+#O< + +#++#+O#" PUT\n' +
    '   6 "#+#O#:+:<:++#O+#" PUT\n' +
    '   7 "#+#+ >:<>##:#+O#" PUT\n' +
    '   8 "#+#:#   O O:#O+#" PUT\n' +
    '   9 "#+#:#::O:<O>O+O#" PUT\n' +
    '  10 "#+#:> O:<OOO>O+#" PUT\n' +
    '  11 "#+#::>:<+:++#+O#" PUT\n' +
    '  12 "#+>::::+#:++#O+#" PUT\n' +
    '  13 "#+:>##:#O##:<+O#" PUT\n' +
    '  14 "#::::::::::::::#" PUT\n' +
    '  15 "################" PUT\n' +
    'LSTORE(map) ;\n' +
    '\n' +
    '# Construct sprites for the map, count diamonds, and find the start position.\n' +
    'TABLE STORE(_bSprites) ;\n' +
    '0.0 STORE(_numDiamonds) ;\n' +
    'LLOAD(map) FOR DROP LSTORE(y) ;\n' +
    '  LOAD(_bSprites) LLOAD(y) TABLE PUT STORE(_bSprites) ;\n' +
    '  LLOAD(map) LLOAD(y) GET LSTORE(row) ;\n' +
    '  LLOAD(row) FOR DROP LSTORE(x) ;\n' +
    '    # TABLE "x" LLOAD(x) PUT "y" LLOAD(y) PUT PRINT\n' +
    '    LLOAD(row) LLOAD(x) GET LSTORE(char) ;\n' +
    '    LOAD(_spriteNames) LLOAD(char) GET 1 1 SPRITE LSTORE(sp) ;\n' +
    '    LLOAD(sp) LLOAD(x) SET(X) ;\n' +
    '    LLOAD(sp) LLOAD(y) SET(Y) ;\n' +
    '    LLOAD(sp) TRUE SET(IsVisible) ;\n' +
    '    LOAD(_bSprites) LLOAD(y) DGET LLOAD(x) LLOAD(sp) PUT PUT STORE(_bSprites) ;\n' +
    '    LLOAD(char) "A" == IF ;\n' +
    '      LLOAD(x) LSTORE(manX) ;\n' +
    '      LLOAD(y) LSTORE(manY) ;\n' +
    '    THEN ;\n' +
    '      LLOAD(char) "+" == IF ;\n' +
    '        LOAD(_numDiamonds) 1 + STORE(_numDiamonds) ;\n' +
    '      THEN ; ELSE ;\n' +
    '    ELSE ;\n' +
    '  NEXT ; ELSE ;\n' +
    'NEXT ; ELSE ;\n' +
    '\n' +
    'LOAD(_numDiamonds) DUMP " diamonds to collect\\A/" DUMP\n' +
    '\n' +
    '# Construct a sprite for the man.\n' +
    '"examples/man.png" 1 1 SPRITE LSTORE(man)\n' +
    'LLOAD(man) TRUE SET(IsVisible)\n' +
    '\n' +
    '# Game loop.\n' +
    '0 LSTORE(dx)\n' +
    '0 LSTORE(dy)\n' +
    '0 LSTORE(manCount)\n' +
    '0 LSTORE(bgCount)\n' +
    'LOOP LOAD(_numDiamonds) 0 > WHILE ;\n' +
    '\n' +
    '  # Display a frame.\n' +
    '  LLOAD(man) LLOAD(manX) LLOAD(dx) LLOAD(manCount) * - SET(X) ;\n' +
    '  LLOAD(man) LLOAD(manY) LLOAD(dy) LLOAD(manCount) * - SET(Y) ;\n' +
    '  WINDOW LLOAD(man) "X" GET 3.5 - SET(X) ;\n' +
    '  WINDOW LLOAD(man) "Y" GET 3.5 - SET(Y) ;\n' +
    '  WINDOW LLOAD(bgCount) 0.05 * SET(B)\n' +
    '  WAIT ;\n' +
    '\n' +
    '  # Read the keyboard, if the man is exactly in a square.\n' +
    '  LLOAD(manCount) 0 == IF ;\n' +
    '    0 LSTORE(dx) ;\n' +
    '    0 LSTORE(dy) ;\n' +
    '    KEYS "LeftArrow" GET IF ;\n' +
    '      LLOAD(dx) 1 - LSTORE(dx)\n' +
    '    THEN ; ELSE ;\n' +
    '    KEYS "RightArrow" GET IF ;\n' +
    '      LLOAD(dx) 1 + LSTORE(dx)\n' +
    '    THEN ; ELSE ;\n' +
    '    KEYS "UpArrow" GET IF ;\n' +
    '      LLOAD(dy) 1 - LSTORE(dy)\n' +
    '    THEN ; ELSE ;\n' +
    '    KEYS "DownArrow" GET IF ;\n' +
    '      LLOAD(dy) 1 + LSTORE(dy)\n' +
    '    THEN ; ELSE ;\n' +
    '    LLOAD(dy) 0 != IF ;\n' +
    '      0 LSTORE(dx)\n' +
    '    THEN ; ELSE ;\n' +
    '    # Check what we\'re about to hit.\n' +
    '      LOAD(_bSprites)\n' +
    '      LLOAD(manY) LLOAD(dy) + GET\n' +
    '      LLOAD(manX) LLOAD(dx) + GET\n' +
    '    LSTORE(aheadSP)\n' +
    '    LLOAD(aheadSP) "Picture" GET LSTORE(ahead) ;\n' +
    '    # Is it a diamond?\n' +
    '    LLOAD(ahead) LOAD(_diamond) == IF ;\n' +
    '      LOAD(_numDiamonds) 1 - STORE(_numDiamonds) ;\n' +
    '      LOAD(_numDiamonds) DUMP " diamonds left\\A/" DUMP ;\n' +
    '      20 LSTORE(bgCount) ;\n' +
    '    THEN ; ELSE ;\n' +
    '    # Is it a boulder?\n' +
    '    LLOAD(ahead) LOAD(_boulder) == IF ;\n' +
    '      # Check what we\'re pushing it into.\n' +
    '        LOAD(_bSprites)\n' +
    '        LLOAD(manY) LLOAD(dy) 2 * + GET\n' +
    '        LLOAD(manX) LLOAD(dx) 2 * + GET\n' +
    '        "Picture" GET\n' +
    '        LOAD(_blank) ==\n' +
    '        LLOAD(dy) 0 == AND\n' +
    '      IF ;\n' +
    '        # Push succeeds.\n' +
    '          LOAD(_bSprites)\n' +
    '          LLOAD(manY) LLOAD(dy) 2 * + GET\n' +
    '          LLOAD(manX) LLOAD(dx) 2 * + GET\n' +
    '          LOAD(_boulder)\n' +
    '        SET(Picture)\n' +
    '      THEN ;\n' +
    '        # Push fails.\n' +
    '        0 LSTORE(dx) ;\n' +
    '        0 LSTORE(dy) ;\n' +
    '      ELSE ;\n' +
    '    THEN ; ELSE ;\n' +
    '    # Is it a wall?\n' +
    '    LOAD(_wall) LLOAD(ahead) GET IF ;\n' +
    '      0 LSTORE(dx) ;\n' +
    '      0 LSTORE(dy) ;\n' +
    '    THEN ; ELSE ;\n' +
    '    # Does the man move?\n' +
    '    LLOAD(dx) 0 != LLOAD(dy) 0 != OR IF ;\n' +
    '      LLOAD(manX) LLOAD(dx) + LSTORE(manX) ;\n' +
    '      LLOAD(manY) LLOAD(dy) + LSTORE(manY) ;\n' +
    '      1.0 LSTORE(manCount)\n' +
    '      # Overwrite the square ahead with a blank.\n' +
    '      LLOAD(aheadSP) LOAD(_blank) SET(Picture)\n' +
    '    THEN ; ELSE ;\n' +
    '  THEN ; ELSE ;\n' +
    '\n' +
    '  # Move the man.\n' +
    '  LLOAD(manCount) 0 > IF ;\n' +
    '    LLOAD(manCount) 0.25 - LSTORE(manCount)\n' +
    '  THEN ; ELSE ;\n' +
    '  LLOAD(manCount) 0 == IF ;\n' +
    '    0 LSTORE(dx) ;\n' +
    '    0 LSTORE(dy) ;\n' +
    '  THEN ; ELSE ;\n' +
    '  \n' +
    '  # Fade the background.\n' +
    '  LLOAD(bgCount) 0 > IF ;\n' +
    '    LLOAD(bgCount) 1 - LSTORE(bgCount) ;\n' +
    '  THEN ; ELSE ;\n' +
    '\n' +
    '  # Scan for boulders.\n' +
    '  LOAD(_bSprites) FOR DROP LSTORE(y) ;\n' +
    '    LOAD(_bSprites) LLOAD(y) GET LSTORE(row) ;\n' +
    '    LLOAD(row) FOR DROP LSTORE(x) ;\n' +
    '      LLOAD(row) LLOAD(x) GET LSTORE(boulderSp) ;\n' +
    '      # Is it a boulder?\n' +
    '      LLOAD(boulderSp) "Picture" GET LOAD(_boulder) == IF ;\n' +
    '        # Which way to fall?\n' +
    '        LOAD(_bSprites) LLOAD(y) 1 + GET LSTORE(rowBelow) ;\n' +
    '        LLOAD(rowBelow) LLOAD(x) GET LSTORE(belowSp) ;\n' +
    '        LLOAD(x) LSTORE(destX) ;\n' +
    '        LLOAD(belowSp) "Picture" GET LSTORE(below) ;\n' +
    '          LOAD(_left) LLOAD(below) GET\n' +
    '          LLOAD(row) LLOAD(x) 1 - GET "Picture" GET LOAD(_blank) == AND\n' +
    '        IF ;\n' +
    '          # It could fall left.\n' +
    '          LLOAD(x) 1 - LSTORE(destX) ;\n' +
    '        THEN ; ELSE ;\n' +
    '          LOAD(_right) LLOAD(below) GET\n' +
    '          LLOAD(row) LLOAD(x) 1 + GET "Picture" GET LOAD(_blank) == AND\n' +
    '        IF ;\n' +
    '          # It could fall right.\n' +
    '          LLOAD(x) 1 + LSTORE(destX) ;\n' +
    '        THEN ; ELSE ;\n' +
    '        # Is there anything in the way?\n' +
    '        LLOAD(rowBelow) LLOAD(destX) GET LSTORE(destSp) ;\n' +
    '          LLOAD(destSp) "Picture" GET LOAD(_blank) ==\n' +
    '          LLOAD(destX) LLOAD(manX) LLOAD(dx) - !=\n' +
    '          LLOAD(destX) LLOAD(manX) != AND\n' +
    '          LLOAD(y) 1 + LLOAD(manY) !=\n' +
    '          LLOAD(y) LLOAD(manY) != AND OR AND\n' +
    '        IF ;\n' +
    '          # Move the boulder.\n' +
    '          LLOAD(boulderSp) LOAD(_blank) SET(Picture)\n' +
    '          LLOAD(destSp) "falling" SET(Picture)\n' +
    '          # Does it squash the man?\n' +
    '            LLOAD(destX) LLOAD(manX) ==\n' +
    '            LLOAD(y) 1 + LLOAD(manY) ==\n' +
    '            LLOAD(y) 2 + LLOAD(manY) ==\n' +
    '            OR AND\n' +
    '          IF ;\n' +
    '            LOAD(_boulder) STORE(_result) ;\n' +
    '            BREAK BREAK BREAK\n' +
    '          THEN ; ELSE ;\n' +
    '        THEN ; ELSE ;\n' +
    '      THEN ; ELSE ;\n' +
    '      LLOAD(boulderSp) "Picture" GET "falling" == IF ;\n' +
    '        LLOAD(boulderSp) LOAD(_boulder) SET(Picture)\n' +
    '      THEN ; ELSE ;\n' +
    '    NEXT ; ELSE ;\n' +
    '  NEXT ; ELSE ;\n' +
    'NEXT ;\n' +
    '  LOAD(_diamond) STORE(_result) ;\n' +
    'ELSE ;\n' +
    '\n' +
    'CLS\n' +
    '\n' +
    'LLOAD(man) FALSE SET(IsVisible)\n' +
    'TABLE STORE(_bsprites)\n' +
    '\n' +
    '# Celebration fireworks.\n' +
    'WINDOW 0 SET(X)\n' +
    'WINDOW 0 SET(Y)\n' +
    'WINDOW 0 SET(B)\n' +
    'TABLE LSTORE(sparks)\n' +
    '100 FOR DROP LSTORE(count) ;\n' +
    '    LLOAD(sparks)\n' +
    '    LLOAD(count)\n' +
    '    TABLE\n' +
    '    "x" 3.5 PUT\n' +
    '    "y" 3.5 PUT\n' +
    '    "dx" RAND 0.5 - 1 * PUT\n' +
    '    "dy" RAND 0.5 - 1 * PUT\n' +
    '    "sp" LOAD(_result) 1 1 SPRITE PUT\n' +
    '  PUT LSTORE(sparks) ;\n' +
    'NEXT ; ELSE ;\n' +
    'LOOP ; TRUE WHILE ;\n' +
    '  LLOAD(count) 1 + 100 % LSTORE(count) ;\n' +
    '    LLOAD(sparks) LLOAD(count) GET "sp" GET FALSE SET(IsVisible)\n' +
    '    LLOAD(sparks)\n' +
    '    LLOAD(count) \n' +
    '    TABLE\n' +
    '    "x" 3.5 PUT\n' +
    '    "y" 3.5 PUT\n' +
    '    "dx" RAND 0.5 - 1 * PUT\n' +
    '    "dy" RAND 0.5 - 1 * PUT\n' +
    '    "sp" LOAD(_result) 1 1 SPRITE PUT\n' +
    '  PUT LSTORE(sparks) ;\n' +
    '  LLOAD(sparks) FOR DROP LSTORE(i) ;\n' +
    '    LLOAD(sparks) LLOAD(i) GET LSTORE(spark) ;\n' +
    '    LLOAD(spark) "x" DGET LLOAD(spark) "dx" GET + PUT LSTORE(spark) ;\n' +
    '    LLOAD(spark) "y" DGET LLOAD(spark) "dy" GET + PUT LSTORE(spark) ;\n' +
    '    LLOAD(spark) "dy" DGET 0.01 + PUT LSTORE(spark) ;\n' +
    '    LLOAD(spark) "sp" GET LSTORE(sparkSp) ;\n' +
    '    LLOAD(sparkSp) LLOAD(spark) "x" GET SET(X) ;\n' +
    '    LLOAD(sparkSp) LLOAD(spark) "y" GET SET(Y) ;\n' +
    '    LLOAD(sparkSp) TRUE SET(IsVisible) ;\n' +
    '    LLOAD(sparks) LLOAD(i) LLOAD(spark) PUT LSTORE(sparks) ;\n' +
    '  NEXT ; ELSE ;\n' +
    '  WAIT ;\n' +
    'NEXT ; ELSE ;'
);
