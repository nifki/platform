package org.sc3d.apt.crazon.compiler;

import org.sc3d.apt.crazon.compiler.ast.*;

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
 *   <li>extracting pages from the FileSystem;
 *   <li>invoking the parser in order to parse them according to the grammar and
 *       to build their abstract syntax trees;
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
   * side of making more things reachable.
   * @param fileSystem the mapping from page names to page text.
   * @param mainPage the name of the page to run as the program.
   * @throws IllegalArgumentException if 'mainPage' is not found.
   */
  public Program(FileSystem fileSystem, String mainPage) {
    this.fileSystem = fileSystem;
    this.mainPage = mainPage;
    this.globals = new HashSet<Global>();
    // Walk the statements in the main page.
    final Page page = this.getPage(mainPage);
    final Declaration<ASTList> ast = page.ast;
    for (int i=0; i<ast.length; i++) {
      final Declaration decl = ast.get(i);
      if (decl.type==Declaration.TT.STATEMENT) {
        this.walkStatement(((Declaration.Statement)decl).statement);
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
    if (!this.loadedPages.containsKey()) {
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
        final Page page = new Page(sentence);
        this.loadedPages.put(name, page);
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
        final int numErrors = p.countErrors();
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
  
  /** Adds a global variable to 'this.done'. If it was not already there, and it
   * is the name of a function, adds to 'this.done' all global variables
   * reachable from its body.
   * @param pageName the name of the page to which the global variable belongs.
   * @param variable the short name of the global variable.
   * @param errorToken a Token whose 'addError()' method to call if the page
   * does not exist.
   */
  private void walkGlobal(String pageName, String variable, Token errorToken) {
    final Global global = new Global(pageName, variable);
    if (!this.done.add(global)) return;
    final Page currentPage = this.getPage(pageName);
    if (currentPage==null) {
      errorToken.addError("There is no page named '"+pageName+"'.");
      return;
    }
    final Declaration.Define func = currentPage.getFunction(variable);
    if (func==null) return;
    this.walkStatements(func.body, currentPage);
  }
  
  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkStatements(ASTList<Statement> ast, Page currentPage) {
    if (ast==null) return;
    for (int i=0; i<ast.length; i++) {
      this.walkStatement(ast.get(i), currentPage);
    }
  }
  
  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkStatement(Statement ast, Page currentPage) {
    if (ast==null) return;
    switch (ast.type) {
    case ASSIGN: {
      final Statement.Assign astInner = (Statement.Assign)ast;
      this.walkLValue(astInner.lValue, currentPage);
      this.walkExpression(astInner.expression, currentPage);
      break;
    }
    case IF: {
      final Statement.If astInner = (Statement.If)ast;
      this.walkExpression(astInner.condition, currentPage);
      this.walkStatements(astInner.statements, currentPage);
      this.walkElse(astInner.els, currentPage);
      break;
    }
    case WHILE: {
      final Statement.While astInner = (Statement.While)ast;
      this.walkExpression(astInner.condition, currentPage);
      this.walkStatements(astInner.statements, currentPage);
      this.walkElse(astInner.els, currentPage);
      break;
    }
    case FOR: {
      final Statement.For astInner = (Statement.For)ast;
      this.walkExpression(astInner.table, currentPage);
      this.walkStatements(astInner.statements, currentPage);
      this.walkElse(astInner.els, currentPage);
      break;
    }
    case BREAK:
      break;
    case ERROR:
      this.walkExpression(astInner.message, currentPage);
      break;
    case RETURN:
      this.walkExpression(astInner.expression, currentPage);
      break;
    case CALL:
      this.walkExpression(astInner.function, currentPage);
      this.walkFields(astInner.args, currentPage);
      break;
    case ACTION:
      this.walkAction(astInner.action, currentPage);
      break;
    case WAIT:
      break;
    case DUMP:
      this.walkExpression(astInner.expression, currentPage);
      break;
    default:
      throw new RuntimeException("Unknown Statement type");
    }
  }

  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkLValue(LValue ast, Page currentPage) {
    if (ast==null) return;
    switch (ast.type) {
    case LOCAL:
      // Do nothing.
      break;
    case GLOBAL:
      this.walkGlobal(currentPage.name, ast.identifier.toString());
      break;
    case LONG:
      this.walkGlobal(ast.page.toString(), ast.identifier.toString());
      break;
    default:
      throw new RuntimeException("Unknown LValue type");
    }
    this.walkSubscripts(ast.subscripts, currentPage);
  }
  
  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkSubscripts(ASTList<Subscript> ast, Page currentPage) {
    if (ast==null) return;
    for (int i=0; i<ast.length; i++) {
      this.walkSubscript(ast.get(i), currentPage);
    }
  }
  
  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkSubscript(Subscript ast, Page currentPage) {
    if (ast==null) return;
    if (ast.type==Subscript.TT.SQUARE) {
      this.walkExpression(((Square)ast).expression, currentPage);
    }
  }
  
  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkExpression(Expression ast, Page currentPage) {
    if (ast==null) return;
    this.walkHead(ast.head, currentPage);
    this.walkTails(ast.tails, currentPage);
  }
  
  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkTails(ASTList<Tail> ast, Page currentPage) {
    if (ast==null) return;
    for (int i=0; i<ast.length; i++) {
      this.walkTail(ast.get(i), currentPage);
    }
  }

  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkTail(Tail ast, Page currentPage) {
    if (ast==null) return;
    this.walkHead(ast.head, currentPage);
  }

  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkHead(Head ast, Page currentPage) {
    if (ast==null) return;
    this.walkAtom(ast.atom, currentPage);
    this.walkPostfixes(ast.postfixes, currentPage);
  }

  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkAtom(Tail ast, Page currentPage) {
    if (ast==null) return;
    switch (ast.type) {
    case NUMBER: break;
    case STRING: break;
    case BOOLEAN: break;
    case TABLE: {
      final Atom.Table astInner = (Atom.Table)ast;
      this.walkFields(astInner.fields);
      break;
    }
    case SPRITE: {
      final Atom.Sprite astInner = (Atom.Sprite)ast;
      this.walkSprite(astInner.sprite);
      break;
    }
    case INPUT: break;
    case BRACKET: {
      final Atom.Bracket astInner = (Atom.Bracket)ast;
      this.walkExpression(astInner.expression);
      break;
    }
    case SHORT: {
      final Atom.Short astInner = (Atom.Short)ast;
      final String identifier = astInner.identifier.toString();
      if (!currentPage.isLocal(identifier)) {
        this.walkGlobal(currentPage.name, identifier);
      }
      break;
    }
    case LONG: {
      final Atom.Long astInner = (Atom.Long)ast;
      this.walkGlobal(astInner.page.toString(), astInner.identifier.toString());
      break;
    }
    default:
      throw new RuntimeException("Unknown Atom type");
    }
  }

  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkFields(ASTList<Field> ast, Page currentPage) {
    if (ast==null) return;
    for (int i=0; i<ast.length; i++) {
      this.walkField(ast.get(i), currentPage);
    }
  }
  
  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkField(Field ast, Page currentPage) {
    if (ast==null) return;
    this.walkExpression(ast.expression);
  }

  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkSprite(Sprite ast, Page currentPage) {
    if (ast==null) return;
    switch (ast.type) {
    case IMAGE: this.walkExpression(((Image)ast).expression); break;
    case TEXT: this.walkExpression(((Text)ast).expression); break;
    case WINDOW: break;
    default:
      throw new RuntimeException("Unknown Sprite type");
    }
  }
  
  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkPostfixes(ASTList<Postfix> ast, Page currentPage) {
    if (ast==null) return;
    for (int i=0; i<ast.length; i++) {
      this.walkPostfix(ast.get(i), currentPage);
    }
  }
  
  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkPostfix(Postfix ast, Page currentPage) {
    if (ast==null) return;
    switch (ast.type) {
    case SUBSCRIPT:
      this.walkSubscript(((Subscript)ast).subscript, currentPage);
      break;
    case CALL:
      this.walkArgs(((Call)ast).call, currentPage);
      break;
    default:
      throw new RuntimeException("Unknown Postfix type");
    }
  }
  
  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkArgs(ASTList<Arg> ast, Page currentPage) {
    if (ast==null) return;
    for (int i=0; i<ast.length; i++) {
      this.walkArg(ast.get(i), currentPage);
    }
  }

  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkArg(Arg ast, Page currentPage) {
    if (ast==null) return;
    this.walkExpression(ast.expression, currentPage);
  }
  
  /** Adds to 'this.done' all global variables reachable from 'ast'. */
  private void walkElse(Els ast, Page currentPage) {
    if (ast==null) return;
    this.walkStatements(ast.statements, currentPage);
  }

  /** Adds to 'this.done' all global variables reachable from 'ast'. */
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
}
