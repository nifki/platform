package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the logical not operation on booleans. */
public final class Not extends Instruction.Op {
  public Not() { super("NOT", 1, 1); }

  /** Pops one value and pushes the result. Returns !x. */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value x = state.pop();
    try {
      final Value.Bool b = ((Value.Bool)x);
      if (b == Value.Bool.TRUE)
        state.push(Value.Bool.FALSE);
      else
        state.push(Value.Bool.TRUE);
    } catch (ClassCastException e) {
      throw new CrazonRuntimeException(
        "Cannot apply NOT to "+x+"; a boolean is required",
        state
      );
    }
  }

 
  /* Test code. */

  public static void test(Value x) throws CrazonRuntimeException {
    final InterpreterState state = new InterpreterState();
    state.frame = new InterpreterState.Call(state.frame, 0, 0, 2);
    state.push(x);
    new Not().execute(state);
    System.out.println("NOT "+x+" = "+state.pop());
  }

  public static void main(String[] args) throws CrazonRuntimeException {
    test(Value.Bool.TRUE);
    test(Value.Bool.FALSE);
  }
}
