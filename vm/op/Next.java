package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "NEXT" operation, which calls a function. */
public final class Next extends Instruction.Op {
  public Next() { super("NEXT", 0, 0); }
  
  /** Calls the 'executeNext()' method of the enclosing loop. */
  public final void execute(InterpreterState state) {
    state.frame.loop.instruction.executeNext(state);
  }
}
