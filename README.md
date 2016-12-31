# Custom_Compiler
Custom compiler based on made up programming language, although very similar to Java, and uses the JLex lexigraphical interpreter and Java Construction of Useful Parsers (CUP) to achieve compiler functionality.

Custom compiler designed around the following typical stages of a compiler:
Stage 1: A source program is interpreted as a sequence of characters and is categorized by a symbol table
Stage 2: Sequences of tokens are interpreted from the sequence of characters by a lexical analyzer (scanner)
Stage 3: The sequences of tokens are validated by a syntax analyzer (parser) to construct an Abstract Syntax Tree (AST) and eventually intermediate code
Stage 4/5: Intermediate code is optimized to reduce redundancy and improve efficiency prior to assembly language creation
Stage 6: Assembly language creation
