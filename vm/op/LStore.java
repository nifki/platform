package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the LSTORE instruction. */
public final class LStore extends Instruction.Op {
  /** @param index the local variable to store. */
  public LStore(int index, String name) {
    super("LSTORE("+name+")", 1, 0);
    this.index = index;
  }

  /** Index of the local variable to store. */
  public final int index;

  /** LSTORE: Pops a Value from the stack and writes it to the local
   * variable with index 'index'.
   */
  public final void execute(InterpreterState state) {
    state.putLocal(this.index, state.pop());
  }
}
