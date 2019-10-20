grammar ICSS;

//--- LEXER: ---
// IF support:
IF: 'if';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';

// STYLE ATTRIBUTES
STYLE_ATTRIBUTES: 'background-color' | 'width' | 'color' | 'height';
//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;

//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';

//--- PARSER: ---
//--- Level 1 Meta: ---
stylesheet: (variableassignment|stylerule)* EOF;

//--- Level 2 Meta: ---
variableassignment:variablereference ASSIGNMENT_OPERATOR expression SEMICOLON;
stylerule: selector OPEN_BRACE (variableassignment|declaration|if_statement)+ CLOSE_BRACE;

//--- Level 3 Meta: ---
expression: literal #literalexpression|
            variablereference #varref|
            expression MUL expression #multiplyOperation|
            expression MIN expression #substractOperation|
            expression PLUS expression #addOperation;


variablereference: CAPITAL_IDENT;

selector:   ID_IDENT #selectorId|
            CLASS_IDENT #selectorClass|
            LOWER_IDENT #selectorTag;

declaration:    propertyName COLON (expression+) SEMICOLON |
                variableassignment;
if_statement: IF BOX_BRACKET_OPEN (variablereference|boolliteral) BOX_BRACKET_CLOSE OPEN_BRACE (declaration|if_statement)+ CLOSE_BRACE;

////--- Level 4 Meta: ---
//operation:(addoperation | multiplyoperation | subtractoperation) | operation PLUS operation | operation MIN operation | operation MUL operation;
literal: scalarliteral|pixelliteral|percentageliteral|colorliteral| boolliteral;
propertyName: STYLE_ATTRIBUTES;

////--- Level 5 Meta ---
//addoperation: literal PLUS literal | variablereference PLUS variablereference | variablereference PLUS literal | literal PLUS variablereference| literal PLUS addoperation | addoperation PLUS literal | addoperation PLUS variablereference;
//multiplyoperation: literal MUL literal | variablereference MUL variablereference | variablereference MUL literal | literal MUL variablereference| literal MUL addoperation | addoperation MUL literal | addoperation MUL variablereference;
//subtractoperation: literal MIN literal | variablereference MIN variablereference | variablereference MIN literal | literal MIN variablereference| literal MIN addoperation | addoperation MIN literal | addoperation MIN variablereference;


//--- LITERALS: ---
scalarliteral: SCALAR;
pixelliteral:PIXELSIZE;
percentageliteral:PERCENTAGE;
colorliteral: COLOR;
boolliteral: TRUE|FALSE;

