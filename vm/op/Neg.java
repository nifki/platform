package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the unary "-" operation. Negates numbers or reverses strings.
 * This instruction can't be called "-" because it would clash with subtraction.
 * */
public final class Neg extends Instruction.Op {
  public Neg() { super("NEG", 1, 1); }

  /** Pops one value and pushes the result. The calculation depends on the types
   * of the value popped:<ul>
   * <li>If the operands is a number, n, the result is its negation, 0 - n.
   * <li>If the operand is a string, the result is its reverse.
   * </ul> */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value x = state.pop();
    switch (x.type) {
      case Value.TYPE_NUM: {
        final double xNum = ((Value.Num)x).v;
        state.push(new Value.Num(-xNum));
        return;
      }
      case Value.TYPE_STR: {
        final String xStr = ((Value.Str)x).v;
        state.push(new Value.Str(reverse(xStr)));
        return;
      }
      default: throw new CrazonRuntimeException("Cannot negate "+x, state);
    }
  }

  private static String reverse(String s) {
    int len = s.length();
    StringBuffer dest = new StringBuffer(len);

    for (int i = len-1; i >= 0; --i) {
      dest.append(s.charAt(i));
    }
    return dest.toString();
  }
    
  /* Test code. */

  public static void test(Value x) throws CrazonRuntimeException {
    final InterpreterState state = new InterpreterState();
    state.frame = new InterpreterState.Call(state.frame, 0, 0, 2);
    state.push(x);
    new Neg().execute(state);
    System.out.println("-"+x+" = "+state.pop());
  }

  public static void main(String[] args) throws CrazonRuntimeException {
    test(new Value.Num(3.1));
    test(new Value.Str("To win the game, you must kill me, John Romero."));
    boolean ok = false;
    try {
      test(new Value.Tab(null)); // should throw.
    } catch (ClassCastException e) {
      ok = true;
      System.out.println("Caught expected exception: "+e);
    }
    if (!ok) System.out.println("FAILED.");
  }
}
