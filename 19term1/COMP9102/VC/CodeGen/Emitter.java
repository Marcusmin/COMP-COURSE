/*
 * Emitter.java 
 * Sun  7 Apr 23:21:06 AEST 2019
 * Jingling Xue, School of Computer Science, UNSW, Australia
 */

// A new frame object is created for every function just before the
// function is being translated in visitFuncDecl.
//
// All the information about the translation of a function should be
// placed in this Frame object and passed across the AST nodes as the
// 2nd argument of every visitor method in Emitter.java.

package VC.CodeGen;

import java.util.LinkedList;
import java.util.Enumeration;
import java.util.ListIterator;

import VC.ASTs.*;
import VC.ErrorReporter;
import VC.StdEnvironment;

public final class Emitter implements Visitor {

  private ErrorReporter errorReporter;
  private String inputFilename;
  private String classname;
  private String outputFilename;

  public Emitter(String inputFilename, ErrorReporter reporter) {
    this.inputFilename = inputFilename;
    errorReporter = reporter;
    
    int i = inputFilename.lastIndexOf('.');
    if (i > 0)
      classname = inputFilename.substring(0, i);
    else
      classname = inputFilename;
    
  }

  // PRE: ast must be a Program node

  public final void gen(AST ast) {
    ast.visit(this, null); 
    JVM.dump(classname + ".j");
  }
    
  // Programs
  public Object visitProgram(Program ast, Object o) {
     /** This method works for scalar variables only. You need to modify
         it to handle all array-related declarations and initialisations.
      **/ 

    // Generates the default constructor initialiser 
    emit(JVM.CLASS, "public", classname);
    emit(JVM.SUPER, "java/lang/Object");

    emit("");

    // Three subpasses:

    // (1) Generate .field definition statements since
    //     these are required to appear before method definitions
    List list = ast.FL;
    while (!list.isEmpty()) {
      DeclList dlAST = (DeclList) list;
      if (dlAST.D instanceof GlobalVarDecl) {
        GlobalVarDecl vAST = (GlobalVarDecl) dlAST.D;
        emit(JVM.STATIC_FIELD, vAST.I.spelling, VCtoJavaType(vAST.T));
        }
      list = dlAST.DL;
    }

    emit("");

    // (2) Generate <clinit> for global variables (assumed to be static)
 
    emit("; standard class static initializer ");
    emit(JVM.METHOD_START, "static <clinit>()V");
    emit("");

    // create a Frame for <clinit>

    Frame frame = new Frame(false);

    list = ast.FL;
    while (!list.isEmpty()) {
      DeclList dlAST = (DeclList) list;
      if (dlAST.D instanceof GlobalVarDecl) {
        GlobalVarDecl vAST = (GlobalVarDecl) dlAST.D;
        if (!vAST.E.isEmptyExpr() && !vAST.T.isArrayType()) {
          vAST.E.visit(this, frame);
        } else if (!vAST.T.isArrayType()){
          // init global var with 0
          if (vAST.T.equals(StdEnvironment.floatType))
            emit(JVM.FCONST_0);
          else
            emit(JVM.ICONST_0);
          frame.push();
        } else {
          //call array type visitor
          // handle init expr
          vAST.T.visit(this, frame);
          vAST.E.visit(this, frame);
        }
        emitPUTSTATIC(VCtoJavaType(vAST.T), vAST.I.spelling); 
        frame.pop();
      }
      list = dlAST.DL;
    }
   
    emit("");
    emit("; set limits used by this method");
    emit(JVM.LIMIT, "locals", frame.getNewIndex());

    emit(JVM.LIMIT, "stack", frame.getMaximumStackSize());
    emit(JVM.RETURN);
    emit(JVM.METHOD_END, "method");

    emit("");

    // (3) Generate Java bytecode for the VC program

    emit("; standard constructor initializer ");
    emit(JVM.METHOD_START, "public <init>()V");
    emit(JVM.LIMIT, "stack 1");
    emit(JVM.LIMIT, "locals 1");
    emit(JVM.ALOAD_0);
    emit(JVM.INVOKESPECIAL, "java/lang/Object/<init>()V");
    emit(JVM.RETURN);
    emit(JVM.METHOD_END, "method");

    return ast.FL.visit(this, o);
  }

  // Statements

  public Object visitStmtList(StmtList ast, Object o) {
    ast.S.visit(this, o);
    ast.SL.visit(this, o);
    return null;
  }

  public Object visitCompoundStmt(CompoundStmt ast, Object o) {
    Frame frame = (Frame) o; 

    String scopeStart = frame.getNewLabel();
    String scopeEnd = frame.getNewLabel();
    frame.scopeStart.push(scopeStart);
    frame.scopeEnd.push(scopeEnd);
   
    emit(scopeStart + ":");
    if (ast.parent instanceof FuncDecl) {
      if (((FuncDecl) ast.parent).I.spelling.equals("main")) {
        emit(JVM.VAR, "0 is argv [Ljava/lang/String; from " + (String) frame.scopeStart.peek() + " to " +  (String) frame.scopeEnd.peek());
        emit(JVM.VAR, "1 is vc$ L" + classname + "; from " + (String) frame.scopeStart.peek() + " to " +  (String) frame.scopeEnd.peek());
        // Generate code for the initialiser vc$ = new classname();
        emit(JVM.NEW, classname);
        emit(JVM.DUP);
        frame.push(2);
        emit("invokenonvirtual", classname + "/<init>()V");
        frame.pop();
        emit(JVM.ASTORE_1);
        frame.pop();
      } else {
        emit(JVM.VAR, "0 is this L" + classname + "; from " + (String) frame.scopeStart.peek() + " to " +  (String) frame.scopeEnd.peek());
        ((FuncDecl) ast.parent).PL.visit(this, o);
      }
    }
    ast.DL.visit(this, o);
    ast.SL.visit(this, o);
    emit(scopeEnd + ":");

    frame.scopeStart.pop();
    frame.scopeEnd.pop();
    return null;
  }
  
