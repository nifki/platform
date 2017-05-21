package org.sc3d.apt.crazon.vm.util;

/** Implemented by objects that provide a 'deepCopy()' method. This is similar
 * to 'clone()' but differs in that it is acceptable for 'deepCopy()' to return
 * 'this' in some cases. */
public interface DeepCopyable {
  /** Returns a deep copy of this object. This method may return 'this' in the
   * case in which it is immutable. */
  public DeepCopyable deepCopy();
}
