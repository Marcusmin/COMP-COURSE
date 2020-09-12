/*
 * Parser.java            
 *
 * This parser for a subset of the VC language is intended to 
 *  demonstrate how to create the AST nodes, including (among others): 
 *  [1] a list (of statements)
 *  [2] a function
 *  [3] a statement (which is an expression statement), 
 *  [4] a unary expression
 *  [5] a binary expression
 *  [6] terminals (identifiers, integer literals and operators)
 *
 * In addition, it also demonstrates how to use the two methods start 
 * and finish to determine the position information for the start and 
 * end of a construct (known as a phrase) corresponding an AST node.
 *
 * NOTE THAT THE POSITION INFORMATION WILL NOT BE MARKED. HOWEVER, IT CAN BE
 * USEFUL TO DEBUG YOUR IMPLEMENTATION.
 *
 * --- 12-March-2019 --- 


program       -> func-decl
func-decl     -> type identifier "(" ")" compound-stmt
type          -> void
identifier    -> ID
// statements
compound-stmt -> "{" stmt* "}" 
stmt          -> expr-stmt
expr-stmt     -> expr? ";"
// expressions 
expr                -> additive-expr
additive-expr       -> multiplicative-expr
                    |  additive-expr "+" multiplicative-expr
                    |  additive-expr "-" multiplicative-expr
multiplicative-expr -> unary-expr
	            |  multiplicative-expr "*" unary-expr
	            |  multiplicative-expr "/" unary-expr
unary-expr          -> "-" unary-expr
		    |  primary-expr

primary-expr        -> identifier
 		    |  INTLITERAL
		    | "(" expr ")"
 */

package VC.Parser;

import VC.Scanner.Scanner;
import VC.Scanner.SourcePosition;
import VC.Scanner.Token;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.StringExpression;
import javafx.beans.value.ChangeListener;
import VC.ErrorReporter;
import VC.ASTs.*;

public class Parser {

  private Scanner scanner;
  private ErrorReporter errorReporter;
  private Token currentToken;
  private SourcePosition previousTokenPosition;
  private SourcePosition dummyPos = new SourcePosition();

  public Parser (Scanner lexer, ErrorReporter reporter) {
    scanner = lexer;
    errorReporter = reporter;

    previousTokenPosition = new SourcePosition();

    currentToken = scanner.getToken();
  }

// match checks to see if the current token matches tokenExpected.
// If so, fetches the next token.
// If not, reports a syntactic error.
  boolean isType(int TokenType) {

    switch(TokenType) {
      case Token.INT: 
      case Token.BOOLEAN: 
      case Token.FLOAT: 
      case Token.VOID:
        return true;
      default:
        return false;
    }
  }

  Type toArrType(Type tAST, Expr dAST, SourcePosition Position) throws SyntaxError{
    Type res = null;
    if (tAST instanceof IntType){
      res = new ArrayType(new IntType(dummyPos), dAST, Position);
    } else if (tAST instanceof FloatType) {
      res = new ArrayType(new FloatType(dummyPos), dAST, Position);
    } else if (tAST instanceof BooleanType) {
      res = new ArrayType(new BooleanType(dummyPos), dAST, Position);
    } else if (tAST instanceof VoidType) {
      res = new ArrayType(new VoidType(dummyPos), dAST, Position);
    } else {
      syntacticError("Cannot conver to ArrayType", "");
    }
    return res;
  }

  Type copyType(Type preType) throws SyntaxError{
    Type tAST = null;
    if (preType instanceof IntType){
      tAST = new IntType(dummyPos);
    } else if (preType instanceof FloatType) {
      tAST = new FloatType(dummyPos);
    } else if (preType instanceof BooleanType) {
      tAST = new BooleanType(dummyPos);
    } else if (preType instanceof VoidType){
      tAST = new VoidType(dummyPos);
    } else {
      syntacticError("this type cannot be copied.", "");
    }
    return tAST;
  }

  void match(int tokenExpected) throws SyntaxError {
    if (currentToken.kind == tokenExpected) {
      previousTokenPosition = currentToken.position;
      currentToken = scanner.getToken();
    } else {
      syntacticError("\"%\" expected here", Token.spell(tokenExpected));
    }
  }

  void accept() {
    previousTokenPosition = currentToken.position;
    currentToken = scanner.getToken();
  }

  void syntacticError(String messageTemplate, String tokenQuoted) throws SyntaxError {
    SourcePosition pos = currentToken.position;
    errorReporter.reportError(messageTemplate, tokenQuoted, pos);
    throw(new SyntaxError());
  }

// start records the position of the start of a phrase.
// This is defined to be the position of the first
// character of the first token of the phrase.

  void start(SourcePosition position) {
    position.lineStart = currentToken.position.lineStart;
    position.charStart = currentToken.position.charStart;
  }

// finish records the position of the end of a phrase.
// This is defined to be the position of the last
// character of the last token of the phrase.

