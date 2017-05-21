package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "+" operation. Adds numbers, concatenates strings, or overlays tables. */
public final class Add extends Instruction.Op {
  public Add() { super("+", 2, 1); }

  /** Pops two values and pushes a result. The calculation depends on the types of the values popped:<ul>
   * <li>If both operands are numbers, the result is their sum.
   * <li>If both operands are strings, the result is their concatenation, with the bottom-most string on the left.
   * <li>If both operands are tables, the result is calculated by applying all assignments in the top-most table to the bottom-most table.
   * </ul> */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value y = state.pop();
    final Value x = state.pop();
    switch (x.type + (y.type<<3)) {
      case Value.TYPE_NUM + (Value.TYPE_NUM<<3): {
        final double xNum = ((Value.Num)x).v;
        final double yNum = ((Value.Num)y).v;
        final double ans = xNum + yNum;
        state.push(new Value.Num(ans));
        return;
      }
      case Value.TYPE_STR + (Value.TYPE_STR<<3): {
        final String xStr = ((Value.Str)x).v;
        final String yStr = ((Value.Str)y).v;
        final String ans = xStr + yStr;
        state.push(new Value.Str(ans));
        return;
      }
      case Value.TYPE_TAB + (Value.TYPE_TAB<<3): {
        Table xTab = ((Value.Tab)x).v;
        final Table yTab = ((Value.Tab)y).v;
        for (Table.Iterator it = Table.iterator(yTab); it!=null; it=it.next()) {
          xTab = Table.put(xTab, it.key, it.value);
        }
        state.push(new Value.Tab(xTab));
        return;
      }
      default: throw new CrazonRuntimeException(
        "Cannot add "+x+" to "+y,
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
    new Add().execute(state);
    System.out.println(x+" + "+y+" = "+state.pop());
  }

  public static void main(String[] args) throws CrazonRuntimeException {
    test(new Value.Num(1.0), new Value.Num(2.0));
    test(new Value.Str("1.0"), new Value.Str("2.0"));
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