  public Object visitIfStmt(IfStmt ast, Object o) {
    Frame frame = (Frame) o;
    String falseLabel = frame.getNewLabel();
    String nextLabel = null;
    if (!ast.S2.isEmptyStmt()) {
      nextLabel = frame.getNewLabel();
    }
    ast.E.visit(this, o);
    emit(JVM.IFEQ, falseLabel);
    frame.pop();
    ast.S1.visit(this, o);
    if (ast.S2.isEmptyStmt()) {
      emit(falseLabel+":");
      ast.S2.visit(this, o);
    } else {
      emit(JVM.GOTO, nextLabel);
      emit(falseLabel+":");
      ast.S2.visit(this, o);
      emit(nextLabel+":");
    }
    return null;
  }

  public Object visitForStmt(ForStmt ast, Object o) {
    Frame frame = (Frame) o;
    String startLabel = frame.getNewLabel();
    String falseLabel = frame.getNewLabel();
    frame.conStack.push(startLabel);
    frame.brkStack.push(falseLabel);
    ast.E1.visit(this, o);  // init
    emit(startLabel+":");
    ast.E2.visit(this, o);
    emit(JVM.IFEQ, falseLabel);
    frame.pop();
    ast.S.visit(this, o);
    ast.E3.visit(this, o);
    emit(JVM.GOTO, startLabel);
    emit(falseLabel+":");
    frame.conStack.pop();
    frame.brkStack.pop();
    return null;
  }

  public Object visitWhileStmt(WhileStmt ast, Object o) {
    Frame frame = (Frame) o;
    String startLabel = frame.getNewLabel();
    String falseLabel = frame.getNewLabel();
    frame.conStack.push(startLabel);
    frame.brkStack.push(falseLabel);
    emit(startLabel+":");
    ast.E.visit(this, o);
    emit(JVM.IFEQ, falseLabel);
    frame.pop();
    ast.S.visit(this, o);
    emit(JVM.GOTO, startLabel);
    emit(falseLabel+":");
    frame.conStack.pop();
    frame.brkStack.pop();
    
    return null;
  }

  public Object visitContinueStmt(ContinueStmt ast, Object o) {
    Frame frame = (Frame) o;
    String startLabel = frame.conStack.peek();
    emit(JVM.GOTO, startLabel);
    return null;
  }

  public Object visitBreakStmt(BreakStmt ast, Object o) {
    Frame frame = (Frame) o;
    String falseLabel = frame.brkStack.peek();
    emit(JVM.GOTO, falseLabel);
    return null;
  }

  // must be public 
  public Object visitReturnStmt(ReturnStmt ast, Object o) {
    Frame frame = (Frame)o;

/*
  int main() { return 0; } must be interpretted as 
  public static void main(String[] args) { return ; }
  Therefore, "return expr", if present in the main of a VC program
  must be translated into a RETURN rather than IRETURN instruction.
*/

     if (frame.isMain())  {
        emit(JVM.RETURN);
        return null;
     }
// Your other code goes here
    ast.E.visit(this, o);
    if (ast.E.isEmptyExpr()) {
      emit(JVM.RETURN);
    } else if (ast.E.type.equals(StdEnvironment.booleanType)) {
      emit(JVM.IRETURN);
    } else if (ast.E.type.equals(StdEnvironment.intType)) {
      emit(JVM.IRETURN);
    } else if (ast.E.type.equals(StdEnvironment.floatType)) {
      emit(JVM.FRETURN);
    }
    frame.pop();
     return null;
  }

  public Object visitExprStmt(ExprStmt ast, Object o) {
    Frame frame = (Frame) o;
    ast.E.visit(this, o);
    if (ast.E.isEmptyExpr()) {
      // no pop
    } else if (ast.E instanceof CallExpr) {
      // void no pop, otherwise pop
      Ident func = (Ident)((CallExpr) ast.E).I;
      FuncDecl funcDecl = (FuncDecl)func.decl;
      if (!funcDecl.T.isVoidType()) {
        emit(JVM.POP);
        frame.pop();
      }
    } else if (ast.E instanceof AssignExpr){
      // no need pop
    } else {
      // pop
      emit(JVM.POP);
      frame.pop();
    }
    return null;
  }

  public Object visitEmptyStmtList(EmptyStmtList ast, Object o) {
    return null;
  }

  public Object visitEmptyCompStmt(EmptyCompStmt ast, Object o) {
    return null;
  }

  public Object visitEmptyStmt(EmptyStmt ast, Object o) {
    return null;
  }

  // Expressions

