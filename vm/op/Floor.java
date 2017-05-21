package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the floor operation. */
public final class Floor extends Instruction.Op {
  public Floor() { super("FLOOR", 1, 1); }

  /** Pops one value and pushes the result. Returns the largest
   * (closest to positive infinity) number value that is not
   * greater than the argument and is equal to a mathematical integer.
   */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value x = state.pop();
    try {
      final double xNum = ((Value.Num)x).v;
      state.push(new Value.Num(Math.floor(xNum)));
    } catch (ClassCastException e) {
      throw new CrazonRuntimeException(
        "Cannot apply FLOOR to "+x+"; a number is required",
        state
      );
    }
  }

  /* Test code. */

  public static void test(Value x) throws CrazonRuntimeException {
    final InterpreterState state = new InterpreterState();
    state.frame = new InterpreterState.Call(state.frame, 0, 0, 2);
    state.push(x);
    new Floor().execute(state);
    System.out.println("FLOOR "+x+" = "+state.pop());
  }

  public static void main(String[] args) throws CrazonRuntimeException {
    test(new Value.Num(3.1));
    test(new Value.Num(-3.1));
  }
}
