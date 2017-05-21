package org.sc3d.apt.crazon.vm;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;

import java.applet.*;
import java.awt.*;
import java.io.*;

public class VMApplet extends Applet implements Runnable {

  /* Implement things in Runnable. */
  
  public void run() {
    // Construct an initial InterpreterState.
    Assembler assembler = null;
    try {
      final Reader in = new InputStreamReader(
        VMApplet.class.getResourceAsStream("/examples/maze.crz")
      );
      assembler = new Assembler(in);
      in.close();
    } catch (SyntaxException e) {
      System.out.println(e.getMessage());
      return;
    } catch (IOException e) {
      System.out.println(e.getMessage());
      return;
    }
    final InterpreterState state = new InterpreterState(
      assembler.getInstructions(),
      assembler.getGlobalValues(),
      this.screen
    );
    state.frame = new InterpreterState.Call(
      state.frame,
      assembler.main.startPC,
      assembler.main.numLocals,
      assembler.main.stackLen
    );
    // Execute instructions.
    try {
      while (this.thread!=null) {
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
  
  /* Override things in Applet. */
  
  public void init() {
    // Read parameters.
    final Dimension dim = this.getSize();
    final int frameTime = Integer.parseInt(this.getParameter("frameTime"));
    this.screen = new Screen(dim.width, dim.height, frameTime);
    // Do the Component stuff.
    this.setLayout(new BorderLayout());
    this.add(this.screen, BorderLayout.CENTER);
    this.screen.requestFocus();
    // Start the game thread.
    this.thread = new Thread(this);
    this.thread.start();
  }
  
  public void destroy() {
    this.thread = null;
  }
  
  /* Private. */
  
  private Thread thread = null;
  private Screen screen = null;
}
