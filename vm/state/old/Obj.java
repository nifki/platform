package org.sc3d.apt.crazon.vm.state;

import java.util.*;

/** A subclass of Value.Obj that represents something that can be displayed on
 * the Screen. This class has attributes to represent a rectangular bounding
 * box, a plotting-order depth, and a boolean that controls whether the Obj
 * is visible or not. Subclasses can add more attributes, and should override
 * the 'plot()' method to define the appearance. */
public abstract class Obj extends Value.Obj {
  /** Constructs a Obj. The following attributes are added to 'attribs':<ul>
   * <li>"X", initially '0', is the x-coordinate of this Obj's top-left
   * corner.
   * <li>"Y", initially '0', is the y-coordinate of this Obj's top-left
   * corner.
   * <li>"W", initially '0', is the width of this Obj.
   * <li>"H", initially '0', is the height of this Obj.
   * <li>"Depth", initially '0', is the plotting-order depth of this Obj.
   * <li>"IsVisble", initially 'FALSE', controls whether this Obj is plotted.
   */
  public Obj(Map attributes) {
    super(attributes);
    attributes.put("X", new Value.Num(0));
    attributes.put("Y", new Value.Num(0));
    attributes.put("W", new Value.Num(0));
    attributes.put("H", new Value.Num(0));
    attributes.put("Depth", new Value.Num(0));
    attributes.put("IsVisible", Value.Bool.FALSE);
  }
  
  /* New API. */
  
  /** Returns the 'IsVisible' attribute of this Obj. */
  public final boolean isVisible() { return this.getBoolean("IsVisible"); }
  
  /** Returns a Pos representing the current position and depth of this Obj
   * relative to 'window'. This method will not be called unless 'isVisible()'.
   * */
  public Pos getPos(Value.Obj window) {
    final double x = window.getDouble("X"), y = window.getDouble("Y");
    final double w = window.getDouble("W"), h = window.getDouble("H");
    return new Pos(
      this.getDouble("Depth"),
      this,
      (this.getDouble("X")-x)/w, (this.getDouble("Y")-y)/h,
      this.getDouble("W")/w, this.getDouble("H")/h
    );
  }
  
  /** Plots this Obj on the Screen of the specified InterpreterState at the
   * specified position and size.
   * The position is specified in a coordinate system in which the
   * top-left corner of the Screen is at '0', '0' and the bottom-right corner is
   * at '1', '1'. This method will not be called unless 'isVisible()'. */
  public abstract void plot(
    InterpreterState state,
    double x, double y, double w, double h
  );
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** Represents the position of this Obj relative to a window. */
  public static class Pos implements Comparable {
    public Pos(
      double depth, org.sc3d.apt.crazon.vm.state.Obj obj,
      double x, double y, double w, double h
    ) {
      this.depth = depth; this.obj = obj;
      this.x = x; this.y = y; this.w = w; this.h = h;
    }
    
    /** The depth of the Obj at the time 'getPos()' was called. */
    public final double depth;
    
    /** The Obj of which this Pos represents the position. */
    public final org.sc3d.apt.crazon.vm.state.Obj obj;
    
    /** The position of the Obj at the time 'getPos()' was called, expressed
     * in the same coordinate system as used by the 'plot()' method. */
    public final double x, y, w, h;
    
    /** Passes the specified InterpreterState and the rectangle represented by
     * this Pos to the Obj's 'plot()' method. */
    public void plot(InterpreterState state) {
      this.obj.plot(state, this.x, this.y, this.w, this.h);
    }
    
    /* Implement things in Comparable. */
    
    /** If 'that' is a Pos, compares their 'depth' fields, and uses 'objNum' to
     * resolve ties. */
    public int compareTo(Object thatObject) {
      final Pos that = (Pos)thatObject;
      if (this.depth<that.depth) return -1;
      if (this.depth>that.depth) return 1;
      return this.obj.objNum - that.obj.objNum;
    }
    
    /* Override things in Object. */
    
    public String toString() {
      return
        "Pos[depth="+this.depth+", obj="+this.obj+
        ", x="+this.x+", y="+this.y+", w="+this.w+", h="+this.h+"]";
    }
  }
}
