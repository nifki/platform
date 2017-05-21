package org.sc3d.apt.crazon.compiler;

import org.sc3d.apt.crazon.compiler.ast.*;

import org.sc3d.apt.sss.v3.*;
import java.io.*;
import java.math.*;
import java.util.*;

/** The algorithm that walks the parse tree and emits Crazon assembler. */
public class Flattener {
  /** Constructs a Flattener for the specified Program. */
  public Flattener(Program program) {
    this.program = program;
    this.output = new ArrayList<String>();
    // Flatten the main program.
    final Page mainPage = program.getPage(program.mainPage);
    final ASTList<Declaration> decls = mainPage.ast;
    for (int i=0; i<decls.length; i++) {
      final Declaration decl = decls.get(i);
      if (decl.type==Declaration.TT.STATEMENT) {
        this.walkStatement(((Declaration.Statement)decl).statement, mainPage);
      }
    }
    // Flatten the subroutine definitions.
    for (Program.Global global: program.globals) {
      final Page page = program.getPage(global.page);
      final Declaration.Define def = page.getFunction(global.variable);
      if (def!=null) this.walkDefine(def, page);
    }
  }
  
  /* New API. */
  
  /** The assembler instructions. */
  public final List<String> output;
  
  /* Private. */
  
  /** Convenience method: adds an instruction to 'output'. */
  private void emit(String instruction) {
    this.output.add(instruction);
  }
  
  /** Emits assembler for 'ast'. */
  private void walkDefine(Declaration.Define ast, Page currentPage) {
    /*
    DEF(Foo_bar)
      LSTORE(ARGS) ;
      LLOAD(ARGS) 0 GET LSTORE(Numbered0) ;
      LLOAD(ARGS) 1 GET LSTORE(Numbered1) ;
      LLOAD(ARGS) "Named0" GET LSTORE(Named0) ;
      LLOAD(ARGS) "Named1" GET LSTORE(Named1) ;
      LLOAD(ARGS) "NamedDefault0" CONTAINS IF ;
        LLOAD(ARGS) "NamedDefault0" GET LSTORE(NamedDefault0) ;
      THEN ;
        <DefaultValue0> LSTORE(NamedDefault0) ;
      ELSE ;
      <body>
      TABLE RETURN ; // FIXME. 
    */
    this.emit("DEF("+currentPage.name+"_"+ast.identifier+")");
    this.emit("LSTORE(ARGS)");
    this.emit(";");
    for (int i=0; i<ast.args.length; i++) {
      this.walkArg(i, ast.args.get(i), currentPage);
    }
    final int numStatements = ast.body.length;
    for (int i=0; i<numStatements; i++) {
      this.walkStatement(ast.body.get(i), currentPage);
    }
    // Pacify the assembler's dead-code analysis.
    boolean fallsThrough = true;
    if (numStatements!=0) {
      switch (ast.body.get(numStatements-1).type) {
         case RETURN: case ERROR: fallsThrough = false;
      }
    }
    if (fallsThrough) {
      this.emit("TABLE");
      this.emit("RETURN");
    }
  }
  
  /** Emits assembler for 'ast'.
   * @param argNumber the position of the argument. This is needed for
   * 'Numbered' arguments.
   */
  private void walkArg(int argNumber, Arg ast, Page currentPage) {
    if (ast.eq==null) {
      // It's a 'Numbered' argument.
      this.emit("LLOAD(ARGS)");
      this.emit(""+argNumber);
      this.emit("GET");
      this.emit("LSTORE("+ast.identifier+")");
      this.emit(";");
    } else if (ast.expression==null) {
      // It's a 'Named' argument with no default value.
      this.emit("LLOAD(ARGS)");
      this.emit("\""+ast.identifier+"\"");
      this.emit("GET");
      this.emit("LSTORE("+ast.identifier+")");
      this.emit(";");
    } else {
      // It's a 'Named' argument with a default value.
      this.emit("LLOAD(ARGS)");
      this.emit("\""+ast.identifier+"\"");
      this.emit("CONTAINS");
      this.emit("IF");
      this.emit(";");
      {
        this.emit("LLOAD(ARGS)");
        this.emit("\""+ast.identifier+"\"");
        this.emit("GET");
        this.emit("LSTORE("+ast.identifier+")");
        this.emit(";");
      }
      this.emit("THEN");
      this.emit(";");
      {
        this.walkExpression(ast.expression, currentPage);
        this.emit("LSTORE("+ast.identifier+")");
        this.emit(";");
      }
      this.emit("ELSE");
      this.emit(";");
    }
  }
  
