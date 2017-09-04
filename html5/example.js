"use strict";

var TEST_CODE = (
    '"abc" LEN DUMP ;\n' +
    'TABLE LEN DUMP ;\n' +
    'TABLE 0 "A" PUT 1 "B" PUT 2 "C" PUT LEN DUMP ;\n' +
    '1 LEN DUMP ;\n' +  // illegal
    ''
);
