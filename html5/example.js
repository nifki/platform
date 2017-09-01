var TEST_CODE = (
    'LOOP ; WINDOW "R" GET 1.0 < WHILE ;\n' +
    '  WINDOW WINDOW "R" GET 0.01 + SET(R) ;\n' +
    '  WAIT ;\n' +
    'NEXT ;\n' +
    'ELSE ;\n' +
    '"DONE" DUMP ;'
);
