%{
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

extern FILE *yyin;
FILE *outfile;
extern int yylineno;

int yylex();
void yyerror(const char *s);

// ============= TABLA DE SÍMBOLOS =============
#define MAX_SYMBOLS 1000

typedef struct {
    char *name;
    char *type;
    int initialized;
    int line;
} Symbol;

Symbol symbol_table[MAX_SYMBOLS];
int symbol_count = 0;
int semantic_errors = 0;

// Buscar símbolo en la tabla
Symbol* lookup_symbol(const char *name) {
    for (int i = 0; i < symbol_count; i++) {
        if (strcmp(symbol_table[i].name, name) == 0) {
            return &symbol_table[i];
        }
    }
    return NULL;
}

// Agregar símbolo a la tabla
void add_symbol(const char *name, const char *type, int initialized) {
    Symbol *existing = lookup_symbol(name);
    if (existing != NULL) {
        fprintf(stderr, "ERROR SEMÁNTICO línea %d: Variable '%s' ya declarada en línea %d\n", 
                yylineno, name, existing->line);
        semantic_errors++;
        return;
    }
    
    if (symbol_count >= MAX_SYMBOLS) {
        fprintf(stderr, "ERROR: Tabla de símbolos llena\n");
        exit(1);
    }
    
    symbol_table[symbol_count].name = strdup(name);
    symbol_table[symbol_count].type = strdup(type);
    symbol_table[symbol_count].initialized = initialized;
    symbol_table[symbol_count].line = yylineno;
    symbol_count++;
}

// Verificar que una variable existe
int check_variable_declared(const char *name) {
    Symbol *sym = lookup_symbol(name);
    if (sym == NULL) {
        fprintf(stderr, "ERROR SEMÁNTICO línea %d: Variable '%s' no declarada\n", 
                yylineno, name);
        semantic_errors++;
        return 0;
    }
    return 1;
}

// Verificar que una variable está inicializada
void check_variable_initialized(const char *name) {
    Symbol *sym = lookup_symbol(name);
    if (sym != NULL && !sym->initialized) {
        fprintf(stderr, "ADVERTENCIA línea %d: Variable '%s' puede no estar inicializada\n", 
                yylineno, name);
    }
}

// Marcar variable como inicializada
void mark_initialized(const char *name) {
    Symbol *sym = lookup_symbol(name);
    if (sym != NULL) {
        sym->initialized = 1;
    }
}

// Verificar compatibilidad de tipos
int check_type_compatibility(const char *type1, const char *type2) {
    // Tipos idénticos son compatibles
    if (strcmp(type1, type2) == 0) return 1;
    
    // int y float son compatibles entre sí
    if ((strcmp(type1, "int") == 0 && strcmp(type2, "float") == 0) ||
        (strcmp(type1, "float") == 0 && strcmp(type2, "int") == 0)) {
        return 1;
    }
    
    return 0;
}

// Obtener tipo de una expresión
char* get_expression_type(const char *expr) {
    // Si es un literal
    if (expr[0] == '"') return "string";
    if (strchr(expr, '.') != NULL) return "float";
    if (expr[0] >= '0' && expr[0] <= '9') return "int";
    
    // Si es una variable
    Symbol *sym = lookup_symbol(expr);
    if (sym != NULL) return sym->type;
    
    // Si es temporal (_t*)
    if (expr[0] == '_' && expr[1] == 't') return "int"; // Asumir int para temporales
    
    return "unknown";
}

// Verificar operación aritmética
char* check_arithmetic_op(const char *left, const char *right) {
    char *left_type = get_expression_type(left);
    char *right_type = get_expression_type(right);
    
    if (strcmp(left_type, "string") == 0 || strcmp(right_type, "string") == 0) {
        fprintf(stderr, "ERROR SEMÁNTICO línea %d: Operación aritmética no válida con strings\n", 
                yylineno);
        semantic_errors++;
        return "error";
    }
    
    // Si alguno es float, el resultado es float
    if (strcmp(left_type, "float") == 0 || strcmp(right_type, "float") == 0) {
        return "float";
    }
    
    return "int";
}

// --- GENERADORES ---
int temp_count = 0;
char* gen_temp() {
    char buffer[20];
    sprintf(buffer, "_t%d", temp_count++);
    fprintf(outfile, "VAR %s\n", buffer);
    return strdup(buffer);
}

int label_count = 0;
char* gen_label() {
    char buffer[20];
    sprintf(buffer, "L%d", label_count++);
    return strdup(buffer);
}

// --- PILAS ---
struct LoopInfo {
    char *start;
    char *end;
    char *body;
    char *inc;
} loop_stack[100];
int l_top = -1;

void push_loop(char *s, char *e, char *b, char *i) {
    l_top++;
    loop_stack[l_top].start = s;
    loop_stack[l_top].end = e;
    loop_stack[l_top].body = b;
    loop_stack[l_top].inc = i;
}
void pop_loop() { l_top--; }

struct IfInfo {
    char *label_else;
    char *label_end;
} if_stack[100];
int i_top = -1;

void push_if(char *l_else, char *l_end) {
    i_top++;
    if_stack[i_top].label_else = l_else;
    if_stack[i_top].label_end = l_end;
}
void pop_if() { i_top--; }

%}

