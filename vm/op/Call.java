package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "CALL" operation, which calls a function. */
public final class Call extends Instruction.Op {
  public Call() { super("CALL", 2, 1); }
  
  /** Pops a function and a table (the function being bottom-most). Constructs
   * a new stack frame for the function, and pushes the table (the arguments).
   * This leaves the InterpreterState ready to execute the function body. */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value args = state.pop();
    final Value f = state.pop();
    try {
      final Value.Tab argsTab = (Value.Tab)args;
      final Value.Func fFunc = (Value.Func)f;
      state.frame = new InterpreterState.Call(
        state.frame,
        fFunc.startPC, fFunc.numLocals, fFunc.stackLen
      );
      state.push(argsTab);
    } catch (ClassCastException e) {
      throw new CrazonRuntimeException(
        "Can't call "+f+" as a function (passing "+args+")",
        state
      );
    }
  }
}
