package org.sc3d.apt.crazon.compiler.ast;

import java.util.*;
import org.sc3d.apt.sss.v3.*;

/** Represents what the grammar calls a 'declaration'. */
public abstract class Declaration extends ASTNode {
  /** For use by subclasses only. */
  private Declaration(TT type) {
    this.type = type;
  }
  
  /* New API. */
  
  /** The type of the 'type' field. */
  public static enum TT {
    // @todo COMMENT,
    DEFINE,
    STATEMENT
  };
  
  /** Identifies of which subclass this Declaration is an instance. */
  public final TT type;

  /** Constructs a Declaration from 'raw' if possible. Otherwise, annotates
   * 'raw' with appropriate error messages and returns 'null'. */
  public static Declaration fromTree(Tree.Production raw) {
    if ("Define".equals(raw.name)) {
      Tree.NonTerminal contents = (Tree.NonTerminal)raw.getT(2).parse();
      ASTList<Arg> args =
        contents==null ? null : Arg.fromTrees(contents);
      ASTList<org.sc3d.apt.crazon.compiler.ast.Statement> body =
        org.sc3d.apt.crazon.compiler.ast.Statement.fromTrees(raw.getT(3));
      if (args==null || body==null) return null;
      return new Define(
        raw.getT(0).t,
        raw.getP(1).getT(0).t,
        args,
        body
      );
    }
    if ("Statement".equals(raw.name)) {
      final org.sc3d.apt.crazon.compiler.ast.Statement statement =
        org.sc3d.apt.crazon.compiler.ast.Statement.fromTree(raw.getP(0));
      if (statement==null) return null;
      return new Statement(statement);
    }
    throw new RuntimeException("Unknown production: "+raw.name);
  }
  
  /** Constructs an ASTList<Declaration> from 'raw', which represents what the
   * grammar calls a 'declaration*'. If there are any errors, annotates 'raw'
   * with appropriate errors and returns 'null'. */
  public static ASTList<Declaration> fromTrees(Tree.NonTerminal raw) {
    ASTList<Declaration> ans = new ASTList<Declaration>(raw.length, null, null);
    for (int i = 0; i<raw.length; i++) {
      ans.add(fromTree(raw.get(i)));
    }
    return ans.fix();
  }

  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Declaration whose 'type' field is 'TT.DEFINE', which
   * represents what the grammar calls a 'Define'. */
  public static class Define extends Declaration {
    public Define(
      Token keyword,
      Token identifier,
      ASTList<Arg> args,
      ASTList<org.sc3d.apt.crazon.compiler.ast.Statement> body
    ) {
      super(TT.DEFINE);
      this.keyword = keyword;
      this.identifier = identifier;
      this.args = args;
      this.body = body;
    }
    
    /* New API. */
    
    /** The keyword "DEF". */
    public final Token keyword;
    
    /** The name of the function being defined. */
    public final Token identifier;
    
    /** The function arguments. */
    public final ASTList<Arg> args;
    
    /** The body of the function being defined. */
    public final ASTList<org.sc3d.apt.crazon.compiler.ast.Statement> body;
    
    /* Override things in ASTNode. */
    
    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword).append(' ').append(this.identifier);
      this.args.toSourceCode(prefix, sb);
      sb.append(' ');
      this.body.toSourceCode(prefix, sb);
    }

    public void findAssignments(
      HashMap<String, Token> locals,
      HashMap<String, Token> globals
    ) {
      this.body.findAssignments(locals, globals);
      this.args.findAssignments(locals, globals);
      globals.put(this.identifier.toString(), this.identifier);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of Declaration whose 'type' field is 'TT.STATEMENT', which
   * represents what the grammar calls a 'Statement'. */
  public static class Statement extends Declaration {
    public Statement(org.sc3d.apt.crazon.compiler.ast.Statement statement) {
      super(TT.STATEMENT);
      this.statement = statement;
    }
    
    /* New API. */
    
    /** The Statement that this object wraps. */
    public final org.sc3d.apt.crazon.compiler.ast.Statement statement;
    
    /* Override things in ASTNode. */
    
    public void toSourceCode(String prefix, StringBuffer sb) {
      this.statement.toSourceCode(prefix, sb);
    }
    
    public void findAssignments(
      HashMap<String, Token> locals,
      HashMap<String, Token> globals
    ) {
      this.statement.findAssignments(locals, globals);
    }
  }
}
