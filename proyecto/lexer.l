%{
#include <stdio.h>
#include <string.h>
#include "parser.tab.h" 

void yyerror(const char *s);
int yylex();
%}

%option noyywrap
%option yylineno

%%

"//".* { /* Ignorar comentarios */ }
[ \t\n\r]               { /* Ignorar espacios y saltos de Windows */ }

"var"                   { return KW_VAR; }
"int"                   { return TYPE_INT; }
"float"                 { return TYPE_FLOAT; }
"bool"                  { return TYPE_BOOL; }
"string"                { return TYPE_STRING; }
"void"                  { return TYPE_VOID; }
"array"                 { return TYPE_ARRAY; }

"func"                  { return KW_FUNC; }
"return"                { return KW_RETURN; }
"if"                    { return KW_IF; }
"while"                 { return KW_WHILE; }
"for"                   { return KW_FOR; }

"new_array"             { return KW_NEW_ARRAY; }
"push"                  { return KW_PUSH; }
"length"                { return KW_LENGTH; }

"print"                 { return KW_PRINT; }
"draw"                  { return KW_DRAW; }
"read"                  { return KW_READ; }
"input"                 { return KW_INPUT; }

">="                    { return GTE; }
"<="                    { return LTE; }
"=="                    { return EQ; }
"!="                    { return NEQ; }
"<"                     { return LT; }
">"                     { return GT; }
"="                     { return ASSIGN; }

"+"                     { return PLUS; }
"-"                     { return MINUS; }
"*"                     { return MULT; }
"/"                     { return DIV; }
"%"                     { return MOD; }
"^"                     { return POW; }

"("                     { return LPAREN; }
")"                     { return RPAREN; }
"{"                     { return LBRACE; }
"}"                     { return RBRACE; }
"["                     { return LBRACKET; }
"]"                     { return RBRACKET; }
";"                     { return SEMICOLON; }
","                     { return COMMA; }

[0-9]+                  { yylval.sval = strdup(yytext); return INT_LIT; }
[0-9]+\.[0-9]+          { yylval.sval = strdup(yytext); return FLOAT_LIT; }
\"[^"]*\"               { yylval.sval = strdup(yytext); return STRING_LIT; }
[a-zA-Z_][a-zA-Z0-9_]* { yylval.sval = strdup(yytext); return ID; }

.                       { printf("Error lexico en linea %d: Caracter '%s' no reconocido\n", yylineno, yytext); }

%%