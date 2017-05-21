package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** An instruction that leaves a constant Value on the stack.
 * <p>This class should only be used for Values whose 'toString()' method returns valid assembler for this Instruction. For tables, use the "TABLE" instruction and then add the contents. */
public class Constant extends Instruction.Op {
  public Constant(Value v) { super(v.toString(), 0, 1); this.v = v; }

  /** The Value that this Instruction leaves on the stack. */
  public final Value v;

  public final void execute(InterpreterState state) { state.push(this.v); }
}
