package org.sc3d.apt.crazon.vm.util;

/** Represents an immutable map from Comparable keys to Object values. All
 * modifications return a fresh map. 'null' represents the empty map. */
public class Table implements DeepCopyable {
  /** Private constructor: the public construct maps from 'null' by using
   * 'put()' repeatedly. */
  private Table(
    Table left, Table right,
    int depth,
    Comparable key, DeepCopyable value
  ) {
    this.left = left; this.right = right;
    this.depth = depth;
    this.key = key; this.value = value;
    this.size = (left==null?0:left.size) + 1 + (right==null?0:right.size);
    if (left!=null && depth<=left.depth)
      throw new IllegalArgumentException("Left is too deep");
    if (right!=null && depth<right.depth)
      throw new IllegalArgumentException("Right is too deep");
  }
  
  /* New API. */
  
  /** Returns the number of key, value pairs in 't'. */
  public static int size(Table t) { return t==null ? 0 : t.size; }
  
  /** Returns the value to which 't' maps 'k', or 'null' if there is none. */
  public static DeepCopyable get(Table t, Comparable k) {
    while (t!=null) {
      final int c = k.compareTo(t.key);
      if (c==0) return t.value;
      t = c<0 ? t.left : t.right;
    }
    return null;
  }
  
  /** Returns a new Table that differs from 't' in that it maps 'k' to 'v'. */
  public static Table put(Table t, Comparable k, DeepCopyable v) {
    if (t==null) return new Table(null, null, RAND.nextInt(), k, v);
    final int c = k.compareTo(t.key);
    if (c==0) return new Table(t.left, t.right, t.depth, k, v);
    if (c<0) {
      final Table u = put(t.left, k, v);
      if (u.depth<t.depth) {
        return new Table(u, t.right, t.depth, t.key, t.value);
      } else {
        t = new Table(u.right, t.right, t.depth, t.key, t.value);
        return new Table(u.left, t, u.depth, u.key, u.value);
      }
    } else {
      final Table u = put(t.right, k, v);
      if (t.depth<u.depth) {
        t = new Table(t.left, u.left, t.depth, t.key, t.value);
        return new Table(t, u.right, u.depth, u.key, u.value);
      } else {
        return new Table(t.left, u, t.depth, t.key, t.value);
      }
    }
  }
  
  /** Returns a deep copy of this Table, by calling the 'deepCopy()' method of
   * all of the values. If none returns a new object, this method returns
   * 'this', otherwise it returns a fresh Table. */
  public Table deepCopyTable() {
    final Table newLeft = this.left==null ? null : this.left.deepCopyTable();
    final Table newRight = this.right==null ? null : this.right.deepCopyTable();
    final DeepCopyable newValue =
      this.value==null ? null : this.value.deepCopy();
    if (this.left==newLeft && this.right==newRight && this.value==newValue)
      return this;
    return new Table(newLeft, newRight, this.depth, this.key, newValue);
  }
  
  /** Returns an Iterator whose 'next()' method, called repeatedly, returns the
   * values in this Table sorted according to their keys. */
  public static Iterator iterator(Table t) {
    Iterator ans = null;
    for (; t!=null; t = t.left)
      ans = new Iterator(t.key, t.value, t.right, ans);
    return ans;
  }
  
  /** Returns a String representation of this Table, including its keys and
   * values, mainly for debugging purposes. */
  public String toString() {
    StringBuffer sb = new StringBuffer("Table[size="+size(this));
    for (Iterator it = iterator(this); it!=null; it = it.next())
      sb.append("\n  ").append(it.key).append(" -> ").append(it.value);
    return sb.append("\n]").toString();
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** The return type of 'iterator()', which represents a sequence of key, value
   * pairs from a Table. 'null' represents the empty sequence. Instances are
   * immutable. */
  public static final class Iterator {
    /** Private constructor: the public uses 'iterator()'. Constructs an
     * Iterator given values for its fields. */
    private Iterator(
      Comparable key, DeepCopyable value,
      Table right, Iterator next
    ) {
      this.key = key; this.value = value;
      this.right = right; this.next = next;
    }
    
    /** The first key in the sequence. */
    public final Comparable key;
    
    /** The first value in the sequence. */
    public final DeepCopyable value;
    
    /** Returns an Iterator representing the sequence that remains after
     * removing the first element from this Iterator. */
    public Iterator next() {
      Iterator ans = this.next;
      for (Table t = this.right; t!=null; t = t.left)
        ans = new Iterator(t.key, t.value, t.right, ans);
      return ans;
    }
    
    /** Returns a String representation of this Iterator, including 'key' and
     * 'value' but not the rest of the sequence, mainly for debugging purposes.
     */
    public String toString() {
      return "Iterator[key="+this.key+", value="+this.value+"]";
    }
    
    /** A Table to loop through. */
    private final Table right;
    
    /** An Iterator to return after 'right' is exhausted. */
    private final Iterator next;
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /* Implement things in DeepCopyable. */
  
  /** Returns 'this.deepCopyTable()'. */
  public final DeepCopyable deepCopy() { return this.deepCopyTable(); }
  
  /* Private. */
  
  private final Table left, right;
  
  /** Depth of 'null' considered to be 'Integer.MIN_VALUE'.
   * Invariant: 'depth>left.depth && depth<=right.depth'. */
  private final int depth;
  
  /** Invariant: 'key' is larger than all 'key's in 'left' and smaller than all
   * those in 'right'. */
  private final Comparable key;
  
  /** The Object to which 'key' maps. */
  private final DeepCopyable value;
  
  /** The number of key, value pairs in this Table.
   * Size of 'null' considered to be '0'.
   * Invariant: 'size == left.size + 1 + right.size'. */
  private final int size;
  
  /** Used to choose the depths of new nodes. */
  private static final java.util.Random RAND = new java.util.Random();
  
  /* Test code. */
  
  private static class DeepCopyableString implements DeepCopyable {
    public DeepCopyableString(String v) { this.v = v; }
    public final String v;
    public final DeepCopyable deepCopy() { return this; }
    public String toString() { return this.v; }
  }
  
  public static void main(String[] args) {
    Table t = null;
    for (int i=0; i<100; i++) t = put(
      t,
      new Integer(RAND.nextInt(100)),
      new DeepCopyableString("v"+RAND.nextInt(100))
    );
    System.out.println(t);
    Table u = t;
    for (int i=0; i<100; i++) u = put(
      u,
      new Integer(RAND.nextInt(100)),
      new DeepCopyableString("v"+RAND.nextInt(100))
    );
    System.out.println(u);
    System.out.println(t);
  }
}
