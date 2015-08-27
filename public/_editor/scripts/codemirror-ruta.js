/* Definition of a simple mode for Apache Ruta scripts */
CodeMirror.defineSimpleMode("ruta", {

  start: [
    {regex: /["'](?:[^\\]|\\.)*?["']/,
      token: "string"}, /* blue */

    {regex: /(?:PACKAGE|DECLARE|ENGINE|UIMAFIT|STRING|INT|FLOAT|DOUBLE|BOOLEAN|WORDLIST|WORDTABLE|TYPELIST|STRINGLIST|INTLIST|BOOLEANLIST)\b/,
      token: "declare"}, /* red bold */

    {regex: /(?:AFTER|AND|BEFORE|CONTAINS|CONTEXTCOUNT|COUNT|CURRENTCOUNT|ENDSWITH|FEATURE|IF|INLIST|IS|LAST|MOFN|NEAR|NOT|OR|PARSE|PARTOF|PARTOFNEQ|POSITION|REGEXP|SCORE|SIZE|STARTSWITH|TOTALCOUNT|VOTE)\b/,
      token: "conditions"}, /* light green bold */

    {regex: /(?:TAG|ADD|ADDFILTERTYPE|ADDRETAINTYPE|ASSIGN|CALL|CLEAR|COLOR|CONFIGURE|CREATE|DEL|DYNAMICANCHORING|EXEC|FILL|FILTERTYPE|GATHER|GET|GETFEATURE|GETLIST|GREEDYANCHORING|LOG|MARK|MARKFAST|MARKFIRST|MARKLAST|MARKONCE|MARKSCORE|MARKTABLE|MATCHEDTEXT|MERGE|REMOVE|REMOVEDUPLICATE|REMOVEFILTERTYPE|REMOVERETAINTYPE|REPLACE|RETAINTYPE|SETFEATURE|SHIFT|TRANSFER|TRIE|TRIM|UNMARK|UNMARKALL|ONTO)\b/,
      token: "actions"}, /* blue bold */

    {regex: /(?:Document|COLON|SW|MARKUP|PERIOD|CW|NUM|QUESTION|SPECIAL|CAP|COMMA|EXCLAMATION|SEMICOLON|NBSP|AMP|SENTENCEEND|W|PM|ANY|ALL|SPACE|BREAK)\b/,
      token: "basictype"}, /* gray */

    {regex: /\/\/.*/,
      token: "comment"},  /* green */
  ],
  comment: [
    {regex: /.*/, token: "comment"}  /* green */
  ],
  meta: {
    lineComment: "//"
  }
});
