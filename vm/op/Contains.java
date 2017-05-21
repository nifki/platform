package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "CONTAINS" operation. Returns "TRUE" iff "GET" would
 * succeed. */
public final class Contains extends Instruction.Op {
  public Contains() { super("CONTAINS", 2, 1); }
  
  /** Pops two values and pushes a result. The calculation depends on the types
   * of the values popped:<ul>
   * <li>If the first (bottom-most) value is a string, tests wether the second
   * is a non-negative integer smaller than the length of the string.
   * <li>If the first value is a table, tests whether the second is one of the
   * keys of the table.
   * <li>If the first value is an object, tests whether the second is one of its
   * attribute names.
   * </ul> */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value k = state.pop();
    final Value t = state.pop();
    boolean v;
    try {
      switch (t.type) {
        case Value.TYPE_STR: {
          final String tStr = ((Value.Str)t).v;
          final double kNum = ((Value.Num)k).v;
          final int index = (int)kNum;
          v = kNum==index && index>=0 && index<tStr.length();
          break;
        }
        case Value.TYPE_TAB: {
          final Table tTab = ((Value.Tab)t).v;
          v = (Value)Table.get(tTab, k) != null;
          break;
        }
        case Value.TYPE_OBJ: {
          final Value.Obj tObj = (Value.Obj)t;
          final String kStr = ((Value.Str)k).v;
          v = tObj.get(kStr) != null;
          break;
        }
        default: throw new CrazonRuntimeException(
          "Type error: cannot subscript "+t,
          state
        );
      }
    } catch (ClassCastException e) {
      v = false;
    }
    state.push(v ? Value.Bool.TRUE : Value.Bool.FALSE);
  }
  
  /* Test code. */

  public static void test(Value x, Value y) throws CrazonRuntimeException {
    final InterpreterState state = new InterpreterState();
    state.frame = new InterpreterState.Call(state.frame, 0, 0, 2);
    state.push(x);
    state.push(y);
    new Contains().execute(state);
    System.out.println(x+" CONTAINS "+y+" = "+state.pop());
  }
  
  public static void main(String[] args) throws CrazonRuntimeException {
    test(new Value.Str("Hello"), new Value.Num(2.0));
    test(new Value.Str("Hello"), new Value.Num(-1.0));
    test(new Value.Str("Hello"), new Value.Num(5.0));
    test(new Value.Str("Hello"), new Value.Tab(null));
    final Table x =
      Table.put(
        Table.put(
          null,
          new Value.Num(0.0),
          new Value.Str("frog")
        ),
        new Value.Num(1.0),
        new Value.Str("pig")
      );
    test(new Value.Tab(x), new Value.Num(0.0));
    test(new Value.Tab(x), new Value.Tab(x));
    test(new org.sc3d.apt.crazon.vm.state.Window(1, 1), new Value.Str("X"));
    test(new org.sc3d.apt.crazon.vm.state.Window(1, 1), new Value.Str("Z"));
    test(new org.sc3d.apt.crazon.vm.state.Window(1, 1), Value.Bool.TRUE);
  }
}