  /** Emits assembler for 'ast'. */
  private void walkStatement(Statement ast, Page currentPage) {
    switch(ast.type) {
      case COMMENT:
        this.emit(((Statement.Comment)ast).comment+"\n");
        break;
      case NOOP: break;
      case ASSIGN: {
        this.walkAssign((Statement.Assign)ast, currentPage);
        break;
      }
      case IF: {
        final Statement.If astInner = (Statement.If)ast;
        this.walkExpression(astInner.condition, currentPage);
        this.emit("IF");
        this.emit(";");
        for (int i=0; i<astInner.statements.length; i++) {
          this.walkStatement(astInner.statements.get(i), currentPage);
        }
        this.emit("THEN");
        this.emit(";");
        for (int i=0; i<astInner.elifs.length; i++) {
          final Elif elif = astInner.elifs.get(i);
          this.walkExpression(elif.condition, currentPage);
          this.emit("IF");
          this.emit(";");
          for (int j=0; j<elif.statements.length; j++) {
            this.walkStatement(elif.statements.get(j), currentPage);
          }
          this.emit("THEN");
          this.emit(";");
        }
        if (astInner.els!=null) {
          for (int i=0; i<astInner.els.statements.length; i++) {
            this.walkStatement(astInner.els.statements.get(i), currentPage);
          }
        }
        for (int i=0; i<astInner.elifs.length; i++) {
          this.emit("ELSE");
          this.emit(";");
        }
        this.emit("ELSE");
        this.emit(";");
        break;
      }
      case WHILE: {
        final Statement.While astInner = (Statement.While)ast;
        this.emit("LOOP");
        this.emit(";");
        this.walkExpression(astInner.condition, currentPage);
        this.emit("WHILE");
        this.emit(";");
        for (int i=0; i<astInner.statements.length; i++) {
          this.walkStatement(astInner.statements.get(i), currentPage);
        }
        this.emit("NEXT");
        this.emit(";");
        if (astInner.els!=null) {
          for (int i=0; i<astInner.els.statements.length; i++) {
            this.walkStatement(astInner.els.statements.get(i), currentPage);
          }
        }
        this.emit("ELSE");
        this.emit(";");
        break;
      }
      case FOR: {
        final Statement.For astInner = (Statement.For)ast;
        this.walkExpression(astInner.table, currentPage);
        this.emit("FOR");
        if (astInner.value==null) {
          this.emit("DROP");
        } else {
          this.emit("LSTORE("+astInner.value+")");
        }
        if (astInner.key==null) {
          this.emit("DROP");
        } else {
          this.emit("LSTORE("+astInner.key+")");
        }
        this.emit(";");
        for (int i=0; i<astInner.statements.length; i++) {
          this.walkStatement(astInner.statements.get(i), currentPage);
        }
        this.emit("NEXT");
        this.emit(";");
        if (astInner.els!=null) {
          for (int i=0; i<astInner.els.statements.length; i++) {
            this.walkStatement(astInner.els.statements.get(i), currentPage);
          }
        }
        this.emit("ELSE");
        this.emit(";");
        break;
      }
      case BREAK: {
        final Statement.Break astInner = (Statement.Break)ast;
        for (int i=0; i<astInner.depth; i++) {
          this.emit("BREAK");
        }
        break;
      }
      case ERROR: {
        final Statement.Error astInner = (Statement.Error)ast;
        this.walkExpression(astInner.message, currentPage);
        this.emit("ERROR");
        break;
      }
      case RETURN: {
        final Statement.Return astInner = (Statement.Return)ast;
        if (astInner.expression==null) {
          this.emit("TABLE");
        } else {
          this.walkExpression(astInner.expression, currentPage);
        }
        this.emit("RETURN");
        break;
      }
      case CALL: {
        final Statement.Call astInner = (Statement.Call)ast;
        this.walkExpression(astInner.function, currentPage);
        this.emit("TABLE");
        for (int i=0; i<astInner.args.length; i++) {
          this.walkField(i, astInner.args.get(i), currentPage);
        }
        this.emit("CALL");
        this.emit("DROPTABLE");
        this.emit(";");
        break;
      }
      case ACTION: {
        this.walkAction(((Statement.Action)ast).action, currentPage);
        break;
      }
      case WAIT: {
        this.emit("WAIT");
        this.emit(";");
        break;
      }
      case DUMP: {
        final Statement.Dump astInner = (Statement.Dump)ast;
        this.walkExpression(astInner.expression, currentPage);
        this.emit("DUMP");
        this.emit(";");
        break;
      }
      default: throw new RuntimeException("Unknown type");
    }
  }

