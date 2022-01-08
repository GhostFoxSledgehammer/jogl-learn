/*
 * 
 */
package Chapter5;

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
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.FPSAnimator;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.joml.Matrix4f;
import static utils.joglutils.checkOpenGLError;
import static utils.joglutils.printProgramLog;
import static utils.joglutils.printShaderLog;
import static utils.joglutils.readShaderSource;

/**
 *
 * @author kshan
 */
public class Program5_2 extends ZoomPanPanel implements GLEventListener {

  private final FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
  private int rendering_program;
  private final int vao[] = new int[1];
  private final int vbo[] = new int[2];
  private float cameraX, cameraY, cameraZ;
  private float cubeLocX, cubeLocY, cubeLocZ;
  private int brickTexture;

  private final Matrix4f mMat = new Matrix4f(); // model matrix
  private final Matrix4f mvMat = new Matrix4f(); // model-view matrix
  private int mvLoc, pLoc;
  private long startTime;

  public Program5_2() {
  }

  @Override
  public void init(GLAutoDrawable drawable) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    rendering_program = createShaderProgram();
    setupVertices();
    cubeLocX = 2.0f;
    cubeLocY = 2.0f;
    cubeLocZ = -8.0f;
    vMat.set(camera.getViewMatrix());
    FPSAnimator animtr = new FPSAnimator(this, 50);
    animtr.start();
// shifted down along    the Y    -axis to reveal perspective
    // Create a perspective matrix, this one has fovy=60, aspect ratio    matches screen window.
    // Values for near and far clipping planes can vary as discussed in Section 4.9.
    aspect = (float) getWidth() / (float) getHeight();
    pMat.setPerspective((float) Math.toRadians(fov), aspect, 0.1f, 1000.0f);
    brickTexture = loadTextureAWT("Chapter5/brick.jpg");
    startTime = System.currentTimeMillis();
  }
// main(), reshape(), and dispose() are are unchanged

  public static void main(String[] args) {
    JFrame jf = new JFrame();
    jf.setTitle("Chapter5 - program1");
    jf.setSize(600, 600);
    jf.add(new Program5_2());
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
    mMat.rotateXYZ(0, 2 * tf, 0);

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

  private static File getFileFromResource(String fileName) {
    File file = null;
    // The class loader that loaded the class
    Class currentClass = new Object() {
    }.getClass().getEnclosingClass();
    ClassLoader classLoader = currentClass.getClassLoader();
    URL resource = classLoader.getResource(fileName);
    try {
      file = new File(resource.toURI());
    } catch (URISyntaxException ex) {
      Logger.getLogger(currentClass.getName()).log(Level.SEVERE, null, ex);
    }
    return file;
  }

  public static int loadTextureAWT(String textureFileName) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    BufferedImage textureImage = getBufferedImage(textureFileName);
    byte[] imgRGBA = getRGBAPixelData(textureImage, true);
    ByteBuffer rgbaBuffer = Buffers.newDirectByteBuffer(imgRGBA);
    int[] textureIDs = new int[1]; // array to hold generated texture IDs
    gl.glGenTextures(1, textureIDs, 0);
    int textureID = textureIDs[0]; // ID for the 0th texture object
    gl.glBindTexture(GL_TEXTURE_2D, textureID); // specifies the active 2D texture
    gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA,// MIPMAP level, color space
    textureImage.getWidth(), textureImage.getHeight(), 0, // image size, border (ignored)
    GL_RGBA, GL_UNSIGNED_BYTE, // pixel format and data type
    rgbaBuffer); // buffer holding texture data
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    return textureID;
  }

  private static BufferedImage getBufferedImage(String fileName) {
    BufferedImage img;
    try {
      img = ImageIO.read(getFileFromResource(fileName));
    } catch (IOException e) {
      System.err.println("Error reading '" + fileName + '"');
      throw new RuntimeException(e);
    }
    return img;
  }

  private static byte[] getRGBAPixelData(BufferedImage img, boolean flip) {
    byte[] imgRGBA;
    int height = img.getHeight(null);
    int width = img.getWidth(null);
    WritableRaster raster
    = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, 4, null);
    ComponentColorModel colorModel = new ComponentColorModel(
    ColorSpace.getInstance(ColorSpace.CS_sRGB),
    new int[]{8, 8, 8, 8}, true, false, // bits, has Alpha, isAlphaPreMultiplied
    ComponentColorModel.TRANSLUCENT, // transparency
    DataBuffer.TYPE_BYTE); // data transfer type
    BufferedImage newImage = new BufferedImage(colorModel, raster, false, null);
    Graphics2D g = newImage.createGraphics();
// use an affine transform to "flip" the image to conform to OpenGL orientation.
// In Java the origin is at the upper left. In OpenGL the origin is at the lower left.
    if (flip) {
      AffineTransform gt = new AffineTransform();
      gt.translate(0, height);
      gt.scale(1, -1d);
      g.transform(gt);
    }
    g.drawImage(img, null, null);
    g.dispose();
    DataBufferByte dataBuf = (DataBufferByte) raster.getDataBuffer();
    imgRGBA = dataBuf.getData();
    return imgRGBA;
  }
}
