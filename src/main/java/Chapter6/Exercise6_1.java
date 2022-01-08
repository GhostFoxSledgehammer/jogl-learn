/*
 * 
 */
package Chapter6;

import Math.Sphere;
import Template.ZoomPanPanel;
import java.nio.*;
import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.common.nio.Buffers;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector2f;
import org.joml.Vector3f;
import static utils.joglutils.checkOpenGLError;
import static utils.joglutils.loadTexture;
import static utils.joglutils.printProgramLog;
import static utils.joglutils.printShaderLog;
import static utils.joglutils.readShaderSource;

/**
 *
 * @author kshan
 */
public class Exercise6_1 extends ZoomPanPanel implements GLEventListener {

  private final FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
  private int rendering_program;
  private final int vao[] = new int[1];
  private final int vbo[] = new int[4];
  private float cameraX, cameraY, cameraZ;
  private float cubeLocX, cubeLocY, cubeLocZ;
  private float pyrLocX, pyrLocY, pyrLocZ;
  private Matrix4fStack mvStack = new Matrix4fStack(5);

  private final Matrix4f mMat = new Matrix4f(); // model matrix
  private final Matrix4f mvMat = new Matrix4f(); // model-view matrix
  private int mvLoc, pLoc;
  private long startTime;
  private int brickTexture;
  private int earthTexture;
  private int moonTexture;
  private int sunTexture;
  private Sphere mySphere;
  private int numSphereVerts;

  public Exercise6_1() {
  }

  @Override
  public void init(GLAutoDrawable drawable) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    rendering_program = createShaderProgram();
    setupVertices();
    cubeLocX = 1.0f;
    cubeLocY = 1.0f;
    cubeLocZ = -8.0f;

    pyrLocX = 20f;
    pyrLocY = 1.0f;
    pyrLocZ = -2.0f;
    vMat.set(camera.getViewMatrix());
    FPSAnimator animtr = new FPSAnimator(this, 50);
    animtr.start();
// shifted down along    the Y    -axis to reveal perspective
    // Create a perspective matrix, this one has fovy=60, aspect ratio    matches screen window.
    // Values for near and far clipping planes can vary as discussed in Section 4.9.
    aspect = (float) getWidth() / (float) getHeight();
    pMat.setPerspective((float) Math.toRadians(fov), aspect, 0.1f, 1000.0f);
    startTime = System.currentTimeMillis();
    Texture joglBrickTexture = loadTexture("Chapter6/earth.jpg");
    earthTexture = joglBrickTexture.getTextureObject();
    Texture joglMoonTexture = loadTexture("Chapter6/moon.jpg");
    moonTexture = joglMoonTexture.getTextureObject();
    Texture joglSunTexture = loadTexture("Chapter6/sun.jpg");
    sunTexture = joglSunTexture.getTextureObject();
  }
// main(), reshape(), and dispose() are are unchanged

  public static void main(String[] args) {
    JFrame jf = new JFrame();
    jf.setTitle("Chapter4 - program1");
    jf.setSize(600, 600);
    jf.add(new Exercise6_1());
    jf.setVisible(true);
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  @Override
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    aspect = (float) width / (float) height;
    pMat.setPerspective((float) Math.toRadians(fov), aspect, 0.1f, 1000.0f);
    drawable.getContext().getGL().glViewport(0, 0, width, height);
  }

  @Override
  public void dispose(GLAutoDrawable drawable) {
  }

  @Override
  public void display(GLAutoDrawable drawable) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    gl.glClear(GL_DEPTH_BUFFER_BIT);
    float bkg[] = {0.0f, 0.0f, 0.0f, 1.0f};
    FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
    gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
    gl.glUseProgram(rendering_program);
// copy perspective and MV matrices to corresponding uniform variables
    mvLoc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
    pLoc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
    gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
// associate VBO with the corresponding vertex attribute in the vertexshader 
    long elapsedTime = System.currentTimeMillis() - startTime;
    double tf = elapsedTime / 1000.0;
    // push view matrix onto the stack
    mvStack.pushMatrix();
    mvStack.mul(vMat);
