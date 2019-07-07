grammar P10a;

file											locals [ ClassSymbol defn ]
	:	NL* 
		'class' qualifiedName NL+
	  ( extendDecl NL+ )?
	  ( interfaceDecl NL+ )*
	  ( member NL+ )*
		EOF
	;
	
extendDecl										locals [ ClassSymbol defn ]
	:	'extends' qualifiedName
	;
	
interfaceDecl									locals [ ClassSymbol defn ]
	:	'interface' qualifiedName
	;

member
	:	importDecl
	|	fieldDecl
	|	methodDecl
	;

importDecl										locals [ ClassSymbol defn ]
	:	'import' qualifiedName
	;

qualifiedName
	:	Identifier ( '.' Identifier )*
	;
	
fieldDecl										locals [ FieldSymbol defn ]
	:	priv='#'? Identifier stat='static'? type ( init=':' NL* expression )?
	;

type											locals [ Type tipe ]
	:	simpleType ( '[' ']' )?
	;

simpleType										locals [ Type tipe ]
	:	'boolean'								# booleanType	
	|	'byte'									# byteType	
	|	'char'									# charType	
	|	'double'								# doubleType	
	|	'float'									# floatType	
	|	'int'									# intType	
	|	'long'									# longType	
	|	'short'									# shortType	
	|	Identifier								# objectType	
	;

methodDecl										locals [ MethodSymbol defn ]
	:	priv='#'? Identifier stat='static'? '(' NL* parameterList? ')' type? body
	;
	
parameterList
	:	parameter ( ',' NL* parameter )*
	;
	
parameter										locals [ FieldSymbol defn ]
	:	Identifier type
	;

body
	:	'native'
	|	   	block
		'.' 
	;

statement
	:	'if' expression
			block
	  ( 'elsif' expression
			block )*
	  ( 'else'
			block )?
	    '.'										# ifStmt
	|   'while' expression
			block
		'.'										# whileStmt
	|   'for' Identifier type ':' NL* expression
			block
		'.'										# forInStmt
	|	'for' statement ( 'while' expression )? ( 'next' statement )? 
			block
		'.'										# forWhileStmt
	|   'switch' expression NL+
	  	switchCase*
	  ( 'else' 
	        block )?
		'.'										# switchStmt
	|	'done'									# doneStmt
	|	'next'									# nextStmt
	|   'return' expression?					# returnStmt
	|   fieldDecl								# fieldStmt
	|	expression								# exprStmt
	;

switchCase
	:	'case' caseExp ( ',' NL* caseExp )* 
	        block
	;

caseExp
	:	Integer									# integerCase
	|	Character								# characterCase
	//LATER expression
	;

block
	:	NL+
	  ( statement NL+ )*
	;

// Operator alternatives are listed in descending precedence order
expression												locals [ Symbol defn ]
	:	primary											# primExpr		// (defn)
	|	expression '.' Identifier						# memberExpr	// (defn)
	|	expression '[' NL* expression ']'				# indexExpr
	|	expression '(' NL* argumentList? ')'			# callExpr
	|	( '+' | '-' ) NL* expression					# minusExpr
	|	( '~' | '!' ) NL* expression					# notExpr
		// dyadic operators in descending precedence
	|	expression ( '*' | '/' | '%' ) NL* expression	# multiplyExpr
	|	expression ( '+' | '-' ) NL* expression			# addExpr
	|	expression ( '<<' | '>>' | '>>>' ) NL* expression	# shiftExpr
	|	expression '&' NL* expression					# bitAndExpr
	|	expression '^' NL* expression					# bitExclExpr
	|	expression '|' NL* expression					# bitOrExpr
	|	expression 'as' NL* type						# castExpr
	|	expression ( '=' | '!=' | '≠' | '<=' | '≤'
				   | '>=' | '≥'| '<' | '>'
				   ) NL* expression						# compareExpr
	|	expression '&&' NL* expression					# andThenExpr
	|	expression '||' NL* expression					# orElseExpr
	|	<assoc=right> expression (
							':'
						|   '+:' | '-:' | '*:' | '/:' | '%:'
						|   '&:' | '|:' | '^:'
						|   '>>:' |  '>>>:' | '<<:'
						) NL* expression				# assignExpr
    ;
