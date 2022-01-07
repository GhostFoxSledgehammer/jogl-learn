/*
 * 
 */
package Chapter6;

import Math.Torus;
import java.nio.*;
import .*;
import javax.swing.*;

import static com.jogamp.opengl.GL4.*;
import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static ..checkOpenGLError;
import static ..printProgramLog;
import static ..printShaderLog;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.*;
import static utils.Math3Dutils.perspective;
import static utils.joglutils.loadTexture;
import static utils.joglutils.readShaderSource;

/**
 *
 * @author kshan
 */
public class Program6_2 extends JFrame implements GLEventListener {

  private final GLJPanel myCanvas;
  private int rendering_program;
  private final int vao[] = new int[1];
  private final int vbo[] = new int[4];
  private float cameraX, cameraY, cameraZ;
  private float cubeLocX, cubeLocY, cubeLocZ;
  private  util = new ();
  private Matrix4f pMat;
  private int brickTexture;
  private Torus myTorus;

  public Program6_2() {
    setTitle("Chapter5 - program1");
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
    myTorus = new Torus(0.5f, 0.2f, 48);
    setupVertices();
    cameraX = 0.0f;
    cameraY = 0.0f;
    cameraZ = 2.0f;
    cubeLocX = 0.0f;
    cubeLocY = 0.0f;
    cubeLocZ = 0.0f;
    float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
    pMat = perspective(70.0f, aspect, 0.1f, 1000.0f);
    Texture joglBrickTexture = loadTexture("Chapter5/brick.jpg");
    brickTexture = joglBrickTexture.getTextureObject();
  }
// main(), reshape(), and dispose() are are unchanged

  public static void main(String[] args) {
    new Program6_2();
  }

  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
    pMat = perspective(70.0f, aspect, 0.1f, 1000.0f);
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
    //gl.glEnable(GL_CULL_FACE);
// build view matrix
    Matrix4f vMat = new Matrix4f();
    vMat.translate(-cameraX, -cameraY, -cameraZ);
// build model matrix
    Matrix4f mMat = new Matrix4f();
    double x = (double) (System.currentTimeMillis()) / 10000.0;
    mMat.translate(cubeLocX, cubeLocY, cubeLocZ);
    mMat.rotate(500 * x, 1000 * x, 0);
// concatenate model and view matrix to create MV matrix
    Matrix4f mvMat = new Matrix4f();
    mvMat.concatenate(vMat);
    mvMat.concatenate(mMat);
// copy perspective and MV matrices to corresponding uniform variables
    int mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
    int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
    gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
    gl.glUniformMatrix4fv(mv_loc, 1, false, mvMat.getFloatValues(), 0);
// associate VBO with the corresponding vertex attribute in the vertexshader 
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    gl.glEnableVertexAttribArray(0);
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

    // activate buffer #1, which contains the texture coordinates
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
    gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(1);
// activate texture unit #0 and bind it to the brick texture object
    gl.glActiveTexture(GL_TEXTURE0);
    gl.glBindTexture(GL_TEXTURE_2D, brickTexture);
    gl.glEnable(GL_DEPTH_TEST);
    gl.glDepthFunc(GL_LEQUAL);
    int numIndices = myTorus.getIndices().length;
    gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3]);
    gl.glDrawElements(GL_TRIANGLES, numIndices, GL_UNSIGNED_INT, 0);
  }

  private void setupVertices() {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    Vertex3D[] vertices = myTorus.getVertices();
    int[] indices = myTorus.getIndices();
    float[] pvalues = new float[vertices.length * 3];
    float[] tvalues = new float[vertices.length * 2];
    float[] nvalues = new float[vertices.length * 3];
    for (int i = 0; i < vertices.length; i++) {
      pvalues[i * 3] = (float) (vertices[i]).getX(); // vertex position 
      pvalues[i * 3 + 1] = (float) (vertices[i]).getY();
      pvalues[i * 3 + 2] = (float) (vertices[i]).getZ();
      tvalues[i * 2] = (float) (vertices[i]).getS(); // texture coordinates 
      tvalues[i * 2 + 1] = (float) (vertices[i]).getT();
      nvalues[i * 3] = (float) (vertices[i]).getNormalX(); // normal vector
      nvalues[i * 3 + 1] = (float) (vertices[i]).getNormalY();
      nvalues[i * 3 + 2] = (float) (vertices[i]).getNormalZ();
    }
    gl.glGenVertexArrays(vao.length, vao, 0);
    gl.glBindVertexArray(vao[0]);
    gl.glGenBuffers(vbo.length, vbo, 0); // generate VBOs as before, plus one for indices 
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); // vertex positions 
    FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
    gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]); // texture coordinates 
    FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
    gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]); // normal vectors
    FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
    gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit() * 4, norBuf, GL_STATIC_DRAW);
    gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3]); // indices
    IntBuffer idxBuf = Buffers.newDirectIntBuffer(indices);
    gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuf.limit() * 4, idxBuf, GL_STATIC_DRAW);
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
