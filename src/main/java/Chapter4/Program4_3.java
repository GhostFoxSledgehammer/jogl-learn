/*
 * 
 */
package Chapter4;

import graphicslib3D.*;
import java.nio.*;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;
import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import com.jogamp.opengl.GLContext;
import static graphicslib3D.GLSLUtils.checkOpenGLError;
import static graphicslib3D.GLSLUtils.printProgramLog;
import static graphicslib3D.GLSLUtils.printShaderLog;
import java.io.InputStream;
import java.util.Scanner;
import java.util.Vector;

/**
 *
 * @author kshan
 */
public class Program4_3 extends JFrame implements GLEventListener {

  private GLCanvas myCanvas;
  private int rendering_program;
  private int vao[] = new int[1];
  private int vbo[] = new int[2];
  private float cameraX, cameraY, cameraZ;
  private float cubeLocX, cubeLocY, cubeLocZ;
  private float pyrLocX, pyrLocY, pyrLocZ;
  private GLSLUtils util = new GLSLUtils();
  private Matrix3D pMat;

  public Program4_3() {
    setTitle("Chapter4 - program1");
    setSize(600, 600);
    myCanvas = new GLCanvas();
    myCanvas.addGLEventListener(this);
    this.add(myCanvas);
    setVisible(true);
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
    cubeLocY = -2.0f;
    cubeLocZ = 0.0f;
    pyrLocX = 0.0f;
    pyrLocY = 2.0f;
    pyrLocZ = 0.0f;
    float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
    pMat = perspective(70.0f, aspect, 0.1f, 1000.0f);
  }
// main(), reshape(), and dispose() are are unchanged

  public static void main(String[] args) {
    new Program4_3();
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
    Matrix3D vMat = new Matrix3D();
    vMat.translate(-cameraX, -cameraY, -cameraZ);
// build model matrix
    Matrix3D mMat = new Matrix3D();
    mMat.translate(cubeLocX, cubeLocY, cubeLocZ);
// concatenate model and view matrix to create MV matrix
    Matrix3D mvMat = new Matrix3D();
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
// adjust OpenGL settings and draw model
    gl.glEnable(GL_DEPTH_TEST);
    gl.glDepthFunc(GL_LEQUAL);
    //gl.glFrontFace(GL_CW);
    gl.glDrawArrays(GL_TRIANGLES, 0, 36);
    mMat = new Matrix3D();
    mMat.translate(pyrLocX, pyrLocY, pyrLocZ);
    mvMat = new Matrix3D();
    mvMat.concatenate(vMat);
    mvMat.concatenate(mMat);
    gl.glUniformMatrix4fv(mv_loc, 1, false, mvMat.getFloatValues(), 0);
    gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0); 
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(0);
    gl.glEnable(GL_DEPTH_TEST);
    gl.glDepthFunc(GL_LEQUAL);
    //gl.glFrontFace(GL_CCW);
    gl.glDrawArrays(GL_TRIANGLES, 0, 18);
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
    float[] pyramid_positions = 
    {-1, -1, 1, 1, -1, 1, 0, 1, 0, //front
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

  private Matrix3D perspective(float fovy, float aspect, float n, float f) {
    float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
    float A = q / aspect;
    float B = (n + f) / (n - f);
    float C = (2.0f * n * f) / (n - f);
    Matrix3D r = new Matrix3D();
    r.setElementAt(0, 0, A);
    r.setElementAt(1, 1, q);
    r.setElementAt(2, 2, B);
    r.setElementAt(3, 2, -1.0f);
    r.setElementAt(2, 3, C);
    r.setElementAt(3, 3, 0.0f);
    return r;
  }
}
