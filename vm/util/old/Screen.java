package org.sc3d.apt.crazon.vm.util;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

/** Represents the screen on which everything is displayed. The size of the
 * screen is fixed at construction time, as is its frame-rate. The screen
 * exposes a BufferedImage to write on, and provides a method 'doFrame()' which
 * displays the frame and then blocks until the next frame is due.
 * A Screen also provides a cache of Images which can be retrieved by filename.
 */
public class Screen extends Canvas {
  /** Constructs a Screen. 
   * @param width the width of the Screen in pixels.
   * @param height the height of the Screen in pixels.
   * @param step the time between frames in milliseconds.
   */
  public Screen(int width, int height, int step) {
    this.width = width; this.height = height; this.step = step;
    this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    this.g = this.image.getGraphics();
    this.setSize(width, height);
    this.nextFrame = System.currentTimeMillis();
    this.cache = new HashMap();
  }
  
  /* New API. */
  
  /** The value passed to the constructor. */
  public final int width, height, step;
  
  /** The BufferedImage on which you should draw in order to display things on
   * this Screen. Call 'doFrame()' when the next frame is ready.
   * <p>The type of this BufferedImage is 'BufferedImage.TYPE_INT_RGB'.
   */
  public final BufferedImage image;
  
  /** A Graphics you can use to draw on 'image'. */
  public final Graphics g;
  
  /** Call this method to display each frame. This method blocks until the next
   * frame is due. */
  public void doFrame() {
    // Update the screen.
    final Graphics g = this.getGraphics();
    this.paint(g);
    g.dispose();
    // Block until next frame is due. Note that timer may overflow.
    final long currentTime = System.currentTimeMillis();
    long delay = this.nextFrame + this.step - currentTime;
    if (delay<0L) {
      System.err.println("Lost "+-delay+"ms");
      delay = 0L;
    }
    this.nextFrame = currentTime + delay;
    try { Thread.currentThread().sleep(delay); }
    catch (InterruptedException e) {}
  }
  
  /** Opens a window that displays this Screen.
   * @param title a title for the window.
   * @param isCloseFatal 'true' if closing the window should call
   * 'System.exit(0)'.
   * @return the new window.
   */
  public Frame display(String title, boolean isCloseFatal) {
    final Frame ans = new Frame(title);
    if (isCloseFatal) {
      ans.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) { System.exit(0); }
      });
    }
    ans.add(this);
    ans.pack();
    ans.setVisible(true);
    this.requestFocus();
    return ans;
  }
  
  /** Returns the Image with the specified filename, or 'null' if there is no
   * such Image. Images are cached so the same Image may be returned more than
   * once. Images are loaded fully before being returned. */
  public Image getImage(String filename) {
    if (!this.cache.containsKey(filename)) {
      final Image ans = Toolkit.getDefaultToolkit().createImage(
        Screen.class.getResource("/"+filename)
      );
      if (!ImageLoader.waitUntilLoaded(ans))
        System.err.println("No such image: '"+filename+"'");
      this.cache.put(filename, ans);
    }
    return (Image)this.cache.get(filename);
  }
  
  /* Override things in Canvas. */
  
  public void paint(Graphics g) { g.drawImage(this.image, 0, 0, this); }
  public void update(Graphics g) { this.paint(g); }
  
  /* Private. */
  
  /** The time at which the next frame is due. */
  private long nextFrame;
  
  /** A HashMap from filename (String) to Image, for cacheing images. */
  private final HashMap cache;
  
  /* Test code. */
  
  public static void main(String[] args) {
    final Screen me = new Screen(640, 480, 50);
    me.display("Testing Screen", true);
    final java.util.Random rand = new java.util.Random();
    while (true) {
      me.g.setColor(new Color(rand.nextInt(1<<24)));
      me.g.fillRect(rand.nextInt(481), rand.nextInt(361), 160, 120);
      me.doFrame();
    }
  }
}