  /** Emits assembler for 'ast'. This breaks the general pattern a bit but
   * assignments are a bit horrible so we hide all the horror in here. The
   * horror is that you have to walk the LValue twice if there are subscripts.
   */
  private void walkAssign(Statement.Assign ast, Page currentPage) {
    final LValue lValue = ast.lValue;
    String instruction;
    // Walk the LValue the first time and emit the "LOAD" part.
    switch (lValue.type) {
      case LOCAL: {
        instruction = "LLOAD("+lValue.identifier+")";
        break;
      }
      case GLOBAL: {
        instruction = "LOAD("+currentPage.name+"_"+lValue.identifier+")";
        break;
      }
      case LONG: {
        final LValue.Long lValueInner = (LValue.Long)lValue;
        instruction = "LOAD("+lValueInner.page+"_"+lValueInner.identifier+")";
        break;
      }
      default: throw new RuntimeException("Unknown type");
      }
      for (int i=0; i<lValue.subscripts.length; i++) {
        this.emit(instruction);
        this.walkSubscript(lValue.subscripts.get(i), currentPage);
        instruction = "DGET";
      }
      // Emit the code to calculate the expression.
      this.walkExpression(ast.expression, currentPage);
      // Walk the LValue a second time and emit the "STORE" part.
      for (int i=0; i<lValue.subscripts.length; i++) {
        this.emit("PUT");
      }
      switch (lValue.type) {
      case LOCAL: {
        this.emit("LSTORE("+lValue.identifier+")");
        break;
      }
      case GLOBAL: {
        this.emit("STORE("+currentPage.name+"_"+lValue.identifier+")");
        break;
      }
      case LONG: {
        final LValue.Long lValueInner = (LValue.Long)lValue;
        this.emit("STORE("+lValueInner.page+"_"+lValueInner.identifier+")");
        break;
      }
      default: throw new RuntimeException("Unknown type");
    }
    this.emit(";");
  }
  
  /** Emits assembler for 'ast'. */
  private void walkAction(Action ast, Page currentPage) {
    switch(ast.type) {
      case SET: {
        final Action.Set astInner = (Action.Set)ast;
        this.walkExpression(astInner.object, currentPage);
        this.walkExpression(astInner.value, currentPage);
        this.emit("SET("+astInner.attribute+")");
        break;
      }
      case MOVE: {
        final Action.Move astInner = (Action.Move)ast;
        this.walkExpression(astInner.object, currentPage);
        this.emit("LSTORE(TEMP)");
        this.emit(";");
        if (astInner.xy.x!=null) {
          this.emit("LLOAD(TEMP)");
          this.walkExpression(astInner.xy.x, currentPage);
          if (!astInner.isAbsolute) {
            this.emit("LLOAD(TEMP)");
            this.emit("\"X\"");
            this.emit("GET");
            this.emit("+");
          }
          this.emit("SET(X)");
          this.emit(";");
        }
        if (astInner.xy.y!=null) {
          this.emit("LLOAD(TEMP)");
          this.walkExpression(astInner.xy.y, currentPage);
          if (!astInner.isAbsolute) {
            this.emit("LLOAD(TEMP)");
            this.emit("\"Y\"");
            this.emit("GET");
            this.emit("+");
          }
          this.emit("SET(Y)");
          this.emit(";");
        }
        //---
        this.emit("LLOAD(TEMP)");
        this.emit("TRUE");
        this.emit("SET(IsVisible)");
        this.emit(";");
        //---
        break;
      }
      case HIDE: {
        final Action.Hide astInner = (Action.Hide)ast;
        this.walkExpression(astInner.object, currentPage);
        this.emit("FALSE");
        this.emit("SET(IsVisible)");
        this.emit(";");
        break;
      }
      case RESIZE: {
        final Action.Resize astInner = (Action.Resize)ast;
        this.walkExpression(astInner.object, currentPage);
        this.emit("LSTORE(TEMP)");
        this.emit(";");
        this.emitResizeCode(astInner.isAbsolute, astInner.xy, currentPage);
        this.emit(";");
        break;
      }
      case CLS: {
        this.emit("CLS");
        this.emit(";");
        break;
      }
      default: throw new RuntimeException("Unknown kind");
    }
  }

