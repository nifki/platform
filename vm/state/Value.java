package org.sc3d.apt.crazon.vm.state;

import org.sc3d.apt.crazon.vm.util.*;

import java.util.*;

/** Represents a Crazon value. */
public abstract class Value implements Comparable, DeepCopyable {
  /** This is a private constructor. The public must use one of the subclasses.
   * <p> Constructs a Value, given its type.
   * @param type one of the 'TYPE_XXX' values.
   */
  private Value(int type) { this.type = type; }

  /* New API. */

  public final int type;

  /** The value of the 'type' field for booleans. */
  public static final int TYPE_BOOL = 1;

  /** The value of the 'type' field for numbers. */
  public static final int TYPE_NUM = 2;

  /** The value of the 'type' field for strings. */
  public static final int TYPE_STR = 3;

  /** The value of the 'type' field for tables. */
  public static final int TYPE_TAB = 4;

  /** The value of the 'type' field for pictures. */
  public static final int TYPE_PIC = 5;

  /** The value of the 'type' field for functions. */
  public static final int TYPE_FUNC = 6;

  /** The value of the 'type' field for objects. */
  public static final int TYPE_OBJ = 7;
  
  /** Returns a string description of one of the 'TYPE_XXX' values suitable for
   * use in error messages. The string is in lower-case and is singular (as
   * opposed to plural). */
  public static String describeType(int type) {
    switch (type) {
      case TYPE_BOOL: return "boolean";
      case TYPE_NUM: return "number";
      case TYPE_STR: return "string";
      case TYPE_TAB: return "table";
      case TYPE_PIC: return "picture";
      case TYPE_FUNC: return "function";
      case TYPE_OBJ: return "object";
      default: throw new RuntimeException("Impossible");
    }
  }

  /** Returns a String which describes this Value in detail.
   * This String is used by the "DUMP" instruction. Where possible, this is a
   * Crazon expression that evaluates to this Value. The default implementation
   * returns 'this.toString()'.
   */
  public String toLongString() { return this.toString(); }
  
  /** Just like 'DeepCopyable.deepCopy()' but declared to return a Value. */
  public Value deepCopyValue() { return this; }
  
  /* Implement things in Comparable. */

  /** Implements the Crazon "&lt;" operator. If 'this' or 'that' is a function
   * or object, throws a ClassCastException. Otherwise, compares their 'type'
   * fields. If equal, distinguishes cases according to 'type':<ul>
   * <li> 'FALSE' is less than 'TRUE'.
   * <li> numbers have the obvious ordering.
   * <li> strings are ordered ASCII-betically.
   * <li> tables are ordered pointwise, considering undefined values to be
   * smaller than defined values. Keys are considered in sorted order. If any of
   * the values is not comparable, then the tables are not comparable.
   * <li> pictures are ordered ASCII-betically on their original variable names
   * (including the page name and underscore).
   * </ul> */
  public final int compareTo(Object thatObject) {
    final Value that = (Value)thatObject;
    if (this.type>5) throw new ClassCastException(
      describeType(this.type)+" is not a comparable type"
    );
    if (that.type>5) throw new ClassCastException(
      describeType(that.type)+" is not a comparable type"
    );
    if (this.type!=that.type) return this.type-that.type;
    switch (this.type) {
      case TYPE_BOOL:
        return this==that ? 0 : this==Bool.FALSE ? -1 : 1;
      case TYPE_NUM:
        final double v1 = ((Num)this).v, v2 = ((Num)that).v;
        return v1==v2 ? 0 : v1<v2 ? -1 : 1;
      case TYPE_STR:
        return ((Str)this).v.compareTo(((Str)that).v);
      case TYPE_TAB:
        Table.Iterator it1 = Table.iterator(((Tab)this).v);
        Table.Iterator it2 = Table.iterator(((Tab)that).v);
        while (true) {
          if (it1==null) return it2==null ? 0 : -1;
          if (it2==null) return 1;
          int ans = -it1.key.compareTo(it2.key);
          if (ans!=0) return ans;
          ans = ((Value)it1.value).compareTo((Value)it2.value);
          if (ans!=0) return ans;
          it1 = it1.next(); it2 = it2.next();
        }
      case TYPE_PIC:
        return ((Pic)this).originalName.compareTo(((Pic)that).originalName);
      default: throw new RuntimeException("Impossible");
    }
  }
  
