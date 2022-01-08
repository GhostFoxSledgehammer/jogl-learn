/*
 * 
 */
package Chapter6;

import Math.Sphere;
import Math.Torus;
import Template.ZoomPanPanel;
import java.nio.*;
import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.common.nio.Buffers;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_MIRRORED_REPEAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import static utils.joglutils.createShaderProgram;
import static utils.joglutils.loadTexture;

/**
 *
 * @author kshan
 */
public class Program6_2 extends ZoomPanPanel implements GLEventListener {

  private final FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
  private int rendering_program;
  private final int vao[] = new int[1];
  private final int vbo[] = new int[4];
  private float cameraX, cameraY, cameraZ;
  private float cubeLocX, cubeLocY, cubeLocZ;
  private int brickTexture;

  private final Matrix4f mMat = new Matrix4f(); // model matrix
  private final Matrix4f mvMat = new Matrix4f(); // model-view matrix
  private int mvLoc, pLoc;
  private long startTime;
  private Sphere mySphere;
  private int numSphereVerts;
  private int numTorusVertices;
  private int numTorusIndices;

  public Program6_2() {
  }

  @Override
  public void init(GLAutoDrawable drawable) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    rendering_program = createShaderProgram("Chapter5/P5_1vertex.shader", "Chapter5/P5_1frag.shader");
    setupVertices();
    cubeLocX = 2.0f;
    cubeLocY = 2.0f;
    cubeLocZ = -4.0f;
    vMat.set(camera.getViewMatrix());
    FPSAnimator animtr = new FPSAnimator(this, 50);
    animtr.start();
// shifted down along    the Y    -axis to reveal perspective
    // Create a perspective matrix, this one has fovy=60, aspect ratio    matches screen window.
    // Values for near and far clipping planes can vary as discussed in Section 4.9.
    aspect = (float) getWidth() / (float) getHeight();
    pMat.setPerspective((float) Math.toRadians(fov), aspect, 0.1f, 1000.0f);
    Texture joglBrickTexture = loadTexture("Chapter5/brick.jpg");
    brickTexture = joglBrickTexture.getTextureObject();
    startTime = System.currentTimeMillis();
  }
// main(), reshape(), and dispose() are are unchanged

  public static void main(String[] args) {
    JFrame jf = new JFrame();
    jf.setTitle("Chapter5 - program1");
    jf.setSize(600, 600);
    jf.add(new Program6_2());
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
    long elapsedTime = System.currentTimeMillis() - startTime;
    float tf = elapsedTime / 1000f;
    mMat.identity();
    mMat.translate(cubeLocX, cubeLocY, cubeLocZ);
    mMat.rotateXYZ(tf, tf, tf);

    mvMat.identity();
    mvMat.mul(vMat);
    mvMat.mul(mMat);
// copy perspective and MV matrices to corresponding uniform variables
    mvLoc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
    pLoc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
    gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
    gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));

// associate VBO with the corresponding vertex attribute in the vertexshader 
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(0);
    // activate buffer #1, which contains the texture coordinates
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
    gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(1);
// activate texture unit #0 and bind it to the brick texture object
    gl.glActiveTexture(GL_TEXTURE0);
    gl.glBindTexture(GL_TEXTURE_2D, brickTexture);
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
    gl.glEnable(GL_DEPTH_TEST);
    gl.glDepthFunc(GL_LEQUAL);
    gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3]);
    gl.glDrawElements(GL_TRIANGLES, numTorusIndices, GL_UNSIGNED_INT, 0);
  }

  private void setupVertices() {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    Torus myTorus = new Torus(0.5f, 0.2f, 48);
    numTorusVertices = myTorus.getNumVertices();
    numTorusIndices = myTorus.getNumIndices();
    Vector3f[] vertices = myTorus.getVertices();
    Vector2f[] texCoords = myTorus.getTexCoords();
    Vector3f[] normals = myTorus.getNormals();
    int[] indices = myTorus.getIndices();
    float[] pvalues = new float[vertices.length * 3];
    float[] tvalues = new float[texCoords.length * 2];
    float[] nvalues = new float[normals.length * 3];
    for (int i = 0; i < numTorusVertices; i++) {
      pvalues[i * 3] = (float) vertices[i].x; // vertex position
      pvalues[i * 3 + 1] = (float) vertices[i].y;
      pvalues[i * 3 + 2] = (float) vertices[i].z;
      tvalues[i * 2] = (float) texCoords[i].x; // texture coordinates
      tvalues[i * 2 + 1] = (float) texCoords[i].y;
      nvalues[i * 3] = (float) normals[i].x; // normal vector
      nvalues[i * 3 + 1] = (float) normals[i].y;
      nvalues[i * 3 + 2] = (float) normals[i].z;
    }
    gl.glGenVertexArrays(vao.length, vao, 0);
    gl.glBindVertexArray(vao[0]);
    gl.glGenBuffers(4, vbo, 0); // generate VBOs as before, plus one for indices
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); // vertex positions
    FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
    gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]); // texture coordinates
    FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
    gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]); // normal vectors
    FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
    gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit() * 4, norBuf, GL_STATIC_DRAW);
    gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3]);
    IntBuffer idxBuf = Buffers.newDirectIntBuffer(indices);
    gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuf.limit() * 4, idxBuf, GL_STATIC_DRAW);
  }
}