  void finish(SourcePosition position) {
    position.lineFinish = previousTokenPosition.lineFinish;
    position.charFinish = previousTokenPosition.charFinish;
  }

  void copyStart(SourcePosition from, SourcePosition to) {
    to.lineStart = from.lineStart;
    to.charStart = from.charStart;
  }

// ========================== PROGRAMS ========================

  public Program parseProgram() {

    Program programAST = null;
    
    SourcePosition programPos = new SourcePosition();
    start(programPos);

    try {
      List dlAST = parseDeclList();
      finish(programPos);
      programAST = new Program(dlAST, programPos); 
      if (currentToken.kind != Token.EOF) {
        syntacticError("\"%\" unknown type", currentToken.spelling);
      }
    }
    catch (SyntaxError s) { return null; }
    return programAST;
  }

// ========================== DECLARATIONS ========================

  List parseDeclList() throws SyntaxError {
    // parse the whole declaration list intead of only function
    List dlAST = null;  // return result
    Decl dAST = null;
    List dl2AST = null;
    //common prefix
    Type tAST = null;
    Ident idAST = null;
    Expr eAST = null;
    // for array type
    Type preType = null;

    SourcePosition dlPos = new SourcePosition();
    SourcePosition dPos = new SourcePosition();

    start(dlPos);
    start(dPos);
    if (isType(currentToken.kind)) {  // not empty decl
      tAST = parseType(); // only can parse primative type
      preType = copyType(tAST);
      idAST = parseIdent();
      if (currentToken.kind == Token.LPAREN) {
        // parse a function declaraion
        List fplAST = parseParaList();
        Stmt cAST = parseCompoundStmt();
        finish(dPos);
        dAST = new FuncDecl(tAST, idAST, fplAST, cAST, dPos);
        dl2AST = parseDeclList();
        finish(dlPos);
        dlAST = new DeclList(dAST, dl2AST, dlPos);
      } else {
        // parse a global declaration
        if (currentToken.kind == Token.LBRACKET) {
          // which means it is a arrType
          match(Token.LBRACKET);
          Expr d1AST = null;
          if (currentToken.kind != Token.RBRACKET) {
             d1AST = parseExpr();
          } else {
            d1AST = new EmptyExpr(dummyPos);
          }
          match(Token.RBRACKET);
          SourcePosition aPos = new SourcePosition();
          copyStart(dlPos, aPos);
          finish(aPos);
          tAST = new ArrayType(tAST, d1AST, aPos);
        }
        if (currentToken.kind == Token.EQ){
          accept();
          eAST = parseExpr();
        } else {
          eAST = new EmptyExpr(dummyPos);
        }
        finish(dPos);
        dAST = new GlobalVarDecl(tAST, idAST, eAST, dPos);
        if (currentToken.kind == Token.COMMA){  // int a, b, c
          match(Token.COMMA);
          dl2AST = parseVarDeclList(preType);  //parse the tokens until encounter ";".
        } else { //int a; int b;
          match(Token.SEMICOLON);
          dl2AST = parseDeclList();
        }
        finish(dlPos);
        dlAST = new DeclList(dAST, dl2AST, dlPos);
      }
    } else {
      dlAST = new EmptyDeclList(dummyPos);
    }

    return dlAST;
  }

  List parseVarDeclList() throws SyntaxError {
    //parse **local** variable declare
    Decl lvAST = null;  // local variable declare
    List dl2AST = null;
    List dlAST = null;  //declare list
    // local variable decl
    Type tAST = null;
    Ident iAST = null;
    Expr exprAST = null;

    SourcePosition vPos = new SourcePosition();
    start(vPos);  //variable declare list position

    if (!isType(currentToken.kind)) {
      //empty decl list
      dlAST = new EmptyDeclList(dummyPos);
      return dlAST;
    } else {
      //starting of decl or a whole statement
      SourcePosition dPos = new SourcePosition();
      start(dPos);  // local variable declare position

      tAST = parseType();
      iAST = parseIdent();

      if (currentToken.kind == Token.LBRACKET) {
        // array type
        accept();
        SourcePosition aPos = new SourcePosition();
        copyStart(vPos, aPos);
        Expr dAST = parseExpr();
        match(Token.RBRACKET);
        finish(aPos);
        Type aAST = toArrType(tAST, dAST, aPos);
        if (currentToken.kind == Token.EQ) {
          // with initial
          accept();
          exprAST = parseExpr();
        } else {
          exprAST = new EmptyExpr(dummyPos);
        }
        finish(dPos);
        lvAST = new LocalVarDecl(aAST, iAST, exprAST, dPos);
      } else {
        // primative type
        if (currentToken.kind == Token.EQ) {
          // with initial
          accept();
          exprAST = parseExpr();
        } else {
          exprAST = new EmptyExpr(dummyPos);
        }
        finish(dPos);
        lvAST = new LocalVarDecl(tAST, iAST, exprAST, dPos);
      }
      if (currentToken.kind == Token.SEMICOLON) {
        accept();
        dl2AST = parseVarDeclList();
      } else {
        match(Token.COMMA);
        dl2AST = parseLocalVarDeclList(tAST);
      }
      finish(vPos);
      dlAST = new DeclList(lvAST, dl2AST, vPos);
      return dlAST;
    }
  }

