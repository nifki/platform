package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "*" operation. Multiplies numbers, replicates strings. */
public final class Mul extends Instruction.Op {
  public Mul() { super("*", 2, 1); }

  /** Pops two values and pushes a result. The calculation depends on the types of the values popped:<ul>
   * <li>If both operands are numbers, the result is their product.
   * <li>If one operand is a string and the other is an integer (either way around), the result is formed by concatenating the specified number of copies of the string.
   * </ul> */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException  {
    final Value y = state.pop();
    final Value x = state.pop();
    switch (x.type + (y.type<<3)) {
      case Value.TYPE_NUM + (Value.TYPE_NUM<<3): {
        final double xNum = ((Value.Num)x).v;
        final double yNum = ((Value.Num)y).v;
        final double ans = xNum * yNum;
        state.push(new Value.Num(ans));
        return;
      }
      case Value.TYPE_NUM + (Value.TYPE_STR<<3): {
        final int xNum = ((Value.Num)x).intValue();
        final String yStr = ((Value.Str)y).v;
        if (xNum<0) throw new CrazonRuntimeException(
          "Cannot concatenate "+x+" copies of "+y,
          state
        );
        final StringBuffer ans = new StringBuffer();
        for (int i=0; i<xNum; i++) ans.append(yStr);
        state.push(new Value.Str(ans.toString()));
        return;
      }
      case Value.TYPE_STR + (Value.TYPE_NUM<<3): {
        final String xStr = ((Value.Str)x).v;
        final int yNum = ((Value.Num)y).intValue();
        if (yNum<0) throw new CrazonRuntimeException(
          "Cannot concatenate "+y+" copies of "+x,
          state
        );
        final StringBuffer ans = new StringBuffer();
        for (int i=0; i<yNum; i++) ans.append(xStr);
        state.push(new Value.Str(ans.toString()));
        return;
      }
      default: throw new CrazonRuntimeException(
        "Cannot multiply "+x+" by "+y,
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
    new Mul().execute(state);
    System.out.println(x+" * "+y+" = "+state.pop());
  }

  public static void main(String[] args) throws CrazonRuntimeException {
    test(new Value.Num(2.0), new Value.Num(3.0));
    test(new Value.Str("Hello"), new Value.Num(2.0));
    test(new Value.Num(2.0), new Value.Str("Hello"));
  }
}
