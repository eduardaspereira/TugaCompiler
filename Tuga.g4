grammar Tuga;

program: varDecl* stmt+ EOF;

varDecl: ID (VIRGULA ID)* DOISPONTOS type PONTOVIRGULA;

type: INTEIRO | REAL | BOLEANO | STR;

stmt:
    ESCREVE expr PONTOVIRGULA                  # printStmt
    | ID ATRIBUICAO expr PONTOVIRGULA         # assignStmt
    | INICIO stmt* FIM                        # blockStmt
    | ENQUANTO LEFT_PAREN expr RIGHT_PAREN stmt      # whileStmt
    | SE LEFT_PAREN expr RIGHT_PAREN stmt (SENAO stmt)? # ifStmt
    | PONTOVIRGULA                            # emptyStmt;

expr:
    ID                                  # varExpr
    | NUMBER                            # numberExpr
    | STRING                            # stringExpr
    | VERDADEIRO                        # trueExpr
    | FALSO                             # falseExpr
    | expr MAIS expr                    # addExpr
    | expr MENOS expr                   # subExpr
    | expr ASTERISCO expr               # mulExpr
    | expr BARRA expr                   # divExpr
    | expr MOD expr                     # modExpr
    | expr IGUAL expr                   # eqExpr
    | expr DIFERENTE expr               # neqExpr
    | expr MENOR expr                   # ltExpr
    | expr MENOR_IGUAL expr             # leqExpr
    | LEFT_PAREN expr RIGHT_PAREN       # parenExpr
    | MENOS expr                        # negExpr
    | expr E expr                       # andExpr
    | expr OU expr                      # orExpr;

// Keywords
ESCREVE: 'escreve';
INICIO: 'inicio';
FIM: 'fim';
ENQUANTO: 'enquanto';
SE: 'se';
SENAO: 'senao';
INTEIRO: 'inteiro';
REAL: 'real';
BOLEANO: 'booleano';
STR: 'string';
VERDADEIRO: 'verdadeiro';
FALSO: 'falso';
E: 'e';
OU: 'ou';

// Operators
ATRIBUICAO: '<-';
MAIS: '+';
MENOS: '-';
ASTERISCO: '*';
BARRA: '/';
MOD: '%';
IGUAL: 'igual';
DIFERENTE: 'diferente';
MENOR: '<';
MENOR_IGUAL: '<=';
LEFT_PAREN: '(';
RIGHT_PAREN: ')';

// Separators
VIRGULA: ',';
DOISPONTOS: ':';
PONTOVIRGULA: ';';

// Literals
ID: [a-zA-Z_][a-zA-Z0-9_]*;
NUMBER: [0-9]+ ('.' [0-9]+)?;
STRING: '"' ~["]* '"';

// Comments
COMMENT_BLOCK: '/*' .*? '*/' -> skip;
COMMENT_STAR: '***' .*? '***' -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;

// Whitespace
WS: [ \t\r\n]+ -> skip;