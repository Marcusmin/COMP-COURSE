/*
 * SourceFile.java                   
 */
import java.io.FileReader; // A class to read character files
import java.io.BufferedReader; // A buffer holds the files(othewise will cause frequent I/O)
import java.io.LineNumberReader; // A reader which is able to record line number of reader

public class SourceFile {

  static final char eof = '\u0000'; //definition of SourceFile.eof
  private LineNumberReader reader;  //a reader

  public SourceFile(String filename) {  // A special LineNumberReader
    try {
      reader = new LineNumberReader(new BufferedReader(new FileReader(filename)));  //open the specific file
    } catch (java.io.FileNotFoundException e) {
      System.out.println("[# vc #]: can't read: " + filename);
      System.exit(1);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Caught IOException: " + e.getMessage());
      System.exit(1);
    }
  }

  char getNextChar() {  //read the next char of reader and return it
    try {
      int  c = reader.read();
      if (c == -1) c = eof;
      return (char) c;
    } catch (java.io.IOException e) {
      System.out.println("Caught IOException: " + e.getMessage());
      return eof;
    }
  }

  char inspectChar(int nthChar) { //read the nth char of the reader
  // nthChar must be >= 1.
    int c;

    try {
    reader.mark(nthChar);
    do {
      c = reader.read();
      nthChar --;
    } while (nthChar != 0);
    reader.reset();
    if (c == -1) c = eof;
    return (char) c;
    } catch (java.io.IOException e) {
      System.out.println("Caught IOException: " + e.getMessage());
      return eof;
    }
  }
  public static void main(String[] args) {
      SourceFile s = new SourceFile(args[0]);
      System.out.println(s.getNextChar());
      System.out.println(s.inspectChar(5));
      System.out.println(s.getNextChar());
      System.out.println(s.inspectChar(5));
  }
}
