package org.sc3d.apt.crazon.compiler.ast;

import java.util.*;
import org.sc3d.apt.sss.v3.*;

/** Represents what the grammar calls an 'lValue'. */
public abstract class LValue extends ASTNode {
  /** Constructs an LValue given values for its fields. For use only by
   * subclasses. */
  private LValue(TT type, Token identifier, ASTList<Subscript> subscripts) {
    this.type = type;
    this.identifier = identifier;
    this.subscripts = subscripts;
  }
  
  /* New API. */
  
  /** The type of the 'type' field. */
  public static enum TT { LOCAL, GLOBAL, LONG };
  
  /** A value which indicates of which inner subclass this LValue is an
   * instance. */
  public final TT type;
  
  /** The identifier. */
  public final Token identifier;
  
  /** The Subscripts. */
  public final ASTList<Subscript> subscripts;
  
  /** Constructs an LValue from 'raw' if possible. Otherwise annotates 'raw'
   * with appropriate error messages. */
  public static LValue fromTree(Tree.Production raw) {
    if ("Local".equals(raw.name)) {
      final ASTList<Subscript> subs = Subscript.fromTrees(raw.getNT(1));
      if (subs==null) return null;
      return new Local(raw.getP(0).getT(0).t, subs);
    }
    if ("Global".equals(raw.name)) {
      final ASTList<Subscript> subs = Subscript.fromTrees(raw.getNT(2));
      if (subs==null) return null;
      return new Global(raw.getT(0).t, raw.getP(1).getT(0).t, subs);
    }
    if ("Long".equals(raw.name)) {
      final ASTList<Subscript> subs = Subscript.fromTrees(raw.getNT(3));
      if (subs==null) return null;
      return new Long(raw.getP(0).getT(0).t, raw.getP(2).getT(0).t, subs);
    }
    throw new RuntimeException("Unknown production: " + raw.name);
  }

  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of LValue whose 'type' field is 'TT.LOCAL' which represents
   * what the grammar refers to as a 'Local'. */
  public static class Local extends LValue {
    public Local(Token identifier, ASTList<Subscript> subscripts) {
      super(TT.LOCAL, identifier, subscripts);
    }
    
    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.identifier);
      this.subscripts.toSourceCode(prefix, sb);
    }
    
    public void findAssignments(
      HashMap<String, Token> locals,
      HashMap<String, Token> globals
    ) {
      locals.put(this.identifier.toString(), this.identifier);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of LValue whose 'type' field is 'TT.GLOBAL' which represents
   * what the grammar refers to as a 'Global'. */
  public static class Global extends LValue {
    public Global(
      Token keyword,
      Token identifier,
      ASTList<Subscript> subscripts
    ) {
      super(TT.GLOBAL, identifier, subscripts);
      this.keyword = keyword;
    }
    
    /* New API. */
    
    /** The keyword "GLOBAL". */
    public final Token keyword;
    
    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.keyword).append(' ').append(this.identifier);
      this.subscripts.toSourceCode(prefix, sb);
    }
    
    public void findAssignments(
      HashMap<String, Token> locals,
      HashMap<String, Token> globals
    ) {
      globals.put(this.identifier.toString(), this.identifier);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  
  /** The subclass of LValue whose 'type' field is 'TT.LONG' which represents
   * what the grammar refers to as a 'Long'. */
  public static class Long extends LValue {
    public Long(
      Token page,
      Token identifier,
      ASTList<Subscript> subscripts
    ) {
      super(TT.LONG, identifier, subscripts);
      this.page = page;
    }
    
    /* New API. */
    
    /** The identifier before the underscore; the name of the Wiki page of the
     * global variable to which this LValue refers. */
    public final Token page;
    
    /* Override things in ASTNode. */

    public void toSourceCode(String prefix, StringBuffer sb) {
      sb.append(this.page).append('_').append(this.identifier);
      this.subscripts.toSourceCode(prefix, sb);
    }
  }
}