  /** Defines the precedences of the infix operators. */
  private static final Map<String, Integer> INFIX_PRECEDENCES;
  static {
    INFIX_PRECEDENCES = new HashMap<String, Integer>();
    INFIX_PRECEDENCES.put("**", new Integer(8));
    INFIX_PRECEDENCES.put("*", new Integer(6));
    INFIX_PRECEDENCES.put("/", new Integer(6));
    INFIX_PRECEDENCES.put("%", new Integer(6));
    INFIX_PRECEDENCES.put("+", new Integer(5));
    INFIX_PRECEDENCES.put("-", new Integer(5));
    INFIX_PRECEDENCES.put("MIN", new Integer(4));
    INFIX_PRECEDENCES.put("MAX", new Integer(4));
    INFIX_PRECEDENCES.put("==", new Integer(3));
    INFIX_PRECEDENCES.put("<", new Integer(3));
    INFIX_PRECEDENCES.put(">", new Integer(3));
    INFIX_PRECEDENCES.put("<>", new Integer(3));
    INFIX_PRECEDENCES.put("!=", new Integer(3));
    INFIX_PRECEDENCES.put(">=", new Integer(3));
    INFIX_PRECEDENCES.put("<=", new Integer(3));
    INFIX_PRECEDENCES.put("XOR", new Integer(2));
    INFIX_PRECEDENCES.put("AND", new Integer(1));
    INFIX_PRECEDENCES.put("OR", new Integer(0));
  }
  
  /** Emits assembler for 'ast'. This method does the operator precedence
   * calculation. */
  private void walkExpression(Expression ast, final Page currentPage) {
    final Precedence<Atom, Prefix, Infix, Postfix, Void> precedence =
      new Precedence<Atom, Prefix, Infix, Postfix, Void> (
        ast.head.getPrefixes(),
        ast.head.atom,
        ast.head.getPostfixes()
      )
    {
      public Void makeAtom(Atom atom) {
        Flattener.this.walkAtom(atom, currentPage);
        return null;
      }
      public Void makePrefix(Prefix prefix, Void term) {
        // With one exception, assembler = source code.
        String ans = prefix.keyword.toString();
        if ("-".equals(ans)) ans = "NEG";
        Flattener.this.emit(ans);
        return null;
      }
      public Void makeInfix(Void term1, Infix infix, Void term2) {
        // Assembler = source code.
        Flattener.this.emit(infix.keyword.toString());
        return null;
      }
      public Void makePostfix(Void term, Postfix postfix) {
        Flattener.this.walkPostfix(postfix, currentPage);
        return null;
      }
      public int getPrefixPrecedence(Prefix op) {
        if ("NOT".equals(op.keyword.toString())) return 2;
        return 7;
      }
      public int getInfixPrecedence(Infix op) {
        return INFIX_PRECEDENCES.get(op.keyword.toString()).intValue();
      }
      public int getPostfixPrecedence(Postfix op) {
        return 9;
      }
      public boolean isLeftAssociative(int precedence) {
        return precedence != 8; // Only "**" is right-associative.
      }
    };
    for (int i=0; i<ast.tails.length; i++) {
      final Tail tail = ast.tails.get(i);
      precedence.add(
        tail.infix,
        tail.head.getPrefixes(),
        tail.head.atom,
        tail.head.getPostfixes()
      );
    }
    precedence.finish(); // Discard return value.
  }
    
