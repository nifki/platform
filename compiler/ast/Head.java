package org.sc3d.apt.crazon.compiler.ast;
import org.sc3d.apt.sss.v3.*;

/** Represents what the grammar calls a 'head', an operand
 * of an 'expression', including its prefix and postfix operators. */
public class Head extends ASTNode {
  public Head(ASTList<Prefix> prefixes, Atom atom, ASTList<Postfix> postfixes) {
    this.prefixes = prefixes;
    this.atom = atom;
    this.postfixes = postfixes;
  }
  
  /* New API. */
  
  /** The prefixes of this Head. */
  public final ASTList<Prefix> prefixes;
  
  /** The atom part of this Head. */
  public final Atom atom;
  
  /** The postfixes of this Head. */
  public final ASTList<Postfix> postfixes;
  
  /** Returns 'prefixes' in an array. */
  public final Prefix[] getPrefixes() {
    final Prefix[] ans = new Prefix[this.prefixes.length];
    for (int i=0; i<ans.length; i++) ans[i] = this.prefixes.get(i);
    return ans;
  }
  
  /** Returns 'postfixes' in an array. */
  public final Postfix[] getPostfixes() {
    final Postfix[] ans = new Postfix[this.postfixes.length];
    for (int i=0; i<ans.length; i++) ans[i] = this.postfixes.get(i);
    return ans;
  }
  
  /** Constructs a Head from 'raw' if possible. Otherwise annotates 'raw'
   * with appropriate error messages and returns 'null' .*/
  public static Head fromTree(Tree.Production raw) {
    final ASTList<Prefix> prefixes = Prefix.fromTrees(raw.getNT(0));
    final Atom atom = Atom.fromTree(raw.getP(1));
    final ASTList<Postfix> postfixes = Postfix.fromTrees(raw.getNT(2));
    if (prefixes==null || atom==null || postfixes==null)
      return null; // Will already be error-annotated.
    return new Head(prefixes, atom, postfixes);
  }
  
  /* Override things in ASTNode. */
  
  public void toSourceCode(String prefix, StringBuffer sb) {
    this.prefixes.toSourceCode(prefix, sb);
    if (this.prefixes.length!=0) sb.append(" ");
    this.atom.toSourceCode(prefix, sb);
    this.postfixes.toSourceCode(prefix, sb);
  }
}
