package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the LOAD instruction. */
public final class Load extends Instruction.Op {
  /** @param index the global variable to load. */
  public Load(int index, String name) {
    super("LOAD("+name+")", 0, 1);
    this.index = index;
  }

  /** Index of the global variable to load. */
  public final int index;

  /** LOAD: Reads the value of the global variable with index 'index' and
   * leaves it on the stack.
   */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    state.push(state.getGlobal(this.index));
  }
}
