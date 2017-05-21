package org.sc3d.apt.crazon.vm;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;
import org.sc3d.apt.crazon.vm.op.*;

import java.io.*;
import java.util.*;

/** Parses a Crazon assembler file and represents the results. */
public class Assembler {
  /** Constructs an Assembler and parses a Crazon assembler file from 'in'.
   * @throws IOException if 'in' does.
   * @throws SyntaxException when a syntax error is detected.
   */
  public Assembler(Reader in) throws IOException, SyntaxException {
    // Initialise reader state.
    this.in = in;
    this.lineNum = 1;
    this.c = in.read();
    this.next();
    // Initialise assembler state.
    this.globalMappings = new HashMap();
    this.globalValues = new Value[16];
    this.instructions = new Instruction[16];
    this.instructionsUsed = 0;
    this.instructionsFilled = 0;
    try {
      // Parse the main program.
      this.main = this.parseFunctionBody(0, 0, "<main>");
      this.append(END);
      // Parse the function definitions.
      while (this.word!=null) {
        if (!this.word.startsWith("DEF("))
          throw new SyntaxException("Expected DEF(name)");
        final String name = this.extractParameter();
        this.next();
        this.defineFunction(name, this.parseFunctionBody(1, -1, name));
      }
    } catch (SyntaxException e) {
      throw new SyntaxException(e.getMessage() + " at line " + this.lineNum);
    }
  }

  /* New API. */

  /** Returns the global variables, and the unique numbers that have been
   * allocated for them. The returned value is a Map from global variable names
   * (Strings) to their indices (Integers). */
  public Map getGlobalMappings() { return new HashMap(this.globalMappings); }

  /** Returns a fresh array with one element for every global variable, in which
   * the elements corresponding to function definitions have been filled in with
   * the corresponding function values. Other elements are 'null'. */
  public Value[] getGlobalValues() {
    final Value[] ans = new Value[this.globalMappings.size()];
    final int toCopy = Math.min(ans.length, this.globalValues.length);
    System.arraycopy(this.globalValues, 0, ans, 0, toCopy);
    return ans;
  }
  
  public String[] getGlobalNames() {
    final String[] ans = new String[this.globalMappings.size()];
    for (Iterator it=this.globalMappings.keySet().iterator(); it.hasNext(); ) {
      final String varName = (String)it.next();
      final int varNum = ((Integer)this.globalMappings.get(varName)).intValue();
      ans[varNum] = varName;
    }
    return ans;
  }

  /** Returns the Instructions in a fresh array. */
  public Instruction[] getInstructions() {
    if (this.instructionsFilled<this.instructionsUsed) {
      throw new IllegalStateException(
        "Not all instructions have been filled in"
      );
    }
    final Instruction[] ans = new Instruction[this.instructionsUsed];
    System.arraycopy(this.instructions, 0, ans, 0, ans.length);
    return ans;
  }

  /** The main program as a Value.Func. */
  public final Value.Func main;

  /* Private. */

  /** The Reader passed to the constructor. */
  private final Reader in;

  /** The current line number in the assembler source file. */
  private int lineNum;
  
  /** The character most recently read from 'in', or '-1' for end-of-file. */
  private int c;

  /** The word most recently read from 'in', or 'null' for end-of-file. */
  private String word;

  /** A Map from global variable names (as Strings) to the corresponding indices
   * (as Integers). The Assembler invents a mapping from names to indices as it
   * parses the input. The indices are used by the interpreter instead of the
   * names for efficiency. */
  private final HashMap globalMappings;

  /** An array which maps global variables to their initial values. This is
   * filled in as functions are defined. */
  private Value[] globalValues;

  /** The Instructions constructed so far. */
  private Instruction[] instructions;

  /** The number of elements allocated in 'instructions', including those that
   * have not yet been filled in. */
  private int instructionsUsed;

  /** The number of Instructions in 'instructions' (i.e. the number that have
   * been filled in). */
  private int instructionsFilled;

