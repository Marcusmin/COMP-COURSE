/**
 * Checker.java   
 * Mar 29 15:57:55 AEST 2019
 **/

/**
 * Completing checker, which is a visitor implementation
 * In vc.java, after we finished parsing syntax tree,
 * AST passed to an instance of checker.
 * The checker run its check method, which intutively call
 * AST subtree's visit method. This visit method will traverse
 * the whole AST from top to botton and decorating it.
 * It is parent's responsible to call its children's visit methods(I guess)
 */
package VC.Checker;

import VC.ASTs.*;
import VC.Scanner.SourcePosition;
import VC.ErrorReporter;
import VC.StdEnvironment;

public final class Checker implements Visitor {

  private String errMesg[] = {
    "*0: main function is missing",                            
    "*1: return type of main is not int",                    

    // defined occurrences of identifiers
    // for global, local and parameters
    "*2: identifier redeclared",                             
    "*3: identifier declared void",                         
    "*4: identifier declared void[]",                      

    // applied occurrences of identifiers
    "*5: identifier undeclared",                          

    // assignments
    "*6: incompatible type for =",                       
    "*7: invalid lvalue in assignment",                 

     // types for expressions 
    "*8: incompatible type for return",                
    "*9: incompatible type for this binary operator", 
    "*10: incompatible type for this unary operator",

     // scalars
     "*11: attempt to use an array/function as a scalar", 

     // arrays
     "*12: attempt to use a scalar/function as an array",
     "*13: wrong type for element in array initialiser",
     "*14: invalid initialiser: array initialiser for scalar",   
     "*15: invalid initialiser: scalar initialiser for array",  
     "*16: excess elements in array initialiser",              
     "*17: array subscript is not an integer",                
     "*18: array size missing",                              

     // functions
     "*19: attempt to reference a scalar/array as a function",

     // conditional expressions in if, for and while
    "*20: if conditional is not boolean",                    
    "*21: for conditional is not boolean",                  
    "*22: while conditional is not boolean",               

    // break and continue
    "*23: break must be in a while/for",                  
    "*24: continue must be in a while/for",              

    // parameters 
    "*25: too many actual parameters",                  
    "*26: too few actual parameters",                  
    "*27: wrong type for actual parameter",           

    // reserved for errors that I may have missed (J. Xue)
    "*28: misc 1",
    "*29: misc 2",

    // the following two checks are optional 
    "*30: statement(s) not reached",     
    "*31: missing return statement",    
  };


  private SymbolTable idTable;
  private static SourcePosition dummyPos = new SourcePosition();
  private ErrorReporter reporter;

  // Checks whether the source program, represented by its AST, 
  // satisfies the language's scope rules and type rules.
  // Also decorates the AST as follows:
  //  (1) Each applied occurrence of an identifier is linked to
  //      the corresponding declaration of that identifier.
  //  (2) Each expression and variable is decorated by its type.

  public Checker (ErrorReporter reporter) {
    this.reporter = reporter;
    this.idTable = new SymbolTable ();
    establishStdEnvironment();
  }

  public void check(AST ast) {
    ast.visit(this, null);  //pass this check as a visitor
  }

  // auxiliary methods

  // Try to find identifier in symbol table
  // report error if it has been declared
  // insert in symbol table if success
  private void declareVariable(Ident ident, Decl decl) {
    IdEntry entry = idTable.retrieveOneLevel(ident.spelling);

    if (entry == null) {
      ; // no problem
    } else
      reporter.reportError(errMesg[2] + ": %", ident.spelling, ident.position); //id redecl error
    idTable.insert(ident.spelling, decl);
  }

  public Expr coercions(Expr expr) {
    Operator op = new Operator("i2f", dummyPos);
    UnaryExpr eAST = new UnaryExpr(op, expr, dummyPos);
    eAST.type = StdEnvironment.floatType;
    return eAST;
  }

  class Response {
    // inner class for check if program has main etc
    public boolean hasMain;
    public boolean isInt;
    public Object ast;
    public Response(boolean init) {
      hasMain = init;
      isInt = init;
    }
    public Response(boolean init, Object astInit) {
      hasMain = init;
      ast = astInit;
    }
  }