// primary could be combined into expression    
primary											locals [ Scope refScope, Symbol defn ]
	:	'(' NL* expression ')'					# parenPrim
	|	'this'									# thisPrim			// (defn)
	|	'super'									# superPrim
	|	Integer									# integerPrim
	|	Float									# floatPrim
	|	Character								# characterPrim
	|	String									# stringPrim
	|	'null'									# nullPrim
	|	'true'									# truePrim
	|	'false'									# falsePrim
	|	Identifier								# idPrim			// (defn)
	;

argumentList
	:	expression ( ',' NL* expression )*
	;

importFile
	:	NL* 
	  ( importExtend NL+ )?
	  ( importInterface NL+ )*
	  ( importMember NL+ )*
		EOF
	;

importExtend	
	:	extendDecl 
	;

importInterface
	:	interfaceDecl
	;

importMember
	:	importDecl																# importImport
	|	priv='#'? Identifier stat='static'? type								# importField
	|	priv='#'? Identifier stat='static'? '(' NL* parameterList? ')' type?	# importMethod
	;

////////// Lexical section

//TODO Unicode Identifier standard http://www.unicode.org/reports/tr31/
Identifier
	:	IdentifierHead IdentifierChar*
	;

fragment IdentifierHead
	:	[a-zA-Z]
	|	'_'
	|	'\u00A8' | '\u00AA' | '\u00AD' | '\u00AF' | [\u00B2-\u00B5] | [\u00B7-\u00BA]
	|	[\u00BC-\u00BE] | [\u00C0-\u00D6] | [\u00D8-\u00F6] | [\u00F8-\u00FF]
	|	[\u0100-\u02FF] | [\u0370-\u167F] | [\u1681-\u180D] | [\u180F-\u1DBF]
	|	[\u1E00-\u1FFF]
	|	[\u200B-\u200D] | [\u202A-\u202E] | [\u203F-\u2040] | '\u2054' | [\u2060-\u206F]
	|	[\u2070-\u20CF] | [\u2100-\u218F] | [\u2460-\u24FF] | [\u2776-\u2793]
	|	[\u2C00-\u2DFF] | [\u2E80-\u2FFF]
	|	[\u3004-\u3007] | [\u3021-\u302F] | [\u3031-\u303F] | [\u3040-\uD7FF]
	|	[\uF900-\uFD3D] | [\uFD40-\uFDCF] | [\uFDF0-\uFE1F] | [\uFE30-\uFE44]
	|	[\uFE47-\uFFFD]
/*
	|	U+10000–U+1FFFD | U+20000–U+2FFFD | U+30000–U+3FFFD | U+40000–U+4FFFD
	|	U+50000–U+5FFFD | U+60000–U+6FFFD | U+70000–U+7FFFD | U+80000–U+8FFFD
	|	U+90000–U+9FFFD | U+A0000–U+AFFFD | U+B0000–U+BFFFD | U+C0000–U+CFFFD
	|	U+D0000–U+DFFFD or U+E0000–U+EFFFD
*/
 	;

fragment IdentifierChar
	:	[0-9]
	|	[\u0300-\u036F] | [\u1DC0-\u1DFF] | [\u20D0-\u20FF] | [\uFE20-\uFE2F]
	|	IdentifierHead
	;

// Numeric literal syntax must be compatible with LLVM constants
Integer
	:	'-'? Decimal Long?
	|	'0' [xX] HexDigit+ Long?
	;
	
fragment Long
	:	[lL]
	;
	
Float
	:	'-'? Decimal '.' [0-9]* ( [eE] [-+]? Decimal )? Double?
	|	'0' [xX] HexDigit+ '.' HexDigit* ( [pP] [-+]? Decimal )? Double?
	;
	
fragment Double
	:	[dD]
	;
	
fragment Decimal
	:	[1-9] [0-9]*
	|	'0'
	// 	Leading zeroes not permitted, to avoid confusion with C octal convention
	;

fragment HexDigit
	:	[0-9a-fA-F]
	;

Character
	:	'`' .	// single Unicode code point
	;

String
	:	'"' StringChar* '"'
	|	'\'' StringChar* '\''
	;

fragment StringChar
	:	~["\'\\]
	|	'\\' [btnfr"\'\\]
	|	'\\x' HexDigit HexDigit 
	|	'\\u' HexDigit HexDigit HexDigit HexDigit 
	|	'\\U' HexDigit HexDigit HexDigit HexDigit HexDigit HexDigit HexDigit HexDigit
	;

Space
	:   [ \t\r]+ -> skip
	;

Comment
	:	'/*' (Comment|.)*? '*/'	 -> skip	// nesting comments allowed
	;

NL
	:	'//' .*? '\n' 
	|	'\n'
	|	';'
	;
