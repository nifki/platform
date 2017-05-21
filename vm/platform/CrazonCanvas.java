package org.sc3d.apt.crazon.vm.platform;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

/** Provides a CrazonCanvas of a fixed size, backed by an image. Provides
 * methods to clear the backing image to a constant colour, to plot scaled
 * images on it, and to copy it onto the screen. */
public class CrazonCanvas extends java.awt.Canvas {
  /** Constructs a CrazonCanvas. The backing image is initially black.
   * @param width the width of the CrazonCanvas in pixels.
   * @param height the height of the CrazonCanvas in pixels.
   * @param delay the time for each animation frame in milliseconds.
   */
  public CrazonCanvas(int width, int height, int delay) {
    this.width = width; this.height = height;
    this.delay = delay;
    this.setSize(width, height);
    this.backingImage =
      new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    this.data =
      ((DataBufferInt)this.backingImage.getRaster().getDataBuffer()).getData();
    this.lastFrameTime = System.currentTimeMillis();
  }
  
  /* New API. */
  
  /** The size of this CrazonCanvas in pixels. */
  public final int width, height;
  
  /** The time for each frame in milliseconds. */
  public final int delay;
  
  /** Displays this CrazonCanvas in its own window with title 'title'. The
   * caller must install a WindowListener in order to trap 'windowClosing()'
   * events.
   * @return the java.awt.Frame which represents the window.
   */
  public Frame display(String title) {
    final Frame frame = new Frame(title);
    frame.add(this);
    frame.pack();
    frame.setVisible(true);
    return frame;
  }
  
  /** Clears the backing image to the specified colour. */
  public void clear(double red, double green, double blue) {
    final int colour =
      (Math.min(0xFF, Math.max(0x00, (int)Math.round(255*red))) << 16) |
      (Math.min(0xFF, Math.max(0x00, (int)Math.round(255*green))) << 8) |
      (Math.min(0xFF, Math.max(0x00, (int)Math.round(255*blue))) << 0);
    for (int i=0; i<this.data.length; i++) this.data[i] = colour;
  }
  
  /** Plots the specified picture onto the backing image, scaling it to fit in
   * the specified rectangle. The scaling algorithm is accurate to a tiny
   * fraction of a pixel. The algorithm assumes the picture's width and height
   * are less than '1<<16'.
   * <p>FIXME: cope with negative 'w' and with negative 'h'. */
  public void plot(Picture picture, double x, double y, double w, double h) {
//    System.out.println("x="+x+", y="+y+", w="+w+", h="+h);
    if (x+w<=0.5 || y+h<=0.5 || x>this.width-0.5 || y>this.height-0.5) return;
    // 'cx', 'cy' are the coordinates of a pixel in the canvas.
    // 'cxLeft', 'cyTop' are the coordinates of the topmost, leftmost pixel
    // whose centre is not above and is not to the left of the top-left corner
    // of the part of the picture that overlaps the canvas.
    final int cxLeft = Math.max(0, (int)Math.ceil(x-0.5));
    final int cyTop = Math.max(0, (int)Math.ceil(y-0.5));
//    System.out.println("cxLeft="+cxLeft+", cyTop="+cyTop);
    // 'px', 'py' are coordinates within the picture. They use a fixed-point
    // representation in which the top 16 bits are the pixel coordinates and the
    // bottom 16 bits are the fractional part.
    // 'pxStep', 'pyStep' are the amount by which 'px', 'py' must change for a
    // movement of one canvas pixel.
    final int pxStep = (int)Math.round((picture.width/w)*(1<<16));
    final int pyStep = (int)Math.round((picture.height/h)*(1<<16));
//    System.out.println("pxStep="+pxStep+", pyStep="+pyStep);
    if (pxStep<=0 || pyStep<=0) return; // Overflow or underflow.
    // 'pxLeft', 'pyTop' are the values of 'px', 'py' for the centre of the
    // canvas pixel 'cxLeft', 'cxTop'.
    final int pxLeft = (int)Math.round((cxLeft+0.5-x)*pxStep);
    final int pyTop = (int)Math.round((cyTop+0.5-y)*pyStep);
//    System.out.println("pxLeft="+pxLeft+", pyTop="+pyTop);
    // 'cxRight', 'cyBottom' are the coordinates of the topmost, leftmost canvas
    // pixel that is not above and is not left of the bottom, right corner of
    // the part of the picture that overlaps the canvas.
    // It is important to calculate these from 'pxLeft', 'pxStep', 'pyTop' and
    // 'pyStep' and not from 'x', 'y', 'w' and 'h'. We have chosen an
    // approximation and we must stick with it. Otherwise we might get an
    // ArrayIndexOutOfBoundsException later.
    final int cxRight = Math.min(
      this.width,
      cxLeft + (int)((((long)picture.width<<16)-pxLeft+pxStep-1) / pxStep)
    );
    final int cyBottom = Math.min(
      this.height,
      cyTop + (int)((((long)picture.height<<16)-pyTop+pyStep-1) / pyStep)
    );
//    System.out.println("cxRight="+cxRight+", cyBottom="+cyBottom);
    if (cxLeft>=cxRight || cyTop>=cyBottom) return;
    int py = pyTop;
    // For each row of canvas pixels...
    for (int cy=cyTop; cy<cyBottom; cy++) { // Canvas y-coordinate.
      final int cRow = cy * this.width;
      final int pRow = (py>>>16) * picture.width;
//      System.out.println("cRow="+cRow+", pRow="+pRow);
      int px = pxLeft;
      // For each canvas pixel in the row...
      for (int cx=cxLeft; cx<cxRight; cx++) { // Canvas x-coordinate.
        final int colour = picture.data[pRow + (px>>>16)];
        if (colour < 0) // Top bit set => more than half opaque.
          this.data[cRow + cx] = colour;
        px += pxStep;
      }
      py += pyStep;
    }
  }
  
