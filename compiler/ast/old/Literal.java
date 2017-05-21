package org.sc3d.apt.crazon.ast;
import org.sc3d.apt.sss.v3.Token;

/** Represents what the grammar refers to as a 'Boolean', 'Number' or
 * 'String' */
public class Literal extends ASTNode {
  public Literal(Token token) {
    this.token = token;
    if (token.type == Token.TYPE_NUMBER) this.type = TYPE_NUMBER;
    else if (token.type == Token.TYPE_STRING) this.type = TYPE_STRING;
    else if (token.type == Token.TYPE_WORD) {
      if (
        token.toString().equals("TRUE") ||
        token.toString().equals("FALSE")
      )
        this.type = TYPE_BOOLEAN;
      else
        throw new IllegalArgumentException("Unknown boolean value");
    } else {
      throw new IllegalArgumentException("Unknown literal type");
    }
  }

  /* New API. */

  /** The Token that represents this literal in the source code. */
  public final Token token;

  /** A value for the 'type' field to indicate that this is a Boolean. */
  public static final int TYPE_BOOLEAN = 1;

  /** A value for the 'type' field to indicate that this is a Number. */
  public static final int TYPE_NUMBER = 2;

  /** A value for the 'type' field to indicate that this is a String. */
  public static final int TYPE_STRING = 3;

  /** One of the values TYPE_BOOLEAN, TYPE_NUMBER or TYPE_STRING. */
  public final int type;

  /** Override things in ASTNode. */

  public void toSourceCode(String prefix, StringBuffer sb) {
    sb.append(this.token);
  }

  public String toString() { return this.token.toString(); }
}

