"use strict";

var TEST_CODE = (
    'FALSE FALSE XOR DUMP ;\n' +
    'FALSE TRUE XOR DUMP ;\n' +
    'TRUE FALSE XOR DUMP ;\n' +
    'TRUE TRUE XOR DUMP ;\n' +
    '"foo" "bar" XOR DUMP ;\n' +  // illegal
    ''
);
