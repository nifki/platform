package org.sc3d.apt.crazon.vm.state;

import java.awt.*;
import java.util.*;

/** A subclass of Obj that plots an Image. */
public class Sprite extends Obj {
  /** Constructs an Sprite.
   * @param attributes a Map of atributes.
   * @param picture an initial value for the "Picture" attribute, which
   * represents the filename of the picture to use.
   */
  public Sprite(HashMap attributes, String picture) {
    super(attributes);
    attributes.put("Picture", new Value.Str(picture));
  }
  
  /** Constructs an Sprite with the minimal attributes. */
  public Sprite(String picture) { this(new HashMap(), picture); }
  
  /* Override things in Obj. */
  
  public void plot(
    InterpreterState state,
    double x, double y, double w, double h
  ) {
    final Image image = state.screen.getImage(this.getString("Picture"));
    if (image!=null) {
      state.screen.g.drawImage(
        image,
        (int)Math.round(x*state.screen.width),
        (int)Math.round(y*state.screen.height),
        (int)Math.round(w*state.screen.width),
        (int)Math.round(h*state.screen.height),
        null
      );
    }
  }
  
  /* Override things in Value. */
  
  public String getObjType() { return "SPRITE"; }
}
