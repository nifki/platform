package org.sc3d.apt.crazon.vm.state;

import java.util.*;

/** Represents the "WINDOW" object, which represents the part of the plane
 * visible in the game window. It has the following attributes:<ul>
 * <li>X - the x-coordinate of the top-left corner of the rectangle (a
 * Value.Num).
 * <li>Y - the y-coordinate of the top-left corner of the rectangle (a
 * Value.Num).
 * <li>W - the width of the rectangle (a Value.Num).
 * <li>H - the height of the rectangle (a Value.Num).
 * <li>R - the red component of the background colour (a Value.Num).
 * <li>G - the green component of the background colour (a Value.Num).
 * <li>B - the blue component of the background colour (a Value.Num).
 * <li>IsVisible - Ignored (a Value.Bool).
 * </ul> */
public class Window extends Value.Obj {
  /** Constructs a Window given values for its 'W' and 'H' attributes. */
  public Window(int width, int height) {
    super(makeMap(width, height));
  }
  
  private Window(Window that) { super(that); }
  
  /* Private. */
  
  /** Constructs a HashMap to represent the attributes of the WINDOW object. */
  private static HashMap makeMap(int width, int height) {
    final HashMap ans = new HashMap();
    ans.put("X", new Value.Num(0.0));
    ans.put("Y", new Value.Num(0.0));
    ans.put("W", new Value.Num(width));
    ans.put("H", new Value.Num(height));
    ans.put("R", new Value.Num(0.0));
    ans.put("G", new Value.Num(0.0));
    ans.put("B", new Value.Num(0.0));
    ans.put("IsVisible", Value.Bool.TRUE);
    return ans;
  }

  public String getObjType() { return "WINDOW"; }
  
  public Value deepCopyValue() { return new Window(this); }
}
