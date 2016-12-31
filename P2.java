import java.util.*;
import java.io.*;
import java_cup.runtime.*;  // defines Symbol

/**
 * This program is to be used to test the Scanner.
 * This version is set up to test all tokens, but more code is needed to test 
 * other aspects of the scanner (e.g., input that causes errors, character 
 * numbers, values associated with tokens)
 */
public class P2 {
    public static void main(String[] args) throws IOException {
                                           // exception may be thrown by yylex
        // test all tokens
        testAllTokens(args[0]);
        CharNum.num = 1;
    
        // ADD CALLS TO OTHER TEST METHODS HERE
    }

    /**
     * testAllTokens
     *
     * Open and read from file allTokens.txt
     * For each token read, write the corresponding string to allTokens.out
     * If the input file contains all tokens, one per line, we can verify
     * correctness of the scanner by comparing the input and output files
     * (e.g., using a 'diff' command).
     */
    private static void testAllTokens(String file) throws IOException {
        // open input and output files
        FileReader inFile = null;
        PrintWriter outFile = null;
        try {
            inFile = new FileReader(file);
	    // I parsed out "." and replace with .out
            outFile = new PrintWriter(new FileWriter(file.substring(0, file.indexOf(".")) + ".out"));
        } catch (FileNotFoundException ex) {
            System.err.println("File " + file + " not found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println(file + " cannot be opened.");
            System.exit(-1);
        }

        // create and call the scanner
        Yylex scanner = new Yylex(inFile);
        Symbol token = scanner.next_token();
        while (token.sym != sym.EOF) {
            switch (token.sym) {

		// For each Symbol, print Symbol type, line number, and character index
            case sym.BOOL:
                outFile.println("Sym: bool\nLine Number: " + 
				((TokenVal) token.value).linenum +
				"\nCharacter Number: " +
				((TokenVal) token.value).charnum +
				"\n"); 
                break;
	    case sym.INT:
		outFile.println("Sym: int\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.VOID:
		outFile.println("Sym: void\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.TRUE:
		outFile.println("Sym: true\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.FALSE:
		outFile.println("Sym: false\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.STRUCT:
		outFile.println("Sym: struct\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.CIN:
		outFile.println("Sym: cin\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.COUT:
		outFile.println("Sym: cout\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;				
            case sym.IF:
		outFile.println("Sym: if\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.ELSE:
		outFile.println("Sym: else\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.WHILE:
		outFile.println("Sym: while\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.RETURN:
		outFile.println("Sym: return\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.ID:
		outFile.println("Sym: ID\nLine Number: " +
                                ((IdTokenVal) token.value).linenum +
                                "\nCharacter Index: " +
                                ((IdTokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.INTLITERAL:  
		outFile.println("Sym: IntLiteral\nLine Number: " +
                                ((IntLitTokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((IntLitTokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.STRINGLITERAL: 
		outFile.println("Sym: StringLiteral\nLine Number: " +
                                ((StrLitTokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((StrLitTokenVal) token.value).charnum +
                                "\n");
                break;    
            case sym.LCURLY:
		outFile.println("Sym: LCurly\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.RCURLY:
		outFile.println("Sym: RCurly\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.LPAREN:
                outFile.println("Sym: LParen\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.RPAREN:
		outFile.println("Sym: RParen\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.SEMICOLON:
		outFile.println("Sym: Semicolon\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.COMMA:
                outFile.println("Sym: Comma\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.DOT:
		outFile.println("Sym: Dot\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.WRITE:
		outFile.println("Sym: Write\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.READ:
		outFile.println("Sym: Read\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;				
            case sym.PLUSPLUS:
                outFile.println("Sym: PlusPlus\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.MINUSMINUS:
                outFile.println("Sym: MinusMiuns\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;	
            case sym.PLUS:
                outFile.println("Sym: RCurly\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.MINUS:
		outFile.println("Sym: Minus\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.TIMES:
                outFile.println("Sym: Times\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.DIVIDE:
                outFile.println("Sym: Divide\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.NOT:
		outFile.println("Sym: Not\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.AND:
		outFile.println("Sym: And\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.OR:
		outFile.println("Sym: Or\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.EQUALS:
                outFile.println("Sym: Equals\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.NOTEQUALS:
                outFile.println("Sym: NotEquals\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
                                "\n");
                break;
            case sym.LESS:
                outFile.println("Sym: Less\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
				"\n");
                break;
            case sym.GREATER:
                outFile.println("Sym: Greater\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
				"\n");
                break;
            case sym.LESSEQ:
                outFile.println("Sym: LessEq\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
				"\n");
                break;
            case sym.GREATEREQ:
                outFile.println("Sym: GreaterEq\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
				"\n");
                break;
	    case sym.ASSIGN:
                outFile.println("Sym: Assign\nLine Number: " +
                                ((TokenVal) token.value).linenum +
                                "\nCharacter Number: " +
                                ((TokenVal) token.value).charnum +
				"\n");
                break;
	    default:	
		outFile.println("UNKNOWN TOKEN");
            } // end switch

            token = scanner.next_token();
        } // end while
        outFile.close();
    }
}
