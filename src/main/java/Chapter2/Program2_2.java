
package Chapter2;

import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JFrame;

/**
 *
 * @author kshan
 */
public class Program2_2 extends JFrame implements GLEventListener {

  private int rendering_program;
  private int vao[] = new int[1];
  private final GLJPanel myCanvas;

  public Program2_2() {
    setTitle("Chapter2 - program2");
    setSize(600, 400);
    setLocation(200, 200);
    myCanvas = new GLJPanel();
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
    GL4 gl = (GL4) GLContext.getCurrentGL();
    String vshaderSource[]
            = {"#version 430 \n",
              "void main(void) \n",
              "{ gl_Position = vec4(0.0, 0.0, 0.0, 1.0); } \n",};
    String fshaderSource[]
            = {"#version 430 \n",
              "out vec4 color; \n",
              "void main(void) \n",
              "{ color = vec4(0.0, 0.0, 1.0, 1.0); } \n",};
    int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
    gl.glShaderSource(vShader, 3, vshaderSource, null, 0);
    gl.glCompileShader(vShader);
    int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
    gl.glShaderSource(fShader, 4, fshaderSource, null, 0);
    gl.glCompileShader(fShader);
    int vfprogram = gl.glCreateProgram();
    gl.glAttachShader(vfprogram, vShader);
    gl.glAttachShader(vfprogram, fShader);
    gl.glLinkProgram(vfprogram);
    gl.glDeleteShader(vShader);
    gl.glDeleteShader(fShader);
    return vfprogram;
  }

  public static void main(String[] args) {
    new Program2_2();
  }
}
