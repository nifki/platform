package org.sc3d.apt.crazon.compiler;

import org.sc3d.apt.crazon.compiler.ast.*;

import java.util.*;
import java.io.*;
import org.sc3d.apt.sss.v3.*;

/** Represents the set of reachable global variables. During the analysis, Page
 * objects are constructed for all pages to which reachable global variables
 * belong. The 'getPage()' method can be used to recover these Page objects.
 * Annotations on the Page objects report any errors which occurred during
 * construction of this Program. More errors can optionally be added to the Page
 * objects by users of this class. Finally, this class can return a full error
 * report.
 * <p>
 * This class is responsible for:
 * <ul>
 *   <li>extracting each page from the FileSystem;
 *   <li>invoking the parser in order to parse it according to the grammar and
 *       to build its abstract syntax tree;
 *   <li>constructing a Page object from the AST in order to perform the
 *       variable name analysis and to compile an index of function
 *       declarations;
 *   <li>performing the global variable name analysis;
 *   <li>and representing the results.
 * </ul>
 */
public class Program {
  /** Tries to find all globals reachable from the specified page. It is
   * necessary to make approximations. These approximations always err on the
   * side of making more things reachable. We assume that if a global variable
   * name is mentioned and it is the name of a global function then it may
   * get called. We also assume that all statements that appear in a function
   * body can potentially be executed.
   * @param fileSystem the mapping from page names to page text.
   * @param mainPage the name of the page to run as the program.
   * @throws IllegalArgumentException if 'mainPage' is not found.
   */
  public Program(FileSystem fileSystem, String mainPage) {
    this.fileSystem = fileSystem;
    this.mainPage = mainPage;
    this.globals = new HashSet<Global>();
    this.loadedPages = new HashMap<String, Page>();
    // Walk the statements in the main page.
    final Page page = this.getPage(mainPage);
    if (page==null) throw new IllegalArgumentException(
      "There is no page named '"+mainPage+"'."
    );
    final ASTList<Declaration> ast = page.ast;
    if (ast==null) return;
    for (int i=0; i<ast.length; i++) {
      final Declaration decl = ast.get(i);
      if (decl.type==Declaration.TT.STATEMENT) {
        this.walkStatement(((Declaration.Statement)decl).statement, page);
      }
    }
  }
  
  /* New API. */

  /** The FileSystem used to find pages and resources. */
  public final FileSystem fileSystem;
  
  /** The name of the main page. */
  public final String mainPage;
  
  /** The set of all global variables reachable from the main program of the
   * page passed to the constructor. */
  public final HashSet<Global> globals;
  
  /** Returns the specified page. If the page has been requested before it
   * returns the same page object as before, otherwise it uses 'fileSystem' to
   * find the source code, creates an AST and builds and returns a Page object
   * from it.
   * @return a Page object, or 'null' if the page does not exist.
   */
  public Page getPage(String name) {
    if (!this.loadedPages.containsKey(name)) {
      try {
        final Reader reader = this.fileSystem.getSourceCode(name);
        final CharArrayWriter writer = new CharArrayWriter();
        final char buf[] = new char[1000];
        while(true) {
          int used = reader.read(buf);
          if (used < 0)
            break;
          writer.write(buf, 0, used);
        }
        reader.close();
        final Sentence sentence = new Sentence(writer.toCharArray());
        this.loadedPages.put(name, new Page(name, sentence));
      } catch (IOException e) {
        this.loadedPages.put(name, null);
      }
    }
    return this.loadedPages.get(name);
  }
  
  /** Returns the total number of errors on all loaded Pages. */
  public int countErrors() {
    int ans = 0;
    for (String name: this.loadedPages.keySet()) {
      final Page p = this.loadedPages.get(name);
      if (p!=null) {
        ans += p.sentence.countErrors();
      }
    }
    return ans;
  }
  
