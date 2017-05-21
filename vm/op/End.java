package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "END" operation, which throws an exception. */
public final class End extends Instruction.Op {
  public End() { super("END", 2, 1); }

  /** Throws an EndException. */
  public final void execute(InterpreterState state)
  throws Instruction.EndException {
    throw new Instruction.EndException("END");
  }
}
