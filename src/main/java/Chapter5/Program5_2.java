/*
 * 
 */
package Chapter5;

import java.nio.*;
import graphicslib3D.*;
import java.io.InputStream;
import java.util.Scanner;
import java.util.Vector;
import javax.swing.*;

import static com.jogamp.opengl.GL4.*;
import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static graphicslib3D.GLSLUtils.checkOpenGLError;
import static graphicslib3D.GLSLUtils.printProgramLog;
import static graphicslib3D.GLSLUtils.printShaderLog;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.*;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 *
 * @author kshan
 */
public class Program5_2 extends JFrame implements GLEventListener {

  private GLCanvas myCanvas;
  private int rendering_program;
  private int vao[] = new int[1];
  private int vbo[] = new int[2];
  private float cameraX, cameraY, cameraZ;
  private float cubeLocX, cubeLocY, cubeLocZ;
  private float pyrLocX, pyrLocY, pyrLocZ;
  private GLSLUtils util = new GLSLUtils();
  private Matrix3D pMat;
  private int brickTexture;

  public Program5_2() {
    setTitle("Program 5.2 Java AWT Routines for Loading Texture Images");
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
    cameraZ = 4.0f;
    cubeLocX = 0.0f;
    cubeLocY = 0.0f;
    cubeLocZ = 0.0f;
    pyrLocX = 0.0f;
    pyrLocY = 0.0f;
    pyrLocZ = 0.0f;
    float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
    pMat = perspective(70.0f, aspect, 0.1f, 1000.0f);
    brickTexture = loadTexture("Chapter5/brick.jpg");
  }
// main(), reshape(), and dispose() are are unchanged

  public static void main(String[] args) {
    new Program5_2();
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
    double x = (double) (System.currentTimeMillis()) / 10000.0;
    mMat.translate(cubeLocX, cubeLocY, cubeLocZ);
    mMat.rotate(0, 2000 * x, 0);
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
              0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f};

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

  private int loadTexture(String textureFileName) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    BufferedImage textureImage = getBufferedImage(textureFileName);
    byte[] imgRGBA = getRGBAPixelData(textureImage);
    ByteBuffer rgbaBuffer = Buffers.newDirectByteBuffer(imgRGBA);
    int[] textureIDs = new int[1]; // array to hold generated texture IDs
    gl.glGenTextures(1, textureIDs, 0);
    int textureID = textureIDs[0]; // ID for the 0th texture object
    gl.glBindTexture(GL_TEXTURE_2D, textureID); // specifies the active 2D texture
    gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, // MIPMAP level, color space
            textureImage.getWidth(), textureImage.getHeight(), 0, // image size, border (ignored)
            GL_RGBA, GL_UNSIGNED_BYTE, // pixel format and data type 
            rgbaBuffer
    ); // buffer holding texture data
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    return textureID;
  }

  private BufferedImage getBufferedImage(String fileName) {
    BufferedImage img;
    try {
      ClassLoader classLoader = getClass().getClassLoader();
      URL resource = classLoader.getResource(fileName);
      img = ImageIO.read(resource);
    } catch (IOException e) {
      System.err.println("Error reading '" + fileName + '"');
      throw new RuntimeException(e);
    }
    return img;
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

  public Texture loadTexture(String textureFileName, String suffix) {
    Texture tex = null;
    try {
      tex = TextureIO.newTexture(getFileFromResourceAsStream(textureFileName), false, suffix);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return tex;
  }

  private byte[] getRGBAPixelData(BufferedImage img) {
    byte[] imgRGBA;
    int height = img.getHeight(null);
    int width = img.getWidth(null);
    WritableRaster raster
            = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, 4,
                    null);
    ComponentColorModel colorModel = new ComponentColorModel(
            ColorSpace.getInstance(ColorSpace.CS_sRGB),
            new int[]{8, 8, 8, 8}, true, false, // bits, has Alpha, isAlphaPreMultiplied
            ComponentColorModel.TRANSLUCENT, // transparency
            DataBuffer.TYPE_BYTE); // data transfer type
    BufferedImage newImage = new BufferedImage(colorModel, raster, false, null);
// use an affine transform to "flip" the image to conform to OpenGL orientation.
// In Java the origin is at the upper left. In OpenGL the origin is at the lower left.
    AffineTransform gt = new AffineTransform();
    gt.translate(0, height);
    gt.scale(1, -1d);
    Graphics2D g = newImage.createGraphics();
    g.transform(gt);
    g.drawImage(img, null, null);
    g.dispose();
    DataBufferByte dataBuf = (DataBufferByte) raster.getDataBuffer();
    imgRGBA = dataBuf.getData();
    return imgRGBA;
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
