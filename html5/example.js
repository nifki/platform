"use strict";

var TEST_CODE = (
    '2 3 * DUMP ;\n' +
    '"Hello" 2 * DUMP ;\n' +
    '2 "Hello" * DUMP ;\n' +
    '"Hello" 0 * DUMP ;\n' +
    '0 "Hello" * DUMP ;\n' +
    '"foo" "bar" * DUMP ;\n' +  // illegal
    ''
);