%union {
    char* sval;
}

%token <sval> INT_LIT FLOAT_LIT STRING_LIT ID
%token KW_VAR KW_FUNC KW_RETURN
%token TYPE_INT TYPE_FLOAT TYPE_BOOL TYPE_STRING TYPE_VOID TYPE_ARRAY
%token KW_IF KW_ELSE KW_WHILE KW_FOR
%token KW_PRINT KW_DRAW KW_INPUT KW_READ
%token KW_NEW_ARRAY KW_PUSH KW_LENGTH
%token ASSIGN PLUS MINUS MULT DIV MOD POW
%token EQ NEQ LT GT LTE GTE
%token LPAREN RPAREN LBRACE RBRACE LBRACKET RBRACKET SEMICOLON COMMA

%type <sval> expression term factor type condition input_expr

%left PLUS MINUS
%left MULT DIV MOD
%right POW

%%

program:
    statement_list
    ;

statement_list:
    statement
    | statement_list statement
    ;

statement:
    var_decl
    | assignment
    | print_stmt
    | pixel_stmt
    | input_stmt
    | if_stmt
    | while_stmt
    | for_stmt
    | block
    ;

block:
    LBRACE statement_list RBRACE
    ;

// --- DECLARACION ---
var_decl:
    KW_VAR type ID ASSIGN expression SEMICOLON {
        // Verificar tipo de la expresión
        char *expr_type = get_expression_type($5);
        if (!check_type_compatibility($2, expr_type)) {
            fprintf(stderr, "ERROR SEMÁNTICO línea %d: Asignación incompatible de tipo '%s' a variable '%s' de tipo '%s'\n", 
                    yylineno, expr_type, $3, $2);
            semantic_errors++;
        }
        
        // Agregar a tabla de símbolos
        add_symbol($3, $2, 1);
        
        // Generar código
        fprintf(outfile, "VAR %s\n", $3);
        fprintf(outfile, "ASSIGN %s %s\n", $5, $3);
    }
    | KW_VAR type ID ASSIGN input_expr SEMICOLON { 
        add_symbol($3, $2, 1);
        fprintf(outfile, "VAR %s\n", $3);
        fprintf(outfile, "KEY %s %s\n", $5, $3); 
    }
    | KW_VAR type ID ASSIGN KW_NEW_ARRAY LPAREN RPAREN SEMICOLON { 
        if (strcmp($2, "array") != 0) {
            fprintf(stderr, "ERROR SEMÁNTICO línea %d: new_array() solo puede asignarse a tipo 'array'\n", 
                    yylineno);
            semantic_errors++;
        }
        add_symbol($3, $2, 1);
        fprintf(outfile, "VAR %s\n", $3);
        fprintf(outfile, "ASSIGN 0 %s\n", $3);
    }
    ;