  public Object visitCallExpr(CallExpr ast, Object o) {
    Frame frame = (Frame) o;
    String fname = ast.I.spelling;

    if (fname.equals("getInt")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System.getInt()I");
      frame.push();
    } else if (fname.equals("putInt")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System.putInt(I)V");
      frame.pop();
    } else if (fname.equals("putIntLn")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System/putIntLn(I)V");
      frame.pop();
    } else if (fname.equals("getFloat")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System/getFloat()F");
      frame.push();
    } else if (fname.equals("putFloat")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System/putFloat(F)V");
      frame.pop();
    } else if (fname.equals("putFloatLn")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System/putFloatLn(F)V");
      frame.pop();
    } else if (fname.equals("putBool")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System/putBool(Z)V");
      frame.pop();
    } else if (fname.equals("putBoolLn")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System/putBoolLn(Z)V");
      frame.pop();
    } else if (fname.equals("putString")) {
      ast.AL.visit(this, o);
      emit(JVM.INVOKESTATIC, "VC/lang/System/putString(Ljava/lang/String;)V");
      frame.pop();
    } else if (fname.equals("putStringLn")) {
      ast.AL.visit(this, o);
      emit(JVM.INVOKESTATIC, "VC/lang/System/putStringLn(Ljava/lang/String;)V");
      frame.pop();
    } else if (fname.equals("putLn")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System/putLn()V");
    } else { // programmer-defined functions

      FuncDecl fAST = (FuncDecl) ast.I.decl;

      // all functions except main are assumed to be instance methods
      if (frame.isMain()) 
        emit("aload_1"); // vc.funcname(...)
      else
        emit("aload_0"); // this.funcname(...)
      frame.push();

      ast.AL.visit(this, o);
    
      String retType = VCtoJavaType(fAST.T);
      
      // The types of the parameters of the called function are not
      // directly available in the FuncDecl node but can be gathered
      // by traversing its field PL.

      StringBuffer argsTypes = new StringBuffer("");
      List fpl = fAST.PL;
      int argc = 0;
      while (! fpl.isEmpty()) {
        if (((ParaList) fpl).P.T.equals(StdEnvironment.booleanType)) {
          argsTypes.append("Z");    
          argc++;     
        }
        else if (((ParaList) fpl).P.T.equals(StdEnvironment.intType)) {
          argsTypes.append("I");         
          argc++;
        }
        else if (((ParaList) fpl).P.T.equals(StdEnvironment.floatType)){
          argsTypes.append("F");         
          argc++;
        } else if (((ParaList) fpl).P.T.isArrayType()){
          argsTypes.append("[");
          ArrayType arrType = (ArrayType)((ParaList) fpl).P.T;
          if (arrType.T.equals(StdEnvironment.booleanType)) {
            argsTypes.append("Z");
            argc++;
          } else if (arrType.T.equals(StdEnvironment.intType)) {
            argsTypes.append("I");
            argc++;
          } else {
            argsTypes.append("F");
            argc++;
          }
        }
        fpl = ((ParaList) fpl).PL;
      }
      
      emit("invokevirtual", classname + "/" + fname + "(" + argsTypes + ")" + retType);
      frame.pop(argc + 1); // plus one for ref

      if (! retType.equals("V"))
        frame.push();
    }
    return null;
  }

  public Object visitEmptyExpr(EmptyExpr ast, Object o) {
    return null;
  }

  public Object visitExprList(ExprList ast, Object o) {
    Frame frame = (Frame) o;
    emit(JVM.DUP);
    emitICONST(ast.index);
    frame.push(2);
    ast.E.visit(this, o);
    if (ast.E.type.equals(StdEnvironment.intType)) {
      emit(JVM.IASTORE);
    } else if (ast.E.type.equals(StdEnvironment.booleanType)){
      emit(JVM.BASTORE);
    } else {
      emit(JVM.FASTORE);
    }
    frame.pop(3);
    ast.EL.visit(this, o);
    return null;
  }

  public Object visitEmptyExprList(EmptyExprList ast, Object o) {
    return null;
  }

  public Object visitIntExpr(IntExpr ast, Object o) {
    ast.IL.visit(this, o);
    return null;
  }

  public Object visitFloatExpr(FloatExpr ast, Object o) {
    ast.FL.visit(this, o);
    return null;
  }

  public Object visitBooleanExpr(BooleanExpr ast, Object o) {
    ast.BL.visit(this, o);
    return null;
  }

  public Object visitInitExpr(InitExpr ast, Object o) {
    ast.IL.visit(this, o);
    return null;
  }

