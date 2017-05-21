package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements attribute assignment. */
public final class SetAttr extends Instruction.Op {
  public SetAttr(String name) {
    super("SET("+name+")", 2, 0);
    this.name = name;
  }
  
  /** The name of the attribute whose value to set. */
  public final String name;

  /** Pops a value, and an object. Sets the attribute of the specified object
   * with name 'name' to the specified value. If the object is an instance of
   * 'Sprite', adds it to the Set of potentially visible sprites. */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value v = state.pop();
    final Value o = state.pop();
    try {
      final Value.Obj oObj = (Value.Obj)o;
      oObj.set(this.name, v);
      if (oObj instanceof Sprite) state.visibleSprites.add(o);
    } catch (ClassCastException e) {
      throw new CrazonRuntimeException(
        "Cannot apply SET to "+o+"; an object is required",
        state
      );
    } catch (CrazonException e) {
      throw new CrazonRuntimeException(e.getMessage(), state);
    }
  }
}
