var TEST_CODE = (
    '"At top level" DUMP ;\n' +
    '0 STORE(n) ;\n' +
    'LOAD(f) TABLE CALL DROP ;\n' +
    'LOAD(f) TABLE CALL DROP ;\n' +
    '"At top level again" DUMP ;\n' +
    'LOAD(n) DUMP ;\n' +
    ';\n' +
    'DEF(f) DROP ;\n' +
    '"In f()" DUMP ;\n' +
    'LOAD(n) 1 + STORE(n) ;\n' +
    '0 RETURN'
);
