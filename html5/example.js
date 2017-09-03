var TEST_CODE = (
    // [0=A, 1=B, 2=C, 3=D]
    'TABLE 0 "A" PUT 1 "B" PUT 2 "C" PUT 3 "D" PUT DUMP ;\n'+
    'TABLE 0 "A" PUT 1 "B" PUT 3 "D" PUT 2 "C" PUT DUMP ;\n'+
    'TABLE 0 "A" PUT 2 "C" PUT 1 "B" PUT 3 "D" PUT DUMP ;\n'+
    'TABLE 0 "A" PUT 2 "C" PUT 3 "D" PUT 1 "B" PUT DUMP ;\n'+
    'TABLE 0 "A" PUT 3 "D" PUT 1 "B" PUT 2 "C" PUT DUMP ;\n'+
    'TABLE 0 "A" PUT 3 "D" PUT 2 "C" PUT 1 "B" PUT DUMP ;\n'+
    'TABLE 1 "B" PUT 0 "A" PUT 2 "C" PUT 3 "D" PUT DUMP ;\n'+
    'TABLE 1 "B" PUT 0 "A" PUT 3 "D" PUT 2 "C" PUT DUMP ;\n'+
    'TABLE 1 "B" PUT 2 "C" PUT 0 "A" PUT 3 "D" PUT DUMP ;\n'+
    'TABLE 1 "B" PUT 2 "C" PUT 3 "D" PUT 0 "A" PUT DUMP ;\n'+
    'TABLE 1 "B" PUT 3 "D" PUT 0 "A" PUT 2 "C" PUT DUMP ;\n'+
    'TABLE 1 "B" PUT 3 "D" PUT 2 "C" PUT 0 "A" PUT DUMP ;\n'+
    'TABLE 2 "C" PUT 0 "A" PUT 1 "B" PUT 3 "D" PUT DUMP ;\n'+
    'TABLE 2 "C" PUT 0 "A" PUT 3 "D" PUT 1 "B" PUT DUMP ;\n'+
    'TABLE 2 "C" PUT 1 "B" PUT 0 "A" PUT 3 "D" PUT DUMP ;\n'+
    'TABLE 2 "C" PUT 1 "B" PUT 3 "D" PUT 0 "A" PUT DUMP ;\n'+
    'TABLE 2 "C" PUT 3 "D" PUT 0 "A" PUT 1 "B" PUT DUMP ;\n'+
    'TABLE 2 "C" PUT 3 "D" PUT 1 "B" PUT 0 "A" PUT DUMP ;\n'+
    'TABLE 3 "D" PUT 0 "A" PUT 1 "B" PUT 2 "C" PUT DUMP ;\n'+
    'TABLE 3 "D" PUT 0 "A" PUT 2 "C" PUT 1 "B" PUT DUMP ;\n'+
    'TABLE 3 "D" PUT 1 "B" PUT 0 "A" PUT 2 "C" PUT DUMP ;\n'+
    'TABLE 3 "D" PUT 1 "B" PUT 2 "C" PUT 0 "A" PUT DUMP ;\n'+
    'TABLE 3 "D" PUT 2 "C" PUT 0 "A" PUT 1 "B" PUT DUMP ;\n'+
    'TABLE 3 "D" PUT 2 "C" PUT 1 "B" PUT 0 "A" PUT DUMP ;\n'+
    // [A=0, B=1, C=2, D=3]
    'TABLE "A" 0 PUT "B" 1 PUT "C" 2 PUT "D" 3 PUT DUMP ;\n'+
    'TABLE "A" 0 PUT "B" 1 PUT "D" 3 PUT "C" 2 PUT DUMP ;\n'+
    'TABLE "A" 0 PUT "C" 2 PUT "B" 1 PUT "D" 3 PUT DUMP ;\n'+
    'TABLE "A" 0 PUT "C" 2 PUT "D" 3 PUT "B" 1 PUT DUMP ;\n'+
    'TABLE "A" 0 PUT "D" 3 PUT "B" 1 PUT "C" 2 PUT DUMP ;\n'+
    'TABLE "A" 0 PUT "D" 3 PUT "C" 2 PUT "B" 1 PUT DUMP ;\n'+
    'TABLE "B" 1 PUT "A" 0 PUT "C" 2 PUT "D" 3 PUT DUMP ;\n'+
    'TABLE "B" 1 PUT "A" 0 PUT "D" 3 PUT "C" 2 PUT DUMP ;\n'+
    'TABLE "B" 1 PUT "C" 2 PUT "A" 0 PUT "D" 3 PUT DUMP ;\n'+
    'TABLE "B" 1 PUT "C" 2 PUT "D" 3 PUT "A" 0 PUT DUMP ;\n'+
    'TABLE "B" 1 PUT "D" 3 PUT "A" 0 PUT "C" 2 PUT DUMP ;\n'+
    'TABLE "B" 1 PUT "D" 3 PUT "C" 2 PUT "A" 0 PUT DUMP ;\n'+
    'TABLE "C" 2 PUT "A" 0 PUT "B" 1 PUT "D" 3 PUT DUMP ;\n'+
    'TABLE "C" 2 PUT "A" 0 PUT "D" 3 PUT "B" 1 PUT DUMP ;\n'+
    'TABLE "C" 2 PUT "B" 1 PUT "A" 0 PUT "D" 3 PUT DUMP ;\n'+
    'TABLE "C" 2 PUT "B" 1 PUT "D" 3 PUT "A" 0 PUT DUMP ;\n'+
    'TABLE "C" 2 PUT "D" 3 PUT "A" 0 PUT "B" 1 PUT DUMP ;\n'+
    'TABLE "C" 2 PUT "D" 3 PUT "B" 1 PUT "A" 0 PUT DUMP ;\n'+
    'TABLE "D" 3 PUT "A" 0 PUT "B" 1 PUT "C" 2 PUT DUMP ;\n'+
    'TABLE "D" 3 PUT "A" 0 PUT "C" 2 PUT "B" 1 PUT DUMP ;\n'+
    'TABLE "D" 3 PUT "B" 1 PUT "A" 0 PUT "C" 2 PUT DUMP ;\n'+
    'TABLE "D" 3 PUT "B" 1 PUT "C" 2 PUT "A" 0 PUT DUMP ;\n'+
    'TABLE "D" 3 PUT "C" 2 PUT "A" 0 PUT "B" 1 PUT DUMP ;\n'+
    'TABLE "D" 3 PUT "C" 2 PUT "B" 1 PUT "A" 0 PUT DUMP ;\n'+
    // WINDOW:0(B=0, G=0, H=256, R=0, W=256, X=0, Y=0)
    'WINDOW DUMP'
);
