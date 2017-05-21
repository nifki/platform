package org.sc3d.apt.crazon.compiler;

import org.sc3d.apt.crazon.compiler.ast.*;

import java.util.*;
import org.sc3d.apt.sss.v3.*;

public class Parser extends org.sc3d.apt.sss.v3.Parser {
  /** This constructor is private; use the 'PARSER' field instead. */
  private Parser() { super(GRAMMAR); }
  
  /* New API. */
  
  /** The Grammar of prover statements. */
  public static final Grammar GRAMMAR = GrammarParser.fromInputStream(
    Parser.class.getResourceAsStream("grammar.sss")
  );
  
  /** The unique instance of 'Parser'. Use this instead of constructing your
   * own. */
  public static final Parser PARSER = new Parser();
  
  /* Override things in Parser. */
  
  public Object postProcess(Tree raw) {
    final Tree.NonTerminal rawNT = (Tree.NonTerminal)raw;
    final ASTList<Declaration> ans = Declaration.fromTrees(rawNT);
    if (ans==null) return null;
    final HashMap<String, Token> locals = new HashMap<String, Token>();
    final HashMap<String, Token> globals = new HashMap<String, Token>();
    ans.findAssignments(locals, globals);
    return ans;
  }
  
  /* Test code. */
  
  public static void main(String[] args) throws java.io.IOException {
    if (args.length==0) {
      PARSER.interactiveTest();
      return;
    }
    for (int i=0; i<args.length; i++) {
      System.out.println("\n\n\nProcessing "+args[i]+":");
      final Sentence sentence = Sentence.readFile(args[i]);
      final Object result = PARSER.parse(sentence);
      if (sentence.countErrors()>0) {
        sentence.printErrorReport(System.out, 10);
      } else {
        System.out.println("Parsed okay, result = \n"+result);
      }
    }
  }
}
