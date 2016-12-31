###
# This Makefile can be used to make a parser for the cats language
# (parser.class) and to make a program (P3.class) that tests the parser and
# the unparse methods in ast.java.
#
# make clean removes all generated files.
#
###

JC = javac
CP = .
CP2 = .

P3.class: P3.java parser.class Yylex.class ASTnode.class
	$(JC)    P3.java

parser.class: parser.java ASTnode.class Yylex.class ErrMsg.class
	$(JC)      parser.java

parser.java: cats.cup
	java   java_cup.Main < cats.cup

Yylex.class: cats.jlex.java sym.class ErrMsg.class
	$(JC)   cats.jlex.java

ASTnode.class: ast.java
	$(JC)  ast.java

cats.jlex.java: cats.jlex sym.class
	java    JLex.Main cats.jlex

sym.class: sym.java
	$(JC)    sym.java

sym.java: cats.cup
	java    java_cup.Main < cats.cup

ErrMsg.class: ErrMsg.java
	$(JC) ErrMsg.java

##test
test:
	java   P3 test.cf test.out

###
# clean
###
clean:
	rm -f *~ *.class parser.java cats.jlex.java sym.java
