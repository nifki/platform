package org.sc3d.apt.crazon.compiler.ast;

import org.sc3d.apt.sss.v3.*;

/** Represents what the grammar calls an 'infix', which represents a infix
 * operator. */
public class Infix extends ASTNode {
  /** Constructs an Infix. */
  public Infix(Token keyword) {
    this.keyword = keyword;
  }
  
  /* New API. */
  
  /** The Token that represents the keyword, one of "**", "*", "/", "%", "+",
   * "-", "MIN", "MAX", "==", "<", ">", "<>", "!=", ">=", "<=", "XOR", "AND",
   * "OR". */
  public final Token keyword;
  
  /** Constructs an Infix from 'raw' if possible. Otherwise, annotates 'raw'
   * with appropriate error messages and returns 'null'. */
  public static Infix fromTree(Tree.Production raw) {
    // We trust SSS to obey the grammar.
    return new Infix(raw.getT(0).t);
  }
  
  /** Constructs an ASTList<Infix> from a Tree.NonTerminal that represents a
   * 'infix*'. */
  public static ASTList<Infix> fromTrees(Tree.NonTerminal raw) {
    final ASTList<Infix> ans = new ASTList<Infix>(raw.length, null, " ");
    for (int i=0; i<raw.length; i++) {
      ans.add(fromTree(raw.get(i)));
    }
    return ans.fix();
  }
  
  /* Override things in ASTNode. */
  
  public void toSourceCode(String prefix, StringBuffer sb) {
    sb.append(this.keyword);
  }
}
