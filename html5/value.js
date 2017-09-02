/*
 * Nifki values are Javascript objects with a "type" field of type String and
 * a "v" field whose type depends on the type. It is the equivalent of the
 * Java type "org.sc3d.apt.crazon.vm.state.Value". The allowed values for the
 * "type" field are exactly as returned by "describeType()" in the Java
 * version, e.g. "number", "string".
 */

var TYPE_INDEX = {
    "boolean": 1,
    "number": 2,
    "string": 3,
    "table": 4,
    "picture": 5,
    "function": 6,
    "object": 7
};

var VALUE_TRUE = {"type": "boolean", "v": true};
var VALUE_FALSE = {"type": "boolean", "v": false};

function newNumber(v) {
    if (typeof v !== "number" || !isFinite(v)) {
        throw "IllegalArgumentException: " + v;
    }
    return {"type": "number", "v": v};
}

function newString(v) {
    if (typeof v !== "string") {
        throw "IllegalArgumentException: " + v;
    }
    return {"type": "string", "v": v};
}

function newFunction(startPC, numLocals, originalName) {
    return {
        "type": "function",
        "startPC": startPC,
        "numLocals": numLocals,
        "originalName": originalName
    };
}

var newObject = (function() {
    var objCount = 0;

    function newObject(objType, v) {
        if (typeof v !== "object") {
            throw "IllegalArgumentException: " + v;
        }
        return {
            "type": "object",
            "objType": objType,
            "objNum": objCount++,
            "v": v
        };
    }

    return newObject;
})();

function valueToString(value) {
    var v = value.v;
    if (value.type === "boolean") {
        return v ? "TRUE" : "FALSE";
    } else if (value.type === "number") {
        return "" + v;
    } else if (value.type === "string") {
        // TODO: SSString.encode equivalent.
        return v;
    } else if (value.type === "function") {
        return value.originalName;
    } else if (value.type === "object") {
        return value.objType + ":" + value.objNum;
    } else {
        return "<TODO: valueToString " + value.type + ">";
    }
}

function valueToLongString(value) {
    var v = value.v;
    var ans, sep;
    if (value.type === "table") {
        return "<TODO: valueToLongString " + value.type + ">";
    } else if (value.type === "object") {
        ans = valueToString(value) + "(";
        sep = "";
        for (var key in v) {
            if (v.hasOwnProperty(key)) {
                ans += sep + key + "=" + valueToString(v[key]);
                sep = ", ";
            }
        }
        return ans + ")";
    } else {
        return valueToString(value);
    }
}
