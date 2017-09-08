var TEST_CODE = (
    'LOOP TRUE WHILE ;\n' +
        '0 LSTORE(i) ;\n' +
        'TABLE LSTORE(pressed) ;\n' +
        'KEYS FOR LSTORE(v) LSTORE(k) ;\n' +
            'LLOAD(v) IF ;\n' +
                'LLOAD(pressed) LLOAD(i) LLOAD(k) PUT LSTORE(pressed) ;\n' +
                'LLOAD(i) 1 + LSTORE(i) ;\n' +
            'THEN ; ELSE ;\n' +
        'NEXT ;\n' +
        'ELSE ;\n' +
    'LLOAD(pressed) DUMP ;\n' +
    'WAIT ;\n' +
    'NEXT ;\n' +
    'ELSE ;\n' +
    ''
);
