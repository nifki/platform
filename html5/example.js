"use strict";

var TEST_CODE = (
//    '"foo" STORE(x) ;\n' +  // makes LOAD(x) valid
//    'LOAD(x) DROP ;\n' +  // invalid without prior STORE(x)
//    '"bar" LSTORE(x) ;\n' +  // makes LLOAD(x) valie
//    'LLOAD(x) DROP ;\n' +  // invalid without prior LSTORE(x)

    // Setup
    'TABLE 0 "frog" PUT 1 "pig" PUT LSTORE(x) ;\n' +

    // CONTAINS => TRUE
    '"Hello" 2 CONTAINS DUMP ;\n' +
    '"Hello" 2 GET DROP ;\n' +
    '"Hello" 4 CONTAINS DUMP ;\n' +
    '"Hello" 4 GET DROP ;\n' +
    'LLOAD(x) 0 CONTAINS DUMP ;\n' +
    'LLOAD(x) 0 GET DROP ;\n' +
    'WINDOW "X" CONTAINS DUMP ;\n' +
    'WINDOW "X" GET DROP ;\n' +

    '"" DUMP ;\n' +

    // CONTAINS => FALSE (the GETs are illegal)
    '"Hello" 1 NEG CONTAINS DUMP ;\n' +
    //'"Hello" 1 NEG GET DROP ;\n' +
    '"Hello" 5 CONTAINS DUMP ;\n' +
    //'"Hello" 5 GET DROP ;\n' +
    '"Hello" TABLE CONTAINS DUMP ;\n' +
    //'"Hello" TABLE GET DROP ;\n' +
    'LLOAD(x) LLOAD(x) CONTAINS DUMP ;\n' +
    //'LLOAD(x) LLOAD(x) GET DROP ;\n' +
    'WINDOW "Z" CONTAINS DUMP ;\n' +
    //'WINDOW "Z" GET DROP ;\n' +
    'WINDOW TRUE CONTAINS DUMP ;\n' +
    //'WINDOW TRUE GET DROP ;\n' +
    ''
);
