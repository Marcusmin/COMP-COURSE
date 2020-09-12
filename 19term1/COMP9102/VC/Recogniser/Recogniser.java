/***
 * *
 * * Recogniser.java            
 * *
 ***/

/* At this stage, this parser accepts a subset of VC defined	by
 * the following grammar. 
 *
 * You need to modify the supplied parsing methods (if necessary) and 
 * add the missing ones to obtain a parser for the VC language.
 *
 * (27---Feb---2019)

program       -> func-decl

// declaration

func-decl     -> void identifier "(" ")" compound-stmt

identifier    -> ID

// statements 
compound-stmt -> "{" stmt* "}" 
stmt          -> continue-stmt
    	      |  expr-stmt
continue-stmt -> continue ";"
expr-stmt     -> expr? ";"

// expressions 
expr                -> assignment-expr
assignment-expr     -> additive-expr
additive-expr       -> multiplicative-expr
                    |  additive-expr "+" multiplicative-expr
multiplicative-expr -> unary-expr
	            |  multiplicative-expr "*" unary-expr
unary-expr          -> "-" unary-expr
		    |  primary-expr

primary-expr        -> identifier
 		    |  INTLITERAL
		    | "(" expr ")"
*/

package VC.Recogniser;

import VC.Scanner.Scanner;
import VC.Scanner.SourcePosition;
import VC.Scanner.Token;
import VC.ErrorReporter;

public class Recogniser {

  private Scanner scanner;
  private ErrorReporter errorReporter;
  private Token currentToken;

  public Recogniser (Scanner lexer, ErrorReporter reporter) {
    scanner = lexer;
    errorReporter = reporter;

    currentToken = scanner.getToken();
  }
  void debuggerNotImplmtYet(String FuncName){
    System.out.println(FuncName + "Not implemented yet");
  }
// match checks to see f the current token matches tokenExpected.
// If so, fetches the next token.
// If not, reports a syntactic error.

  void match(int tokenExpected) throws SyntaxError {
    if (currentToken.kind == tokenExpected) {
      currentToken = scanner.getToken();
    } else {
      // System.out.println(currentToken.spelling);
      syntacticError("\"%\" expected here", Token.spell(tokenExpected));
    }
  }
 // accepts the current token and fetches the next
  void accept() {
    currentToken = scanner.getToken();
  }

  void syntacticError(String messageTemplate, String tokenQuoted) throws SyntaxError {
    SourcePosition pos = currentToken.position;
    errorReporter.reportError(messageTemplate, tokenQuoted, pos);
    throw(new SyntaxError());
  }


// ========================== primitive types ========================
  /*type                -> void | boolean | int | float*/
  boolean isType(int tok){
    switch(tok){
      case Token.BOOLEAN: return true;
      case Token.FLOAT: return true;
      case Token.INT: return true;
      case Token.VOID: return true;
      default: return false;
    }
  }

  void parseType() throws SyntaxError{
    switch(currentToken.kind){
      case Token.BOOLEAN: match(Token.BOOLEAN); break;
      case Token.FLOAT: match(Token.FLOAT); break;
      case Token.INT: match(Token.INT); break;
      case Token.VOID: match(Token.VOID); break;
      default:
        syntacticError("\"%\" wrong result type for a function", currentToken.spelling);
    }
  }
// ========================== PROGRAMS ========================
/*
program  ->  ( func-decl | var-decl )*
func-decl           -> type identifier para-list compound-stmt
var-decl            -> type init-declarator-list ";"
left-factoring: 
program ->   (type identifier (para-list compound-stmt | (e |"[" INTLITERAL? "]") ( "=" initialiser )?  ( "," init-declarator )* ";"))*
              
*/
  public void parseProgram() {
    try {
      while (currentToken.kind != Token.EOF){
        parseType();  // a program may begin with a type
        parseIdent(); // profix of both func & var
        if (currentToken.kind == Token.LPAREN){ //para list
          //parse function decl
          parseParaList();
          parseCompoundStmt();
        } else if (currentToken.kind == Token.LBRACKET
                   || currentToken.kind == Token.EQ
                   || currentToken.kind == Token.COMMA
                   || currentToken.kind == Token.SEMICOLON){  //select set of rest of var decl
          // parse var decl
          parseRestOfVarDecl();
        } else {
          // syntacticError
          syntacticError("\"%\" wrong result type for a function", currentToken.spelling);
        }
      }
    }
    catch (SyntaxError s) {  }
  }

// ========================== DECLARATIONS ========================
/*
func-decl           -> type identifier para-list compound-stmt
var-decl            -> type init-declarator-list ";"
=> var-decl -> type init-declarator ( "," init-declarator )* ";"
=> var-decl -> type identifier ( e | "[" INTLITERAL? "]") ( "=" initialiser )? ( "," init-declarator )* ";"
rest-of-var-decl -> ( e | "[" INTLITERAL? "]") ( "=" initialiser )? ( "," init-declarator )* ";"
*/
  void parseVarDecl() throws SyntaxError{
    parseType();
    parseInitDeclaratorList();
    match(Token.SEMICOLON);
  }

