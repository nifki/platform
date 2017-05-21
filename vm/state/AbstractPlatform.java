package org.sc3d.apt.crazon.vm.state;

import org.sc3d.apt.crazon.vm.util.*;

import java.io.*;

/** The interface assumed by the "state" package and implemented by the
 * "platform" package. This abstract class encapsulates everything that the
 * Crazon language run-time system needs to know about the IO libraries. All
 * Crazon instructions are implemented on top of this class. */
public abstract class AbstractPlatform implements DeepCopyable {
  /** Constructs an AbstractPlatform given the size of its screen. */
  public AbstractPlatform(int width, int height) {
    this.width = width; this.height = height;
    this.mustQuit = false;
  }
  
  /* New API. */
  
  /** The size of the screen in pixels. */
  public final int width, height;
  
  /** Initially 'false'. The game thread must terminate soon after this flag
   * becomes 'true'. */
  public boolean mustQuit;
  
  /** Returns the current state of the keyboard, in the form of a Value.Tab 
   * whose keys are the names of keys on the keyboard (Value.Strs) and whose
   * values are 'Value.Bool.TRUE' if the key is pressed or 'Value.Bool.FALSE' if
   * it isn't. The table should have entries for at least all the key names
   * defined in class "KeyNames". */
  public abstract Value.Tab getKeys();
  
  /** Constructs a new Value.Pic.
   * @param varName the name of the global variable which will be initialised
   * with this picture. This string will be returned by the picture's
   * 'toString()' method.
   * @param in an InputStream from which to read an image file.
   * @return a fresh Value.Pic, or 'null' if the bytes read could not be
   * interpreted as an image.
   * @throws IOException if 'in' does.
   */
  public abstract Value.Pic newPicture(String varName, InputStream in)
  throws IOException;
  
  /** Renders a frame and blocks until the next frame is due.
   * @param window the "WINDOW" object, from which this method extracts the
   * background colour and the window coordinates.
   * @param sprites the sprites which are visible (in the sense of the
   * "IsVisible" field) and which overlap the window, in decreasing order of
   * depth.
   * @throws CrazonException if any Sprite or the Window has a non-positive
   * width or height.
   */
  public abstract void render(Window window, Sprite[] sprites)
  throws CrazonException;
  
  /** Outputs 'text' to the debug console. */
  public abstract void print(String text);
  
  /** Calls 'this.print(text+"\n")'. */
  public final void println(String text) { this.print(text+'\n'); }
  
  /* Implement things in DeepCopyable. */
  
  /** Copies all state in this AbstractPlatform into a fresh AbstractPlatform.
   * Subclasses must not omit to copy 'mustQuit'. */
  public abstract DeepCopyable deepCopy();
}
