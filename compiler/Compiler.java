package org.sc3d.apt.crazon.compiler;

import java.io.*;
import java.util.*;
import java.util.jar.*;

/** The main program of the command-line version of the compiler. Run this class
 * with two arguments:
 * <ul>
 *  <li> The path to the wiki data. This probably ends "/wiki/".
 *  <li> The name of the main page.
 * </ul>
 * It then analyses the source code of the main page and finds all parts of the
 * wiki that are reachable from it, and all resource files that it could
 * possibly need. It then compiles the source code, and constructs a jar file
 * containing the Crazon assembler file, a 'properties.txt' file, all necessary
 * resource files, and a 'resources.txt' index file. The jar file is written
 * back into the wiki.
 * <p>
 * All accesses to the wiki, including read and write operations, are made
 * through a FileSystem object. When run as an application, an instance of
 * LocalFileSystem is used, which does not provide any opportunity for e.g.
 * version control.
 */ 
public class Compiler {
  /** Constructs a Main that uses the specified FileSystem to access the wiki.
   */
  public Compiler(FileSystem fileSystem, int numErrors) {
    this.fileSystem = fileSystem;
    this.numErrors = numErrors;
  }
  
  /* New API. */
  
  /** The FileSystem object used as an interface to the wiki data structures. */
  public final FileSystem fileSystem;
  
  /** The maximum number of errors to display. */
  public final int numErrors;
  
  /** Compiles the program with main page 'pageName'. The output is either a jar
   * file or an error report. Either way, it writes it to 'fileSystem'. If the
   * compiler is for any reason unable to produce correct output (e.g. it
   * crashes) no output is written, and the previous output survives unchanged.
   * If there is an error while writing (as opposed to calculating) the output,
   * then 'fileSystem' itself defines the behaviour.
   * @return 'true' if the program compiled and the output is a jar file, or
   * 'false' if the output was an error report.
   */
  public boolean compile(String pageName) throws IOException {
    final Program program = new Program(this.fileSystem, pageName);
    if (program.countErrors()!=0) {
      final ByteArrayOutputStream errorReport = new ByteArrayOutputStream();
      final PrintStream ps = new PrintStream(errorReport, false, "UTF-8");
      program.printErrorReport(ps, this.numErrors);
      ps.close();
      this.fileSystem.putErrorReport(pageName, errorReport.toByteArray());
      return false;
    } else {
      final byte[] buffer = new byte[1024]; // For copying files.
      final ByteArrayOutputStream jarFile = new ByteArrayOutputStream();
      final JarOutputStream jos = new JarOutputStream(jarFile);
      {
        // Write the "asm.nfk" file.
        final ByteArrayOutputStream asm = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(asm, false, "UTF-8");
        final Flattener f = new Flattener(program);
        for (String i: f.output) {
          ps.print(i);
          ps.print(";".equals(i) ? '\n' : ' ');
        }
        ps.close();
        jos.putNextEntry(new JarEntry(PREFIX+"asm.nfk"));
        jos.write(asm.toByteArray());
      }
      {
        // Write the "properties.txt" file.
        final InputStream is = this.fileSystem.getProperties(pageName);
        jos.putNextEntry(new JarEntry(PREFIX+"properties.txt"));
        while (true) {
          final int used = is.read(buffer);
          if (used==-1) break;
          jos.write(buffer, 0, used);
        }
        is.close();
      }
      {
        // Write the resource files. Include any resource file that has the same
        // name as a reachable global variable.
        // At the same time, compile the "resources.txt" file.
        final ByteArrayOutputStream res = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(res, false, "UTF-8");
        for (Program.Global g: program.globals) {
          InputStream is = null;
          try {
            is = this.fileSystem.getResource(g.page, g.variable);
          } catch (IOException e) {
            // Assume it is just an ordinary global, not a resource file.
          }
          if (is!=null) {
            final String name = g.page + "_" + g.variable;
            ps.println(name);
            jos.putNextEntry(new JarEntry(PREFIX + name));
            while (true) {
              final int used = is.read(buffer);
              if (used==-1) break;
              jos.write(buffer, 0, used);
            }
            is.close();
          }
        }
        ps.close();
        // Write the "resources.txt" file.
        jos.putNextEntry(new JarEntry(PREFIX+"resources.txt"));
        jos.write(res.toByteArray());
      }
      {
        // The purpose of this index file is to speed up searching of the jar
        // files. It is merely an optimisation; the program works fine without
        // it.
        jos.putNextEntry(new JarEntry("META-INF/INDEX.LIST"));
        jos.write((
          "JarIndex-Version: 1.0\n"+
          "\n"+
          "nifki-lib.jar\n"+
          "org/sc3d/apt/crazon/vm\n"+
          "org/sc3d/apt/crazon/vm/util\n"+
          "org/sc3d/apt/crazon/vm/state\n"+
          "org/sc3d/apt/crazon/vm/op\n"+
          "org/sc3d/apt/crazon/vm/platform\n"
          // No entry for <pageName>.jar to keep the directory hierarchy
          // flexible.
        ).getBytes("UTF-8"));
      }
      {
        // The purpose of this manifest is to allow the game to be run with
        // "java -jar <pageName>.jar". For this to work, <pageName>.jar and
        // nifki-lib.jar must be in the same directory. This manifest file has
        // nothing to do with the applet, which has no such restriction.
        jos.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
        jos.write((
          "Manifest-Version: 1.0\n"+
          "Created-By: 0.1 (Nifki.net)\n"+
          "Main-Class: org.sc3d.apt.crazon.vm.Main\n"+
          "Class-Path: nifki-lib.jar\n"
        ).getBytes("UTF-8"));
      }
      jos.close();
      this.fileSystem.putJarFile(pageName, jarFile.toByteArray());
      return true;
    }
  }
  
  /* Private. */
  
  /** The package name used as a prefix for all data in the jar file. */
  private static final String PREFIX = "org/sc3d/apt/crazon/gamedata/";
  
  /* Main method. */

  public static void main(String[] args) throws IOException {
    if (args.length!=2) throw new IllegalArgumentException(
      "Syntax: java org.sc3d.apt.crazon.compiler.Compiler "+
      "<wiki path> <page name>"
    );
    final Compiler me = new Compiler(
      new LocalFileSystem(new File(args[0])),
      10
    );
    if (me.compile(args[1])) {
      System.out.println("Wrote "+args[0]+"/nifki-out/"+args[1]+".jar");
    } else {
      System.out.println("Wrote "+args[0]+"/nifki-out/"+args[1]+".err");
    }
  }
}
