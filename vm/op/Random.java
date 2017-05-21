package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "RANDOM" instruction, which leaves a random number on the
 * stack. */
public final class Random extends Instruction.Op {
  public Random() { super("RANDOM", 0, 1); }

  /** The java.util.Random that backs this Random. */
  public static final java.util.Random RANDOM = new java.util.Random();

  /** Pushes a number chosen from a uniform distribution from 0 to 1. */
  public final void execute(InterpreterState state) {
    state.push(new Value.Num(RANDOM.nextDouble()));
  }
}
