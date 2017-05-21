package org.sc3d.apt.crazon.vm;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;
import org.sc3d.apt.crazon.vm.platform.*;

import java.awt.*;
import java.awt.event.*;
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
    if (args.length>3 || args.length==1) throw new IllegalArgumentException(
      "Syntax: java org.sc3d.apt.crazon.Main <width> <height> <delay>"
    );
    
    final GameEngine.Properties props = GameEngine.getProperties();
    final int width =
      args.length>0 ? Integer.parseInt(args[0]) : props.width;
    final int height =
      args.length>1 ? Integer.parseInt(args[1]) : props.height;
    final int msPerFrame =
      args.length>2 ? Integer.parseInt(args[2]) : props.msPerFrame;
    final boolean debug =
      args.length>3 ? parseBoolean(args[3]) : props.debug;
    // Bootstrap and run.
    final Platform platform = new Platform(width, height, msPerFrame);
    final Frame frame = platform.cc.display(props.name);
    final WindowListener closeListener = new WindowAdapter() {
      public void windowClosing(WindowEvent e) { platform.mustQuit = true; }
    };
    if (debug) platform.debugWindow.setVisible(true);
    frame.addWindowListener(closeListener);
    platform.debugWindow.addWindowListener(closeListener);
    try {
      GameEngine.run(platform);
      platform.println("Normal exit.");
    } catch (CrazonRuntimeException e) {
      platform.debugWindow.setVisible(true);
      platform.println("Execution error:");
      platform.println(e.getMessage());
      final InterpreterState state = e.state;
      platform.println("This occurred at instruction "+(state.frame.pc-1));
      for (int i=state.frame.pc-6; i<state.frame.pc+5; i++) {
        if (i>=0 && i<state.instructions.length) {
          platform.println(i+" : "+state.instructions[i]);
        }
      }
    } catch (CrazonException e) {
      platform.debugWindow.setVisible(true);
      platform.println(e.getMessage());
    }
    System.exit(0);
  }
  
  /* Private. */
  
  /** Returns 'true' given "true" and 'false' given "false". Otherwise throws an
   * IllegalArgumentException. */
  private static boolean parseBoolean(String bool) {
    if ("true".equals(bool)) return true;
    if ("false".equals(bool)) return false;
    throw new IllegalArgumentException("'"+bool+"' is not a boolean value");
  }
}
