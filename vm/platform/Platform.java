package org.sc3d.apt.crazon.vm.platform;

import org.sc3d.apt.crazon.vm.state.*;
import org.sc3d.apt.crazon.vm.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

/** Implementation of the AbstractPlatform class. */
public class Platform extends AbstractPlatform implements KeyListener {
  public Platform(int width, int height, int msPerFrame) {
    super(width, height);
    this.cc = new CrazonCanvas(width, height, msPerFrame);
    // Construct but don't show the debug window.
    this.debug = new TextArea(15, 80) {
      public boolean isFocusTraversable() { return false; }
    };
    this.debug.setEditable(false);
    this.debug.setFont(new Font("Monospaced", Font.PLAIN, 12));
    this.debugWindow = new Frame("Debug output");
    this.debugWindow.add(this.debug);
    this.debugWindow.pack();
    // Initialise the key table.
    this.keyTable = null;
    for (Iterator it = KEY_MAP.keySet().iterator(); it.hasNext(); ) {
      final Value.Str keyName = (Value.Str)KEY_MAP.get(it.next());
      this.keyTable = Table.put(this.keyTable, keyName, Value.Bool.FALSE);
    }
    this.cc.addKeyListener(this);
  }
  
  /* New API. */
  
  /** The CrazonCanvas on which output will be displayed. */
  public final CrazonCanvas cc;

  /** The window in which debugging output from the 'print()' method is shown.
   */
  public final Frame debugWindow;
  
  /* Override things in AbstractPlatform. */
  
  public Value.Tab getKeys() {
    return new Value.Tab(this.keyTable);
  }
  
  public Value.Pic newPicture(String varName, java.io.InputStream in)
  throws IOException {
    // Read from the InputStream until there are no more bytes.
    byte[] buf = new byte[1024];
    int used = 0;
    while (true) {
      final int n = in.read(buf, used, buf.length-used);
      if (n==-1) break;
      used += n;
      if (used>=buf.length) {
        byte[] old = buf;
        buf = new byte[2*used];
        System.arraycopy(old, 0, buf, 0, used);
      }
    }
    // Construct a Java Image object.
    final Image image = Toolkit.getDefaultToolkit().createImage(buf, 0, used);
    if (image==null || !ImageLoader.waitUntilLoaded(image))
      return null; // Couldn't understand the image data (probably).
    // Extract width, height and check they are sensible.
    final int w = image.getWidth(null), h = image.getHeight(null);
    if (w <= 0 || h <= 0)
      throw new IllegalArgumentException("Image has non-positive dimension(s)");
    if (w >= 1<<16 || h >= 1<<16)
      throw new IllegalArgumentException("Image is too big");
    // Convert to required format and extract image data.
    final BufferedImage bi =
      new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    final Graphics gfx = bi.getGraphics();
    gfx.drawImage(image, 0, 0, null);
    gfx.dispose();
    final int[] data =
      ((DataBufferInt)(bi.getRaster().getDataBuffer())).getData();
    // Wrap and return.
    return new Picture(varName, w, h, data);
  }
  
  public void render(
    org.sc3d.apt.crazon.vm.state.Window window,
    Sprite[] sprites
  ) throws CrazonException {
    this.cc.clear(
      window.getDouble("R"),
      window.getDouble("G"),
      window.getDouble("B")
    );
    final double wx = window.getDouble("X");
    final double wy = window.getDouble("Y");
    final double ww = window.getDouble("W");
    final double wh = window.getDouble("H");
    if (ww<=0 || wh<=0) throw new CrazonException(
      "WINDOW.W and WINDOW.H must be positive"
    ); // FIXME: allow negative (non-zero) values.
    final double xStretch = this.cc.width/ww, yStretch = this.cc.height/wh;
    for (int i=0; i<sprites.length; i++) {
      final double sx = sprites[i].getDouble("X");
      final double sy = sprites[i].getDouble("Y");
      final double sw = sprites[i].getDouble("W");
      final double sh = sprites[i].getDouble("H");
      if (sw==0 || sh==0) continue;
      if (sw<0 || sh<0) throw new CrazonException(
        "Attributes W and H of a sprite must be positive"
      ); // FIXME: allow negative (non-zero) values.
      final Picture picture = (Picture)sprites[i].get("Picture");
      this.cc.plot(
        picture,
        (sx-wx)*xStretch, (sy-wy)*yStretch,
        sw*xStretch, sh*yStretch
      );
    }
    final long loss = this.cc.doFrame();
    if (loss > 0L) {
      // Commented out because it makes lots of output and the debug window is
      // never cleaned up. Put it back when we have a better debug window.
      // this.println("Lost "+loss+"ms.");
    }
  }

  public void print(String text) {
    this.debug.append(text);
  }
  
  /* Implement things in DeepCopyable. */
  
  /** Throws an UnsupportedOperationException. */
  public DeepCopyable deepCopy() {
    throw new UnsupportedOperationException(
      "Can't copy an org.sc3d.apt.crazon.vm.platform.Platform"
    );
  }
  
  /* Implement things in KeyListener. */
  
  /** Sets the 'keyTable' entry corresponding to 'e' to 'Value.Bool.TRUE'. */
  public void keyPressed(KeyEvent e) {
    final Value.Str keyName =
      (Value.Str)KEY_MAP.get(new Integer(e.getKeyCode()));
    if (keyName!=null) {
      this.keyTable = Table.put(this.keyTable, keyName, Value.Bool.TRUE);
    }
  }
  