  List parseLocalVarDeclList(Type preType) throws SyntaxError {
    // parse rest of multiple declare
    Decl dAST = null;  // local var decl
    List dl2AST = null; // parse rest of local var decl
    List dlAST = null;  //return value
    // component of a local var decl
    Type tAST = null; // local var's type
    Expr eAST = null; // local var's initial expression, might be empty

    Ident iAST = parseIdent();

    SourcePosition llPos = new SourcePosition();
    start(llPos); // var decl list position

    if (preType instanceof IntType){
      tAST = new IntType(dummyPos);
    } else if (preType instanceof FloatType) {
      tAST = new FloatType(dummyPos);
    } else if (preType instanceof BooleanType) {
      tAST = new BooleanType(dummyPos);
    } else if (preType instanceof VoidType){
      tAST = new VoidType(dummyPos);
    } else {
      syntacticError("\"%\" cannot be an Array", currentToken.spelling);
    }
    if (currentToken.kind == Token.LBRACKET) {
      // arrayType
      accept();
      SourcePosition aAST = new SourcePosition();
      copyStart(preType.position, aAST);
      Expr e1AST = null;
      if (currentToken.kind != Token.RBRACKET) {
        e1AST = parseExpr(); // might be empty empression
      } else {
        e1AST = new EmptyExpr(dummyPos);
      }
      match(Token.RBRACKET);
      finish(aAST);
      tAST = toArrType(preType, e1AST, aAST);
    }
    SourcePosition dPos = new SourcePosition();
    copyStart(llPos, dPos);
    if (currentToken.kind == Token.EQ) {
      accept();
      eAST = parseExpr();
    } else {
      eAST = new EmptyExpr(dummyPos);
    }
    finish(dPos);
    dAST = new LocalVarDecl(tAST, iAST, eAST, dPos);
    if (currentToken.kind == Token.SEMICOLON) {
      accept();
      dl2AST = parseVarDeclList();
    } else {
      match(Token.COMMA);
      dl2AST = parseLocalVarDeclList(preType);
    }
    finish(llPos);
    dlAST = new DeclList(dAST, dl2AST, llPos);
    return dlAST;
  }

  List parseVarDeclList(Type preType) throws SyntaxError {
    // called to parse multiple variable declare,return a decl list
    // parse global decl list
    Decl dAST = null; //global declare
    List dlAST = null;
    List dl2AST = null;
    // shape a global decl
    Type tAST = null;
    Expr eAST = null;

    Ident idAST = parseIdent();
    SourcePosition vdPos = new SourcePosition();
    start(vdPos);

    if (preType instanceof IntType){
      tAST = new IntType(dummyPos);
    } else if (preType instanceof FloatType) {
      tAST = new FloatType(dummyPos);
    } else if (preType instanceof BooleanType) {
      tAST = new BooleanType(dummyPos);
    } else if (preType instanceof VoidType){
      tAST = new VoidType(dummyPos);
    } else {
      syntacticError("\"%\" cannot be an Array", currentToken.spelling);
    }
    if (currentToken.kind == Token.LBRACKET) {
      // arrType
      accept();
      Expr e1AST = null;
      if (currentToken.kind != Token.RBRACKET){
        e1AST = parseExpr();
      } else {
        e1AST = new EmptyExpr(dummyPos);
      }
      match(Token.RBRACKET);
      SourcePosition atPos = new SourcePosition();   // arrayType position
      copyStart(preType.position, atPos);
      finish(atPos);  // arry type's postion
      tAST = new ArrayType(tAST, e1AST, atPos);
    }
    SourcePosition dPos = new SourcePosition();
    copyStart(vdPos, dPos);
    if (currentToken.kind != Token.COMMA && currentToken.kind != Token.SEMICOLON) {
      match(Token.EQ);
      //assignment or initialization
      eAST = parseExpr();
    } else {
      eAST = new EmptyExpr(dummyPos);
    }
    finish(dPos); // declare' position
    dAST = new GlobalVarDecl(tAST, idAST, eAST, dPos);
    if (currentToken.kind == Token.SEMICOLON) {
      accept();
      dl2AST = parseDeclList();
    } else {
      //parse multiple declare
      match(Token.COMMA);
      dl2AST = parseVarDeclList(preType);
    }
    finish(vdPos);
    dlAST = new DeclList(dAST, dl2AST, vdPos);
    return dlAST;
  }
//  ======================== TYPES ==========================

