var TEST_CODE = (
    '"------ boolean, boolean" DUMP ;\n' +
    '"==" DUMP ;  FALSE TRUE == NOT DUMP ;\n' +
    '"!=" DUMP ;  FALSE TRUE != DUMP ;\n' +
    '"<>" DUMP ;  FALSE TRUE <> DUMP ;\n' +
    '"< " DUMP ;  FALSE TRUE <  DUMP ;\n' +
    '"<=" DUMP ;  FALSE TRUE <= DUMP ;\n' +
    '"> " DUMP ;  FALSE TRUE >  NOT DUMP ;\n' +
    '">=" DUMP ;  FALSE TRUE >= NOT DUMP ;\n' +
    '"" DUMP ;\n' +
    '"==" DUMP ;  FALSE FALSE == DUMP ;\n' +
    '"!=" DUMP ;  FALSE FALSE != NOT DUMP ;\n' +
    '"<>" DUMP ;  FALSE FALSE <> NOT DUMP ;\n' +
    '"< " DUMP ;  FALSE FALSE <  NOT DUMP ;\n' +
    '"<=" DUMP ;  FALSE FALSE <= DUMP ;\n' +
    '"> " DUMP ;  FALSE FALSE >  NOT DUMP ;\n' +
    '">=" DUMP ;  FALSE FALSE >= DUMP ;\n' +
    '"" DUMP ;\n' +
    '"==" DUMP ;  TRUE TRUE == DUMP ;\n' +
    '"!=" DUMP ;  TRUE TRUE != NOT DUMP ;\n' +
    '"<>" DUMP ;  TRUE TRUE <> NOT DUMP ;\n' +
    '"< " DUMP ;  TRUE TRUE <  NOT DUMP ;\n' +
    '"<=" DUMP ;  TRUE TRUE <= DUMP ;\n' +
    '"> " DUMP ;  TRUE TRUE >  NOT DUMP ;\n' +
    '">=" DUMP ;  TRUE TRUE >= DUMP ;\n' +
    '"------ boolean, number" DUMP ;\n' +
    '"==" DUMP ;  FALSE 1 == NOT DUMP ;\n' +
    '"!=" DUMP ;  FALSE 1 != DUMP ;\n' +
    '"<>" DUMP ;  FALSE 1 <> DUMP ;\n' +
    '"< " DUMP ;  FALSE 1 <  DUMP ;\n' +
    '"<=" DUMP ;  FALSE 1 <= DUMP ;\n' +
    '"> " DUMP ;  FALSE 1 >  NOT DUMP ;\n' +
    '">=" DUMP ;  FALSE 1 >= NOT DUMP ;\n' +
    '"------ number, boolean" DUMP ;\n' +
    '"==" DUMP ;  1 FALSE == NOT DUMP ;\n' +
    '"!=" DUMP ;  1 FALSE != DUMP ;\n' +
    '"<>" DUMP ;  1 FALSE <> DUMP ;\n' +
    '"< " DUMP ;  1 FALSE <  NOT DUMP ;\n' +
    '"<=" DUMP ;  1 FALSE <= NOT DUMP ;\n' +
    '"> " DUMP ;  1 FALSE >  DUMP ;\n' +
    '">=" DUMP ;  1 FALSE >= DUMP ;\n' +
    '"------ number, number" DUMP ;\n' +
    '"==" DUMP ;  0 1 == NOT DUMP ;\n' +
    '"!=" DUMP ;  0 1 != DUMP ;\n' +
    '"<>" DUMP ;  0 1 <> DUMP ;\n' +
    '"< " DUMP ;  0 1 <  DUMP ;\n' +
    '"<=" DUMP ;  0 1 <= DUMP ;\n' +
    '"> " DUMP ;  0 1 >  NOT DUMP ;\n' +
    '">=" DUMP ;  0 1 >= NOT DUMP ;\n' +
    '"" DUMP ;\n' +
    '"==" DUMP ;  0 0 == DUMP ;\n' +
    '"!=" DUMP ;  0 0 != NOT DUMP ;\n' +
    '"<>" DUMP ;  0 0 <> NOT DUMP ;\n' +
    '"< " DUMP ;  0 0 <  NOT DUMP ;\n' +
    '"<=" DUMP ;  0 0 <= DUMP ;\n' +
    '"> " DUMP ;  0 0 >  NOT DUMP ;\n' +
    '">=" DUMP ;  0 0 >= DUMP ;\n' +
    '"------ string, string" DUMP ;\n' +
    '"==" DUMP ;  "a" "aa" == NOT DUMP ;\n' +
    '"!=" DUMP ;  "a" "aa" != DUMP ;\n' +
    '"<>" DUMP ;  "a" "aa" <> DUMP ;\n' +
    '"< " DUMP ;  "a" "aa" <  DUMP ;\n' +
    '"<=" DUMP ;  "a" "aa" <= DUMP ;\n' +
    '"> " DUMP ;  "a" "aa" >  NOT DUMP ;\n' +
    '">=" DUMP ;  "a" "aa" >= NOT DUMP ;\n' +
    '"------ number, string" DUMP ;\n' +
    '"==" DUMP ;  0 "1" == NOT DUMP ;\n' +
    '"!=" DUMP ;  0 "1" != DUMP ;\n' +
    '"<>" DUMP ;  0 "1" <> DUMP ;\n' +
    '"< " DUMP ;  0 "1" <  DUMP ;\n' +
    '"<=" DUMP ;  0 "1" <= DUMP ;\n' +
    '"> " DUMP ;  0 "1" >  NOT DUMP ;\n' +
    '">=" DUMP ;  0 "1" >= NOT DUMP ;\n' +
    '"------ string, number" DUMP ;\n' +
    '"==" DUMP ;  "1" 0 == NOT DUMP ;\n' +
    '"!=" DUMP ;  "1" 0 != DUMP ;\n' +
    '"<>" DUMP ;  "1" 0 <> DUMP ;\n' +
    '"< " DUMP ;  "1" 0 <  NOT DUMP ;\n' +
    '"<=" DUMP ;  "1" 0 <= NOT DUMP ;\n' +
    '"> " DUMP ;  "1" 0 >  DUMP ;\n' +
    '">=" DUMP ;  "1" 0 >= DUMP ;\n' +
    ''
);
