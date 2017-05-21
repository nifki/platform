package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "WHILE" operation. */
public final class While extends Instruction.Op {
  public While() { super("WHILE", 1, 0); }
  
  /** Pops a boolean. If 'false', exits the enclosing loop and sets the program counter to its 'elsePC'. */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value cond = state.pop();
    try {
      if (Value.Bool.FALSE==(Value.Bool)cond) {
        state.frame.pc = state.frame.loop.instruction.elsePC;
        state.frame.loop = state.frame.loop.enclosing;
      }
    } catch (ClassCastException e) {
      throw new CrazonRuntimeException(
        "Cannot execute WHILE "+cond+"; a boolean is required",
        state
      );
    }
  }
}