  Type parseType() throws SyntaxError {
    Type typeAST = null;

    SourcePosition typePos = new SourcePosition();
    start(typePos);

    switch(currentToken.kind){
      case Token.INT:
        accept();
        finish(typePos);
        typeAST = new IntType(typePos);
        break;
      case Token.FLOAT:
        accept();
        finish(typePos);
        typeAST = new FloatType(typePos);
        break;
      case Token.BOOLEAN:
        accept();
        finish(typePos);
        typeAST = new BooleanType(typePos);
        break;
      case Token.VOID:
        accept();
        finish(typePos);
        typeAST = new VoidType(typePos);
        break;
      default:
        syntacticError("Token [Type] expected here.", "");
    }

    return typeAST;
    }

// ======================= STATEMENTS ==============================

  Stmt parseCompoundStmt() throws SyntaxError {
    Stmt cAST = null; 

    SourcePosition stmtPos = new SourcePosition();
    start(stmtPos);

    match(Token.LCURLY);

    // Insert code here to build a DeclList node for variable declarations
    List vlAST = parseVarDeclList();  // parse variable declarations, can return EmptyDeclList
    List slAST = parseStmtList(); // parse StmtList, can return EmptyStmt

    match(Token.RCURLY);
    finish(stmtPos);

    /* In the subset of the VC grammar, no variable declarations are
     * allowed. Therefore, a block is empty iff it has no statements.
     */
    if (slAST instanceof EmptyStmtList && vlAST instanceof EmptyDeclList){
      cAST = new EmptyCompStmt(stmtPos);
    } else {
      cAST = new CompoundStmt(vlAST, slAST, stmtPos);
    }
    return cAST;
  }


  List parseStmtList() throws SyntaxError {
    List slAST = null; 

    SourcePosition stmtPos = new SourcePosition();
    start(stmtPos);

    if (currentToken.kind != Token.RCURLY) {
      Stmt sAST = parseStmt();
      {
        if (currentToken.kind != Token.RCURLY) {
          slAST = parseStmtList();
          finish(stmtPos);
          slAST = new StmtList(sAST, slAST, stmtPos);
        } else {
          finish(stmtPos);
          slAST = new StmtList(sAST, new EmptyStmtList(dummyPos), stmtPos);
        }
      }
    }
    else
      slAST = new EmptyStmtList(dummyPos);
    
    return slAST;
  }

  Stmt parseStmt() throws SyntaxError {
    Stmt sAST = null;
    switch (currentToken.kind){
      case Token.IF:
        sAST = parseIfStmt();
        break;
      case Token.FOR:
        sAST = parseForStmt();
        break;
      case Token.WHILE:
        sAST = parseWhileStmt();
        break;
      case Token.BREAK:
        sAST = parseBreakStmt();
        break;
      case Token.CONTINUE:
        sAST = parseContinueStmt();
        break;
      case Token.RETURN:
        sAST = parseReturnStmt();
        break;
      case Token.LCURLY:
        sAST = parseCompoundStmt();
        break;
      default:
        sAST = parseExprStmt();
    }
    return sAST;
  }

  Stmt parseIfStmt() throws SyntaxError {
    Expr e1AST = null;
    Stmt sAST = null;
    Stmt ifStmtAST = null;

    SourcePosition ifPos = new SourcePosition();
    start(ifPos);
    match(Token.IF);
    match(Token.LPAREN);
    e1AST = parseExpr();
    match(Token.RPAREN);
    sAST = parseStmt();

    if (currentToken.kind == Token.ELSE) {
      match(Token.ELSE);  // accept is alternative here
      Stmt s2AST = parseStmt();
      finish(ifPos);
      ifStmtAST = new IfStmt(e1AST, sAST, s2AST, ifPos);
    } else {
      finish(ifPos);
      ifStmtAST = new IfStmt(e1AST, sAST, ifPos);
    }

    return ifStmtAST;
  }

  Stmt parseWhileStmt() throws SyntaxError {
    Expr eAST = null;
    Stmt sAST = null;
    Stmt whileStmtAST = null;

    SourcePosition whPos = new SourcePosition();
    start(whPos);
    match(Token.WHILE);
    match(Token.LPAREN);
    eAST = parseExpr();
    match(Token.RPAREN);
    sAST = parseStmt();
    finish(whPos);
    whileStmtAST = new WhileStmt(eAST, sAST, whPos);

    return whileStmtAST;
  }

