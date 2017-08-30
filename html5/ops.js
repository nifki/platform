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
                    throw "Cannot add "+x+" to "+y;
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
                throw "NotImplemented: SET";
            },
            2,
            0
        );
    }
})();
