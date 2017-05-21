package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "ROUND" operation. Rounds a number to the nearest integer. */
public final class Round extends Instruction.Op {
  public Round() { super("ROUND", 1, 1); }

  /** Pops one number value, x, and pushes 'round(x)' (which is equal to
   * 'floor(x+0.5)'). */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value x = state.pop();
    try {
      final double xNum = ((Value.Num)x).v;
      state.push(new Value.Num(Math.round(xNum)));
    } catch (ClassCastException e) {
      throw new CrazonRuntimeException(
        "Cannot apply ROUND to "+x+"; a number is required",
        state
      );
    }
  }

 
  /* Test code. */

  public static void test(Value x) throws CrazonRuntimeException {
    final InterpreterState state = new InterpreterState();
    state.frame = new InterpreterState.Call(state.frame, 0, 0, 2);
    state.push(x);
    new Round().execute(state);
    System.out.println("ROUND "+x+" = "+state.pop());
  }

  public static void main(String[] args) throws CrazonRuntimeException {
    test(new Value.Num(3.1));
    test(new Value.Num(-3.1));
    test(new Value.Num(3.7));
    test(new Value.Num(-3.7));
  }
}