  public Object visitBinaryExpr(BinaryExpr ast, Object o) {
    // ast.E1.visit(this, o);
    // ast.E2.visit(this, o);
    // int operation = 0;
    String op = (String)ast.O.visit(this, o);
    Frame frame = (Frame) o;
    if (op.equals("i+")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitIARITHMETIC(op, frame);
    } else if (op.equals("i-")) {  // Arithmetic operator
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitIARITHMETIC(op, frame);
    } else if (op.equals("i*")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitIARITHMETIC(op, frame);
    } else if (op.equals("i/")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitIARITHMETIC(op, frame);
    } else if (op.equals("f+")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitFARITHMETIC(op, frame);
    } else if (op.equals("f-")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitFARITHMETIC(op, frame);
    } else if (op.equals("f*")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitFARITHMETIC(op, frame);
    } else if (op.equals("f/")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitFARITHMETIC(op, frame);
    } else if (op.equals("i&&")) {  //logical operators
      String L1 = frame.getNewLabel();
      String L2 = frame.getNewLabel();
      ast.E1.visit(this, o);
      emit(JVM.IFEQ, L1);
      ast.E2.visit(this, o);
      emit(JVM.IFEQ, L1);
      emitICONST(1);
      emit(JVM.GOTO,L2);
      emit(L1+":");
      emitICONST(0);
      emit(L2+":");
      frame.push(); // finally should be a value push in stack
    } else if (op.equals("i||")) {
      String L1 = frame.getNewLabel();
      String L2 = frame.getNewLabel();
      ast.E1.visit(this, o);
      emit(JVM.IFNE, L1);
      ast.E2.visit(this, o);
      emit(JVM.IFNE, L1);
      emitICONST(0);
      emit(JVM.GOTO, L2);
      emit(L1+":");
      emitICONST(1);
      emit(L2+":");
      frame.push();
    } else if (op.equals("i<")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitIF_ICMPCOND(op, frame);
    } else if (op.equals("i<=")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitIF_ICMPCOND(op, frame);
    } else if (op.equals("i>")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitIF_ICMPCOND(op, frame);
    } else if (op.equals("i>=")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitIF_ICMPCOND(op, frame);
    } else if (op.equals("f<")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitFCMP(op, frame);
    } else if (op.equals("f<=")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitFCMP(op, frame);
    } else if (op.equals("f>")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitFCMP(op, frame);
    } else if (op.equals("f>=")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitFCMP(op, frame);
    } else if (op.equals("f==")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitFCMP(op, frame);
    } else if (op.equals("f!=")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitFCMP(op, frame);
    } else if (op.equals("i!=")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitIF_ICMPCOND(op, frame);
    } else if (op.equals("i==")) {
      ast.E1.visit(this, o);
      ast.E2.visit(this, o);
      emitIF_ICMPCOND(op, frame);
    } else {
      // nothing to be done
    }
    return null;
  }

  public Object visitUnaryExpr(UnaryExpr ast, Object o) {
    ast.E.visit(this, o);
    String op = (String)ast.O.visit(this, o);
    Frame frame = (Frame) o;
    if (op.equals("i+")) {
      // nothing to be done
    } else if (op.equals("i-")) {
      emit(JVM.INEG);
    } else if (op.equals("i!")) {
      emitBCONST(true);
      frame.push();
      emit(JVM.IXOR);
      frame.pop(2);
      frame.push();
    } else if (op.equals("f+")) {
      // nothing to do
    } else if (op.equals("f-")) {
      emit(JVM.FNEG);
    } else if (op.equals("i2f")) {
      emit("i2f");
    } else {
      // nothing to be done
    }
    return null;
  }

  // assign expr is responsible for load & store
  public Object visitAssignExpr(AssignExpr ast, Object o) {
    Frame frame = (Frame) o;
    /**
     * If left hand side is an var expression, visitVarExpr will return
     * the ident of scalar
     * If left hand side is an array expression, visitArrayExpr will
     * push the ref into stack and evalutate the array's sub-expression
     * and return array's ident
     */
    Object LHS = ast.E1.visit(this, o); // lvalue must be a var or array
    if (ast.E2 instanceof VarExpr || ast.E2 instanceof ArrayExpr) {
      // ex. a = i or a = i[5]
      Ident RHS = (Ident)ast.E2.visit(this, o);
      Decl iDecl = (Decl)RHS.decl;
      int index = iDecl.index;
      if (RHS.decl instanceof LocalVarDecl || RHS.decl instanceof ParaDecl) {
        if (ast.E2 instanceof VarExpr) {
          if (ast.E2.type.equals(StdEnvironment.floatType)) {
            emitFLOAD(index);
          } else {
            emitILOAD(index);
          }
        } else {
          if (ast.E2.type.equals(StdEnvironment.floatType)) {
            emit(JVM.FALOAD);
          } else if (ast.E2.type.equals(StdEnvironment.booleanType)){
            emit(JVM.BALOAD);
          } else {
            emit(JVM.IALOAD);
          }
          frame.pop(2); // pop ref&&index
        }
        frame.push(); // load value into op stack
      } else {
        if (ast.E2 instanceof VarExpr) {
          emitGETSTATIC(VCtoJavaType(iDecl.T), RHS.spelling);
        } else {
          if (ast.E2.type.equals(StdEnvironment.floatType)) {
            emit(JVM.FALOAD);
          } else if (ast.E2.type.equals(StdEnvironment.booleanType)) {
            emit(JVM.BALOAD);
          } else {
            emit(JVM.IALOAD);
          }
          frame.pop(2); // same as above
        }
        frame.push();
      }
    } else {
      ast.E2.visit(this, o);
    }
    if (ast.parent instanceof AssignExpr) {
      // dup
      emit(JVM.DUP);
      frame.push();
    }
    // store instructions
    if (ast.E1 instanceof ArrayExpr) {
      // array store
      if (LHS instanceof Ident) {
        Ident id = (Ident) LHS;
        Decl iDecl = (Decl)id.decl;
        if (ast.E1.type.equals(StdEnvironment.floatType)) {
          emit(JVM.FASTORE);
        } else if (ast.E1.type.equals(StdEnvironment.booleanType)) {
          emit(JVM.BASTORE);
        } else {
          emit(JVM.IASTORE);
        }
        frame.pop(3); // pop ref&&index&&value
      }
    } else {  // if ast.E1 is varexpr, id is scalar
      if (LHS instanceof Ident) { // avoid runtime error
        Ident id = (Ident)LHS;
        if (id.decl instanceof LocalVarDecl) {
          Type type = ((LocalVarDecl) id.decl).T;
          if (type.equals(StdEnvironment.booleanType)) {
            emitISTORE(id);
          } else if (type.equals(StdEnvironment.intType)) {
            emitISTORE(id);
          } else {  // if (type.equals(StdEnvironment.floatType))
            emitFSTORE(id);
          }
          frame.pop();
        } else if (id.decl instanceof ParaDecl) {
          Type type = ((ParaDecl) id.decl).T;
          if (type.equals(StdEnvironment.booleanType)) {
            emitISTORE(id);
          } else if (type.equals(StdEnvironment.intType)) {
            emitISTORE(id);
          } else {  // if (type.equals(StdEnvironment.floatType))
            emitFSTORE(id);
          }
          frame.pop();
        } else {  //if (id.decl instanceof GlobalVarDecl) {
          // need to do something with global variable
          Type type = ((GlobalVarDecl) id.decl).T;
          emitPUTSTATIC(VCtoJavaType(type), id.spelling);
          frame.pop();
        }
      }
    } 
    return null;
  }

