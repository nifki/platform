package org.sc3d.apt.crazon.vm.op;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

/** The subclass of Instruction.Loop that marks the beginning of a "LOOP ... WHILE ... NEXT ... ELSE" structure. */
public class Loop extends Instruction.Loop {
  /** Constructs a Loop.
   * @param loopPC the index of the Instruction just after the "LOOP" marker.
   * @param elsePC the index of the Instruction just after the "NEXT" marker.
   * @param breakPC the index of the Instruction just after the "ELSE" marker.
   */
  public Loop(int loopPC, int elsePC, int breakPC) {
    super(0, 0, loopPC, elsePC, breakPC);
  }

  /* Override things in Loop. */

  public final void executeNext(InterpreterState state) {
    state.frame.pc = this.loopPC;
  }

  /* Override things in Instruction. */

  public final void execute(InterpreterState state) {
    state.frame.loop = new State(state.frame.loop, this);
  }

  /** Returns "LOOP". */
  public final String toString() { return "LOOP"; }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** Represents the state of a "LOOP" loop. */
  public static class State extends InterpreterState.Loop {
    /** Constructs a State.
     * @param enclosing the state of the enclosing loop, or 'null' if none.
     * @param instruction the Loop at the start of the loop.
     */
    public State(
      InterpreterState.Loop enclosing,
      Instruction.Loop instruction
    ) {
      super(enclosing, instruction);
    }
    
    /* Implement things in DeepCopyable. */
    
    public DeepCopyable deepCopy() {
      final DeepCopyable enclosing =
        this.enclosing==null ? null : this.enclosing.deepCopy();
      return new State(
        (InterpreterState.Loop)enclosing,
        this.instruction
      );
    }
  }
}