  void parseInitDeclaratorList() throws SyntaxError{
    parseInitDeclarator();
    while(currentToken.kind == Token.COMMA){
      match(Token.COMMA);
      parseInitDeclarator();
    }
  }

  void parseRestOfVarDecl() throws SyntaxError {
    //  type has been dealt by parseType
    //  indentifier has been dealt by parseIdent
    if (currentToken.kind == Token.LBRACKET){
      //   "[" INTLITERAL? "]"
      match(Token.LBRACKET);
      if (currentToken.kind != Token.RBRACKET){
        parseIntLiteral();
      }
      match(Token.RBRACKET);
    }
    if (currentToken.kind == Token.EQ){
      // match
      match(Token.EQ);
      parseInitializer();
      // parse initializer
    } 
    while(currentToken.kind == Token.COMMA){
      match(Token.COMMA);
      // parse init-decl
      parseInitDeclarator();
    }
    match(Token.SEMICOLON);
  }

  void parseDeclarator() throws SyntaxError{
    parseIdent();
    if (currentToken.kind == Token.LBRACKET){
      match(Token.LBRACKET);
      if (currentToken.kind == Token.INTLITERAL){
        parseIntLiteral();
      }
      match(Token.RBRACKET);
    }
  }

  void parseInitDeclarator() throws SyntaxError{
    parseDeclarator();
    if (currentToken.kind == Token.EQ){
      match(Token.EQ);
      parseInitializer();
    }
  }

/*initialiser         -> expr 
                        |  "{" expr ( "," expr )* "}"
 */

  void parseInitializer() throws SyntaxError{
    if(currentToken.kind == Token.LCURLY){
      match(Token.LCURLY);
      parseExpr();
      while (currentToken.kind == Token.COMMA){
        match(Token.COMMA);
        parseExpr();
      }
      match(Token.RCURLY);
    } else {
      parseExpr();
    }
  }
// ======================= STATEMENTS ==============================


  //  compound-stmt       -> "{" var-decl* stmt* "}" 
  void parseCompoundStmt() throws SyntaxError {
    match(Token.LCURLY);
    while (isType(currentToken.kind)) {
      parseVarDecl();
    }
    while (currentToken.kind != Token.RCURLY){
      parseStmt();
    }
    match(Token.RCURLY);
  }

 // Here, a new nontermial has been introduced to define { stmt } *
  // void parseStmtList() throws SyntaxError {

  //   while (currentToken.kind != Token.RCURLY) 
  //     parseStmt();
  // }

  void parseStmt() throws SyntaxError {

    switch (currentToken.kind) {
      case Token.LCURLY:
        parseCompoundStmt();
        break;
      case Token.IF:
        parseIfStmt();
        break;
      case Token.WHILE:
        parseWhileStmt();
        break;
      case Token.FOR:
        parseForStmt();
        break;
      case Token.BREAK:
        parseBreakStmt();
        break;
      case Token.CONTINUE:
        parseContinueStmt();
        break;
      case Token.RETURN:
        parseReturnStmt();
        break;
      default:
        parseExprStmt();
        break;
    }
  }
  //  if-stmt             -> if "(" expr ")" stmt ( else stmt )?
  void parseIfStmt() throws SyntaxError{
    match(Token.IF);
    match(Token.LPAREN);
    parseExpr();
    match(Token.RPAREN);
    parseStmt();
    if (currentToken.kind == Token.ELSE){
      match(Token.ELSE);
      parseStmt();
    }
  }

