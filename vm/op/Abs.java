package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the 'ABS' operation which returns abs(x). */
public final class Abs extends Instruction.Op {
  public Abs() { super("ABS", 1, 1); }

  /** Pops one number value, x, and pushes abs(-x). */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value x = state.pop();
    try {
      final double xNum = ((Value.Num)x).v;
      state.push(new Value.Num(Math.abs(xNum)));
    } catch (ClassCastException e) {
      throw new CrazonRuntimeException(
        "Cannot apply ABS to "+x+"; a number is required",
        state
      );
    }
  }

 
  /* Test code. */

  public static void test(Value x) throws CrazonRuntimeException {
    final InterpreterState state = new InterpreterState();
    state.frame = new InterpreterState.Call(state.frame, 0, 0, 2);
    state.push(x);
    new Abs().execute(state);
    System.out.println("ABS "+x+" = "+state.pop());
  }

  public static void main(String[] args) throws CrazonRuntimeException {
    test(new Value.Num(3.1));
    test(new Value.Num(-3.1));
    test(new Value.Num(3.7));
    test(new Value.Num(-3.7));
  }
}
