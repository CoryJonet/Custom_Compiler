import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a Mini program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Kids
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignNode          ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of kids, or
// internal nodes with a fixed number of kids:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of kids:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, ReadStmtNode,   WriteStmtNode   
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode { 
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
        for (int k=0; k<indent; k++) p.print(" ");
    }

    /**
     * "1. Add to the name analyzer or type checker (your choice), a
     * check whether the program contains a function named main. If
     * there is no such function, print the error message:
     * "No main function". Use 0,0 as the line and character numbers."
     */
    public static boolean main = false;
    public static int currentScope = 0;
    public static int lastTotalParams = 0;
    public static String lastFunction = "";
}

// **********************************************************************
// ProgramNode,  DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    /**
     * nameAnalysis
     * Creates an empty symbol table for the outermost scope, then processes
     * all of the globals, struct defintions, and functions in the program.
     */
    public void nameAnalysis() {
        SymTable symTab = new SymTable();
        myDeclList.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck() {
        myDeclList.typeCheck();

	/**
	 * "1. Modify name analysis or type checking to ensure that a
	 * main function is declared."
	 */
	if (!main)
	    ErrMsg.fatal(0, 0, "No main function");
    }
    
    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    public void codeGen() {

	myDeclList.codeGen();

    }

    // 1 kid
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    public int countVariables() {
	return myDecls.size();
    }
    /**
     * nameAnalysis
     * Given a symbol table symTab, process all of the decls in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        nameAnalysis(symTab, symTab);
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab and a global symbol table globalTab
     * (for processing struct names in variable decls), process all of the 
     * decls in the list.
     */    
    public void nameAnalysis(SymTable symTab, SymTable globalTab) {
        for (DeclNode node : myDecls) {
            if (node instanceof VarDeclNode) {
                ((VarDeclNode)node).nameAnalysis(symTab, globalTab);
            } else {
                node.nameAnalysis(symTab);
            }
        }

	for(int i = 0; i < myDecls.size(); i++) {
	    
	    try {
	
		VarDeclNode varDeclNode = (VarDeclNode)myDecls.get(i);

		varDeclNode.setOffset(-4 * (lastTotalParams + 2 + i));

	    }

	    catch (ClassCastException ex) {

	    }
	}
    }    
    
    /**
     * typeCheck
     */
    public void typeCheck() {
        for (DeclNode node : myDecls) {
            node.typeCheck();
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    public void codeGen() {

	for (DeclNode node : myDecls)
	    node.codeGen();

    }

    public int numDecls() {

	return myDecls.size();

    }

    // list of kids (DeclNodes)
    private List<DeclNode> myDecls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * for each formal decl in the list
     *     process the formal decl
     *     if there was no error, add type of formal decl to list
     */
    public List<Type> nameAnalysis(SymTable symTab) {
        List<Type> typeList = new LinkedList<Type>();
        for (FormalDeclNode node : myFormals) {
            SemSym sym = node.nameAnalysis(symTab);
            if (sym != null) {
                typeList.add(sym.getType());
            }
        }
        return typeList;
    }    
 
    public int length() {
	return myFormals.size();
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    public void codeGen() {

	for (FormalDeclNode node : myFormals)
	    node.codeGen();

    }

    // list of kids (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    public int numLocals() {
	return myDeclList.countVariables();
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the declaration list
     * - process the statement list
     */
    public void nameAnalysis(SymTable symTab) {
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
    }    
 
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        myStmtList.typeCheck(retType);
    }    
          
    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    public void codeGen() {

	myStmtList.codeGen();

    }

    /**
     * 1. Modify the name analyzer to compute and save the total size of
     * the local variables declared in each function (e.g., in a new field
     * of the function name's symbol-table entry). This will be useful when
     * you do code generation for function entry (to set the SP correctly).
     */
    public int numLocalVars() {

	return myDeclList.numDecls();

    }

    // 2 kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, process each statement in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (StmtNode node : myStmts) {
            node.nameAnalysis(symTab);
        }
    }    
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        for(StmtNode node : myStmts) {
            node.typeCheck(retType);
        }
    }
    
    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    public void codeGen() {

	for (StmtNode node : myStmts)
	    node.codeGen();

    }

    // list of kids (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }
    
    public int size() {
        return myExps.size();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, process each exp in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (ExpNode node : myExps) {
            node.nameAnalysis(symTab);
        }
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(List<Type> typeList) {
        int k = 0;
        try {
            for (ExpNode node : myExps) {
                Type actualType = node.typeCheck();     // actual type of arg
                
                if (!actualType.isErrorType()) {        // if this is not an error
                    Type formalType = typeList.get(k);  // get the formal type
                    if (!formalType.equals(actualType)) {
                        ErrMsg.fatal(node.lineNum(), node.charNum(),
                                     "Type of actual does not match type of formal");
                    }
                }
                k++;
            }
        } catch (NoSuchElementException e) {
            System.err.println("unexpected NoSuchElementException in ExpListNode.typeCheck");
            System.exit(-1);
        }
    }
    
    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    public void codeGen() {

	for (ExpNode node: myExps)
	    node.codeGen();

    }

    // list of kids (ExpNodes)
    private List<ExpNode> myExps;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    /**
     * Note: a formal decl needs to return a sym
     */
    abstract public SemSym nameAnalysis(SymTable symTab);

    // default version of typeCheck for non-function decls
    public void typeCheck() { }

    public void codeGen() { }
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    /**
     * nameAnalysis (overloaded)
     * Given a symbol table symTab, do:
     * if this name is declared void, then error
     * else if the declaration is of a struct type, 
     *     lookup type name (globally)
     *     if type name doesn't exist, then error
     * if no errors so far,
     *     if name has already been declared in this scope, then error
     *     else add name to local symbol table     
     *
     * symTab is local symbol table (say, for struct field decls)
     * globalTab is global symbol table (for struct type names)
     * symTab and globalTab can be the same
     */
    public SemSym nameAnalysis(SymTable symTab) {
        return nameAnalysis(symTab, symTab);
    }
    
    public SemSym nameAnalysis(SymTable symTab, SymTable globalTab) {
        boolean badDecl = false;
        String name = myId.name();
        SemSym sym = null;
        IdNode structId = null;

        if (myType instanceof VoidNode) {  // check for void type
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        else if (myType instanceof StructNode) {
            structId = ((StructNode)myType).idNode();
            sym = globalTab.lookupGlobal(structId.name());
            
            // if the name for the struct type is not found, 
            // or is not a struct type
            if (sym == null || !(sym instanceof StructDefSym)) {
                ErrMsg.fatal(structId.lineNum(), structId.charNum(), 
                             "Invalid name of struct type");
                badDecl = true;
            }
            else {
                structId.link(sym);
	    }
        }
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Multiply declared identifier");
            badDecl = true;            
        }
        
        if (!badDecl) {  // insert into symbol table
            try {
                if (myType instanceof StructNode) {
                    sym = new StructSym(structId);
                }
                else {
                    sym = new SemSym(myType.type(), symTab.size() - 1);

		    /**
		     * "...Change the name analyzer to compute offsets for each
		     * function's parameters and local variables..."
		     */
		    sym.setOffset(symTab.size() - 1);
                }
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return sym;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.println(";");
    }

    public void codeGen() {

	if(currentScope == 0) {
	    
	    Codegen.generate(".data");
	    Codegen.generate(".align 2");
	    Codegen.genLabel("_" + myId.name());
	    Codegen.generate(".space 4");
	
	}

	// I tried this to fix unalignedness but didn't work?
	//else 
	//  Codegen.generate(".align 2");

    }

    public void setOffset(int o) {
	SemSym sym = myId.sym();
	sym.offset = o;
    }

    // 3 kids
    private TypeNode myType;
    private IdNode myId;
    private int mySize;  // use value NOT_STRUCT if this is not a struct type

    public static int NOT_STRUCT = -1;
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type,
                      IdNode id,
                      FormalsListNode formalList,
                      FnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name has already been declared in this scope, then error
     * else add name to local symbol table
     * in any case, do the following:
     *     enter new scope
     *     process the formals
     *     if this function is not multiply declared,
     *         update symbol table entry with types of formals
     *     process the body of the function
     *     exit scope
     */
    public SemSym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        FnSym sym = null;

	if (name.equals("main"))
	    main = true;

	int sizeOfLocals = myBody.numLocals();
	lastTotalParams = myFormalsList.length();
	
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                         "Multiply declared identifier");
        }
        
        else { // add function name to local symbol table
            try {
                sym = new FnSym(myType.type(), myFormalsList.length(),
				sizeOfLocals, symTab.size());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        symTab.addScope();  // add a new scope for locals and params
        
        // process the formals
        List<Type> typeList = myFormalsList.nameAnalysis(symTab);
        if (sym != null) {
            sym.addFormals(typeList);
        }
        
	/**
         * 2. Either write a method to compute the total size of the formal
         * parameters declared in a function, or modify the name analyzer to
         * compute and store that value (in the function name's symbol-table
         * entry). This will also be useful for code generation for function
         * entry.
         */
	lastTotalParams = myFormalsList.length();

        myBody.nameAnalysis(symTab); // process the function body
        
        try {
            symTab.removeScope();  // exit scope
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in FnDeclNode.nameAnalysis");
            System.exit(-1);
        }

        return null;
    } 

    /**
     * typeCheck
     */
    public void typeCheck() {
        myBody.typeCheck(myType.type());
    }
        
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent+4);
        p.println("}\n");
    }

    public void codeGen() {

	lastFunction = Codegen.nextLabel();
	int parameterCount = ((FnSym) myId.sym()).getNumParams();
	int localsCount = myBody.numLocals();

	// The function "preamble"
	Codegen.generate(".text");

	// For the main function, generate:
	if (myId.name().equals("main")) {
	    
	    Codegen.generate(".globl main");
	    Codegen.genLabel("main", "METHOD ENTRY");
	    Codegen.genLabel("__start");
	
	}

	// For all other functions, generate:
	else
	    Codegen.genLabel("_" + myId.name(), "METHOD ENTRY");

	/**
	 * After generating the "preamble" code, you will generate
	 * code for (1) function entry, (2) function body, and
	 * (3) function exit.
	 */
	
	// (1) function entry:
	currentScope++;

	// 1. Push the return address
	Codegen.genPush(Codegen.RA);

	// 2. Push the control link
	Codegen.genPush(Codegen.FP);

	// 3. Set the FP
	/**
	 * Note: the following sets the FP to point to the "bottom"
	 * of the new AR; the reason for "+8" is: 4 bytes each for
	 * the control link and the return addr
	 */
	Codegen.generate("addu", Codegen.FP, Codegen.SP, (parameterCount * 4) + 8);
	
	// 4. Push space for local variables
	Codegen.generate("subu", Codegen.SP, Codegen.SP, localsCount * 4);
	// (2) Function Body
	
	/**
	 * Note: we are talking about the codeGen method for the
	 * FnBodyNode...
	 * There is no need to generate any code for the
	 * declarations. So to generate code for the function body,
	 * just call the codeGen method of each statement in the list.
	 */
	myBody.codeGen();
	
	// (3) Function Exit

	Codegen.genLabel("\t\t# FUNCTION EXIT");

	if (myId.name().equals("main"))
	    Codegen.genLabel("_main_Exit");
	else
	    Codegen.genLabel(lastFunction);
	
	if (!myType.type().isVoidType()) {

	    Codegen.genPop(Codegen.V0);

	}

	// Load return address
	Codegen.generateIndexed("lw", Codegen.RA, Codegen.FP, -4 * parameterCount);
	
	// Save control link
	Codegen.generate("move", Codegen.T0, Codegen.FP);
	
	// Restore FP
	Codegen.generateIndexed("lw", Codegen.FP, Codegen.FP, ((-4 * parameterCount) - 4));
	
	// Restore SP
	Codegen.generate("move", Codegen.SP, Codegen.T0);
	
	// Return
	/**
	 * Note that there are two things that cause a function to
	 * return:
	 * 1. A return statement is executed, or
	 * 2. The last statement in the function is executed
	 */
	if (myId.name().equals("main")) {

	    //Codegen.genLabel("main_Exit");
	    Codegen.generate("li", Codegen.V0, 10);
	    Codegen.generate("syscall");
	
	}

	/**
	 * For each return statement, generate a jump to the label
	 * you used (the op code for an unconditional jump).
	 * As for the return statement value, the codeGen method
	 * for the returned expression will generate code to
	 * evaluate that expression, leaving the value on the
	 * stack.
	 */
	else
	    Codegen.generate("jr", Codegen.RA);

	currentScope--;
    }

    // 4 kids
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FnBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this formal is declared void, then error
     * else if this formal is already in the local symble table,
     *     then issue multiply declared error message and return null
     * else add a new entry to the symbol table and return that Sym
     */
    public SemSym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;
        SemSym sym = null;
        
        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Multiply declared identifier");
            badDecl = true;
        }
        
        if (!badDecl) {  // insert into symbol table
            try {
                sym = new SemSym(myType.type(), symTab.size());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return sym;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
    }

    public void setOffset(int offset) {

	myId.sym().offset = offset;

    }

    // 2 kids
    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name is already in the symbol table,
     *     then multiply declared error (don't add to symbol table)
     * create a new symbol table for this struct definition
     * process the decl list
     * if no errors
     *     add a new entry to symbol table for this struct
     */
    public SemSym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;
        
	//this.bodySize = this.myDeclList.numDecls();

        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Multiply declared identifier");
            badDecl = true;            
        }

        SymTable structSymTab = new SymTable();
        
        // process the fields of the struct
        myDeclList.nameAnalysis(structSymTab, symTab);
        
        if (!badDecl) {
            try {   // add entry to symbol table
                StructDefSym sym = new StructDefSym(structSymTab);
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return null;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("struct ");
        p.print(myId.name());
        p.println("{");
        myDeclList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("};\n");

    }

    // 2 kids
    private IdNode myId;
    private DeclListNode myDeclList;
    
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    /* all subclasses must provide a type method */
    abstract public Type type();
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new IntType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }
}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new BoolType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }
    
    /**
     * type
     */
    public Type type() {
        return new VoidType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        myId = id;
    }

    public IdNode idNode() {
        return myId;
    }
    
    /**
     * type
     */
    public Type type() {
        return new StructType(myId);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        p.print(myId.name());
    }
    
    // 1 kid
    private IdNode myId;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void nameAnalysis(SymTable symTab);
    abstract public void typeCheck(Type retType);

    abstract public void codeGen();

}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        myAssign = assign;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myAssign.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        myAssign.typeCheck();
    }
        
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    public void codeGen() {

	/**
	 * The codeGen method for an assignment expression must 
	 * generate code to:
	 * 1. Evaluate the right-hand-side expression, leaving
	 * the value on the stack
	 * 2. Push the address on the left-hand-side Id onto the
	 * stack.
	 * 3. Store the value into the address
	 * 4. Leave a copy of the value on the stack
	 * Most of the work is done by calling the AssignNode's
	 * children's methods: The codeGen method of the
	 * right-hand-side ExpNode, and the genAddr method of
	 * the left-hand-side IdNode
	 */
	myAssign.codeGen();

	Codegen.genPop(Codegen.T0);

    }

    // 1 kid
    private AssignNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Arithmetic operator applied to non-numeric operand");
        }
    }
        
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++;");
    }

    public void codeGen() {

	myExp.codeGen();
	
	((IdNode)myExp).genAddr();
		
	Codegen.genPop(Codegen.T0);
	
	Codegen.genPop(Codegen.T1);
	
	// Increment
	Codegen.generate("add", Codegen.T1, Codegen.T1, 1);
	
	Codegen.generateIndexed("sw", Codegen.T1, Codegen.T0, 0);

    }

    // 1 kid
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Arithmetic operator applied to non-numeric operand");
        }
    }
        
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--;");
    }
    
    public void codeGen() {

	myExp.codeGen();

	((IdNode)myExp).genAddr();

	Codegen.genPop(Codegen.T0);
	
	Codegen.genPop(Codegen.T1);
 
	// Decrement
	Codegen.generate("add", Codegen.T1, Codegen.T1, -1);

	Codegen.generateIndexed("sw", Codegen.T1, Codegen.T0, 0);

    }

    // 1 kid
    private ExpNode myExp;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }    
 
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (type.isFnType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to read a function");
        }
        
        if (type.isStructDefType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to read a struct name");
        }
        
        if (type.isStructType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to read a struct variable");
        }
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("cin >> ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    /**
     * To read an integer value into register V0, you must generate
     * this code:
     * li    $v0, 5
     * syscall
     * The code loads the special value 5 into register V0, then
     * does a syscall. The fact that V0 contains the value 5 tells
     * the syscall to read an integer value from standard input,
     * storing the value back into register V0. So, we must start by
     * generating the above code, then we must generate code to store
     * the value from V0 to the address of the IdNode.
     */
    public void codeGen() {

	IdNode currId = ((IdNode) myExp);

	if (currId.sym().getType().isIntType() ||
	    currId.sym().getType().isBoolType()) {

	    // Load 5 into V0, then do syscall
	    Codegen.generate("li", Codegen.V0, 5);
	    Codegen.generate("syscall");

	}
	
	currId.genAddr();

	Codegen.genPop(Codegen.T0);
	
	// Store value from V0 to the address of the IdNode
	Codegen.generateIndexed("sw", Codegen.V0, Codegen.T0, 0);
    }

    // 1 kid (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (type.isFnType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to write a function");
        }
        
        if (type.isStructDefType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to write a struct name");
        }
        
        if (type.isStructType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to write a struct variable");
        }
        
        if (type.isVoidType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to write void");
        }
    }
        
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("cout << ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    /**
     * To generate code for a write statement whose expression is of
     * type int, you must:
     * 1. Call the codeGen method of the expression being printed. That
     * method will generate code to evaluate the expression, leaving
     * that value on the top of the stack
     * 2. Generate code to pop the top-of-stack value into register A0
     * (a special register used for output of strings and ints)
     * 3. Generate code to set register V0 to 1
     * 4. Generate a syscall instruction
     */
    public void codeGen() {

	Codegen.genLabel("\t\t# WRITE");

	if(IntLitNode.class.isInstance(myExp)) {

	    // step (1)
	    myExp.codeGen();

	    // step (2)
	    Codegen.genPop(Codegen.A0);

	    // step (3)
	    Codegen.generate("li", Codegen.V0, 1);
	    
	    // step (4)
	    Codegen.generate("syscall");

	}

	else if(StringLitNode.class.isInstance(myExp)) {
	    
	    // step (1)
	    myExp.codeGen();
	    
	    // step (2)
	    Codegen.genPop(Codegen.A0);

	    // step (3)
	    Codegen.generate("li", Codegen.V0, 4);
	    
	    // step (4)
	    Codegen.generate("syscall");
	}

	else if (TrueNode.class.isInstance(myExp) ||
		 FalseNode.class.isInstance(myExp)) {

	    String falseLabel = Codegen.nextLabel();

	    // step (1)
	    myExp.codeGen();
	    
	    // step (2)
	    Codegen.genPop(Codegen.A0);

	    // step (3)
	    Codegen.generate("li", Codegen.A0, 0);

	    Codegen.generate("beqz", Codegen.T0, falseLabel);

	    Codegen.generate("addi", Codegen.A0, Codegen.A0, 1);

	    Codegen.genLabel(falseLabel);

	    // step (4)
	    Codegen.generate("li", Codegen.V0, 1);

	    // step (5)
	    Codegen.generate("syscall");
	}
    }

    // 1 kid
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Non-bool expression used as an if condition");        
        }
        
        myStmtList.typeCheck(retType);
    }
       
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }

    /**
     * The code generated by the IfStmtNode's codeGen method will
     * have the following form:
     * 1. Evaluate the condition, leaving the value on the stack
     * 2. Pop the top-of-stack value into register T0
     * 3. Jump to FalseLabel if T0 == FALSE
     * 4. Code for the statement list
     * 5. FalseLabel
     * Labels: Note that the code generated for an if-then statement
     * will need to include a label. Each label in the generated code
     * must have a unique name (although we will refer to labels in
     * these notes using names like "FalseLabel" as above). As
     * discussed above, we will assume that there is a method called
     * nextlabel that returns (as a String) a new label every time
     * it is called, and we will assume that there is a method called
     * genLabel that prints the given label to the assembly-code file.
     */
    public void codeGen() {

	// Retrieve random label
	String falseLabel = Codegen.nextLabel();

	// step (1)
	myExp.codeGen();

	// step (2)
	Codegen.genPop(Codegen.T0);

	// step (3)
	Codegen.generate("beqz", Codegen.T0, falseLabel);
	
	// step (4)
	myDeclList.codeGen();
	myStmtList.codeGen();
	
	// step (5)
	Codegen.genLabel(falseLabel);

    }

    // e kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
                          StmtListNode slist1, DeclListNode dlist2,
                          StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts of then
     * - exit the scope
     * - enter a new scope
     * - process the decls and stmts of else
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myThenDeclList.nameAnalysis(symTab);
        myThenStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
        symTab.addScope();
        myElseDeclList.nameAnalysis(symTab);
        myElseStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Non-bool expression used as an if condition");        
        }
        
        myThenStmtList.typeCheck(retType);
        myElseStmtList.typeCheck(retType);
    }
        
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent+4);
        myThenStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
        doIndent(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent+4);
        myElseStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");        
    }

    /**
     * The code generated by the IfStmtNode's codeGen method will
     * have the following form:
     * 1. Evaluate the condition, leaving the value on the stack
     * 2. Pop the top-of-stack value into register T0
     * 3. Jump to FalseLabel if T0 == FALSE
     * 4. Code for the statement list
     * 5. FalseLabel
     * Labels: Note that the code generated for an if-then statement
     * will need to include a label. Each label in the generated code
     * must have a unique name (although we will refer to labels in
     * these notes using names like "FalseLabel" as above). As
     * discussed above, we will assume that there is a method called
     * nextlabel that returns (as a String) a new label every time
     * it is called, and we will assume that there is a method called
     * genLabel that prints the given label to the assembly-code file.
     */
    public void codeGen() {

	// Retrieve random labels for false, end
	String falseLabel = Codegen.nextLabel();
	String endLabel = Codegen.nextLabel();

	// step (1)
	myExp.codeGen();
	
	// step (2)
	Codegen.genPop(Codegen.T0);
	
	// step (3), continue if else if, otherwise jump to else
	Codegen.generate("beqz", Codegen.T0, falseLabel);
	
	// step (4) if else if
	myThenDeclList.codeGen();
	myThenStmtList.codeGen();
	
	// step (5) if else if
	Codegen.generate("b", endLabel);
	
	// step (3), go to here if only else
	Codegen.genLabel(falseLabel);
	
	// step (4) if else only
	myElseDeclList.codeGen();
	myElseStmtList.codeGen();

	// step (5) if else only
	Codegen.genLabel(endLabel);

    }

    // 5 kids
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Non-bool expression used as a while condition");        
        }
        
        myStmtList.typeCheck(retType);
    }
        
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }
    
    /**
     * The code generated by the WhileStmtNode's codeGen method will
     * have the following form:
     * 1. Evaluate the condition, leaving the value on the stack
     * 2. Pop the top-of-stack value into register T0
     * 3. Jump to FalseLabel if T0 == FALSE
     * 4. Code for the statement list
     * 5. FalseLabel
     * Labels: Note that the code generated for an if-then statement
     * will need to include a label. Each label in the generated code
     * must have a unique name (although we will refer to labels in
     * these notes using names like "FalseLabel" as above). As
     * discussed above, we will assume that there is a method called
     * nextlabel that returns (as a String) a new label every time
     * it is called, and we will assume that there is a method called
     * genLabel that prints the given label to the assembly-code file.
     */
    public void codeGen() {

	// Generate random labels for loop and end labels
	String loopLabel = Codegen.nextLabel();
	String endLabel = Codegen.nextLabel();
	
	Codegen.genLabel(loopLabel);
	
	// step (1)
	myExp.codeGen();
	
	// step (2)
	Codegen.genPop(Codegen.T0);
	
	// step (3)
	Codegen.generate("beq", Codegen.T0, "0", endLabel);

	// step (4)
	myDeclList.codeGen();
	myStmtList.codeGen();
	
	Codegen.generate("b", loopLabel);
	
	// step (5)
	Codegen.genLabel(endLabel);
    }

    // 3 kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myCall.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        myCall.typeCheck();
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }
    
    /**
     * Note that there is also a call statement:
     * In this case, the called function may not actually return a
     * value (i.e., you may return type void). It doesn't hurt to
     * have the CallExpNode's codeGen method push the value in V0
     * (of F0) after the call (it will just be pushing some random
     * garbage), but it is important for the CallStmtNode's codeGen
     * method to pop that value.
     */
    public void codeGen() {

	// Call codeGen method to push value in V0
	myCall.codeGen();
	
	// Pop value in V0 (important)
	Codegen.genPop(Codegen.V0);
    
    }

    // 1 kid
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child,
     * if it has one
     */
    public void nameAnalysis(SymTable symTab) {
        if (myExp != null) {
            myExp.nameAnalysis(symTab);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        if (myExp != null) {  // return value given
            Type type = myExp.typeCheck();
            
            if (retType.isVoidType()) {
                ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                             "Return with a value in a void function");                
            }
            
            else if (!retType.isErrorType() && !type.isErrorType() && !retType.equals(type)){
                ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                             "Bad return value");
            }
        }
        
        else {  // no return value given -- ok if this is a void function
            if (!retType.isVoidType()) {
                ErrMsg.fatal(0, 0, "Missing return value");                
            }
        }
        
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(";");
    }

    /**
     * To generate the code that actually does the return, use one
     * of the following approaches:
     * 1. For each return statement in the program, generate a
     * copy of the code that pops the AR off the stack then jumps
     * to the return address (that code was discussed above under
     * Function Exit) OR
     * 2. For each return statement in the program, generate a
     * jump to the "return" code that is generated at the end of the
     * function. Note that in this case, you will need to label that
     * return code, and you will need to know what that label is when
     * generating code for a return statement
     */
    public void codeGen() {

	if (myExp != null) {

	    myExp.codeGen();
	    Codegen.genPop(Codegen.V0);

	}

	Codegen.generate("b", lastFunction);

    }

    // 1 kid
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    /**
     * Default version for nodes with no names
     */
    public void nameAnalysis(SymTable symTab) { }
    
    abstract public Type typeCheck();
    abstract public int lineNum();
    abstract public int charNum();

    abstract public void codeGen();
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }
    
    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }
        
    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new IntType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    /**
     * The codeGen methods for IntLitNodes must simply generate code to
     * push the literal value onto the stack. The generated code will
     * look like this:
     * li    $t0, <value>    # load value into T0
     * sw    $t0, ($sp)      # push onto stack
     * subu  $sp, sp, 4
     */
    public void codeGen() {

	// Push int literal onto stack
	Codegen.generate("li", Codegen.T0, myIntVal);

	// Push onto stack
	Codegen.genPush(Codegen.T0);

    }

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }
    
    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new StringType();
    }
        
    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    /**
     * For a StringLitNode, the string literal itself must be stored in the
     * static data area, and its address must be pushed. The code to store a
     * string literal in the static data area looks like this:
     *          .data
     * <label>: .asciiz <string value>
     * Note:
     * 1. <label> needs to be a new label; e.g., returned by a call to nextLabel
     * 2. The <string value> needs to be a string in quotes. You should be
     * storing string literals that way, so just write out the value of the
     * string literal, quotes and all.
     *
     * To avoid storing the same string literal value more than once, keep a
     * hashtable in which the keys are the string literals, and the associated
     * information is the static-data-area label. When you process a string
     * label, look it up in the hashtable; if it is there, use its associated
     * label; otherwise, generate code to store it in the static data area,
     * and add it to the hashtable.
     * The code you need to generate to push the address of a string literal onto
     * the stack looks like this:
     * .text
     * la    $t0, <label> # load addr into $t0
     * sw    $t0, ($sp)   # push onto stack
     * subu  $sp, $sp, 4
     */
    public void codeGen() {

	// Generate random label for <label>:
	String stringLabel = Codegen.nextLabel();

	Codegen.generate(".data");

	Codegen.generateLabeled(stringLabel, ".asciiz ", "", myStrVal);

	// Code to generate the pushing of address onto the stack
	Codegen.generate(".text");
	Codegen.generate("la", Codegen.T0, stringLabel);
	Codegen.genPush(Codegen.T0);

    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new BoolType();
    }
        
    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }

    public void codeGen() {

	Codegen.generate("li", Codegen.T0, Codegen.TRUE);

	Codegen.genPush(Codegen.T0);

    }

    private int myLineNum;
    private int myCharNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new BoolType();
    }
        
    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }

    public void codeGen() {

	Codegen.generate("li", Codegen.T0, Codegen.FALSE);

	Codegen.genPush(Codegen.T0);

    }

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    /**
     * Link the given symbol to this ID.
     */
    public void link(SemSym sym) {
        mySym = sym;
    }
    
    /**
     * Return the name of this ID.
     */
    public String name() {
        return myStrVal;
    }
    
    /**
     * Return the symbol associated with this ID.
     */
    public SemSym sym() {
        return mySym;
    }
    
    /**
     * Return the line number for this ID.
     */
    public int lineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this ID.
     */
    public int charNum() {
        return myCharNum;
    }    
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - check for use of undeclared name
     * - if ok, link to symbol table entry
     */
    public void nameAnalysis(SymTable symTab) {
        SemSym sym = symTab.lookupGlobal(myStrVal);
        if (sym == null) {
            ErrMsg.fatal(myLineNum, myCharNum, "Undeclared identifier");
        } else {
            link(sym);
        }
    }
 
    /**
     * typeCheck
     */
    public Type typeCheck() {
        if (mySym != null) {
            return mySym.getType();
        } 
        else {
            System.err.println("ID with null sym field in IdNode.typeCheck");
            System.exit(-1);
        }
        return null;
    }
           
    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (mySym != null) {
            p.print("(" + mySym + ")");
        }
    }
    
    /**
     * Code Generation for IdNodes: The code that needs to be generated for
     * the name will be different in each context:
     * We use "codeGen" for the second case (fetching the value and pushing
     * it onto the stack) since that is what the codegen methods of all
     * ExpNodes must do
     * 2. For an expression, we will need to generate code to fetch the current
     * value eitehr from the static data area or from the current Activation
     * Record, and push that value onto the stack
     *
     * The codeGen method must copy the value of the global/local variable
     * into a register (e.g., T0 for an int variable and F0 for a double),
     * then push the value onto the stack. Different code will be generated for 
     * a global and a local variable. Below are four examples:
     * lw $t0 _g // load the value of int global g into T0
     * lw t00(fp)// load the value of the int local stored at offset 0 into T0
     * l.d $f0 _d// load the value of dbl global d into F0
     * l.d f0−4(fp)// load the value of the dbl local stored at offset -4 into F0
     *
     * Note that this means that there must be a way to tell whether an IdNode
     * represents a global or a local variable. There are several possible
     * ways to accomplish this:
     * 1. The symbol-table entry includes a "kind" field (which distinguishes
     * between globals and locals).
     * 2. Different sub-classes of the Sym class are used for globals and for
     * local variables (so you can tell whether you have a global or a local
     * using "instanceof", or using an IsGlobal method that you write for each
     * sub-class of Sym).
     * 3. The symbol-tabl entry includes an "offset" field; for local variables,
     * that field has a value less than or equal to zero, while for globals, the
     * value is greater than zero.
     */
    public void codeGen() {
	
	//System.out.println("2: " + scope);
	// Determine if global
	if (currentScope > mySym.scope) {

	    Codegen.generate("la", Codegen.T0, "_" + myStrVal);

	    Codegen.generateIndexed("lw", Codegen.T0, Codegen.T0, 0);

	    Codegen.genPush(Codegen.T0);
	    
	}
	
	// Otherwise local
	Codegen.generate("add", Codegen.T0, Codegen.FP, mySym.getBaseScope());
	Codegen.generateIndexed("lw", Codegen.T0, Codegen.T0, 0);
	
    }

    /**
     * genJumpAndLink: The genJumpAndLink method will simply generate a 
     * jump-and-link instruction (with opcode jal) using the appropriate label
     * as the target of the jump. If the called function is "main", the label is
     * just "main". For all other functions, the label is of the form:
     * _<functionName>
     */
    public void genJumpAndLink() {

	Codegen.generate("jal", "_" + myStrVal);

    }

    
    /**
     * genAddr: The genAddr method must load the address of the identifier into
     * a register (e.g., T0), then push it onto the stack. The code is very 
     * similar to the code to load the value of the identifier; we just use the
     * la (load address) opcode instead of the lw (load word) or l.d 
     * (load double) opcode.
     * Here is the code you need to generate to load the address of a global
     * variable g into register T0 (this works whether g is int or double, since
     * an address always takes 4 bytes):
     * la $t0, _g
     * and here is the code for a local, stored at offset -8:
     * la $t0, -8($fp)
     */
    public void genAddr() {

	//System.out.println("scope of sym: " + mySym.getScope());
	// Determine if global
	if (currentScope > mySym.scope) {
	    //  System.out.println("type: " + mySym.toString());
	    Codegen.generate("la", Codegen.T0, "_" 
			     + myStrVal);
	    Codegen.genPush(Codegen.T0);
	}

	// Otherwise local	
	Codegen.generateIndexed("la", Codegen.T0, 
				Codegen.FP, mySym.getBaseScope());
	Codegen.genPush(Codegen.T0);
    }
    
    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private SemSym mySym;
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;    
        myId = id;
        mySym = null;
    }

    /**
     * Return the symbol associated with this dot-access node.
     */
    public SemSym sym() {
        return mySym;
    }    
    
    /**
     * Return the line number for this dot-access node. 
     * The line number is the one corresponding to the RHS of the dot-access.
     */
    public int lineNum() {
        return myId.lineNum();
    }
    
    /**
     * Return the char number for this dot-access node.
     * The char number is the one corresponding to the RHS of the dot-access.
     */
    public int charNum() {
        return myId.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the LHS of the dot-access
     * - process the RHS of the dot-access
     * - if the RHS is of a struct type, set the sym for this node so that
     *   a dot-access "higher up" in the AST can get access to the symbol
     *   table for the appropriate struct definition
     */
    public void nameAnalysis(SymTable symTab) {
        badAccess = false;
        SymTable structSymTab = null; // to lookup RHS of dot-access
        SemSym sym = null;
        
        myLoc.nameAnalysis(symTab);  // do name analysis on LHS
        
        // if myLoc is really an ID, then sym will be a link to the ID's symbol
        if (myLoc instanceof IdNode) {
            IdNode id = (IdNode)myLoc;
            sym = id.sym();
            
            // check ID has been declared to be of a struct type
            
            if (sym == null) { // ID was undeclared
                badAccess = true;
            }
            else if (sym instanceof StructSym) { 
                // get symbol table for struct type
                SemSym tempSym = ((StructSym)sym).getStructType().sym();
                structSymTab = ((StructDefSym)tempSym).getSymTable();
            } 
            else {  // LHS is not a struct type
                ErrMsg.fatal(id.lineNum(), id.charNum(), 
                             "Dot-access of non-struct type");
                badAccess = true;
            }
        }
        
        // if myLoc is really a dot-access (i.e., myLoc was of the form
        // LHSloc.RHSid), then sym will either be
        // null - indicating RHSid is not of a struct type, or
        // a link to the Sym for the struct type RHSid was declared to be
        else if (myLoc instanceof DotAccessExpNode) {
            DotAccessExpNode loc = (DotAccessExpNode)myLoc;
            
            if (loc.badAccess) {  // if errors in processing myLoc
                badAccess = true; // don't continue proccessing this dot-access
            }
            else { //  no errors in processing myLoc
                sym = loc.sym();

                if (sym == null) {  // no struct in which to look up RHS
                    ErrMsg.fatal(loc.lineNum(), loc.charNum(), 
                                 "Dot-access of non-struct type");
                    badAccess = true;
                }
                else {  // get the struct's symbol table in which to lookup RHS
                    if (sym instanceof StructDefSym) {
                        structSymTab = ((StructDefSym)sym).getSymTable();
                    }
                    else {
                        System.err.println("Unexpected Sym type in DotAccessExpNode");
                        System.exit(-1);
                    }
                }
            }

        }
        
        else { // don't know what kind of thing myLoc is
            System.err.println("Unexpected node type in LHS of dot-access");
            System.exit(-1);
        }
        
        // do name analysis on RHS of dot-access in the struct's symbol table
        if (!badAccess) {
        
            sym = structSymTab.lookupGlobal(myId.name()); // lookup
            if (sym == null) { // not found - RHS is not a valid field name
                ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                             "Invalid struct field name");
                badAccess = true;
            }
            
            else {
                myId.link(sym);  // link the symbol
                // if RHS is itself as struct type, link the symbol for its struct 
                // type to this dot-access node (to allow chained dot-access)
                if (sym instanceof StructSym) {
                    mySym = ((StructSym)sym).getStructType().sym();
                }
            }
        }
    }    
 
    /**
     * typeCheck
     */
    public Type typeCheck() {
        return myId.typeCheck();
    }
    
    public void unparse(PrintWriter p, int indent) {
        myLoc.unparse(p, 0);
        p.print(".");
        myId.unparse(p, 0);
    }

    public void codeGen() { }

    // 2 kids
    private ExpNode myLoc;    
    private IdNode myId;
    private SemSym mySym;          // link to Sym for struct type
    private boolean badAccess;  // to prevent multiple, cascading errors
}

