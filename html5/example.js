"use strict";

var TEST_CODE = (
    'TABLE 3 "?" PUT 1 "one" PUT TABLE 2 "two" PUT 3 "three" PUT + DUMP ;\n' +
    '3 2 - DUMP ;\n' +
    '"hello" 2 - DUMP ;\n' +
    '2 "hello" - DUMP ;\n' +
    'TABLE 3 "?" PUT 1 "one" PUT TABLE 2 "two" PUT 3 "three" PUT - DUMP ;\n' +
    ''
);
