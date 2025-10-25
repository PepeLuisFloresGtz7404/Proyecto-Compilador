%{
/*
 * SECCIÓN 1: DEFINICIONES
 * Incluimos el archivo de cabecera que Bison generará (parser.tab.h).
 * Este archivo contiene las definiciones de los tokens (NUMERO, ID, etc.)
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "parser.tab.h" // Archivo generado por Bison

/* Para compatibilidad con la unión yylval */
extern YYSTYPE yylval;
%}

DIGITO    [0-9]
LETRA     [a-zA-Z]
ID        {LETRA}({LETRA}|{DIGITO})*
NUMERO    {DIGITO}+(\.{DIGITO}+)?
OPERADOR  [+\-*/]

%%
{NUMERO}    { yylval.num = atof(yytext); return NUMERO; }
{ID}        { yylval.id = strdup(yytext); return ID; }
{OPERADOR}  { /* dejamos esto por compatibilidad si se usa; pero preferimos literales */ return yytext[0]; }

[ \t]       { /* Ignorar */ }
"="         { return ASIGNAR ; }
\n          { return '\n'; }
.           { return yytext[0]; } // Retorna cualquier otro carácter tal cual

%%

int yywrap() {
    return 1;
}