  /** Emits assembler for 'ast'. */
  private void walkAtom(Atom ast, Page currentPage) {
    switch (ast.type) {
      case NUMBER: {
        final SSSNumber token = (SSSNumber)((Atom.Literal)ast).token;
        final BigInteger mantissa =
          new BigInteger(token.getMantissa(), token.getRadix());
        final double shift = Math.pow(token.getRadix(), token.getShift());
        final double value = mantissa.doubleValue() * shift;
        this.emit(""+value);
        break;
      }
      case STRING: {
        final SSSString token = (SSSString)((Atom.Literal)ast).token;
        this.emit(""+token); // Assembler = source code.
        break;
      }
      case BOOLEAN: {
        final Token token = ((Atom.Literal)ast).token;
        this.emit(""+token); // Assembler = source code.
        break;
      }
      case TABLE: {
        final Atom.Table astInner = (Atom.Table)ast;
        this.emit("TABLE");
        for (int i=0; i<astInner.fields.length; i++) {
          this.walkField(i, astInner.fields.get(i), currentPage);
        }
        break;
      }
      case OBJECT: {
        this.walkObject(((Atom.Obj)ast).object, currentPage);
        break;
      }
      case INPUT: {
        final Token token = ((Atom.Input)ast).token;
        this.emit(""+token); // Assembler = source code.
        break;
      }
      case BRACKET:
        this.walkExpression(((Atom.Bracket)ast).expression, currentPage);
        break;
      case SHORT: {
        final String identifier = ((Atom.Short)ast).identifier.toString();;
        if (currentPage.isLocal(identifier)) {
          this.emit("LLOAD("+identifier+")");
        } else {
          this.emit("LOAD("+currentPage.name+"_"+identifier+")");
        }
        break;
      }
      case LONG: {
        final Atom.Long astInner = (Atom.Long)ast;
        this.emit("LOAD("+astInner.page+"_"+astInner.identifier+")");
        break;
      }
      default: throw new RuntimeException("Unknown kind");
    }
  }
  
  /** Emits assembler for 'ast'. */
  private void walkField(int fieldNumber, Field ast, Page currentPage) {
    if (ast.isNamed()) {
      this.emit("\""+ast.identifier+"\"");
    } else {
      this.emit(""+fieldNumber);
    }
    this.walkExpression(ast.expression, currentPage);
    this.emit("PUT");
  }
  
  /** Emits assembler for 'ast'. */
  private void walkObject(Obj ast, Page currentPage) {
    switch (ast.type) {
      case SPRITE: {
        final Obj.Sprite astInner = (Obj.Sprite)ast;
        this.walkExpression(astInner.expression, currentPage);
        this.emit("SPRITE");
        if (astInner.size!=null) {
          this.emit("LSTORE(TEMP)");
          this.emitResizeCode(true, astInner.size, currentPage);
          this.emit("LLOAD(TEMP)");
        }
        break;
      }
      case TEXT:
        throw new RuntimeException("NYI: Object/Text");
      case WINDOW:
        this.emit("WINDOW");
        break;
      default: throw new RuntimeException("Unknown kind");
    }
  }
  
