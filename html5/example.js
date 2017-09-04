"use strict";

var TEST_CODE = (
    '0 1 MAX DUMP ;\n' +
    '1 0 MAX DUMP ;\n' +
    '0 "a" MAX DUMP ;\n' +
    '"a" 0 MAX DUMP ;\n' +
    '0 TABLE MAX DUMP ;\n' +
    'TABLE 0 MAX DUMP ;\n' +
    '"" DUMP ;\n' +
    '0 1 MIN DUMP ;\n' +
    '1 0 MIN DUMP ;\n' +
    '0 "1" MIN DUMP ;\n' +
    '"1" 0 MIN DUMP ;\n' +
    '0 TABLE MIN DUMP ;\n' +
    'TABLE 0 MIN DUMP ;\n' +
    ''
);
