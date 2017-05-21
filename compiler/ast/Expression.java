package org.sc3d.apt.crazon.compiler.ast;

import org.sc3d.apt.sss.v3.*;

/** Represents what the grammar calls an 'expression'. */
public class Expression extends ASTNode {
  /** Constructs an Expression given values for its fields. */
  public Expression(Head head, ASTList<Tail> tails) {
    this.head = head;
    this.tails = tails;
  }
  
  /* New API. */

  /** The first operand in this Expression. */
  public final Head head;
  
  /** The other operands in this Expression. */
  public final ASTList<Tail> tails;
  
  /** Constructs an Expression from 'raw' if possible. Otherwise annotates 'raw'
   * with appropriate error messages and returns 'null' .*/
  public static Expression fromTree(Tree.Production raw) {
    final Head head = Head.fromTree(raw.getP(0));
    final ASTList<Tail> tails = Tail.fromTrees(raw.getNT(1));
    if (head==null || tails==null)
      return null; // Will already be error-annotated.
    return new Expression(head, tails);
  }
  
  /* Override things in ASTNode. */
  
  public void toSourceCode(String prefix, StringBuffer sb) {
    this.head.toSourceCode(prefix, sb);
    if (this.tails.length!=0) sb.append(" ");
    this.tails.toSourceCode(prefix, sb);
  }
}
