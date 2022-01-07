// License: GPL. For details, see LICENSE file.
package Template;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import org.joml.Matrix4f;

/**
 *
 * @author Kishan Tripathi
 */
public abstract class ZoomPanPanel extends GLJPanel implements GLEventListener, Zoomable, Pannable {

  public final Camera camera;
  private boolean panningEnabled;
  private boolean zoomEnabled;
  protected int fov;
  private static final int MAX_FOV = 110;
  private static final int MIN_FOV = 20;
  private static final int IDEAL_FOV = 90;
  protected final Matrix4f vMat = new Matrix4f();
  protected final Matrix4f pMat = new Matrix4f();
  protected float aspect;

  public ZoomPanPanel() {
    camera = new Camera();
    addGLEventListener(this);
    HandleMouseEvent mouseListener = new HandleMouseEvent();
    addMouseListener(mouseListener);
    addMouseMotionListener(mouseListener);
    addMouseWheelListener(mouseListener);
    panningEnabled = true;
    zoomEnabled = true;
    fov = IDEAL_FOV;
  }

  @Override
  public boolean isPanningEnabled() {
    return panningEnabled;
  }

  @Override
  public void enablePanning(boolean enable) {
    this.panningEnabled = enable;
  }

  @Override
  public void pan(float panX, float panY) {
    if (!panningEnabled) {
      return;
    }
    float scale = (float) (Math.sin(Math.toRadians(fov / 2)) / Math.sin(Math.toRadians(IDEAL_FOV / 2)));
    float newYaw = (panX * scale);
    float newPitch = (panY * scale);
    camera.rotate(newYaw, newPitch);
    vMat.set(camera.getViewMatrix());
    repaint();
  }

  @Override
  public void enableZoom(boolean enable) {
    this.zoomEnabled = enable;
  }

  @Override
  public boolean isZoomEnabled() {
    return zoomEnabled;
  }

  @Override
  public void zoom(float zoomBy) {
    if (!zoomEnabled) {
      return;
    }
    fov += (int) zoomBy;
    fov = Math.min(fov, MAX_FOV);
    fov = Math.max(fov, MIN_FOV);
    pMat.setPerspective((float) Math.toRadians(fov), aspect, 0.1f, 1000.0f);
    repaint();
  }

  @Override
  public void reshape(GLAutoDrawable glad, int x, int y, int width, int height) {
    /* Resize haack required for java8 + GLCanvas.
    http://forum.jogamp.org/canvas-not-filling-frame-td4040092.html#a4040138*/
//    GL4 gl = glad.getGL().getGL4();
//    double dpiScalingFactor = ((Graphics2D) getGraphics()).getTransform().getScaleX();
//    width = (int) (width * dpiScalingFactor);
//    height = (int) (height * dpiScalingFactor);
//    gl.glViewport(0, 0, width, height);
    aspect = (float) getWidth() / (float) getHeight();
    pMat.setPerspective((float) Math.toRadians(fov), aspect, 0.1f, 1000.0f);
  }
  /**
   *
   * Handles Mouse Events for Panning and Zooming
   */
  private class HandleMouseEvent extends MouseAdapter {

    private int finalX;
    private int finalY;

    public HandleMouseEvent() {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
      zoom(e.getWheelRotation() * 5);
    }

    @Override
    public void mousePressed(MouseEvent e) {
      finalX = e.getX();
      finalY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      int newX = e.getX();
      int newY = e.getY();
      int width = e.getComponent().getWidth();
      int height = e.getComponent().getHeight();
      double yaw = Math.PI * (newX - finalX) / width;
      double pitch = Math.PI * (finalY - newY) / height;
      pan((float) yaw, (float) pitch);
      finalX = newX;
      finalY = newY;
    }
  }
}
