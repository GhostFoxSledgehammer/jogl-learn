package Chapter2;

import java.nio.FloatBuffer;
import javax.swing.*;

import static com.jogamp.opengl.GL4.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;

public class Program2_1 extends JFrame implements GLEventListener {

  private final GLCanvas myCanvas;

  public Program2_1() {
    setTitle("Chapter2 - program1");
    setSize(600, 400);
    setLocation(200, 200);
    myCanvas = new GLCanvas();
    myCanvas.addGLEventListener(this);
    this.add(myCanvas);
    setVisible(true);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  @Override
  public void display(GLAutoDrawable drawable) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    float bkg[] = {1.0f, 0.0f, 0.0f, 1.0f};
    FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
    gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
  }

  @Override
  public void init(GLAutoDrawable drawable) {
  }

  @Override
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
  }

  @Override
  public void dispose(GLAutoDrawable drawable) {
  }

  public static void main(String[] args) {
    new Program2_1();
  }
}
