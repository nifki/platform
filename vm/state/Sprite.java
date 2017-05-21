package org.sc3d.apt.crazon.vm.state;

import java.util.*;

/** A Sprite is a Value.Obj that represents a rectangle containing a Value.Pic.
 * It has the following attributes:<ul>
 * <li>X - the x-coordinate of the top-left corner of the rectangle (a
 * Value.Num).
 * <li>Y - the y-coordinate of the top-left corner of the rectangle (a
 * Value.Num).
 * <li>W - the width of the rectangle (a Value.Num).
 * <li>H - the height of the rectangle (a Value.Num).
 * <li>Depth - the plotting depth (a Value.Num). A Sprite with a smaller depth
 * can obscure one with a larger.
 * <li>IsVisible - a flag indicating whether this Sprite should be plotted (a
 * Value.Bool).
 * <li>Picture - the Value.Pic that defines the appearance of this Sprite.
 * </ul> */
public final class Sprite extends Value.Obj {
  public Sprite(Value.Pic picture) {
    super(makeMap(picture));
  }
  
  private Sprite(Sprite that) { super(that); }
  
  /* Private. */
  
  /** Constructs a HashMap suitable for passing to the superclass constructor.
   */
  private static HashMap makeMap(Value.Pic picture) {
    final HashMap ans = new HashMap();
    ans.put("X", new Value.Num(0.0));
    ans.put("Y", new Value.Num(0.0));
    ans.put("W", new Value.Num(picture.width));
    ans.put("H", new Value.Num(picture.height));
    ans.put("Depth", new Value.Num(0.0));
    ans.put("IsVisible", Value.Bool.FALSE);
    ans.put("Picture", picture);
    return ans;
  }

  public String getObjType() { return "SPRITE"; }
  
  public Value deepCopyValue() { return new Sprite(this); }
}