  Stmt parseForStmt() throws SyntaxError {
    Expr e1AST = null;
    Expr e2AST = null;
    Expr e3AST = null;
    Stmt sAST = null;
    Stmt fsAST = null;

    SourcePosition fPos = new SourcePosition();
    start(fPos);
    match(Token.FOR);
    match(Token.LPAREN);
    if (currentToken.kind == Token.SEMICOLON)
      e1AST = new EmptyExpr(dummyPos);
    else
      e1AST = parseExpr();
    match(Token.SEMICOLON);
    if (currentToken.kind == Token.SEMICOLON)
      e2AST = new EmptyExpr(dummyPos);
    else
      e2AST = parseExpr();
    match(Token.SEMICOLON);
    if (currentToken.kind == Token.RPAREN)
      e3AST = new EmptyExpr(dummyPos);
    else
      e3AST = parseExpr();
    match(Token.RPAREN);
    sAST = parseCompoundStmt();
    finish(fPos);
    fsAST = new ForStmt(e1AST, e2AST, e3AST, sAST, fPos);
    return fsAST;
  }

  Stmt parseBreakStmt() throws SyntaxError {
    Stmt bAST = null;

    SourcePosition bPos = new SourcePosition();
    start(bPos);
    match(Token.BREAK);
    match(Token.SEMICOLON);
    finish(bPos);
    bAST = new BreakStmt(bPos);

    return bAST;
  }

  Stmt parseContinueStmt() throws SyntaxError {
    Stmt cAST = null;

    SourcePosition cPos = new SourcePosition();
    start(cPos);
    match(Token.CONTINUE);
    match(Token.SEMICOLON);
    finish(cPos);
    cAST = new ContinueStmt(cPos);
    return cAST;
  }

  Stmt parseReturnStmt() throws SyntaxError {
    Expr eAST = null;
    Stmt rAST = null;

    SourcePosition rPos = new SourcePosition();
    start(rPos);
    match(Token.RETURN);
    if (currentToken.kind != Token.SEMICOLON) {
      // return expr ;
      eAST = parseExpr();
      match(Token.SEMICOLON);
    } else {
      eAST = new EmptyExpr(dummyPos);
      match(Token.SEMICOLON);
    }
    rAST = new ReturnStmt(eAST, rPos);

    return rAST;
  }

  Stmt parseExprStmt() throws SyntaxError {
    Stmt sAST = null;

    SourcePosition stmtPos = new SourcePosition();
    start(stmtPos);

    if (currentToken.kind == Token.ID
          || currentToken.kind == Token.INTLITERAL
          || currentToken.kind == Token.MINUS
          || currentToken.kind == Token.LPAREN
          || currentToken.kind == Token.PLUS
          || currentToken.kind == Token.NOT
          || currentToken.kind == Token.LPAREN
          || currentToken.kind == Token.BOOLEANLITERAL
          || currentToken.kind == Token.FLOATLITERAL
          || currentToken.kind == Token.STRINGLITERAL) {
        Expr eAST = parseExpr();
        match(Token.SEMICOLON);
        finish(stmtPos);
        sAST = new ExprStmt(eAST, stmtPos);
    } else {
      match(Token.SEMICOLON);
      finish(stmtPos);
      sAST = new ExprStmt(new EmptyExpr(dummyPos), stmtPos);
    }
    return sAST;
  }


// ======================= PARAMETERS =======================

  List parseParaList() throws SyntaxError {
    // call when a function is declaring
    List formalsAST = null;
    ParaDecl pAST = null; // parameter declaration
    List plAST = null;  // parameter list

    SourcePosition formalsPos = new SourcePosition();
    start(formalsPos);
    match(Token.LPAREN);
    // todo: parse proper parameter here
    if (currentToken.kind == Token.RPAREN) {  // Empty parameter list like int a()
      accept();
      formalsAST = new EmptyParaList(dummyPos);
    } else {
      pAST = parseParaDecl();
      if (currentToken.kind == Token.COMMA) {
        accept();
        plAST = __parseParaList();
      } else {
        plAST = new EmptyParaList(dummyPos);
      }
      // formalsAST = parseProperArgList();
      match(Token.RPAREN);
      finish(formalsPos);
      formalsAST = new ParaList(pAST, plAST, formalsPos);
    }

    return formalsAST;
  }
  List __parseParaList() throws SyntaxError {
    // parse rest of para in para decl list
    List formalsAST = null;
    ParaDecl pAST = null; // parameter declaration
    List plAST = null;  // parameter list

    SourcePosition formalsPos = new SourcePosition();
    start(formalsPos);
    // todo: parse proper parameter here
    if (currentToken.kind == Token.RPAREN) {  // Empty parameter list like int a()
      accept();
      formalsAST = new EmptyParaList(dummyPos);
    } else {
      pAST = parseParaDecl();
      if (currentToken.kind == Token.COMMA) {
        accept();
        plAST = __parseParaList();
      } else {
        plAST = new EmptyParaList(dummyPos);
      }
      // formalsAST = parseProperArgList();
      finish(formalsPos);
      formalsAST = new ParaList(pAST, plAST, formalsPos);
    }

    return formalsAST;
  }
  ParaDecl parseParaDecl() throws SyntaxError {
    // parse parameter declaration
    Type tAST  = null;
    Ident idAST = null;
    ParaDecl pdAST = null;

    SourcePosition pPos = new SourcePosition();
    start(pPos);
    tAST = parseType();
    idAST = parseIdent();
    if (currentToken.kind == Token.LBRACKET) {  // int a[10]
      // arrType
      accept();
      Expr dAST = null;
      if (currentToken.kind != Token.RBRACKET){
        dAST = parseExpr();
      } else {
        dAST = new EmptyExpr(dummyPos);
      }
      match(Token.RBRACKET);
      SourcePosition aPos = new SourcePosition();
      copyStart(pPos, aPos);
      finish(aPos);
      tAST = new ArrayType(tAST, dAST, aPos);
    }
    finish(pPos);
    pdAST = new ParaDecl(tAST, idAST, pPos);
    return pdAST;
  }

