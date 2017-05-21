package org.sc3d.apt.crazon.compiler.ast;

import org.sc3d.apt.sss.v3.*;

/** Represents what the grammar calls a 'subscript'. */
public abstract class Subscript extends ASTNode {
  /** This constructor is private. You should construct one of the inner
   * subclasses instead. */
  private Subscript(TT type) {
    this.type = type;
  }
  
  /* New API. */
  
  /** The type of the 'type' field. */
  public static enum TT {SQUARE, DOT};
  
  /** A value which identifies to which subclass this Subscript belongs. */
  public final TT type;

  /** Constructs a Subscript from 'raw', if possible. Otherwise, annotates 'raw'
   * with appropriate error messages and returns 'null'. */
  public static Subscript fromTree(Tree.Production raw) {
    if ("Square".equals(raw.name)) {
      final Tree.NonTerminal square = (Tree.NonTerminal)raw.getT(0).parse();
      if (square==null) return null;
      final Expression expr = Expression.fromTree(square.get(0));
      if (expr==null) return null;
      return new Square(raw.getT(0).t, expr);
    }
    if ("Dot".equals(raw.name)) {
      // The notation 'e.B' is syntactic sugar for 'e["B"]', i.e. subscripting
      // by a constant string. We don't try to de-sugar it here, because that
      // would involve creating new tokens or similar hacks.
      return new Dot(raw.getT(0).t, raw.getP(1).getT(0).t);
    }
    throw new RuntimeException("Unknown production: " + raw.name);
  }
  
  /** Constructs an ASTList to represent what the grammar calls a 'subscript*'.
   * */
  public static ASTList<Subscript> fromTrees(Tree.NonTerminal raw) {
    final ASTList<Subscript> ans = new ASTList<Subscript>(raw.length, null, "");
    for (int i=0; i<raw.length; i++) {
      ans.add(fromTree(raw.get(i)));
    }
    return ans.fix();
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Subscript with 'type==SQUARE', which represents what the
   * grammar calls a 'Square'. */
  public static class Square extends Subscript {
    /** Constructs a Square given values for its fields. */
    public Square(Token square, Expression expression) {
      super(TT.SQUARE);
      this.square = square;
      this.expression = expression;
    }
    
    /* New API. */
    
    /** The Token that represents the square brackets. This is provided so you
     * can call its 'addError()' method. */
    public final Token square;

    /** The Expression inside the square brackets. */
    public final Expression expression;

    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      final String newPrefix = prefix + "  ";
      sb.append("[").append(newPrefix);
      this.expression.toSourceCode(newPrefix, sb);
      sb.append(prefix).append("]"); // Outdent.
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Subscript with 'type==DOT', which represents what the
   * grammar calls a 'Dot'. */
  public static class Dot extends Subscript {
    /** Constructs a Dot given values for its fields. */
    public Dot(Token dot, Token id) {
      super(TT.DOT);
      this.dot = dot;
      this.id = id;
    }
    
    /* New API. */
    
    /** The "." Token. This is provided so you can call its 'addError()' method.
     * */
    public final Token dot;
    
    /** The Token which represents the CONSTANT or the IDENTIFIER after the dot.
     * */
    public final Token id;

    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      // The space ensures "1 .d0" does not turn into "1.d0".
      sb.append(" ").append(this.dot).append(this.id);
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
}

