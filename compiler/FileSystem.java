package org.sc3d.apt.crazon.compiler;

import java.io.*;

/** The abstraction layer that defines the mapping of wiki names to wiki files.
 * This abstraction layer is designed to support multiple implementations. One
 * implementation will simply map wiki names to filenames. Another might use CVS
 * or a database. */
public abstract class FileSystem {
  /** Returns the wiki text for the specified wiki page. It is the caller's
   * responsibility to call 'close()'.
   * @return a non-null BufferedReader.
   * @throws IOException if page does not exist.
   */
  public abstract BufferedReader getWikiText(String pageName)
  throws IOException;
  
  /** Returns the source code for the specified wiki page. It is the caller's
   * responsibility to call 'close()'.
   * @return a non-null BufferedReader.
   * @throws IOException if page does not exist.
   */
  public abstract BufferedReader getSourceCode(String pageName)
  throws IOException;
  
  /** Returns a resource file attached to a wiki page. It is the caller's
   * responsibility to call 'close()'.
   * @return a non-null BufferedInputStream.
   * @throws IOException if page does not exist.
   */
  public abstract BufferedInputStream getResource(
    String pageName,
    String globalName
  ) throws IOException;
  
  /** Returns the properties file of a wiki page. It is the caller's
   * responsibility to call 'close()'.
   * @return a non-null BufferedInputStream.
   * @throws IOException if page does not exist.
   */
  public abstract BufferedInputStream getProperties(String pageName)
  throws IOException;
  
  // TODO: Create page, rename page, versioning stuff.
  
  /** Set the compiler output for the specified page to the specified jar file,
   * replacing any earlier output (jar file or error report). This is an atomic
   * operation (it is synchronized on this FileSystem object). Subclasses should
   * define the effect of this method on the filesystem if an error occurs. */
  public abstract void putJarFile(String pageName, byte[] jarFile)
  throws IOException;
  
  /** Set the compiler output for the specified page to the specified error
   * report, replacing any earlier output (jar file or error report). The error
   * report should be UTF-8 encoded. This is an atomic operation (it is
   * synchronized on this FileSystem object). Subclasses should define the
   * effect of this method on the filesystem if an error occurs. */
  public abstract void putErrorReport(String pageName, byte[] errorReport)
  throws IOException;
}