class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }
    
    /**
     * Return the line number for this assignment node. 
     * The line number is the one corresponding to the left operand.
     */
    public int lineNum() {
        return myLhs.lineNum();
    }
    
    /**
     * Return the char number for this assignment node.
     * The char number is the one corresponding to the left operand.
     */
    public int charNum() {
        return myLhs.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myLhs.nameAnalysis(symTab);
        myExp.nameAnalysis(symTab);
    }
 
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type typeLhs = myLhs.typeCheck();
        Type typeExp = myExp.typeCheck();
        Type retType = typeLhs;
        
        if (typeLhs.isFnType() && typeExp.isFnType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Function assignment");
            retType = new ErrorType();
        }
        
        if (typeLhs.isStructDefType() && typeExp.isStructDefType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Struct name assignment");
            retType = new ErrorType();
        }
        
        if (typeLhs.isStructType() && typeExp.isStructType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Struct variable assignment");
            retType = new ErrorType();
        }        
        
        if (!typeLhs.equals(typeExp) && !typeLhs.isErrorType() && !typeExp.isErrorType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Type mismatch");
            retType = new ErrorType();
        }
        
        if (typeLhs.isErrorType() || typeExp.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
    
    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)  p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)  p.print(")");
    }

    /**
     * The AST for an assignment statement looks like this: The AssignStmtNode's
     * codeGen method can call the AssignNode's method to do the assignment, but
     * be careful: an AssignNode is a subclass of ExpNode, so like all nodes
     * that represent expressions, it must leave the value of the expression
     * on the stack. Therefore, the AssignStmtNode must generate code to pop
     * (and ignore) that value.
     */
    public void codeGen() {

	//Codegen.generate(".align 2");
	// step 1: evaluate the right-hand-side expression, leaving the value
	myExp.codeGen();
	
	//Codegen.generate(".align 2");
	((IdNode) myLhs).genAddr();
	//Codegen.generate(".space 4");
	// step 2: Puch the address of the left-hand-side Id onto the stack
       	Codegen.genPop(Codegen.T0);
	Codegen.genPop(Codegen.T1);
	//Codegen.generate(".align 2");
	// step 3: Store the value into the address
	Codegen.generateIndexed("sw", Codegen.T1, Codegen.T0, 0);

	// step 4: Leave a copy of the value on the stack
	Codegen.genPush(Codegen.T1);

    }

    // 2 kids
    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }

    /**
     * Return the line number for this call node. 
     * The line number is the one corresponding to the function name.
     */
    public int lineNum() {
        return myId.lineNum();
    }
    
    /**
     * Return the char number for this call node.
     * The char number is the one corresponding to the function name.
     */
    public int charNum() {
        return myId.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myId.nameAnalysis(symTab);
        myExpList.nameAnalysis(symTab);
    }  
      
    /**
     * typeCheck
     */
    public Type typeCheck() {
        if (!myId.typeCheck().isFnType()) {  
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Attempt to call a non-function");
            return new ErrorType();
        }
        
        FnSym fnSym = (FnSym)(myId.sym());
        
        if (fnSym == null) {
            System.err.println("null sym for Id in CallExpNode.typeCheck");
            System.exit(-1);
        }
        
        if (myExpList.size() != fnSym.getNumParams()) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Function call with wrong number of args");
            return fnSym.getReturnType();
        }
        
        myExpList.typeCheck(fnSym.getParamTypes());
        return fnSym.getReturnType();
    }
        
    // ** unparse **
    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");
    }

    public void codeGen() {

	if(myExpList != null)
	    myExpList.codeGen();

	myId.genJumpAndLink(); 

	Codegen.genPush(Codegen.V0);

    }

    // 2 kids
    private IdNode myId;
    private ExpListNode myExpList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }
    
    /**
     * Return the line number for this unary expression node. 
     * The line number is the one corresponding to the  operand.
     */
    public int lineNum() {
        return myExp.lineNum();
    }
    
    /**
     * Return the char number for this unary expression node.
     * The char number is the one corresponding to the  operand.
     */
    public int charNum() {
        return myExp.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    // one child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }
    
    /**
     * Return the line number for this binary expression node. 
     * The line number is the one corresponding to the left operand.
     */
    public int lineNum() {
        return myExp1.lineNum();
    }
    
    /**
     * Return the char number for this binary expression node.
     * The char number is the one corresponding to the left operand.
     */
    public int charNum() {
        return myExp1.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myExp1.nameAnalysis(symTab);
        myExp2.nameAnalysis(symTab);
    }
    
    // two kids
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type = myExp.typeCheck();
        Type retType = new IntType();
        
        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (type.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }

    /**
     * The codeGen methods for the non short-circuited operators must all do
     * the same basic sequence of tasks:
     * 1. Call each child's codeGen method to generate code that will evaluate
     * the operand(s), leaving the value(s) on the stack.
     * 2. Generate code to pop the operand value(s) off the stack into
     * register(s) (e.g., T0 and T1 for ints: F0 and F2 for doubles). Remember
     * that if there are two operands, the right one will be on the top of the
     * stack.
     * 3. Generate code to perform the operation (see Spim documentation for a
     * list of opcodes).
     * 4. Generate code to push teh result onto the stack
     */
    public void codeGen() {

	// step 1: evaluate operand
	myExp.codeGen();

	// step 2: pop value in T0
	Codegen.genPop(Codegen.T0);

	// step 3: perform UnaryMinus
	Codegen.generate("li", Codegen.T1, "0"); 
	Codegen.generate("sub", Codegen.T1, Codegen.T1, Codegen.T0);
	
	// step 4: push result
	Codegen.genPush(Codegen.T1);
    }

}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type = myExp.typeCheck();
        Type retType = new BoolType();
        
        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }
        
        if (type.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(!");
        myExp.unparse(p, 0);
        p.print(")");
    }

    /**
     * The codeGen methods for the non short-circuited operators must all do
     * the same basic sequence of tasks:
     * 1. Call each child's codeGen method to generate code that will evaluate
     * the operand(s), leaving the value(s) on the stack.
     * 2. Generate code to pop the operand value(s) off the stack into
     * register(s) (e.g., T0 and T1 for ints: F0 and F2 for doubles). Remember
     * that if there are two operands, the right one will be on the top of the
     * stack.
     * 3. Generate code to perform the operation (see Spim documentation for a
     * list of opcodes).
     * 4. Generate code to push teh result onto the stack
     */
    public void codeGen() {

	// step 1: evaluate operand
	myExp.codeGen();
	
	// step 2: pop value in T0
	Codegen.genPop(Codegen.T0);

	// step 3: perform NOT
	Codegen.generate("li", Codegen.T1, 1);	
	Codegen.generate("xor", Codegen.T0, Codegen.T0, Codegen.T1);
	
	// step 4: push result
	Codegen.genPush(Codegen.T0);

    }
}

// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************

abstract class ArithmeticExpNode extends BinaryExpNode {
    public ArithmeticExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new IntType();
        
        if (!type1.isErrorType() && !type1.isIntType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(),
                         "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (!type2.isErrorType() && !type2.isIntType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(),
                         "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

abstract class LogicalExpNode extends BinaryExpNode {
    public LogicalExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BoolType();
        
        if (!type1.isErrorType() && !type1.isBoolType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(),
                         "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }
        
        if (!type2.isErrorType() && !type2.isBoolType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(),
                         "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

abstract class EqualityExpNode extends BinaryExpNode {
    public EqualityExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BoolType();
        
        if (type1.isVoidType() && type2.isVoidType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to void functions");
            retType = new ErrorType();
        }
        
        if (type1.isFnType() && type2.isFnType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to functions");
            retType = new ErrorType();
        }
        
        if (type1.isStructDefType() && type2.isStructDefType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to struct names");
            retType = new ErrorType();
        }
        
        if (type1.isStructType() && type2.isStructType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to struct variables");
            retType = new ErrorType();
        }        
        
        if (!type1.equals(type2) && !type1.isErrorType() && !type2.isErrorType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Type mismatch");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

abstract class RelationalExpNode extends BinaryExpNode {
    public RelationalExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BoolType();
        
        if (!type1.isErrorType() && !type1.isIntType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(),
                         "Relational operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (!type2.isErrorType() && !type2.isIntType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(),
                         "Relational operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

class PlusNode extends ArithmeticExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    /**
     * The codeGen methods for the non short-circuited operators must all do
     * the same basic sequence of tasks:
     * 1. Call each child's codeGen method to generate code that will evaluate
     * the operand(s), leaving the value(s) on the stack.
     * 2. Generate code to pop the operand value(s) off the stack into
     * register(s) (e.g., T0 and T1 for ints: F0 and F2 for doubles). Remember
     * that if there are two operands, the right one will be on the top of the
     * stack.
     * 3. Generate code to perform the operation (see Spim documentation for a
     * list of opcodes).
     * 4. Generate code to push teh result onto the stack
     */
    public void codeGen() {

	// step 1: evaluate both operands
	myExp2.codeGen();
	myExp1.codeGen();

	// step 2: pop values in T0 and T1
	Codegen.genPop(Codegen.T0);
	Codegen.genPop(Codegen.T1);

	// step 3: perform the addition (T0 = T0 + 1)
	Codegen.generate("add", Codegen.T0, Codegen.T0, Codegen.T1);
	
	// step 4: push result
	Codegen.genPush(Codegen.T0);
    }

}

class MinusNode extends ArithmeticExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    /**
     * The codeGen methods for the non short-circuited operators must all do
     * the same basic sequence of tasks:
     * 1. Call each child's codeGen method to generate code that will evaluate
     * the operand(s), leaving the value(s) on the stack.
     * 2. Generate code to pop the operand value(s) off the stack into
     * register(s) (e.g., T0 and T1 for ints: F0 and F2 for doubles). Remember
     * that if there are two operands, the right one will be on the top of the
     * stack.
     * 3. Generate code to perform the operation (see Spim documentation for a
     * list of opcodes).
     * 4. Generate code to push teh result onto the stack
     */
    public void codeGen() {

	// step 1: evaluate both operands
	myExp2.codeGen();
	myExp1.codeGen();

	// step 2: pop values in T0 and T1
	Codegen.genPop(Codegen.T0);
	Codegen.genPop(Codegen.T1);

	// step 3: perform the subtraction
	Codegen.generate("sub", Codegen.T0, Codegen.T0, Codegen.T1);
	
	// step 4: push result
	Codegen.genPush(Codegen.T0);
    }
}

class TimesNode extends ArithmeticExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    /**
     * The codeGen methods for the non short-circuited operators must all do
     * the same basic sequence of tasks:
     * 1. Call each child's codeGen method to generate code that will evaluate
     * the operand(s), leaving the value(s) on the stack.
     * 2. Generate code to pop the operand value(s) off the stack into
     * register(s) (e.g., T0 and T1 for ints: F0 and F2 for doubles). Remember
     * that if there are two operands, the right one will be on the top of the
     * stack.
     * 3. Generate code to perform the operation (see Spim documentation for a
     * list of opcodes).
     * 4. Generate code to push teh result onto the stack
     */
    public void codeGen() {

	// step 1: evaluate both operands
	myExp2.codeGen();
	myExp1.codeGen();

	// step 2: pop values in T0 and T1
	Codegen.genPop(Codegen.T0);
	Codegen.genPop(Codegen.T1);

	// step 3: perform the multiplication
	Codegen.generate("mul", Codegen.T0, Codegen.T0, Codegen.T1);
	
	// step 4: push result
	Codegen.genPush(Codegen.T0);
    }
}

class DivideNode extends ArithmeticExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    /**
     * The codeGen methods for the non short-circuited operators must all do
     * the same basic sequence of tasks:
     * 1. Call each child's codeGen method to generate code that will evaluate
     * the operand(s), leaving the value(s) on the stack.
     * 2. Generate code to pop the operand value(s) off the stack into
     * register(s) (e.g., T0 and T1 for ints: F0 and F2 for doubles). Remember
     * that if there are two operands, the right one will be on the top of the
     * stack.
     * 3. Generate code to perform the operation (see Spim documentation for a
     * list of opcodes).
     * 4. Generate code to push teh result onto the stack
     */
    public void codeGen() {

	// step 1: evaluate both operands
	myExp2.codeGen();
	myExp1.codeGen();
	
	// step 2: pop values in T0 and T1
	Codegen.genPop(Codegen.T0);
	Codegen.genPop(Codegen.T1);
	
	// step 3: perform the division
	Codegen.generate("div", Codegen.T0, Codegen.T0, Codegen.T1);
	
	// step 4: push the result
	Codegen.genPush(Codegen.T0);
    }
}

class AndNode extends LogicalExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" && ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    /**
     * "Short-circuiting" means that the right operand is evaluated only if
     * necessary. This means that the code generated for the logical operators
     * will need to involve some jumps depending on the values of some
     * expressions.
     */
    public void codeGen() {

	// step 1: evaluate both operands
	myExp2.codeGen();
	myExp1.codeGen();
	
	// step 2: pop values in T0 and T1
	Codegen.genPop(Codegen.T0);
	Codegen.genPop(Codegen.T1);
	
	String shortCkt = Codegen.nextLabel();
        String done = Codegen.nextLabel();

	// step 3: perform the AND
	Codegen.generate("li", Codegen.T2, 0);
	Codegen.generate("beq", Codegen.T2, Codegen.T0, shortCkt);
	Codegen.generate("and", Codegen.T0, Codegen.T0, Codegen.T1);
	
	// step 4: push result if AND
	Codegen.genPush(Codegen.T0);

	// step 3: perform NOT AND
	Codegen.generate("b", done);
	
	Codegen.genLabel(shortCkt);
	
	// step 4: push result if NOT AND
	Codegen.genPush(Codegen.T0);
	
	Codegen.genLabel(done);
    }
}

class OrNode extends LogicalExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" || ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    /**
     * "Short-circuiting" means that the right operand is evaluated only if
     * necessary. This means that the code generated for the logical operators
     * will need to involve some jumps depending on the values of some
     * expressions.
     */
    public void codeGen() {

	// step 1: evaluate both operands
	myExp2.codeGen();
	myExp1.codeGen();

	// step 2: pop values in T0 and T1
	Codegen.genPop(Codegen.T0);
	Codegen.genPop(Codegen.T1);

	String shortCkt = Codegen.nextLabel();
        String done = Codegen.nextLabel();

	// step 3: perform the OR
	Codegen.generate("li", Codegen.T2, 0);
	Codegen.generate("bne", Codegen.T2, Codegen.T0, shortCkt);
	Codegen.generate("or", Codegen.T0, Codegen.T0, Codegen.T1);
	
	// step 4: push result if OR done
	Codegen.genPush(Codegen.T0);

	// step 3: perform NOT OR
	Codegen.generate("b", done);

	Codegen.genLabel(shortCkt);

	// step 4: push result if OR not done
	Codegen.genPush(Codegen.T0);

	Codegen.genLabel(done);

    }
}

class EqualsNode extends EqualityExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    /**
     * The codeGen methods for the non short-circuited operators must all do
     * the same basic sequence of tasks:
     * 1. Call each child's codeGen method to generate code that will evaluate
     * the operand(s), leaving the value(s) on the stack.
     * 2. Generate code to pop the operand value(s) off the stack into
     * register(s) (e.g., T0 and T1 for ints: F0 and F2 for doubles). Remember
     * that if there are two operands, the right one will be on the top of the
     * stack.
     * 3. Generate code to perform the operation (see Spim documentation for a
     * list of opcodes).
     * 4. Generate code to push teh result onto the stack
     */
    public void codeGen() {

	// step 1: evaluate both operands
	myExp1.codeGen();
	myExp2.codeGen();
	
	// step 2: pop values in T0 and T1
	Codegen.genPop(Codegen.T0);
	Codegen.genPop(Codegen.T1);
	
	// step 3: perform the equality
	Codegen.generate("xor", Codegen.T0, Codegen.T0, Codegen.T1);
	Codegen.generate("not", Codegen.T0, Codegen.T0);
	
	// step 4: push result
	Codegen.genPush(Codegen.T0);

    }
}

class NotEqualsNode extends EqualityExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" != ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    /**
     * The codeGen methods for the non short-circuited operators must all do
     * the same basic sequence of tasks:
     * 1. Call each child's codeGen method to generate code that will evaluate
     * the operand(s), leaving the value(s) on the stack.
     * 2. Generate code to pop the operand value(s) off the stack into
     * register(s) (e.g., T0 and T1 for ints: F0 and F2 for doubles). Remember
     * that if there are two operands, the right one will be on the top of the
     * stack.
     * 3. Generate code to perform the operation (see Spim documentation for a
     * list of opcodes).
     * 4. Generate code to push teh result onto the stack
     */
    public void codeGen() {

	// step 1: evaluate both operands
	myExp1.codeGen();
	myExp2.codeGen();

	// step 2: pop values in T0 and T1
	Codegen.genPop(Codegen.T0);
	Codegen.genPop(Codegen.T1);

	// step 3: perform not equals
	Codegen.generate("xor", Codegen.T0, Codegen.T0, Codegen.T1);
	
	// step 4: push result
	Codegen.genPush(Codegen.T0);

    }

}

class LessNode extends RelationalExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    /**
     * The codeGen methods for the non short-circuited operators must all do
     * the same basic sequence of tasks:
     * 1. Call each child's codeGen method to generate code that will evaluate
     * the operand(s), leaving the value(s) on the stack.
     * 2. Generate code to pop the operand value(s) off the stack into
     * register(s) (e.g., T0 and T1 for ints: F0 and F2 for doubles). Remember
     * that if there are two operands, the right one will be on the top of the
     * stack.
     * 3. Generate code to perform the operation (see Spim documentation for a
     * list of opcodes).
     * 4. Generate code to push teh result onto the stack
     */
    public void codeGen() {

	// step 1: evaluate both operands
	myExp2.codeGen();
	myExp1.codeGen();

	// step 2: pop values in T0 and T1
	Codegen.genPop(Codegen.T0);
	Codegen.genPop(Codegen.T1);

	String bl = Codegen.nextLabel();

	// step 3: perform less operation
	Codegen.generate("li", Codegen.T2, 0);
	Codegen.generate("bge", Codegen.T0, Codegen.T1, bl);
	Codegen.generate("addi", Codegen.T2, 1);
	Codegen.genLabel(bl);

	// step 4: push result
	Codegen.genPush(Codegen.T2);

    }
}

