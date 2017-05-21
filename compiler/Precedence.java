package org.sc3d.apt.crazon.compiler;

/** Represents the state of a Precedence parser. This parser copes with
 * expressions that combine atoms using prefix, infix and postfix operators. The
 * caller is assumed to have worked out already how to break the expression into
 * atoms and operators. For example, you could use this class from the
 * 'postProcess()' method of an SSS Parser.

 * <p>This parser treats all atoms and operators as abstract Objects. Subclasses
 * must implement the 'getXXXPrecedence()' methods to define the precedence of
 * the operators, and must override the 'getAssociativity()' method to define
 * their associativity (left or right). The parse-tree is returned as a tree of
 * Precedence.Nodes.

 * <p>Precedence levels are numbered from '0' to 'numLevels-1'. Operators with
 * higher precedence bind their operands before operators with lower precedence.
 * For example, if '*' has a precedence of '1' and '+' has a precedence of '0'
 * then '1+2*3' means '1+(2*3)', but if the precedences are reversed it means
 * '(1+2)*3'.
 */
public abstract class Precedence<AtomT, PrefixT, InfixT, PostfixT, ResultT> {
  /** Starts parsing a new expression. The first atom and the prefix and postfix
   * operators that surround it must be passed to the constructor. The rest of
   * the expression must be passed to the 'add()' method.
   * @param prefixes an array containing the prefix operators immediately before
   * the first atom, in left-to-right order.
   * @param atom the first atom.
   * @param postfixes an array containing the postfix operators immediately
   * after the first atom, in left-to-right order.
   */
  public Precedence(PrefixT[] prefixes, AtomT atom, PostfixT[] postfixes) {
    this.stack = new Stack(null, null, prefixes, this.makeAtom(atom));
    this.addPostfixes(postfixes);
  }
  
  /* New API. */
  
  /** Adds one more atom to the expression, along with the prefix and postfix
   * operators that surround it, and the infix operator between it and the
   * previous atom.
   * @param infix the infix operator.
   * @param prefixes same as for the constructor.
   * @param atom same as for the constructor.
   * @param postfixes same as for the constructor.
   */
  public void add(
    InfixT infix,
    PrefixT[] prefixes, AtomT atom, PostfixT[] postfixes
  ) {
    this.collapse(this.getInfixPrecedence(infix));
    this.stack = new Stack(this.stack, infix, prefixes, this.makeAtom(atom));
    this.addPostfixes(postfixes);
  }
  
  /** Returns the parse-tree of the expression. Do not call 'add()' any more
   * after calling this method. */
  public ResultT finish() {
    this.collapse(Integer.MIN_VALUE);
    return this.stack.e;
  }
  
  /** Subclasses must override this method to return a ResultT that represents
   * 'atom'. */
  public abstract ResultT makeAtom(AtomT atom);
  
  /** Subclasses must override this method to return a ResultT that represents
   * 'prefix' applied to 'term'. */
  public abstract ResultT makePrefix(PrefixT prefix, ResultT term);
  
  /** Subclasses must override this method to return a ResultT that represents
   * 'infix' applied to 'term1' and 'term2'. */
  public abstract ResultT makeInfix(ResultT term1, InfixT infix, ResultT term2);
  
  /** Subclasses must override this method to return a ResultT that represents
   * 'postfix' applied to 'term'. */
  public abstract ResultT makePostfix(ResultT term, PostfixT postfix);
  
  /** Subclasses must override this method to return the precedence of the
   * operator 'prefix'. */
  public abstract int getPrefixPrecedence(PrefixT prefix);
  
  /** Subclasses must override this method to return the precedence of the 
   * operator 'infix'. */
  public abstract int getInfixPrecedence(InfixT infix);
  
  /** Subclasses must override this method to return the precedence of the
   * operator 'postfix'. */
  public abstract int getPostfixPrecedence(PostfixT postfix);
  
  /** Subclasses must override this method to define the associativity of the
   * operators with precedence 'level'. This parser cannot cope with operators
   * whose left- and right-precedences are different.
   * @param precedence the precedence level. All operators with the same
   * precedence must have the same associativity.
   * @return 'true' if the operators with the given precedence are
   * left-associative, otherwise 'false'.
   */
  public abstract boolean isLeftAssociative(int level);
  
