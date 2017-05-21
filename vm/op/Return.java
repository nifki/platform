package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "RETURN" operation, which returns from a function. */
public final class Return extends Instruction.Op {
  public Return() { super("RETURN", 1, 0); }
  
  /** Pops a return value. Destroys a call frame. Pushes the return value. */
  public final void execute(InterpreterState state) {
    final Value result = state.pop();
    state.frame = state.frame.caller;
    state.push(result);
  }
}
