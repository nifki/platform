package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements table assignment. */
public final class Put extends Instruction.Op {
  public Put() { super("PUT", 3, 1); }

  /** Pops a value, a key and a table. Modifies the table so that it maps the key to the value. Pushes the resulting table. */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException  {
    final Value v = state.pop();
    final Value k = state.pop();
    final Value t = state.pop();
    if (k.type>5) throw new CrazonRuntimeException(
      "'"+k+"' cannot be used as key in a table",
      state
    );
    Table table;
    try {
      table = ((Value.Tab)t).v;
    } catch (ClassCastException e) {
      throw new CrazonRuntimeException("'"+t+"' is not a table", state);
    }
    final Table newTable = Table.put(table, k, v);
    state.push(new Value.Tab(newTable));
  }

  /* Test code. */

  public static void test(Value t, Value k, Value v)
  throws CrazonRuntimeException {
    final InterpreterState state = new InterpreterState();
    state.frame = new InterpreterState.Call(state.frame, 0, 0, 2);
    state.push(t);
    state.push(k);
    state.push(v);
    new Put().execute(state);
    System.out.println(t+"["+k+"]="+v+" leaves "+state.pop());
  }

  public static void main(String[] args)
  throws CrazonRuntimeException {
    test(new Value.Tab(null), new Value.Tab(null), new Value.Tab(null));
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
    test(new Value.Tab(x), new Value.Tab(y), new Value.Str("horse"));
    test(new Value.Tab(x), new Value.Str("horse"), new Value.Tab(y));
  }
}
