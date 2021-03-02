/*
 * 
 */
package Chapter5;

import graphicslib3D.*;
import java.nio.*;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
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
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.Vector;
import javax.imageio.ImageIO;
import static utils.Math3Dutils.perspective;
import static utils.joglutils.loadTexture;
import static utils.joglutils.readShaderSource;

/**
 *
 * @author kshan
 */
public class Exercise5_3 extends JFrame implements GLEventListener {

  private GLCanvas myCanvas;
  private int rendering_program;
  private int vao[] = new int[1];
  private int vbo[] = new int[4];
  private float cameraX, cameraY, cameraZ;
  private float cubeLocX, cubeLocY, cubeLocZ;
  private float pyrLocX, pyrLocY, pyrLocZ;
  private GLSLUtils util = new GLSLUtils();
  private Matrix3D pMat;
  private int brickTexture;

  public Exercise5_3() {
    setTitle("Chapter5 - Exercise3");
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
    Texture joglBrickTexture = loadTexture("Chapter5/brick.jpg");
    brickTexture = joglBrickTexture.getTextureObject();
  }
// main(), reshape(), and dispose() are are unchanged

  public static void main(String[] args) {
    new Exercise5_3();
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
    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(0);

    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
    gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(1);

    gl.glActiveTexture(GL_TEXTURE0);
    gl.glBindTexture(GL_TEXTURE_2D, brickTexture);
// adjust OpenGL settings and draw model
    gl.glEnable(GL_DEPTH_TEST);
    gl.glDepthFunc(GL_LEQUAL);
    gl.glDrawArrays(GL_TRIANGLES, 0, 18);

    mvStack.popMatrix();
    mvStack.pushMatrix();
    mvStack.translate(Math.sin(amt) * 4.0f, 0.0f, Math.cos(amt) * 4.0f);
    mvStack.pushMatrix();
    mvStack.rotate((System.currentTimeMillis()) / 10.0, 1.0, 1.0, 0.0);
    gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);

    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(0);

    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
    gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
    gl.glEnableVertexAttribArray(1);

    gl.glActiveTexture(GL_TEXTURE0);
    gl.glBindTexture(GL_TEXTURE_2D, brickTexture);
    gl.glEnable(GL_DEPTH_TEST);
    gl.glDepthFunc(GL_LEQUAL);
    gl.glDrawArrays(GL_TRIANGLES, 0, 36);
  }

  private void setupVertices() {
    GL4 gl = (GL4) GLContext.getCurrentGL();
// 36 vertices of the 12 triangles making up a 2 x 2 x 2 cube centered    at the origin
    float[] cube_positions
            = {-1, 1, -1, -1, -1, -1, 1, -1, -1,
              1, -1, -1, 1, 1, -1, -1, 1, -1,
              1, -1, -1, 1, -1, 1, 1, 1, -1,
              1, -1, 1, 1, 1, 1, 1, 1, -1,
              1, -1, 1, -1, -1, 1, 1, 1, 1,
              -1, -1, 1, -1, 1, 1, 1, 1, 1,
              -1, -1, 1, -1, -1, -1, -1, 1, 1,
              -1, -1, -1, -1, 1, -1, -1, 1, 1,
              -1, -1, 1, 1, -1, 1, 1, -1, -1,
              1, -1, -1, -1, -1, -1, -1, -1, 1,
              -1, 1, -1, 1, 1, -1, 1, 1, 1, 1,
              1, 1, -1, 1, 1, -1, 1, -1
            };
    float[] pyramid_positions
            = {-1, -1, 1, 1, -1, 1, 0, 1, 0, //front
              1, -1, 1, 1, -1, -1, 0, 1, 0,//right face
              1, -1, -1, -1, -1, -1, 0, 1, 0, //back face
              -1, -1, -1, -1, -1, 1, 0, 1, 0, //left face
              -1, -1, -1, 1, -1, 1, -1, -1, 1, // base-left block
              1, -1, 1, -1, -1, -1, 1, -1, -1 // base - right block
          };
    float[] pyr_texture_coordinates
            = {0, 0, 1, 0, 0.5f, 1, 0, 0, 1, 0, 0.5f, 1,
              0, 0, 1, 0, 0.5f, 1, 0, 0, 1, 0, 0.5f, 1,
              0, 0, 1, 1, 0, 1, 1, 1, 0, 0, 1, 0};
    float[] cube_texture_coordinates
            = {1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0,
              1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0,
              1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0,
              1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0,
              1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0,
              1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0,
              1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0,
              1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0,
              1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0,
              1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0,
              1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0,
              1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0};
    gl.glGenVertexArrays(vao.length, vao, 0);
    gl.glBindVertexArray(vao[0]);
    gl.glGenBuffers(vbo.length, vbo, 0);

    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
    FloatBuffer cubeBuf = Buffers.newDirectFloatBuffer(cube_positions);
    gl.glBufferData(GL_ARRAY_BUFFER, cubeBuf.limit() * 4, cubeBuf, GL_STATIC_DRAW);

    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
    FloatBuffer pyrBuf = Buffers.newDirectFloatBuffer(pyramid_positions);
    gl.glBufferData(GL_ARRAY_BUFFER, pyrBuf.limit() * 4, pyrBuf, GL_STATIC_DRAW);

    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
    FloatBuffer pTexBuf = Buffers.newDirectFloatBuffer(pyr_texture_coordinates);
    gl.glBufferData(GL_ARRAY_BUFFER, pTexBuf.limit() * 4, pTexBuf, GL_STATIC_DRAW);

    gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
    FloatBuffer cTexBuf = Buffers.newDirectFloatBuffer(cube_texture_coordinates);
    gl.glBufferData(GL_ARRAY_BUFFER, cTexBuf.limit() * 4, cTexBuf, GL_STATIC_DRAW);
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
