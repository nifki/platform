var OPS;
var CONSTANT;
var LOAD;
var STORE;
var LLOAD;
var LSTORE;
var SET;

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
        "WAIT": makeOp(
            function WAIT(state) {
                throw "WAIT";
            },
            0,
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
    }

    LOAD = function LOAD(index, name) {
        return makeOp(
            function LOAD(state) {
                state.frame.stack.push(state.globals[index]);
            },
            0,
            1
        );
    }

    STORE = function STORE(index, name) {
        return makeOp(
            function STORE(state) {
                state.globals[index] = state.frame.stack.pop();
            },
            1,
            0
        );
    }

    LLOAD = function LLOAD(index, name) {
        return makeOp(
            function LLOAD(state) {
                state.frame.stack.push(
                    state.frame.locals[index]);
            },
            0,
            1
        );
    }

    LSTORE = function LSTORE(index, name) {
        return makeOp(
            function LSTORE(state) {
                state.frame.locals[index] = state.frame.stack.pop();
            },
            1,
            0
        );
    }

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
    }
})();
