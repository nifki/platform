var TEST_CODE = (
    'TABLE "D" 3 PUT "C" 2 PUT "B" 1 PUT "A" 0 PUT\n' +
    'FOR DUMP DUMP NEXT "Doing ELSE" DUMP ELSE DROP ; "DONE" DUMP ;\n' +
    '\n' +
    'TABLE "D" 3 PUT "C" 2 PUT "B" 1 PUT "A" 0 PUT\n' +
    'FOR DUMP DUMP BREAK NEXT "Doing ELSE" DUMP ELSE DROP ; "DONE" DUMP ;' +
    '\n' +
    '"abc"\n' +
    'FOR DUMP DUMP NEXT "Doing ELSE" DUMP ELSE DROP ; "DONE" DUMP ;\n' +
    '\n' +
    '"abc"\n' +
    'FOR DUMP DUMP BREAK NEXT "Doing ELSE" DUMP ELSE DROP ; "DONE" DUMP ;' +
    '\n' +
    '3\n' +
    'FOR DUMP DUMP NEXT "Doing ELSE" DUMP ELSE DROP ; "DONE" DUMP ;\n' +
    '\n' +
    '3\n' +
    'FOR DUMP DUMP BREAK NEXT "Doing ELSE" DUMP ELSE DROP ; "DONE" DUMP ;'
);
