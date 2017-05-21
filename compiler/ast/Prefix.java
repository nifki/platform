package org.sc3d.apt.crazon.compiler.ast;

import org.sc3d.apt.sss.v3.*;

/** Represents what the grammar calls a 'prefix', which represents a prefix
 * operator. */
public class Prefix extends ASTNode {
  /** Constructs a Prefix. */
  public Prefix(Token keyword) {
    this.keyword = keyword;
  }
  
  /* New API. */
  
  /** The Token that represents the keyword, one of "-", "FLOOR", "ROUND",
   * "CEIL", "ABS", "SGN", "SQRT", "LEN", "STR", "VAL", "TRIM", "UPPER", "LOWER"
   * and "NOT". */
  public final Token keyword;
  
  /** Constructs a Prefix from 'raw' if possible. Otherwise, annotates 'raw'
   * with appropriate error messages and returns 'null'. */
  public static Prefix fromTree(Tree.Production raw) {
    // We trust SSS to obey the grammar.
    return new Prefix(raw.getT(0).t);
  }
  
  /** Constructs an ASTList<Prefix> from a Tree.NonTerminal that represents a
   * 'prefix*'. */
  public static ASTList<Prefix> fromTrees(Tree.NonTerminal raw) {
    final ASTList<Prefix> ans = new ASTList<Prefix>(raw.length, null, " ");
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
