package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Prints the value at the top of the stack. */
public final class Dump extends Instruction.Op {
  public Dump() { super("DUMP", 1, 0); }
  
  /** Pops a Value and prints it (without a newline). If the Value is not a
   * Value.Str, it is converted to a String using 'toLongString()'. */
  public final void execute(InterpreterState state) {
    final Value v = state.pop();
    if (v.type==Value.TYPE_STR)
      state.platform.print(((Value.Str)v).v);
    else
      state.platform.print(v.toLongString());
  }
}