  class InitHelper {
    public int indexLimit;
    public int indexCounter;
    public ArrayType arrType;
    public String ident;
    public InitHelper(Type arr, Ident id) {
      indexCounter = 0;
      ident = id.spelling;
      ArrayType aType = (ArrayType)arr;
      if (aType.E.isEmptyExpr()) {
        indexLimit = 0;
      } else {
        indexLimit = Integer.parseInt(((IntExpr)aType.E).IL.spelling);  // size of array
      }
      arrType = aType;
    }
  }
  // Programs

  public Object visitProgram(Program ast, Object o) {
    Response response = new Response(false);
    ast.FL.visit(this, response); // traverse the program ast, pass Checker class as visitor
    if (response.hasMain != true) {
      reporter.reportError(errMesg[0], "", ast.position);
    } else if (!response.isInt) {
      reporter.reportError(errMesg[1], "", ast.position);
    }
    // check if program has main func
    return null;
  }

  // Statements
  public Object visitEmptyCompStmt(EmptyCompStmt ast, Object o) {
    // Todo: implementation
    if ((ast.parent instanceof FuncDecl) && !((FuncDecl)ast.parent).T.isVoidType()) {
      reporter.reportError(errMesg[31], "", ast.parent.position);
    }
    return null;
  }

  public Object visitCompoundStmt(CompoundStmt ast, Object o) { // get access to function's parameter
    if (!(ast.parent instanceof FuncDecl)) {
      idTable.openScope();  // scope level +1
      ast.DL.visit(this, o); // type check decl list
      ast.SL.visit(this, o); // type check stmt list
      idTable.closeScope(); // scope level -1
      return null;
    } else {
      // Your code goes here
      // todo: type check on compoundstmt
      // Find out if it has a return stmt (optional)
      ast.DL.visit(this, null); // type check decl list
      FuncDecl fDecl = (FuncDecl)o;
      Response response = new Response(false, o);
      ast.SL.visit(this, response); // type check stmt list
      if (!response.hasMain && !fDecl.T.isVoidType()) {
        reporter.reportError(errMesg[31],"", ast.position);
      }
      return null;
    }
  }

  public Object visitStmtList(StmtList ast, Object o) {
    if (o != null) {
      ast.S.visit(this, o); // type check stmt
      if (ast.S instanceof ReturnStmt && ast.SL instanceof StmtList)  // no stmt after return
        reporter.reportError(errMesg[30], "", ast.SL.position);
      ast.SL.visit(this, o);  // type check stmt list
    } else {
      ast.S.visit(this, null); // type check stmt
      if (ast.S instanceof ReturnStmt && ast.SL instanceof StmtList)  // no stmt after return
        reporter.reportError(errMesg[30], "", ast.SL.position);
      ast.SL.visit(this, null);  // type check stmt list
    }
    return null;
  }


  public Object visitExprStmt(ExprStmt ast, Object o) {
    ast.E.visit(this, null);
    return null;
  }

  public Object visitReturnStmt(ReturnStmt ast, Object o) {
    // Todo:
    Type eType = (Type)ast.E.visit(this, o);
    if (o != null) {
      Response r = (Response)o;
      FuncDecl fDecl = (FuncDecl)r.ast;
      if (fDecl.T.isVoidType()) {
        // should have no return statement
        reporter.reportError(errMesg[8], "", ast.position);
      }
      if (!fDecl.T.equals(eType) && eType.isIntType() && fDecl.T.isFloatType()) {
        ast.E = coercions(ast.E);
      } else if (!fDecl.T.equals(eType)) {
        reporter.reportError(errMesg[8], "", ast.position);
      }
      r.hasMain = true;
    }
    return null;
  }

  public Object visitContinueStmt(ContinueStmt ast, Object o) {
    // todo
    try {
      Boolean isInLoop = (Boolean)o;
    } catch(Exception e) {
      reporter.reportError(errMesg[24], "", ast.position);
    }
    return null;
  }

  public Object visitBreakStmt(BreakStmt ast, Object o) {
    // todo
    try{
      Boolean isInLoop = (Boolean)o;
    } catch(Exception e) {
      reporter.reportError(errMesg[23], "", ast.position);
    }
    return null;
  }

