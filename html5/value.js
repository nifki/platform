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
var VALUE_EMPTY_TABLE = {"type": "table", "v": {"keys": [], "values": []}};

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

function newTable() {
    return VALUE_EMPTY_TABLE;
}

function isEmptyTable(value) {
    return value === VALUE_EMPTY_TABLE;
}

function arrayFind(array, element) {
    if (array == null) {
        throw '"this" is null or not defined';
    }

    var obj = Object(array);
    var len = obj.length >>> 0;
    if (len === 0) {
        return -1;
    }

    for (var k=0; k < len; k++) {
        if (obj[k] === element) {  // We don't need to support NaN or {}.
            return k;
        }
    }

    return -1;
}

function tablePut(table, key, value) {
    if (TYPE_INDEX[key.type] > 5) {
        throw "'" + valueToString(key) + "' cannot be used as key in a table";
    }
    if (table.type !== "table") {
        throw "'" + valueToString(table) + "' is not a table";
    }
    var newKeys = table.v.keys.slice();
    var newValues = table.v.values.slice();
    // TODO: Insertion sort here?
    var i = arrayFind(table.v, key);
    if (i === -1) {
        newKeys.push(key);
        newValues.push(value);
    } else {
        newValues[i] = value;
    }
    return {"type": "table", "v": {"keys": newKeys, "values": newValues}};
}

function valueToString(value) {
    var v = value.v;
    if (value.type === "boolean") {
        return v ? "TRUE" : "FALSE";
    } else if (value.type === "number") {
        return "" + v;
    } else if (value.type === "string") {
        // TODO: SSString.encode equivalent.
        return v;
    } else if (value.type === "table") {
        return "TABLE(" + v.keys.length + " keys)";
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
    var i, result, sep, keys, key;
    if (value.type === "table") {
        // TODO: Sort order?
        result = "[";
        sep = "";
        keys = v.keys;
        var values = v.values;
        for (i=0; i < keys.length; i++) {
            key = keys[i];
            var value = values[i];
            result += sep + valueToString(key) + "=" + valueToString(value);
            sep = ", ";
        }
        return result + "]";
    } else if (value.type === "object") {
        // TODO: Sort order?
        result = valueToString(value) + "(";
        sep = "";
        keys = Object.getOwnPropertyNames(v);
        keys.sort();
        for (i=0; i < keys.length; i++) {
            key = keys[i];
            result += sep + key + "=" + valueToString(v[key]);
            sep = ", ";
        }
        return result + ")";
    } else {
        return valueToString(value);
    }
}
