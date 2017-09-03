var TEST_CODE = (
    '"At top level" DUMP ;\n' +
    '0 STORE(n) ;\n' +
    'LOAD(f) TABLE CALL DROPTABLE ;\n' +
    'LOAD(f) TABLE CALL DROPTABLE ;\n' +
    '"At top level again" DUMP ;\n' +
    'LOAD(n) DUMP ;\n' +
    ';\n' +
    'DEF(f) DROP ;\n' +
    '"In f()" DUMP ;\n' +
    'LOAD(n) 1 + STORE(n) ;\n' +
    'TABLE RETURN'
);