  public Object visitForStmt(ForStmt ast, Object o) {
    // todo
    // "*21: for conditional is not boolean"
    Type e1Type = (Type)ast.E1.visit(this, o);
    Type e2Type = (Type)ast.E2.visit(this, o);
    Type e3Type = (Type)ast.E3.visit(this, o);
    if (!e2Type.isBooleanType()) {
      reporter.reportError(errMesg[21]+" (found: %)", e2Type.toString(), ast.E2.position);
    }
    ast.S.visit(this, o);
    return null;
  }

  public Object visitWhileStmt(WhileStmt ast, Object o) {
    // todo 
    Type eType = (Type)ast.E.visit(this, o);
    if (!eType.isBooleanType()) {
      reporter.reportError(errMesg[22]+" (found: %)", eType.toString(), ast.E.position);
    }
    Boolean isWhile = new Boolean(true);
    ast.S.visit(this, isWhile);
    return null;
  }

  public Object visitIfStmt(IfStmt ast, Object o) {
    // todo
    Type eType = (Type)ast.E.visit(this, o);
    if (!eType.isBooleanType()) {
      reporter.reportError(errMesg[20]+" (found: %)", eType.toString(), ast.E.position);
    }
    ast.S1.visit(this, o);
    ast.S2.visit(this, o);
    return null;
  }



  public Object visitEmptyStmt(EmptyStmt ast, Object o) {
    return null;
  }

  public Object visitEmptyStmtList(EmptyStmtList ast, Object o) {
    return null;
  }

  // Expressions

  // Returns the Type denoting the type of the expression. Does
  // not use the given object.

  // change the return type
  public Object visitEmptyExprList(EmptyExprList ast, Object o) {
    // todo
    return null;
  }

  public Type visitEmptyExpr(EmptyExpr ast, Object o) {
    ast.type = StdEnvironment.errorType;
    return ast.type;      
  }

  public Type visitBooleanExpr(BooleanExpr ast, Object o) {
    ast.type = StdEnvironment.booleanType;
    return ast.type;
  }

  public Type visitIntExpr(IntExpr ast, Object o) {
    ast.type = StdEnvironment.intType;
    return ast.type;
  }

  public Type visitFloatExpr(FloatExpr ast, Object o) {
    ast.type = StdEnvironment.floatType;
    return ast.type;
  }

  public Type visitStringExpr(StringExpr ast, Object o) {
    ast.type = StdEnvironment.stringType;
    return ast.type;
  }

  public Type visitVarExpr(VarExpr ast, Object o) {
      ast.type = (Type)ast.V.visit(this, ast);
    return ast.type;
  }

  public Type visitAssignExpr(AssignExpr ast, Object o) {
    // todo: implementation
    // type coercion
    // invalid lvalue
    // incompatible assigment
    if (!(ast.E1 instanceof VarExpr)&&!(ast.E1 instanceof ArrayExpr)) {
      // invalid lvalue
      reporter.reportError(errMesg[7], "", ast.position);
      ast.type = StdEnvironment.errorType;
    }
    Type e1Type = (Type)ast.E1.visit(this, ast);
    Type e2Type = (Type)ast.E2.visit(this, null);
    if (e2Type.isErrorType() || e1Type.isErrorType()) {
      // one of expressions is error type
      ast.type = StdEnvironment.errorType;
    } else if (e1Type.assignable(e2Type)) {
      ast.type = e2Type;
    } else if (e1Type.isFloatType() && e2Type.isIntType()) {
      // type coercions
      ast.E2 = coercions(ast.E2);
      ast.type = StdEnvironment.floatType;
    } else {
      // incompatible assignment
      reporter.reportError(errMesg[6], "", ast.position);
      ast.type = StdEnvironment.errorType;
    }
    return ast.type;
  }

