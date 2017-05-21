package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** The subclass of Instruction.LoopStart that marks the beginning of a "FOR ...
 * NEXT ... ELSE" structure. */
public class For extends Instruction.Loop {
  /** Constructs a For.
   * @param loopPC the index of the Instruction just after the "FOR" marker.
   * @param elsePC the index of the Instruction just after the "NEXT" marker.
   * @param breakPC the index of the Instruction just after the "ELSE" marker.
   */
  public For(int loopPC, int elsePC, int breakPC) {
    super(1, 2, loopPC, elsePC, breakPC);
  }

  /* New API. */

  /* Override things in Instruction.LoopStart. */

  public final void executeNext(InterpreterState state) {
    final State loopState = (State)state.frame.loop;
    if (loopState.it==null) {
      // Exit the loop.
      state.frame.loop = loopState.enclosing;
      state.frame.pc = this.elsePC;
    } else {
      // Execute the body of the loop.
      state.push((Value)loopState.it.key);
      state.push((Value)loopState.it.value);
      loopState.it = loopState.it.next();
      state.frame.pc = this.loopPC;
    }
  }

  /* Override things in Instruction. */

  /** Pops a Value, constructs a Table, gets its Iterator, puts a For.State on
   * the loop stack, and calls 'executeNext()'. The calculation of the
   * Table depends on the type of the Value popped:<ul>
   * <li>If it is an integer 'n', the Table maps all integers from '0' to 'n-1'
   * to themselves.
   * <li>If it is a string, the Table maps indices into the string to the
   * corresponding characters (strings of length 1).
   * <li>If it is a Table, it is used unchanged.
   * </ul> */
  public final void execute(InterpreterState state)
  throws CrazonRuntimeException {
    final Value t = state.pop();
    switch (t.type) {
    case Value.TYPE_NUM: {
      final double d = ((Value.Num)t).v;
      final int n = (int)d;
      if (n != d)
        throw new CrazonRuntimeException(d+" is not an integer", state);
      Table range = null;
      for (int i=0; i<n; i++) {
        final Value.Num v = new Value.Num(i);
        range = Table.put(range, v, v);
      }
      state.frame.loop = new State(
        state.frame.loop,
        this,
        Table.iterator(range)
      );
      break;
    }
    case Value.TYPE_STR: {
      final String s = ((Value.Str)t).v;
      final int length = s.length();
      Table range = null;
      for (int i=0; i<length; i++) {
        final Value.Num k = new Value.Num(i);
        final Value.Str v = new Value.Str(s.substring(i, i+1));
        range = Table.put(range, k, v);
      }
      state.frame.loop = new State(
        state.frame.loop,
        this,
        Table.iterator(range)
      );
      break;
    }
    case Value.TYPE_TAB: {
      state.frame.loop = new State(
        state.frame.loop,
        this,
        Table.iterator(((Value.Tab)t).v)
      );
      break;
    }
    default:
      throw new CrazonRuntimeException("Can't iterate through "+t, state);
    }
    this.executeNext(state);
  }

  /** Returns "FOR". */
  public final String toString() { return "FOR"; }

  ////////////////////////////////////////////////////////////////////////////

  /** Represents the state of a "FOR ... NEXT ... ELSE" loop. */
  public static class State extends InterpreterState.Loop {
    /** Constructs a State.
     * @param enclosing the state of the enclosing loop, or 'null' if none.
     * @param instruction the instruction at the beginning of the loop.
     * @param it the Table.Iterator through which to loop.
     * @param useKeys 'true' to iterate through the keys of the table, or
     * 'false' to iterate through its values.
     */
    public State(
      InterpreterState.Loop enclosing,
      Instruction.Loop instruction,
      Table.Iterator it
    ) {
      super(enclosing, instruction);
      this.it = it;
    }

    /** The Table.Iterator through which to loop. */
    public Table.Iterator it;
    
    /* Implement things in DeepCopyable. */
    
    public DeepCopyable deepCopy() {
      final DeepCopyable enclosing =
        this.enclosing==null ? null : this.enclosing.deepCopy();
      return new State(
        (InterpreterState.Loop)enclosing,
        this.instruction,
        this.it
      );
    }
  }
}