  /** Emits code to resize the sprite stored in "TEMP" to the size specified by
   * 'ast'. This code can be called in the middle of an expression, and so must
   * not emit ";" instructions. */
  private void emitResizeCode(
    boolean isAbsolute,
    ExpressionPair ast,
    Page currentPage
  ) {
    if (isAbsolute) {
      // Three cases.
      if (ast.x==null) {
        // 'y' must be non-null.
        // We need to choose the width to preserve the aspect ratio.
        // Here is the equivant code:
        //   ratio = TEMP.W / TEMP.H
        //   TEMP.H = <y>
        //   TEMP.W = ratio * TEMP.H
        // (a stack location is used for the variable 'ratio'.)
        this.emit("LLOAD(TEMP)");
        this.emit("LLOAD(TEMP)");
        this.emit("\"W\"");
        this.emit("GET");
        this.emit("LLOAD(TEMP)");
        this.emit("\"H\""); 
        this.emit("GET");
        this.emit("/");
        this.emit("LLOAD(TEMP)");
        this.walkExpression(ast.y, currentPage); // FIXME: Could corrupt TEMP.
        this.emit("SET(H)");
        this.emit("LLOAD(TEMP)");
        this.emit("\"H\"");
        this.emit("GET");
        this.emit("*");
        this.emit("SET(W)");
      } else if (ast.y==null) {
        // 'x' must be non-null.
        // We need to choose the height to preserve the aspect ratio.
        // Here is the equivant code:
        //   ratio = TEMP.H / TEMP.W
        //   TEMP.W = <x>
        //   TEMP.H = ratio * TEMP.W
        // (a stack location is used for the variable 'ratio'.)
        this.emit("LLOAD(TEMP)");
        this.emit("LLOAD(TEMP)");
        this.emit("\"H\"");
        this.emit("GET");
        this.emit("LLOAD(TEMP)");
        this.emit("\"W\""); 
        this.emit("GET");
        this.emit("/");
        this.emit("LLOAD(TEMP)");
        this.walkExpression(ast.x, currentPage); // FIXME: Could corrupt TEMP.
        this.emit("SET(W)");
        this.emit("LLOAD(TEMP)");
        this.emit("\"W\"");
        this.emit("GET");
        this.emit("*");
        this.emit("SET(H)");
      } else {
        // Both are non-null.
        this.emit("LLOAD(TEMP)");
        this.walkExpression(ast.x, currentPage); // FIXME: Could corrupt TEMP.
        this.emit("SET(W)");
        this.emit("LLOAD(TEMP)");
        this.walkExpression(ast.y, currentPage); // FIXME: Could corrupt TEMP.
        this.emit("SET(H)");
      }
    } else { // !isAbsolute
      if (ast.x==null || ast.y==null)
        throw new RuntimeException("Not yet implemented");
      this.emit("LLOAD(TEMP)");
      this.walkExpression(ast.x, currentPage); // FIXME: Could corrupt TEMP.
      this.emit("LLOAD(TEMP)");
      this.emit("\"W\"");
      this.emit("GET");
      this.emit("*");
      this.emit("SET(W)");
      this.emit("LLOAD(TEMP)");
      this.walkExpression(ast.y, currentPage); // FIXME: Could corrupt TEMP.
      this.emit("LLOAD(TEMP)");
      this.emit("\"H\"");
      this.emit("GET");
      this.emit("*");
      this.emit("SET(H)");
    }
  }

  /** Emits assembler for 'ast'. */
  private void walkPostfix(Postfix ast, Page currentPage) {
    switch (ast.type) {
      case SUBSCRIPT: {
        this.walkSubscript(((Postfix.Subscript)ast).subscript, currentPage);
        this.emit("GET");
        break;
      }
      case CALL: {
        final Postfix.Call astInner = (Postfix.Call)ast;
        this.emit("TABLE");
        for (int i=0; i<astInner.args.length; i++) {
          this.walkField(i, astInner.args.get(i), currentPage);
        }
        this.emit("CALL");
        break;
      }
      default: throw new RuntimeException("Unknown kind");
    }
  }

  /** Emits assembler for 'ast'. */
  private void walkSubscript(Subscript ast, Page currentPage) {
    switch(ast.type) {
      case SQUARE: {
        final Subscript.Square astInner = (Subscript.Square)ast;
        this.walkExpression(astInner.expression, currentPage);
        break;
      }
      case DOT: {
        final Subscript.Dot astInner = (Subscript.Dot)ast;
        this.emit("\""+astInner.id+"\"");
        break;
      }
      default: throw new RuntimeException("Unknown type");
    }
  }

  /** The Program passed to the constructor. */
  private final Program program;
  
  /* Test code. */
  
  public static void main(String[] args) {
    if (args.length!=2) throw new IllegalArgumentException(
      "Syntax: java org.sc3d.apt.crazon.compiler.Flattener "+
      "<wiki path> <page name>"
    );
    final FileSystem fs = new LocalFileSystem(new File(args[0]));
    final Program p = new Program(fs, args[1]);
    if (p.countErrors()!=0) {
      p.printErrorReport(System.out, 1);
      return;
    }
    final Flattener me = new Flattener(p);
    for (String i: me.output) {
      System.out.print(i+" ");
      if (";".equals(i)) System.out.println();
    }
  }
}
