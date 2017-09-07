"use strict";

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

function isEmptyTable(value) {
    return value === VALUE_EMPTY_TABLE;
}

function tableSize(table) {
    return table.v.keys.length;
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
