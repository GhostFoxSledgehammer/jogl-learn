
package Chapter2;

import static com.jogamp.opengl.GL.GL_NO_ERROR;
import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_INFO_LOG_LENGTH;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import javax.swing.JFrame;

/**
 *
 * @author kshan
 */
public class Program2_3 extends JFrame implements GLEventListener {

  private int rendering_program;
  private int vao[] = new int[1];
  private final GLCanvas myCanvas;

  public Program2_3() {
    setTitle("Chapter2 - program3");
    setSize(600, 400);
    setLocation(200, 200);
    myCanvas = new GLCanvas();
    myCanvas.addGLEventListener(this);
    this.add(myCanvas);
    setVisible(true);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  @Override
  public void display(GLAutoDrawable drawable) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    gl.glUseProgram(rendering_program);
    gl.glPointSize(150);
    gl.glDrawArrays(GL_POINTS, 0, 1);
  }

  @Override
  public void init(GLAutoDrawable drawable) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    rendering_program = createShaderProgram();
    gl.glGenVertexArrays(vao.length, vao, 0);
    gl.glBindVertexArray(vao[0]);
  }

  @Override
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
  }

  @Override
  public void dispose(GLAutoDrawable drawable) {
  }

  private int createShaderProgram() {
    int[] vertCompiled = new int[1];
    int[] fragCompiled = new int[1];
    int[] linked = new int[1];
    GL4 gl = (GL4) GLContext.getCurrentGL();
    String vshaderSource[]
            = {"#version 430 \n",
              "void main(void) \n",
              "{ gl_Position = vec4(0.0, 0.0, 0.0, 1.0); } \n",};
    String fshaderSource[]
            = {"#version 430 \n",
              "out vec4 color; \n",
              "void main(void) \n",
              "{ if (gl_FragCoord.x < 200) color = vec4(1.0, 0.0, 0.0, 1.0); else color = vec4(0.0, 0.0, 1.0, 1.0);\n",
              "}\n"};

    int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
    gl.glShaderSource(vShader, 3, vshaderSource, null, 0);
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
    gl.glShaderSource(fShader, 5, fshaderSource, null, 0);
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

  private void printShaderLog(int shader) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    int[] len = new int[1];
    int[] chWrittn = new int[1];
    byte[] log = null;
    // determine the length of the shader compilation log
    gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, len, 0);
    if (len[0] > 0) {
      log = new byte[len[0]];
      gl.glGetShaderInfoLog(shader, len[0], chWrittn, 0, log, 0);
      System.out.println("Shader Info Log: ");
      for (int i = 0; i < log.length; i++) {
        System.out.print((char) log[i]);
      }
    }
  }

  void printProgramLog(int prog) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    int[] len = new int[1];
    int[] chWrittn = new int[1];
    byte[] log = null;
// determine the length of the program linking log
    gl.glGetProgramiv(prog, GL_INFO_LOG_LENGTH, len, 0);
    if (len[0] > 0) {
      log = new byte[len[0]];
      gl.glGetProgramInfoLog(prog, len[0], chWrittn, 0, log, 0);
      System.out.println("Program Info Log: ");
      for (int i = 0; i < log.length; i++) {
        System.out.print((char) log[i]);
      }
    }
  }

  boolean checkOpenGLError() {
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

  public static void main(String[] args) {
    new Program2_3();
  }
}
