package org.sc3d.apt.crazon.compiler;

import java.io.*;

/** An implementation of FileSystem on top of the local filesystem. */
public class LocalFileSystem extends FileSystem {
  public LocalFileSystem(File pathToWiki) {
    this.path = pathToWiki;
  }
  
  /* Override things in FileSystem. */

  /** Returns the contents of the file "pageName/wiki.txt", in which "/" stands
   * for the platform's file separator character. The file is converted to
   * characters using the "UTF-8" encoding. */
  public BufferedReader getWikiText(String pageName) throws IOException {
    final File page = new File(this.path, pageName);
    final File file = new File(page, "wiki.txt");
    return new BufferedReader(
      new InputStreamReader(new FileInputStream(file), "UTF-8")
    );
  }
  
  /** Returns the contents of the file "pageName/source.sss", in which "/"
   * stands for the platform's file separator character. The file is converted
   * to characters using the "UTF-8" encoding. */
  public BufferedReader getSourceCode(String pageName) throws IOException {
    final File page = new File(this.path, pageName);
    final File file = new File(page, "source.sss");
    return new BufferedReader(
      new InputStreamReader(new FileInputStream(file), "UTF-8")
    );
  }
  
  /** Returns the contents of the file "pageName/res/globalName", in which "/"
   * stands for the platform's file separator character. */
  public BufferedInputStream getResource(String pageName, String globalName)
  throws IOException {
    final File page = new File(this.path, pageName);
    final File resources = new File(page, "res");
    final File file = new File(resources, globalName);
    return new BufferedInputStream(new FileInputStream(file));
  }
  
  /** Returns the contents of the file "pageName/properties.txt", in which "/"
   * stands for the platform's file separator character. */
  public BufferedInputStream getProperties(String pageName)
  throws IOException {
    final File page = new File(this.path, pageName);
    final File file = new File(page, "properties.txt");
    return new BufferedInputStream(new FileInputStream(file));
  }
  
  /** Deletes whichever of "out/pageName.err" and "out/pageName.jar" exists and
   * then writes the specified bytes to "out/pageName.jar". If an error occurs,
   * deletes any unfinished file, then throws an IOException. */
  public synchronized void putJarFile(String pageName, byte[] jarFile)
  throws IOException {
    this.putOutput(pageName, ".jar", jarFile);
  }
  
  /** Deletes whichever of "out/pageName.err" and "pageName/pageName.jar" exists
   * and then writes the specified bytes to "out/pageName.err". If an error
   * occurs, deletes any unfinished file, then throws an IOException. */
  public synchronized void putErrorReport(String pageName, byte[] errorReport)
  throws IOException {
    this.putOutput(pageName, ".err", errorReport);
  }
  
  /* Private.*/
  
  private final File path;
  
  private void putOutput(String pageName, String ext, byte[] data)
  throws IOException {
    final File outDir = new File(this.path, "nifki-out");
    deleteIfThere(new File(outDir, pageName+".err"));
    deleteIfThere(new File(outDir, pageName+".jar"));
    final File outFile = new File(outDir, pageName+ext);
    OutputStream out = null;
    try {
      out = new BufferedOutputStream(new FileOutputStream(outFile));
      out.write(data);
      out.close();
    } catch (IOException e) {
      if (out!=null) out.close();
      deleteIfThere(outFile);
      throw e;
    }
  }
  
  private static void deleteIfThere(File file) throws IOException {
    if (file.exists()) file.delete();
  }
}
