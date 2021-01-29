
package Chapter2;

import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import java.io.InputStream;
import java.util.Scanner;
import java.util.Vector;
import javax.swing.JFrame;

/**
 *
 * @author kshan
 */
public class Program2_5 extends JFrame implements GLEventListener {

  private int rendering_program;
  private int vao[] = new int[1];
  private  GLCanvas myCanvas;

  public Program2_5() {
    setTitle("Chapter2 - program5");
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
    gl.glDrawArrays(GL_TRIANGLES, 0, 3);
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
    GL4 gl = (GL4) GLContext.getCurrentGL();
    String[] vshaderSource = readShaderSource("Chapter2/vertex2_5.shader");
    String[] fshaderSource = readShaderSource("CHapter2/frag2_4.shader");
    int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
    gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
    gl.glCompileShader(vShader);
    int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
    gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);
    gl.glCompileShader(fShader);
    int vfprogram = gl.glCreateProgram();
    gl.glAttachShader(vfprogram, vShader);
    gl.glAttachShader(vfprogram, fShader);
    gl.glLinkProgram(vfprogram);
    gl.glDeleteShader(vShader);
    gl.glDeleteShader(fShader);
    return vfprogram;
  }

  private String[] readShaderSource(String filename) {

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

  private InputStream getFileFromResourceAsStream(String fileName) {

    // The class loader that loaded the class
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(fileName);

    // the stream holding the file content
    if (inputStream == null) {
      return null;
    } else {
      return inputStream;
    }

  }

  public static void main(String[] args) {
    new Program2_5();
  }
}