  public Object visitVarExpr(VarExpr ast, Object o) {
    Frame frame = (Frame) o;
    Ident id = (Ident)ast.V.visit(this, o);
    if (ast.parent instanceof AssignExpr) {
      // do nothing
    } else {
      // load into op stack
      if (id.decl instanceof LocalVarDecl || id.decl instanceof ParaDecl) {
        Decl decl = (Decl) id.decl;
        int index = decl.index;
        if (decl.T.equals(StdEnvironment.booleanType)) {
          emitILOAD(index);
        } else if (decl.T.equals(StdEnvironment.intType)) {
          emitILOAD(index);
        } else if (decl.T.equals(StdEnvironment.floatType)){
          emitFLOAD(index);
        } else {
          emitALOAD(index);
        }
        frame.push(); // push value in op stack
      } else {
        Decl decl = (Decl) id.decl;
        emitGETSTATIC(VCtoJavaType(decl.T), id.spelling);
        frame.push(); // push ref into stack
      }
    }
    return id;
  }

  public Object visitArrayExpr(ArrayExpr ast, Object o) {
    Ident id = (Ident)ast.V.visit(this, o);
    Frame frame = (Frame) o;
    if (id.decl instanceof LocalVarDecl || id.decl instanceof ParaDecl){
      // load array ref into op stack
      Decl decl = (Decl) id.decl;
      int index = decl.index;
      emitALOAD(index);
    } else {
      Decl decl = (Decl) id.decl;
      emitGETSTATIC(VCtoJavaType(decl.T), id.spelling);
      // problemetic here
    }
    frame.push(); // load reference into stack
    ast.E.visit(this, o);
    if (ast.parent instanceof AssignExpr) {
      // do nothing
    } else {
      // load value into op stack
      // we assume that array epression's type is their decl's type
      if (ast.type.equals(StdEnvironment.floatType)) {
        emit(JVM.FALOAD);
      } else if (ast.type.equals(StdEnvironment.booleanType)) {
        emit(JVM.BALOAD);
      } else{
        emit(JVM.IALOAD);
      }
      frame.pop(2); // pop ref & index
      frame.push(); // push value
    }
    return id;
  }

  public Object visitStringExpr(StringExpr ast, Object o) {
    ast.SL.visit(this, o);
    return null;
  }

  // Declarations

  public Object visitDeclList(DeclList ast, Object o) {
    ast.D.visit(this, o);
    ast.DL.visit(this, o);
    return null;
  }

  public Object visitEmptyDeclList(EmptyDeclList ast, Object o) {
    return null;
  }