  /* Implement things in DeepCopyable. */

  /** Returns 'this.deepCopyValue()'. */
  public final DeepCopyable deepCopy() { return this.deepCopyValue(); }
  
  /* Override things in Object. */

  /** Returns a short String representation of this Value suitable for use in
   * error messages. */
  public abstract String toString();

  /** Returns a hashcode that is compatible with 'equals()'. The default
   * implementation returns 'Object.hashCode()'. */
  public int hashCode() { return super.hashCode(); }

  /** Returns 'true' iff 'this==that' or 'this.compareTo(that)==0'. Returns
   * 'false' otherwise, and in particular when 'this!=that' and 'compareTo()'
   * fails. */
  public final boolean equals(Object that) {
    if (that==null) return false;
    if (this==that) return true;
    try { return this.compareTo(that)==0; }
    catch (ClassCastException e) { return false; }
  }

  ////////////////////////////////////////////////////////////////////////////

  /** The subclass of Value that represents a boolean. */
  public static final class Bool extends Value {
    /** Private constructor: the only instances of this class are 'TRUE' and
     * 'FALSE'. */
    private Bool() { super(TYPE_BOOL); }

    /* New API. */

    /** The Value.Bool that represents 'TRUE'. */
    public static final Value.Bool TRUE = new Value.Bool();

    /** The Value.Bool that represents 'FALSE'. */
    public static final Value.Bool FALSE = new Value.Bool();

    /* Implement things in Value. */

    public String toString() { return this==TRUE ? "TRUE" : "FALSE"; }
  }

  ////////////////////////////////////////////////////////////////////////////

  /** The subclass of Value that represents a number. */
  public static final class Num extends Value {
    /** Constructs a Num given its value as a double. */
    public Num(double v) {
      super(TYPE_NUM); this.v = v;
      if (Double.isNaN(v) || Double.isInfinite(v))
        throw new IllegalArgumentException(""+v);
    }

    /* NEW API. */

    /** The value of this Num as a double, which is finite and not a NaN. */
    public final double v;

    /** Returns this Value.Num as an integer, or throws IllegalArgumentException
     * if it is not representable as an integer. */
    public int intValue() {
      final int ans = (int)this.v;
      if (this.v!=ans)
        throw new IllegalArgumentException(this+" is not an integer");
      return ans;
    }

    /* Implement things in Value. */

    public String toString() { return ""+this.v; }

