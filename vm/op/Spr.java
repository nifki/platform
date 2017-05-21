package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the SPRITE instruction. */
public final class Spr extends Instruction.Op {
  public Spr() { super("SPRITE", 1, 1); }
  
  /** Pops a picture object.
   * Constructs a new Sprite and leaves it on the stack. */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value pic = state.pop();
    try {
      final Sprite ans = new Sprite((Value.Pic)pic);
      state.push(ans);
      state.visibleSprites.add(ans);
    } catch (ClassCastException e) {
      throw new CrazonRuntimeException(
        "Cannot apply SPRITE to "+pic+"; a picture is required",
        state
      );
    }
  }
}