  List parseArgList() throws SyntaxError {
    // call when a function is called
    // todo: f(xx, xx....)
    Arg aAST = null;
    List alAST = null;
    List palAST = null;  // argument list

    SourcePosition plPos = new SourcePosition();
    start(plPos);
    match(Token.LPAREN);
    // parse proper argument list
    if (currentToken.kind == Token.RPAREN) {
      // empty arg list
      accept();
      palAST = new EmptyArgList(dummyPos);
    } else {  // there is at least one arg
      aAST = parseArg();
      if (currentToken.kind == Token.COMMA) {
        accept();
        alAST = __parseArgList();
        finish(plPos);
      } else {  // if there is no argument needed
        alAST = new EmptyArgList(dummyPos); // no idea what is dummyPos
      }
      match(Token.RPAREN);
      finish(plPos);
      palAST = new ArgList(aAST, alAST, plPos);
    }
    return palAST;
  }
  List __parseArgList() throws SyntaxError {
    Arg aAST = null;
    List alAST = null;
    List palAST = null;  // argument list

    SourcePosition plPos = new SourcePosition();
    start(plPos);
    // parse proper argument list
    if (currentToken.kind == Token.RPAREN) {
      // empty arg list
      accept();
      palAST = new EmptyArgList(dummyPos);
    } else {  // there is at least one arg
      aAST = parseArg();
      if (currentToken.kind == Token.COMMA) {
        accept();
        alAST = __parseArgList();
        finish(plPos);
      } else {  // if there is no argument needed
        alAST = new EmptyArgList(dummyPos); // no idea what is dummyPos
      }
      finish(plPos);
      palAST = new ArgList(aAST, alAST, plPos);
    }
    return palAST;
  }
  Arg parseArg() throws SyntaxError {
    Arg aAST = null;
    Expr exprAST = null;

    SourcePosition aPos = new SourcePosition();
    start(aPos);
    exprAST = parseExpr();
    finish(aPos);
    aAST = new Arg(exprAST, aPos);
    return aAST;
  }

// ======================= EXPRESSIONS ======================

  // It would not return empty expression
  Expr parseExpr() throws SyntaxError {
    Expr exprAST = null;

    if (currentToken.kind != Token.LCURLY)
      exprAST = parseAssignExpr();
    else
      exprAST = parseInitExpr();
    return exprAST;
  }

  List parseExprList() throws SyntaxError {
    List ilAST = null;
    Expr eAST = null;
    List elAST = null;

    SourcePosition ilPos = new SourcePosition();
    start(ilPos);

    if (currentToken.kind == Token.RCURLY){
      ilAST = new EmptyExprList(dummyPos);
      return ilAST;
    }
    eAST = parseExpr();
    if (currentToken.kind == Token.COMMA) {
      match(Token.COMMA);
    }
    elAST = parseExprList();
    finish(ilPos);
    ilAST = new ExprList(eAST, elAST, ilPos);
    return ilAST;
  }

  Expr parseInitExpr() throws SyntaxError {
    Expr exprAST = null;
    List ilAST = null;

    SourcePosition iePos = new SourcePosition();
    start(iePos);
    
    match(Token.LCURLY);
    ilAST = parseExprList();
    match(Token.RCURLY);
    finish(iePos);
    exprAST = new InitExpr(ilAST, iePos);

    return exprAST;
  }

  Expr parseAssignExpr() throws SyntaxError {
    Expr exprAST = null;
    Expr e2AST = null;

    SourcePosition eStartPos = new SourcePosition();
    start(eStartPos);
    exprAST = parseCondOrExpr();
    if (currentToken.kind == Token.EQ) {
      accept();
      e2AST = parseAssignExpr();
      // SourcePosition ePos = new SourcePosition();
      // copyStart(eStartPos, ePos);
      // finish(ePos);
      finish(eStartPos);
      exprAST = new AssignExpr(exprAST, e2AST, eStartPos);
    }
    return exprAST;
  }

