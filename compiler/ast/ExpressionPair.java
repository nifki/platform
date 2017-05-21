package org.sc3d.apt.crazon.compiler.ast;

import org.sc3d.apt.sss.v3.*;

/** Represents what the grammar calls a 'ROUND(expressionPair)'. */
public class ExpressionPair extends ASTNode {
  /** Constructs an ExpressionPair given values for its fields. */
  public ExpressionPair(Token round, Expression x, Expression y) {
    this.round = round;
    this.x = x; this.y = y;
  }
  
  /* New API. */
  
  /** The Token that represents the entire bracket. */
  public final Token round;
  
  /** One of the two Expressions, or 'null' if it was omitted. */
  public final Expression x, y;
  
  /** Constructs an ExpressionPair from 'raw' if possible. Otherwise, annotates
   * 'raw' with appropriate error messages and returns 'null'. Note that unlike
   * most other subclasses of ASTNode, 'raw' is a Tree.Terminal, because it
   * represents something in brackets. */
  public static ExpressionPair fromTree(Tree.Terminal raw) {
    final Tree.NonTerminal contents = (Tree.NonTerminal)raw.parse();
    if (contents==null) return null;
    final boolean hasX = contents.get().getNT(0).length>0;
    final Expression x =
      hasX ? Expression.fromTree(contents.get().getP(0)) : null;
    final boolean hasY = contents.get().getNT(2).length>0;
    final Expression y =
      hasY ? Expression.fromTree(contents.get().getP(2)) : null;
    if ((hasX && x==null) || (hasY && y==null)) return null;
    if (x==null && y==null) {
      raw.t.addError("You must provide at least one of the components.");
      return null;
    }
    return new ExpressionPair(raw.t, x, y);
  }
  
  /* Override things in ASTNode. */
  
  public void toSourceCode(String prefix, StringBuffer sb) {
    final String newPrefix = prefix + "  ";
    sb.append("(").append(newPrefix);
    if (this.x!=null) this.x.toSourceCode(newPrefix, sb);
    sb.append(", ");
    if (this.y!=null) this.y.toSourceCode(newPrefix, sb);
    sb.append(prefix).append(")");
  }
}
