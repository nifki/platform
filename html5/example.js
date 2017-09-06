"use strict";

var TEST_CODE = (
    // Setup
    'TABLE 0 "frog" PUT 1 "pig" PUT STORE(x) ;\n' +
    'TABLE 0 "sheep" PUT 2 "goose" PUT STORE(y) ;\n' +

    // TRUE
    'TABLE TABLE == DUMP ;\n' +
    'LOAD(x) LOAD(x) == DUMP ;\n' +
    'LOAD(y) LOAD(y) == DUMP ;\n' +
    'TABLE LOAD(x) != DUMP ;\n' +
    'LOAD(x) TABLE != DUMP ;\n' +
    'TABLE LOAD(y) != DUMP ;\n' +
    'LOAD(y) TABLE != DUMP ;\n' +
    'LOAD(x) LOAD(y) != DUMP ;\n' +
    'LOAD(y) LOAD(x) != DUMP ;\n' +

    '"" DUMP ; \n' +

    // FALSE
    'LOAD(x) LOAD(x) != DUMP ;\n' +
    'LOAD(y) LOAD(y) != DUMP ;\n' +
    'TABLE LOAD(x) == DUMP ;\n' +
    'LOAD(x) TABLE == DUMP ;\n' +
    'TABLE LOAD(y) == DUMP ;\n' +
    'LOAD(y) TABLE == DUMP ;\n' +
    'LOAD(x) LOAD(y) == DUMP ;\n' +
    'LOAD(y) LOAD(x) == DUMP ;\n' +
    ''
);
