package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the 'CEIL' operation which rounds up (towards +infinity). */
public final class Ceil extends Instruction.Op {
  public Ceil() { super("CEIL", 1, 1); }

  /** Pops one number value, x, and pushes -floor(-x). */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value x = state.pop();
    try {
      final double xNum = ((Value.Num)x).v;
      state.push(new Value.Num(-Math.floor(-xNum)));
    } catch (ClassCastException e) {
      throw new CrazonRuntimeException(
        "Cannot apply CEIL to "+x+"; a number is required",
        state
      );
    }
  }
 
  /* Test code. */

  public static void test(Value x) throws CrazonRuntimeException {
    final InterpreterState state = new InterpreterState();
    state.frame = new InterpreterState.Call(state.frame, 0, 0, 2);
    state.push(x);
    new Ceil().execute(state);
    System.out.println("CEIL "+x+" = "+state.pop());
  }

  public static void main(String[] args) throws CrazonRuntimeException {
    test(new Value.Num(3.1));
    test(new Value.Num(-3.1));
    test(new Value.Num(3.7));
    test(new Value.Num(-3.7));
  }
}
