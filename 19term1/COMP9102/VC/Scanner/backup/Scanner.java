/**
 **	Scanner.java                        
 **/

package VC.Scanner;

import VC.ErrorReporter;

public final class Scanner { 

  private SourceFile sourceFile;
  private boolean debug;

  private ErrorReporter errorReporter;
  private StringBuffer currentSpelling;
  private char currentChar;
  private SourcePosition sourcePos;

  //counters for line and column numbers
  private int lineNumber;
  private int columnNumber;
  private int theLineNum, theCharStart, theCharFinish;

// =========================================================

  public Scanner(SourceFile source, ErrorReporter reporter) {
    sourceFile = source;
    errorReporter = reporter;
    currentChar = sourceFile.getNextChar();
    debug = false;

    lineNumber = 1;
    columnNumber = 1;
    // you may initialise your counters for line and column numbers here
  }

  public void enableDebugging() {
    debug = true;
  }

  // accept gets the next character from the source program.

  private void accept() {
    currentSpelling.append(currentChar);
    currentChar = sourceFile.getNextChar();
    columnNumber ++;
  // you may save the lexeme of the current token incrementally here
  // you may also increment your line and column counters here
  }
  // inspectChar returns the n-th character after currentChar
  // in the input stream. 
  //
  // If there are fewer than nthChar characters between currentChar 
  // and the end of file marker, SourceFile.eof is returned.
  // 
  // Both currentChar and the current position in the input stream
  // are *not* changed. Therefore, a subsequent call to accept()
  // will always return the next char after currentChar.

  private char inspectChar(int nthChar) {
    return sourceFile.inspectChar(nthChar);
  }
  private void skipChar(int n){
    for(int i = 0; i < n; i++){
      if(currentChar == '\n'){
        lineNumber ++;
        columnNumber = 1;
        currentChar = sourceFile.getNextChar();
      } else if (currentChar == '\t'){
        columnNumber += 9 - columnNumber % 8;
        currentChar = sourceFile.getNextChar();
      } else {
        columnNumber ++;
        currentChar = sourceFile.getNextChar();
      }
    }
  }
  private void recogniseDigits(){ // collect digits
    switch(currentChar){
      case '1': case '2': case '3': case '4': case '5':
      case '6': case '7': case '8': case '9': case '0':
        //digits+
        accept();
        recogniseDigits();
      default:
        break;
    }
  }

  private void collectLetters(){
    // accept letters from [a-zA-Z_]
    while((currentChar>='A'&&currentChar<='Z') || (currentChar>='a'&&currentChar<='z') || currentChar == '_'|| (currentChar >= '0' && currentChar <= '9')){
      accept();
    }
  }

