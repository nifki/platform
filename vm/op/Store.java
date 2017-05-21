package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the STORE instruction. */
public final class Store extends Instruction.Op {
  /** @param index the global variable to store. */
  public Store(int index, String name) {
    super("STORE("+name+")", 1, 0);
    this.index = index;
  }

  /** Index of the global variable to store. */
  public final int index;

  /** STORE: Pops a Value from the stack and writes it to the global
   * variable with index 'index'.
   */
  public final void execute(InterpreterState state) {
    state.putGlobal(this.index, state.pop());
  }
}
