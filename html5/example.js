"use strict";

var TEST_CODE = (
    'TABLE 0 "frog" PUT 1 "pig" PUT LSTORE(x) ;\n' +
    'LLOAD(x) 0 DGET DUMP DUMP DUMP ;\n' +
    //'LLOAD(x) 2 DGET DROP DROP DROP\n' +  // not defined
    '5 0 DGET DROP DROP DROP ;\n' +
    ''
);
