package org.sc3d.apt.crazon.vm.platform;

import java.awt.*;
import java.awt.image.*;

/** A utility for forcing Images to load. */
public class ImageLoader implements ImageObserver {
  /** Private constructor: no need for the public to construct instances. */
  private ImageLoader() {}
  
  /* New API. */
  
  /** Does not return until 'image' is completely loaded.
   * @return 'true' if loading was successful, otherwise 'false'.
   */
  public static boolean waitUntilLoaded(Image image) {
    final Toolkit toolkit = Toolkit.getDefaultToolkit();
    toolkit.prepareImage(image, -1, -1, ME);
    try { synchronized (ME) { while (true) {
      final int flags = toolkit.checkImage(image, -1, -1, ME);
      if ((flags&ALLBITS)!=0) return true;
      if ((flags&(ABORT|ERROR))!=0) return false;
      ME.wait();
    }}} catch (InterruptedException e) {
      e.printStackTrace();
      return false;
    }
  }
  
  /* Implement things in ImageObserver. */
  
  /** Calls 'this.notifyAll()' whenever it receives the 'ALLBITS' flag. */
  public synchronized boolean imageUpdate(
    Image image,
    int flags,
    int x, int y, int w, int h
  ) {
    if ((flags&(ALLBITS|ABORT|ERROR))==0) return true;
    // The image is completely loaded, or aborted.
    this.notifyAll();
    return false;
  }
  
  /* Private. */
  
  private static final ImageLoader ME = new ImageLoader();
}
