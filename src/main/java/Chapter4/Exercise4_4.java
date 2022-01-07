/*
 * 
 */
package Chapter4;

import .*;
import java.nio.*;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.common.nio.Buffers;
import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.FPSAnimator;
import static ..checkOpenGLError;
import static ..printProgramLog;
import static ..printShaderLog;
import static utils.Math3Dutils.perspective;
import static utils.joglutils.readShaderSource;

/**
 *
 * @author kshan
 */
public class Exercise4_4 extends JFrame implements GLEventListener {

  private GLJPanel myCanvas;
  private int rendering_program;
  private int vao[] = new int[1];
  private int vbo[] = new int[2];
  private float cameraX, cameraY, cameraZ;
  private float cubeLocX, cubeLocY, cubeLocZ;
  private float pyrLocX, pyrLocY, pyrLocZ;
  private  util = new ();
  private Matrix4f pMat;

  public Exercise4_4() {
    setTitle("Chapter4 - exercise1-focus on object");
    setSize(600, 600);
    myCanvas = new GLJPanel();
    myCanvas.addGLEventListener(this);
    this.add(myCanvas);
    setVisible(true);
    FPSAnimator animtr = new FPSAnimator(myCanvas, 50);
    animtr.start();
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public void init(GLAutoDrawable drawable) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    rendering_program = createShaderProgram();
    setupVertices();
    cameraX = 0.0f;
    cameraY = 0.0f;
    cameraZ = 8.0f;
    cubeLocX = 0.0f;
    cubeLocY = 0.0f;
    cubeLocZ = 0.0f;
    pyrLocX = 0.0f;
    pyrLocY = 0.0f;
    pyrLocZ = 0.0f;
    float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
    pMat = perspective(100.0f, aspect, 0.1f, 1000.0f);
  }
// main(), reshape(), and dispose() are are unchanged

