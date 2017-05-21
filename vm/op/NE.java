package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "!=" operation in terms of 'equals()'. */
public final class NE extends Instruction.Op {
  public NE() { super("!=", 2, 1); }

  /** Pops two values and pushes a boolean result. */
  public final void execute(InterpreterState state) {
    final Value y = state.pop();
    final Value x = state.pop();
    final boolean ans = !x.equals(y);
    state.push(ans ? Value.Bool.TRUE : Value.Bool.FALSE);
  }

  /* Test code. */

  public static void test(Value x, Value y) {
    final InterpreterState state = new InterpreterState();
    state.frame = new InterpreterState.Call(state.frame, 0, 0, 2);
    state.push(x);
    state.push(y);
    new NE().execute(state);
    System.out.println(x+" != "+y+" = "+state.pop());
  }

  public static void main(String[] args) {
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