  public Type visitBinaryExpr(BinaryExpr ast, Object o) {
    // // todo: implementation
    //+  -  *  /  <  <=  >  >=  ==  != for integer
    // ==  !=  &&  ||  ! for boolean
    Type e1Type = (Type)ast.E1.visit(this, null);
    Type e2Type = (Type)ast.E2.visit(this, null);
    ast.O.visit(this, null);
    String op = ast.O.spelling;
    if (op.equals("+")|| op.equals("-")|| op.equals("*")|| op.equals("/")){
      if (e1Type.isIntType() && e2Type.isIntType()) {
        ast.O.spelling = "i" + ast.O.spelling;
        ast.type = StdEnvironment.intType;
      } else if (e1Type.isFloatType() && e2Type.isFloatType()){
        ast.O.spelling = "f" + ast.O.spelling;
        ast.type = StdEnvironment.floatType;
      } else if (e1Type.isFloatType() && e2Type.isIntType()){
        ast.O.spelling = "f" + ast.O.spelling;
        ast.E2 = coercions(ast.E2);
        ast.type = StdEnvironment.floatType;
      } else if (e1Type.isIntType() && e2Type.isFloatType()){
        ast.O.spelling = "f" + ast.O.spelling;
        ast.E1 = coercions(ast.E1);
        ast.type = StdEnvironment.floatType;
      } else {
        // imcompatible type for operator
        if (!e1Type.isErrorType() && !e2Type.isErrorType()) {
          reporter.reportError(errMesg[9]+": %", op, ast.position);
        }
        ast.type = StdEnvironment.errorType;
      }
    } else if (op.equals(">=")||op.equals(">")||op.equals("<=")||op.equals("<")||op.equals("==")||op.equals("!=")) {
      if (op.equals("==") || op.equals("!=")) {
        if (e1Type.isIntType() && e2Type.isIntType()) {
          ast.O.spelling = "i" + ast.O.spelling;
          ast.type = StdEnvironment.booleanType;
        } else if (e1Type.isFloatType() && e2Type.isFloatType()){
          ast.O.spelling = "f" + ast.O.spelling;
          ast.type = StdEnvironment.booleanType;
        } else if (e1Type.isBooleanType() && e2Type.isBooleanType()){
          ast.O.spelling = "i" + ast.O.spelling;
          ast.type = StdEnvironment.booleanType;
        } else {
          // imcompatible type for operator
          if (!e1Type.isErrorType() && !e2Type.isErrorType()) {
            reporter.reportError(errMesg[9], op, ast.position);
          }
          ast.type = StdEnvironment.errorType;
        }
      } else {
        if (e1Type.isIntType() && e2Type.isIntType()) {
          ast.O.spelling = "i" + ast.O.spelling;
          ast.type = StdEnvironment.booleanType;
        } else if (e1Type.isFloatType() && e2Type.isFloatType()){
          ast.O.spelling = "f" + ast.O.spelling;
          ast.type = StdEnvironment.booleanType;
        } else if (e1Type.isFloatType() && e2Type.isIntType()){
          ast.O.spelling = "f" + ast.O.spelling;
          ast.E2 = coercions(ast.E2);
          ast.type = StdEnvironment.booleanType;
        } else if (e1Type.isIntType() && e2Type.isFloatType()){
          ast.O.spelling = "f" + ast.O.spelling;
          ast.E1 = coercions(ast.E1);
          ast.type = StdEnvironment.booleanType;
        } else {
          // imcompatible type for operator
          if (!e1Type.isErrorType() && !e2Type.isErrorType()) {
            reporter.reportError(errMesg[9], op, ast.position);
          }
          ast.type = StdEnvironment.errorType;
        }
      }
    } else if (op.equals("&&")||op.equals("||")) {
      if (e1Type.isBooleanType() && e2Type.isBooleanType()) {
        ast.O.spelling = "i" + ast.O.spelling;
        ast.type = StdEnvironment.booleanType;
      } else {
        if (!e1Type.isErrorType() && !e2Type.isErrorType()) {
          reporter.reportError(errMesg[9], op, ast.position);
        }
        ast.type = StdEnvironment.errorType;
      }
    }
    return ast.type;
  }

