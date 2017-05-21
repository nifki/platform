package org.sc3d.apt.crazon.compiler.ast;

import org.sc3d.apt.sss.v3.*;

/** Represents what the grammar calls a 'postfix'. */
public abstract class Postfix extends ASTNode {
  /** Constructor is private: use one of the inner classes. */
  private Postfix(TT type) {
    this.type = type;
  }

  /* New API. */

  public static enum TT {SUBSCRIPT, CALL};

  /** One of the values SUBSCRIPT or CALL. This is a value which identifies
   * to which subclass this Postfix belongs. */
  public final TT type;

  public static Postfix fromTree(Tree.Production raw) {
    if ("Subscript9".equals(raw.name)) {
      final org.sc3d.apt.crazon.compiler.ast.Subscript subscript =
        org.sc3d.apt.crazon.compiler.ast.Subscript.fromTree(raw.getP(0));
      if (subscript==null) return null;
      return new Subscript(subscript);
    }
    if ("Call9".equals(raw.name)) {
      final Tree.NonTerminal contents = (Tree.NonTerminal)raw.getT(0).parse();
      if (contents==null) return null;
      final ASTList<Field> args = Field.fromTrees(contents, "()");
      if (args==null) return null;
      return new Call(raw.getT(0).t, args);
    }
    throw new RuntimeException("Unknown production: " + raw.name);
  }

  public static ASTList<Postfix> fromTrees(Tree.NonTerminal raw) {
    final ASTList<Postfix> ans =
      new ASTList<Postfix>(raw.length, null, "");
    for (int i=0; i<raw.length; i++) {
      ans.add(Postfix.fromTree(raw.get(i)));
    }
    return ans.fix();
  }
  
  /////////////////////////////////////////////////////////////////////////

  /** Represents what the grammar calls a 'Subscript9'.
   * This is a thin wrapper around Subscript. */
  public static class Subscript extends Postfix {
    /** Constructs a Subscript. */
    public Subscript(org.sc3d.apt.crazon.compiler.ast.Subscript subscript) {
      super(TT.SUBSCRIPT);
      this.subscript = subscript;
    }

    /* New API. */

    /** What the grammar calls a 'subscript'. */
    public final org.sc3d.apt.crazon.compiler.ast.Subscript subscript;

    /** Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      this.subscript.toSourceCode(prefix, sb);
    }

    public String toString() { return this.subscript.toString(); }
  }

  /////////////////////////////////////////////////////////////////////////

  /** Represents what the grammar calls a 'Call9'. */
  public static class Call extends Postfix {
    /** Constructs a Call given values for its fields. */
    public Call(Token round, ASTList<Field> args) {
      super(TT.CALL);
      this.round = round;
      this.args = args;
    }

    /* New API. */
    
    /** The token representing the round brackets that contains the
     * function call arguments. This is provided so that you can call its
     * 'addError()' method. */
    public final Token round;
    
    /** The function arguments. */
    public final ASTList<Field> args;

    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      this.args.toSourceCode(prefix, sb);
    }
  }
}

