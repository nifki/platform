package org.sc3d.apt.crazon.compiler.ast;

import java.util.*;
import org.sc3d.apt.sss.v3.*;

/** Represents what the grammar calls a 'field'. */
public class Field extends ASTNode {
  /** Constructs a Field. */
  public Field(Token identifier, Expression expression) {
    this.identifier = identifier;
    this.expression = expression;
  }

  /* New API. */

  /** The 'id' part of this Field, or 'null' if this is a 'Numbered' field. */
  public final Token identifier;

  /** The 'expression' part of this Field. */
  public final Expression expression;

  public boolean isNamed() { return (this.identifier != null); }
  
  /** Constructs a Field from 'raw' if possible. Otherwise annotates 'raw'
   * with appropriate error messages and returns 'null' .*/
  public static Field fromTree(Tree.Production raw) {
    if ("Named".equals(raw.name)) {
      final Expression expr = Expression.fromTree(raw.getP(2));
      if (expr==null) return null;
      return new Field(raw.getP(0).getT(0).t, expr);
    }
    if ("Numbered".equals(raw.name)) {
      final Expression expr = Expression.fromTree(raw.getP(0));
      if (expr==null) return null;
      return new Field(null, expr);
    }
    throw new RuntimeException("Unknown production: "+raw.name);
  }
  
  /** Constructs an ASTList<Field> from a Tree.NonTerminal representing what the
   * grammar calls a 'listField?', if possible. Otherwise, annotates the Tree
   * with appropriate error messages and returns 'null'.
   * @param brackets a two character string representing in what kind of bracket
   * this listField is contained. This string is passed to the constructor of
   * ASTList.
   */
  public static ASTList<Field> fromTrees(
    Tree.NonTerminal raw,
    String brackets
  ) {
    final Tree.Production[] prods = removeCommas(raw);
    final ASTList<Field> ans = new ASTList<Field>(prods.length, brackets, ", ");
    for (int i=0; i<prods.length; i++) ans.add(fromTree(prods[i]));
    if (ans.fix()==null) return null;
    // Check that numbered fields precede named fields.
    for (int i = 1; i<ans.length; i++) {
      if (ans.get(i-1).isNamed() && !ans.get(i).isNamed()) {
        ans.get(i-1).identifier.addError(
          "Numbered fields must precede named fields"
        );
        return null;
      }
    }
    // Check that named fields have distinct names.
    final HashSet<String> namesUsed = new HashSet<String>();
    for (int i=0; i<ans.length; i++) {
      if (ans.get(i).isNamed()) {
        if (!namesUsed.add(ans.get(i).identifier.toString())) {
          ans.get(i).identifier.addError(
            "Table already has a key named '"+ans.get(i).identifier+"'"
          );
        }
      }
    }
    return ans;
  }

  /** Override things in ASTNode. */

  public void toSourceCode(String prefix, StringBuffer sb) {
    if (this.isNamed()) {
      sb.append(this.identifier);
      sb.append("=");
    }
    this.expression.toSourceCode(prefix, sb);
  }
}