  Expr parseCondOrExpr() throws SyntaxError {
    Expr exprAST = null;
    Expr e2AST = null;

    SourcePosition eStartPos = new SourcePosition();
    start(eStartPos);
    exprAST = parseCondAndExpr();
    while (currentToken.kind == Token.OROR) {
      Operator opAST = acceptOperator();
      e2AST = parseCondAndExpr();
      SourcePosition ePos = new SourcePosition();
      copyStart(eStartPos, ePos);
      finish(ePos);
      exprAST = new BinaryExpr(exprAST, opAST, e2AST, ePos);
    }
    return exprAST;
  }

  Expr parseCondAndExpr() throws SyntaxError {
    Expr exprAST = null;
    Expr e2AST = null;

    SourcePosition eStartPos = new SourcePosition();
    start(eStartPos);
    exprAST = parseEquExpr();
    while (currentToken.kind == Token.ANDAND) {
      Operator opAST = acceptOperator();
      e2AST = parseEquExpr();
      SourcePosition ePos = new SourcePosition();
      copyStart(eStartPos, ePos);
      finish(ePos);
      exprAST = new BinaryExpr(exprAST, opAST, e2AST, ePos);
    }
    return exprAST;
  }

  Expr parseEquExpr() throws SyntaxError {
    Expr exprAST = null;
    // Expr e2AST = null;

    SourcePosition eStartPos = new SourcePosition();
    start(eStartPos);
    exprAST = parseRelExpr();
    while (currentToken.kind == Token.EQEQ
            || currentToken.kind == Token.NOTEQ) {
      Operator opAST = acceptOperator();
      Expr e2Expr = parseRelExpr();

      SourcePosition ePos = new SourcePosition();
      copyStart(eStartPos, ePos);
      finish(ePos);
      exprAST = new BinaryExpr(exprAST, opAST, e2Expr, ePos);
    }
    return exprAST;
  }

  Expr parseRelExpr() throws SyntaxError {
    Expr exprAST = null;
    Expr e2AST = null;

    SourcePosition eStartPos = new SourcePosition();
    start(eStartPos);
    exprAST = parseAdditiveExpr();
    while (currentToken.kind == Token.LT
          || currentToken.kind == Token.LTEQ
          || currentToken.kind == Token.GT
          || currentToken.kind == Token.GTEQ) {
          Operator opAST = acceptOperator();
          e2AST = parseAdditiveExpr();

          SourcePosition ePos = new SourcePosition();
          copyStart(eStartPos, ePos);
          finish(ePos);
          exprAST = new BinaryExpr(exprAST, opAST, e2AST, ePos);
    }
    return exprAST;
  }

  Expr parseAdditiveExpr() throws SyntaxError {
    Expr exprAST = null;

    SourcePosition addStartPos = new SourcePosition();
    start(addStartPos);

    exprAST = parseMultiplicativeExpr();
    while (currentToken.kind == Token.PLUS
           || currentToken.kind == Token.MINUS) {
      Operator opAST = acceptOperator();
      Expr e2AST = parseMultiplicativeExpr();

      SourcePosition addPos = new SourcePosition();
      copyStart(addStartPos, addPos);
      finish(addPos);
      exprAST = new BinaryExpr(exprAST, opAST, e2AST, addPos);
    }
    return exprAST;
  }

  Expr parseMultiplicativeExpr() throws SyntaxError {

    Expr exprAST = null;

    SourcePosition multStartPos = new SourcePosition();
    start(multStartPos);

    exprAST = parseUnaryExpr();
    while (currentToken.kind == Token.MULT
           || currentToken.kind == Token.DIV) {
      Operator opAST = acceptOperator();
      Expr e2AST = parseUnaryExpr();
      SourcePosition multPos = new SourcePosition();
      copyStart(multStartPos, multPos);
      finish(multPos);
      exprAST = new BinaryExpr(exprAST, opAST, e2AST, multPos);
    }
    return exprAST;
  }

  Expr parseUnaryExpr() throws SyntaxError {

    Expr exprAST = null;

    SourcePosition unaryPos = new SourcePosition();
    start(unaryPos);

    switch (currentToken.kind) {
      case Token.MINUS:
        {
          Operator opAST = acceptOperator();
          Expr e2AST = parseUnaryExpr();
          finish(unaryPos);
          exprAST = new UnaryExpr(opAST, e2AST, unaryPos);
        }
        break;
      case Token.NOT:
        {
          Operator opAST = acceptOperator();
          Expr e2AST = parseUnaryExpr();
          finish(unaryPos);
          exprAST = new UnaryExpr(opAST, e2AST, unaryPos);
        }
        break;
      case Token.PLUS:
        {
          Operator opAST = acceptOperator();
          Expr e2AST = parseUnaryExpr();
          finish(unaryPos);
          exprAST = new UnaryExpr(opAST, e2AST, unaryPos);
        }
        break;

      default:
        exprAST = parsePrimaryExpr();
        break;
       
    }
    return exprAST;
  }

