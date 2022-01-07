
package Chapter3;

import com.jogamp.common.nio.Buffers;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import java.nio.FloatBuffer;
import javax.swing.JFrame;
import static utils.joglutils.readShaderSource;

/**
 *
 * @author kshan
 */
public class Exercise3_1 extends JFrame implements GLEventListener {

  private int rendering_program;
  private int vao[] = new int[1];
  private  GLJPanel myCanvas;
  private float x = 0.0f;
  private float inc = 0.05f;

  public Exercise3_1() {
    setTitle("Chapter3 - exercise1");
    setSize(600, 400);
    setLocation(200, 200);
    myCanvas = new GLJPanel();
    myCanvas.addGLEventListener(this);
    this.add(myCanvas);
    setVisible(true);
    FPSAnimator animtr = new FPSAnimator(myCanvas, 50);
    animtr.start();
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  @Override
  public void display(GLAutoDrawable drawable) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    gl.glUseProgram(rendering_program);
    float bkg[] = {0.0f, 0.0f, 0.0f, 1.0f};
    FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
    gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
    x+=inc;
    int angle_loc = gl.glGetUniformLocation(rendering_program, "angle");
    gl.glProgramUniform1f(rendering_program, angle_loc, x);
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
    String[] vshaderSource = readShaderSource("Chapter3/E3_1vertex.shader");
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

  public static void main(String[] args) {
    new Exercise3_1();
  }
}