  public Type visitCallExpr(CallExpr ast, Object o) {
    // todo: implementation
    // many/few/wrong parameter
    Decl bind = (Decl)ast.I.visit(this, null);
    if (bind == null) {
      // ident undecl
      reporter.reportError(errMesg[5]+": %", ast.I.spelling, ast.position);
      ast.type = StdEnvironment.errorType;
    } else if (!bind.isFuncDecl()) {
      // which is not a function decl
      reporter.reportError(errMesg[19]+": %", ast.I.spelling, ast.I.position);
      ast.type = StdEnvironment.errorType;
    } else {
      // return type of its decl
      ast.type = (Type)bind.T.visit(this, null);
    } 
    if (!ast.type.isErrorType()) {
      // error
      ast.AL.visit(this, ((FuncDecl)bind).PL);
    }
    // return call's return value type
    return ast.type;
  }

  public Type visitUnaryExpr(UnaryExpr ast, Object o) {
    // todo: implementation
    // incompatible unary operator
    ast.O.visit(this, null);
    Type eType = (Type)ast.E.visit(this, null);
    String op = ast.O.spelling;
    if (op.equals("!")) {
      if (eType.isBooleanType()) {
        ast.O.spelling = "i" + ast.O.spelling;
        ast.type = StdEnvironment.booleanType;
      } else {
        if (!eType.isErrorType()) {
          reporter.reportError(errMesg[10]+": %", op, ast.position);
        }
        ast.type = StdEnvironment.errorType;
      }
    } else {
      if (!eType.isFloatType() && !eType.isIntType()) {
        if (!eType.isErrorType()) {
          reporter.reportError(errMesg[10]+": %", op, ast.position);
        }
        ast.type = StdEnvironment.errorType;
      } else {
        if (eType.isIntType()) {
          ast.O.spelling = "i" + ast.O.spelling;
          ast.type = StdEnvironment.intType;
        } else {
          ast.O.spelling = "f" + ast.O.spelling;
          ast.type = StdEnvironment.floatType;
        }
      }
    }
    return null;
  }

  public Type visitArrayExpr(ArrayExpr ast, Object o) {
    // todo: implementation
    Type eType = (Type)ast.E.visit(this, null);
    if (!eType.isIntType()) {
      // array subscript is not an integer
      reporter.reportError(errMesg[17], "", ast.position);
      ast.type = StdEnvironment.errorType;
    }
      // in assignment expression
    ast.type = (Type)ast.V.visit(this, ast);
    ast.type = (Type)ast.V.visit(this, null);
    return ast.type;
  }

  public Type visitInitExpr(InitExpr ast, Object o) {
    // todo: implementation
    InitHelper initHelper = (InitHelper)o;
    ArrayType arrType = initHelper.arrType;
    ast.IL.visit(this, initHelper);
    if (arrType.E.isEmptyExpr()) {
      IntLiteral iL = new IntLiteral(Integer.toString(initHelper.indexCounter), dummyPos);
      IntExpr iE = new IntExpr(iL, dummyPos);
      arrType.E = iE;
    }
    if (initHelper.indexLimit > 0 && initHelper.indexCounter >= initHelper.indexLimit) {
      reporter.reportError(errMesg[16]+": %", initHelper.ident, ast.parent.position);
    }
    return null;
  }

  public Object visitExprList(ExprList ast, Object o) {
    // todo: implementation
    // object o is a helper to locate index for expr list
    InitHelper initHelper = (InitHelper)o;
    ArrayType aType = initHelper.arrType;
    Type eType = (Type)ast.E.visit(this, null);
    ast.index = initHelper.indexCounter++;
    if (!eType.equals(aType.T)) {
      // wrong type element in initialiser
      if (eType.isIntType() && aType.T.isFloatType()){
        ast.E = coercions(ast.E);
      } else {
        reporter.reportError(errMesg[13]+": %", "at position "+ast.index, ast.E.position);
      }
      // return null; // terminate visit expr list
    }
    ast.EL.visit(this, o);
    return null;
  }
  
  // Declarations

  // Always returns null. Does not use the given object.

