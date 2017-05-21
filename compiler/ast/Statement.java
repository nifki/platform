package org.sc3d.apt.crazon.compiler.ast;

import java.util.*;
import org.sc3d.apt.sss.v3.*;

/** Implements what the grammar calls a 'statement'. */
public abstract class Statement extends ASTNode {
  private Statement(TT type) {
    this.type = type;
  }
  
  /* New API. */
  
  /** The type of the 'type' field. */
  public static enum TT {
    COMMENT,
    NOOP,
    ASSIGN,
    IF,
    WHILE,
    FOR,
    BREAK,
    ERROR,
    RETURN,
    CALL,
    ACTION,
    WAIT,
    DUMP
  };
  
  /** Identifies of which subclass this Statement is an instance. */
  public final TT type;

  /** Constructs a Statement from 'raw' if possible. Otherwise, annotates
   * 'raw' with appropriate error messages and returns 'null'. */
  public static Statement fromTree(Tree.Production raw) {
    if ("Comment".equals(raw.name)) {
      return new Comment(raw.getT(0).t);
    }
    if ("NoOp".equals(raw.name)) {
      return new NoOp();
    }
    if ("Assign".equals(raw.name)) {
      final LValue lValue = LValue.fromTree(raw.getP(0));
      final Expression expr = Expression.fromTree(raw.getP(2));
      if (lValue==null || expr==null) return null;
      return new Assign(lValue, raw.getT(1).t, expr);
    }
    if ("If".equals(raw.name)) {
      final Expression cond = Expression.fromTree(raw.getP(1));
      final ASTList<Statement> statements =
        Statement.fromTrees(raw.getT(2));
      final ASTList<Elif> elifs = Elif.fromTrees(raw.getNT(3));
      final boolean hasElse = raw.getNT(4).length>0;
      final Else els = hasElse ? Else.fromTree(raw.getP(4)) : null;
      if (
        cond==null ||
        statements==null ||
        elifs==null ||
        (hasElse && els==null)
      ) {
        return null;
      }
      return new If(raw.getT(0).t, cond, statements, elifs, els);
    }
    if ("While".equals(raw.name)) {
      final Expression cond = Expression.fromTree(raw.getP(1));
      final ASTList<Statement> statements =
        Statement.fromTrees(raw.getT(2));
      final boolean hasElse = raw.getNT(3).length>0;
      final Else els = hasElse ? Else.fromTree(raw.getP(3)) : null;
      if (cond==null || statements==null || (hasElse && els==null)) return null;
      return new While(raw.getT(0).t, cond, statements, els);
    }
    if ("For".equals(raw.name)) {
      final boolean hasKey = raw.getNT(1).length>0;
      final boolean hasVal = raw.getNT(3).length>0;
      final Expression table = Expression.fromTree(raw.getP(5));
      final ASTList<Statement> statements =
        Statement.fromTrees(raw.getT(6));
      final boolean hasElse = raw.getNT(7).length>0;
      final Else els = hasElse ? Else.fromTree(raw.getP(7)) : null;
      if (
        table==null ||
        statements==null ||
        (hasElse && els==null)
      ) {
        return null;
      }
      if (!hasKey && !hasVal) {
        raw.getT(2).t.addError(
          "You must provide a variable name for at least one of the key and "+
          "the value."
        );
        return null;
      }
      return
        new For(
          raw.getT(0).t,
          hasKey ? raw.getP(1).getT(0).t : null,
          hasVal ? raw.getP(3).getT(0).t : null,
          raw.getT(4).t,
          table,
          statements,
          els
        );
    }
    if ("Break".equals(raw.name)) {
      // SSS guarantees that there is at least one "BREAK" token.
      return new Break(raw.getNT(0).get(0).getT(0).t, raw.getNT(0).length);
    }
    if ("Error".equals(raw.name)) {
      final Expression expr = Expression.fromTree(raw.getP(1));
      if (expr==null) return null;
      return new Error(raw.getT(0).t, expr);
    }
    if ("Return".equals(raw.name)) {
      final boolean hasExpr = raw.getNT(1).length>0;
      final Expression expr = hasExpr ? Expression.fromTree(raw.getP(1)) : null;
      if (hasExpr && expr==null) return null;
      return new Return(raw.getT(0).t, expr);
    }
    if ("Call".equals(raw.name)) {
      final Expression expr = Expression.fromTree(raw.getP(0));
      final Tree.NonTerminal contents = (Tree.NonTerminal)raw.getT(1).parse();
      if (contents==null) return null;
      final ASTList<Field> args = Field.fromTrees(contents, "()");
      if (expr==null || args==null) return null;
      return new Call(expr, raw.getT(1).t, args);
    }
    if ("Action".equals(raw.name)) {
      final org.sc3d.apt.crazon.compiler.ast.Action action =
        org.sc3d.apt.crazon.compiler.ast.Action.fromTree(raw.getP(0));
      if (action==null) return null;
      return new Action(action);
    }
    if ("Wait".equals(raw.name)) {
      return new Wait(raw.getT(0).t);
    }
    if ("Dump".equals(raw.name)) {
      final Expression expr = Expression.fromTree(raw.getP(1));
      if (expr==null) return null;
      return new Dump(raw.getT(0).t, expr);
    }
    throw new RuntimeException("Unknown production: " + raw.name);
  }
  
