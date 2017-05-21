package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "MIN" operation in terms of 'compareTo()'. */
public final class Min extends Instruction.Op {
  public Min() { super("MIN", 2, 1); }

  /** Pops two values and pushes a boolean result. */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    try {
      final Value y = state.pop();
      final Value x = state.pop();
      final Value ans = x.compareTo(y)<0 ? x : y;
      state.push(ans);
    } catch (ClassCastException e) {
      throw new CrazonRuntimeException(e.getMessage(), state);
    }
  }

  /* Test code. */

  public static void test(Value x, Value y) throws CrazonRuntimeException {
    final InterpreterState state = new InterpreterState();
    state.frame = new InterpreterState.Call(state.frame, 0, 0, 2);
    state.push(x);
    state.push(y);
    new Min().execute(state);
    System.out.println(x+" MIN "+y+" = "+state.pop());
  }

  public static void main(String[] args) throws CrazonRuntimeException {
    test(new Value.Num(1.0), new Value.Num(2.0));
    test(new Value.Num(1.0), new Value.Num(1.0));
    test(new Value.Num(2.0), new Value.Num(1.0));
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
        new Value.Str("pig")
      );
    final Table y =
      Table.put(
        Table.put(
          null,
          new Value.Num(0.0),
          new Value.Str("sheep")
        ),
        new Value.Num(2.0),
        new Value.Str("goose")
      );
    test(new Value.Tab(x), new Value.Tab(y));
  }
}
