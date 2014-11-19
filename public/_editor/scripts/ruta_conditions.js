"ruta_conditions": {
  "section": {
    "-id": "ugr.tools.ruta.language.conditions",
    "title": "Conditions",
    "section": [
      {
        "-id": "ugr.tools.ruta.language.conditions.after",
        "title": "AFTER",
        "para": "
      The AFTER condition evaluates true, if the matched annotation
      starts after the beginning of an arbitrary annotation of the passed
      type. If a list of types is passed, this has to be true for at least
      one of them.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "AFTER(Type|TypeListExpression)" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "CW{AFTER(SW)};"
              },
              "
        Here, the rule matches on a capitalized word, if there is any
        small written word previously.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.and",
        "title": "AND",
        "para": "
      The AND condition is a composed condition and evaluates true, if
      all contained conditions evaluate true.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "AND(Condition1,...,ConditionN)" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Paragraph{AND(PARTOF(Headline),CONTAINS(Keyword))
          ->MARK(ImportantHeadline)};"
              },
              "
        In this example, a paragraph is annotated with an
        ImportantHeadline annotation, if it is part of a Headline and
        contains a Keyword annotation.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.before",
        "title": "BEFORE",
        "para": "
      The BEFORE condition evaluates true, if the matched annotation
      starts before the beginning of an arbitrary annotation of the passed
      type. If a list of types is passed, this has to be true for at least
      one of them.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "BEFORE(Type|TypeListExpression)" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "CW{BEFORE(SW)};"
              },
              "
        Here, the rule matches on a capitalized word, if there is any
        small written word afterwards.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.contains",
        "title": "CONTAINS",
        "para": "
      The CONTAINS condition evaluates true on a matched annotation,
      if
      the frequency of the passed type lies within an optionally passed
      interval. The limits of the passed interval are per default
      interpreted as absolute numeral values. By passing a further boolean
      parameter set to true the limits are interpreted as percental
      values.
      If no interval parameters are passed at all, then the condition
      checks
      whether the matched annotation contains at least one
      occurrence of the
      passed type.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "CONTAINS(Type(,NumberExpression,NumberExpression(,BooleanExpression)?)?)" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Paragraph{CONTAINS(Keyword)->MARK(KeywordParagraph)};"
              },
              "
        A Paragraph is annotated with a KeywordParagraph annotation, if
        it contains a Keyword annotation.
      ",
              {
                "programlisting": "Paragraph{CONTAINS(Keyword,2,4)->MARK(KeywordParagraph)};"
              },
              "
        A Paragraph is annotated with a KeywordParagraph annotation, if
        it contains between two and four Keyword annotations.
      ",
              {
                "programlisting": "Paragraph{CONTAINS(Keyword,50,100,true)->MARK(KeywordParagraph)};"
              },
              {
                "#text": [
                  "
        A Paragraph is annotated with a KeywordParagraph annotation, if it
        contains between 50% and 100% Keyword annotations. This is
        calculated based on the tokens of the Paragraph. If the Paragraph
        contains six basic annotations (see
        ",
                  "), two of them are part of one Keyword annotation, and if one basic
        annotation is also annotated with a Keyword annotation, then the
        percentage of the contained Keywords is 50%.
      "
                ],
                "xref": { "-linkend": "ugr.tools.ruta.language.seeding" }
              }
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.contextcount",
        "title": "CONTEXTCOUNT",
        "para": "
      The CONTEXTCOUNT condition numbers all occurrences of the
      matched type within the context of a passed type's annotation
      consecutively, thus assigning an index to each occurrence.
      Additionally it stores the index of the matched annotation in a
      numerical variable if one is passed. The condition evaluates true if
      the index of the matched annotation is within a passed interval. If
      no interval is passed, the condition always evaluates true.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "CONTEXTCOUNT(Type(,NumberExpression,NumberExpression)?(,Variable)?)" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Keyword{CONTEXTCOUNT(Paragraph,2,3,var)
          ->MARK(SecondOrThirdKeywordInParagraph)};"
              },
              "
        Here, the position of the matched Keyword annotation within a
        Paragraph annotation is calculated and stored in the variable 'var'.
        If the counted value lies within the interval [2,3], then the matched
        Keyword is annotated with the SecondOrThirdKeywordInParagraph
        annotation.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.count",
        "title": "COUNT",
        "para": "
      The COUNT condition can be used in two different ways. In the
      first case (see first definition), it counts the number of
      annotations of the passed type within the window of the matched
      annotation and stores the amount in a numerical variable, if such a
      variable is passed. The condition evaluates true if the counted
      amount is within a specified interval. If no interval is passed, the
      condition always evaluates true. In the second case (see second
      definition), it counts the number of occurrences of the passed
      VariableExpression (second parameter) within the passed list (first
      parameter) and stores the amount in a numerical variable, if such a
      variable is passed. Again, the condition evaluates true if the counted
      amount is within a specified interval. If no interval is passed, the
      condition always evaluates true.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": [
              { "programlisting": "COUNT(Type(,NumberExpression,NumberExpression)?(,NumberVariable)?)" },
              {
                "programlisting": "COUNT(ListExpression,VariableExpression
          (,NumberExpression,NumberExpression)?(,NumberVariable)?)"
              }
            ]
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Paragraph{COUNT(Keyword,1,10,var)->MARK(KeywordParagraph)};"
              },
              "
        Here, the amount of Keyword annotations within a Paragraph is
        calculated and stored in the variable 'var'. If one to ten Keywords
        were counted, the paragraph is marked with a KeywordParagraph
        annotation.
      ",
              {
                "programlisting": "Paragraph{COUNT(list,\"author\",5,7,var)};"
              },
              "
        Here, the number of occurrences of STRING \"author\" within the
        STRINGLIST 'list' is counted and stored in the variable 'var'. If
        \"author\" occurs five to seven times within 'list', the condition
        evaluates true.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.currentcount",
        "title": "CURRENTCOUNT",
        "para": "
      The CURRENTCOUNT condition numbers all occurrences of the matched
      type within the whole document consecutively, thus assigning an index
      to each occurrence. Additionally, it stores the index of the matched
      annotation in a numerical variable, if one is passed. The condition
      evaluates true if the index of the matched annotation is within a
      specified interval. If no interval is passed, the condition always
      evaluates true.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "CURRENTCOUNT(Type(,NumberExpression,NumberExpression)?(,Variable)?)" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Paragraph{CURRENTCOUNT(Keyword,3,3,var)->MARK(ParagraphWithThirdKeyword)};"
              },
              "
        Here, the Paragraph, which contains the third Keyword of the
        whole document, is annotated with the ParagraphWithThirdKeyword
        annotation. The index is stored in the variable 'var'.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.endswith",
        "title": "ENDSWITH",
        "para": "
      The ENDSWITH condition evaluates true, if an annotation of the
      given type ends exactly at the same position as the matched
      annotation. If a list of types is passed, this has to be true for at
      least one of them.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "ENDSWITH(Type|TypeListExpression) " }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Paragraph{ENDSWITH(SW)};"
              },
              "
        Here, the rule matches on a Paragraph annotation, if it ends
        with a small written word.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.feature",
        "title": "FEATURE",
        "para": "
      The FEATURE condition compares a feature of the matched
      annotation with the second argument.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "FEATURE(StringExpression,Expression) " }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Document{FEATURE(\"language\",targetLanguage)}"
              },
              "
        This rule matches, if the feature named 'language' of the
        document annotation equals the value of the variable
        'targetLanguage'.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.if",
        "title": "IF",
        "para": "
      The IF condition evaluates true, if the contained boolean
      expression evaluates true.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "IF(BooleanExpression) " }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Paragraph{IF(keywordAmount > 5)->MARK(KeywordParagraph)};"
              },
              "
        A Paragraph annotation is annotated with a KeywordParagraph
        annotation, if the value of the variable 'keywordAmount' is greater
        than five.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.inlist",
        "title": "INLIST",
        "para": "
      The INLIST condition is fulfilled, if the matched annotation is listed
      in a given word or string list. If an optional agrument is given, then
      the value of the argument is used instead of the covered text of the matched annotation
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": [
              { "programlisting": "INLIST(WordList(,StringExpression)?) " },
              { "programlisting": "INLIST(StringList(,StringExpression)?) " }
            ]
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Keyword{INLIST(SpecialKeywordList)->MARK(SpecialKeyword)};"
              },
              "
        A Keyword is annotated with the type SpecialKeyword, if the text
        of the Keyword annotation is listed in the word list or string list
        SpecialKeywordList.
      ",
              {
                "programlisting": "Token{INLIST(MyLemmaList, Token.lemma)->MARK(SpecialLemma)};"
              },
              "
        This rule creates an annotation of the type SpecialLemma for each token that provides a feature value
        of the feature \"lemma\" that is present in the string list or word list MyLemmaList.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.is",
        "title": "IS",
        "para": "
      The IS condition evaluates true, if there is an annotation of the
      given type with the same beginning and ending offsets as the
      matched
      annotation. If a list of types is given, the condition
      evaluates true,
      if at least one of them fulfills the former condition.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "IS(Type|TypeListExpression) " }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Author{IS(Englishman)->MARK(EnglishAuthor)};"
              },
              "
        If an Author annotation is also annotated with an Englishman
        annotation, it is annotated with an EnglishAuthor annotation.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.last",
        "title": "LAST",
        "para": "
      The LAST condition evaluates true, if the type of the last token
      within the window of the matched annotation is of the given type.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "LAST(TypeExpression) " }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Document{LAST(CW)};"
              },
              "
        This rule fires, if the last token of the document is a
        capitalized word.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.mofn",
        "title": "MOFN",
        "para": "
      The MOFN condition is a composed condition. It evaluates true if
      the number of containing conditions evaluating true is within a given
      interval.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "MOFN(NumberExpression,NumberExpression,Condition1,...,ConditionN) " }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Paragraph{MOFN(1,1,PARTOF(Headline),CONTAINS(Keyword))
          ->MARK(HeadlineXORKeywords)};"
              },
              "
        A Paragraph is marked as a HeadlineXORKeywords, if the matched
        text is either part of a Headline annotation or contains Keyword
        annotations.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.near",
        "title": "NEAR",
        "para": "
      The NEAR condition is fulfilled, if the distance of the matched
      annotation to an annotation of the given type is within a given
      interval. The direction is defined by a boolean parameter, whose
      default value is set to true, therefore searching forward. By default this
      condition works on an unfiltered index. An optional fifth boolean
      parameter can be set to true to get the condition being evaluated on
      a filtered index.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": {
              "programlisting": "NEAR(TypeExpression,NumberExpression,NumberExpression
          (,BooleanExpression(,BooleanExpression)?)?) "
            }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Paragraph{NEAR(Headline,0,10,false)->MARK(NoHeadline)};"
              },
              "
        A Paragraph that starts at most ten tokens after a Headline
        annotation is annotated with the NoHeadline annotation.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.not",
        "title": "NOT",
        "para": "
      The NOT condition negates the result of its contained
      condition.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "\"-\"Condition" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Paragraph{-PARTOF(Headline)->MARK(Headline)};"
              },
              "
        A Paragraph that is not part of a Headline annotation so far is
        annotated with a Headline annotation.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.or",
        "title": "OR",
        "para": "
      The OR Condition is a composed condition and evaluates true, if
      at least one contained condition is evaluated true.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "OR(Condition1,...,ConditionN)" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Paragraph{OR(PARTOF(Headline),CONTAINS(Keyword))
                                           ->MARK(ImportantParagraph)};"
              },
              "
        In this example a Paragraph is annotated with the
        ImportantParagraph annotation, if it is a Headline or contains
        Keyword annotations.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.parse",
        "title": "PARSE",
        "para": "
      The PARSE condition is fulfilled, if the text covered by the
      matched annotation can be transformed into a value of the given
      variable's type. If this is possible, the parsed value is
      additionally assigned to the passed variable.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "PARSE(variable)" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "NUM{PARSE(var)};"
              },
              "
        If the variable 'var' is of an appropriate numeric type, the
        value of NUM is parsed and subsequently stored in 'var'.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.partof",
        "title": "PARTOF",
        "para": "
      The PARTOF condition is fulfilled, if the matched annotation is
      part of an annotation of the given type. However, it is not necessary
      that the matched annotation is smaller than the annotation of the
      given type. Use the (much slower) PARTOFNEQ condition instead, if this
      is needed. If a type list is given, the condition evaluates true, if
      the former described condition for a single type is fulfilled for at
      least one of the types in the list.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "PARTOF(Type|TypeListExpression)" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Paragraph{PARTOF(Headline) -> MARK(ImportantParagraph)};"
              },
              "
        A Paragraph is an ImportantParagraph, if the matched text is
        part of a Headline annotation.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.partofneq",
        "title": "PARTOFNEQ",
        "para": "
      The PARTOFNEQ condition is fulfilled if the matched annotation
      is part of (smaller than and inside of) an annotation of the given
      type. If also annotations of the same size should be acceptable, use
      the PARTOF condition. If a type list is given, the condition
      evaluates true if the former described condition is fulfilled for at
      least one of the types in the list.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "PARTOFNEQ(Type|TypeListExpression)" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "W{PARTOFNEQ(Headline) -> MARK(ImportantWord)};"
              },
              {
                "#text": [
                  "
        A word is an ",
                  ", if it is part of a headline.
      "
                ],
                "quote": "ImportantWord"
              }
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.position",
        "title": "POSITION",
        "para": "
      The POSITION condition is fulfilled, if the matched type is the
      k-th occurence of this type within the window of an annotation of the
      passed type, whereby k is defined by the value of the passed
      NumberExpression. If the additional boolean paramter is set to false,
      then k counts the occurences of of the minimal annotations.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "POSITION(Type,NumberExpression(,BooleanExpression)?)" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Keyword{POSITION(Paragraph,2)->MARK(SecondKeyword)};"
              },
              "
        The second Keyword in a Paragraph is annotated with the type
        SecondKeyword.
      ",
              {
                "programlisting": "Keyword{POSITION(Paragraph,2,false)->MARK(SecondKeyword)};"
              },
              "
        A Keyword in a Paragraph is annotated with the type
        SecondKeyword, if it starts at the same offset as the second
        (visible) RutaBasic annotation, which normally corresponds to
        the tokens.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.regexp",
        "title": "REGEXP",
        "para": {
          "#text": [
            "
      The REGEXP condition is fulfilled, if the given pattern matches on the
      matched annotation. However, if a string variable is given as the
      first
      argument, then the pattern is evaluated on the value of the
      variable.
      For more details on the syntax of regular
      expressions, take a
      look at
      the
      ",
            "
      . By default the REGEXP condition is case-sensitive. To change this,
      add an optional boolean parameter, which is set to true. The regular expression is
      initialized with the flags DOTALL and MULTILINE, and if the optional parameter is set to true,
      then additionally with the flags CASE_INSENSITIVE and UNICODE_CASE.
    "
          ],
          "ulink": {
            "-url": "http://docs.oracle.com/javase/1.4.2/docs/api/java/util/regex/Pattern.html",
            "#text": "Java API"
          }
        },
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "REGEXP((StringVariable,)? StringExpression(,BooleanExpression)?)" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Keyword{REGEXP(\"..\")->MARK(SmallKeyword)};"
              },
              "
        A Keyword that only consists of two chars is annotated with a
        SmallKeyword annotation.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.score",
        "title": "SCORE",
        "para": "
      The SCORE condition evaluates the heuristic score of the matched
      annotation. This score is set or changed by the MARK action.
      The
      condition is fulfilled, if the score of the matched annotation is
      in a
      given interval. Optionally, the score can be stored in a
      variable.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "SCORE(NumberExpression,NumberExpression(,Variable)?)" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "MaybeHeadline{SCORE(40,100)->MARK(Headline)};"
              },
              "
        An annotation of the type MaybeHeadline is annotated with
        Headline, if its score is between 40 and 100.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.size",
        "title": "SIZE",
        "para": "
      The SIZE contition counts the number of elements in the given
      list. By default, this condition always evaluates true. When an interval
      is passed, it evaluates true, if the counted number of list elements
      is within the interval. The counted number can be stored in an
      optionally passed numeral variable.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "SIZE(ListExpression(,NumberExpression,NumberExpression)?(,Variable)?)" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Document{SIZE(list,4,10,var)};"
              },
              {
                "#text": [
                  "
        This rule fires, if the given list contains between 4 and 10
        elements. Additionally, the exact amount is stored in the variable
        ",
                  ".
      "
                ],
                "quote": "var"
              }
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.startswith",
        "title": "STARTSWITH",
        "para": "
      The STARTSWITH condition evaluates true, if an annotation of the
      given type starts exactly at the same position as the matched
      annotation. If a type list is given, the condition evaluates true, if
      the former is true for at least one of the given types in the list.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "STARTSWITH(Type|TypeListExpression)" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Paragraph{STARTSWITH(SW)};"
              },
              "
        Here, the rule matches on a Paragraph annotation, if it starts
        with small written word.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.totalcount",
        "title": "TOTALCOUNT",
        "para": "
      The TOTALCOUNT condition counts the annotations of the passed
      type within the whole document and stores the amount in an optionally
      passed numerical variable. The condition evaluates true, if the
      amount
      is within the passed interval. If no interval is passed, the
      condition always evaluates true.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "TOTALCOUNT(Type(,NumberExpression,NumberExpression(,Variable)?)?)" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Paragraph{TOTALCOUNT(Keyword,1,10,var)->MARK(KeywordParagraph)};"
              },
              "
        Here, the amount of Keyword annotations within the whole
        document is calculated and stored in the variable 'var'. If one to
        ten Keywords were counted, the Paragraph is marked with a
        KeywordParagraph annotation.
      "
            ]
          }
        ]
      },
      {
        "-id": "ugr.tools.ruta.language.conditions.vote",
        "title": "VOTE",
        "para": "
      The VOTE condition counts the annotations of the given two types
      within the window of the matched annotation and evaluates true,
      if it
      finds more annotations of the first type.
    ",
        "section": [
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Definition:"
              }
            },
            "para": { "programlisting": "VOTE(TypeExpression,TypeExpression)" }
          },
          {
            "title": {
              "emphasis": {
                "-role": "bold",
                "#text": "Example:"
              }
            },
            "para": [
              {
                "programlisting": "Paragraph{VOTE(FirstName,LastName)};"
              },
              "
        Here, this rule fires, if a paragraph contains more firstnames
        than lastnames.
      "
            ]
          }
        ]
      }
    ]
  }
}
