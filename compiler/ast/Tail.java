package org.sc3d.apt.crazon.compiler.ast;

import org.sc3d.apt.sss.v3.*;

public class Tail extends ASTNode {
  /** constructs a Tail given values for its fields. */
  public Tail(Infix infix, Head head) {
    this.infix = infix;
    this.head = head;
  }
  
  /* New API. */
  
  /** The infix operator. */
  public final Infix infix;
  
  /** The operand, including its prefix and postfix operators. */
  public final Head head;
  
  /** Constructs a Tail for 'raw' if possible. Otherwise, annotates 'raw' with
   * appropriate error messages and returns 'null'. */
  public static Tail fromTree(Tree.Production raw) {
    final Infix infix = Infix.fromTree(raw.getP(0));
    final Head head = Head.fromTree(raw.getP(1));
    if (infix==null || head==null) return null;
    return new Tail(infix, head);
  }
  
  /** Constructs an ASTList<Tail> from 'raw', which represents what the grammar
   * calls a 'tail*', if possible. Otherwise, annotates 'raw' with appropriate
   * error messages and returns 'null'. */
  public static ASTList<Tail> fromTrees(Tree.NonTerminal raw) {
    final ASTList<Tail> ans = new ASTList<Tail>(raw.length, null, " ");
    for (int i=0; i<raw.length; i++) {
      ans.add(fromTree(raw.get(i)));
    }
    return ans.fix();
  }
  
  /* Override things in ASTNode. */
  
  public void toSourceCode(String prefix, StringBuffer sb) {
    this.infix.toSourceCode(prefix, sb);
    sb.append(" ");
    this.head.toSourceCode(prefix, sb);
  }
}
