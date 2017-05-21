package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "BREAK" operation, which calls a function. */
public final class Break extends Instruction {
  public Break(int numLoops) {
    super(0, 0);
    this.numLoops = numLoops;
    if (numLoops<1) throw new IllegalArgumentException();
  }
  
  /** The number of loops out of which to break. */
  public final int numLoops;
  
  /** Breaks out of the enclosing loops. */
  public final void execute(InterpreterState state) {
    for (int i=1; i<this.numLoops; i++) {
      state.frame.loop = state.frame.loop.enclosing;
    }
    state.frame.pc = state.frame.loop.instruction.breakPC;
    state.frame.loop = state.frame.loop.enclosing;
  }
  
  /** Returns a String of the form "BREAK(n)" where "n" is 'numLoops'. */
  public final String toString() { return "BREAK("+this.numLoops+")"; }
}
