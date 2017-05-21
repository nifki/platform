package org.sc3d.apt.crazon.vm.state;

import org.sc3d.apt.crazon.vm.util.*;

import java.util.*;

/** Represents the state of the Crazon interpreter. Instances are mutable, and
 * indeed are modified by the 'execute()' methods of Instructions.
 * <p>
 * The state consists of the array of Instructions being executed, the values
 * of the global variables, the state of all visible objects, and a linked-list
 * of stack frames. Each stack frame consists of the value of the program
 * counter, the values of the local variables, a short arithmetic stack, and the
 * value of the stack pointer.
 * <p>
 * For efficiency, all fields of this method are public and can be accessed
 * directly. Accessor methods are also provided as a safer alternative.
 */
// FIXME: debugging information (names of globals and locals) too.
public final class InterpreterState implements DeepCopyable {
  /** Constructs an InterpreterState.
   * @param instructions the program to execute.
   * @param globals the initial values of the global variables.
   * @param globalNames the source-code names of the global variables (used in
   * diagnostics only).
   * @param platform the I/O library to use, or 'null' for a console
   * application.
   */
  public InterpreterState(
    Instruction[] instructions,
    Value[] globals,
    String[] globalNames,
    AbstractPlatform platform
  ) {
    this.instructions = instructions;
    this.globals = globals;
    this.globalNames = globalNames;
    this.platform = platform;
    this.frame = null;
    if (platform==null) {
      this.visibleSprites = null;
      this.window = null;
      this.keys = null;
    } else {
      this.visibleSprites = new HashSet();
      this.window = new Window(platform.width, platform.height);
      this.keys = platform.getKeys();
    }
  }
  
  /** Constructs an InterpreterState with 'null' for 'instructions', 'globals',
   * 'globalNames' and 'platform'. This is used by the Instructions' test code.
   */
  public InterpreterState() { this(null, null, null, null); }

  /** Used by 'deepCopy()'. */
  private InterpreterState(InterpreterState that) {
    // Immutable state is shared.
    this.instructions = that.instructions;
    this.globalNames = that.globalNames;
    this.keys = that.keys;
    // Mutable state is deep-copied.
    if (that.globals==null) {
      this.globals = null;
    } else {
      this.globals = new Value[that.globals.length];
      for (int i=0; i<this.globals.length; i++) {
        this.globals[i] = that.globals[i].deepCopyValue();
      }
    }
    this.platform =
      that.platform==null ? null : (AbstractPlatform)that.platform.deepCopy();
    this.frame = that.frame==null ? null : (Call)that.frame.deepCopy();
    if (that.visibleSprites==null) {
      this.visibleSprites = null;
    } else {
      this.visibleSprites = new HashSet();
      for (Iterator it=that.visibleSprites.iterator(); it.hasNext(); ) {
        final Sprite sp = (Sprite)it.next();
        this.visibleSprites.add((Sprite)sp.deepCopyValue());
      }
    }
    this.window =
      that.window==null ? null : (Window)that.window.deepCopyValue();
  }

  /* New API. */

  /** The Instructions that make up the program that this interpreter is
   * executing. Can be 'null' for the benefit of test code. */
  public final Instruction[] instructions;

  /** The Values of the global variables. Global variables are indexed by
   * number. It is the assembler's job to assign unique numbers to global
   * variable names. Can be 'null' for the benefit of test code. */
  public final Value[] globals;
  
  /** The source-code names of the global variables, used in diagnostics only.
   * Can be 'null' for the benefit of test code.
   */
  public final String[] globalNames;
  
  /** The I/O library to use, or 'null' for a console application. Can be 'null'
   * for the benefit of test code. */
  public final AbstractPlatform platform;
  
  /** A Set containing at least the currently visible Sprites. Sprites
   * whose 'IsVisible' flag is 'FALSE' will be removed from this Set during the
   * next animation frame, so as to give the garbage collector a chance. When
   * the 'IsVisible' attribute of a Sprite is set to 'TRUE' it must be put back
   * into this Set. Can be 'null' for the benefit of test code. */
  public final HashSet visibleSprites;

  /** The Window which represents the viewing window. Moving this object
   * scrolls the screen, and resizing it zooms in and out. Can be 'null' for the
   * benefit of test code. */
  public final Window window;
  
  /** The keyboard state at the end of the last 'WAIT' instruction. Can be
   * 'null' for the benefit of test code. */
  public Value.Tab keys;
  