  public Object visitFuncDecl(FuncDecl ast, Object o) {
    // idTable.insert (ast.I.spelling, ast); 
    declareVariable(ast.I, ast);  // check redecl
    idTable.openScope();
    // Your code goes here
    Type type = (Type)ast.T.visit(this, ast); // type visit
    ast.PL.visit(this, ast);    // parameter visit
    // HINT
    // Pass ast as the 2nd argument (as done below) so that the
    // formal parameters of the function can be extracted from ast when the
    // function body is later visited
    // ast.I.visit(this, null); // call visitIdent
    ast.S.visit(this, ast); //compound stmt visit
    if (ast.I.spelling.equals("main")){
      ((Response)o).hasMain = true;
      if (ast.T.isIntType()) {
        // main's return type is not int
        ((Response)o).isInt = true;
      }
    }
    idTable.closeScope();
    return null;
  }

  public Object visitDeclList(DeclList ast, Object o) { // get boolean object from visitProg
    ast.D.visit(this, o);
    ast.DL.visit(this, o);
    return null;
  }

  public Object visitEmptyDeclList(EmptyDeclList ast, Object o) {
    return null;
  }

  public Object visitGlobalVarDecl(GlobalVarDecl ast, Object o) {
    // Id cannot decl as void
    declareVariable(ast.I, ast);  // check redecl
    if (ast.T.isVoidType()) {
      reporter.reportError(errMesg[3]+": %", ast.I.spelling, ast.I.position);
    } else if (!ast.T.isArrayType() && ast.E instanceof InitExpr) {
      // array initialiser for scala
      reporter.reportError(errMesg[14], "", ast.E.position);
    } else if (ast.T.isArrayType() && (ast.E instanceof InitExpr)){
      InitHelper initHelper = new InitHelper(ast.T, ast.I);
      ast.E.visit(this, initHelper);  // check expr, decorate arraytype
      ast.T.visit(this, ast.I);  // check identifer
    } else {
      ast.E.visit(this, null); // check expr, decorate arraytype
      ast.T.visit(this, ast.I);  // check identifer
    }
    // fill the rest
    return null; // not sure yet
  }

  public Object visitLocalVarDecl(LocalVarDecl ast, Object o) {
    declareVariable(ast.I, ast);  // check redecl
    if (ast.T.isVoidType()) {
      reporter.reportError(errMesg[3]+": %", ast.I.spelling, ast.I.position);
    } else if (!ast.T.isArrayType() && ast.E instanceof InitExpr) {
      // array initialiser for scala
      reporter.reportError(errMesg[14], "", ast.E.position);
    } else if (ast.T.isArrayType() && !(ast.E instanceof InitExpr)) {
      // array init should have an initexpr
      reporter.reportError(errMesg[15]+": %", ast.I.spelling, ast.position);
    } else if (ast.T.isArrayType() && (ast.E instanceof InitExpr)){
      InitHelper initHelper = new InitHelper(ast.T, ast.I);
      ast.E.visit(this, initHelper);  // check expr, decorate arraytype
      ast.T.visit(this, ast.I);  // check identifer
    } else {
      ast.E.visit(this, null); // check expr, decorate arraytype
      ast.T.visit(this, ast.I);  // check identifer
    }
    // fill the rest
    return null;  // return type of this decl
  }

  // Parameters

 // Always returns null. Does not use the given object.

  public Object visitParaList(ParaList ast, Object o) {
    ast.P.visit(this, null);
    ast.PL.visit(this, null);
    return null;
  }

  public Object visitParaDecl(ParaDecl ast, Object o) {
     declareVariable(ast.I, ast);

    if (ast.T.isVoidType()) {
      reporter.reportError(errMesg[3] + ": %", ast.I.spelling, ast.I.position);
    } else if (ast.T.isArrayType()) {
     if (((ArrayType) ast.T).T.isVoidType())
        reporter.reportError(errMesg[4] + ": %", ast.I.spelling, ast.I.position);
    }
    return null;
  }

  public Object visitEmptyParaList(EmptyParaList ast, Object o) {
    return null;
  }

  // Arguments

  // Your visitor methods for arguments go here


