package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the 'SQRT' operation which returns the square root of x. */
public final class Sqrt extends Instruction.Op {
  public Sqrt() { super("SQRT", 1, 1); }

  /** Pops one number value, x, and pushes sqrt(-x). */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value x = state.pop();
    switch (x.type) {
      case Value.TYPE_NUM: {
        final double xNum = ((Value.Num)x).v;
        if (xNum < 0)
          throw new CrazonRuntimeException(
            "Cannot square root negative number "+x,
            state
          );
        state.push(new Value.Num(Math.sqrt(xNum)));
        return;
      }
      default: throw new CrazonRuntimeException(
        "Cannot square root "+x,
        state
      );
    }
  }

 
  /* Test code. */

  public static void test(Value x) throws CrazonRuntimeException {
    final InterpreterState state = new InterpreterState();
    state.frame = new InterpreterState.Call(state.frame, 0, 0, 2);
    state.push(x);
    new Sqrt().execute(state);
    System.out.println("SQRT "+x+" = "+state.pop());
  }

  public static void main(String[] args) throws CrazonRuntimeException {
    test(new Value.Num(3.1));
    test(new Value.Num(3.7));
    int caught = 0;
    try {
      test(new Value.Num(-3.1));
    } catch (CrazonRuntimeException e) {
      System.out.println("Caught expected exception: "+e);
      ++caught;
    }
    
    try {
      test(new Value.Num(-3.7));
    } catch (CrazonRuntimeException e) {
      System.out.println("Caught expected exception: "+e);
      ++caught;
    }
    
    if (caught != 2)
      System.out.println("FAILED.");
  }
}