  /** Reads a word of input and assigns it to 'word'. Most words are maximal
   * non-whitespace strings. The exceptions are:<ul>
   * <li>Literal strings, which are recognised by the leading '"' character,
   * extend to the next '"' character.
   * <li>Comments, which are recognised by the leading '#' character, extend to
   * the next newline character.
   * </ul>
   * @throws SyntaxException when unclosed string literal is detected.
   */
  private void next() throws IOException, SyntaxException {
    while (this.c!=-1 && "\t\n\r ".indexOf(this.c)>=0) {
      if (this.c=='\n') this.lineNum++;
      this.c = this.in.read();
    }
    if (this.c==-1) {
      this.word = null;
    } else if (this.c=='#') { // Comment.
      final StringBuffer sb = new StringBuffer();
      while (this.c!=-1 && this.c!='\n') {
        sb.append((char)this.c);
        this.c = this.in.read();
      }
      this.word = sb.toString();
    } else if (this.c=='"') { // Literal string.
      final StringBuffer sb = new StringBuffer("\"");
      this.c = this.in.read();
      while (this.c!=-1 && this.c!='"') {
        sb.append((char)this.c);
        this.c = this.in.read();
      }
      if (this.c==-1) throw new SyntaxException("Unclosed string literal: "+sb);
      this.word = sb.append('"').toString();
      this.c = in.read();
    } else { // Number or ordinary instruction.
      final StringBuffer sb = new StringBuffer();
      while (this.c!=-1 && "\t\n\r ".indexOf(this.c)<0) {
        sb.append((char)this.c);
        this.c = this.in.read();
      }
      this.word = sb.toString();
    }
  }

  /** Checks that the next word matches 'expected' and calls 'next()'.
   * @throws SyntaxException if the word does not match.
   */
  private void expect(String expected) throws IOException, SyntaxException {
    if (!expected.equals(this.word))
      throw new SyntaxException("Expected "+expected);
    this.next();
  }

  /** Returns the index assigned to the global variable with name 'name',
   * allocating a new one if necessary.
   * @param name the (fully-qualified) name of the global variable.
   */
  private int getGlobalIndex(String name) {
    if (!this.globalMappings.containsKey(name)) {
      this.globalMappings.put(name, new Integer(this.globalMappings.size()));
    }
    return ((Integer)this.globalMappings.get(name)).intValue();
  }

  /** Returns the index assigned to the local variable with name 'name',
   * allocating a new one if necessary.
   * @param name the name of the local variable.
   * @param localMappings the HashMap used to record the indices assigned to
   * local variables in the current scope. It is a Map from names (Strings) to
   * indices (Integers).
   */
  private int getLocalIndex(String name, HashMap localMappings) {
    if (!localMappings.containsKey(name)) {
      localMappings.put(name, new Integer(localMappings.size()));
    }
    return ((Integer)localMappings.get(name)).intValue();
  }

  /** Defines the value of the specified global variable. */
  private void defineFunction(String name, Value.Func value) {
    final int index = this.getGlobalIndex(name);
    while (this.globalValues.length<=index) {
      final Value[] old = this.globalValues;
      this.globalValues = new Value[2*old.length];
      System.arraycopy(old, 0, this.globalValues, 0, old.length);
    }
    this.globalValues[index] = value;
  }

  /** Returns an object representing the right to fill in the next Instruction
   * in 'instructions'. */
  private Slot allocate() {
    if (this.instructionsUsed>=this.instructions.length) {
      final Instruction[] old = this.instructions;
      this.instructions = new Instruction[2*old.length];
      System.arraycopy(old, 0, this.instructions, 0, old.length);
    }
    return new Slot(this.instructionsUsed++);
  }

  /** Fills in the next instruction.
   * Equivalent to 'allocate().fill(instruction)'. */
  private void append(Instruction instruction) {
    this.allocate().fill(instruction);
  }