  private int nextToken() {
    // for each line ,there shall be a '\n' at the end of the line
  // Tokens: separators, operators, literals, identifiers and keyworods
    String errorMessage;
    SourcePosition errorPos;
    String tokenName;
    switch (currentChar) {
       // separators 
    case '{': theCharStart=theCharFinish=columnNumber; accept(); return Token.LCURLY;
    case '}': theCharStart=theCharFinish=columnNumber; accept(); return Token.RCURLY;
    case '(': theCharStart=theCharFinish=columnNumber; accept(); return Token.LPAREN;
    case ')': theCharStart=theCharFinish=columnNumber; accept(); return Token.RPAREN;
    case '[': theCharStart=theCharFinish=columnNumber; accept(); return Token.LBRACKET;
    case ']': theCharStart=theCharFinish=columnNumber; accept(); return Token.RBRACKET;
    case ';': theCharStart=theCharFinish=columnNumber; accept(); return Token.SEMICOLON;
    case ',': theCharStart=theCharFinish=columnNumber; accept(); return Token.COMMA;
    //---------------------------------------
    // literals
    case '1': case '2': case '3': case '4': case '5':
    case '6': case '7': case '8': case '9': case '0':
    // might be integer
      theCharStart = columnNumber;
      accept();
      recogniseDigits(); // collect all digits
      if(currentChar != '.' && currentChar != 'E' && currentChar != 'e'){
        // in this case, it cannot be a float.
        theCharFinish = columnNumber - 1;
        return Token.INTLITERAL;
      } else {
        // might be a float
        switch(currentChar){
          case '.':
            // it must be a float, same case as follow
            accept();
            switch(currentChar){
              case '1': case '2': case '3': case '4': case '5':
              case '6': case '7': case '8': case '9': case '0':
                // followed by digits
                // eg 1.2
                accept();
                recogniseDigits();
                if(currentChar == 'E' || currentChar == 'e'){
                  //exponent?
                  if(inspectChar(1) == '+' || inspectChar(1) == '-'){
                    //case 1: digits
                    switch(inspectChar(2)){
                      case '1': case '2': case '3': case '4': case '5':
                      case '6': case '7': case '8': case '9': case '0':
                        accept();
                        accept();
                        recogniseDigits();
                        theCharFinish = columnNumber - 1;
                        return Token.FLOATLITERAL;
                      //case 2: E not represents a exponet
                      default:
                        theCharFinish = columnNumber - 1;
                        return Token.FLOATLITERAL;
                    }
                  } else {
                  // exponent without '+' or '-' or another token
                    switch(inspectChar(1)){
                      //case 1:digits
                      case '1': case '2': case '3': case '4': case '5':
                      case '6': case '7': case '8': case '9': case '0':
                        accept();
                        recogniseDigits();
                        theCharFinish = columnNumber - 1;
                        return Token.FLOATLITERAL;
                      //case 2: error
                      default:
                        theCharFinish = columnNumber - 1;
                        return Token.INTLITERAL;
                    }
                  }
                } else {
                  //float without exponent
                  theCharFinish = columnNumber - 1;
                  return Token.FLOATLITERAL;
                }
              case 'E': case 'e':
                if(inspectChar(1) == '+' || inspectChar(1) == '-'){
                  if(inspectChar(2) >= '0' && inspectChar(2) <= '9'){ // afterward is a number
                    // accept, float
                    accept(); //accept E or e
                    accept(); // accept + or -
                    recogniseDigits();
                    theCharFinish = columnNumber - 1;
                    return Token.FLOATLITERAL;
                  } else {
                    //reject
                    return Token.FLOATLITERAL;
                  }
                } else {
                  if(inspectChar(1) >= '0' && inspectChar(1) <= '9'){
                    accept();
                    recogniseDigits();
                    theCharFinish = columnNumber - 1;
                    return Token.FLOATLITERAL;
                  } else {
                    theCharFinish = columnNumber - 1;
                    return Token.FLOATLITERAL;
                  }
                }
              default:
                break;
            }
            theCharFinish = columnNumber - 1;
            return Token.FLOATLITERAL;
          case 'E':
          case 'e':
          // digits+ exponent
          // inspect rather than accept
            if(inspectChar(1) == '+' || inspectChar(1) == '-'){
              if(inspectChar(2) >= '0' && inspectChar(2) <= '9'){
                // accept, float
                accept();
                accept();
                recogniseDigits();
                theCharFinish = columnNumber - 1;
                return Token.FLOATLITERAL;
              } else {
                //reject
                return Token.INTLITERAL;
              }
            } else {
              if(inspectChar(1) >= '0' && inspectChar(1) <= '9'){
                accept();
                recogniseDigits();
                theCharFinish = columnNumber - 1;
                return Token.FLOATLITERAL;
              } else {
                theCharFinish = columnNumber - 1;
                return Token.INTLITERAL;
              }
            }
        }
      }
    // otherwise float
    case '.':
        //  attempting to recognise a float
        // the backward must contains digits or a exponent
      theCharStart = columnNumber;
      accept();
      switch(currentChar){
        case '1': case '2': case '3': case '4': case '5':
        case '6': case '7': case '8': case '9': case '0':
          //.digits+
          accept();
          recogniseDigits();
          if(currentChar == 'E' || currentChar == 'e'){
            //.fraction exponent?
            // inspect chars
            if(inspectChar(1) == '+' || inspectChar(1) == '-'){
              //case 1: digits
              switch(inspectChar(2)){
                case '1': case '2': case '3': case '4': case '5':
                case '6': case '7': case '8': case '9': case '0':
                  //accept iterals as float
                  accept(); // accept 'E' or 'e'
                  accept(); // accept '+' or '-'
                  recogniseDigits();  // accept all digits
                  theCharFinish = columnNumber - 1;
                  return Token.FLOATLITERAL;
                //case 2: error
                default:
                  // eg. .2E+ shall be 3 tokens, only return .2 part as float
                  theCharFinish = columnNumber - 1;
                  return Token.FLOATLITERAL;
              }
            } else {
              switch(inspectChar(1)){ // inspect the next char of 'E' or 'e'
                //case 1:digits, exponent without '+' or '-'
                case '1': case '2': case '3': case '4': case '5':
                case '6': case '7': case '8': case '9': case '0':
                  accept(); // accept 'E' or 'e'
                  recogniseDigits();  // accept all digits afterwards
                  theCharFinish = columnNumber - 1;
                  return Token.FLOATLITERAL;
                // case 2: error
                // eg .2E
                default:
                  theCharFinish = columnNumber - 1;
                  return Token.ERROR;
              }
            }
          }
          //float without exponent
          theCharFinish = columnNumber - 1;
          return Token.FLOATLITERAL;
        default:
        // in this case, there is only a single dot, which is a error
          break;
      }
      theCharFinish = columnNumber - 1;
      return Token.ERROR;
    // identifier
    case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': 
    case 'I': case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': case 'P': 
    case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': 
    case 'Y': case 'Z': case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': 
    case 'g': case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n': 
    case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u': case 'v': 
    case 'w': case 'x': case 'y': case 'z': case '_':
      theCharStart = columnNumber;
      accept(); //accept the first alphabet
      collectLetters();
      theCharFinish = columnNumber - 1;
      if(currentSpelling.toString().equals("true") || currentSpelling.toString().equals("false")){
        return Token.BOOLEANLITERAL;
      } else {
        return Token.ID;
      }
    //operators
    case '+': theCharStart=theCharFinish=columnNumber; accept(); return Token.PLUS;
    case '-': theCharStart=theCharFinish=columnNumber; accept(); return Token.MINUS;
    case '*': theCharStart=theCharFinish=columnNumber; accept(); return Token.MULT;
    case '/': theCharStart=theCharFinish=columnNumber; accept(); return Token.DIV;
    case '!':
      theCharStart = columnNumber;
      accept();
      if (currentChar == '='){
        theCharFinish = columnNumber;
        accept();
        return Token.NOTEQ;
      } else {
        theCharFinish = columnNumber - 1;
        return Token.NOT;
      }
    case '=':
      theCharStart = columnNumber;
      accept();
      if(currentChar == '='){
        theCharFinish = columnNumber;
        accept();
        return Token.EQEQ;
      }else{
        theCharFinish = columnNumber - 1;
        return Token.EQ;
      }
    case '<':
      theCharStart = columnNumber;
      accept();
      if(currentChar == '='){
        theCharFinish = columnNumber;
        accept();
        return Token.LTEQ;
      } else {
        theCharFinish = columnNumber - 1;
        return Token.LT;
      }
    case '>':
      theCharStart = columnNumber;
      accept();
      if(currentChar == '='){
        theCharFinish = columnNumber;
        accept();
        return Token.GTEQ;
      } else {
        theCharFinish = columnNumber - 1;
        return Token.GT;
      }
    case '&':
      theCharStart = columnNumber;
      accept();
      if(currentChar == '&'){
        theCharFinish = columnNumber;
        accept();
        return Token.ANDAND;
      } else {
        theCharFinish = columnNumber - 1;
        return Token.ERROR;
      }
    case '|':	
      theCharStart = columnNumber;
       	accept();
      	if (currentChar == '|') {
          theCharFinish = columnNumber;
          accept();
	        return Token.OROR;
      	} else {
          theCharFinish = columnNumber - 1;
	        return Token.ERROR;
        }
    // string
    case '\"':
      // case 1: a string
      // case 2: unterminated error
      theCharStart = columnNumber;
      skipChar(1);
      while(currentChar != '\n' && currentChar != SourceFile.eof && currentChar != '\"'){
        switch(currentChar){
          case '\\':
            // once encounter a escape
            // transform it with char afterward
            switch(inspectChar(1)){
              case 'b':currentSpelling.append('\b'); skipChar(2); break;
              case 'f':currentSpelling.append('\f'); skipChar(2); break;
              case 'n':currentSpelling.append('\n'); skipChar(2); break;
              case 'r':currentSpelling.append('\r'); skipChar(2); break;
              case 't':currentSpelling.append('\t'); skipChar(2); break;
              case '\'':currentSpelling.append('\''); skipChar(2); break;
              case '\"':currentSpelling.append('\"'); skipChar(2); break;
              case '\\': currentSpelling.append('\\'); skipChar(2); break;
              default:
                // also print out the error
                // illegal escape character
                theCharFinish = columnNumber;
                errorPos = new SourcePosition(lineNumber, theCharStart, theCharFinish);
                errorMessage = "%: illegal escape character";
                tokenName = "" + currentChar + inspectChar(1);
                errorReporter.reportError(errorMessage, tokenName, errorPos);
                accept();
                accept();
                break;
            }
            break;
          default:
            accept();
            break;
        }
      }
      if(currentChar == '\n' || currentChar == SourceFile.eof){  // unterminated
        // print unterminated string error
        theCharFinish = columnNumber - 1;
        errorPos = new SourcePosition(lineNumber, theCharStart, theCharStart);
        errorMessage = "%: unterminated string";
        tokenName = currentSpelling.toString(); // pop out the redundent quto
        errorReporter.reportError(errorMessage, tokenName, errorPos);
        return Token.STRINGLITERAL;
        // return token
      } else {
        skipChar(1);
        // return token
        theCharFinish = columnNumber - 1;
        return Token.STRINGLITERAL;
      }
    // ....
    case SourceFile.eof:
      theCharStart = theCharFinish = columnNumber;	
	    currentSpelling.append(Token.spell(Token.EOF));
	    return Token.EOF;
    default:
	    break;
    }
    theCharStart = theCharFinish = columnNumber;
    // an error token detected
    if(currentChar != '\n' && currentChar != '\t'){
      accept();
      return Token.ERROR;
    } else if (currentChar == '\n'){
      currentChar = sourceFile.getNextChar();
      lineNumber ++;
      columnNumber = 1;
      skipSpaceAndComments();
      return nextToken();
    } else {
        // current char is \t
        // dealing with tab
      skipChar(1);
      skipSpaceAndComments();
      return nextToken();
    }
  }