  Expr parsePrimaryExpr() throws SyntaxError {
    // todo:
    Expr exprAST = null;

    SourcePosition primPos = new SourcePosition();
    start(primPos);

    switch (currentToken.kind) {

      case Token.ID:
        SourcePosition iPos = new SourcePosition(); // identifier's position
        copyStart(primPos, iPos);
        Ident iAST = parseIdent();
        finish(iPos);
        if (currentToken.kind == Token.LBRACKET) {
          // identifier "[" expr "]"
          accept(); // accept "["
          Var svAST = new SimpleVar(iAST, iPos);
          Expr indexAST = parseExpr();
          match(Token.RBRACKET);
          finish(primPos);
          exprAST = new ArrayExpr(svAST, indexAST, primPos);
        } else if (currentToken.kind == Token.LPAREN){
          // identifier arg-list?
          List alAST = parseArgList();  // maybe better if use ArgList type
          finish(primPos);
          exprAST = new CallExpr(iAST, alAST, primPos);
        } else {
          finish(primPos);
          Var svAST = new SimpleVar(iAST, iPos);
          exprAST = new VarExpr(svAST, primPos);
        }
        break;

      case Token.LPAREN:
        {
          accept();
          exprAST = parseExpr();
	        match(Token.RPAREN);
        }
        break;

      case Token.INTLITERAL:
        IntLiteral ilAST = parseIntLiteral();
        finish(primPos);
        exprAST = new IntExpr(ilAST, primPos);
        break;

      case Token.BOOLEANLITERAL:
        BooleanLiteral blAST = parseBooleanLiteral();
        finish(primPos);
        exprAST = new BooleanExpr(blAST, primPos);
        break;

      case Token.FLOATLITERAL:
        FloatLiteral flAST = parseFloatLiteral();
        finish(primPos);
        exprAST = new FloatExpr(flAST, primPos);
        break;

      case Token.STRINGLITERAL:
        StringLiteral slAST = parseStringLiteral();
        finish(primPos);
        exprAST = new StringExpr(slAST, primPos);
        break;

      default:
        syntacticError("% illegal primary expression", currentToken.spelling);
       
    }
    return exprAST;
  }

// ========================== ID, OPERATOR and LITERALS ========================
// todo: done?
  Ident parseIdent() throws SyntaxError {

    Ident I = null; 

    if (currentToken.kind == Token.ID) {
      previousTokenPosition = currentToken.position;
      String spelling = currentToken.spelling;
      I = new Ident(spelling, previousTokenPosition);
      currentToken = scanner.getToken();
    } else 
      syntacticError("identifier expected here", "");
    return I;
  }

// acceptOperator parses an operator, and constructs a leaf AST for it

  Operator acceptOperator() throws SyntaxError {
    Operator O = null;

    previousTokenPosition = currentToken.position;
    String spelling = currentToken.spelling;
    O = new Operator(spelling, previousTokenPosition);
    currentToken = scanner.getToken();
    return O;
  }


  IntLiteral parseIntLiteral() throws SyntaxError {
    IntLiteral IL = null;

    if (currentToken.kind == Token.INTLITERAL) {
      String spelling = currentToken.spelling;
      accept();
      IL = new IntLiteral(spelling, previousTokenPosition);
    } else 
      syntacticError("integer literal expected here", "");
    return IL;
  }

  FloatLiteral parseFloatLiteral() throws SyntaxError {
    FloatLiteral FL = null;

    if (currentToken.kind == Token.FLOATLITERAL) {
      String spelling = currentToken.spelling;
      accept();
      FL = new FloatLiteral(spelling, previousTokenPosition);
    } else 
      syntacticError("float literal expected here", "");
    return FL;
  }

  BooleanLiteral parseBooleanLiteral() throws SyntaxError {
    BooleanLiteral BL = null;

    if (currentToken.kind == Token.BOOLEANLITERAL) {
      String spelling = currentToken.spelling;
      accept();
      BL = new BooleanLiteral(spelling, previousTokenPosition);
    } else 
      syntacticError("boolean literal expected here", "");
    return BL;
  }

  StringLiteral parseStringLiteral() throws SyntaxError {
    StringLiteral SL = null;

    if (currentToken.kind == Token.STRINGLITERAL) {
      String spelling = currentToken.spelling;
      accept();
      SL = new StringLiteral(spelling, previousTokenPosition);
    } else {
      syntacticError("string literal expected here", "");
    }
    return SL;
  }

}