  /** Parses the portion of the input corresponding to a single function body,
   * and returns it as a Value.Func.
   * @param entrySP the number of items that will be on the arithmetic stack on
   * entry to the function body.
   * @param exitSp the number of items that should be on the arithmetic stack on
   * 'fall-though' exit from the function body, or '-1' if fall-through is not
   * allowed.
   * @throws SyntaxException if necessary.
   */
  private Value.Func parseFunctionBody(
    int entrySP,
    int exitSP,
    String originalName
  ) throws IOException, SyntaxException {
    final HashMap localMappings = new HashMap(); // create new scope.
    if (this.instructionsUsed!=this.instructionsFilled)
      throw new RuntimeException("Used != Filled");
    final int startPC = this.instructionsUsed;
    this.parseBlock(entrySP, exitSP, 0, localMappings);
    if (this.instructionsUsed!=this.instructionsFilled)
      throw new RuntimeException("Used != Filled");
    final int endPC = this.instructionsUsed;
    // Work out the stack requirements.
    int stackLen = 0, sp = entrySP;
    for (int i=startPC; i<endPC; i++) {
      sp -= this.instructions[i].pops;
      sp += this.instructions[i].pushes;
      if (sp>stackLen) stackLen = sp;
    }
    return new Value.Func(
      startPC,
      localMappings.size(),
      stackLen,
      originalName
    );
  }