// --- ASIGNACION ---
assignment:
    ID ASSIGN expression SEMICOLON {
        // Verificar que la variable existe
        if (check_variable_declared($1)) {
            Symbol *sym = lookup_symbol($1);
            char *expr_type = get_expression_type($3);
            
            // Verificar compatibilidad de tipos
            if (!check_type_compatibility(sym->type, expr_type)) {
                fprintf(stderr, "ERROR SEMÁNTICO línea %d: Asignación incompatible de tipo '%s' a variable '%s' de tipo '%s'\n", 
                        yylineno, expr_type, $1, sym->type);
                semantic_errors++;
            }
            
            mark_initialized($1);
        }
        
        fprintf(outfile, "ASSIGN %s %s\n", $3, $1);
    }
    ;

for_update:
    ID ASSIGN expression {
        if (check_variable_declared($1)) {
            mark_initialized($1);
        }
        fprintf(outfile, "ASSIGN %s %s\n", $3, $1);
    }
    ;

input_stmt:
    input_expr SEMICOLON
    ;

input_expr:
     KW_INPUT LPAREN INT_LIT RPAREN { $$ = $3; }
   | KW_READ LPAREN INT_LIT RPAREN { $$ = $3; }
   ;

print_stmt:
    KW_PRINT LPAREN expression RPAREN SEMICOLON {
        fprintf(outfile, "PRINT %s\n", $3);
    }
    ;

pixel_stmt:
    KW_DRAW LPAREN expression COMMA expression COMMA expression RPAREN SEMICOLON {
        // Verificar que las coordenadas y color son numéricos
        char *type1 = get_expression_type($3);
        char *type2 = get_expression_type($5);
        char *type3 = get_expression_type($7);
        
        if (strcmp(type1, "string") == 0 || strcmp(type2, "string") == 0 || strcmp(type3, "string") == 0) {
            fprintf(stderr, "ERROR SEMÁNTICO línea %d: draw() requiere valores numéricos\n", 
                    yylineno);
            semantic_errors++;
        }
        
        fprintf(outfile, "PIXEL %s %s %s\n", $3, $5, $7);
    }
    ;

if_stmt:
    KW_IF LPAREN condition RPAREN {
        char *L_else = gen_label();
        char *L_end = gen_label();
        push_if(L_else, L_end);
        fprintf(outfile, "IFFALSE %s GOTO %s\n", $3, L_else);
    } block {
        fprintf(outfile, "GOTO %s\n", if_stack[i_top].label_end);
        fprintf(outfile, "LABEL %s\n", if_stack[i_top].label_else);
    } optional_else
    ;

optional_else:
    /* vacio */ {
        fprintf(outfile, "LABEL %s\n", if_stack[i_top].label_end);
        pop_if();
    }
    | KW_ELSE block {
        fprintf(outfile, "LABEL %s\n", if_stack[i_top].label_end);
        pop_if();
    }
    ;

while_stmt:
    KW_WHILE LPAREN {
        char *L_start = gen_label();
        fprintf(outfile, "LABEL %s\n", L_start);
        push_loop(L_start, NULL, NULL, NULL); 
    } condition RPAREN {
        char *L_end = gen_label();
        loop_stack[l_top].end = L_end;
        fprintf(outfile, "IFFALSE %s GOTO %s\n", $4, L_end);
    } block {
        fprintf(outfile, "GOTO %s\n", loop_stack[l_top].start);
        fprintf(outfile, "LABEL %s\n", loop_stack[l_top].end);
        pop_loop();
    }
    ;

for_stmt:
    KW_FOR LPAREN var_decl {
        char *L_start = gen_label();
        fprintf(outfile, "LABEL %s\n", L_start);
        push_loop(L_start, NULL, NULL, NULL);
    } condition SEMICOLON {
        char *L_end = gen_label();
        char *L_body = gen_label();
        char *L_inc = gen_label();
        
        loop_stack[l_top].end = L_end;
        loop_stack[l_top].body = L_body;
        loop_stack[l_top].inc = L_inc;

        fprintf(outfile, "IFFALSE %s GOTO %s\n", $5, L_end);
        fprintf(outfile, "GOTO %s\n", L_body);
        fprintf(outfile, "LABEL %s\n", L_inc);
    } for_update RPAREN { 
        fprintf(outfile, "GOTO %s\n", loop_stack[l_top].start);
        fprintf(outfile, "LABEL %s\n", loop_stack[l_top].body);
    } block {
        fprintf(outfile, "GOTO %s\n", loop_stack[l_top].inc);
        fprintf(outfile, "LABEL %s\n", loop_stack[l_top].end);
        pop_loop();
    }
    ;

