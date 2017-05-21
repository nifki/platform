package org.sc3d.apt.crazon.compiler.ast;

import org.sc3d.apt.sss.v3.*;

/** Represents what the grammar calls an 'atom'. */
public abstract class Atom extends ASTNode {
  /** Constructs an Atom. */
  private Atom(TT type) {
    this.type = type;
  }
  
  /* New API. */
  
  /** The type of the 'type' field. The "ARGS" atom is represented as a SHORT.
   */
  public static enum TT {
    NUMBER, STRING, BOOLEAN, TABLE, OBJECT, INPUT, BRACKET, SHORT, LONG
  };
  
  /** A value which indicates which inner subclass this Atom is an instance of.
   * */
  public final TT type;
  
  /** Constructs an Atom from 'raw' if possible. Otherwise, annotates 'raw' with
   * appropriate error messages and returns 'null'. */
  public static Atom fromTree(Tree.Production raw) {
    if ("Boolean".equals(raw.name)) {
      return new Literal(raw.getP(0).getT(0).t);
    }
    if ("Number".equals(raw.name) || "String".equals(raw.name)) {
      return new Literal(raw.getT(0).t);
    }
    if ("Table".equals(raw.name)) {
      final Tree.NonTerminal contents = (Tree.NonTerminal)raw.getT(0).parse();
      if (contents==null) return null;
      final ASTList<Field> fields = Field.fromTrees(contents, "[]");
      if (fields==null) return null;
      return new Table(raw.getT(0).t, fields);
    }
    if ("Object".equals(raw.name)) {
      return new Obj(
        org.sc3d.apt.crazon.compiler.ast.Obj.fromTree(raw.getP(0))
      );
    }
    if ("Input".equals(raw.name)) {
      return new Input(raw.getP(0).getT(0).t);
    }
    if ("Bracket".equals(raw.name)) {
      final Tree.NonTerminal contents = (Tree.NonTerminal)raw.getT(0).parse();
      if (contents==null) return null;
      final Expression expression = Expression.fromTree(contents.get());
      if (expression==null) return null;
      return new Bracket(expression);
    }
    if ("Short".equals(raw.name)) {
      return new Short(raw.getP(0).getT(0).t);
    }
    if ("Long".equals(raw.name)) {
      return new Long(raw.getP(0).getT(0).t, raw.getP(2).getT(0).t);
    }
    if ("Args".equals(raw.name)) {
      return new Short(raw.getT(0).t);
    }
    throw new RuntimeException("Unknown production: "+raw.name);
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Atom whose 'type' field is 'NUMBER', 'STRING' or 'BOOLEAN'
   * which represents what the grammar refers to as a 'Number', 'String'
   * or 'Boolean'.
   */
  public static class Literal extends Atom {
    public Literal(Token token) {
      super(deriveType(token));
      this.token = token;
    }
    
    /* New API. */

    /** The Token that represents this literal in the source code. */
    public final Token token;

    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.token);
    }

  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Atom whose 'type' field is 'TABLE',
   * which represents what the grammar calls a Table. */
  public static class Table extends Atom {
    public Table(Token bracket, ASTList<Field> fields) {
      super(TT.TABLE);
      this.bracket = bracket;
      this.fields = fields;
    }
    
    /* New API. */

    /** The Token that represents this table in the source code. */
    public final Token bracket;
    
    /** The fields of this table expression. */
    public final ASTList<Field> fields;

    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      this.fields.toSourceCode(prefix, sb);
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Atom whose 'type' field is 'OBJECT',
   * which represents what the grammar calls an Object. */
  public static class Obj extends Atom {
    public Obj(org.sc3d.apt.crazon.compiler.ast.Obj object) {
      super(TT.OBJECT);
      this.object = object;
    }
    
    /* New API. */

    /** The Obj. */
    public final org.sc3d.apt.crazon.compiler.ast.Obj object;

    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      this.object.toSourceCode(prefix, sb);
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Atom whose 'type' field is 'INPUT'
   * which represents what the grammar refers to as an 'Input'.
   */
  public static class Input extends Atom {
    public Input(Token token) {
      super(TT.INPUT);
      this.token = token;
    }
    
    /* New API. */

    /** The Token that represents this input in the source code. */
    public final Token token;

    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.token);
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Atom whose 'type' field is 'BRACKET',
   * which represents what the grammar calls a Bracket. */
  public static class Bracket extends Atom {
    public Bracket(Expression expression) {
      super(TT.BRACKET);
      this.expression = expression;
    }
    
    /* New API. */

    /** The Expression. */
    public final Expression expression;

    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      final String newPrefix = prefix + "  ";
      sb.append("(").append(newPrefix);
      this.expression.toSourceCode(newPrefix, sb);
      sb.append(prefix).append(")"); // Outdent.
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Atom whose 'type' field is 'SHORT',
   * which represents what the grammar calls a Short or an Args. */
  public static class Short extends Atom {
    public Short(Token identifier) {
      super(TT.SHORT);
      this.identifier = identifier;
    }
    
    /* New API. */

    /** The identifier. */
    public final Token identifier;

    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.identifier);
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Atom whose 'type' field is 'LONG',
   * which represents what the grammar calls a Long. */
  public static class Long extends Atom {
    public Long(Token page, Token identifier) {
      super(TT.LONG);
      this.page = page;
      this.identifier = identifier;
    }
    
    /* New API. */

    /** The page name. */
    public final Token page;

    /** The identifier. */
    public final Token identifier;

    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.page).append('_').append(this.identifier);
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /* Private. */

  private static TT deriveType(Token token) {
    if (token.type == Token.TYPE_NUMBER) {
      return TT.NUMBER;
    } else if (token.type == Token.TYPE_STRING) {
      return TT.STRING;
    } else if (token.type == Token.TYPE_WORD) {
      if (
        token.toString().equals("TRUE") ||
        token.toString().equals("FALSE")
      ) {
        return TT.BOOLEAN;
      } else {
        throw new IllegalArgumentException("Unknown boolean value");
      }
    } else {
      throw new IllegalArgumentException("Unknown literal type");
    }
  }
}