  // for-stmt            -> for "(" expr? ";" expr? ";" expr? ")" stmt
  void parseForStmt() throws SyntaxError{
    match(Token.FOR);
    match(Token.LPAREN);
    if (currentToken.kind != Token.SEMICOLON){
      parseExpr();
    }
    match(Token.SEMICOLON);
    if (currentToken.kind != Token.SEMICOLON){
      parseExpr();
    }
    match(Token.SEMICOLON);
    if (currentToken.kind != Token.RPAREN){
      parseExpr();
    }
    match(Token.RPAREN);
    parseStmt();
  }
  //  while-stmt          -> while "(" expr ")" stmt
  void parseWhileStmt() throws SyntaxError{
    match(Token.WHILE);
    match(Token.LPAREN);
    parseExpr();
    match(Token.RPAREN);
    parseStmt();
  }

  //  break-stmt          -> break ";"
  void parseBreakStmt() throws SyntaxError{
    match(Token.BREAK);
    match(Token.SEMICOLON);
  }


  // continue-stmt       -> continue ";"
  void parseContinueStmt() throws SyntaxError {
    match(Token.CONTINUE);
    match(Token.SEMICOLON);
  }

  //  return-stmt         -> return expr? ";"
  void parseReturnStmt() throws SyntaxError{
    match(Token.RETURN);
    if (currentToken.kind != Token.SEMICOLON){
      parseExpr();
    }
    match(Token.SEMICOLON);
  }

  void parseExprStmt() throws SyntaxError {

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
        parseExpr();
        match(Token.SEMICOLON);
    } else {
      match(Token.SEMICOLON);
    }
  }


// ======================= IDENTIFIERS ======================

 // Call parseIdent rather than match(Token.ID). 
 // In Assignment 3, an Identifier node will be constructed in here.


  void parseIdent() throws SyntaxError {

    if (currentToken.kind == Token.ID) {
      currentToken = scanner.getToken();
    } else 
      syntacticError("identifier expected here", "");
  }

// ======================= OPERATORS ======================

 // Call acceptOperator rather than accept(). 
 // In Assignment 3, an Operator Node will be constructed in here.

  void acceptOperator() throws SyntaxError {

    currentToken = scanner.getToken();
  }


