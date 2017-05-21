package org.sc3d.apt.crazon.compiler.ast;

import java.util.*;
import org.sc3d.apt.sss.v3.*;

/** Represents a node of the abstract syntax tree (AST).
 *<p>
 * Subclasses usually implement a static 'fromTree()' method which turns an SSS
 * Tree.Production into an instance of that subclass. If the subclass represents
 * a grammatical unit that can be repeated then it usually implements a static
 * 'fromTrees()' method which turns an SSS Tree.NonTerminal into an ASTList
 * of instances of that subclass.
 *<p>
 * The contract of the 'fromTree()' and 'fromTrees()' methods in the case of an
 * error is that they annotate the SSS tree with error messages and return
 * 'null'. If an ASTNode contains multiple constituent parts, it should attempt
 * to parse them all prior to returning, even in the case of errors.
 */
public abstract class ASTNode {

  /* New API. */

  /** Constructs valid Crazon source code for this ASTNode, and appends it
   * to a StringBuffer. The string 'prefix' is used to start new lines. New
   * lines are never put at the beginning or end of the output, except that
   * comments always end with a newline so that they lex correctly.
   * @param prefix a String consisting of a newline character followed by zero
   * or more spaces.
   * @param sb  the StringBuffer to which to append.
   */
  public abstract void toSourceCode(String prefix, StringBuffer sb);
  
  /** Part of the algorithm for classifying short variable names as locals or
  * globals. This method must be implemented by every ASTNode that represents
  * code that assigns to short variable names. Assignments to long variable
  * names and reads of any variable are ignored. It is possible to classify
  * every assignment as local or global. Every assignment involves a Token that
  * represents the variable name. This method adds to one of 'locals' and
  * 'globals' a mapping from the short variable name to the Token that
  * represents it. If there are multiple occurrences of the same variable name
  * within this ASTNode, the earliest is used. If the map already contains a
  * mapping for the variable name, this method replaces it.
  * <p>
  * The default implementation does nothing, which is appropriate for any
  * ASTNode that does not contain assignments to variables.
  * @param locals a map each of whose elements represents an assignment to a
  * short variable name used as a local variable. The keys are variable names.
  * The value corresponding to a key is a Token standing for an occurrence of
  * that variable name in the assignment.
  * @param globals analogous to 'locals', but for assignments to short variable
  * names used as global variables.
  */
  public void findAssignments(
    HashMap<String, Token> locals,
    HashMap<String, Token> globals
  ) {}
  
  /** Turns the parse-tree of a 'listXXX?' or an 'xxxs?' into an array of
   * parse-trees of 'xxx's.
   * where 'listXXX' is defined in terms of 'xxx' by the following SSS grammar
   * fragment:<pre>
   * c ::= {Comment {COMMENT}}
   * xxxs ::= {Some {ROUND(listXXX?)}}
   * xxx ::= { ... }
   * commaXXX ::= {Comma {"," c* xxx}}
   * listXXX ::= {List {c* xxx commaXXX*}}
   * </pre>
   * @param raw the raw parse-tree to process.
   * @return an array of Tree.Productions or 'null'.
   */
  public static Tree.Production[] removeCommas(Tree.NonTerminal raw) {
    Tree.Production prod = raw.get();
    if (prod!=null && prod.numParts==1) {
      // It is an 'xxxs'.
      raw = (Tree.NonTerminal)prod.getT(0).parse();
      if (raw==null) return null; // SSS will already have generated errors.
      prod = raw.get();
    }
    if (prod==null) return new Tree.Production[0]; // Optional and absent.
    // Now it is a 'listXXX'.
    final Tree.NonTerminal commaXXXs = prod.getNT(2);
    final int length = 1 + commaXXXs.length;
    final Tree.Production[] ans = new Tree.Production[length];
    ans[0] = prod.getP(1);
    for (int i=1; i<length; i++) ans[i] = commaXXXs.get(i-1).getP(2);
    return ans;
  }

  /* Override things in Object. */

  /** Returns a sensibly brief description of this ASTNode. The default
   * implementation is implemented in terms of 'toSourceCode()'. */
  public String toString() {
    final StringBuffer ans = new StringBuffer();
    this.toSourceCode("\n", ans);
    return ans.toString();
  }
}
