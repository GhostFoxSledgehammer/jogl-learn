/*
 * 
 */
package utils;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_NO_ERROR;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_INFO_LOG_LENGTH;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import java.util.Scanner;
import java.util.Vector;
import static utils.IOutils.getBufferedImage;
import static utils.IOutils.getFileFromResource;
import static utils.IOutils.getFileFromResourceAsStream;

import static utils.imageutils.getRGBAPixelData;

/**
 *
 * @author kshan
 */
public class joglutils {

  public static int loadTextureID(String textureFileName) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    BufferedImage textureImage = getBufferedImage(textureFileName);
    byte[] imgRGBA = getRGBAPixelData(textureImage);
    ByteBuffer rgbaBuffer = Buffers.newDirectByteBuffer(imgRGBA);
    int[] textureIDs = new int[1]; // array to hold generated texture IDs
    gl.glGenTextures(1, textureIDs, 0);
    int textureID = textureIDs[0]; // ID for the 0th texture object
    gl.glBindTexture(GL_TEXTURE_2D, textureID); // specifies the active 2D texture
    gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, // MIPMAP level, color space
    textureImage.getWidth(), textureImage.getHeight(), 0, // image size, border (ignored)
    GL_RGBA, GL_UNSIGNED_BYTE, // pixel format and data type 
    rgbaBuffer
    ); // buffer holding texture data
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    return textureID;
  }

  public static Texture loadTexture(String textureFileName) {
    Texture tex = null;
    try {
      tex = TextureIO.newTexture(getFileFromResource(textureFileName), false);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return tex;
  }

  public static String[] readShaderSource(String filename) {

    Vector<String> lines = new Vector<String>();
    Scanner sc;
    sc = new Scanner(getFileFromResourceAsStream(filename));
    while (sc.hasNext()) {
      lines.addElement(sc.nextLine());
    }
    String[] program = new String[lines.size()];
    for (int i = 0; i < lines.size(); i++) {
      program[i] = (String) lines.elementAt(i) + "\n";
    }
    return program;
  }

  public static boolean checkOpenGLError() {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    boolean foundError = false;
    GLU glu = new GLU();
    int glErr = gl.glGetError();
    while (glErr != GL_NO_ERROR) {
      System.err.println("glError: " + glu.gluErrorString(glErr));
      foundError = true;
      glErr = gl.glGetError();
    }
    return foundError;
  }

  public static void printShaderLog(int shader) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    int[] len = new int[1];
    int[] chWrittn = new int[1];
    byte[] log = null;
    // determine the length of the shader compilation log
    gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, len, 0);
    if (len[0] > 0) {
      log = new byte[len[0]];
      gl.glGetShaderInfoLog(shader, len[0], chWrittn, 0, log, 0);
      System.err.println("Shader Info Log: ");
      for (int i = 0; i < log.length; i++) {
        System.err.print((char) log[i]);
      }
    }
  }
  
  public static void printProgramLog(int prog) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    int[] len = new int[1];
    int[] chWrittn = new int[1];
    byte[] log = null;
// determine the length of the program linking log
    gl.glGetProgramiv(prog, GL_INFO_LOG_LENGTH, len, 0);
    if (len[0] > 0) {
      log = new byte[len[0]];
      gl.glGetProgramInfoLog(prog, len[0], chWrittn, 0, log, 0);
      System.err.println("Program Info Log: ");
      for (int i = 0; i < log.length; i++) {
        System.err.print((char) log[i]);
      }
    }
  }
  

  public static int createShaderProgram(String vPath, String fPath) {

    int[] vertCompiled = new int[1];
    int[] fragCompiled = new int[1];
    int[] linked = new int[1];
    GL4 gl = (GL4) GLContext.getCurrentGL();
    String[] vshaderSource = readShaderSource(vPath);
    String[] fshaderSource = readShaderSource(fPath);
    int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
    gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
    gl.glCompileShader(vShader);
    checkOpenGLError(); // can use returned boolean
    gl.glGetShaderiv(vShader, GL_COMPILE_STATUS, vertCompiled, 0);
    if (vertCompiled[0] == 1) {
      System.out.println("vShader vertex compilation success.");
    } else {
      System.out.println("vShader vertex compilation failed.");
      printShaderLog(vShader);
    }

    int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
    gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);
    gl.glCompileShader(fShader);
    checkOpenGLError(); // can use returned boolean
    gl.glGetShaderiv(fShader, GL_COMPILE_STATUS, fragCompiled, 0);
    if (fragCompiled[0] == 1) {
      System.out.println("fShader fragment compilation success.");
    } else {
      System.out.println("fShader fragment compilation failed.");
      printShaderLog(fShader);
    }

    if ((vertCompiled[0] != 1) || (fragCompiled[0] != 1)) {
      System.out.println("\nCompilation error; return-flags:");
      System.out.println(" vertCompiled = " + vertCompiled[0]
      + "fragCompiled =  " + fragCompiled[0]);
    } else {
      System.out.println("Successful compilation");
    }

    int vfprogram = gl.glCreateProgram();
    gl.glAttachShader(vfprogram, vShader);
    gl.glAttachShader(vfprogram, fShader);
    gl.glLinkProgram(vfprogram);
    gl.glLinkProgram(vfprogram);

    checkOpenGLError();
    gl.glGetProgramiv(vfprogram, GL_LINK_STATUS, linked, 0);
    if (linked[0] == 1) {
      System.out.println("vfprogram linking succeeded.");
    } else {
      System.out.println("vfprogram linking failed.");
      printProgramLog(vfprogram);
    }

    gl.glDeleteShader(vShader);
    gl.glDeleteShader(fShader);
    return vfprogram;
  }
}