  /* Private. */
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** Represents a linked list, each element of which represents a complete
   * sub-tree and some unused operators. */
  private class Stack {
    /** Constructs a Stack given values for its fields. */
    public Stack(Stack previous, InfixT infix, PrefixT[] prefixes, ResultT e) {
      this.previous = previous; this.infix = infix;
      this.infixPrecedence = infix==null
        ? Integer.MIN_VALUE
        : Precedence.this.getInfixPrecedence(infix);
      this.prefixes = prefixes; this.numPrefixes = prefixes.length;
      this.prefixPrecedences = new int[this.numPrefixes];
      for (int i=0; i<this.numPrefixes; i++) {
        this.prefixPrecedences[i] =
         Precedence.this.getPrefixPrecedence(prefixes[i]);
      }
      this.e = e;
    }
  
    /** The previous item in the stack, or 'null' if this is the first item. */
    public final Stack previous;
    
    /** The infix operator between this and the previous item in the stack, or
     * 'null' if this is the first item in the stack. */
    public final InfixT infix;
    
    /** The precedence of 'infix'. */
    public final int infixPrecedence;
    
    /** The prefix operators. Elements '0' to 'numPrefixes' are the unused ones.
     */
    public final PrefixT[] prefixes;
    
    /** The precedences of the prefix operators in 'prefixes'. */
    public final int[] prefixPrecedences;
    
    /** The number of unused prefix operators in 'prefixes'. */
    public int numPrefixes;
    
    /** The complete sub-tree. */
    public ResultT e;
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  /** The parser state. */
  private Stack stack;
  
  /** Tests which of two operators wins the precedence battle. */
  private boolean beats(int leftPrecedence, int rightPrecedence) {
    if (leftPrecedence>rightPrecedence) return true;
    if (leftPrecedence<rightPrecedence) return false;
    return this.isLeftAssociative(leftPrecedence);
  }
    
  /** Uses a prefix operator. */
  private void usePrefix() {
    this.stack.e = this.makePrefix(
      this.stack.prefixes[--this.stack.numPrefixes],
      this.stack.e
    );
  }

  /** Uses the infix operator. */
  private void useInfix() {
    if (this.stack.numPrefixes>0) throw new IllegalArgumentException();
    this.stack.previous.e = this.makeInfix(
      this.stack.previous.e,
      this.stack.infix,
      this.stack.e
    );
    this.stack = this.stack.previous;
  }
  
  /** Uses the specified postfix operator. */
  private void usePostfix(PostfixT postfix) {
    this.stack.e = this.makePostfix(this.stack.e, postfix);
  }
  
  private void addPostfixes(PostfixT[] postfixes) {
    for (int i=0; i<postfixes.length; i++) {
      final PostfixT postfix = postfixes[i];
      this.collapse(this.getPostfixPrecedence(postfix));
      this.usePostfix(postfix);
    }
  }

  /** Calls 'usePrefix()' and 'useInfix()' as many times as is appropriate,
   * given that the next operator (postfix or infix) has a precedence of
   * 'rightPrecedence'. */
  private void collapse(int rightPrecedence) {
    while (true) {
      while (this.stack.numPrefixes>0) {
        if (!beats(
          this.stack.prefixPrecedences[this.stack.numPrefixes-1],
          rightPrecedence
        )) return;
        this.usePrefix();
      }
      if (this.stack.previous==null) return;
      if (!beats(
        this.stack.infixPrecedence,
        rightPrecedence
      )) return;
      this.useInfix();
    }
  }
  
  /* Test code. */
  
  public static void main(String[] args) {
    final String[] empty = new String[0];
    final Precedence<String, String, String, String, String> me =
      new Precedence<String, String, String, String, String>(empty, "5", empty)
    {
      public String makeAtom(String atom) { return atom; }
      public String makePrefix(String prefix, String term) {
        return prefix+"("+term+")";
      }
      public String makeInfix(String term1, String infix, String term2) {
        return "("+term1+")"+infix+"("+term2+")";
      }
      public String makePostfix(String term, String postfix) {
        return "("+term+")"+postfix;
      }
      public int getPrefixPrecedence(String op) { return 2; }
      public int getInfixPrecedence(String op) {
        if (op=="+") return 0;
        if (op=="*") return 1;
        throw new IllegalArgumentException(op);
      }
      public int getPostfixPrecedence(String op) { return 2; }
      public boolean isLeftAssociative(int precedence) { return true; }
    };
    me.add("+", new String[] {"-", "-"}, "7", empty);
    me.add("*", empty, "9", new String[] {"!", "!"});
    me.add("+", empty, "3", empty);
    System.out.println(me.finish());
  }
}
