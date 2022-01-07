package com.jogl.jogl_learn.GUI;

import java.nio.FloatBuffer;
import javax.swing.*;

import static com.jogamp.opengl.GL4.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.common.nio.Buffers;

public class JoglCanvas extends JFrame implements GLEventListener {

  private final GLJPanel myCanvas;

  public JoglCanvas() {
    setTitle("Jogl Canvas");
    setSize(600, 400);
    setLocation(200, 200);
    myCanvas = new GLJPanel();
    myCanvas.addGLEventListener(this);
    this.add(myCanvas);
    setVisible(true);
  }

  @Override
  public void display(GLAutoDrawable drawable) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    float bkg[] = {1.0f, 0.0f, 0.0f, 1.0f};
    FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
    gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
  }

  public static void main(String[] args) {
    new JoglCanvas();
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
}
