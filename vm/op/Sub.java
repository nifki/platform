package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "-" operation. Subtracts numbers, shortens strings. */
public final class Sub extends Instruction.Op {
  public Sub() { super("-", 2, 1); }

  /** Pops two values and pushes a result. The calculation depends on the types of the values popped:<ul>
   * <li>If both operands are numbers, the result is the bottom-most minus the top-most.
   * <li>If the bottom-most is a string and the top-most an integer, the result is formed by removing the specified number of characters from the right-hand end of the string.
   * <li>If the top-most is a string and the bottom-most an integer, the result is formed by removing the specified number of characters from the left-hand end of the string.
   * </ul> */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value y = state.pop();
    final Value x = state.pop();
    switch (x.type + (y.type<<3)) {
      case Value.TYPE_NUM + (Value.TYPE_NUM<<3): {
        final double xNum = ((Value.Num)x).v;
        final double yNum = ((Value.Num)y).v;
        final double ans = xNum - yNum;
        state.push(new Value.Num(ans));
        return;
      }
      case Value.TYPE_NUM + (Value.TYPE_STR<<3): {
        final int xNum = ((Value.Num)x).intValue();
        final String yStr = ((Value.Str)y).v;
        if (xNum<0 || xNum>yStr.length()) throw new CrazonRuntimeException(
          "Cannot remove "+x+" characters from "+y+": index out of range",
          state
        );
        final String ans = yStr.substring(xNum);
        state.push(new Value.Str(ans));
        return;
      }
      case Value.TYPE_STR + (Value.TYPE_NUM<<3): {
        final String xStr = ((Value.Str)x).v;
        final int yNum = ((Value.Num)y).intValue();
        if (yNum<0 || yNum>xStr.length()) throw new CrazonRuntimeException(
          "Cannot remove "+y+" characters from "+x+": index out of range",
          state
        );
        final String ans = xStr.substring(0, xStr.length()-yNum);
        state.push(new Value.Str(ans));
        return;
      }
      case Value.TYPE_TAB + (Value.TYPE_TAB<<3): {
        final Table xTab = ((Value.Tab)x).v;
        final Table yTab = ((Value.Tab)y).v;
        Table ans = null;
        for (Table.Iterator it = Table.iterator(xTab); it!=null; it=it.next()) {
          if (Table.get(yTab, it.key)==null)
            ans = Table.put(ans, it.key, it.value);
        }
        state.push(new Value.Tab(ans));
        return;
      }
      default: throw new CrazonRuntimeException(
        "Cannot subtract "+y+" from "+x,
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
    new Sub().execute(state);
    System.out.println(x+" - "+y+" = "+state.pop());
  }

  public static void main(String[] args) throws CrazonRuntimeException {
    test(new Value.Num(1.0), new Value.Num(2.0));
    test(new Value.Str("Hello"), new Value.Num(2.0));
    test(new Value.Num(2.0), new Value.Str("Hello"));
    test(new Value.Tab(null), new Value.Tab(null));
    final Table x =
      Table.put(
        Table.put(
          null,
          new Value.Num(0.0),
          new Value.Str("frog")
        ),
        new Value.Num(1.0),
        new Value.Str("goose")
      );
    final Table y =
      Table.put(
        Table.put(
          null,
          new Value.Num(1.0),
          new Value.Str("sheep")
        ),
        new Value.Num(2.0),
        new Value.Str("pig")
      );
    test(new Value.Tab(x), new Value.Tab(y));
  }
}
