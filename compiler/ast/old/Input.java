package org.sc3d.apt.crazon.ast;
import org.sc3d.apt.sss.v3.Token;

/** Represents what the grammar calls an 'input'. */
public class Input extends ASTNode {
  /** Constructs an Input. */
  public Input(Token keyword) {
    this.keyword = keyword;
    final String s = keyword.toString();
    if ("MOUSE".equals(s)) this.type = TYPE_MOUSE;
    else if ("KEYS".equals(s)) this.type = TYPE_KEYS;
    else if ("GET".equals(s)) this.type = TYPE_GET;
    else if ("TIME".equals(s)) this.type = TYPE_TIME;
    else if ("RANDOM".equals(s)) this.type = TYPE_RANDOM;
    else throw new IllegalArgumentException("Unknown Input keyword");
  }

  /* New API. */

  /** A value for the 'type' field of "MOUSE" Inputs. */
  public static final int TYPE_MOUSE = 1;

  /** A value for the 'type' field of "KEYS" Inputs. */
  public static final int TYPE_KEYS = 2;

  /** A value for the 'type' field of "GET" Inputs. */
  public static final int TYPE_GET = 3;

  /** A value for the 'type' field of "TIME" Inputs. */
  public static final int TYPE_TIME = 4;

  /** A value for the 'type' field of "RANDOM" Inputs. */
  public static final int TYPE_RANDOM = 5;

  /** One of the 'TYPE_XXX' values indicating what kind of Input this is. */
  public final int type;

  /** The Token that represents the keyword. */
  public final Token keyword;

  /* Override things in ASTNode. */

  public void toSourceCode(String prefix, StringBuffer sb) {
    sb.append(this.keyword);
  }
}