// ---------------------- pyramid == sun --------------------------------------------
    mvStack.pushMatrix();
    mvStack.translate(cubeLocX, cubeLocY, cubeLocZ); // sun’s position
    mvStack.pushMatrix();
    mvStack.rotate((float) tf, 1.0f, 0.0f, 0.0f); // sun’s rotation on its axis
    gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
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
    gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVerts);
    mvStack.popMatrix(); // remove the sun’s axial rotation from the stack
//----------------------- cube == planet ---------------------------------------------
    mvStack.pushMatrix();
    mvStack.translate((float) Math.sin(tf) * 4.0f, 0.0f, (float) Math.cos(tf) * 4.0f); // planet moves around sun
    mvStack.pushMatrix();
    mvStack.rotate((float) tf, 0.0f, 1.0f, 0.0f); // planet axis rotation
    gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(0);

    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
    gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(1);

    gl.glActiveTexture(GL_TEXTURE0);
    gl.glBindTexture(GL_TEXTURE_2D, earthTexture);
    gl.glEnable(GL_DEPTH_TEST);
    gl.glDepthFunc(GL_LEQUAL);
    gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVerts); // draw the planet
    mvStack.popMatrix(); // remove the planet’s axial rotation from the stack
//----------------------- smaller cube == moon -----------------------------------
    mvStack.pushMatrix();
    mvStack.translate(0.0f, (float) Math.sin(tf) * 2.0f, (float) Math.cos(tf) * 2.0f); // moon moves around planet
    mvStack.rotate((float) tf, 0.0f, 0.0f, 1.0f); // moon’s rotation on its axis
    mvStack.scale(0.25f, 0.25f, 0.25f); // make the moon smaller
    gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(0);

    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
    gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(1);

    gl.glActiveTexture(GL_TEXTURE0);
    gl.glBindTexture(GL_TEXTURE_2D, moonTexture);
    gl.glEnable(GL_DEPTH_TEST);
    gl.glDepthFunc(GL_LEQUAL);
    gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVerts); // draw the moon
// remove moon scale/rotation/position, planet position, sun position, and view matrices from stack
    mvStack.popMatrix();
    mvStack.popMatrix();
    mvStack.popMatrix();
    mvStack.popMatrix();
  }

  private void setupVertices() {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    mySphere = new Sphere(24);
    numSphereVerts = mySphere.getIndices().length;
    int[] indices = mySphere.getIndices();
    Vector3f[] vert = mySphere.getVertices();
    Vector2f[] tex = mySphere.getTexCoords();
    Vector3f[] norm = mySphere.getNormals();
    float[] pvalues = new float[indices.length * 3]; // vertex positions
    float[] tvalues = new float[indices.length * 2]; // texture coordinates
    float[] nvalues = new float[indices.length * 3]; // normal vectors
    for (int i = 0; i < indices.length; i++) {
      pvalues[i * 3] = (float) (vert[indices[i]]).x;
      pvalues[i * 3 + 1] = (float) (vert[indices[i]]).y;
      pvalues[i * 3 + 2] = (float) (vert[indices[i]]).z;
      tvalues[i * 2] = (float) (tex[indices[i]]).x;
      tvalues[i * 2 + 1] = (float) (tex[indices[i]]).y;
      nvalues[i * 3] = (float) (norm[indices[i]]).x;
      nvalues[i * 3 + 1] = (float) (norm[indices[i]]).y;
      nvalues[i * 3 + 2] = (float) (norm[indices[i]]).z;
    }
    gl.glGenVertexArrays(vao.length, vao, 0);
    gl.glBindVertexArray(vao[0]);
    gl.glGenBuffers(3, vbo, 0);
// put the vertices into buffer #0
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
    gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);
// put the texture coordinates into buffer #1
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
    FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
    gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);
// put the normals into buffer #2
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
