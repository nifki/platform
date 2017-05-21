package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "**" operation. Raises one number to the power of another. */
public final class Pow extends Instruction.Op {
  public Pow() { super("**", 2, 1); }

  /** Pops two values and pushes a result. The calculation depends on the types of the values popped:<ul>
   * <li>If both operands are numbers, the result is their product.
   * <li>If one operand is a string and the other is an integer (either way around), the result is formed by concatenating the specified number of copies of the string.
   * </ul> */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value y = state.pop();
    final Value x = state.pop();
    try {
      final double yNum = ((Value.Num)y).v;
      final double xNum = ((Value.Num)x).v;
      final double ans = Math.pow(xNum, yNum);
      state.push(new Value.Num(ans));
    } catch (ClassCastException e) {
      throw new CrazonRuntimeException(
        "Cannot apply ** to "+x+" and "+y+"; two numbers are required",
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
    new Pow().execute(state);
    System.out.println(x+" ** "+y+" = "+state.pop());
  }

  public static void main(String[] args) throws CrazonRuntimeException {
    test(new Value.Num(2.0), new Value.Num(3.0));
    test(new Value.Num(3.0), new Value.Num(2.0));
    test(new Value.Num(2.0), new Value.Num(-0.5));
    test(new Value.Num(-0.5), new Value.Num(2.0));
  }
}
