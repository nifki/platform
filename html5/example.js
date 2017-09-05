"use strict";

var TEST_CODE = (
    '3 5 * DUMP ;\n' +
    '"xy" 5 * DUMP ;\n' +
    '5 "xy" * DUMP ;\n' +
    '"xy" 0 * DUMP ;\n' +
    '"xy" 1 * DUMP ;\n' +
    '"xy" 2 * DUMP ;\n' +
    '"xy" 3 * DUMP ;\n' +
    '"xy" 4 * DUMP ;\n' +
    '"xy" 5 * DUMP ;\n' +
    '"xy" 6 * DUMP ;\n' +
    '"xy" 7 * DUMP ;\n' +
    '"xy" 8 * DUMP ;\n' +
    '"foo" "bar" * DUMP ;\n' +  // illegal
    ''
);
