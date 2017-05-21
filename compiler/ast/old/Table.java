package org.sc3d.apt.crazon.ast;

/** Represents what the grammar calls a 'Table'. */
public class Table extends ASTNode {
  /** Constructs a Table. */
  public Table(Field[] fields) {
    for (int i = 1;  i < fields.length;  ++i) {
      if (fields[i-1].isNamed() && ! fields[i].isNamed()) {
        throw new IllegalArgumentException(
                    "Numbered fields must precede named fields");
      }
    }
    this.fields = fields;
  }

  /* New API. */

  public final int length() { return this.fields.length; }

  public final Field get(int i) { return this.fields[i]; }

  /** Override things in ASTNode. */

  public void toSourceCode(String prefix, StringBuffer sb) {
    String sep = "";
    for (int i = 0;  i < this.fields.length;  ++i) {
      sb.append(sep);
      this.fields[i].toSourceCode(prefix+"  ", sb);
      sep = ", ";
    }
  }

  /** Returns e.g. '[&lt;5 fields&gt;]'. */
  public String toString() {
    return "[<" + this.length() + " fields>]";
  }

  /* Private. */
  private final Field[] fields;
}

