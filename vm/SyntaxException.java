package org.sc3d.apt.crazon.vm;

import org.sc3d.apt.crazon.vm.state.*;

/** Thrown by the assembler when it detects a syntactic or structural error. */
public class SyntaxException extends CrazonException {
  public SyntaxException() { super(); }
  public SyntaxException(String msg) { super(msg); }
}
