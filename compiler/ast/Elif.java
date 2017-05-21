package org.sc3d.apt.crazon.compiler.ast;

import org.sc3d.apt.sss.v3.*;

import java.util.*;

/** Represents what the grammar calls an 'elif'. */
public class Elif extends ASTNode {
  /** Constructs an Elif given values for its fields. */
  public Elif(
    Token keyword,
    Expression condition,
    ASTList<Statement> statements
  ) {
    this.keyword = keyword;
    this.condition = condition;
    this.statements = statements;
  }
  
  /* New API. */
  
  /** The Token which represents the "ELIF" keyword. */
  public final Token keyword;
  
  /** The Expression that represents the condition tested by this Elif. */
  public final Expression condition;
  
  /** The statements in the braces after the "ELIF" keyword. */
  public final ASTList<Statement> statements;
  
  /** Constructs an Elif from 'raw' if possible. Otherwise, annotates 'raw' with
   * apropriate error messages and returns 'null'. */
  public static Elif fromTree(Tree.Production raw) {
    final Expression condition = Expression.fromTree(raw.getP(1));
    final ASTList<Statement> statements = Statement.fromTrees(raw.getT(2));
    if (condition==null || statements==null) return null;
    return new Elif(raw.getT(0).t, condition, statements);
  }

  /** Constructs an ASTList<Elif> from 'raw', which represents what the
   * grammar calls a 'elif*'. If there are any errors, this method annotates
   * 'raw' with appropriate error messages and returns 'null'.
   */
  public static ASTList<Elif> fromTrees(Tree.NonTerminal raw) {
    final ASTList<Elif> ans = new ASTList<Elif>(raw.length, null, null);
    for (int i=0; i<raw.length; i++) ans.add(fromTree(raw.get(i)));
    return ans.fix();
  }

  /* Override things in ASTNode. */
  
  public void toSourceCode(String prefix, StringBuffer sb) {
    sb.append(this.keyword).append(' ');
    this.condition.toSourceCode(prefix, sb);
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
