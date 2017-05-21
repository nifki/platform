package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the KEYS instruction, which places a table representing the
 * current state of the keyboard on the stack. */
public final class Keys extends Instruction.Op {
  public Keys() { super("KEYS", 0, 1); }
  
  /** Leaves 'state.keys' on the stack. */
  public final void execute(InterpreterState state) {
    state.push(state.keys);
  }
}
