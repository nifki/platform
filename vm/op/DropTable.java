package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the "DROPTABLE" instruction which discards the top element of the
 * stack after checking that it is an empty table. If it is not an empty table,
 * this instruction throws a CrazonRuntimeException. This instruction is used
 * to check that subroutine calls (as opposed to function calls) return the
 * empty table. */
public final class DropTable extends Instruction.Op {
  public DropTable() { super("DROPTABLE", 1, 0); }

  /** Pops one value.
   * @throws CrazonRuntimeException if the value popped is not the empty
   * table.
   */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value value = state.pop();
    if (value.type!=Value.TYPE_TAB || ((Value.Tab)value).v!=null) {
      throw new CrazonRuntimeException(
        "A function can be called as a subroutine only if it returns [] "+
        "(the empty table)",
        state
      );
    }
  }
}
