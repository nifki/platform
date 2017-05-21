package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

import java.awt.*;
import java.util.*;

/** Implements the "WAIT" instruction. */
public final class Wait extends Instruction.Op {
  public Wait() { super("WAIT", 0, 0); }
  
  /** Loops through the Set of potentially visible Sprites, and removes any for
   * which 'IsVisible' is 'FALSE'. Sorts them into plotting order and passes
   * them to 'AbstractPlatform.render()'. */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    // Filter and sort the Objects.
    final TreeSet toPlot = new TreeSet(SPRITE_CMP);
    for (Iterator it = state.visibleSprites.iterator(); it.hasNext(); ) {
      final Sprite spr = (Sprite)it.next();
      if (spr.getBoolean("IsVisible")) toPlot.add(spr);
      else it.remove();
    }
    final Sprite[] sorted = (Sprite[])toPlot.toArray(new Sprite[toPlot.size()]);
    try {
      state.platform.render(state.window, sorted);
    } catch (CrazonException e) {
      throw new CrazonRuntimeException(e.getMessage(), state);
    }
    // Update the keyboard state.
    state.keys = state.platform.getKeys();
  }
  
  /** A Comparator that defines the plotting order on Sprites. */
  private static final Comparator SPRITE_CMP = new Comparator() {
    public final int compare(Object a, Object b) {
      final Sprite as = (Sprite)a;
      final Sprite bs = (Sprite)b;
      final double aDepth = as.getDouble("Depth");
      final double bDepth = bs.getDouble("Depth");
      if (aDepth < bDepth) return +1;
      if (bDepth < aDepth) return -1;
      if (as.objNum < bs.objNum) return -1;
      if (bs.objNum < as.objNum) return +1;
      if (a == b) return 0;
      throw new RuntimeException("Two sprites have the same objNum");
    }
  };
}
