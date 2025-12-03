all: compiler

compiler: parser.tab.c lex.yy.c
	gcc -o compilador parser.tab.c lex.yy.c -lfl

parser.tab.c: parser.y
	bison -d parser.y

lex.yy.c: lexer.l
	flex lexer.l

clean:
	rm -f compilador parser.tab.c parser.tab.h lex.yy.c output.fis