  public Object visitFuncDecl(FuncDecl ast, Object o) {

    Frame frame;

    if (ast.I.spelling.equals("main")) {

       frame = new Frame(true);

      // Assume that main has one String parameter and reserve 0 for it
      frame.getNewIndex(); 

      emit(JVM.METHOD_START, "public static main([Ljava/lang/String;)V"); 
      // Assume implicitly that
      //      classname vc$; 
      // appears before all local variable declarations.
      // (1) Reserve 1 for this object reference.

      frame.getNewIndex(); 

    } else {

       frame = new Frame(false);

      // all other programmer-defined functions are treated as if
      // they were instance methods
      frame.getNewIndex(); // reserve 0 for "this"

      String retType = VCtoJavaType(ast.T);

      // The types of the parameters of the called function are not
      // directly available in the FuncDecl node but can be gathered
      // by traversing its field PL.

      StringBuffer argsTypes = new StringBuffer("");
      List fpl = ast.PL;
      while (! fpl.isEmpty()) {
        if (((ParaList) fpl).P.T.equals(StdEnvironment.booleanType)) {
          argsTypes.append("Z");         
        }
        else if (((ParaList) fpl).P.T.equals(StdEnvironment.intType)) {
          argsTypes.append("I");         
        }
        else if (((ParaList) fpl).P.T.equals(StdEnvironment.floatType)) {
          argsTypes.append("F");
        }
        else {
          if (((ParaList) fpl).P.T.isArrayType()) {
            ArrayType type = (ArrayType)((ParaList) fpl).P.T;
            Type arrType = type.T;
            if (arrType.equals(StdEnvironment.booleanType)) {
              argsTypes.append("[Z");
            } else if (arrType.equals(StdEnvironment.intType)) {
              argsTypes.append("[I");
            } else if (arrType.equals(StdEnvironment.floatType)) {
              argsTypes.append("[F");
            }
          }
        }
        fpl = ((ParaList) fpl).PL;
      }

      emit(JVM.METHOD_START, ast.I.spelling + "(" + argsTypes + ")" + retType);
    }

    ast.S.visit(this, frame);

    // JVM requires an explicit return in every method. 
    // In VC, a function returning void may not contain a return, and
    // a function returning int or float is not guaranteed to contain
    // a return. Therefore, we add one at the end just to be sure.

    if (ast.T.equals(StdEnvironment.voidType)) {
      emit("");
      emit("; return may not be present in a VC function returning void"); 
      emit("; The following return inserted by the VC compiler");
      emit(JVM.RETURN); 
    } else if (ast.I.spelling.equals("main")) {
      // In case VC's main does not have a return itself
      emit(JVM.RETURN);
    } else
      emit(JVM.NOP); 

    emit("");
    emit("; set limits used by this method");
    emit(JVM.LIMIT, "locals", frame.getNewIndex());

    emit(JVM.LIMIT, "stack", frame.getMaximumStackSize());
    emit(".end method");

    return null;
  }

  public Object visitGlobalVarDecl(GlobalVarDecl ast, Object o) {
    // nothing to be done
    return null;
  }

  public Object visitLocalVarDecl(LocalVarDecl ast, Object o) {
    Frame frame = (Frame) o;
    ast.index = frame.getNewIndex();
    String T = VCtoJavaType(ast.T);

    emit(JVM.VAR + " " + ast.index + " is " + ast.I.spelling + " " + T + " from " + (String) frame.scopeStart.peek() + " to " +  (String) frame.scopeEnd.peek());
    if (!ast.E.isEmptyExpr() && !ast.T.isArrayType()) {
      ast.E.visit(this, o);
      if (ast.T.equals(StdEnvironment.floatType)) {
        // cannot call emitFSTORE(ast.I) since this I is not an
        // applied occurrence 
        if (ast.index >= 0 && ast.index <= 3) 
          emit(JVM.FSTORE + "_" + ast.index); 
        else
          emit(JVM.FSTORE, ast.index); 
        frame.pop();
      } else if (ast.T.equals(StdEnvironment.booleanType)||
                  ast.T.equals(StdEnvironment.intType)){
        // cannot call emitISTORE(ast.I) since this I is not an
        // applied occurrence 
        if (ast.index >= 0 && ast.index <= 3) 
          emit(JVM.ISTORE + "_" + ast.index); 
        else
          emit(JVM.ISTORE, ast.index); 
        frame.pop();
      }
    } else if (ast.T.isArrayType() && ast.E.isEmptyExpr()) {
      ast.T.visit(this, o);
      if (ast.index >= 0 && ast.index <= 3) {
        emit(JVM.ASTORE+"_"+ast.index);
      } else {
        emit(JVM.ASTORE, ast.index);
      }
      frame.pop();  // pop ref
    } else if (ast.T.isArrayType() && !ast.E.isEmptyExpr()){
      ast.T.visit(this, o);
      ast.E.visit(this, o);
      if (ast.index >= 0 && ast.index <= 3) {
        emit(JVM.ASTORE+"_"+ast.index);
      } else {
        emit(JVM.ASTORE, ast.index);
      }
      frame.pop();  // pop ref
    }
    return null;
  }

  // Parameters

  public Object visitParaList(ParaList ast, Object o) {
    ast.P.visit(this, o);
    ast.PL.visit(this, o);
    return null;
  }

  public Object visitParaDecl(ParaDecl ast, Object o) {
    Frame frame = (Frame) o;
    ast.index = frame.getNewIndex();
    String T = VCtoJavaType(ast.T);

    emit(JVM.VAR + " " + ast.index + " is " + ast.I.spelling + " " + T + " from " + (String) frame.scopeStart.peek() + " to " +  (String) frame.scopeEnd.peek());
    return null;
  }

  public Object visitEmptyParaList(EmptyParaList ast, Object o) {
    return null;
  }

  // Arguments

  public Object visitArgList(ArgList ast, Object o) {
    ast.A.visit(this, o);
    ast.AL.visit(this, o);
    return null;
  }

  public Object visitArg(Arg ast, Object o) {
    ast.E.visit(this, o);
    return null;
  }

  public Object visitEmptyArgList(EmptyArgList ast, Object o) {
    return null;
  }

  // Types

  public Object visitIntType(IntType ast, Object o) {
    return null;
  }

  public Object visitFloatType(FloatType ast, Object o) {
    return null;
  }

