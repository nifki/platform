package org.sc3d.apt.crazon.vm;

import org.sc3d.apt.crazon.vm.state.*;

import java.io.*;
import java.util.*;

/** This is the main class of the platform-independent part of Crazon. The game
 * is loaded from a fixed location on the classpath. All the necessary files
 * have the prefix "org.sc3d.apt.crazon.gamedata.". There are three fixed files:
 * <ul>
 *  <li>properties.txt - A properties file giving, for example, the name of the
 *      game, the width, height and frame rate.
 *  <li>asm.nfk - The assembler file to run.
 *  <li>resources.txt - A file listing the global variable names of all pictures
 *      needed by the game, with one picture per line.
 * </ul>
 * In addition, for every line in resources.txt there must be a resource file of
 * the same name as the global variable. This file must be a PNG or JPEG file.
 */
public class GameEngine {
  /** This class is not instantiable. */
  private GameEngine() { }
  
  /** Returns the properties of the game, as set by the author(s) of the game,
   * from properties.txt. */
  public static Properties getProperties() throws IOException {
    final InputStream is = openInputStream("properties.txt");
    final Reader reader = new InputStreamReader(is, "UTF-8");
    final BufferedReader br = new BufferedReader(reader);
    final Properties ans = new Properties(
      parseProperty("name", readStrippedLine(br)),
      Integer.parseInt(parseProperty("width", readStrippedLine(br))),
      Integer.parseInt(parseProperty("height", readStrippedLine(br))),
      Integer.parseInt(parseProperty("msPerFrame", readStrippedLine(br))),
      parseBoolean(parseProperty("debug", readStrippedLine(br)))
    );
    if (readStrippedLine(br)!=null)
      throw new IOException("Unexpected text at end of properties file");
    br.close();
    is.close();
    return ans;
  }
  
  /** Runs the game using 'platform' as the IO library.
   * @throws CrazonException if a Crazon run-time error occurs.
   */
  public static void run(AbstractPlatform platform)
  throws CrazonException, IOException {
    Assembler assembler = null;
    final InputStream asm = openInputStream("asm.nfk");
    final Reader asmReader = new InputStreamReader(asm, "UTF-8");
    final BufferedReader in = new BufferedReader(asmReader);
    try {
      assembler = new Assembler(in);
    } catch (SyntaxException e) {
      System.out.println(e.getMessage());
      return;
    } finally {
      in.close();
      asm.close();
    }
    // Initialize the state.
    final InterpreterState state = new InterpreterState(
      assembler.getInstructions(),
      assembler.getGlobalValues(),
      assembler.getGlobalNames(),
      platform
    );
    state.frame = new InterpreterState.Call(
      state.frame,
      assembler.main.startPC,
      assembler.main.numLocals,
      assembler.main.stackLen
    );
    // Parse resources.txt and construct the picture object globals.
    final Map nameToNum = assembler.getGlobalMappings();
    final InputStream res = openInputStream("resources.txt");
    final Reader resReader = new InputStreamReader(res, "UTF-8");
    final BufferedReader br = new BufferedReader(resReader);
    while (true) {
      final String var = readStrippedLine(br);
      if (var==null) break;
      final InputStream picData = openInputStream(var);
      final Value.Pic picture = platform.newPicture(var, picData);
      if (picture==null)
        throw new CrazonException("Couldn't load picture '"+var+"'");
      picData.close();
      final Integer varNum = (Integer)nameToNum.get(var);
      if (varNum==null) {
        throw new CrazonException(
          "The picture '"+var+"' appears in resources.txt, but is not used"
        );
      } else {
        if (state.globals[varNum.intValue()]!=null)
          throw new CrazonException(
            "Global variable '"+var+"' is initialized more than once"
          );
        state.globals[varNum.intValue()] = picture;
      }
    }
    br.close();
    res.close();
    // Execute instructions.
    try {
      while (!platform.mustQuit) {
        state.instructions[state.frame.pc++].execute(state);
      }
    } catch (Instruction.EndException e) {}
  }
  
  /////////////////////////////////////////////////////////////////////////
  
  /** The return type of 'getProperties()'. */
  public static class Properties {
    /** Constructs a Properties, given values for its fields. */
    public Properties(
      String name,
      int width, int height,
      int msPerFrame,
      boolean debug
    ) {
      this.name = name;
      this.width = width;
      this.height = height;
      this.msPerFrame = msPerFrame;
      this.debug = debug;
    }
    
    /** The name of the game. */
    public final String name;
    
    /** The designed width of the game in pixels. This value is only a hint
     * and may be ignored. */
    public final int width;

    /** The designed height of the game in pixels. This value is only a hint
     * and may be ignored. */
    public final int height;
    
    /** The designed frame rate of the game, expressed as the number of
     * milliseconds per frame. This value is only a hint and may be ignored. */
    public final int msPerFrame;
    
    /** 'true' if the debug output should be shown. */
    public final boolean debug;
  }

  /////////////////////////////////////////////////////////////////////////
  
  /* Private. */
  
  /** Opens a file in the Java classpath with the prefix
   * "org.sc3d.apt.crazon.gamedata." and returns its contents. */
  private static InputStream openInputStream(String filename)
  throws IOException {
    final String path = "org/sc3d/apt/crazon/gamedata/"+filename;
    final InputStream ans =
      GameEngine.class.getClassLoader().getResourceAsStream(path);
    if (ans==null) throw new IOException("Couldn't open '"+path+"'");
    return ans;
  }
  
  /** Reads lines from 'br'. Strips comments (starting '#') and leading and
   * trailing whitespace. Returns the first non-blank line. Returns null if end
   * of file is reached. */
  private static String readStrippedLine(BufferedReader br)
  throws IOException {
    while (true) {
      String line = br.readLine();
      if (line==null) return null;
      final int pos = line.indexOf('#');
      if (pos>=0) line = line.substring(0, pos);
      line = line.trim();
      if (!"".equals(line)) return line;
    }
  }
  
  /** Returns the text following the colon in a line of properties.txt. Checks
   * that the format is "XXX : YYY" where "XXX" matches 'propertyName' and
   * returns "YYY". Whitespace immediately before and after the colon is
   * ignored. */
  private static String parseProperty(String propertyName, String line)
  throws IOException {
    if (line==null) throw new IOException(
      "Corrupt properties file. Expected a line beginning '" + propertyName +
      "' but found end of file"
    );
    if (!line.startsWith(propertyName)) throw new IOException(
      "Corrupt properties file. Expected a line beginning '" + propertyName +
      "' but found:\n" + line
    );
    line = line.substring(propertyName.length()).trim();
    if (line.charAt(0)!=':') throw new IOException(
      "Corrupt properties file. Expected a colon after '" + propertyName +
      "' but found:\n" + line
    );
    return line.substring(1).trim();
  }
  
  /** Returns 'true' given "true" and 'false' given "false". Otherwise throws an
   * IllegalArgumentException. */
  private static boolean parseBoolean(String bool) {
    if ("true".equals(bool)) return true;
    if ("false".equals(bool)) return false;
    throw new IllegalArgumentException("'"+bool+"' is not a boolean value");
  }
}
