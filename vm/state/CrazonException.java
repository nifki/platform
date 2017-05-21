package org.sc3d.apt.crazon.vm.state;

/** Thrown to report an error in a Crazon program. */
public class CrazonException extends Exception {
  public CrazonException() { super(); }
  public CrazonException(String msg) { super(msg); }
}
