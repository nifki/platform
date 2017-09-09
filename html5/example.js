var TEST_CODE = (
    'LOAD(manPNG) SPRITE STORE(man) ;\n' +
    'LOAD(man) DUMP ;\n' +
    'LOAD(man) "Picture" GET DUMP ;\n' +
    'LOAD(man) 0.5 SET(X) ;\n' +
    'LOAD(man) 0.5 SET(Y) ;\n' +
    'LOAD(man) TRUE SET(IsVisible) ;\n' +
    'WAIT ;\n' +
    '\n' +
    'LOOP TRUE WHILE ;\n' +
        '0 LSTORE(i) ;\n' +
        'KEYS FOR LSTORE(v) LSTORE(k) ;\n' +
            'LLOAD(v) IF ;\n' +
                'LLOAD(k) "LeftArrow" == IF ;\n' +
                    'LOAD(man) LOAD(man) "X" GET 0.01 - SET(X) ;\n' +
                'THEN ; ELSE ; \n' +
                'LLOAD(k) "RightArrow" == IF ;\n' +
                    'LOAD(man) LOAD(man) "X" GET 0.01 + SET(X) ;\n' +
                'THEN ; ELSE ; \n' +
                'LLOAD(k) "UpArrow" == IF ;\n' +
                    'LOAD(man) LOAD(man) "Y" GET 0.01 - SET(Y) ;\n' +
                'THEN ; ELSE ; \n' +
                'LLOAD(k) "DownArrow" == IF ;\n' +
                    'LOAD(man) LOAD(man) "Y" GET 0.01 + SET(Y) ;\n' +
                'THEN ; ELSE ; \n' +
                'LLOAD(k) "Space" == IF ;\n' +
                    'CLS ;\n' +
                    '"CLS" DUMP ;\n' +
                'THEN ; ELSE ; \n' +
                'LLOAD(k) "LetterS" == IF ;\n' +
                    'LOAD(man) TRUE SET(IsVisible) ;\n' +
                    '"SHOW" DUMP ;\n' +
                'THEN ; ELSE ; \n' +
            'THEN ; ELSE ;\n' +
        'NEXT ;\n' +
        'ELSE ;\n' +
        'WAIT ;\n' +
    'NEXT ;\n' +
    'ELSE ;\n' +
    ''
);
