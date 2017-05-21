package org.sc3d.apt.crazon.compiler.ast;

import org.sc3d.apt.sss.v3.*;

/** Represents what the grammar calls an 'object'. This class is called "Obj" not "Object" to avoid a clash with java.lang.Object. */
public abstract class Obj extends ASTNode {
  /** Constructor has private scope: use one of the inner classes.
   * The parameters are values for this.type and this.keyword.
   */
  private Obj(TT type, Token keyword) {
    this.type = type;
    this.keyword = keyword;
  }

  /* New API. */
  
  /** The type of the 'type' field. */
  public static enum TT {SPRITE, TEXT, WINDOW};

  /** A value identifying of which subclass of Obj this is an instance. */
  public final TT type;

  /** The keyword that introduces this Obj. */
  public final Token keyword;
  
  /** Constructs an Obj from 'raw' if possible. Otherwise annotates 'raw'
   * with appropriate error messages and returns 'null' .*/
  public static Obj fromTree(Tree.Production raw) {
    if ("Sprite".equals(raw.name)) {
      final Tree.NonTerminal contents = (Tree.NonTerminal)raw.getT(1).parse();
      final Expression expression =
        contents==null ? null : Expression.fromTree(contents.get());
      final boolean hasSize = raw.getNT(2).length>0;
      final ExpressionPair size = hasSize
        ? ExpressionPair.fromTree(raw.getP(2).getT(1))
        : null;
      if (expression==null || (hasSize && size==null)) return null;
      return new Sprite(raw.getT(0).t, expression, size);
    }
    if ("Text".equals(raw.name)) {
      final Expression expression = Expression.fromTree(raw.getP(1));
      if (expression==null) return null;
      return new Text(raw.getT(0).t, expression);
    }
    if ("Window".equals(raw.name)) {
      return new Window(raw.getT(0).t);
    }
    throw new RuntimeException("Unknown production: "+raw.name);
  }

  /////////////////////////////////////////////////////////////////////////

  /** The subclass which represents a Sprite and has 'type==TT.SPRITE'. */
  public static class Sprite extends Obj {
    /** Constructs a Sprite. */
    public Sprite(Token keyword, Expression expression, ExpressionPair size) {
      super(TT.SPRITE, keyword);
      this.expression = expression;
      this.size = size;
    }

    /* New API. */

    /** The Expression that initialises the picture of this sprite. */
    public final Expression expression;

    /** The opitional initial size of this sprite, or 'null' if omitted. */
    public final ExpressionPair size;

    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword);
      this.expression.toSourceCode(prefix, sb);
      if (this.size!=null) {
        sb.append(" SIZE ");
        this.size.toSourceCode(prefix, sb);
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////

  /** The subclass which represents a Text and has 'type==TT.TEXT'. */
  public static class Text extends Obj {
    /** Constructs a Text. */
    public Text(Token keyword, Expression expression) {
      super(TT.TEXT, keyword);
      this.expression = expression;
    }

    /* New API. */

    /** The Expression inside the round brackets. */
    public final Expression expression;

    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword);
      sb.append("(");
      this.expression.toSourceCode(prefix+"  ", sb);
      sb.append(")");
    }
  }

  /////////////////////////////////////////////////////////////////////////

  /** The subclass which represents a Window and has 'type==TT.WINDOW'. */
  public static class Window extends Obj {
    /** Constructs a Window. */
    public Window(Token keyword) {
      super(TT.WINDOW, keyword);
    }

    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword);
    }
  }
}