class GreaterNode extends RelationalExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    /**
     * The codeGen methods for the non short-circuited operators must all do
     * the same basic sequence of tasks:
     * 1. Call each child's codeGen method to generate code that will evaluate
     * the operand(s), leaving the value(s) on the stack.
     * 2. Generate code to pop the operand value(s) off the stack into
     * register(s) (e.g., T0 and T1 for ints: F0 and F2 for doubles). Remember
     * that if there are two operands, the right one will be on the top of the
     * stack.
     * 3. Generate code to perform the operation (see Spim documentation for a
     * list of opcodes).
     * 4. Generate code to push teh result onto the stack
     */
    public void codeGen() {

	// step 1: evaluate both operands
	myExp2.codeGen();
	myExp1.codeGen();

	// step 2: pop values in T0 and T1
	Codegen.genPop(Codegen.T0);
	Codegen.genPop(Codegen.T1);

	String bg = Codegen.nextLabel();

	// step 3: perform greater operation
	Codegen.generate("li", Codegen.T2, 0);
	Codegen.generate("ble", Codegen.T0, Codegen.T1, bg);
	Codegen.generate("addi", Codegen.T2, 1);
	Codegen.genLabel(bg);

	// step 4: push result
	Codegen.genPush(Codegen.T2);

    }
}

