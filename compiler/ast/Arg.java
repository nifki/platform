package org.sc3d.apt.crazon.compiler.ast;

import java.util.*;
import org.sc3d.apt.sss.v3.*;

/** Represents what the grammar calls an 'arg'. */
public class Arg extends ASTNode {
  /** Constructs an Arg. */
  public Arg(Token identifier, Token eq, Expression expression) {
    this.identifier = identifier;
    this.eq = eq;
    this.expression = expression;
    if (this.expression!=null && this.eq==null) {
      throw new IllegalArgumentException("Default value for Numbered argument");
    }
  }

  /* New API. */

  /** The 'identifier' part of this Arg. */
  public final Token identifier;
  
  /** The "=" Token, or 'null' if this is a 'Numbered' argument. */
  public final Token eq;

  /** The optional 'expression' part of this Arg, or 'null' if it is omitted or
   * if this is a 'Numbered' argument. */
  public final Expression expression;
  
  /** Returns 'true' if this is a 'Named' argument or 'false' if it is
   * 'Numbered'. */
  public final boolean isNamed() { return this.eq!=null; }
  
  /** Returns 'true' if this is a 'Named' argument and its optional default
   * value expression is present, otherwise returns 'false'. */
  public final boolean hasDefault() { return this.expression!=null; }

  /** Constructs an Arg from 'raw' if possible. Otherwise annotates 'raw'
   * with appropriate error messages and returns 'null' .*/
  public static Arg fromTree(Tree.Production raw) {
    if ("Named".equals(raw.name)) {
      final boolean hasExpr = raw.getNT(2).length>0;
      final Expression expr = hasExpr ? Expression.fromTree(raw.getP(2)) : null;
      if (hasExpr && expr==null) return null;
      return new Arg(raw.getP(0).getT(0).t, raw.getT(1).t, expr);
    }
    if ("Numbered".equals(raw.name)) {
      return new Arg(raw.getP(0).getT(0).t, null, null);
    }
    throw new RuntimeException("Unknown production: "+raw.name);
  }
  
  /** Constructs an ASTList<Arg> from a Tree.NonTerminal representing what the
   * grammar calls a 'listArg?', if possible. Otherwise, annotates the Tree
   * with appropriate error messages and returns 'null'.
   */
  public static ASTList<Arg> fromTrees(Tree.NonTerminal raw) {
    final Tree.Production[] prods = removeCommas(raw);
    final ASTList<Arg> ans = new ASTList<Arg>(prods.length, "()", ", ");
    for (int i=0; i<prods.length; i++) ans.add(fromTree(prods[i]));
    if (ans.fix()==null) return null;
    for (int i=1; i<ans.length; ++i) {
      if (ans.get(i-1).isNamed() && !ans.get(i).isNamed()) {
        ans.get(i-1).eq.addError(
          "Numbered arguments must precede named arguments"
        );
        return null;
      }
    }
    return ans;
  }

  /** Override things in ASTNode. */

  public void toSourceCode(String prefix, StringBuffer sb) {
    sb.append(this.identifier);
    if (this.isNamed()) {
      sb.append(this.eq);
      if (this.hasDefault()) {
        this.expression.toSourceCode(prefix, sb);
      }
    }
  }
  
  public void findAssignments(
    HashMap<String, Token> locals,
    HashMap<String, Token> globals
  ) {
    locals.put(this.identifier.toString(), this.identifier);
  }
}

