/*
 * 
 */
package Chapter6;

import Math.Sphere;
import graphicslib3D.*;
import java.nio.*;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import static graphicslib3D.GLSLUtils.checkOpenGLError;
import static graphicslib3D.GLSLUtils.printProgramLog;
import static graphicslib3D.GLSLUtils.printShaderLog;
import static utils.Math3Dutils.perspective;
import static utils.joglutils.loadTexture;
import static utils.joglutils.readShaderSource;

/**
 *
 * @author kshan
 */
public class Exercise6_1 extends JFrame implements GLEventListener {

  private GLCanvas myCanvas;
  private int rendering_program;
  private int vao[] = new int[1];
  private int vbo[] = new int[3];
  private float cameraX, cameraY, cameraZ;
  private float cubeLocX, cubeLocY, cubeLocZ;
  private float pyrLocX, pyrLocY, pyrLocZ;
  private GLSLUtils util = new GLSLUtils();
  private Matrix3D pMat;
  private Sphere mySphere;
  private int earthTexture, sunTexture, moonTexture;

  public Exercise6_1() {
    setTitle("Chapter4 - program4");
    setSize(600, 600);
    myCanvas = new GLCanvas();
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
    mySphere = new Sphere(64);
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
    Texture joglBrickTexture = loadTexture("Chapter6/earth.jpg");
    earthTexture = joglBrickTexture.getTextureObject();
    joglBrickTexture = loadTexture("Chapter6/sun.jpg");
    sunTexture = joglBrickTexture.getTextureObject();
    joglBrickTexture = loadTexture("Chapter6/moon.jpg");
    moonTexture = joglBrickTexture.getTextureObject();
  }
// main(), reshape(), and dispose() are are unchanged

  public static void main(String[] args) {
    new Exercise6_1();
  }

  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
    pMat = perspective(100.0f, aspect, 0.1f, 1000.0f);
  }

  public void dispose(GLAutoDrawable drawable) {
  }

  public void display(GLAutoDrawable drawable) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    gl.glClear(GL_DEPTH_BUFFER_BIT);
    float bkg[] = {0.0f, 0.0f, 0.0f, 1.0f};
    FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
    gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
    gl.glUseProgram(rendering_program);
// build view matrix
    MatrixStack mvStack = new MatrixStack(20);
    mvStack.pushMatrix();
    mvStack.translate(-cameraX, -cameraY, -cameraZ);
    double amt = (double) (System.currentTimeMillis()) / 1000.0;
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
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(0);

    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
    gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(1);
    gl.glActiveTexture(GL_TEXTURE0);
    gl.glBindTexture(GL_TEXTURE_2D, sunTexture);
// adjust OpenGL settings and draw model
    gl.glEnable(GL_DEPTH_TEST);
    gl.glDepthFunc(GL_LEQUAL);
    int numVerts = mySphere.getIndices().length;
    gl.glDrawArrays(GL_TRIANGLES, 0, numVerts);

    mvStack.popMatrix();
    mvStack.pushMatrix();
    mvStack.translate(Math.sin(amt) * 4.0f, 0.0f, Math.cos(amt) * 4.0f);
    mvStack.pushMatrix();
    mvStack.rotate((System.currentTimeMillis()) / 10.0, 0.0, 1.0, 0.0);
    mvStack.scale(0.75, 0.75, 0.75);
    gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(0);
    gl.glBindTexture(GL_TEXTURE_2D, earthTexture);
    gl.glDrawArrays(GL_TRIANGLES, 0, numVerts);
    mvStack.popMatrix();
    mvStack.pushMatrix();
    mvStack.translate(0.0f, Math.sin(amt) * 2.0f, Math.cos(amt) * 2.0f);
    mvStack.rotate((System.currentTimeMillis()) / 10.0, 0.0, 0.0, 1.0);
    mvStack.scale(0.25, 0.25, 0.25); // make the moon smaller
    gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(0);
    gl.glBindTexture(GL_TEXTURE_2D, moonTexture);
    gl.glDrawArrays(GL_TRIANGLES, 0, numVerts);
    mvStack.popMatrix();
    mvStack.popMatrix();
    mvStack.popMatrix();
    mvStack.popMatrix();
  }

  private void setupVertices() {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    Vertex3D[] vertices = mySphere.getVertices();
    int[] indices = mySphere.getIndices();
    float[] pvalues = new float[indices.length * 3]; // vertex positions
    float[] tvalues = new float[indices.length * 2]; // texture coordinates
    float[] nvalues = new float[indices.length * 3]; // normal vectors
    for (int i = 0; i < indices.length; i++) {
      pvalues[i * 3] = (float) (vertices[indices[i]]).getX();
      pvalues[i * 3 + 1] = (float) (vertices[indices[i]]).getY();
      pvalues[i * 3 + 2] = (float) (vertices[indices[i]]).getZ();
      tvalues[i * 2] = (float) (vertices[indices[i]]).getS();
      tvalues[i * 2 + 1] = (float) (vertices[indices[i]]).getT();
      nvalues[i * 3] = (float) (vertices[indices[i]]).getNormalX();
      nvalues[i * 3 + 1] = (float) (vertices[indices[i]]).getNormalY();
      nvalues[i * 3 + 2] = (float) (vertices[indices[i]]).getNormalZ();
    }
    gl.glGenVertexArrays(vao.length, vao, 0);
    gl.glBindVertexArray(vao[0]);
    gl.glGenBuffers(3, vbo, 0);
// put the vertices into buffer #0
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
    gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf,
            GL_STATIC_DRAW);
// put the texture coordinates into buffer #1
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
    FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
    gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);
// put the normal coordinates into buffer #2
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
    FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
    gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit() * 4, norBuf, GL_STATIC_DRAW);
  }

  private int createShaderProgram() {

    int[] vertCompiled = new int[1];
    int[] fragCompiled = new int[1];
    int[] linked = new int[1];
    GL4 gl = (GL4) GLContext.getCurrentGL();
    String[] vshaderSource = readShaderSource("Chapter5/P5_1vertex.shader");
    String[] fshaderSource = readShaderSource("Chapter5/P5_1frag.shader");
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
