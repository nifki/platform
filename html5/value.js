/*
 * Nifki values are Javascript objects with a "type" field of type String and
 * a "v" field whose type depends on the type. It is the equivalent of the
 * Java type "org.sc3d.apt.crazon.vm.state.Value". The allowed values for the
 * "type" field are exactly as returned by "describeType()" in the Java
 * version, e.g. "number", "string".
 */

function newNumber(v) {
    if (typeof v !== "number" || !isFinite(v))
        throw "IllegalArgumentException: " + v;
    return {"type": "number", "v": v};
}

function newString(v) {
    if (typeof v !== "string")
        throw "IllegalArgumentException: " + v;
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