  /** Constructs an ASTList<Statement> from 'raw', which represents what the
   * grammar calls a 'BRACE(statement*)'. If there are any errors, this method
   * annotates 'raw' with appropriate error messages and returns 'null'.
   * <p>
   * Note that 'raw' is a Tree.Terminal which represents the entire clause
   * including the braces, not a Tree.NonTerminal which represents the contents
   * of the braces.
   */
  public static ASTList<Statement> fromTrees(Tree.Terminal raw) {
    final Tree.NonTerminal contents = (Tree.NonTerminal)raw.parse();
    if (contents==null) return null;
    final ASTList<Statement> ans =
      new ASTList<Statement>(contents.length, "{}", null);
    for (int i=0; i<contents.length; i++) ans.add(fromTree(contents.get(i)));
    return ans.fix();
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Statement whose 'type' field is 'TT.COMMENT',
   * which represents what the grammar calls a 'Comment'. */
  public static class Comment extends Statement {
    public Comment(Token comment) {
      super(TT.COMMENT);
      this.comment = comment;
    }
    
    /** The Token which represents the Comment. */
    public final Token comment;
    
    /* Override things in ASTNode. */
    
    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.comment).append(prefix);
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Statement whose 'type' field is 'TT.NOOP',
   * which represents what the grammar calls an 'NoOp'. */
  public static class NoOp extends Statement {
    public NoOp() { super(TT.NOOP); }

    /* Override things in ASTNode. */
    
    /** Returns ";". */
    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(";");
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Statement whose 'type' field is 'TT.ASSIGN',
   * which represents what the grammar calls an 'Assign'. */
  public static class Assign extends Statement {
    public Assign(LValue lValue, Token eq, Expression expression) {
      super(TT.ASSIGN);
      this.lValue = lValue;
      this.eq = eq;
      this.expression = expression;
    }
    
    /* New API. */
    
    /** The LValue to the left of the "=" sign. */
    public final LValue lValue;
    
    /** The "=" sign. */
    public final Token eq;
    
    /** The Expression to the right of the "=" sign. */
    public final Expression expression;
    
    /* Override things in ASTNode. */
    
    public void toSourceCode(String prefix, StringBuffer sb) {
      this.lValue.toSourceCode(prefix, sb);
      sb.append(" = ");
      this.expression.toSourceCode(prefix, sb);
    }
    
    public void findAssignments(
      HashMap<String, Token> locals,
      HashMap<String, Token> globals
    ) {
      this.lValue.findAssignments(locals, globals);
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Statement whose 'type' field is 'TT.IF',
   * which represents what the grammar calls an 'If'. */
  public static class If extends Statement {
    public If(
      Token keyword,
      Expression condition,
      ASTList<Statement> statements,
      ASTList<Elif> elifs,
      Else els
    ) {
      super(TT.IF);
      this.keyword = keyword;
      this.condition = condition;
      this.statements = statements;
      this.elifs = elifs;
      this.els = els;
    }
    
    /* New API. */
    
    /** The Token that represents the "IF" keyword. */
    public final Token keyword;
    
    /** The Expression that represents the condition tested by this If. */
    public final Expression condition;
    
    /** The statements executed if the condition is true. */
    public final ASTList<Statement> statements;

    /** The optional ELIF blocks. */
    public final ASTList<Elif> elifs;
    
    /** The optional statements executed if the "IF" condition and all the
     * conditionns of the optional "ELIF" blocks are false, or 'null' if
     * there is no "ELSE" clause. */
    public final Else els;
    
    /* Override things in ASTNode. */
    
    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword).append(' ');
      this.condition.toSourceCode(prefix, sb);
      sb.append(' ');
      this.statements.toSourceCode(prefix, sb);
      if (this.els != null) {
        sb.append(' ');
        this.els.toSourceCode(prefix, sb);
      }
    }

    public void findAssignments(
      HashMap<String, Token> locals,
      HashMap<String, Token> globals
    ) {
      if (this.els!=null) this.els.findAssignments(locals, globals);
      this.statements.findAssignments(locals, globals);
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Statement whose 'type' field is 'TT.WHILE',
   * which represents what the grammar calls a 'While'. */
  public static class While extends Statement {
    public While(
      Token keyword,
      Expression condition,
      ASTList<Statement> statements,
      Else els
    ) {
      super(TT.WHILE);
      this.keyword = keyword;
      this.condition = condition;
      this.statements = statements;
      this.els = els;
    }
    
    /* New API. */
    
    /** The Token that represents the "WHILE" keyword. */
    public final Token keyword;
    
    /** The Expression that represents the condition tested by this While. */
    public final Expression condition;
    
    /** The statements executed while the condition is true. */
    public final ASTList<Statement> statements;
    
    /** The optional statements executed when the condition is false, or
     * 'null' if there is no "ELSE" clause. */
    public final Else els;
    
    /* Override things in ASTNode. */
    
    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword).append(' ');
      this.condition.toSourceCode(prefix, sb);
      sb.append(' ');
      this.statements.toSourceCode(prefix, sb);
      if (this.els != null) {
        sb.append(' ');
        this.els.toSourceCode(prefix, sb);
      }
    }

    public void findAssignments(
      HashMap<String, Token> locals,
      HashMap<String, Token> globals
    ) {
      if (this.els!=null) this.els.findAssignments(locals, globals);
      this.statements.findAssignments(locals, globals);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Statement whose 'type' field is 'TT.FOR',
   * which represents what the grammar calls a 'For'. */
  public static class For extends Statement {
    public For(
      Token keyword,
      Token key,
      Token value,
      Token in,
      Expression table,
      ASTList<Statement> statements,
      Else els
    ) {
      super(TT.FOR);
      this.keyword = keyword;
      this.key = key;
      this.value = value;
      this.in = in;
      this.table = table;
      this.statements = statements;
      this.els = els;
    }
    
    /* New API. */
    
    /** The Token that represents the "FOR" keyword. */
    public final Token keyword;
    
    /** The identifier of the local variable to which the key is assigned on
     * each iteration, or 'null' if omitted. */
    public final Token key;
    
    /** The identifier of the local variable to which the value is assigned on
     * each iteration, or 'null' if omitted. */
    public final Token value;
    
    /** The Token that represents the "IN" keyword. */
    public final Token in;
    
    /** The Expression that represents the table through which this For loops.
     */
    public final Expression table;
    
    /** The statements executed for each key-value pair in the table. */
    public final ASTList<Statement> statements;
    
    /** The optional statements executed when the loop terminates normally, or
     * 'null' if there is no "ELSE" clause. */
    public final Else els;
    
    /* Override things in ASTNode. */
    
    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword).append(' ');
      if (this.key != null) sb.append(this.key);
      sb.append('=');
      if (this.value != null) sb.append(this.value);
      sb.append(' ').append(this.in).append(' ');
      this.table.toSourceCode(prefix, sb);
      sb.append(' ');
      this.statements.toSourceCode(prefix, sb);
      if (this.els != null) {
        sb.append(' ');
        this.els.toSourceCode(prefix, sb);
      }
    }
    
    public void findAssignments(
      HashMap<String, Token> locals,
      HashMap<String, Token> globals
    ) {
      if (this.els!=null) this.els.findAssignments(locals, globals);
      this.statements.findAssignments(locals, globals);
      if (this.value!=null) locals.put(this.value.toString(), this.value);
      if (this.key!=null) locals.put(this.key.toString(), this.key);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Statement whose 'type' field is 'TT.BREAK', which
   * represents what the grammar calls a 'Break'. */
  public static class Break extends Statement {
    public Break(Token keyword, int depth) {
      super(TT.BREAK);
      this.keyword = keyword;
      this.depth = depth;
    }
    
    /* New API. */
    
    /** The first "BREAK" keyword. */
    public final Token keyword;
    
    /** The number of repeats of the keyword "BREAK" (including the first which
     * is called 'keyword'), which indicates the number of nested loops to break
     * out of. */
    public final int depth;
    
    /* Override things in ASTNode. */
    
    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword);
      for (int i=1; i<this.depth; i++) {
        sb.append(" BREAK");
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Statement whose 'type' field is 'TT.ERROR', which
   * represents what the grammar calls an 'Error'. */
  public static class Error extends Statement {
    public Error(Token keyword, Expression message) {
      super(TT.ERROR);
      this.keyword = keyword;
      this.message = message;
    }
    
    /* New API. */
    
    /** The "ERROR" keyword. */
    public final Token keyword;
    
    /** The Expression that represents the error message. */
    public final Expression message;
    
    /* Override things in ASTNode. */
    
    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword).append(' ');
      this.message.toSourceCode(prefix, sb);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Statement whose 'type' field is 'TT.RETURN', which
   * represents what the grammar calls a 'Return'. */
  public static class Return extends Statement {
    public Return(Token keyword, Expression expression) {
      super(TT.RETURN);
      this.keyword = keyword;
      this.expression = expression;
    }
    
    /* New API. */
    
    /** The "RETURN" keyword. */
    public final Token keyword;
    
    /** The Expression that represents the return value, or 'null' if omitted.
     * */
    public final Expression expression;
    
    /* Override things in ASTNode. */
    
    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword);
      if (this.expression!=null) {
        sb.append(' ');
        this.expression.toSourceCode(prefix, sb);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Statement whose 'type' field is 'TT.CALL', which
   * represents what the grammar calls a 'Call'. */
  public static class Call extends Statement {
    public Call(Expression function, Token round, ASTList<Field> args) {
      super(TT.CALL);
      this.function = function;
      this.round = round;
      this.args = args;
    }
    
    /* New API. */
    
    /** The Expression that represents the function to call. */
    public final Expression function;
    
    /** The Token that represents the argument list in round brackets. This is
     * provided so that you can call its 'addError()' method. */
    public final Token round;
    
    /** The Arguments to pass to the function. This ASTList has round brackets
     * and ", " as the separator. */
    public final ASTList<Field> args;
    
    /* Override things in ASTNode. */
    
    public void toSourceCode(String prefix, StringBuffer sb) {
      this.function.toSourceCode(prefix, sb);
      this.args.toSourceCode(prefix, sb);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Statement whose 'type' field is 'TT.ACTION', which
   * represents what the grammar calls an 'Action'. */
  public static class Action extends Statement {
    public Action(org.sc3d.apt.crazon.compiler.ast.Action action) {
      super(TT.ACTION);
      this.action = action;
    }
    
    /* New API. */
    
    /** The Action object that this class wraps. */
    public final org.sc3d.apt.crazon.compiler.ast.Action action;
    
    /* Override things in ASTNode. */
    
    public void toSourceCode(String prefix, StringBuffer sb) {
      this.action.toSourceCode(prefix, sb);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Statement whose 'type' field is 'TT.WAIT', which
   * represents what the grammar calls a 'Wait'. */
  public static class Wait extends Statement {
    public Wait(Token keyword) {
      super(TT.WAIT);
      this.keyword = keyword;
    }
    
    /* New API. */
    
    /** The "WAIT" keyword. */
    public final Token keyword;
    
    /* Override things in ASTNode. */
    
    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Statement whose 'type' field is 'TT.DUMP', which
   * represents what the grammar calls a 'Dump'. */
  public static class Dump extends Statement {
    public Dump(Token keyword, Expression expression) {
      super(TT.DUMP);
      this.keyword = keyword;
      this.expression = expression;
    }
    
    /* New API. */
    
    /** The "DUMP" keyword. */
    public final Token keyword;
    
    /** The Expression that this Dump prints out. */
    public final Expression expression;
    
    /* Override things in ASTNode. */
    
    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword).append(' ');
      this.expression.toSourceCode(prefix, sb);
    }
  }
}
