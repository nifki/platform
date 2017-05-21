package org.sc3d.apt.crazon.vm.platform;

import org.sc3d.apt.crazon.vm.util.*;

import java.awt.event.*;
import java.util.*;

/** Maintains a table of currently pressed keys. */
public class Keys implements KeyListener {
  /** Constructs a Keys. */
  public Keys() {
    this.keys = null;
    for (Iterator it = MAP.keySet().iterator(); it.hasNext(); ) {
      final Integer code = (Integer)it.next();
      final Value.Str name = (Value.Str)MAP.get(code);
      this.keys = Table.put(this.keys, name, Value.Bool.FALSE);
    }
  }
  
  /* New API. */
  
  /* Implement things in KeyListener. */
  
  /** Called by the AWT when a key is pressed. */
  public synchronized void keyPressed(KeyEvent e) {
    final Value.Str name = (Value.Str)MAP.get(new Integer(e.getKeyCode()));
    if (name!=null) {
      this.keys = Table.put(this.keys, name, Value.Bool.TRUE);
    } else {
      System.out.println("Unknown code: "+KeyEvent.getKeyText(e.getKeyCode()));
    }
  }

  /** Called by the AWT when a key is released. */
  public synchronized void keyReleased(KeyEvent e) {
    final Value.Str name = (Value.Str)MAP.get(new Integer(e.getKeyCode()));
    if (name!=null) {
      this.keys = Table.put(this.keys, name, Value.Bool.FALSE);
    } else {
      System.out.println("Unknown code: "+KeyEvent.getKeyText(e.getKeyCode()));
    }
  }
  
  /** Called by the AWT when a key is typed. This implentation does nothing. */
  public void keyTyped(KeyEvent e) {}
  
  /* Private. */
  
  /** A Table that maps key names (Value.Strs) to booleans (Value.Bools). */
  private Table keys;
  
  /** A Map from Java key codes (Integers) to Crazon key names (Value.Strs). */
  private static final HashMap MAP = new HashMap();
  private static void installKey(int code, String name) {
    if (MAP.put(new Integer(code), new Value.Str(name))!=null)
      throw new IllegalArgumentException();
  }
  static {
    // Patterned cases.
    for (char c='0'; c<='9'; c++) installKey(c, "Number"+c);
    for (char c='A'; c<='Z'; c++) installKey(c, ""+c);
    // Top row.
    installKey(KeyEvent.VK_ESCAPE, "Escape");
    // FIXME: Function keys go here.
    installKey(KeyEvent.VK_PRINTSCREEN, "PrintScreen");
    installKey(KeyEvent.VK_SCROLL_LOCK, "ScrollLock");
    installKey(KeyEvent.VK_PAUSE, "Break");
    // Typewriter first row.
    installKey(KeyEvent.VK_BACK_QUOTE, "BackQuote");
    installKey(KeyEvent.VK_MINUS, "Minus");
    installKey(KeyEvent.VK_EQUALS, "Equals");
    installKey(KeyEvent.VK_BACK_SPACE, "BackSpace");
    // Typewriter second row.
    installKey(KeyEvent.VK_TAB, "Tab");
    installKey(KeyEvent.VK_OPEN_BRACKET, "OpenBracket");
    installKey(KeyEvent.VK_CLOSE_BRACKET, "CloseBracket");
    installKey(KeyEvent.VK_BRACELEFT, "LeftBrace");
    installKey(KeyEvent.VK_BRACERIGHT, "RightBrace");
    installKey(KeyEvent.VK_ENTER, "Enter");
    // Typewriter third row.
    installKey(KeyEvent.VK_CAPS_LOCK, "CapsLock");
    installKey(KeyEvent.VK_SEMICOLON, "Semicolon");
    installKey(KeyEvent.VK_QUOTE, "Quote");
    installKey(KeyEvent.VK_NUMBER_SIGN, "Hash");
    // Typewriter fourth row.
    installKey(KeyEvent.VK_SHIFT, "Shift");
    installKey(KeyEvent.VK_BACK_SLASH, "BackSlash");
    installKey(KeyEvent.VK_COMMA, "Comma");
    installKey(KeyEvent.VK_PERIOD, "FullStop");
    installKey(KeyEvent.VK_SLASH, "Slash");
    // Typewriter fifth row.
    installKey(KeyEvent.VK_CONTROL, "Control");
    installKey(KeyEvent.VK_ALT, "Alt");
    installKey(KeyEvent.VK_ALT_GRAPH, "Alt");
    installKey(KeyEvent.VK_SPACE, "Space");
    // Navigation keys.
    installKey(KeyEvent.VK_INSERT, "Insert");
    installKey(KeyEvent.VK_HOME, "Home");
    installKey(KeyEvent.VK_PAGE_UP, "PageUp");
    installKey(KeyEvent.VK_DELETE, "Delete");
    installKey(KeyEvent.VK_END, "End");
    installKey(KeyEvent.VK_PAGE_DOWN, "PageDown");
    // Arrow keys.
    installKey(KeyEvent.VK_DOWN, "DownArrow");
    installKey(KeyEvent.VK_LEFT, "LeftArrow");
    installKey(KeyEvent.VK_RIGHT, "RightArrow");
    installKey(KeyEvent.VK_UP, "UpArrow");
  }
}
