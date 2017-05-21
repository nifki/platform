package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** Implements the subscript operation. Gets a value from a table, or a character from a string. */
public final class Get extends Instruction.Op {
  public Get() { super("GET", 2, 1); }
  
  /** Pops two values and pushes a result. The calculation depends on the types of the values popped:<ul>
   * <li>If the first (bottom-most) value is a string, the second must be an integer, and is used to retrieve a character from the string, expressed as an integer.
   * <li>If the first value is a table, the second is used as a key to retrieve a value from the table.
   * <li>If the first value is an object, the second must be a string and is used as a name to retrieve an attribute of the object.
   * </ul> */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value k = state.pop();
    final Value t = state.pop();
    Value v;
    try {
      switch (t.type) {
        case Value.TYPE_STR: {
          final String tStr = ((Value.Str)t).v;
          final double kNum = ((Value.Num)k).v;
          if (kNum!=(int)kNum) throw new CrazonRuntimeException(
            "String subscript must be an integer, not "+k,
            state
          );
          final int index = (int)kNum;
          v = new Value.Str(tStr.substring(index, index+1));
          break;
        }
        case Value.TYPE_TAB: {
          final Table tTab = ((Value.Tab)t).v;
          v = (Value)Table.get(tTab, k);
          break;
        }
        case Value.TYPE_OBJ: {
          final Value.Obj tObj = (Value.Obj)t;
          final String kStr = ((Value.Str)k).v;
          v = tObj.get(kStr);
          break;
        }
        default: throw new CrazonRuntimeException(
          "Type error: cannot subscript "+t,
          state
        );
      }
    } catch (ClassCastException e) {
      throw new CrazonRuntimeException(
        "Type error: cannot subscript "+t+" by "+k,
        state
      );
    }
    if (v==null) throw new CrazonRuntimeException(
      t+"["+k+"] is not defined",
      state
    );
    state.push(v);
  }
}
