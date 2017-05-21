package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** A version of the table subscript operation that leaves the table and the key on the stack. This is useful for implementing assignment to bits of tables. This only works for tables. */
public final class DGet extends Instruction.Op {
  public DGet() { super("DGET", 2, 3); }
  
  /** Pops a key and a table. Retrieves from the table the value corresponding to the key. Pushes the table the key and the value. */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value k = state.pop();
    final Value t = state.pop();
    try {
      final Table tTable = ((Value.Tab)t).v;
      final Value v = (Value)Table.get(tTable, k);
      if (v==null)
        throw new CrazonRuntimeException(t+"["+k+"] is not defined", state);
      state.push(t);
      state.push(k);
      state.push(v);
    } catch (ClassCastException e) {
      throw new CrazonRuntimeException(
        "Cannot assign to "+t+"["+k+"]; a table is required",
        state
      );
    }
  }
}