class LessEqNode extends RelationalExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    /**
     * The codeGen methods for the non short-circuited operators must all do
     * the same basic sequence of tasks:
     * 1. Call each child's codeGen method to generate code that will evaluate
     * the operand(s), leaving the value(s) on the stack.
     * 2. Generate code to pop the operand value(s) off the stack into
     * register(s) (e.g., T0 and T1 for ints: F0 and F2 for doubles). Remember
     * that if there are two operands, the right one will be on the top of the
     * stack.
     * 3. Generate code to perform the operation (see Spim documentation for a
     * list of opcodes).
     * 4. Generate code to push teh result onto the stack
     */
    public void codeGen() {

	// step 1: evaluate both operands
	myExp2.codeGen();
	myExp1.codeGen();

	// step 2: pop values in T0 and T1
	Codegen.genPop(Codegen.T0);
	Codegen.genPop(Codegen.T1);

	String ble = Codegen.nextLabel();

	// step 3: perform the less than or equal
	Codegen.generate("li", Codegen.T2, 0);
	Codegen.generate("bgt", Codegen.T0, Codegen.T1, ble);
	Codegen.generate("addi", Codegen.T2, 1);
	Codegen.genLabel(ble);

	// step 4: push result
	Codegen.genPush(Codegen.T2);

    }
}

class GreaterEqNode extends RelationalExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    public void codeGen() {

	// step 1: evaluate both operands
	myExp2.codeGen();
	myExp1.codeGen();

	// step 2: pop values in T0 and T1
	Codegen.genPop(Codegen.T0);
	Codegen.genPop(Codegen.T1);

	String ge = Codegen.nextLabel();

	// step 3: perform the greater equal operation
	Codegen.generate("li", Codegen.T2, 0);
	Codegen.generate("blt", Codegen.T0, Codegen.T1, ge);
	Codegen.generate("addi", Codegen.T2, 1); 
	Codegen.genLabel(ge);

	// step 4: push result
	Codegen.genPush(Codegen.T2);

    }

}
