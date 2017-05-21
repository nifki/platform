package org.sc3d.apt.crazon.vm.state;

/** Represents a virtual machine instruction. Some special subclasses are inner
 * classes. Subclasses of Op are in the org.sc3d.apt.crazon.op package. */
public abstract class Instruction {
  /** Constructs an Instruction given values for its fields. */
  public Instruction(int pops, int pushes) {
    this.pops = pops; this.pushes = pushes;
  }

  /* NEW API. */

  /** The number of Values that this Instruction removes from the stack. */
  public final int pops;

  /** The number of Values that this Instruction leaves on the stack. */
  public final int pushes;

  /** Defines the semantics of this Instruction. Principally, this takes the
   * form of side-effects applied to 'state'.
   * @throws EndException when the end of program is reached.
   * @throws CrazonRuntimeException if this Instruction cannot be executed in
   * the specified VM state.
   */
  public abstract void execute(InterpreterState state)
  throws EndException, CrazonRuntimeException;

  /* Override things in Object. */

  /** Returns an assembler representation of this Instruction. */
  public abstract String toString();

  ////////////////////////////////////////////////////////////////////////////

  /** The abstract superclass of instructions whose assembler representation is
   * constant. */
  public static abstract class Op extends Instruction {
    public Op(String name, int pops, int pushes) {
      super(pops, pushes);
      this.name = name;
    }

    /* New API. */

    /** The assembler representation of this Instruction. */
    public final String name;

    /** Returns 'name'. */
    public final String toString() { return this.name; }
  }

  ////////////////////////////////////////////////////////////////////////////

  /** The superclass of instructions that mark the beginnings of loops. Such
   * instructions interact with "NEXT" and "BREAK" instructions. */
  public static abstract class Loop extends Instruction {
    /** Constructs a Loop, given values for its fields. */
    public Loop(
      int pops, int pushes,
      int loopPC, int elsePC, int breakPC
    ) {
      super(pops, pushes);
      this.loopPC = loopPC; this.elsePC = elsePC; this.breakPC = breakPC;
    }

    /* New API. */

    /** The value of the program counter at the beginning of the loop body. The
     * program counter is set to this value each time the loop body needs to be
     * executed. Normally, the loop body starts immediately after this
     * Instruction. */
    public final int loopPC;

    /** The value of the program counter at the beginning of the "ELSE" clause.
     * The program counter is set to this value on normal exit from the loop. */
    public final int elsePC;

    /** The value of the program counter just after the "ELSE" clause. The
     * program counter is set to this value on exiting the loop using "BREAK".
     * */
    public final int breakPC;

    /** Defines the behaviour of the "NEXT" instruction when inside this loop.
     * @param state the InterpreterState to modify.
     */
    public abstract void executeNext(InterpreterState state);
  }

  ////////////////////////////////////////////////////////////////////////////

  /** Thrown by the execute method of an End instruction. */
  public class EndException extends Exception {
    public EndException() { super(); }
    public EndException(String msg) { super(msg); }
  }
}
