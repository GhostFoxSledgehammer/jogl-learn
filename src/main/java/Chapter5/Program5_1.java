/*
 * 
 */
package Chapter5;

import java.nio.*;
import .*;
import java.io.InputStream;
import java.util.Scanner;
import javax.swing.*;

import static com.jogamp.opengl.GL4.*;
import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.*;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import static utils.joglutils.readShaderSource;

/**
 *
 * @author kshan
 */
public class Program5_1 extends JFrame implements GLEventListener {

  private GLJPanel myCanvas;
  private int rendering_program;
  private int vao[] = new int[1];
  private int vbo[] = new int[2];
  private float cameraX, cameraY, cameraZ;
  private float cubeLocX, cubeLocY, cubeLocZ;
  private float pyrLocX, pyrLocY, pyrLocZ;
  private  util = new ();
  private Matrix4f pMat;
  private int brickTexture;

  public Program5_1() {
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
    setupVertices();
    cameraX = 0.0f;
    cameraY = 0.0f;
    cameraZ = 4.0f;
    cubeLocX = 0.0f;
    cubeLocY = 0.0f;
    cubeLocZ = 0.0f;
    pyrLocX = 0.0f;
    pyrLocY = 2.0f;
    pyrLocZ = 0.0f;
    float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
    pMat = perspective(70.0f, aspect, 0.1f, 1000.0f);
    Texture joglBrickTexture = loadTexture("Chapter5/brick.jpg");
    brickTexture = joglBrickTexture.getTextureObject();
  }
// main(), reshape(), and dispose() are are unchanged

  public static void main(String[] args) {
    new Program5_1();
  }

  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
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
    mMat.rotate(2000 * x,0,0);
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
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(0);
    // activate buffer #1, which contains the texture coordinates
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
    gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(1);
// activate texture unit #0 and bind it to the brick texture object
    gl.glActiveTexture(GL_TEXTURE0);
    gl.glBindTexture(GL_TEXTURE_2D, brickTexture);
    gl.glEnable(GL_DEPTH_TEST);
    gl.glDepthFunc(GL_LEQUAL);
    gl.glDrawArrays(GL_TRIANGLES, 0, 18);
  }

  private void setupVertices() {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    float[] pyramid_positions
            = {-1, -1, 1, 1, -1, 1, 0, 1, 0, //front
              1, -1, 1, 1, -1, -1, 0, 1, 0,//right face
              1, -1, -1, -1, -1, -1, 0, 1, 0, //back face
              -1, -1, -1, -1, -1, 1, 0, 1, 0, //left face
              -1, -1, -1, 1, -1, 1, -1, -1, 1, // base-left block
              1, -1, 1, -1, -1, -1, 1, -1, -1 // base - right block
          };
    float[] pyr_texture_coordinates
            = {0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
              0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
              0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f,  0.0f};
    
    gl.glGenVertexArrays(vao.length, vao, 0);
    gl.glBindVertexArray(vao[0]);
    gl.glGenBuffers(vbo.length, vbo, 0);
    
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    FloatBuffer pyrBuf = Buffers.newDirectFloatBuffer(pyramid_positions);
    gl.glBufferData(GL_ARRAY_BUFFER, pyrBuf.limit() * 4, pyrBuf,
            GL_STATIC_DRAW);
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
    FloatBuffer pTexBuf
            = Buffers.newDirectFloatBuffer(pyr_texture_coordinates);
    gl.glBufferData(GL_ARRAY_BUFFER, pTexBuf.limit() * 4, pTexBuf,
            GL_STATIC_DRAW);
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
    gl.glGetShaderiv(vShader, GL_COMPILE_STATUS, vertCompiled, 0);
    if (vertCompiled[0] == 1) {
      System.out.println("vShader vertex compilation success.");
    } else {
      System.out.println("vShader vertex compilation failed.");
    }

    int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
    gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);
    gl.glCompileShader(fShader);
    gl.glGetShaderiv(fShader, GL_COMPILE_STATUS, fragCompiled, 0);
    if (fragCompiled[0] == 1) {
      System.out.println("fShader fragment compilation success.");
    } else {
      System.out.println("fShader fragment compilation failed.");
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

    gl.glGetProgramiv(vfprogram, GL_LINK_STATUS, linked, 0);
    if (linked[0] == 1) {
      System.out.println("vfprogram linking succeeded.");
    } else {
      System.out.println("vfprogram linking failed.");
    }

    gl.glDeleteShader(vShader);
    gl.glDeleteShader(fShader);
    return vfprogram;
  }

  public Texture loadTexture(String textureFileName) {
    Texture tex = null;
    try {
      tex = TextureIO.newTexture(getFileFromResource(textureFileName), false);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return tex;
  }

  private File getFileFromResource(String fileName) {
    File file = null;
    // The class loader that loaded the class
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource(fileName);
    try {
      file = new File(resource.toURI());
    } catch (URISyntaxException ex) {
      Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
    }
    return file;
  }

  private Matrix4f perspective(float fovy, float aspect, float n, float f) {
    float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
    float A = q / aspect;
    float B = (n + f) / (n - f);
    float C = (2.0f * n * f) / (n - f);
    Matrix4f r = new Matrix4f();
    r.setElementAt(0, 0, A);
    r.setElementAt(1, 1, q);
    r.setElementAt(2, 2, B);
    r.setElementAt(3, 2, -1.0f);
    r.setElementAt(2, 3, C);
    r.setElementAt(3, 3, 0.0f);
    return r;
  }
}
