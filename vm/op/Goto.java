package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "GOTO" operation, which sets the program counter. */
public final class Goto extends Instruction {
  public Goto(int targetPC) {
    super(0, 0);
    this.targetPC = targetPC;
  }
  
  /** The value to which to set the program counter. */
  public final int targetPC;
  
  /** Sets the program counter to 'targetPC'. */
  public final void execute(InterpreterState state) {
    state.frame.pc = this.targetPC;
  }
  
  /** Returns a String of the form "GOTO(n)" where "n" is 'targetPC'. */
  public final String toString() { return "GOTO("+this.targetPC+")"; }
}