  public Object visitArg(Arg ast, Object o) {
    // Todo: implementation
    // object o is corresponding parameter
    ParaDecl paraDecl = (ParaDecl)o;
    Type argType = (Type)ast.E.visit(this, null); // visitXXExpr, return expression's type
    // Type paraType = (Type)paraDecl.T.visit(this, paraDecl.I); //visitXXType, return type
    if (argType.isIntType() && paraDecl.T.isFloatType()) {
      ast.E = coercions(ast.E);      
    } else if (!argType.equals(paraDecl.T) && !paraDecl.T.isArrayType()) {
      reporter.reportError(errMesg[27]+": %", paraDecl.I.spelling, ast.position);
    } else if (paraDecl.T.isArrayType() && !argType.equals(((ArrayType)paraDecl.T).T.visit(this, null))) {
      reporter.reportError(errMesg[27]+": %", paraDecl.I.spelling, ast.position);
    }
    return null;
  }

  public Object visitArgList(ArgList ast, Object o) {
    // todo
    // object o is corresponding paralist
    ParaList fpl = (ParaList)o;
    ast.A.visit(this, fpl.P);
    if (!fpl.PL.isEmptyParaList()){
      ast.AL.visit(this, fpl.PL);
    } else if (!ast.AL.isEmptyArgList() && fpl.PL.isEmptyParaList()) {
      // too many args
      reporter.reportError(errMesg[25], "", ast.AL.position);
    }
    return null;
  }

  public Object visitEmptyArgList(EmptyArgList ast, Object o) {
    // todo
    ParaList fpl = (ParaList)o;
    if (!fpl.PL.isEmptyParaList()) {
      reporter.reportError(errMesg[26], "", ast.position);
    }
    return null;
  }
  // Types 

  // Returns the type predefined in the standard environment. 

  public Object visitErrorType(ErrorType ast, Object o) {
    return StdEnvironment.errorType;
  }

  public Object visitBooleanType(BooleanType ast, Object o) {
    return StdEnvironment.booleanType;
  }

  public Object visitIntType(IntType ast, Object o) {
    return StdEnvironment.intType;
  }

  public Object visitFloatType(FloatType ast, Object o) {
    return StdEnvironment.floatType;
  }

  public Object visitStringType(StringType ast, Object o) {
    return StdEnvironment.stringType;
  }

  public Object visitVoidType(VoidType ast, Object o) {
    return StdEnvironment.voidType;
  }

  public Object visitArrayType(ArrayType ast, Object o) {
    // todo: implementation
    // object o is ident of array type
    Ident id = (Ident)o;
    Type eType = (Type)ast.E.visit(this, null);
    Type rType = (Type)ast.T.visit(this, null);
    if (rType.isVoidType()) {
      reporter.reportError(errMesg[4]+": %", id.spelling, id.position); // not allow: void a[]
      rType = StdEnvironment.errorType;
    }
    if (!eType.isIntType() && !eType.isErrorType()) {
      // array subscript
      reporter.reportError(errMesg[17], "", ast.E.position);
      return StdEnvironment.errorType;
    } else if (eType.isErrorType()) {
      // array size miss
      reporter.reportError(errMesg[18]+": %", id.spelling, id.position);
      return StdEnvironment.errorType;
    } else {
      return rType;
    }
  }

  // Variables
  public Object visitSimpleVar(SimpleVar ast, Object o) {
    // todo: implementation
    Decl bind = (Decl)ast.I.visit(this, null);
    if (bind == null) {
      // undecl
      reporter.reportError(errMesg[5]+": %", ast.I.spelling, ast.position);
      ast.type = StdEnvironment.errorType;
    } else if (bind.isFuncDecl()) {
      // use function as scalar
      reporter.reportError(errMesg[11]+": %", ast.I.spelling, ast.I.position);
      ast.type = StdEnvironment.errorType;
    } else if ((o instanceof ArrayExpr) && !(bind.T.isArrayType())) {
      // use scalar as array
      ArrayExpr aExpr = (ArrayExpr)o;
      reporter.reportError(errMesg[12], ast.I.spelling, aExpr.position);
      ast.type = StdEnvironment.errorType;
    } else if ((o instanceof VarExpr) && bind.T.isArrayType() && !(((VarExpr)o).parent instanceof Arg)){
      // use scalar as array
      reporter.reportError(errMesg[11]+": %", ast.I.spelling, ast.I.position);
      ast.type = StdEnvironment.errorType;
    } else {
      // ast.type = bind.T;
      if (bind.T.isArrayType()) {
        ast.type = (Type)((ArrayType)bind.T).T.visit(this, null);
      } else {
        ast.type = (Type)bind.T.visit(this, null);
      }
    }
    return ast.type;
  }
  // Literals, Identifiers and Operators