  /** Sets the 'keyTable' entry corresponding to 'e' to 'Value.Bool.FALSE'. */
  public void keyReleased(KeyEvent e) {
    final Value.Str keyName =
      (Value.Str)KEY_MAP.get(new Integer(e.getKeyCode()));
    if (keyName!=null) {
      this.keyTable = Table.put(this.keyTable, keyName, Value.Bool.FALSE);
    }
  }
  
  /** Does nothing. */
  public void keyTyped(KeyEvent e) {}
  
  /* Private. */

  /** The text area in 'debugWindow'. */
  private final TextArea debug;
  
  /** A Table with one entry for each key on the keyboard. The keys of the table
   * are the strings defined in class KeyNames. The values are 'Value.Bool.TRUE'
   * for keys that are pressed or 'Value.Bool.FALSE' for keys that are not
   * pressed. */
  private Table keyTable;
  
  /** Maps Integers representing Java key codes (the
   * 'java.awt.event.KeyEvent.VK_XXX' values) to the corresponding Crazon key
   * strings (one of the values defined in KeyNames). */
  private static final HashMap KEY_MAP = new HashMap();
  static {
    for (char c='A'; c<='Z'; c++) {
      installKey(c, KeyNames.LETTER+c);
    }
    for (char c='0'; c<='9'; c++) {
      installKey(c, KeyNames.NUMBER+c);
    }
    // Top row.
    installKey(KeyEvent.VK_ESCAPE, KeyNames.ESCAPE);
    installKey(KeyEvent.VK_PRINTSCREEN, KeyNames.PRINTSCREEN);
    installKey(KeyEvent.VK_SCROLL_LOCK, KeyNames.SCROLL_LOCK);
    installKey(KeyEvent.VK_PAUSE, KeyNames.BREAK);
    // Typewriter first row.
    installKey(KeyEvent.VK_BACK_QUOTE, KeyNames.BACK_QUOTE);
    installKey(KeyEvent.VK_MINUS, KeyNames.MINUS);
    installKey(KeyEvent.VK_EQUALS, KeyNames.EQUALS);
    installKey(KeyEvent.VK_BACK_SPACE, KeyNames.BACK_SPACE);
    // Typewriter second row.
    installKey(KeyEvent.VK_TAB, KeyNames.TAB);
    installKey(KeyEvent.VK_OPEN_BRACKET, KeyNames.OPEN_BRACKET);
    installKey(KeyEvent.VK_CLOSE_BRACKET, KeyNames.CLOSE_BRACKET);
    installKey(KeyEvent.VK_BRACELEFT, KeyNames.LEFT_BRACE);
    installKey(KeyEvent.VK_BRACERIGHT, KeyNames.RIGHT_BRACE);
    installKey(KeyEvent.VK_ENTER, KeyNames.ENTER);
    // Typewriter third row.
    installKey(KeyEvent.VK_CAPS_LOCK, KeyNames.CAPS_LOCK);
    installKey(KeyEvent.VK_SEMICOLON, KeyNames.SEMICOLON);
    installKey(KeyEvent.VK_QUOTE, KeyNames.QUOTE);
    installKey(KeyEvent.VK_NUMBER_SIGN, KeyNames.HASH);
    // Typewriter fourth row.
    installKey(KeyEvent.VK_SHIFT, KeyNames.SHIFT);
    installKey(KeyEvent.VK_BACK_SLASH, KeyNames.BACK_SLASH);
    installKey(KeyEvent.VK_COMMA, KeyNames.COMMA);
    installKey(KeyEvent.VK_PERIOD, KeyNames.FULL_STOP);
    installKey(KeyEvent.VK_SLASH, KeyNames.SLASH);
    // Typewriter fifth row.
    installKey(KeyEvent.VK_CONTROL, KeyNames.CONTROL);
    installKey(KeyEvent.VK_ALT, KeyNames.ALT);
    installKey(KeyEvent.VK_ALT_GRAPH, KeyNames.ALT);
    installKey(KeyEvent.VK_SPACE, KeyNames.SPACE);
    // Navigation keys.
    installKey(KeyEvent.VK_INSERT, KeyNames.INSERT);
    installKey(KeyEvent.VK_HOME, KeyNames.HOME);
    installKey(KeyEvent.VK_PAGE_UP, KeyNames.PAGE_UP);
    installKey(KeyEvent.VK_DELETE, KeyNames.DELETE);
    installKey(KeyEvent.VK_END, KeyNames.END);
    installKey(KeyEvent.VK_PAGE_DOWN, KeyNames.PAGE_DOWN);
    // Arrow keys.
    installKey(KeyEvent.VK_DOWN, KeyNames.DOWN_ARROW);
    installKey(KeyEvent.VK_LEFT, KeyNames.LEFT_ARROW);
    installKey(KeyEvent.VK_RIGHT, KeyNames.RIGHT_ARROW);
    installKey(KeyEvent.VK_UP, KeyNames.UP_ARROW);
  }
  
  /** Used to initialise 'KEY_MAP'. */
  private static void installKey(int vkCode, String keyName) {
    KEY_MAP.put(new Integer(vkCode), new Value.Str(keyName));
  }
}
