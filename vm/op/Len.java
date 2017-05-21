package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "LEN" operation. Counts the characters in a string or the
 * keys of a table. */
public final class Len extends Instruction.Op {
  public Len() { super("LEN", 1, 1); }

  /** Pops one value and pushes the result. The calculation depends on the types
   * of the value popped:<ul>
   * <li>If the operands is a string, the result is the number of characters in
   * the string.
   * <li>If the operand is a table, the result is the number of keys in the
   * table.
   * </ul> */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value x = state.pop();
    switch (x.type) {
      case Value.TYPE_STR: {
        final String xStr = ((Value.Str)x).v;
        state.push(new Value.Num(xStr.length()));
        return;
      }
      case Value.TYPE_TAB: {
        final Table xTab = ((Value.Tab)x).v;
        state.push(new Value.Num(Table.size(xTab)));
        return;
      }
      default: throw new CrazonRuntimeException(
        "Cannot apply LEN to "+x,
        state
      );
    }
  }

  /* Test code. */

  public static void test(Value x) throws CrazonRuntimeException {
    final InterpreterState state = new InterpreterState();
    state.frame = new InterpreterState.Call(state.frame, 0, 0, 2);
    state.push(x);
    new Len().execute(state);
    System.out.println("LEN "+x+" = "+state.pop());
  }

  public static void main(String[] args) throws CrazonRuntimeException {
    test(new Value.Str("To win the game, you must kill me, John Romero."));
    test(new Value.Tab(null));
    test(new Value.Tab(Table.put(null, Value.Bool.TRUE, Value.Bool.TRUE)));
  }
}
