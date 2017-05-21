package org.sc3d.apt.crazon.vm.platform;

import org.sc3d.apt.crazon.vm.state.*;

import java.awt.image.*;
import java.awt.*;

/** Represents an image with 8-bit RGB components packed into integer pixels.
 * It should be treated as immutable after construction. Image dimensions must
 * be greater than zero and less than '1<<16'. */
public class Picture extends Value.Pic {
  /** Constructs a Picture, given values for its fields. */
  public Picture(String originalName, int width, int height, int[] data) {
    super(originalName, width, height);
    this.data = data;
  }
  
  /** The pixel data in left-to-right, top-to-bottom order. */
  public final int[] data;
}
