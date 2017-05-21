package org.sc3d.apt.crazon.vm.state;

/** Thrown by the run-time when it detects an error in a running Crazon program.
 * The field 'state' contains the state of the virtual machine at the moment of
 * the error and can be used to extract diagnostic information. */
public class CrazonRuntimeException extends CrazonException {
  public CrazonRuntimeException(InterpreterState state) {
    super();
    this.state = state;
  }
  
  public CrazonRuntimeException(String msg, InterpreterState state) {
    super(msg);
    this.state = state;
  }
  
  /* New API. */
  
  /** The state of the virtual machine at the moment this Exception was thrown.
   */
  public final InterpreterState state;
}