  /** The current stack frame, or 'null' if none has yet been constructed. */
  public Call frame;

  // FIXME: comments.

  public final Value getGlobal(int i) throws CrazonRuntimeException {
    final Value ans = this.globals[i];
    if (ans==null) throw new CrazonRuntimeException(
      "Global variable '"+this.globalNames[i]+"' not defined", this
    );
    return ans;
  }

  public final void putGlobal(int i, Value v) {
    if (v==null) throw new NullPointerException();
    this.globals[i] = v;
  }

  public final Value getLocal(int i) throws CrazonRuntimeException {
    final Value ans = this.frame.locals[i];
    if (ans==null) throw new CrazonRuntimeException(
      "Local variable "+i+" not defined", this
    );
    return ans;
  }

  public final void putLocal(int i, Value v) {
    if (v==null) throw new NullPointerException();
    this.frame.locals[i] = v;
  }

  public final Value pop() { return this.frame.stack[--this.frame.sp]; }

  public final void push(Value v) {
    if (v==null) throw new NullPointerException();
    this.frame.stack[this.frame.sp++] = v;
  }

  /* Implement things in DeepCopyable. */
  
  /** Copies everything necessary to fork the virtual machine. */
  public DeepCopyable deepCopy() { return new InterpreterState(this); }
  
  ////////////////////////////////////////////////////////////////////////////

  /** Represents a subroutine call stack frame. */
  public static final class Call implements DeepCopyable {
    /** Constructs a CallStack.
     * @param caller the caller's stack frame, or 'null' if this is to be the
     * stack frame of the main program.
     * @param startPC the index into 'instructions' of the start of the function
     * body to execute.
     * @param numLocals the number of local variables needed. They are all
     * initialised to 'null'.
     * @param stackLen the stack space needed. The stack is initially empty.
     */
    public Call(
      Call caller,
      int startPC,
      int numLocals,
      int stackLen
    ) {
      this.caller = caller;
      this.pc = startPC;
      this.locals = new Value[numLocals];
      this.stack = new Value[stackLen];
      this.sp = 0;
      this.loop = null;
    }

    /* New API. */

    /** The caller's stack frame, or 'null' if this is the stack frame of the
     * main program. */
    public final Call caller;

    /** The program counter, expressed as an index into 'instructions'. During
     * execution of an Instruction, this is the index of the next instruction.
     * */
    public int pc;

    /** The Values of the local variables. Local variables are indexed by
     * number. It is the parser's job to assign unique numbers to the local
     * variables in each function. */
    public final Value[] locals;

    /** The arithmetic stack. The bottom of the stack is at index '0' and the
     * top of the stack (i.e. the Value that will be popped first) is at index
     * 'sp-1'. */
    public final Value[] stack;

    /** The stack pointer, expressed as the number of items on the stack. */
    public int sp;

    /** The state of the most tightly enclosing loop, or 'null' if we're not in
     * a loop. */
    public Loop loop;
    
    /* Implement things in DeepCopyable. */
    
    public DeepCopyable deepCopy() {
      final Call ans = new Call(
        this.caller==null ? null : (Call)this.caller.deepCopy(),
        this.pc,
        this.locals.length,
        this.stack.length
      );
      ans.sp = this.sp;
      ans.loop = this.loop==null ? null : (Loop)this.loop.deepCopy();
      for (int i=0; i<this.locals.length; i++) {
        ans.locals[i] = this.locals[i].deepCopyValue();
      }
      for (int i=0; i<this.sp; i++) {
        ans.stack[i] = this.stack[i].deepCopyValue();
      }
      return ans;
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  /** Represents the state of a loop. There is a subclass for each kind of loop.
   * */
  public static abstract class Loop implements DeepCopyable {
    /** Constructs a Loop.
     * @param enclosing the state of the enclosing loop, or 'null' if none.
     * @param instruction the Instruction.Loop at the start of the loop.
     */
    public Loop(Loop enclosing, Instruction.Loop instruction) {
      this.enclosing = enclosing; this.instruction = instruction;
    }

    /* New API. */

    /** The state of the enclosing loop, or 'null' if there is no enclosing
     * loop. */
    public final Loop enclosing;

    /** The Instruction.Loop at the beginning of the loop. */
    public final Instruction.Loop instruction;
  }
}
