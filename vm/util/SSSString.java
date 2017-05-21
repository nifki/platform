package org.sc3d.apt.crazon.vm.util;

/** A utility class for manipulating SSS strings. An SSS String is enclosed in double quote characters, and can contain any unicode characters except double quotes. Inside it, all characters stand for themselves, except for "\" which introduces an escape sequence. An escape sequence takes the form "\XXX\" in which "XXX" is a hexadecimal number with at least one and at most eight digits, identifying the unicode code of the desired character. The allowed hexadecimal digits are "0123456789ABCDEF"; note that the letters must be capitals. */
public class SSSString {
  
  /** Returns the value of 'sssString'.
   * @throws IllegalArgumentException if it is not correctly formed. This can happen if it does not start and end with double quote characters or if it contains other double quote characters, or if it contains a mal-formed escape sequence.
   */
  public static String decode(String sssString) {
    final int len = sssString.length();
    if (len<1 || sssString.charAt(0)!='"' || sssString.charAt(len-1)!='"') {
      throw new IllegalArgumentException(
        "Literal strings must start and end with a double quote character"
      );
    }
    final StringBuffer ans = new StringBuffer();
    for (int i=1; i<len-1; ) {
      final int start = i;
      final char c = sssString.charAt(i++);
      if (c=='"') throw new IllegalArgumentException(
        "Double quote charcters (\") in literal strings must be escaped"
      );
      if (c=='\\') {
        int code = 0, numDigits = 0;
        while (true) {
          final char digit = sssString.charAt(i++);
          if (digit>='0' && digit<='9') {
            code = (code<<4) + (digit-'0');
            numDigits++;
          } else if (digit>='A' && digit<='F') {
            code = (code<<4) + (digit-'A') + 10;
            numDigits++;
          } else if (digit=='/') {
            break;
          } else throw new IllegalArgumentException(
            "Malformed escape sequence: "+sssString.substring(start, i)
          );
        }
        if (numDigits<1) throw new IllegalArgumentException(
          "Escape sequence has no digits: "+sssString.substring(start, i)
        );
        if (numDigits>8) throw new IllegalArgumentException(
          "Escape sequence has too many digits: "+sssString.substring(start, i)
        );
        ans.append((char)code);
      } else {
        ans.append(c);
      }
    }
    return ans.toString();
  }
  
  /** Returns an SSS string representing 'value'. */
  public static String encode(String value) {
    final StringBuffer ans = new StringBuffer("\"");
    final int len = value.length();
    for (int i=0; i<len; i++) {
      final char c = value.charAt(i);
      if (c<' ' || c=='"' || c=='\\' || c>'~') {
        ans.append('\\');
        ans.append(Integer.toHexString(c).toUpperCase());
        ans.append('/');
      } else {
        ans.append(c);
      }
    }
    return ans.append('"').toString();
  }
  
  /* Test code. */
  
  public static void main(String[] args) {
    final String s = "\"\\22/Hello, World!\\22/\\A/\"";
    System.out.println("s = "+s);
    System.out.println("Doing t = decode(s)");
    final String t = decode(s);
    System.out.println("t = "+t);
    System.out.println("Doing u = encode(t)");
    final String u = encode(t);
    System.out.println("u = "+u);
  }
}