// ======================= EXPRESSIONS ======================

  void parseExpr() throws SyntaxError {
    parseAssignExpr();
  }

  void parseAssignExpr() throws SyntaxError {
    parseConOrExpr();
    if (currentToken.kind == Token.EQ){
      match(Token.EQ);
      parseAssignExpr();
    }
  }
  void parseConOrExpr() throws SyntaxError {
    parseConAndExpr();
    if (currentToken.kind == Token.OROR){
      match(Token.OROR);
      parseConAndExpr();
    }
  }

  void parseConAndExpr() throws SyntaxError {
    parseEqualityExpr();
    if (currentToken.kind == Token.ANDAND){
      match(Token.ANDAND);
      parseConAndExpr();
    }
  }

  void parseEqualityExpr() throws SyntaxError {
    parseRelExpr();
    if (currentToken.kind == Token.EQEQ){
      match(Token.EQEQ);
      parseEqualityExpr();
    } else if (currentToken.kind == Token.NOTEQ){
      match(Token.NOTEQ);
      parseEqualityExpr();
    }
  }

  void parseRelExpr() throws SyntaxError {
    parseAdditiveExpr();
    if (currentToken.kind == Token.GT){
      match(Token.GT);
      parseRelExpr();
    } else if (currentToken.kind == Token.GTEQ){
      match(Token.GTEQ);
      parseRelExpr();
    } else if (currentToken.kind == Token.LT) {
      match(Token.LT);
      parseRelExpr();
    } else if (currentToken.kind == Token.LTEQ) {
      match(Token.LTEQ);
      parseRelExpr();
    }
  }

  void parseAdditiveExpr() throws SyntaxError {

    parseMultiplicativeExpr();
    if (currentToken.kind == Token.PLUS){
      acceptOperator();
      parseAdditiveExpr();
    } else if (currentToken.kind == Token.MINUS){
      acceptOperator();
      parseAdditiveExpr();
    }
    // while (currentToken.kind == Token.PLUS) {
    //   acceptOperator();
    //   parseMultiplicativeExpr();
    // }
  }

  void parseMultiplicativeExpr() throws SyntaxError {

    parseUnaryExpr();
    if (currentToken.kind == Token.MULT){
      match(Token.MULT);
      parseMultiplicativeExpr();
    } else if (currentToken.kind == Token.DIV){
      match(Token.DIV);
      parseMultiplicativeExpr();
    }
  }

  void parseUnaryExpr() throws SyntaxError {

    switch (currentToken.kind) {
      case Token.MINUS:
        {
          acceptOperator();
          parseUnaryExpr();
        }
        break;
      case Token.PLUS:
        {
          acceptOperator();
          parseUnaryExpr();
        }
        break;
      case Token.NOT:
        {
          acceptOperator();
          parseUnaryExpr();
        }
        break;
      default:
        parsePrimaryExpr();
        break;
       
    }
  }

  void parsePrimaryExpr() throws SyntaxError {

    switch (currentToken.kind) {

      case Token.ID:
        parseIdent();
        if (currentToken.kind == Token.LPAREN){
          parseArgList();
        }else if(currentToken.kind == Token.LBRACKET){
          match(Token.LBRACKET);
          parseExpr();
          match(Token.RBRACKET);
        }
        break;

      case Token.LPAREN:
        {
          match(Token.LPAREN);
          // accept();
          parseExpr();
	        match(Token.RPAREN);
        }
        break;

      case Token.INTLITERAL:
        parseIntLiteral();
        break;
      case Token.FLOATLITERAL:
        parseFloatLiteral();
        break;
      case Token.BOOLEANLITERAL:
        parseBooleanLiteral();
        break;
      case Token.STRINGLITERAL:
        parseStringLiteral();
        break;
      default:
        // System.out.println(currentToken.spelling);
        syntacticError("illegal parimary expression", currentToken.spelling);
       
    }
  }

// ========================== LITERALS ========================

  // Call these methods rather than accept().  In Assignment 3, 
  // literal AST nodes will be constructed inside these methods. 

  void parseIntLiteral() throws SyntaxError {

    if (currentToken.kind == Token.INTLITERAL) {
      currentToken = scanner.getToken();
    } else 
      syntacticError("integer literal expected here", "");
  }

  void parseFloatLiteral() throws SyntaxError {

    if (currentToken.kind == Token.FLOATLITERAL) {
      currentToken = scanner.getToken();
    } else 
      syntacticError("float literal expected here", "");
  }

  void parseBooleanLiteral() throws SyntaxError {

    if (currentToken.kind == Token.BOOLEANLITERAL) {
      currentToken = scanner.getToken();
    } else 
      syntacticError("boolean literal expected here", "");
  }

  void parseStringLiteral() throws SyntaxError {

    if (currentToken.kind == Token.STRINGLITERAL){
      currentToken = scanner.getToken();
    } else
      syntacticError("string literal expected here", "");
  }
// ========================== PARAMETERS ========================

  void parseParaList() throws SyntaxError { //done
    // type has been dealt by parseType
    // identifier has been dealt by parseIdent
    match(Token.LPAREN);
    // maybe parseProperParaList
    if (currentToken.kind != Token.RPAREN) {
      parseProperParaList();
    }
    match(Token.RPAREN);
  }
  
  void parseProperParaList() throws SyntaxError { //done
    parseParaDecl();
    while (currentToken.kind == Token.COMMA){
      match(Token.COMMA);
      parseParaDecl();
    }
  }

  void parseParaDecl() throws SyntaxError {  //done
    parseType();
    parseDeclarator();
  }

  void parseArgList() throws SyntaxError {
    match(Token.LPAREN);
    if (currentToken.kind != Token.RPAREN) {
      parseProperArgList();
    }
    match(Token.RPAREN);
  }

  void parseProperArgList() throws SyntaxError {
    parseArg();
    while (currentToken.kind == Token.COMMA) {
      match(Token.COMMA);
      parseArg();
    }
  }

  void parseArg() throws SyntaxError {
    parseExpr();
  }
}
