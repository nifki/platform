"use strict";

var BREAK;
var CONSTANT;
var FOR;
var GOTO;
var IF;
var LLOAD;
var LOAD;
var LOOP;
var LSTORE;
var SET;
var STORE;
var OPS;

(function() {
    // TODO: Adapt this when we do SSS unescaping.
    var NEWLINE = "\\A/";
    var dumpBuffer = "";
    function dump(s) {
        var start = 0;
        while (true) {
            var index = s.indexOf(NEWLINE, start);
            if (index < 0) {
                dumpBuffer += s.substring(start);
                break;
            }
            console.log("DUMP: " + dumpBuffer + s.substring(start, index));
            dumpBuffer = "";
            start = index + NEWLINE.length;
        }
    }

    function makeOp(func, pops, pushes) {
        func.pops = pops;
        func.pushes = pushes;
        return func;
    }

    BREAK = function BREAK(numLoops) {
        return makeOp(
            function BREAK(state, args) {
                var i = 1;
                while (i < numLoops) {
                    args.loop = args.loop.enclosing;
                    i++;
                }
                args.pc = args.loop.breakPC;
                args.loop = args.loop.enclosing;
                args.numResults = 0;
            },
            0,
            0
        );
    };

    /**
     * @param {object} value - the value to push on the stack (see
     * "value.txt").
     */
    CONSTANT = function CONSTANT(value) {
        return makeOp(
            function CONSTANT(state, args) {
                args.r0 = value;
                args.numResults = 1;
            },
            0,
            1
        );
    };

    /** The beginning of a "FOR ... NEXT ... ELSE" structure. */
    FOR = function FOR(loopPC, elsePC, breakPC) {
        return makeOp(
            function FOR(state, args) {
                var t = args.a0;
                function NEXT(state, args) {
                    var loopState = args.loop;
                    if (loopState.it === null) {
                        // Exit the loop.
                        args.loop = loopState.enclosing;
                        args.pc = elsePC;
                        args.numResults = 0;
                    } else {
                        // Execute the body of the loop.
                        args.r0 = loopState.it.key;
                        args.r1 = loopState.it.value;
                        args.numResults = 2;
                        args.pc = loopPC;
                        loopState.it = loopState.it.next();
                    }
                }
                args.loop = {
                    "loopPC": loopPC,
                    "elsePC": elsePC,
                    "breakPC": breakPC,
                    "enclosing": args.loop,
                    "next": NEXT,
                    "it": valueIterator(t)
                };
                args.loop.next(state, args);
            },
            1,
            2
        );
    };

    GOTO = function GOTO(targetPC) {
        return makeOp(
            function GOTO(state, args) {
                args.pc = targetPC;
                args.numResults = 0;
            },
            0,
            0
        );
    };

    IF = function IF(targetPC) {
        return makeOp(
            function IF(state, args) {
                var cond = args.a0;
                if (cond.type !== "boolean") {
                    throw (
                        "IF requires a boolean, not '" + valueToString(cond) +
                        "'"
                    );
                }
                if (cond.v === false) {
                    args.pc = targetPC;
                }
                args.numResults = 0;
            },
            1,
            0
        );
    };

    LLOAD = function LLOAD(index, name) {
        return makeOp(
            function LLOAD(state, args) {
                var value = args.locals[index];
                // `value === null` is just paranoia. It might be impossible.
                if (typeof value === "undefined" || value === null) {
                    throw (
                        "Local variable " + index + " (" + name +
                        ") not defined"  // The Java version omits "(<name>)"
                    );
                }
                args.r0 = value;
                args.numResults = 1;
            },
            0,
            1
        );
    };

    LOAD = function LOAD(index, name) {
        return makeOp(
            function LOAD(state, args) {
                var value = state.globals[index];
                if (typeof value === "undefined" || value === null) {
                    throw "Global variable " + name + " not defined";
                }
                args.r0 = value;
                args.numResults = 1;
            },
            0,
            1
        );
    };

    /** The beginning of a "LOOP ... WHILE ... NEXT ... ELSE" structure. */
    LOOP = function LOOP(loopPC, elsePC, breakPC) {
        return makeOp(
            function LOOP(state, args) {
                args.loop = {
                    "loopPC": loopPC,
                    "elsePC": elsePC,
                    "breakPC": breakPC,
                    "enclosing": args.loop,
                    "next": function NEXT(state, args) {
                        args.pc = loopPC;
                        args.numResults = 0;
                    }
                };
                // FIXME: Branch to `loopPC`?
                // args.loop.next(state);
                args.numResults = 0;
            },
            0,
            0
        );
    };

    LSTORE = function LSTORE(index, name) {
        return makeOp(
            function LSTORE(state, args) {
                args.locals[index] = args.a0;
                args.numResults = 0;
            },
            1,
            0
        );
    };

    SET = function SET(name) {
        return makeOp(
            function SET(state, args) {
                var v = args.a0;
                var o = args.a1;
                if (o.type !== "object") {
                    throw (
                        "Cannot apply SET to " + valueToString(o) +
                        "; an object is required"
                    );
                }
                var old = o.v[name];
                if (typeof old === "undefined" || old.type !== v.type) {
                    throw (
                        "SET " + valueToString(o) + "." + name +
                        " = " + valueToString(v)
                    );
                }
                if (o.objType === "SPRITE") {
                    // We store all touched sprites, and prune invisible ones
                    // at render time.
                    state.visibleSprites[o.objNum] = o;
                }
                o.v[name] = v;
                args.numResults = 0;
            },
            2,
            0
        );
    };

    STORE = function STORE(index, name) {
        return makeOp(
            function STORE(state, args) {
                state.globals[index] = args.a0;
                args.numResults = 0;
            },
            1,
            0
        );
    };

    OPS = {
        "+": makeOp(
            function ADD(state, args) {
                var y = args.a0;
                var x = args.a1;
                if (x.type === "number" && y.type === "number") {
                    args.r0 = newNumber(x.v + y.v);
                } else if (x.type === "string" && y.type === "string") {
                    args.r0 = newString(x.v + y.v);
                } else if (x.type === "table" && y.type === "table") {
                    var result = x.v;
                    var it = tableIterator(y.v);
                    while (it !== null) {
                        result = tablePut(result, it.key, it.value);
                        it = it.next();
                    }
                    args.r0 = newTable(result);
                } else {
                    throw (
                        "Cannot add " + valueToString(x) + " to " +
                        valueToString(y)
                    );
                }
                args.numResults = 1;
            },
            2,
            1
        ),
        "-": makeOp(
            function SUB(state, args) {
                var y = args.a0;
                var x = args.a1;
                if (x.type === "number" && y.type === "number") {
                    args.r0 = newNumber(x.v - y.v);
                } else if (x.type === "number" && y.type === "string") {
                    var yStr = y.v;
                    var xInt = x.v | 0;
                    if (xInt !== x.v) {
                        throw valueToString(x) + " is not an integer";
                    }
                    if (xInt < 0 || xInt > yStr.length) {
                        throw (
                            "Cannot remove " + valueToString(x) +
                            " characters from " + valueToString(y) +
                            ": index out of range"
                        );
                    }
                    var result = yStr.substring(xInt);
                    args.r0 = newString(result);
                } else if (x.type === "string" && y.type === "number") {
                    var xStr = x.v;
                    var yInt = y.v | 0;
                    if (yInt !== y.v) {
                        throw valueToString(y) + " is not an integer";
                    }
                    if (yInt < 0 || yInt > xStr.length) {
                        throw (
                            "Cannot remove " + valueToString(y) +
                            " characters from " + valueToString(x) +
                            ": index out of range"
                        );
                    }
                    var result = xStr.substring(0, xStr.length - yInt);
                    args.r0 = newString(result);
                } else if (x.type === "table" && y.type === "table") {
                    var result = EMPTY_TABLE;
                    var it = tableIterator(x.v);
                    while (it !== null) {
                        if (tableGet(y.v, it.key) === null) {
                            result = tablePut(result, it.key, it.value);
                        }
                        it = it.next();
                    }
                    args.r0 = newTable(result);
                } else {
                    throw (
                        "Cannot subtract " + valueToString(y) + " from " +
                        valueToString(x)
                    );
                }
                args.numResults = 1;
            },
            2,
            1
        ),
        "*": makeOp(
            function MUL(state, args) {
                function repeatString(string, number) {
                    var count = number.v | 0;
                    if (number.v !== count) {
                        throw valueToString(number) + " is not an integer";
                    }
                    if (count < 0) {
                        throw (
                            "Cannot concatenate " + valueToString(number) +
                            " copies of " + valueToString(string)
                        );
                    }
                    var result = "";
                    var unit = string.v;
                    while (count !== 0) {
                        if (count & 1) result += unit;
                        unit += unit;
                        count >>= 1;
                    }
                    return newString(result);
                }
                var y = args.a0;
                var x = args.a1;
                if (x.type === "number" && y.type === "number") {
                    args.r0 = newNumber(x.v * y.v);
                } else if (x.type === "number" && y.type === "string") {
                    args.r0 = repeatString(y, x);
                } else if (x.type === "string" && y.type === "number") {
                    args.r0 = repeatString(x, y);
                } else {
                    throw (
                        "Cannot multiply " + valueToString(x) + " by " +
                        valueToString(y)
                    );
                }
                args.numResults = 1;
            },
            2,
            1
        ),
        "/": makeOp(
            function DIV(state, args) {
                var y = args.a0;
                var x = args.a1;
                if (x.type === "number" && y.type === "number") {
                    args.r0 = newNumber(x.v / y.v);
                } else if (x.type === "number" && y.type === "string") {
                    var xInt = x.v | 0;
                    if (xInt !== x.v) {
                        throw valueToString(x) + " is not an integer";
                    }
                    var yStr = y.v;
                    if (xInt < 0 || xInt > yStr.length) {
                        throw (
                            "Cannot keep " + valueToString(x) +
                            " characters from " + valueToString(y) +
                            ": index out of range"
                        );
                    }
                    args.r0 = newString(yStr.substring(0, xInt));
                } else if (x.type === "string" && y.type === "number") {
                    var xStr = x.v;
                    var yInt = y.v | 0;
                    if (yInt !== y.v) {
                        throw valueToString(y) + " is not an integer";
                    }
                    if (yInt < 0 || yInt > xStr.length) {
                        throw (
                            "Cannot keep " + valueToString(y) +
                            " characters from " + valueToString(x) +
                            ": index out of range"
                        );
                    }
                    args.r0 = newString(xStr.substring(xStr.length - yInt));
                } else if (x.type === "table" && y.type === "table") {
                    var result = EMPTY_TABLE;
                    var it = tableIterator(x.v);
                    while (it !== null) {
                        if (tableGet(y.v, it.key) !== null) {
                            result = tablePut(result, it.key, it.value);
                        }
                        it = it.next();
                    }
                    args.r0 = newTable(result);
                } else {
                    throw (
                        "Cannot divide " + valueToString(x) + " by " +
                        valueToString(y)
                    );
                }
                args.numResults = 1;
            },
            2,
            1
        ),
        "%": makeOp(
            function MOD(state, args) {
                var y = args.a0;
                var x = args.a1;
                if (x.type !== "number" || y.type !== "number") {
                    throw (
                        "Cannot apply % to " + valueToString(x) + " and " +
                        valueToString(y) + "; two numbers are required"
                    );
                }
                var result = x.v - y.v * Math.floor(x.v / y.v);
                args.r0 = newNumber(result);
                args.numResults = 1;
            },
            2,
            1
        ),
        "**": makeOp(
            function POW(state, args) {
                var y = args.a0;
                var x = args.a1;
                if (x.type !== "number" || y.type !== "number") {
                    throw (
                        "Cannot apply ** to " + valueToString(x) + " and " +
                        valueToString(y) + "; two numbers are required"
                    );
                }
                args.r0 = newNumber(Math.pow(x.v, y.v));
                args.numResults = 1;
            },
            2,
            1
        ),
        "==": makeOp(
            function EQ(state, args) {
                var y = args.a0;
                var x = args.a1;
                try {
                    var cmp = compareValues(x, y);
                    args.r0 = (cmp === 0 ? VALUE_TRUE : VALUE_FALSE);
                } catch(err) {
                    args.r0 = VALUE_FALSE;
                }
                args.numResults = 1;
            },
            2,
            1
        ),
        "!=": makeOp(
            function NE(state, args) {
                var y = args.a0;
                var x = args.a1;
                try {
                    var cmp = compareValues(x, y);
                    args.r0 = (cmp !== 0 ? VALUE_TRUE : VALUE_FALSE);
                } catch(err) {
                    args.r1 = VALUE_TRUE;
                }
                args.numResults = 1;
            },
            2,
            1
        ),
        "<>": makeOp(
            function LG(state, args) {
                var y = args.a0;
                var x = args.a1;
                var cmp = compareValues(x, y);
                args.r0 = (cmp !== 0 ? VALUE_TRUE : VALUE_FALSE);
                args.numResults = 1;
            },
            2,
            1
        ),
        "<": makeOp(
            function LT(state, args) {
                var y = args.a0;
                var x = args.a1;
                var cmp = compareValues(x, y);
                args.r0 = (cmp < 0 ? VALUE_TRUE : VALUE_FALSE);
                args.numResults = 1;
            },
            2,
            1
        ),
        "<=": makeOp(
            function LTE(state, args) {
                var y = args.a0;
                var x = args.a1;
                var cmp = compareValues(x, y);
                args.r0 = (cmp <= 0 ? VALUE_TRUE : VALUE_FALSE);
                args.numResults = 1;
            },
            2,
            1
        ),
        ">": makeOp(
            function GT(state, args) {
                var y = args.a0;
                var x = args.a1;
                var cmp = compareValues(x, y);
                args.r0 = (cmp > 0 ? VALUE_TRUE : VALUE_FALSE);
                args.numResults = 1;
            },
            2,
            1
        ),
        ">=": makeOp(
            function GTE(state, args) {
                var y = args.a0;
                var x = args.a1;
                var cmp = compareValues(x, y);
                args.r0 = (cmp >= 0 ? VALUE_TRUE : VALUE_FALSE);
                args.numResults = 1;
            },
            2,
            1
        ),
        "ABS": makeOp(
            function ABS(state, args) {
                var x = args.a0;
                if (x.type !== "number") {
                    throw (
                        "Cannot apply ABS to " + valueToString(x) +
                        "; a number is required"
                    );
                }
                args.r0 = newNumber(Math.abs(x.v));
                args.numResults = 1;
            },
            1,
            1
        ),
        "AND": makeOp(
            function AND(state, args) {
                var y = args.a0;
                var x = args.a1;
                if (x.type !== "boolean" || y.type !== "boolean") {
                    throw (
                        "Cannot apply AND to " + valueToString(x) + " and " +
                        valueToString(y) + "; two booleans are required"
                    );
                }
                var result = x.v && y.v;
                args.r0 = (result ? VALUE_TRUE : VALUE_FALSE);
                args.numResults = 1;
            },
            2,
            1
        ),
        "CALL": makeOp(
            function CALL(state, args) {
                var funcArgs = args.a0;
                var f = args.a1;
                if (funcArgs.type !== "table" || f.type !== "function") {
                    throw (
                        "Can't call " + valueToString(f) +
                        " as a function (passing " + valueToString(funcArgs) +
                        ")"
                    );
                }
                state.frame.pc = args.pc;
                state.frame.loop = args.loop;
                if (state.frame.locals !== args.locals) throw "Assertion failure";
                state.frame = newStackFrame(f, state.frame);
                args.pc = state.frame.pc;
                args.locals = state.frame.locals;
                args.loop = null;
                args.r0 = funcArgs;
                args.numResults = 1;
            },
            2,
            1
        ),
        "CEIL": makeOp(
            function CEIL(state, args) {
                var x = args.a0;
                if (x.type !== "number") {
                    throw (
                        "Cannot apply CEIL to " + valueToString(x) +
                        "; a number is required"
                    );
                }
                args.r0 = newNumber(-Math.floor(-x.v));
                args.numResults = 1;
            },
            1,
            1
        ),
        "CLS": makeOp(
            function CLS(state, args) {
                for (var spriteNum in state.visibleSprites) {
                    var sprite = state.visibleSprites[spriteNum];
                    sprite.v.IsVisible = VALUE_FALSE;
                }
                args.numResults = 0;
            },
            0,
            0
        ),
        "CONTAINS": makeOp(
            function CONTAINS(state, args) {
                var k = args.a0;
                var t = args.a1;
                var result;
                if (t.type === "string") {
                    if (k.type === "number") {
                        var index = k.v | 0;
                        result = (
                            k.v === index && index >= 0 && index < t.v.length
                        );
                    } else {
                        result = false;
                    }
                } else if (t.type === "table") {
                    result = tableGet(t.v, k) !== null;
                } else if (t.type === "object") {
                    result = (typeof t.v[k.v]) !== "undefined";
                } else {
                    throw "Type error: cannot subscript " + valueToString(t);
                }
                args.r0 = (result ? VALUE_TRUE : VALUE_FALSE);
                args.numResults = 1;
            },
            2,
            1
        ),
        "DGET": makeOp(
            function DGET(state, args) {
                var k = args.a0;
                var t = args.a1;
                if (t.type === "table") {
                    var v = tableGet(t.v, k);
                    if (v === null) {
                        throw (
                            valueToString(t) + "[" + valueToString(k) +
                            "] is not defined"
                        );
                    }
                    args.r0 = t;
                    args.r1 = k;
                    args.r2 = v;
                    args.numResults = 3;
                } else {
                    // This instruction is only used to implement assignment
                    // to a value in a table.
                    throw (
                        "Cannot assign to " + valueToString(t) + "[" +
                        valueToString(k) + "]; a table is required"
                    );
                }
            },
            2,
            3
        ),
        "DROP": makeOp(
            function DROP(state, args) {
                // Ignores args.a0
                args.numResults = 0;
            },
            1,
            0
        ),
        "DROPTABLE": makeOp(
            function DROPTABLE(state, args) {
                var value = args.a0;
                if (value.type !== "table" || tableSize(value.v) !== 0) {
                    throw (
                        "A function can be called as a subroutine only if " +
                        "it returns [] (the empty table)"
                    );
                }
                args.numResults = 0;
            },
            1,
            0
        ),
        "DUMP": makeOp(
            function DUMP(state, args) {
                var value = args.a0;
                if (value.type === "string") {
                    dump(value.v);
                } else {
                    dump(valueToLongString(value));
                }
                args.numResults = 0;
            },
            1,
            0
        ),
        "END": makeOp(
            function END(state, args) {
                throw "END";
            },
            0,
            0
        ),
        "FALSE": CONSTANT(VALUE_FALSE),
        "FLOOR": makeOp(
            function FLOOR(state, args) {
                var x = args.a0;
                if (x.type !== "number") {
                    throw (
                        "Cannot apply FLOOR to " + valueToString(x) +
                        "; a number is required"
                    );
                }
                args.r0 = newNumber(Math.floor(x.v));
                args.numResults = 1;
            },
            1,
            1
        ),
        "GET": makeOp(
            function GET(state, args) {
                var k = args.a0;
                var t = args.a1;
                var result;
                if (t.type === "string") {
                    if (k.type !== "number") {
                        throw (
                            "Cannot subscript " + valueToString(t) +
                            " by " + valueToString(k)
                        );
                    }
                    var index = k.v | 0;
                    if (index !== k.v) {
                        throw (
                            "String subscript must be an integer, not " +
                            valueToString(k)
                        );
                    }
                    if (index >= 0 && index < t.v.length) {
                        result = newString(t.v.substring(index, index + 1));
                    } else {
                        throw (
                            "IndexOutOfBoundsException: " +
                            "String index out of range: " + index
                        );
                    }
                } else if (t.type === "table") {
                    result = tableGet(t.v, k);
                } else if (t.type === "object") {
                    if (k.type !== "string") {
                        throw (
                            "Cannot subscript " + valueToString(t) +
                            " by " + valueToString(k)
                        );
                    }
                    result = t.v[k.v];
                } else {
                    throw "Cannot subscript " + valueToString(t);
                }
                if (typeof result === "undefined" || result === null) {
                    throw (
                        valueToString(t) + "[" + valueToString(k) + "]" +
                        " is not defined"
                    );
                }
                args.r0 = result;
                args.numResults = 1;
            },
            2,
            1
        ),
        "KEYS": makeOp(
            function KEYS(state, args) {
                var result = state.platform.getKeys();
                args.r0 = newTable(result);
                args.numResults = 1;
            },
            0,
            1
        ),
        "LEN": makeOp(
            function LEN(state, args) {
                var x = args.a0;
                if (x.type === "string") {
                    args.r0 = newNumber(x.v.length);
                } else if (x.type === "table") {
                    args.r0 = newNumber(tableSize(x.v));
                } else {
                    throw "Cannot apply LEN to " + valueToString(x);
                }
                args.numResults = 1;
            },
            1,
            1
        ),
        "MAX": makeOp(
            function MAX(state, args) {
                var y = args.a0;
                var x = args.a1;
                var cmp = compareValues(x, y);
                args.r0 = (cmp > 0 ? x : y);
                args.numResults = 1;
            },
            2,
            1
        ),
        "MIN": makeOp(
            function MIN(state, args) {
                var y = args.a0;
                var x = args.a1;
                var cmp = compareValues(x, y);
                args.r0 = (cmp < 0 ? x : y);
                args.numResults = 1;
            },
            2,
            1
        ),
        "NEG": makeOp(
            function NEG(state, args) {
                var x = args.a0;
                if (x.type === "number") {
                    args.r0 = newNumber(-x.v);
                } else if (x.type === "string") {
                    // TODO: Proper unicode.
                    args.r0 = newString(x.v.split("").reverse().join(""));
                } else {
                    throw "Cannot negate " + valueToString(x);
                }
                args.numResults = 1;
            },
            1,
            1
        ),
        "NEXT": makeOp(
            function NEXT(state, args) {
                args.loop.next(state, args);
            },
            0,
            0
        ),
        "NOT": makeOp(
            function NOT(state, args) {
                var x = args.a0;
                if (x.type === "boolean") {
                    args.r0 = (x.v ? VALUE_FALSE : VALUE_TRUE);
                    args.numResults = 1;
                } else {
                    throw (
                        "Cannot apply NOT to " + valueToString(x) +
                        "; a boolean is required"
                    );
                }
            },
            1,
            1
        ),
        "OR": makeOp(
            function OR(state, args) {
                var y = args.a0;
                var x = args.a1;
                if (x.type !== "boolean" || y.type !== "boolean") {
                    throw (
                        "Cannot apply OR to " + valueToString(x) + " and " +
                        valueToString(y) + "; two booleans are required"
                    );
                }
                var result = x.v || y.v;
                args.r0 = (result ? VALUE_TRUE : VALUE_FALSE);
                args.numResults = 1;
            },
            2,
            1
        ),
        "PUT": makeOp(
            function PUT(state, args) {
                var v = args.a0;
                var k = args.a1;
                var t = args.a2;
                if (TYPE_INDEX[k.type] > 5) {
                    throw (
                        "'" + valueToString(k) +
                        "' cannot be used as key in a table"
                    );
                }
                if (t.type !== "table") {
                    throw "'" + valueToString(t) + "' is not a table";
                }
                var result = tablePut(t.v, k, v);
                args.r0 = newTable(result);
                args.numResults = 1;
            },
            3,
            1
        ),
        "RANDOM": makeOp(
            function RANDOM(state, args) {
                // TODO: We should rethink this API with cryptographic
                // randomness in mind. (JS numbers don't have enough bits).
                args.r0 = newNumber(Math.random());
                args.numResults = 1;
            },
            0,
            1
        ),
        "RETURN": makeOp(
            function RETURN(state, args) {
                var result = args.a0;
                state.frame = state.frame.caller;
                args.pc = state.frame.pc;
                args.locals = state.frame.locals;
                args.loop = state.frame.loop;
                args.r0 = result;
                args.numResults = 1;
            },
            1,
            0
        ),
        "ROUND": makeOp(
            function ROUND(state, args) {
                var x = args.a0;
                if (x.type !== "number") {
                    throw (
                        "Cannot apply ROUND to " + valueToString(x) +
                        "; a number is required"
                    );
                }
                args.r0 = newNumber(Math.round(x.v));
                args.numResults = 1;
            },
            1,
            1
        ),
        "SPRITE": makeOp(
            function SPRITE(state, args) {
                var picture = args.a0;
                if (picture.type !== "picture") {
                    throw (
                        "Cannot apply SPRITE " + valueToString(picture) +
                        "; a picture is required"
                    );
                }
                var sprite = newObject(
                    "SPRITE",  // Shown in long string representation.
                    {
                        "X": newNumber(0),
                        "Y": newNumber(0),
                        "W": newNumber(picture.v.width),
                        "H": newNumber(picture.v.height),
                        "Depth": newNumber(0),
                        "Picture": picture,
                        "IsVisible": VALUE_FALSE
                    }
                );
                args.r0 = sprite;
                args.numResults = 1;
            },
            1,
            1
        ),
        "SQRT": makeOp(
            function SQRT(state, args) {
                var x = args.a0;
                if (x.type === "number") {
                    if (x.v < 0) {
                        throw (
                            "Cannot square root negative number " +
                            valueToString(x)
                        );
                    }
                    args.r0 = newNumber(Math.sqrt(x.v));
                    args.numResults = 1;
                } else {
                    throw "Cannot square root " + valueToString(x);
                }
            },
            1,
            1
        ),
        "TABLE": CONSTANT(newTable(EMPTY_TABLE)),
        "TRUE": CONSTANT(VALUE_TRUE),
        "WAIT": makeOp(
            function WAIT(state, args) {
                throw "WAIT";
            },
            0,
            0
        ),
        "WHILE": makeOp(
            function WHILE(state, args) {
                var cond = args.a0;
                if (cond.type !== "boolean") {
                    throw (
                        "Cannot execute WHILE " + valueToString(cond) +
                        "; a boolean is required"
                    );
                }
                if (cond.v === false) {
                    args.pc = args.loop.elsePC;
                    args.loop = args.loop.enclosing;
                }
                args.numResults = 0;
            },
            1,
            0
        ),
        "WINDOW": makeOp(
            function WINDOW(state, args) {
                args.r0 = state.window;
                args.numResults = 1;
            },
            0,
            1
        ),
        "XOR": makeOp(
            function XOR(state, args) {
                var y = args.a0;
                var x = args.a1;
                if (x.type !== "boolean" || y.type !== "boolean") {
                    throw (
                        "Cannot apply XOR to " + valueToString(x) + " and " +
                        valueToString(y) + "; two booleans are required"
                    );
                }
                var result = x.v ^ y.v;
                args.r0 = (result ? VALUE_TRUE : VALUE_FALSE);
                args.numResults = 1;
            },
            2,
            1
        )
    };
})();
