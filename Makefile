# define the java compiler to be used and the flags
JC = javac
FLAGS = -g -cp $(CP)
CP = .

P2.class: P2.java Yylex.class sym.class
	$(JC) $(FLAGS) P2.java

Yylex.class: cats.jlex.java ErrMsg.class sym.class
	$(JC) $(FLAGS) cats.jlex.java

cats.jlex.java: cats.jlex sym.class
	java -cp $(CP) JLex.Main cats.jlex

sym.class: sym.java
	$(JC) $(FLAGS) sym.java

ErrMsg.class: ErrMsg.java
	$(JC) $(FLAGS) ErrMsg.java

###
# testing - add more here to run your tester and compare its results
# to expected results
###
test:
	@echo -e "\nTesting ALL in allTokens.in\n"
	java -cp $(CP) P2 allTokens.in
	diff allTokens_solutions.txt allTokens.out

	@echo -e "\nTesting Unterminated and Bad Characters\n"
	java -cp $(CP) P2 unterminated_strings.in
	diff unterminated_strings_solution.txt unterminated_strings.out

	@echo -e "\nTesting EOF\n"
	java -cp $(CP) P2 eof.in
	diff eof_solution.txt eof.out
	@echo -e "\nSuccess!\n"

test_all:

	@echo -e "\nTesting ALL\n"
	@echo -e "Testing: Reserved Words"
	java -cp $(CP) P2 reserved_words.in
	diff reserved_words_solution.txt reserved_words.out

	@echo -e "\nTesting: Identifiers"
	java -cp $(CP) P2 identifiers.in
	diff identifiers_solution.txt identifiers.out

	@echo -e "\nTesting: Integer Literals"
	java -cp $(CP) P2 integer_literals.in
	diff integer_literals_solution.txt integer_literals.out

	@echo -e "\nTesting: String Literals"
	java -cp $(CP) P2 string_literals.in
	diff string_literals_solution.txt string_literals.out

	@echo -e "\nTesting: Two Character Symbols"
	java -cp $(CP) P2 two_character_symbols.in
	diff two_character_symbols_solution.txt two_character_symbols.out
	@echo -e "\nTesting: Comments"
	java -cp $(CP) P2 comments.in
	diff comments_solution.txt comments.out

	@echo -e "\nTesting: Illegal Characters"
	java -cp $(CP) P2 illegal_characters.in
	diff illegal_characters_solution.txt illegal_characters.out

	@echo -e "Testing: EOF"
	java -cp $(CP) P2 eof.in
	diff eof_solution.txt eof.out
	@echo -e "\nSuccess!\n"

test_identifiers:
	@echo -e "\nTesting: Identifiers ONLY"
	java -cp $(CP) P2 identifiers.in 
	diff identifiers_solution.txt identifiers.out
	@echo -e "\nSuccess!\n"

test_reserved_words:
	@echo -e "\nTesting: Reserved Words ONLY"
	java -cp $(CP) P2 reserved_words.in
	diff reserved_words_solution.txt reserved_words.out
	@echo -e "\nSuccess!\n"

test_integer_literals:
	@echo -e "\nTesting: Integer Literals ONLY"
	java -cp $(CP) P2 integer_literals.in
	diff integer_literals_solution.txt integer_literals.out
	@echo -e "\nSuccess!\n"

test_string_literals:
	@echo -e "\nTesting: String Literals ONLY"
	java -cp $(CP) P2 string_literals.in
	diff string_literals_solution.txt string_literals.out
	@echo -e "\nSuccess!\n"

test_two_character_symbols:
	@echo -e "\nTesting: Two Character Symbols"
	java -cp $(CP) P2 two_character_symbols.in
	diff two_character_symbols_solution.txt two_character_symbols..out
	@echo -e "\nSuccess!\n"

test_comments:
	@echo -e "\nTesting: Comments ONLY"
	java -cp $(CP) P2 comments.in
	diff comments_solution.txt comments.out
	@echo -e "\nSuccess!\n"

test_illegal_characters:
	@echo -e "\nTesting: Illegal Characters"
	java -cp $(CP) P2 illegal_characters.in
	diff illegal_characters_solution.txt illegal_characters.out
	@echo -e "\nSuccess!\n"

test_unterminated_strings:
	@echo -e "\nTesting: Unterminated Strings\n"
	java -cp $(CP) P2 unterminated_strings.in
	diff unterminated_strings_solution.txt unterminated_strings.out
	@echo -e "\nSuccess!\n"

test_eof:
	@echo -e "\nTesting EOF"
	java -cp $(CP) P2 eof.in
	diff eof_solution.txt eof.out
	@echo -e "\nSuccess!\n"

test_long_strings:
	@echo -e "\nTesting: Long Strings"
	java -cp $(CP) P2 long_strings.in
	diff long_strings_solution.txt long_strings.out
	@echo -e "\nSuccess!\n"

###
# clean up
###
clean:
	rm -f *~ *.class cats.jlex.java
	rm -f allTokens.out
	rm -f unterminated_strings.out
	rm -f eof.out

clean_all:
	rm -f reserved_words.out
	rm -f identifiers.out
	rm -f integer_literals.out
	rm -f string_literals.out
	rm -f two_character_symbols.out
	rm -f comments.out
	rm -f illegal_characters.out
	rm -f unterminated_strings.out
	rm -f eof.out
	rm -f long_strings.out

clean_reserved_words:
	rm -f reserved_words.out

clean_identifiers:
	rm -f identifiers.out

clean_integer_literals:
	rm -f integer_literals.out

clean_string_literals:
	rm -f string_literals.out

clean_two_character_symbols:
	rm -f two_character_symbols.out

clean_comments:
	rm -f comments.out

clean_illegal_characters:
	rm -f illegal_characters.out

clean_unterminated_strings:
	rm -f unterminated_strings.out

clean_eof:
	rm -f eof.out

clean_long_strings:
	rm -f long_strings.out
