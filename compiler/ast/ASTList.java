package org.sc3d.apt.crazon.compiler.ast;

import java.util.*;
import org.sc3d.apt.sss.v3.*;

/** Represents a list of ASTNodes. Provides facilities for incremental
 * construction, error collation, and pretty printing. A finished ASTNode is
 * immutable.
 * <p>
 * The 'toSourceCode()' method is moderately flexible. The list can be
 * enclosed in any kind of brackets and it is possible to define a separator
 * string if required. These printing options are specified at construction
 * time. */
public class ASTList<E extends ASTNode> extends ASTNode {
  /** Constructs an ASTList given values for its fields.
   * @param length the number of elements that will eventually be in this list.
   * @param brackets a two-character String giving the open and close bracket
   * characters, or 'null' if this ASTList should not be printed in brackets.
   * @param separator the list separator. 'null' means a newline with
   * appropriate indentation. Do not use a newline character, otherwise the
   * indentation will go wrong.
   */
  public ASTList(int length, String brackets, String separator) {
    if (brackets!=null && brackets.length()!=2)
      throw new IllegalArgumentException("Bad bracket characters");
    this.length = length;
    this.brackets = brackets;
    this.separator = separator;
    this.elements = (E[])new ASTNode[length];
    this.used = 0;
    this.isFixed = false;
  }

  /** Constructs an ASTList that is not enclosed in brackets.
   * @param length the number of elements that will eventually be in this list.
   * @param separator the list separator. 'null' means the empty string.
   */
  public ASTList(int length, String separator) {
    this(length, null, separator);
  }
  
  /* New API. */
  
  /** The number of elements in this ASTList. */
  public final int length;
  
  /** Returns the element with index 'index'.
   * @throws IllegalStateException if this method is called before 'fix()'.
   */
  public final E get(int index) {
    if (!this.isFixed) throw new IllegalStateException("Need to call 'fix()'");
    return this.elements[index];
  }
  
  /** For incremental construction: call this method to add the elements of this
   * ASTList in order.
   * @param toAdd the element to add, or 'null' if there is a parsing error.
   * @throws IllegalStateException if this method is called after 'fix()'.
   */
  public final void add(E toAdd) {
    if (this.isFixed) throw new IllegalStateException("Already called 'fix()'");
    this.elements[this.used++] = toAdd;
  }
  
  /** Checks that all elements have been added, and returns 'this' if they are
   * all non-null, or 'null' if at least one is 'null'.
   * @throws IllegalStateException if 'length' differs from the number of times
   * 'add()' has been called before this method.
   */
  public final ASTList<E> fix() {
    if (this.used!=this.length) throw new IllegalStateException(
      "Not enough elements passed to 'add()'"
    );
    this.isFixed = true;
    for (int i=0; i<this.length; i++) if (this.get(i)==null) return null;
    return this;
  }
  
  // Can we hook in to the nice Java 1.5 for(:) syntax? Something to do with
  // Iterator.
  
  /** A two-character String giving the open and close bracket characters, or
   * 'null' if this ASTList should not be printed in brackets. */
  public final String brackets;
  
  /** The separator string. 'null' indicates that the elements of this list
   * should be separated by newlines and appropriate indentation. */
  public final String separator;
  
  /* Override things in ASTNode. */
  
  public void toSourceCode(String prefix, StringBuffer sb) {
    String newPrefix = prefix, sep = this.separator;
    if (this.brackets!=null) {
      newPrefix += "  ";
      sb.append(this.brackets.charAt(0));
    }
    if (this.separator==null) {
      sep = newPrefix;
    }
    for (int i=0; i<this.length; i++) {
      if (i==0) {
        if (this.brackets!=null || this.separator==null) sb.append(newPrefix);
      } else {
        sb.append(sep);
      }
      this.get(i).toSourceCode(newPrefix, sb);
    }
    if (this.brackets!=null) {
      if (this.length>0) sb.append(prefix);
      sb.append(this.brackets.charAt(1));
    }
  }
  
  public void findAssignments(
    HashMap<String, Token> locals,
    HashMap<String, Token> globals
  ) {
    if (!this.isFixed) throw new IllegalStateException("Need to call 'fix()'");
    for (int i=this.length-1; i>=0; i--) {
      this.get(i).findAssignments(locals, globals);
    }
  }
  
  /* Private. */
  
  /** The contents of this ASTList. */
  private final E[] elements;
  
  /** The number of calls to 'add()' so far. */
  private int used;
  
  /** 'true' after calling 'fix()', and 'false' before. */
  private boolean isFixed;
}