  public Object visitBooleanType(BooleanType ast, Object o) {
    return null;
  }

  public Object visitVoidType(VoidType ast, Object o) {
    return null;
  }

  public Object visitArrayType(ArrayType ast, Object o) {
    Frame frame = (Frame) o;
    if (ast.parent instanceof LocalVarDecl) {
      LocalVarDecl decl = (LocalVarDecl) ast.parent;
      int index = decl.index;
      if (ast.T.equals(StdEnvironment.floatType)) {
        ast.E.visit(this, o);
        emit(JVM.NEWARRAY, "float");
        frame.pop();  //pop count
        frame.push(); // push array ref
      } else if (ast.T.equals(StdEnvironment.booleanType)){
        ast.E.visit(this, o);
        emit(JVM.NEWARRAY, "boolean");
        frame.pop();
        frame.push();
      } else {
        ast.E.visit(this, o);
        emit(JVM.NEWARRAY, "int");
        frame.pop();  // pop count
        frame.push(); // push array ref
      }
    } else if (ast.parent instanceof GlobalVarDecl){
      GlobalVarDecl decl = (GlobalVarDecl) ast.parent;
      // create array reference and push into stack
      ast.E.visit(this, o);
      if (ast.T.equals(StdEnvironment.floatType)) {
        emit(JVM.NEWARRAY, "float");
      } else if (ast.T.equals(StdEnvironment.intType)) {  // if (type.equals(StdEnvironment.intType)) 
        emit(JVM.NEWARRAY, "int");
      } else {
        emit(JVM.NEWARRAY, "boolean");
      }
      // pop count, push ref
    }
    return null;
  }

  public Object visitStringType(StringType ast, Object o) {
    return null;
  }



  public Object visitErrorType(ErrorType ast, Object o) {
    return null;
  }

  // Literals, Identifiers and Operators 

  public Object visitIdent(Ident ast, Object o) {
    // return ident
    return ast;
  }

  public Object visitIntLiteral(IntLiteral ast, Object o) {
    Frame frame = (Frame) o;
    emitICONST(Integer.parseInt(ast.spelling));
    frame.push();
    return null;
  }

  public Object visitFloatLiteral(FloatLiteral ast, Object o) {
    Frame frame = (Frame) o;
    emitFCONST(Float.parseFloat(ast.spelling));
    frame.push();
    return null;
  }

  public Object visitBooleanLiteral(BooleanLiteral ast, Object o) {
    Frame frame = (Frame) o;
    emitBCONST(ast.spelling.equals("true"));
    frame.push();
    return null;
  }

  public Object visitStringLiteral(StringLiteral ast, Object o) {
    Frame frame = (Frame) o;
    emit(JVM.LDC, "\"" + ast.spelling + "\"");
    frame.push();
    return null;
  }

  public Object visitOperator(Operator ast, Object o) {
    String op = (String)ast.spelling;
    return op;
  }

  // Variables 

  public Object visitSimpleVar(SimpleVar ast, Object o) {
    Object id = ast.I.visit(this, o);
    return id;
  }

  // Auxiliary methods for byte code generation

  // The following method appends an instruction directly into the JVM 
  // Code Store. It is called by all other overloaded emit methods.

  private void emit(String s) {
    JVM.append(new Instruction(s)); 
  }

  private void emit(String s1, String s2) {
    emit(s1 + " " + s2);
  }

  private void emit(String s1, int i) {
    emit(s1 + " " + i);
  }

  private void emit(String s1, float f) {
    emit(s1 + " " + f);
  }

  private void emit(String s1, String s2, int i) {
    emit(s1 + " " + s2 + " " + i);
  }

  private void emit(String s1, String s2, String s3) {
    emit(s1 + " " + s2 + " " + s3);
  }

  private void emitIF_ICMPCOND(String op, Frame frame) {
    String opcode;

    if (op.equals("i!="))
      opcode = JVM.IF_ICMPNE;
    else if (op.equals("i=="))
      opcode = JVM.IF_ICMPEQ;
    else if (op.equals("i<"))
      opcode = JVM.IF_ICMPLT;
    else if (op.equals("i<="))
      opcode = JVM.IF_ICMPLE;
    else if (op.equals("i>"))
      opcode = JVM.IF_ICMPGT;
    else // if (op.equals("i>="))
      opcode = JVM.IF_ICMPGE;

    String falseLabel = frame.getNewLabel();
    String nextLabel = frame.getNewLabel();

    emit(opcode, falseLabel);
    frame.pop(2); 
    emit("iconst_0");
    emit(JVM.GOTO, nextLabel);
    emit(falseLabel + ":");
    emit(JVM.ICONST_1);
    frame.push(); 
    emit(nextLabel + ":");
  }

  private void emitFCMP(String op, Frame frame) {
    String opcode;

    if (op.equals("f!="))
      opcode = JVM.IFNE;
    else if (op.equals("f=="))
      opcode = JVM.IFEQ;
    else if (op.equals("f<"))
      opcode = JVM.IFLT;
    else if (op.equals("f<="))
      opcode = JVM.IFLE;
    else if (op.equals("f>"))
      opcode = JVM.IFGT;
    else // if (op.equals("f>="))
      opcode = JVM.IFGE;

    String falseLabel = frame.getNewLabel();
    String nextLabel = frame.getNewLabel();

    emit(JVM.FCMPG);
    frame.pop(2); // pop 2 value
    frame.push(); // push result
    emit(opcode, falseLabel);
    frame.pop();
    emit(JVM.ICONST_0);
    emit(JVM.GOTO, nextLabel);
    emit(falseLabel + ":");
    emit(JVM.ICONST_1);
    frame.push();
    emit(nextLabel + ":");

  }

