package org.sc3d.apt.crazon.vm;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

import java.io.*;

/** The main program. */
public class Main {
  /** Takes the following arguments:<ul>
   * <li>Name of assembler file to execute.
   * <li>Width of screen in pixels.
   * <li>Height of screen in pixels.
   * <li>Delay between frames in milliseconds.
   * </ul>If trailing arguments are omitted, the bahviour is as follows:<ul>
   * <li>The delay defaults to 80 milliseconds.
   * <li>The height defaults to the width.
   * <li>If the width is omitted, no screen is constructed and any graphics
   * commands will fail. This is useful for console applications.
   * </ul>
   */
  public static void main(String[] args) throws IOException {
    // Parse arguments.
    if (args.length!=4) throw new IllegalArgumentException(
      "Syntax: java org.sc3d.apt.crazon.Main <assembler file> <width> "+
      "<height> <delay>"
    );
    final String filename = args[0];
    final int width = Integer.parseInt(args[1]);
    final int height = Integer.parseInt(args[2]);
    final int delay = Integer.parseInt(args[3]);
    final Screen screen = new Screen(width, height, delay);
    // Construct an initial InterpreterState.
    final FileReader in = new FileReader(args[0]);
    Assembler me = null;
    try {
      me = new Assembler(in);
    } catch (SyntaxException e) {
      System.out.println(e.getMessage());
      return;
    } finally {
      in.close();
    }
    final InterpreterState state = new InterpreterState(
      me.getInstructions(),
      me.getGlobalValues(),
      screen
    );
    state.frame = new InterpreterState.Call(
      state.frame, me.main.startPC, me.main.numLocals, me.main.stackLen
    );
    // Execute instructions.
    screen.display(filename, true);
    try {
      while (true) {
        state.instructions[state.frame.pc++].execute(state);
      }
    } catch (Instruction.EndException e) {
      System.out.println("Normal exit.");
    } catch (RuntimeException e) {
      System.out.println("Execution error:");
      e.printStackTrace();
      System.out.println("This occurred at instruction "+(state.frame.pc-1));
      for (int i=state.frame.pc-6; i<state.frame.pc+5; i++) {
        if (i>=0 && i<state.instructions.length) {
          System.out.println(i+" : "+state.instructions[i]);
        }
      }
    }
  }
}