  /** Prints error messages on all loaded pages to 'out'.
   * @param maxErrorsPerPage the maximum number of errors to show for each page.
   */
  public void printErrorReport(PrintStream out, int maxErrorsPerPage) {
    for (String name: this.loadedPages.keySet()) {
      final Page p = this.loadedPages.get(name);
      if (p!=null) {
        final int numErrors = p.sentence.countErrors();
        if (numErrors>0) {
          out.println(numErrors+" errors found on page "+name+":");
          p.sentence.printErrorReport(out, maxErrorsPerPage);
          out.println();
          out.println();
        }
      }
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** Represents a fully-qualified global variable. */
  public static class Global {
    /** Initializes the members. */
    public Global(String page, String variable) {
      this.page = page;
      this.variable = variable;
    }
    
    /* New API. */
    
    /** The name of the page to which this Global belongs. */
    public final String page;
    
    /** The short name of this Global. */
    public final String variable;
    
    /* Override things in Object. */
    
    public int hashCode() {
      // '234874623' is just an arbitrary large odd number.
      return this.page.hashCode() + 234874623*this.variable.hashCode();
    }
    
    public boolean equals(Object that) {
      if (that==null) return false;
      try {
        final Global thatGlobal = (Global)that;
        return
          this.page.equals(thatGlobal.page) &&
          this.variable.equals(thatGlobal.variable);
      } catch (ClassCastException e) { return false; }
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** A map from page name to Page object. Pages are loaded and parsed and put
   * into this map the first time they are requested. */
  private final HashMap<String, Page> loadedPages;
  
  /** Adds a global variable to 'this.globals'. If it was not already there, and
   * it is the name of a function, adds to 'this.globals' all global variables
   * reachable from its body.
   * @param pageName the name of the page to which the global variable belongs.
   * @param variable the short name of the global variable.
   * @param errorToken a Token whose 'addError()' method to call if the page
   * does not exist.
   */
  private void walkGlobal(String pageName, String variable, Token errorToken) {
    final Global global = new Global(pageName, variable);
    if (!this.globals.add(global)) return;
    final Page currentPage = this.getPage(pageName);
    if (currentPage==null) {
      errorToken.addError("There is no page named '"+pageName+"'.");
      return;
    }
    this.walkDefine(currentPage.getFunction(variable), currentPage);
  }
  
  /** Adds to 'this.globals' all global variables reachable from 'ast'. */
  private void walkDefine(Declaration.Define ast, Page currentPage) {
    if (ast==null) return;
    for (int i=0; i<ast.args.length; i++) {
      this.walkArg(ast.args.get(i), currentPage);
    }
    for (int i=0; i<ast.body.length; i++) {
      this.walkStatement(ast.body.get(i), currentPage);
    }
  }

  /** Adds to 'this.globals' all global variables reachable from 'ast'. */
  private void walkArg(Arg ast, Page currentPage) {
    if (ast==null) return;
    this.walkExpression(ast.expression, currentPage);
  }
  
  /** Adds to 'this.globals' all global variables reachable from 'ast'. */
  private void walkStatement(Statement ast, Page currentPage) {
    if (ast==null) return;
    switch (ast.type) {
    case COMMENT: break;
    case NOOP: break;
    case ASSIGN: {
      final Statement.Assign astInner = (Statement.Assign)ast;
      this.walkLValue(astInner.lValue, currentPage);
      this.walkExpression(astInner.expression, currentPage);
      break;
    }
    case IF: {
      final Statement.If astInner = (Statement.If)ast;
      this.walkExpression(astInner.condition, currentPage);
      for (int i=0; i<astInner.statements.length; i++) {
        this.walkStatement(astInner.statements.get(i), currentPage);
      }
      for (int i=0; i<astInner.elifs.length; i++) {
        final Elif elif = astInner.elifs.get(i);
        for (int j=0; j<elif.statements.length; j++) {
          this.walkStatement(elif.statements.get(j), currentPage);
        }
      }
      this.walkElse(astInner.els, currentPage);
      break;
    }
    case WHILE: {
      final Statement.While astInner = (Statement.While)ast;
      this.walkExpression(astInner.condition, currentPage);
      for (int i=0; i<astInner.statements.length; i++) {
        this.walkStatement(astInner.statements.get(i), currentPage);
      }
      this.walkElse(astInner.els, currentPage);
      break;
    }
    case FOR: {
      final Statement.For astInner = (Statement.For)ast;
      this.walkExpression(astInner.table, currentPage);
      for (int i=0; i<astInner.statements.length; i++) {
        this.walkStatement(astInner.statements.get(i), currentPage);
      }
      this.walkElse(astInner.els, currentPage);
      break;
    }
    case BREAK:
      break;
    case ERROR:
      this.walkExpression(((Statement.Error)ast).message, currentPage);
      break;
    case RETURN:
      this.walkExpression(((Statement.Return)ast).expression, currentPage);
      break;
    case CALL: {
      final Statement.Call astInner = (Statement.Call)ast;
      this.walkExpression(astInner.function, currentPage);
      for (int i=0; i<astInner.args.length; i++) {
        this.walkField(astInner.args.get(i), currentPage);
      }
      break;
    }
    case ACTION:
      this.walkAction(((Statement.Action)ast).action, currentPage);
      break;
    case WAIT:
      break;
    case DUMP:
      this.walkExpression(((Statement.Dump)ast).expression, currentPage);
      break;
    default:
      throw new RuntimeException("Unknown Statement type");
    }
  }

  /** Adds to 'this.globals' all global variables reachable from 'ast'. */
  private void walkLValue(LValue ast, Page currentPage) {
    if (ast==null) return;
    switch (ast.type) {
    case LOCAL:
      // Do nothing.
      break;
    case GLOBAL: {
      final LValue.Global astInner = (LValue.Global)ast;
      this.walkGlobal(
        currentPage.name,
        astInner.identifier.toString(),
        astInner.identifier
      );
      break;
    }
    case LONG: {
      final LValue.Long astInner = (LValue.Long)ast;
      this.walkGlobal(
        astInner.page.toString(),
        astInner.identifier.toString(),
        astInner.page
      );
      break;
    }
    default:
      throw new RuntimeException("Unknown LValue type");
    }
    for (int i=0; i<ast.subscripts.length; i++) {
      this.walkSubscript(ast.subscripts.get(i), currentPage);
    }
  }
  
  /** Adds to 'this.globals' all global variables reachable from 'ast'. */
  private void walkSubscript(Subscript ast, Page currentPage) {
    if (ast==null) return;
    if (ast.type==Subscript.TT.SQUARE) {
      this.walkExpression(((Subscript.Square)ast).expression, currentPage);
    }
  }
  
  /** Adds to 'this.globals' all global variables reachable from 'ast'. */
  private void walkExpression(Expression ast, Page currentPage) {
    if (ast==null) return;
    this.walkHead(ast.head, currentPage);
    for (int i=0; i<ast.tails.length; i++) {
      this.walkHead(ast.tails.get(i).head, currentPage);
    }
  }

  /** Adds to 'this.globals' all global variables reachable from 'ast'. */
  private void walkHead(Head ast, Page currentPage) {
    if (ast==null) return;
    this.walkAtom(ast.atom, currentPage);
    for (int i=0; i<ast.postfixes.length; i++) {
      this.walkPostfix(ast.postfixes.get(i), currentPage);
    }
  }

  /** Adds to 'this.globals' all global variables reachable from 'ast'. */
  private void walkAtom(Atom ast, Page currentPage) {
    if (ast==null) return;
    switch (ast.type) {
    case NUMBER: break;
    case STRING: break;
    case BOOLEAN: break;
    case TABLE: {
      final Atom.Table astInner = (Atom.Table)ast;
      for (int i=0; i<astInner.fields.length; i++) {
        this.walkField(astInner.fields.get(i), currentPage);
      }
      break;
    }
    case OBJECT:
      this.walkObject(((Atom.Obj)ast).object, currentPage);
      break;
    case INPUT: break;
    case BRACKET:
      this.walkExpression(((Atom.Bracket)ast).expression, currentPage);
      break;
    case SHORT: {
      final Atom.Short astInner = (Atom.Short)ast;
      final String identifier = astInner.identifier.toString();
      if (!currentPage.isLocal(identifier)) {
        this.walkGlobal(currentPage.name, identifier, astInner.identifier);
      }
      break;
    }
    case LONG: {
      final Atom.Long astInner = (Atom.Long)ast;
      this.walkGlobal(
        astInner.page.toString(),
        astInner.identifier.toString(),
        astInner.page
      );
      break;
    }
    default:
      throw new RuntimeException("Unknown Atom type");
    }
  }

  /** Adds to 'this.globals' all global variables reachable from 'ast'. */
  private void walkField(Field ast, Page currentPage) {
    if (ast==null) return;
    this.walkExpression(ast.expression, currentPage);
  }

  /** Adds to 'this.globals' all global variables reachable from 'ast'. */
  private void walkObject(Obj ast, Page currentPage) {
    if (ast==null) return;
    switch (ast.type) {
    case SPRITE: {
      final Obj.Sprite astInner = (Obj.Sprite)ast;
      this.walkExpression(astInner.expression, currentPage);
      this.walkExpressionPair(astInner.size, currentPage);
      break;
    }
    case TEXT:
      this.walkExpression(((Obj.Text)ast).expression, currentPage);
      break;
    case WINDOW:
      break;
    default:
      throw new RuntimeException("Unknown Object type");
    }
  }
  
  /** Adds to 'this.globals' all global variables reachable from 'ast'. */
  private void walkPostfix(Postfix ast, Page currentPage) {
    if (ast==null) return;
    switch (ast.type) {
    case SUBSCRIPT:
      this.walkSubscript(((Postfix.Subscript)ast).subscript, currentPage);
      break;
    case CALL:
      final Postfix.Call astInner = (Postfix.Call)ast;
      for (int i=0; i<astInner.args.length; i++) {
        this.walkField(astInner.args.get(i), currentPage);
      }
      break;
    default:
      throw new RuntimeException("Unknown Postfix type");
    }
  }
  
  /** Adds to 'this.globals' all global variables reachable from 'ast'. */
  private void walkElse(Else ast, Page currentPage) {
    if (ast==null) return;
    for (int i=0; i<ast.statements.length; i++) {
      this.walkStatement(ast.statements.get(i), currentPage);
    }
  }

  /** Adds to 'this.globals' all global variables reachable from 'ast'. */
  private void walkAction(Action ast, Page currentPage) {
    if (ast==null) return;
    switch (ast.type) {
    case SET: {
      final Action.Set astInner = (Action.Set)ast;
      this.walkExpression(astInner.object, currentPage);
      this.walkExpression(astInner.value, currentPage);
      break;
    }
    case MOVE: {
      final Action.Move astInner = (Action.Move)ast;
      this.walkExpression(astInner.object, currentPage);
      this.walkExpressionPair(astInner.xy, currentPage);
      break;
    }
    case HIDE: {
      final Action.Hide astInner = (Action.Hide)ast;
      this.walkExpression(astInner.object, currentPage);
      break;
    }
    case RESIZE: {
      final Action.Resize astInner = (Action.Resize)ast;
      this.walkExpression(astInner.object, currentPage);
      this.walkExpressionPair(astInner.xy, currentPage);
      break;
    }
    case CLS: break;
    default:
      throw new RuntimeException("Unknown Atom type");
    }
  }
  
  /** Adds to 'this.globals' all global variables reachable from 'ast'. */
  private void walkExpressionPair(ExpressionPair ast, Page currentPage) {
    if (ast==null) return;
    this.walkExpression(ast.x, currentPage);
    this.walkExpression(ast.y, currentPage);
  }
}
