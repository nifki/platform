"use strict";

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

/** Returns negative, zero or positive indicating x < y, x == y, x > y
 * respectively, or throws if x and y are not comparable.
 *
 * If x or y is a function or object, throws an exception. Otherwise,
 * compares their types by their order in TYPE_INDEX. If equal,
 * distinguishes cases according to type:
 * <ul>
 * <li> FALSE is less than TRUE.
 * <li> numbers have the obvious ordering.
 * <li> strings are ordered ASCII-betically.
 * <li> tables are ordered pointwise, considering undefined values to be
 * smaller than defined values. Keys are considered in sorted order. If
 * any of the values is not comparable, then the tables are not
 * comparable.
 * <li> pictures are ordered ASCII-betically on their original variable
 * names (including the page name and underscore).
 * </ul>
 */
// TODO: Test this thoroughly.
function compareValues(x, y) {
    var xTypeIndex = TYPE_INDEX[x.type];
    var yTypeIndex = TYPE_INDEX[y.type];
    if (xTypeIndex > 5) throw x.type + " is not a comparable type";
    if (yTypeIndex > 5) throw y.type + " is not a comparable type";
    if (xTypeIndex !== yTypeIndex) return xTypeIndex - yTypeIndex;
    if (xTypeIndex < 4) {
        // JavaScript's semantics for boolean/number/string match ours.
        if (x.v < y.v) return -1;
        if (y.v < x.v) return 1;
        return 0;
    }
    if (xTypeIndex === 5) {
        return compareValues(x.v.originalName, y.v.originalName);
    }
    var xKeys = x.v.keys, xValues = x.v.values;
    var yKeys = y.v.keys, yValues = y.v.values;
    var it1 = 0;
    var it2 = 0;
    while (true) {
        if (it1 >= xKeys.length) {
            return it2 >= yKeys.length ? 0 : -1;
        }
        if (it2 >= yKeys.length) {
            return 1;
        }
        var result = -compareValues(xKeys[it1], yKeys[it2]);
        if (result !== 0) {
            return result;
        }
        result = compareValues(xValues[it1], yValues[it2]);
        if (result !== 0) {
            return result;
        }
        it1++;
        it2++;
    }
}

// Helper: Returns the point at which to insert/overwrite the element.
function sortedArrayFind(array, element) {
    var len = array.length;
    var start = 0, end = len;
    while (start < end) {
        var i = (start + end) >> 1;
        if (compareValues(array[i], element) < 0) {
            start = i + 1;
        } else {
            end = i;
        }
    }
    return start;
}

function tablePut(table, key, value) {
    if (TYPE_INDEX[key.type] > 5) {
        throw "'" + valueToString(key) + "' cannot be used as key in a table";
    }
    if (table.type !== "table") {
        throw "'" + valueToString(table) + "' is not a table";
    }
    var keys = table.v.keys;
    var newKeys = keys.slice();
    var newValues = table.v.values.slice();
    // Insert/overwrite preserving sorted order.
    var i = sortedArrayFind(keys, key);
    if (i < keys.length && compareValues(keys[i], key) === 0) {
        newKeys[i] = key;
        newValues[i] = value;
    } else {
        // Insert key and value at position `i`.
        newKeys.splice(i, 0, key);
        newValues.splice(i, 0, value);
    }
    return {"type": "table", "v": {"keys": newKeys, "values": newValues}};
}

function tableGet(table, key) {
    var keys = table.v.keys;
    var i = sortedArrayFind(keys, key);
    if (i < keys.length && compareValues(keys[i], key) === 0) {
        return table.v.values[i];
    }
    return null;
}

function tableIterator(table) {
    var keys = table.v.keys;
    if (keys.length === 0) {
        return null;
    }
    var values = table.v.values;
    var iterItem = {
        "key": keys[0],
        "value": values[0],
        "next": null  // Set below.
    };
    var i = 0;
    function tableIteratorNext() {
        i++;
        if (i >= keys.length) {
            return null;
        }
        iterItem.key = keys[i];
        iterItem.value = values[i];
        return iterItem;
    }
    iterItem.next = tableIteratorNext;
    return iterItem;
}

function tableSize(table) {
    return table.v.keys.length;
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
            result += (
                sep + valueToString(key) + "=" + valueToString(values[i]));
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
