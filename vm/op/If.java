package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "IF" operation. */
public final class If extends Instruction {
  public If(int targetPC) {
    super(1, 0);
    this.targetPC = targetPC;
  }
  
  /** The value to which to set the program counter when the condition is 'FALSE'. */
  public final int targetPC;
  
  /** Pops a boolean. If 'false', sets the program counter to 'targetPC'. */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value cond = state.pop();
    try {
      if (Value.Bool.FALSE==(Value.Bool)cond) state.frame.pc = this.targetPC;
    } catch (ClassCastException e) {
      throw new CrazonRuntimeException(
        "IF requires a boolean, not '"+cond+"'",
        state
      );
    }
  }
  
  /** Returns a String of the form "IF(n)" where "n" is 'targetPC'. */
  public final String toString() { return "IF("+this.targetPC+")"; }
}
