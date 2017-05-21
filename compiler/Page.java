package org.sc3d.apt.crazon.compiler;

import org.sc3d.apt.crazon.compiler.ast.*;

import java.util.*;
import org.sc3d.apt.sss.v3.*;

/** Represents a parsed source file including an index of its functions and the
 * result of an analysis that classifies short variable names as locals or
 * globals. */
public class Page {
  /** Constructs a Page from an abstract syntax tree. This method checks the
   * following errors:<ul>
   * <li> Short variable name used both as a local and as a global.
   * <li> Function declared more than once.
   * </ul>
   * @param name a non-null page name.
   * @param sentence a non-null Sentence object.
   * @return a non-null Page object.
   */
  public Page(String name, Sentence sentence) {
    this.name = name;
    this.sentence = sentence;
    this.ast = (ASTList<Declaration>)Parser.PARSER.parse(sentence);
    this.locals = ast==null ? null : classifyVariables(ast);
    this.index = ast==null ? null : calculateIndex(ast);
  }
  
  /* New API. */
  
  /** The name of this Page. Always non-null. */
  public final String name;
  
  /** The Sentence representing this Page. All error messages associated with
   * this Page end up here. Always non-null. */
  public final Sentence sentence;
  
  /** The abstract syntax tree of this Page, or 'null' if the sentence doesn't
   * parse correctly. */
  public final ASTList<Declaration> ast;
  
  /** Returns 'true' if the short name 'identifier' used in this Page should be
   * interpreted as a local variable, or 'false' otherwise. Must not be called
   * if 'ast' is null. */
  public boolean isLocal(String identifier) {
    if (this.ast==null) throw new RuntimeException();
    return this.locals.contains(identifier);
  }
  
  /** Returns the declaration of a function in this Page with the specified
   * name, or 'null' if there is none. Must not be called if 'ast' is null. */
  public Declaration.Define getFunction(String name) {
    if (this.ast==null) throw new RuntimeException();
    return this.index.get(name);
  }
  
  /* Private. */
  
  /** The set of all identifiers for which 'isLocal()' returns 'true'. This
   * field is 'null' if and only if 'ast' is null. */
  private final HashSet<String> locals;
  
  /** The index which maps function names to function declarations. This field
   * is 'null' if and only if 'ast' is null. */
  private final HashMap<String, Declaration.Define> index;
  
  /** Used by the constructor to calculate 'index'. */
  private static HashMap<String, Declaration.Define> calculateIndex(
    ASTList<Declaration> ast
  ) {
    final HashMap<String, Declaration.Define> index =
      new HashMap<String, Declaration.Define>();
    for (int i=0; i<ast.length; i++) {
      final Declaration d = ast.get(i);
      if (d.type==Declaration.TT.DEFINE) {
        final Declaration.Define dInner = (Declaration.Define)d;
        final Declaration.Define old =
          index.put(dInner.identifier.toString(), dInner);
        if (old!=null) {
          old.identifier.addError(
            "This function is declared again later."
          );
          dInner.identifier.addError(
            "This function has already been declared."
          );
        }
      }
    }
    return index;
  }
  
  /** Used by the constructor to calculate 'locals'. */
  private static HashSet<String> classifyVariables(
    ASTList<Declaration> ast
  ) {
    final HashMap<String, Token> localMap = new HashMap<String, Token>();
    final HashMap<String, Token> globalMap = new HashMap<String, Token>();
    ast.findAssignments(localMap, globalMap);
    final HashSet<String> locals = new HashSet<String>(localMap.keySet());
    for (String identifier: locals) {
      if (globalMap.containsKey(identifier)) {
        localMap.get(identifier).addError(
          "This short variable name is used as a local variable here, but it "+
          "is also used elsewhere on the same page as a global variable. You "+
          "need to rename one of them."
        );
        globalMap.get(identifier).addError(
          "This short variable name is used as a global variable here, but it "+
          "is also used elsewhere on the same page as a local variable. You "+
          "need to rename one of them."
        );
      }
    }
    return locals;
  }
}
