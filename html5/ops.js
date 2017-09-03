var OPS;
var BREAK;
var CONSTANT;
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
                    throw "NotImplemented: table addition";  // TODO
                } else {
                    throw "Cannot add " + x.type + " to " + y.type;
                }
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
        "GET": makeOp(
            function GET(state) {
                var k = state.frame.stack.pop();
                var t = state.frame.stack.pop();
                var ans;
                if (t.type === "string") {
                    if (k.type !== "number") {
                        throw "Cannot subscript " + t.type + " by " + k.type;
                    }
                    if (k.v % 1 !== 0) {
                        throw (
                            "String subscript must be an integer, not " +
                            k.v
                        );
                    }
                    ans = newString(t.v.substring(k.v, k.v + 1));
                } else if (t.type === "table") {
                    throw "NotImplemented: table GET"; // TODO.
                } else if (t.type === "object") {
                    if (k.type !== "string") {
                        throw "Cannot subscript " + t.type + " by " + k.type;
                    }
                    ans = t.v[k.v];
                } else {
                    throw "Cannot subscript " + t.type;
                }
                if (typeof ans === "undefined") {
                    throw (
                        valueToString(t) + "[" + valueToString(k) + "]" +
                        " is not defined"
                    );
                }
                state.frame.stack.push(ans);
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