condition:
      expression EQ expression { $$=gen_temp(); fprintf(outfile, "EQ %s %s %s\n", $1, $3, $$); }
    | expression NEQ expression { $$=gen_temp(); fprintf(outfile, "NEQ %s %s %s\n", $1, $3, $$); }
    | expression LT expression { $$=gen_temp(); fprintf(outfile, "LT %s %s %s\n", $1, $3, $$); }
    | expression GT expression { $$=gen_temp(); fprintf(outfile, "GT %s %s %s\n", $1, $3, $$); }
    | expression LTE expression { $$=gen_temp(); fprintf(outfile, "LTE %s %s %s\n", $1, $3, $$); }
    | expression GTE expression { $$=gen_temp(); fprintf(outfile, "GTE %s %s %s\n", $1, $3, $$); }
    ;

expression:
    expression PLUS expression { 
        check_arithmetic_op($1, $3);
        $$=gen_temp(); 
        fprintf(outfile, "ADD %s %s %s\n", $1, $3, $$); 
    }
    | expression MINUS expression { 
        check_arithmetic_op($1, $3);
        $$=gen_temp(); 
        fprintf(outfile, "SUB %s %s %s\n", $1, $3, $$); 
    }
    | term
    ;

term:
    term MULT term { 
        check_arithmetic_op($1, $3);
        $$=gen_temp(); 
        fprintf(outfile, "MUL %s %s %s\n", $1, $3, $$); 
    }
    | term DIV term { 
        check_arithmetic_op($1, $3);
        $$=gen_temp(); 
        fprintf(outfile, "DIV %s %s %s\n", $1, $3, $$); 
    }
    | term MOD term { 
        check_arithmetic_op($1, $3);
        $$=gen_temp(); 
        fprintf(outfile, "MOD %s %s %s\n", $1, $3, $$); 
    }
    | factor
    ;

factor:
    LPAREN expression RPAREN { $$ = $2; }
    | ID { 
        check_variable_declared($1);
        check_variable_initialized($1);
        $$ = $1; 
    }
    | INT_LIT { $$ = $1; }
    | FLOAT_LIT { $$ = $1; }
    | STRING_LIT { $$ = $1; }
    ;

type:
      TYPE_INT { $$ = "int"; }
    | TYPE_FLOAT { $$ = "float"; }
    | TYPE_STRING { $$ = "string"; }
    | TYPE_BOOL { $$ = "bool"; }
    | TYPE_VOID { $$ = "void"; }
    | TYPE_ARRAY { $$ = "array"; }
    ;

%%

void yyerror(const char *s) {
    fprintf(stderr, "Error de sintaxis en línea %d: %s\n", yylineno, s);
    exit(1);
}

void print_symbol_table() {
    printf("\n=== TABLA DE SÍMBOLOS ===\n");
    printf("%-15s %-10s %-12s %s\n", "Variable", "Tipo", "Inicializada", "Línea");
    printf("------------------------------------------------\n");
    for (int i = 0; i < symbol_count; i++) {
        printf("%-15s %-10s %-12s %d\n", 
               symbol_table[i].name, 
               symbol_table[i].type,
               symbol_table[i].initialized ? "Sí" : "No",
               symbol_table[i].line);
    }
    printf("\n");
}

int main(int argc, char **argv) {
    if (argc != 2) {
        printf("Uso: %s <archivo.src>\n", argv[0]);
        return 1;
    }
    yyin = fopen(argv[1], "r");
    if (!yyin) return 1;
    outfile = fopen("output.fis", "w");
    fprintf(outfile, "// Codigo FIS-25 generado\n");
    yyparse();
    
    // Mostrar tabla de símbolos
    print_symbol_table();
    
    if (semantic_errors > 0) {
        printf("\n Compilación fallida: %d errores semánticos encontrados\n", semantic_errors);
        fclose(yyin);
        fclose(outfile);
        return 1;
    }
    
    printf("Compilación exitosa! Revisa output.fis\n");
    fclose(yyin);
    fclose(outfile);
    return 0;
}