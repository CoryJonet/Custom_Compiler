import java.util.*;

/**
 * " 1. Modify the Sym class from program 1 (by including some new fields and
 * methods and/or by defining some subclasses)."
 */
public class SemSym {
 
    private String type;

    public SemSym(String type) {

	this.type = type;

    }

    public void setType(String type) {

	this.type = type;

    }

    public String getType() {

	return type;

    }

    public String toString() {

	return type;

    }

}

/** 
 * It is up to you how you store information in each symbol-table entry 
 * (each Sym). To implement the changes to the unparser described below you 
 * will need to know each name's type. For function names, this includes the 
 * return type and the number of parameters and their types. You can modify the 
 * Sym class by adding some new fields (e.g., a kind field) and/or by declaring 
 * some subclasses (e.g., a subclass for functions that has extra fields for the 
 * return type and the list of parameter types). You will probably also want to 
 * add new methods that return the values of the new fields and it may be 
 * helpful to change the toString method so that you can print the contents of a 
 * Sym for debugging purposes.
 */

/* Function declaration */
class FunctionSym extends SemSym {

    private List<FormalDeclNode> formalList;

    public FunctionSym(String paramType, List<FormalDeclNode> formalList) {

        super(paramType);
        this.formalList = formalList;

    }

    public List<FormalDeclNode> getFormalList() {

        return formalList;

    }
}

/* Struct declaration */
class StructDeclSym extends SemSym {

    private SymTable symTab;
    public StructDeclSym(String paramType) {

	super(paramType);
        symTab = new SymTable();

    }

    public SymTable getSymTable() {

        return symTab;

    }

}

/* Struct member(s) */
class StructVarSym extends SemSym {

    private String structType;

    public StructVarSym(String paramType) {
        super(paramType);
    }

    public String getStructType() {

	return structType;

    }
}