  /** Parses a basic block. Parsing ends at one of the 'STOP_WORDS', at
   * "DEF(name)", or just after "RETURN" or "ERROR" or "BREAK".
   * @param entrySP the number of Values that will be on the arithmetic stack on
   * entry to the block.
   * @param exitSP the number of Values that should be on the arithmetic stack
   * on 'fall-through' exit from the block, i.e. not via "RETURN" or "BREAK", or
   * '-1' if fall-through is not allowed.
   * @param numLoops the number of loops that enclose this block. This is used
   * to limit the number of "BREAK"s.
   * @throws SyntaxException if the block is not correctly structured. This can
   * happen if "IF ... THEN ... ELSE" structures, "LOOP ... WHILE ... NEXT ...
   * ELSE" structures and "FOR ... NEXT ... ELSE" structures are not
   * properly nested, if there are too many "BREAK" instructions, or if the
   * wrong number of Values is left on the stack at any point.
   */
  private void parseBlock(
    final int entrySP, final int exitSP,
    final int numLoops,
    final HashMap localMappings
  ) throws IOException, SyntaxException {
    int sp = entrySP;
    while (!STOP_WORDS.contains(this.word) && !this.word.startsWith("DEF(")) {
      final char c = this.word.charAt(0);
      if ("IF".equals(this.word)) {
        if (sp!=1) throw new SyntaxException(
          "Stack should contain 1 item (the condition) before executing IF"
        );
        final Slot ifSlot = this.allocate();
        this.next();
        sp--;
        this.parseBlock(0, 0, numLoops, localMappings); // The "THEN" block.
        this.expect("THEN");
        final Slot thenSlot = this.allocate();
        final int elsePC = this.instructionsUsed;
        this.parseBlock(0, 0, numLoops, localMappings); // The "ELSE" block.
        this.expect("ELSE");
        final int endPC = this.instructionsUsed;
        ifSlot.fill(new If(elsePC));
        thenSlot.fill(new Goto(endPC));
      } else if ("LOOP".equals(this.word)) {
        if (sp!=0) throw new SyntaxException(
          "Stack should be empty before executing LOOP"
        );
        this.next();
        final Slot slot = this.allocate();
        final int loopPC = this.instructionsUsed;
        this.parseBlock(0, 1, numLoops+1, localMappings); // The condition.
        ++sp;
        this.expect("WHILE");
        this.append(WHILE);
        --sp;
        this.parseBlock(0, 0, numLoops+1, localMappings); // The loop body.
        this.expect("NEXT");
        this.append(NEXT);
        final int elsePC = this.instructionsUsed;
        this.parseBlock(0, 0, numLoops, localMappings); // The "ELSE" block.
        this.expect("ELSE");
        final int breakPC = this.instructionsUsed;
        slot.fill(new Loop(loopPC, elsePC, breakPC));
      } else if ("FOR".equals(this.word)) {
        if (sp!=1) throw new SyntaxException(
          "Stack should contain 1 item (the table or string) before executing "+
          "FOR"
        );
        this.next();
        final Slot slot = this.allocate();
        final int loopPC = this.instructionsUsed;
        this.parseBlock(2, 0, numLoops+1, localMappings); // The loop body.
        --sp;
        this.expect("NEXT");
        this.append(NEXT);
        final int elsePC = this.instructionsUsed;
        this.parseBlock(0, 0, numLoops, localMappings); // The "ELSE" block.
        this.expect("ELSE");
        final int breakPC = this.instructionsUsed;
        slot.fill(new For(loopPC, elsePC, breakPC));
      } else if (";".equals(this.word)) {
        if (sp!=0) throw new SyntaxException(
          "Stack should be empty before executing ;"
        );
        this.next();
      } else if (c=='#') {
        this.next();
      } else if ("BREAK".equals(this.word)) {
        if (sp!=0) throw new SyntaxException(
          "Stack should be empty before executing BREAK"
        );
        // Glob up as many "BREAK"s as possible.
        int numBreaks = 0;
        while ("BREAK".equals(this.word)) {
          numBreaks++;
          this.next();
        }
        if (numBreaks>numLoops) throw new SyntaxException(
          numBreaks+" BREAK instructions, but only "+numLoops+" enclosing loops"
        );
        this.append(new Break(numBreaks));
        return;
      } else if ("RETURN".equals(this.word)) {
        if (sp!=1) throw new SyntaxException(
          "Stack should contain 1 item (the result) before executing RETURN"
        );
        this.append(RETURN);
        this.next();
        return;
      } else if ("ERROR".equals(this.word)) {
        if (sp!=1) throw new SyntaxException(
          "Stack should contain 1 item (the result) before executing ERROR"
        );
        // FIXME:
        // this.append(ERROR);
        // this.next();
        // return;
        throw new RuntimeException("ERROR instruction not yet implemented");
      } else {
        Instruction instruction = null;
        if (c=='"') { // Literal string.
          instruction = new Constant(
            new Value.Str(SSSString.decode(this.word))
          );
        } else if (c>='0' && c<='9') { // Literal number.
          instruction = new Constant(
            new Value.Num(Double.parseDouble(this.word)) // FIXME: use SSS.
          );
        } else if (OPS.containsKey(this.word)) { // Arithmetic.
          instruction = (Instruction)OPS.get(this.word);
        } else if (this.word.startsWith("LOAD(")) { // Load global.
          final String name = this.extractParameter();
          final int index = this.getGlobalIndex(name);
          instruction = new Load(index, name);
        } else if (this.word.startsWith("STORE(")) { // Store global.
          final String name = this.extractParameter();
          final int index = this.getGlobalIndex(name);
          instruction = new Store(index, name);
        } else if (this.word.startsWith("LLOAD(")) { // Load local.
          final String name = this.extractParameter();
          final int index = this.getLocalIndex(name, localMappings);
          instruction = new LLoad(index, name);
        } else if (this.word.startsWith("LSTORE(")) { // Store local.
          final String name = this.extractParameter();
          final int index = this.getLocalIndex(name, localMappings);
          instruction = new LStore(index, name);
        } else if (this.word.startsWith("SET(")) { // Set object attribute.
          instruction = new SetAttr(this.extractParameter());
        } else {
          throw new SyntaxException("Unknown instruction: "+this.word);
        }
        sp -= instruction.pops;
        if (sp<0) throw new SyntaxException("Stack underflow");
        sp += instruction.pushes;
        this.append(instruction);
        this.next();
      }
    }
    if (sp!=exitSP) throw new SyntaxException(
      "Stack contains "+sp+" items; should be "+exitSP
    );
  }

  /** The Set of words at which 'parseBlock()' stops, not including "DEF(name)".
   * It contains 'null', "THEN", "WHILE", "NEXT" and "ELSE". */
  private static final HashSet STOP_WORDS = new HashSet();
  static {
    STOP_WORDS.add(null);
    STOP_WORDS.add("THEN");
    STOP_WORDS.add("WHILE");
    STOP_WORDS.add("NEXT");
    STOP_WORDS.add("ELSE");
  }

