package org.sc3d.apt.crazon.ast;
import org.sc3d.apt.sss.v3.*;

/** Represents what the grammar calls a 'variable'. */
public class Variable extends ASTNode {
  /** Constructs a Variable given values for its fields. */
  public Variable(Token page, Token underscore, Token id) {
    this.page = page;
    this.underscore = underscore;
    this.id = id;
  }

  /* New API. */

  /** The 'page name' part of this Variable, or 'null' if this is a local
   * Variable or if the 'page name' part was omitted. */
  public final Token page;

  /** The Token that represents the underscore character between the
   * 'page name' part and the 'variable name' part, or 'null' if this is a
   * local Variable. */
  public final Token underscore;

  /** The 'variable name' part of this Variable. */
  public final Token id;

  /** Returns the Crazon assembler variable name that represents this
   * Variable.
   * @param currentPage  the name of the wiki page on which this Variable
   * appears. This will be used to supply the 'page name' part if it was
   * absent in the source code.
   */
  public String getAssemblerName(String currentPage) {
    final StringBuffer ans = new StringBuffer();
    if (this.isGlobal()) {
      ans.append(this.page==null ? currentPage : this.page.toString());
      ans.append(this.underscore);
    }
    return ans.append(this.id).toString();
  }

  /** Returns 'true' if this Variable is global, i.e. if the underscore is
   * present. */
  public boolean isGlobal() {
    return this.underscore!=null;
  }
  
  /** Constructs a Variable from 'raw', if possible, otherwise annotates 'raw'
   * with error messages and returns 'null'. */
  public static Variable fromTree(Tree.Production raw) {
    if ("Local".equals(raw.name)) {
      return new Variable(null, null, raw.getP(0).getT(0).t);
    }
    if ("Global".equals(raw.name)) {
      final Tree.Production page = raw.getP(0);
      return new Variable(
        page==null ? null : page.getT(0).t,
        raw.getT(1).t,
        raw.getP(2).getT(0).t
      );
    }
    throw new RuntimeException("Unknown production: "+raw.name);
  }

  /* Override things in ASTNode. */

  public void toSourceCode(String prefix, StringBuffer sb) {
    if (this.underscore!=null) {
      if (this.page!=null) {
        sb.append(this.page);
      }
      sb.append("_");
    }
    sb.append(this.id);
  }
}
