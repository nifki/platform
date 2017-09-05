"use strict";

var TEST_CODE = (
    '"abcde" 0 GET DUMP ;\n' +
    '"abcde" 1 GET DUMP ;\n' +
    '"abcde" 2 GET DUMP ;\n' +
    '"abcde" 3 GET DUMP ;\n' +
    '"abcde" 4 GET DUMP ;\n' +
    '"abcde" 3.9999999999999999999 GET DUMP ;\n' +  // barely legal
    //'"abcde" 5 GET DUMP ;\n' +  // illegal
    //'"abcde" 1 NEG GET DUMP ;\n' +  // illegal
    //'"abcde" "x" GET DUMP ;\n' +  // illegal
    //'"abcde" 4.5 GET DUMP ;\n' +  // illegal
    ''
);
