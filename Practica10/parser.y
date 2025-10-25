%{
/*
 * SECCIoN 1: DEFINICIONES
 * Codigo C que se copia al inicio del archivo C generado.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_VARS 100

typedef struct {
    char *nombre;
    double valor;
} Variable;

Variable tabla[MAX_VARS];
int num_vars = 0;

double obtener_valor(char *id) {
    for (int i = 0; i < num_vars; i++) {
        if (strcmp(tabla[i].nombre, id) == 0)
            return tabla[i].valor;
    }
    
    tabla[num_vars].nombre = strdup(id);
    tabla[num_vars].valor = 0.0;
    num_vars++;
    return 0.0;
}

void asignar_valor(char *id, double valor) {
    for (int i = 0; i < num_vars; i++) {
        if (strcmp(tabla[i].nombre, id) == 0) {
            tabla[i].valor = valor;
            return;
        }
    }
    
    tabla[num_vars].nombre = strdup(id);
    tabla[num_vars].valor = valor;
    num_vars++;
}


/* Prototipo de la funcion de análisis lexico (creada por Flex) */
int yylex(); 

/* Funcion para reportar errores sintácticos */
void yyerror(const char *s) {
    fprintf(stderr, "Error Sintactico: %s\n", s);
}
%}

/*
 * Declaracion de "tokens" (terminales).
 * Estos son los valores que Flex nos retornara.
 * Yacc generara automaticamente un enum para ellos en y.tab.h.
 */
/* Tipos para valores semánticos */
%union {
    double num;
    char *id;
}

/* Tokens con sus tipos */
%token <num> NUMERO
%token <id> ID
%token <id> ASIGNAR

/* Precedencia y asociatividad para operadores reales (usando literales) */
%left '+' '-'
%left '*' '/'
%right UMINUS

/* El tipo del no terminal 'expresion' es num (double) */
%type <num> expresion

/*
 * Declaracion del simbolo inicial de la gramatica.
 * El analisis comenzara intentando encontrar un 'programa'.
 */
%start programa

%%
/*
 * SECCIoN 2: REGLAS GRAMATICALES
 * Aqui definimos la estructura de nuestro lenguaje.
 * La sintaxis es:
 * simbolo_no_terminal : componentes... { accion C }
 * | otros_componentes... { otra accion }
 * ;
 */

programa:
    /* Un programa puede estar vacio */
    | programa linea   /* O puede ser un programa seguido de otra linea */
    ;

linea:
    '\n'                /* Una linea puede ser solo un salto de linea */
    | expresion '\n'    { printf(">> Linea valida. Resultado: %f\n", $1); }
    | ID ASIGNAR expresion '\n'{
            asignar_valor($1, $3);
            printf(" >> Asignación válida.\n");
        }
    ;

expresion:
    NUMERO              { $$ = $1; } /* Una expresion puede ser un numero */
    | ID                  { $$ = obtener_valor($1); } /* O un identificador (valor simulado) */
    | expresion '+' expresion { $$ = $1 + $3; }
    | expresion '-' expresion { $$ = $1 - $3; }
    | expresion '*' expresion { $$ = $1 * $3; }
    | expresion '/' expresion { if ($3 == 0.0) { yyerror("division por cero"); $$ = 0.0; } else { $$ = $1 / $3; } }
    | '-' expresion %prec UMINUS { $$ = - $2; }
    | '(' expresion ')' { $$ = $2; }
    ;

%%
/*
 * SECCIoN 3: CoDIGO DE USUARIO
 * Aqui colocamos la funcion main().
 */

int main(int argc, char *argv[]) {
    // *IMPORTANTE*: Modifica esta linea con tu nombre para el entregable.
    printf("Practica realizada por: [Jose Luis Flores Gutierrez]\n\n");
    printf("Iniciando analizador sintactico. Ingresa expresiones (Ctrl+D para terminar):\n");
    printf("----------------------------------------\n");

    /* Llama a la funcion del analizador sintactico */
    yyparse(); 

    printf("----------------------------------------\n");
    printf("Analisis finalizado.\n");
    return 0;
}