  void skipSpaceAndComments() {
    String errorMessage;
    String tokenName;
    SourcePosition errorPos;
    int errorLineNum;
    //skip empty line
    //skip heading space
    while(currentChar == ' '){
      currentChar = sourceFile.getNextChar();
      columnNumber ++;
    }
    if(currentChar == '/'){ // may also be a operator DIV
      theCharStart = columnNumber;
      errorLineNum = lineNumber;
      if(inspectChar(1) == '/'){  // A line comment
        while(currentChar != '\n' && currentChar != SourceFile.eof){
          currentChar = sourceFile.getNextChar();
          columnNumber ++;
        }
        if (currentChar == '\n'){
          columnNumber = 1;
          lineNumber ++;
          currentChar = sourceFile.getNextChar();
        }
        skipSpaceAndComments(); // may be multiple lines
      }else if(inspectChar(1) == '*'){  //A block comment
        while((currentChar != '*' || inspectChar(1) != '/') && inspectChar(1)!= SourceFile.eof){
        //move on, looking for terminator, until the end of last letter before $ or found terminator
        //it may involve a infinite loop
          currentChar = sourceFile.getNextChar();
          columnNumber ++;
          if (currentChar == '\n'){
            // a block comments may involves multiple lines
            columnNumber = 1;
            lineNumber ++;
          }
        }
        if(currentChar == '*' && inspectChar(1) == '/'){
          //case 1: find terminator of comment
          skipChar(2);
          skipSpaceAndComments(); //recursive
        } else {
          // may need to hanle error message if there is no terminator
          errorMessage = "%: unterminated comment";
          errorPos = new SourcePosition(errorLineNum, theCharStart, theCharStart);
          tokenName = "";
          errorReporter.reportError(errorMessage, tokenName, errorPos);
          // drop the rest input
          currentChar = sourceFile.getNextChar();
        }
      }
    }
  }

  public Token getToken() {
    Token tok;
    int kind;

    // skip white space and comments

   skipSpaceAndComments();

   currentSpelling = new StringBuffer("");

   sourcePos = new SourcePosition();

   // You must record the position of the current token somehow

   kind = nextToken();
   sourcePos = new SourcePosition(lineNumber, theCharStart, theCharFinish);
   tok = new Token(kind, currentSpelling.toString(), sourcePos);

   // * do not remove these three lines
   if (debug)
     System.out.println(tok);
   return tok;
   }
}
