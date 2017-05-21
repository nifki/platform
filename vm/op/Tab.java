package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Leaves the empty table on the stack. */
public final class Tab extends Instruction.Op {
  public Tab() { super("TABLE", 0, 1); }

  /** The Value that represents the empty table. */
  public static final Value.Tab TABLE = new Value.Tab(null);

  /** Pushes 'TABLE'. */
  public final void execute(InterpreterState state) { state.push(TABLE); }
}
