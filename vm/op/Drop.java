package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "DROP" instruction which discards the top element of the
 * stack. */
public final class Drop extends Instruction.Op {
  public Drop() { super("DROP", 1, 0); }

  /** Pops one value. */
  public final void execute(InterpreterState state) {
    state.pop();
  }
}
