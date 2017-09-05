"use strict";

var OPS;
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

(function() {
    function makeOp(func, pops, pushes) {
        func.pops = pops;
        func.pushes = pushes;
        return func;
    }

    OPS = {
        "+": makeOp(
            function ADD(state) {
                var y = state.frame.stack.pop();
                var x = state.frame.stack.pop();
                if (x.type === "number" && y.type === "number") {
                    state.frame.stack.push(newNumber(x.v + y.v));
                } else if (x.type === "string" && y.type === "string") {
                    state.frame.stack.push(newString(x.v + y.v));
                } else if (x.type === "table" && y.type === "table") {
                    var it = tableIterator(y);
                    while (it !== null) {
                        x = tablePut(x, it.key, it.value);
                        it = it.next();
                    }
                    state.frame.stack.push(x);
                } else {
                    throw (
                        "Cannot add " + valueToString(x) + " to " +
                        valueToString(y)
                    );
                }
            },
            2,
            1
        ),
        "-": makeOp(
            function SUB(state) {
                var y = state.frame.stack.pop();
                var x = state.frame.stack.pop();
                if (x.type === "number" && y.type === "number") {
                    state.frame.stack.push(newNumber(x.v - y.v));
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
                    state.frame.stack.push(newString(result));
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
                    state.frame.stack.push(newString(result));
                } else if (x.type === "table" && y.type === "table") {
                    var result = newTable();
                    var it = tableIterator(x);
                    while (it !== null) {
                        if (tableGet(y, it.key) === null) {
                            result = tablePut(result, it.key, it.value);
                        }
                        it = it.next();
                    }
                    state.frame.stack.push(result);
                } else {
                    throw (
                        "Cannot subtract " + valueToString(y) + " from " +
                        valueToString(x)
                    );
                }
            },
            2,
            1
        ),
        "*": makeOp(
            function MUL(state) {
                function repeatString(string, number) {
                    var count = number.v | 0;
                    if (number.v != count) {
                        throw valueToString(number) + " is not an integer";
                    }
                    if (count < 0) {
                        throw (
                            "Cannot concatenate " + valueToString(number) +
                            " copies of " + valueToString(string)
                        );
                    }
                    var result = '';
                    for (var i=0; i < count; i++) {
                        result += string.v;
                    }
                    return newString(result);
                }
                var y = state.frame.stack.pop();
                var x = state.frame.stack.pop();
                if (x.type === "number" && y.type === "number") {
                    state.frame.stack.push(newNumber(x.v * y.v));
                } else if (x.type === "number" && y.type === "string") {
                    state.frame.stack.push(repeatString(y, x));
                } else if (x.type === "string" && y.type === "number") {
                    state.frame.stack.push(repeatString(x, y));
                } else {
                    throw (
                        "Cannot multiply " + valueToString(x) + " by " +
                        valueToString(y)
                    );
                }
            },
            2,
            1
        ),
        "/": makeOp(
            function DIV(state) {
                var y = state.frame.stack.pop();
                var x = state.frame.stack.pop();
                if (x.type === "number" && y.type === "number") {
                    state.frame.stack.push(newNumber(x.v / y.v));
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
                    state.frame.stack.push(
                        newString(yStr.substring(0, xInt))
                    );
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
                    state.frame.stack.push(
                        newString(xStr.substring(xStr.length - yInt))
                    );
                } else if (x.type === "table" && y.type === "table") {
                    var result = newTable();
                    var it = tableIterator(x);
                    while (it !== null) {
                        if (tableGet(y, it.key) !== null) {
                            result = tablePut(result, it.key, it.value);
                        }
                        it = it.next();
                    }
                    state.frame.stack.push(result);
                } else {
                    throw (
                        "Cannot divide " + valueToString(x) + " by " +
                        valueToString(y)
                    );
                }
            },
            2,
            1
        ),
        "%": makeOp(
            function MOD(state) {
                var y = state.frame.stack.pop();
                var x = state.frame.stack.pop();
                if (x.type !== "number" || y.type !== "number") {
                    throw (
                        "Cannot apply % to " + valueToString(x) + " and " +
                        valueToString(y) + "; two numbers are required"
                    );
                }
                var result = x.v - y.v * Math.floor(x.v / y.v);
                state.frame.stack.push(newNumber(result));
            },
            2,
            1
        ),
        "==": makeOp(
            function EQ(state) {
                var y = state.frame.stack.pop();
                var x = state.frame.stack.pop();
                try {
                    var cmp = compareValues(x, y);
                    state.frame.stack.push(
                        cmp === 0 ? VALUE_TRUE : VALUE_FALSE);
                } catch(err) {
                    state.frame.stack.push(VALUE_FALSE);
                }
            },
            2,
            1
        ),
        "!=": makeOp(
            function NE(state) {
                var y = state.frame.stack.pop();
                var x = state.frame.stack.pop();
                try {
                    var cmp = compareValues(x, y);
                    state.frame.stack.push(
                        cmp !== 0 ? VALUE_TRUE : VALUE_FALSE);
                } catch(err) {
                    state.frame.stack.push(VALUE_TRUE);
                }
            },
            2,
            1
        ),
        "<>": makeOp(
            function LG(state) {
                var y = state.frame.stack.pop();
                var x = state.frame.stack.pop();
                var cmp = compareValues(x, y);
                state.frame.stack.push(cmp !== 0 ? VALUE_TRUE : VALUE_FALSE);
            },
            2,
            1
        ),
        "<": makeOp(
            function LT(state) {
                var y = state.frame.stack.pop();
                var x = state.frame.stack.pop();
                var cmp = compareValues(x, y);
                state.frame.stack.push(cmp < 0 ? VALUE_TRUE : VALUE_FALSE);
            },
            2,
            1
        ),
        "<=": makeOp(
            function LTE(state) {
                var y = state.frame.stack.pop();
                var x = state.frame.stack.pop();
                var cmp = compareValues(x, y);
                state.frame.stack.push(cmp <= 0 ? VALUE_TRUE : VALUE_FALSE);
            },
            2,
            1
        ),
        ">": makeOp(
            function GT(state) {
                var y = state.frame.stack.pop();
                var x = state.frame.stack.pop();
                var cmp = compareValues(x, y);
                state.frame.stack.push(cmp > 0 ? VALUE_TRUE : VALUE_FALSE);
            },
            2,
            1
        ),
        ">=": makeOp(
            function GTE(state) {
                var y = state.frame.stack.pop();
                var x = state.frame.stack.pop();
                var cmp = compareValues(x, y);
                state.frame.stack.push(cmp >= 0 ? VALUE_TRUE : VALUE_FALSE);
            },
            2,
            1
        ),
        "ABS": makeOp(
            function ABS(state) {
                var x = state.frame.stack.pop();
                if (x.type !== "number") {
                    throw (
                        "Cannot apply ABS to " + valueToString(x) +
                        "; a number is required"
                    );
                }
                state.frame.stack.push(newNumber(Math.abs(x.v)));
            },
            1,
            1
        ),
        "AND": makeOp(
            function AND(state) {
                var y = state.frame.stack.pop();
                var x = state.frame.stack.pop();
                if (x.type !== "boolean" || y.type !== "boolean") {
                    throw (
                        "Cannot apply AND to " + valueToString(x) + " and " +
                        valueToString(y) + "; two booleans are required"
                    );
                }
                var result = x.v && y.v;
                state.frame.stack.push(result ? VALUE_TRUE : VALUE_FALSE);
            },
            2,
            1
        ),
        "CALL": makeOp(
            function CALL(state) {
                var args = state.frame.stack.pop();
                var f = state.frame.stack.pop();
                if (args.type !== "table" || f.type !== "function") {
                    throw (
                        "Can't call " + valueToString(f) +
                        " as a function (passing " + valueToString(args) + ")"
                    );
                }
                state.frame = newStackFrame(f, state.frame);
                state.frame.stack.push(args);
            },
            2,
            1
        ),
        "CEIL": makeOp(
            function CEIL(state) {
                var x = state.frame.stack.pop();
                if (x.type !== "number") {
                    throw (
                        "Cannot apply CEIL to " + valueToString(x) +
                        "; a number is required"
                    );
                }
                state.frame.stack.push(newNumber(-Math.floor(-x.v)));
            },
            1,
            1
        ),
        "DROP": makeOp(
            function DROP(state) {
                state.frame.stack.pop();
            },
            1,
            0
        ),
        "DROPTABLE": makeOp(
            function DROPTABLE(state) {
                var value = state.frame.stack.pop();
                if (!isEmptyTable(value)) {
                    throw (
                        "A function can be called as a subroutine only if " +
                        "it returns [] (the empty table)"
                    );
                }
            },
            1,
            0
        ),
        "DUMP": makeOp(
            function DUMP(state) {
                var value = state.frame.stack.pop();
                if (value.type === "string") {
                    console.log("DUMP: " + value.v);
                } else {
                    console.log("DUMP: " + valueToLongString(value));
                }
            },
            1,
            0
        ),
        "END": makeOp(
            function END(state) {
                throw "END";
            },
            0,
            0
        ),
        "FALSE": makeOp(
            function FALSE(state) {
                state.frame.stack.push(VALUE_FALSE);
            },
            0,
            1
        ),
        "FLOOR": makeOp(
            function FLOOR(state) {
                var x = state.frame.stack.pop();
                if (x.type !== "number") {
                    throw (
                        "Cannot apply FLOOR to " + valueToString(x) +
                        "; a number is required"
                    );
                }
                state.frame.stack.push(newNumber(Math.floor(x.v)));
            },
            1,
            1
        ),
        "GET": makeOp(
            function GET(state) {
                var k = state.frame.stack.pop();
                var t = state.frame.stack.pop();
                var result;
                if (t.type === "string") {
                    if (k.type !== "number") {
                        throw (
                            "Cannot subscript " + valueToString(t) +
                            " by " + valueToString(k)
                        );
                    }
                    if (k.v % 1 !== 0) {
                        throw (
                            "String subscript must be an integer, not " +
                            valueToString(k)
                        );
                    }
                    result = newString(t.v.substring(k.v, k.v + 1));
                } else if (t.type === "table") {
                    result = tableGet(t, k);
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
                state.frame.stack.push(result);
            },
            2,
            1
        ),
        "LEN": makeOp(
            function LEN(state) {
                var x = state.frame.stack.pop();
                if (x.type === "string") {
                    state.frame.stack.push(newNumber(x.v.length));
                } else if (x.type === "table") {
                    state.frame.stack.push(newNumber(tableSize(x)));
                } else {
                    throw "Cannot apply LEN to " + valueToString(x);
                }
            },
            1,
            1
        ),
        "MAX": makeOp(
            function MAX(state) {
                var y = state.frame.stack.pop();
                var x = state.frame.stack.pop();
                var cmp = compareValues(x, y);
                state.frame.stack.push(cmp > 0 ? x : y);
            },
            2,
            1
        ),
        "MIN": makeOp(
            function MIN(state) {
                var y = state.frame.stack.pop();
                var x = state.frame.stack.pop();
                var cmp = compareValues(x, y);
                state.frame.stack.push(cmp < 0 ? x : y);
            },
            2,
            1
        ),
        "NEG": makeOp(
            function NEG(state) {
                var x = state.frame.stack.pop();
                if (x.type === "number") {
                    state.frame.stack.push(newNumber(-x.v));
                } else if (x.type === "string") {
                    // TODO: Proper unicode.
                    state.frame.stack.push(
                        newString(x.v.split("").reverse().join("")));
                } else {
                    throw "Cannot negate " + x.type;
                }
            },
            1,
            1
        ),
        "NEXT": makeOp(
            function NEXT(state) {
                state.frame.loop.next(state);
            },
            0,
            0
        ),
        "NOT": makeOp(
            function NOT(state) {
                var x = state.frame.stack.pop();
                if (x.type === "boolean") {
                    state.frame.stack.push(x.v ? VALUE_FALSE : VALUE_TRUE);
                } else {
                    throw (
                        "Cannot apply NOT to " + x.type +
                        "; a boolean is required"
                    );
                }
            },
            1,
            1
        ),
        "OR": makeOp(
            function OR(state) {
                var y = state.frame.stack.pop();
                var x = state.frame.stack.pop();
                if (x.type !== "boolean" || y.type !== "boolean") {
                    throw (
                        "Cannot apply OR to " + valueToString(x) + " and " +
                        valueToString(y) + "; two booleans are required"
                    );
                }
                var result = x.v || y.v;
                state.frame.stack.push(result ? VALUE_TRUE : VALUE_FALSE);
            },
            2,
            1
        ),
        "PUT": makeOp(
            function PUT(state) {
                var stack = state.frame.stack;
                var v = stack.pop();
                var k = stack.pop();
                var t = stack.pop();
                stack.push(tablePut(t, k, v));
            },
            3,
            1
        ),
        "RETURN": makeOp(
            function RETURN(state) {
                var result = state.frame.stack.pop();
                state.frame = state.frame.caller;
                state.frame.stack.push(result);
            },
            1,
            0
        ),
        "ROUND": makeOp(
            function ROUND(state) {
                var x = state.frame.stack.pop();
                if (x.type !== "number") {
                    throw (
                        "Cannot apply ROUND to " + valueToString(x) +
                        "; a number is required"
                    );
                }
                state.frame.stack.push(newNumber(Math.round(x.v)));
            },
            1,
            1
        ),
        "TABLE": makeOp(
            function TABLE(state) {
                state.frame.stack.push(newTable());
            },
            0,
            1
        ),
        "TRUE": makeOp(
            function TRUE(state) {
                state.frame.stack.push(VALUE_TRUE);
            },
            0,
            1
        ),
        "WAIT": makeOp(
            function WAIT(state) {
                throw "WAIT";
            },
            0,
            0
        ),
        "WHILE": makeOp(
            function WHILE(state) {
                var cond = state.frame.stack.pop();
                if (cond.type !== "boolean") {
                    throw (
                        "Cannot execute WHILE " + cond.type +
                        "; a boolean is required"
                    );
                }
                if (cond.v === false) {
                    state.frame.pc = state.frame.loop.elsePC;
                    state.frame.loop = state.frame.loop.enclosing;
                }
            },
            1,
            0
        ),
        "WINDOW": makeOp(
            function WINDOW(state) {
                state.frame.stack.push(state.window);
            },
            0,
            1
        ),
        "XOR": makeOp(
            function XOR(state) {
                var y = state.frame.stack.pop();
                var x = state.frame.stack.pop();
                if (x.type !== "boolean" || y.type !== "boolean") {
                    throw (
                        "Cannot apply XOR to " + valueToString(x) + " and " +
                        valueToString(y) + "; two booleans are required"
                    );
                }
                var result = x.v ^ y.v;
                state.frame.stack.push(result ? VALUE_TRUE : VALUE_FALSE);
            },
            2,
            1
        )
    };

    BREAK = function BREAK(numLoops) {
        return makeOp(
            function BREAK(state) {
                var i = 1;
                while (i < numLoops) {
                    state.frame.loop = state.frame.loop.enclosing;
                    i++;
                }
                state.frame.pc = state.frame.loop.breakPC;
                state.frame.loop = state.frame.loop.enclosing;
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
            function CONSTANT(state) {
                state.frame.stack.push(value);
            },
            0,
            1
        );
    };

    /** The beginning of a "FOR ... NEXT ... ELSE" structure. */
    FOR = function FOR(loopPC, elsePC, breakPC) {
        return makeOp(
            function FOR(state) {
                function NEXT(state) {
                    var loopState = state.frame.loop;
                    if (loopState.it === null) {
                        // Exit the loop.
                        state.frame.loop = loopState.enclosing;
                        state.frame.pc = elsePC;
                    } else {
                        // Execute the body of the loop.
                        state.frame.stack.push(loopState.it.key);
                        state.frame.stack.push(loopState.it.value);
                        loopState.it = loopState.it.next();
                        state.frame.pc = loopPC;
                    }
                }
                var loopState = {
                    "loopPC": loopPC,
                    "elsePC": elsePC,
                    "breakPC": breakPC,
                    "enclosing": state.frame.loop,
                    "next": NEXT,
                    "it": null  // Set below.
                };
                var t = state.frame.stack.pop();
                if (t.type === "number") {
                    var d = t.v;
                    var n = d | 0;
                    if (n !== d) {
                        throw d + " is not an integer";
                    }
                    var range = newTable();
                    for (var i=0; i < n; i++) {
                        var v = newNumber(i);
                        range = tablePut(range, v, v);
                    }
                    loopState.it = tableIterator(range);
                } else if (t.type === "string") {
                    var s = t.v;
                    var range = newTable();
                    for (var i=0; i < s.length; i++) {
                        var k = newNumber(i);
                        var v = newString(s.substring(i, i+1));
                        range = tablePut(range, k, v);
                    }
                    loopState.it = tableIterator(range);
                } else if (t.type === "table") {
                    loopState.it = tableIterator(t);
                } else {
                    throw "Can't iterate through " + valueToString(t);
                }
                state.frame.loop = loopState;
                NEXT(state);
            },
            1,
            2
        );
    };

    GOTO = function GOTO(targetPC) {
        return makeOp(
            function GOTO(state) {
                state.frame.pc = targetPC;
            },
            0,
            0
        );
    };

    IF = function IF(targetPC) {
        return makeOp(
            function IF(state) {
                var cond = state.frame.stack.pop();
                if (cond.type !== "boolean") {
                    throw "IF requires a boolean, not '" + cond.type + "'";
                }
                if (cond.v === false) {
                    state.frame.pc = targetPC;
                }
            },
            1,
            0
        );
    };

    LLOAD = function LLOAD(index, name) {
        return makeOp(
            function LLOAD(state) {
                state.frame.stack.push(
                    state.frame.locals[index]);
            },
            0,
            1
        );
    };

    LOAD = function LOAD(index, name) {
        return makeOp(
            function LOAD(state) {
                state.frame.stack.push(state.globals[index]);
            },
            0,
            1
        );
    };

    /** The beginning of a "LOOP ... WHILE ... NEXT ... ELSE" structure. */
    LOOP = function LOOP(loopPC, elsePC, breakPC) {
        return makeOp(
            function LOOP(state) {
                state.frame.loop = {
                    "loopPC": loopPC,
                    "elsePC": elsePC,
                    "breakPC": breakPC,
                    "enclosing": state.frame.loop,
                    "next": function NEXT(state) {
                        state.frame.pc = loopPC;
                    }
                };
            },
            0,
            0
        );
    };

    LSTORE = function LSTORE(index, name) {
        return makeOp(
            function LSTORE(state) {
                state.frame.locals[index] = state.frame.stack.pop();
            },
            1,
            0
        );
    };

    SET = function SET(name) {
        return makeOp(
            function SET(state) {
                var v = state.frame.stack.pop();
                var o = state.frame.stack.pop();
                if (typeof o !== "object") {
                    throw (
                        "Cannot apply SET to " + o.type +
                        "; an object is required"
                    );
                }
                var old = o.v[name];
                if (typeof old !== typeof v) {
                    throw (
                        "SET " + o.objType + "." + name +
                        " = " + v.type
                    );
                }
                o.v[name] = v;
            },
            2,
            0
        );
    };

    STORE = function STORE(index, name) {
        return makeOp(
            function STORE(state) {
                state.globals[index] = state.frame.stack.pop();
            },
            1,
            0
        );
    };
})();
