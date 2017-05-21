package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the WINDOW instruction, which places the object representing the viewing window on the stack. */
public final class Window extends Instruction.Op {
  public Window() { super("WINDOW", 0, 1); }
  
  /** Leaves 'state.window' on the stack. */
  public final void execute(InterpreterState state) {
    state.push(state.window);
  }
}
