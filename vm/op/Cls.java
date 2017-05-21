package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

import java.util.*;

/** Implements the "CLS" instruction. */
public final class Cls extends Instruction.Op {
  public Cls() { super("CLS", 0, 0); }
  
  /** Loops through the Sprites in 'state.visibleSprites' and for each one sets
   * its 'IsVisible' attribute to 'FALSE'. */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    for (Iterator it = state.visibleSprites.iterator(); it.hasNext(); ) {
      final Sprite spr = (Sprite)it.next();
      try {
        spr.set("IsVisible", Value.Bool.FALSE);
      } catch (CrazonException e) {
        throw new CrazonRuntimeException(e.getMessage(), state);
      }
    }
  }
}
