package org.sc3d.apt.crazon.vm.state;

/** Defines the Crazon names of the keys on the keyboard. */
public class KeyNames {
  /** Don't want any instances of this class. */
  private KeyNames() {}
  
  /* New API. */
  
  /** The common prefix of the key names for keys labelled with a letter. For
   * example, the key labelled "A" has the name 'LETTER+"A"' which evaluates to
   * "LetterA". */
  public static final String LETTER = "Letter";

  /** The common prefix of the key names for keys labelled with a number. For
   * example, the key labelled "1" has the name 'NUMBER+"1"' which evaluates to
   * "Number1". */
  public static final String NUMBER = "Number";
  
  // Top row.
  
  /** The name of the key labelled "Escape". */
  public static final String ESCAPE = "Escape";
  
  // FIXME: Function keys go here.

  /** The name of the key labelled "PrintScreen". */
  public static final String PRINTSCREEN = "PrintScreen";

  /** The name of the key labelled "ScrollLock". */
  public static final String SCROLL_LOCK = "ScrollLock";

  /** The name of the key labelled "Break". */
  public static final String BREAK = "Break";

  // Typewriter first row.

  /** The name of the key labelled "BackQuote". */
  public static final String BACK_QUOTE = "BackQuote";

  /** The name of the key labelled "Minus". */
  public static final String MINUS = "Minus";

  /** The name of the key labelled "Equals". */
  public static final String EQUALS = "Equals";

  /** The name of the key labelled "BackSpace". */
  public static final String BACK_SPACE = "BackSpace";

  // Typewriter second row.

  /** The name of the key labelled "Tab". */
  public static final String TAB = "Tab";

  /** The name of the key labelled "OpenBracket". */
  public static final String OPEN_BRACKET = "OpenBracket";

  /** The name of the key labelled "CloseBracket". */
  public static final String CLOSE_BRACKET = "CloseBracket";

  /** The name of the key labelled "LeftBrace". */
  public static final String LEFT_BRACE = "LeftBrace";

  /** The name of the key labelled "RightBrace". */
  public static final String RIGHT_BRACE = "RightBrace";

  /** The name of the key labelled "Enter". */
  public static final String ENTER = "Enter";

  // Typewriter third row.

  /** The name of the key labelled "CapsLock". */
  public static final String CAPS_LOCK = "CapsLock";

  /** The name of the key labelled "Semicolon". */
  public static final String SEMICOLON = "Semicolon";

  /** The name of the key labelled "Quote". */
  public static final String QUOTE = "Quote";

  /** The name of the key labelled "Hash". */
  public static final String HASH = "Hash";

  // Typewriter fourth row.

  /** The name of the key labelled "Shift". */
  public static final String SHIFT = "Shift";

  /** The name of the key labelled "BackSlash". */
  public static final String BACK_SLASH = "BackSlash";

  /** The name of the key labelled "Comma". */
  public static final String COMMA = "Comma";

  /** The name of the key labelled "FullStop". */
  public static final String FULL_STOP = "FullStop";

  /** The name of the key labelled "Slash". */
  public static final String SLASH = "Slash";

  // Typewriter fifth row.

  /** The name of the key labelled "Control". */
  public static final String CONTROL = "Control";

  /** The name of the key labelled "Alt". */
  public static final String ALT = "Alt";

  /** The name of the key labelled "Space". */
  public static final String SPACE = "Space";

  // Navigation keys.

  /** The name of the key labelled "Insert". */
  public static final String INSERT = "Insert";

  /** The name of the key labelled "Home". */
  public static final String HOME = "Home";

  /** The name of the key labelled "PageUp". */
  public static final String PAGE_UP = "PageUp";

  /** The name of the key labelled "Delete". */
  public static final String DELETE = "Delete";

  /** The name of the key labelled "End". */
  public static final String END = "End";

  /** The name of the key labelled "PageDown". */
  public static final String PAGE_DOWN = "PageDown";

  // Arrow keys.

  /** The name of the key labelled "DownArrow". */
  public static final String DOWN_ARROW = "DownArrow";

  /** The name of the key labelled "LeftArrow". */
  public static final String LEFT_ARROW = "LeftArrow";

  /** The name of the key labelled "RightArrow". */
  public static final String RIGHT_ARROW = "RightArrow";

  /** The name of the key labelled "UpArrow". */
  public static final String UP_ARROW = "UpArrow";

  // FIXME: Numpad keys go here.
}