  /** Copies the backing image onto the screen, then waits until the next frame
   * is due.
   * @return Number of milliseconds by which this frame is late, or zero if the
   * frame is on time. This time is lost forever; future frames will not be
   * shortened to compensate.
   */
  public long doFrame() {
    // The next few lines are a replacement for the call:
    //   this.repaint(0, 0, this.width, this.height);
    // The main difference is that painting happens in this thread, not in the
    // AWT event thread.
    final Graphics g = this.getGraphics();
    if (g!=null) {
      this.paint(g);
      g.dispose();
    }
    // Block until the next frame is due.
    this.lastFrameTime += this.delay;
    long now = System.currentTimeMillis();
    while (now<this.lastFrameTime) {
      try {
        Thread.currentThread().sleep(this.lastFrameTime-now);
      } catch (InterruptedException e) {}
      now = System.currentTimeMillis();
    }
    final long loss = now - (this.lastFrameTime + this.delay);
    if (loss>0L) {
      // We are behind schedule by more than one frame. Revise schedule.
      this.lastFrameTime += loss;
      return loss;
    }
    return 0L; // No loss.
  }
  
  /* Override things in Component. */
  
  /** Override this so that it calls 'paint()' without first clearing the
   * background. */
  public void update(Graphics g) { this.paint(g); }
  
  /** Plots the backing image on the screen. */
  public void paint(Graphics g) {
    g.drawImage(this.backingImage, 0, 0, null);
  }
  
  public boolean isFocusTraversable() { return true; }
  
  /* Private API. */
  
  /** The backing image, onto which everything is drawn. The image is then
   * copied onto the screen. */
  private final BufferedImage backingImage;
  
  /** The raw image data inside 'backingImage'. */
  private final int[] data;
  
  /** The value returned by 'System.currentTimeMillis()' at the moment we
   * intended to be the beginning of the current frame. In practice we may
   * sometimes start the frame a little late. */
  private long lastFrameTime;
  
  /* Test code. */
  
  public static void main(String[] args) {
    // Construct a Picture.
    final int pw = 32, ph = 32;
    final int[] data = new int[pw*ph];
    for (int y=0; y<ph; y++) {
      for (int x=0; x<pw; x++) {
        data[x+pw*y] = ((255*x/pw) << 16) | ((255*y/ph) << 8) | (0xFF << 0);
      }
    }
    final Picture pic = new Picture("Page_Test", pw, ph, data);
    // Construct a CrazonCanvas.
    final int cw = 640, ch = 480;
    final CrazonCanvas cc = new CrazonCanvas(cw, ch, 30);
    cc.display("Testing CrazonCanvas");
    // Run a little animation.
    while (true) {
      for (int i=0; i<10000; i++) {
        cc.clear(0.5, 0.7, 0.3);
        for (int j=0; j<1000; j++) {
          final double a = 2*Math.PI*(i+4*j)/10000;
          final double x = cw/2 + cw*Math.sin(17*a);
          final double y = ch/2 + ch*Math.sin(15*a);
          final double w = 2 * pw * (1+Math.cos(141*a));
          final double h = 2 * ph * (1+Math.cos(147*a));
          cc.plot(pic, x-w/2, y-w/2, w, h);
        }
        cc.doFrame();
      }
    }
  }
}
