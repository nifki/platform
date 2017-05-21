package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the LLOAD instruction. */
public final class LLoad extends Instruction.Op {
  /** @param index the local variable to load. */
  public LLoad(int index, String name) {
    super("LLOAD("+name+")", 0, 1);
    this.index = index;
  }

  /** Index of the local variable to load. */
  public final int index;

  /** LLOAD: Reads the value of the local variable with index 'index' and
   * leaves it on the stack.
   */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    state.push(state.getLocal(this.index));
  }
}
