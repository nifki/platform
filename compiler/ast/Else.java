package org.sc3d.apt.crazon.compiler.ast;

import org.sc3d.apt.sss.v3.*;

import java.util.*;

/** Represents what the grammar calls an 'else'. */
public class Else extends ASTNode {
  /** Constructs an Else given values for its fields. */
  public Else(Token els, ASTList<Statement> statements) {
    this.els = els;
    this.statements = statements;
  }
  
  /* New API. */
  
  /** The Token which represents the "ELSE" keyword. */
  public final Token els;
  
  /** The statements in the braces after the "ELSE" keyword. */
  public final ASTList<Statement> statements;
  
  /** Constructs an Else from 'raw' if possible. Otherwise, annotates 'raw' with
   * apropriate error messages and returns 'null'. */
  public static Else fromTree(Tree.Production raw) {
    final ASTList<Statement> statements = Statement.fromTrees(raw.getT(1));
    if (statements==null) return null;
    return new Else(raw.getT(0).t, statements);
  }
  
  /* Override things in ASTNode. */
  
  public void toSourceCode(String prefix, StringBuffer sb) {
    sb.append(this.els);
    sb.append(" ");
    this.statements.toSourceCode(prefix, sb);
  }
  
  public void findAssignments(
    HashMap<String, Token> locals,
    HashMap<String, Token> globals
  ) {
    this.statements.findAssignments(locals, globals);
  }
}