  private void emitILOAD(int index) {
    if (index >= 0 && index <= 3) 
      emit(JVM.ILOAD + "_" + index); 
    else
      emit(JVM.ILOAD, index); 
  }

  private void emitFLOAD(int index) {
    if (index >= 0 && index <= 3) 
      emit(JVM.FLOAD + "_"  + index); 
    else
      emit(JVM.FLOAD, index); 
  
  }

  private void emitALOAD(int index) {
    if (index >= 0 && index <= 3) 
      emit(JVM.ALOAD + "_"  + index); 
    else
      emit(JVM.ALOAD, index);
  }

  private void emitGETSTATIC(String T, String I) {
    emit(JVM.GETSTATIC, classname + "/" + I, T); 
  }

  private void emitISTORE(Ident ast) {
    int index;
    if (ast.decl instanceof ParaDecl)
      index = ((ParaDecl) ast.decl).index; 
    else
      index = ((LocalVarDecl) ast.decl).index; 
    
    if (index >= 0 && index <= 3) 
      emit(JVM.ISTORE + "_" + index); 
    else
      emit(JVM.ISTORE, index); 
  }

  private void emitFSTORE(Ident ast) {
    int index;
    if (ast.decl instanceof ParaDecl)
      index = ((ParaDecl) ast.decl).index; 
    else
      index = ((LocalVarDecl) ast.decl).index; 
    if (index >= 0 && index <= 3) 
      emit(JVM.FSTORE + "_" + index); 
    else
      emit(JVM.FSTORE, index); 
  }

  private void emitASTORE(Ident ast) {
    int index;
    if (ast.decl instanceof ParaDecl)
      index = ((ParaDecl) ast.decl).index; 
    else
      index = ((LocalVarDecl) ast.decl).index; 
    if (index >= 0 && index <= 3) 
      emit(JVM.ASTORE + "_" + index); 
    else
      emit(JVM.ASTORE, index); 
  }

  private void emitPUTSTATIC(String T, String I) {
    emit(JVM.PUTSTATIC, classname + "/" + I, T); 
  }

  private void emitICONST(int value) {
    if (value == -1)
      emit(JVM.ICONST_M1); 
    else if (value >= 0 && value <= 5) 
      emit(JVM.ICONST + "_" + value); 
    else if (value >= -128 && value <= 127) 
      emit(JVM.BIPUSH, value); 
    else if (value >= -32768 && value <= 32767)
      emit(JVM.SIPUSH, value); 
    else 
      emit(JVM.LDC, value); 
  }

  private void emitFCONST(float value) {
    if(value == 0.0)
      emit(JVM.FCONST_0); 
    else if(value == 1.0)
      emit(JVM.FCONST_1); 
    else if(value == 2.0)
      emit(JVM.FCONST_2); 
    else 
      emit(JVM.LDC, value); 
  }

  private void emitBCONST(boolean value) {
    if (value)
      emit(JVM.ICONST_1);
    else
      emit(JVM.ICONST_0);
  }

  private void emitIARITHMETIC(String op, Frame frame) {
    String opcode;
    if (op.equals("i+")) {
      opcode = JVM.IADD;
    } else if (op.equals("i-")) {  // Arithmetic operator
      opcode = JVM.ISUB;
    } else if (op.equals("i*")) {
      opcode = JVM.IMUL;
    } else {  // if op.equals("i/")
      opcode = JVM.IDIV;
    }
    emit(opcode);
    frame.pop(2);  // v1, v2 pop
    frame.push(); // push value1 + value 2
  }

  private void emitFARITHMETIC(String op, Frame frame) {
    String opcode;
    if (op.equals("f+")) {
      opcode = JVM.FADD;
    } else if (op.equals("f-")) {  // Arithmetic operator
      opcode = JVM.FSUB;
    } else if (op.equals("f*")) {
      opcode = JVM.FMUL;
    } else {  // if op.equals("f/")
      opcode = JVM.FDIV;
    }
    emit(opcode);
    frame.pop(2);  // v1, v2 pop
    frame.push(); // push value1 + value 2
  }

  private String VCtoJavaType(Type t) {
    if (t.equals(StdEnvironment.booleanType))
      return "Z";
    else if (t.equals(StdEnvironment.intType))
      return "I";
    else if (t.equals(StdEnvironment.floatType))
      return "F";
    else if (t.equals(StdEnvironment.voidType))
      return "V";
    else  // if t is array type
      if (t.isArrayType()) {
        ArrayType arrType = (ArrayType)t;
        if (arrType.T.equals(StdEnvironment.booleanType))
          return "[Z";
        else if (arrType.T.equals(StdEnvironment.intType))
          return "[I";
        else // if (t.equals(StdEnvironment.floatType))
          return "[F";
      } else {
        // do nothing
      }
      return null;
  }

}
