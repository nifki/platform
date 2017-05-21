package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "%" operation. Takes the remainder on dividing two numbers. */
public final class Mod extends Instruction.Op {
  public Mod() { super("%", 2, 1); }

  /** Pops two numbers 'x' and 'y' ('x' being the bottom-most) and pushes a result equal to 'x - y*FLOOR(x/y)'. */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value y = state.pop();
    final Value x = state.pop();
    try {
      final double yNum = ((Value.Num)y).v;
      final double xNum = ((Value.Num)x).v;
      final double ans = xNum - yNum*Math.floor(xNum/yNum);
      state.push(new Value.Num(ans));
    } catch (ClassCastException e) {
      throw new CrazonRuntimeException(
        "Cannot apply % to "+x+" and "+y+"; two numbers are required",
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
    new Mod().execute(state);
    System.out.println(x+" % "+y+" = "+state.pop());
  }

  public static void main(String[] args) throws CrazonRuntimeException {
    test(new Value.Num(7.0), new Value.Num(3.0));
    test(new Value.Num(3.0), new Value.Num(7.0));
    test(new Value.Num(-7.0), new Value.Num(3.0));
    test(new Value.Num(7.0), new Value.Num(-3.0));
    test(new Value.Num(-7.0), new Value.Num(-3.0));
  }
}