  /** The set of all arithmetic instructions recognised by 'parseBlock()',
   * represented as a Map from op-codes (Strings) to Instructions. */
  private static final HashMap OPS = new HashMap();
  private static void installOp(Instruction.Op op) { OPS.put(op.name, op); }
  static {
    installOp(new Cls());
    installOp(new Tab()); installOp(new Put());
    installOp(new Get()); installOp(new DGet());
    installOp(new Contains());
    installOp(new org.sc3d.apt.crazon.vm.op.Random()); installOp(new Keys());
    installOp(new Call());
    installOp(new Pow());
    installOp(new Abs()); installOp(new Sqrt());
    installOp(new Neg()); installOp(new Not());
    installOp(new Round()); installOp(new Floor()); installOp(new Ceil());
    installOp(new Len());
    installOp(new Mul()); installOp(new Div()); installOp(new Mod());
    installOp(new Add()); installOp(new Sub());
    installOp(new Min()); installOp(new Max());
    installOp(new EQ()); installOp(new NE()); installOp(new LG());
    installOp(new LT()); installOp(new GE());
    installOp(new GT()); installOp(new LE());
    installOp(new Xor()); installOp(new And()); installOp(new Or());
    installOp(new Constant(Value.Bool.TRUE));
    installOp(new Constant(Value.Bool.FALSE));
    installOp(new Spr()); installOp(new org.sc3d.apt.crazon.vm.op.Window());
    installOp(new Drop()); installOp(new DropTable());
    installOp(new Dump()); installOp(new Wait());
  }

  /** Extracts "name" from 'this.word' if is of the form "OP(name)".
   * @throws SyntaxException if 'this.word' is not of that form.
   */
  private final String extractParameter() throws SyntaxException {
    final int len = this.word.length();
    final int pos = this.word.indexOf('(');
    if (pos < 0)
      throw new SyntaxException("Missing '(' in "+this.word);
    if (this.word.charAt(len-1)!=')')
      throw new SyntaxException("Missing ')' in "+this.word);
    return this.word.substring(pos+1, len-1);
  }

  /** An instance of Instruction.While. */
  private static final Instruction WHILE = new While();

  /** An instance of Instruction.Next. */
  private static final Instruction NEXT = new Next();

  /** An instance of Instruction.Return. */
  private static final Instruction RETURN = new Return();

  /** An instance of Instruction.End. */
  private static final Instruction END = new End();

  ////////////////////////////////////////////////////////////////////////////

  /** The return type of 'allocate()', which represents the right to fill in an
   * Instruction. */
  private class Slot {
    /** Constructs a Slot that represents the right to fill in the Instruction
     * at index 'index'. */
    public Slot(int index) { this.index = index; }

    /** The index that may be filled in. */
    public final int index;

    /** Fills in the Instruction, checking that it has not already been filled.
     * */
    public void fill(Instruction instruction) {
      final Assembler outer = Assembler.this;
      if (outer.instructions[this.index]!=null) throw new IllegalStateException(
        "This Slot has already been filled"
      );
      outer.instructions[this.index] = instruction;
      outer.instructionsFilled++;
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  /* Test code. */

  public static void main(String[] args) throws IOException {
    if (args.length!=1) {
      throw new IllegalArgumentException(
        "usage: java org.sc3d.apt.crazon.Assembler <assembler file>"
      );
    }

    final FileReader in = new FileReader(args[0]);
    Assembler me = null;
    try {
      me = new Assembler(in);
    } catch (SyntaxException e) {
      System.out.println(e.getMessage());
      return;
    }
    // Print out the instructions.
    final Instruction[] instructions = me.getInstructions();
    for (int i=0; i<instructions.length; i++) {
      System.out.println("instructions["+i+"] = "+instructions[i]);
    }
    // Print out the non-null globals.
    final Value[] globals = me.getGlobalValues();
    for (int i=0; i<globals.length; i++) if (globals[i]!=null) {
      System.out.println("globals["+i+"] = "+globals[i]);
    }
  }
}
