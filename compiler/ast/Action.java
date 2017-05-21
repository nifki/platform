package org.sc3d.apt.crazon.compiler.ast;

import org.sc3d.apt.sss.v3.*;

/** Represents what the grammar calls a 'sprite'. */
public abstract class Action extends ASTNode {
  /** Constructor has private scope: use one of the inner classes.
   * The parameters are values for this.type and this.keyword.
   */
  private Action(TT type, Token keyword) {
    this.type = type;
    this.keyword = keyword;
  }

  /* New API. */
  
  /** The type of the 'type' field. */
  public static enum TT {SET, MOVE, HIDE, RESIZE, CLS};

  /** A value identifying of which subclass of Action this is an instance. */
  public final TT type;

  /** The keyword that introduces this Action. */
  public final Token keyword;
  
  /** Constructs an Action from 'raw' if possible. Otherwise annotates 'raw'
   * with appropriate error messages and returns 'null' .*/
  public static Action fromTree(Tree.Production raw) {
    if ("Set".equals(raw.name)) {
      final Expression obj = Expression.fromTree(raw.getP(1));
      final Expression val = Expression.fromTree(raw.getP(5));
      if (obj==null || val==null) return null;
      return new Set(
        raw.getT(0).t,
        obj,
        raw.getT(2).t,
        raw.getP(3).getT(0).t,
        raw.getT(4).t,
        val
      );
    }
    if ("Move".equals(raw.name)) {
      final Expression obj = Expression.fromTree(raw.getP(1));
      final ExpressionPair xy = ExpressionPair.fromTree(raw.getT(3));
      if (obj==null || xy==null) return null;
      return new Move(raw.getT(0).t, obj, ("To".equals(raw.getP(2).name)), xy);
    }
    if ("Hide".equals(raw.name)) {
      final Expression expr = Expression.fromTree(raw.getP(1));
      if (expr==null) return null;
      return new Hide(raw.getT(0).t, expr);
    }
    if ("Resize".equals(raw.name)) {
      final Expression obj = Expression.fromTree(raw.getP(1));
      final ExpressionPair xy = ExpressionPair.fromTree(raw.getT(3));
      if (obj==null || xy==null) return null;
      return new Resize(
        raw.getT(0).t,
        obj,
        ("To".equals(raw.getP(2).name)),
        xy
      );
    }
    if ("Cls".equals(raw.name)) {
      return new Cls(raw.getT(0).t);
    }
    throw new RuntimeException("Unknown production: "+raw.name);
  }

  /////////////////////////////////////////////////////////////////////////

  /** The subclass which represents a SET action and has 'type==TT.SET'. */
  public static class Set extends Action {
    /** Constructs a Set. */
    public Set(
      Token keyword,
      Expression object,
      Token dot,
      Token attribute,
      Token eq,
      Expression value
    ) {
      super(TT.SET, keyword);
      this.object = object;
      this.dot = dot;
      this.attribute = attribute;
      this.eq = eq;
      this.value = value;
    }

    /** The object that this SET acts on. */
    public final Expression object;
    
    /** The "." Token. */
    public final Token dot;
    
    /** The Token that represents the name of the attribute. */
    public final Token attribute;
    
    /** The "=" Token. */
    public final Token eq;
    
    /** The new value for the attribute. */
    public final Expression value;
      
    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword);
      sb.append(" ");
      this.object.toSourceCode(prefix, sb);
      sb.append(" ."); // avoid confusion with "1."
      sb.append(this.attribute);
      sb.append(" = ");
      this.value.toSourceCode(prefix, sb);
    }
  }
  
  /////////////////////////////////////////////////////////////////////////

  /** The subclass which represents a "MOVE" Action and has 'type==TT.MOVE'. */
  public static class Move extends Action {
    /** Constructs a Move. */
    public Move(
      Token keyword,
      Expression object,
      boolean isAbsolute,
      ExpressionPair xy
    ) {
      super(TT.MOVE, keyword);
      this.object = object;
      this.isAbsolute = isAbsolute;
      this.xy = xy;
    }

    /* New API. */
    
    /** The object that this Action moves. */
    public final Expression object;
    
    /** 'true' if this Action moves the object to an absolute position ("TO"),
     * 'false' if it moves the object relative to its previous position ("BY").
     **/
    public final boolean isAbsolute;
    
    /** The coordinates of the "MOVE" Action. */
    public final ExpressionPair xy;
    
    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword);
      sb.append(" ");
      this.object.toSourceCode(prefix, sb);
      sb.append(isAbsolute ? " TO " : " BY ");
      this.xy.toSourceCode(prefix, sb);
    }
  }
  
  /////////////////////////////////////////////////////////////////////////

  /** The subclass which represents a HIDE action and has 'type==TT.HIDE'. */
  public static class Hide extends Action {
    /** Constructs a Hide. */
    public Hide(Token keyword, Expression object) {
      super(TT.HIDE, keyword);
      this.object = object;
    }

    /* New API. */
    
    /** The object that this Action hides. */
    public final Expression object;
    
    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword);
      sb.append(" ");
      this.object.toSourceCode(prefix, sb);
    }
  }
  
  /////////////////////////////////////////////////////////////////////////

  /** The subclass which represents a "RESIZE" Action and has 'type==TT.RESIZE'.
   * */
  public static class Resize extends Action {
    /** Constructs a Resize. */
    public Resize(
      Token keyword,
      Expression object,
      boolean isAbsolute,
      ExpressionPair xy
    ) {
      super(TT.RESIZE, keyword);
      this.object = object;
      this.isAbsolute = isAbsolute;
      this.xy = xy;
    }

    /* New API. */
    
    /** The object that this Action resizes. */
    public final Expression object;
    
    /** 'true' if this Action resizes the object to an absolute size ("TO"),
     * 'false' if it resizes the object relative to its previous size
     * ("BY"). */
    public final boolean isAbsolute;
    
    /** The scale factors of this "RESIZE" Action. */
    public final ExpressionPair xy;
    
    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword);
      sb.append(" ");
      this.object.toSourceCode(prefix, sb);
      sb.append(isAbsolute ? " TO " : " BY ");
      this.xy.toSourceCode(prefix, sb);
    }
  }
  
  /////////////////////////////////////////////////////////////////////////

  /** The subclass which represents a CLS action and has 'type==TT.CLS'. */
  public static class Cls extends Action {
    /** Constructs a Cls. */
    public Cls(Token keyword) {
      super(TT.CLS, keyword);
    }

    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword);
    }
  }
}
