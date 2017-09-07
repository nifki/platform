var TEST_CODE = (
    '10 STORE(count) ;\n' +
    'LOOP ;\n' +
    'LOAD(count) 0 > WHILE ;\n' +
    '"foo" DUMP ;\n' +
    'LOAD(count) DUMP ;\n' +
    'LOAD(count) 1 - STORE(count) ;\n' +
    'NEXT ;\n' +
    'ELSE ;\n' +
    ''
);
