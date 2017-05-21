package org.sc3d.apt.crazon.vm;

import org.sc3d.apt.crazon.vm.util.*;
import org.sc3d.apt.crazon.vm.state.*;
import org.sc3d.apt.crazon.vm.platform.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/** The main program. */
public class NifkiApplet extends Applet implements Runnable {
  public NifkiApplet() {
    try {
      this.properties = GameEngine.getProperties();
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }
  
  /* New API. */
  
  /** The game's Properties object. */
  public final GameEngine.Properties properties;

  /* Implement things in Runnable. */
  
  public void run() {
    try {
      GameEngine.run(this.platform);
      this.platform.println("Normal exit.");
    } catch (CrazonRuntimeException e) {
      this.platform.debugWindow.setVisible(true);
      this.platform.println("Execution error:");
      this.platform.println(e.getMessage());
      final InterpreterState state = e.state;
      this.platform.println("This occurred at instruction "+(state.frame.pc-1));
      for (int i=state.frame.pc-6; i<state.frame.pc+5; i++) {
        if (i>=0 && i<state.instructions.length) {
          this.platform.println(i+" : "+state.instructions[i]);
        }
      }
    } catch (CrazonException e) {
      this.platform.debugWindow.setVisible(true);
      this.platform.println(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* Override things in Applet. */
  
  public String getAppletInfo() {
    return this.properties.name+"\nProduced with Nifki.net";
  }
  
  public void init() {}
  
  /** Constructs a Platform and a Thread and starts the game. */
  public void start() {
    this.platform = new Platform(
      this.getWidth(),
      this.getHeight(),
      this.properties.msPerFrame
    );
    this.setLayout(new BorderLayout());
    this.add(this.platform.cc, BorderLayout.CENTER);
    this.validate();
    this.platform.debugWindow.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        platform.debugWindow.setVisible(false);
      }
    });
    if (this.properties.debug) this.platform.debugWindow.setVisible(true);
    this.platform.cc.requestFocus(); // FIXME: Broken if (properties.debug).
    this.thread = new Thread(this);
    this.thread.start();
  }
  
  /** Stops the game, waits for the Thread to terminate, and frees the Platform.
   */
  public void stop() {
    this.platform.mustQuit = true;
    try {
      this.thread.join();
    } catch (InterruptedException e) {}
    this.remove(this.platform.cc);
    this.platform = null; this.thread = null;
  }
  
  public void destroy() {}
  
  /* Private. */
  
  /** The Platform object we are using, or 'null' if the game is not running. */
  private Platform platform = null;
  
  /** The game Thread we are using, or 'null'. */
  private Thread thread = null;
}
