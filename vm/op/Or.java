package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "OR" operation. */
public final class Or extends Instruction.Op {
  public Or() { super("OR", 2, 1); }

  /** Pops two booleans 'x' and 'y' and pushes a their logical OR. */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value y = state.pop();
    final Value x = state.pop();
    try {
      final boolean yBool = ((Value.Bool)y)==Value.Bool.TRUE;
      final boolean xBool = ((Value.Bool)x)==Value.Bool.TRUE;
      final boolean ans = xBool | yBool;
      state.push(ans ? Value.Bool.TRUE : Value.Bool.FALSE);
    } catch (ClassCastException e) {
      throw new CrazonRuntimeException(
        "Cannot apply OR to "+x+" and "+y+"; two booleans are required",
        state
      );
    }
  }

  /* Test code. */

  public static void test(Value x, Value y) throws CrazonRuntimeException {
    final InterpreterState state = new InterpreterState();
    state.frame = new InterpreterState.Call(state.frame, 0, 0, 2);
    state.push(x);
    state.push(y);
    new Or().execute(state);
    System.out.println(x+" OR "+y+" = "+state.pop());
  }

  public static void main(String[] args) throws CrazonRuntimeException {
    test(Value.Bool.FALSE, Value.Bool.FALSE);
    test(Value.Bool.TRUE, Value.Bool.FALSE);
    test(Value.Bool.FALSE, Value.Bool.TRUE);
    test(Value.Bool.TRUE, Value.Bool.TRUE);
  }
}
