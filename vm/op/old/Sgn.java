package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the 'SGN' operation which returns -1, 0 or 1. */
public final class Sgn extends Instruction.Op {
  public Sgn() { super("SGN", 1, 1); }

  /** Pops one number value, x, and pushes -1, 0 or 1,
   * if x is less than, equal to or greater than zero
   * respectively.
   */
  public final void execute(InterpreterState state) {
    final Value x = state.pop();
    switch (x.type) {
      case Value.TYPE_NUM: {
        final double xNum = ((Value.Num)x).v;
        if (xNum < 0)
          state.push(new Value.Num(-1)); // should we use a singleton?
        else if (xNum > 0)
          state.push(new Value.Num(+1));
        else
          state.push(new Value.Num(0));
        return;
      }
      default: throw new ClassCastException("Cannot use SGN on "+x);
    }
  }

 
  /* Test code. */

  public static void test(Value x) {
    final InterpreterState state = new InterpreterState();
    state.frame = new InterpreterState.Call(state.frame, 0, 0, 2);
    state.push(x);
    new Signum().execute(state);
    System.out.println("SGN "+x+" = "+state.pop());
  }

  public static void main(String[] args) {
    test(new Value.Num(3.1));
    test(new Value.Num(-3.1));
    test(new Value.Num(3.7));
    test(new Value.Num(-3.7));
    test(new Value.Num(0));
  }
}