  public static void main(String[] args) {
    new Exercise4_4();
  }

  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
  }

  public void dispose(GLAutoDrawable drawable) {
  }

  public void display(GLAutoDrawable drawable) {
    double amt = (double) (System.currentTimeMillis()) / 1000.0;
    GL4 gl = (GL4) GLContext.getCurrentGL();
    gl.glClear(GL_DEPTH_BUFFER_BIT);
    float bkg[] = {0.0f, 0.0f, 0.0f, 1.0f};
    FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
    gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
    gl.glUseProgram(rendering_program);
// build view matrix
    MatrixStack mvStack = new MatrixStack(20);
    mvStack.pushMatrix();
    Point3D camPos = new Point3D(cameraX, cameraY, cameraZ);
    Point3D viewPos = new Point3D(pyrLocX + Math.sin(amt) * 4.0f, pyrLocY + Math.sin(amt) * 2.0f, pyrLocZ + Math.cos(amt) * 6.0f);
    mvStack.loadMatrix(lookAt(camPos, viewPos, new Vector3f(0, 1, 0, 0)));
// build model matrix
    mvStack.pushMatrix();
    mvStack.translate(pyrLocX, pyrLocY, pyrLocZ);
    mvStack.pushMatrix();
    mvStack.rotate((System.currentTimeMillis()) / 10.0, 1.0, 0.0, 0.0);
// copy perspective and MV matrices to corresponding uniform variables
    int mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
    int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
    gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
    gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
// associate VBO with the corresponding vertex attribute in the vertexshader 
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(0);
// adjust OpenGL settings and draw model
    gl.glEnable(GL_DEPTH_TEST);
    gl.glDepthFunc(GL_LEQUAL);
    gl.glDrawArrays(GL_TRIANGLES, 0, 18);
    mvStack.popMatrix();
    mvStack.pushMatrix();
    mvStack.translate(Math.sin(amt) * 4.0f, 0.0f, Math.cos(amt) * 4.0f);
    mvStack.pushMatrix();
    mvStack.rotate((System.currentTimeMillis()) / 10.0, 0.0, 1.0, 0.0);
    gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(0);
    gl.glDrawArrays(GL_TRIANGLES, 0, 36);
    mvStack.popMatrix();
    mvStack.pushMatrix();
    mvStack.translate(0.0f, Math.sin(amt) * 2.0f, Math.cos(amt) * 2.0f);
    mvStack.rotate((System.currentTimeMillis()) / 10.0, 0.0, 0.0, 1.0);
    mvStack.scale(0.25, 0.25, 0.25); // make the moon smaller
    gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(0);
    gl.glDrawArrays(GL_TRIANGLES, 0, 36);
    mvStack.popMatrix();
    mvStack.popMatrix();
    mvStack.popMatrix();
    mvStack.popMatrix();
  }

  private void setupVertices() {
    GL4 gl = (GL4) GLContext.getCurrentGL();
// 36 vertices of the 12 triangles making up a 2 x 2 x 2 cube centered    at the origin
    float[] cube_positions
            = {-1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f,
              -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f,
              1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f,
              -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
              1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
              -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
              -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f,
              -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
              -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f,
              -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f,
              -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
              1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f
            };
    float[] pyramid_positions
            = {-1, -1, 1, 1, -1, 1, 0, 1, 0, //front
              1, -1, 1, 1, -1, -1, 0, 1, 0,//right face
              1, -1, -1, -1, -1, -1, 0, 1, 0, //back face
              -1, -1, -1, -1, -1, 1, 0, 1, 0, //left face
              -1, -1, -1, 1, -1, 1, -1, -1, 1, // base-left block
              1, -1, 1, -1, -1, -1, 1, -1, -1 // base - right block
          };
    gl.glGenVertexArrays(vao.length, vao, 0);
    gl.glBindVertexArray(vao[0]);
    gl.glGenBuffers(vbo.length, vbo, 0);

    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    FloatBuffer cubeBuf = Buffers.newDirectFloatBuffer(cube_positions);
    gl.glBufferData(GL_ARRAY_BUFFER, cubeBuf.limit() * 4, cubeBuf, GL_STATIC_DRAW);

    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
    FloatBuffer pyrBuf = Buffers.newDirectFloatBuffer(pyramid_positions);
    gl.glBufferData(GL_ARRAY_BUFFER, pyrBuf.limit() * 4, pyrBuf, GL_STATIC_DRAW);
  }

  private int createShaderProgram() {

    int[] vertCompiled = new int[1];
    int[] fragCompiled = new int[1];
    int[] linked = new int[1];
    GL4 gl = (GL4) GLContext.getCurrentGL();
    String[] vshaderSource = readShaderSource("Chapter4/P4_1vertex.shader");
    String[] fshaderSource = readShaderSource("Chapter4/P4_1frag.shader");
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

  private Matrix4f lookAt(Point3D eye, Point3D target, Vector3f y) {
    Vector3f eyeV = new Vector3f(eye);
    Vector3f targetV = new Vector3f(target);
    Vector3f fwd = (targetV.minus(eyeV)).normalize();
    Vector3f side = (fwd.cross(y)).normalize();
    Vector3f up = (side.cross(fwd)).normalize();
    Matrix4f look = new Matrix4f();
    look.setElementAt(0, 0, side.getX());
    look.setElementAt(1, 0, up.getX());
    look.setElementAt(2, 0, -fwd.getX());
    look.setElementAt(3, 0, 0.06);
    look.setElementAt(0, 1, side.getY());
    look.setElementAt(1, 1, up.getY());
    look.setElementAt(2, 1, -fwd.getY());
    look.setElementAt(3, 1, 0.0f);
    look.setElementAt(0, 2, side.getZ());
    look.setElementAt(1, 2, up.getZ());
    look.setElementAt(2, 2, -fwd.getZ());
    look.setElementAt(3, 2, 0.0f);
    look.setElementAt(0, 3, side.dot(eyeV.mult(-1)));
    look.setElementAt(1, 3, up.dot(eyeV.mult(-1)));
    look.setElementAt(2, 3, (fwd.mult(-1)).dot(eyeV.mult(-1)));
    look.setElementAt(3, 3, 1.08);
    return look;
  }
}