    /** Returns the same value as 'Double.hashCode()'. */
    public int hashCode() {
      final long v = Double.doubleToLongBits(this.v);
      return (int)(v^(v>>>32));
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  /** The subclass of Value that represents a string. */
  public static final class Str extends Value {
    /** Constructs a Num given its value as a String. */
    public Str(String v) {
      super(TYPE_STR); this.v = v;
      if (v==null) throw new IllegalArgumentException();
    }

    /* NEW API. */

    /** The value of this Num as a String, which is non-null. */
    public final String v;

    /* Implement things in Value. */

    /** Returns 'v' suitably escaped and wrapped in quotes. */
    public String toString() {
      return SSSString.encode(this.v);
    }

    /** Returns the same value as 'String.hashCode()'. */
    public int hashCode() {
      return this.v.hashCode();
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  /** The subclass of Value that represents a table. */
  public static final class Tab extends Value {
    /** Constructs a Num given its value as a Table. */
    public Tab(Table v) {
      super(TYPE_TAB); this.v = v;
    }

    /* NEW API. */

    /** The value of this Num as a Table. */
    public final Table v;

    /* Implement things in Value. */

    public String toString() {
      return "TABLE("+Table.size(this.v)+" keys)";
    }

    public String toLongString() {
      final StringBuffer ans = new StringBuffer("[");
      String sep = "";
      for (Table.Iterator it = Table.iterator(this.v); it!=null; it=it.next()) {
        ans.append(sep).append(it.key).append("=").append(it.value);
        sep = ", ";
      }
      return ans.append("]").toString();
    }
    
    /** Deep copies all the values in this Tab. If none of them return a new
     * object, returns 'this', otherwise returns a fresh 'Tab'. */
    public Value deepCopyValue() {
      final Table ans = this.v.deepCopyTable();
      return ans==this.v ? this : new Tab(ans);
    }

    /** Returns the same value as the following code:<pre>
     * ans = 0;
     * for (Table.Iterator it = Table.iterator(v); it!=null; it=it.next()) {
     *   ans = 568742265*ans + it.key.hashCode();
     *   ans = 568742265*ans + it.value.hashCode();
     * }
     * return ans;
     * </pre> */
    public int hashCode() {
      return this.v.hashCode();
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  /** The subclass of Value that represents a picture. A Pic is immutable. Some
   * global variables are initialised to Pic objects when the VM starts up. */
  public static abstract class Pic extends Value {
    /** Constructs a Pic, given values for its fields. */
    public Pic(String originalName, int width, int height) {
      super(TYPE_PIC);
      this.originalName = originalName;
      this.width = width; this.height = height;
    }
    
    /* New API. */
    
    /** The name of the global variable which was set to this picture value when
     * the virtual machine started up. All picture values start in global
     * variables. */
    public final String originalName;
    
    /** The size of this Pic in pixels. */
    public final int width, height;

    /* Implement things in Value. */
    
    /** Returns 'originalName'. */
    public String toString() {
      return this.originalName;
    }
    
    /** Returns 'originalName.hashCode()'. */
    public int hashCode() {
      return this.originalName.hashCode();
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  /** The subclass of Value that represents a function. The semantics of calling
   * a function is implemented elsewhere, but is described here in order to
   * explain the meaning of the fields.
   * <p>When the function is called, a new stack frame is constructed with
   * 'numLocals' local variables and a stack with room for 'stackLen' Values.
   * The function arguments, in the form of a single Value.Tab, are placed on
   * the otherwise empty stack. The 'instructions' are then executed until a
   * "RETURN" Instruction is encountered. At this point, the stack must contain
   * a single Value, which is returned to the caller. */
  public static final class Func extends Value {
    /** Constructs a Func given its fields. */
    public Func(int startPC, int numLocals, int stackLen, String originalName) {
      super(TYPE_FUNC);
      this.startPC = startPC;
      this.numLocals = numLocals;
      this.stackLen = stackLen;
      this.originalName = originalName;
    }

    /* New API. */

    /** The value of the program counter at the start of the function body. */
    public final int startPC;

    /** The number of local variables needed to execute 'instructions'. */
    public final int numLocals;

    /** The number of stack positions needed to execute 'instructions'. */
    public final int stackLen;
    
    /** The name under which this function was defined, for the purposes of
     * reporting errors. */
    public final String originalName;

    /* Implement things in Value. */

    /** Returns 'this.originalName'. */
    public String toString() { return this.originalName; }
  }

  ////////////////////////////////////////////////////////////////////////////

  /** The subclass of Value that represents an object. An Obj is passed by
   * reference and which has a fixed set of attributes. The values of the
   * attributes can be changed but not their types. */
  public static abstract class Obj extends Value {
    /** Constructs an Obj given its attributes.
     * @param attributes a Map from attribute names (Strings) to attribute
     * values (Values). The attribute values must be immutable. The caller must
     * not modify the Map hereafter.
     */
    public Obj(Map attributes) {
      super(TYPE_OBJ);
      this.attributes = attributes;
      this.objNum = objCount++;
      // Assertion: none of the values are mutable.
      for (Iterator it = this.attributes.values().iterator(); it.hasNext(); ) {
        switch (((Value)it.next()).type) {
          case TYPE_TAB:
          case TYPE_OBJ:
            throw new IllegalArgumentException("Obj attribute is mutable");
          default:
        }
      }
    }
    
    /** Constructs a deep copy of 'that'. It will have the same 'objNum'.
     * Therefore, a copy of an object constructed in this way should not be put
     * into the same InterpreterState as the original object. This constructor
     * is intended to be used to implement 'deepCopyValue()'. */
    protected Obj(Obj that) {
      super(TYPE_OBJ);
      this.attributes = new HashMap(that.attributes);
      this.objNum = that.objNum;
    }

    /* NEW API. */
    
    /** A unique identifier for this Obj. Objs constructed earlier have
     * smaller identifiers. */
    public final int objNum;
    
    /** Sets the value of an attribute.
     * @param name the name of the attribute.
     * @param v the new Value.
     * @throws CrazonException if the attribute does not exist, or if
     * its old Value does not have the same type as 'v'.
     */
    public final void set(String name, Value v) throws CrazonException {
      final Value old = this.get(name);
      if (old==null || old.type!=v.type)
        throw new CrazonException("SET "+this+"."+name+" = "+v);
      this.attributes.put(name, v);
    }
    
    /** Returns the Value of an attribute, or 'null' if it does not exist. */
    public final Value get(String name) {
      return (Value)this.attributes.get(name);
    }
    
    /** Returns the Value of an attribute as a boolean. */
    public final boolean getBoolean(String name) {
      return ((Value.Bool)this.get(name))==Value.Bool.TRUE;
    }

    /** Returns the Value of an attribute as a double. */
    public final double getDouble(String name) {
      return ((Value.Num)this.get(name)).v;
    }

    /** Returns the Value of an attribute as a String. */
    public final String getString(String name) {
      return ((Value.Str)this.get(name)).v;
    }
    
    /** Returns a String describing the type of this Object. The String is used
     * by 'toString()'. */
    public abstract String getObjType();
    
    /** Returns a String of the form "CLASS:ID(attributes)" where "CLASS" is the
     * String returned by 'getObjType()', "ID" is a unique identifier for this
     * object and "attributes" is a list of attribute names and values. */
    public String toLongString() {
      final StringBuffer ans = new StringBuffer(this.toString());
      ans.append('(');
      String sep = "";
      for (Iterator it = this.attributes.keySet().iterator(); it.hasNext(); ) {
        final String key = (String)it.next();
        final Value value = (Value)this.attributes.get(key);
        ans.append(sep).append(key).append('=').append(value);
        sep = ", ";
      }
      return ans.append(')').toString();
    }
    
    /** Returns a fresh object. Subclasses must implement this in order to
     * return an instance of the correct subclass. The contract is the same as
     * for 'DeepCopyable.deepCopy()'. */
    public abstract Value deepCopyValue();

    /* Override things in Object. */
    
    /** Returns a String of the form "CLASS:ID" where "CLASS" is the String
     * returned by 'getObjType()' and "ID" is a unique identifier for this
     * object. */
    public String toString() {
      return this.getObjType()+':'+this.objNum;
    }
    
    /* Private. */

    /** The Map that stores the attributes. */
    private final Map attributes;
    
    /** The number of Objs constructed so far. */
    private static int objCount = 0;
  }

  ////////////////////////////////////////////////////////////////////////////

  /* Test code. */

  public static void main(String[] args) {
    final Value[] vs = new Value[] {
      Value.Bool.FALSE,
      Value.Bool.TRUE,
      new Value.Num(1.0),
      new Value.Num(2.0),
      new Value.Str("frog"),
      new Value.Str("goose"),
      new Value.Tab(null),
      new Value.Tab(Table.put(null, new Value.Str("frog"), new Value.Num(1.0))),
      new Value.Tab(Table.put(null, new Value.Str("frog"), new Value.Num(2.0))),
      new Value.Tab(Table.put(null, new Value.Str("pig"), new Value.Num(2.0))),
      new Value.Pic("Page_variable", 32, 32) {}
    };
    for (int i=0; i<vs.length; i++) for (int j=0; j<vs.length; j++) {
      System.out.println(vs[i]+".compareTo("+vs[j]+")="+vs[i].compareTo(vs[j]));
    }
  }
}