  // return a pointer to its decl
  public Object visitIdent(Ident I, Object o) {
    Decl binding = idTable.retrieve(I.spelling);
    if (binding != null)
      I.decl = binding;
    // else  // temp place here
    //   reporter.reportError(errMesg[5]+": %", I.spelling, I.position);
    return binding;
  }

  public Object visitBooleanLiteral(BooleanLiteral SL, Object o) {
    return StdEnvironment.booleanType;
  }

  public Object visitIntLiteral(IntLiteral IL, Object o) {
    return StdEnvironment.intType;
  }

  public Object visitFloatLiteral(FloatLiteral IL, Object o) {
    return StdEnvironment.floatType;
  }

  public Object visitStringLiteral(StringLiteral IL, Object o) {
    return StdEnvironment.stringType;
  }

  public Object visitOperator(Operator O, Object o) {
    return null;
  }



  // Creates a small AST to represent the "declaration" of each built-in
  // function, and enters it in the symbol table.

  private FuncDecl declareStdFunc (Type resultType, String id, List pl) {

    FuncDecl binding;

    binding = new FuncDecl(resultType, new Ident(id, dummyPos), pl, 
           new EmptyStmt(dummyPos), dummyPos);
    idTable.insert (id, binding);
    return binding;
  }

  // Creates small ASTs to represent "declarations" of all 
  // build-in functions.
  // Inserts these "declarations" into the symbol table.

  private final static Ident dummyI = new Ident("x", dummyPos);

  private void establishStdEnvironment () {

    // Define four primitive types
    // errorType is assigned to ill-typed expressions

    StdEnvironment.booleanType = new BooleanType(dummyPos);
    StdEnvironment.intType = new IntType(dummyPos);
    StdEnvironment.floatType = new FloatType(dummyPos);
    StdEnvironment.stringType = new StringType(dummyPos);
    StdEnvironment.voidType = new VoidType(dummyPos);
    StdEnvironment.errorType = new ErrorType(dummyPos);

    // enter into the declarations for built-in functions into the table

    StdEnvironment.getIntDecl = declareStdFunc( StdEnvironment.intType,
	"getInt", new EmptyParaList(dummyPos)); 
    StdEnvironment.putIntDecl = declareStdFunc( StdEnvironment.voidType,
	"putInt", new ParaList(
	new ParaDecl(StdEnvironment.intType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 
    StdEnvironment.putIntLnDecl = declareStdFunc( StdEnvironment.voidType,
	"putIntLn", new ParaList(
	new ParaDecl(StdEnvironment.intType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 
    StdEnvironment.getFloatDecl = declareStdFunc( StdEnvironment.floatType,
	"getFloat", new EmptyParaList(dummyPos)); 
    StdEnvironment.putFloatDecl = declareStdFunc( StdEnvironment.voidType,
	"putFloat", new ParaList(
	new ParaDecl(StdEnvironment.floatType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 
    StdEnvironment.putFloatLnDecl = declareStdFunc( StdEnvironment.voidType,
	"putFloatLn", new ParaList(
	new ParaDecl(StdEnvironment.floatType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 
    StdEnvironment.putBoolDecl = declareStdFunc( StdEnvironment.voidType,
	"putBool", new ParaList(
	new ParaDecl(StdEnvironment.booleanType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 
    StdEnvironment.putBoolLnDecl = declareStdFunc( StdEnvironment.voidType,
	"putBoolLn", new ParaList(
	new ParaDecl(StdEnvironment.booleanType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 

    StdEnvironment.putStringLnDecl = declareStdFunc( StdEnvironment.voidType,
	"putStringLn", new ParaList(
	new ParaDecl(StdEnvironment.stringType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 

    StdEnvironment.putStringDecl = declareStdFunc( StdEnvironment.voidType,
	"putString", new ParaList(
	new ParaDecl(StdEnvironment.stringType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 

    StdEnvironment.putLnDecl = declareStdFunc( StdEnvironment.voidType,
	"putLn", new EmptyParaList(dummyPos));

  }


}
