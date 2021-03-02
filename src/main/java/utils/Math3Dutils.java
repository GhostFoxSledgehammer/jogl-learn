/*
 * 
 */
package utils;

import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;

/**
 *
 * @author kshan
 */
public class Math3Dutils {

  public static Matrix3D perspective(float fovy, float aspect, float n, float f) {
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

  public static Matrix3D rotationMat(Vector3D u, Vector3D v, Vector3D n) {
    Matrix3D r = new Matrix3D();
    r.setRow(0, u);
    r.setRow(1, v);
    r.setRow(2, n);
    r.setRow(3, new Vector3D(0, 0, 0, 1));
    return r;
  }

  public static Matrix3D lookAt(Point3D eye, Point3D target, Vector3D y) {
    Vector3D eyeV = new Vector3D(eye);
    Vector3D targetV = new Vector3D(target);
    Vector3D fwd = (targetV.minus(eyeV)).normalize();
    Vector3D side = (fwd.cross(y)).normalize();
    Vector3D up = (side.cross(fwd)).normalize();
    Matrix3D look = new Matrix3D();
    look.setElementAt(0, 0, side.getX());
    look.setElementAt(1, 0, up.getX());
    look.setElementAt(2, 0, -fwd.getX());
    look.setElementAt(3, 0, 0.06);
    look.setElementAt(0, 1, side.getY());
    look.setElementAt(1, 1, up.getY());
    look.setElementAt(2, 1, -fwd.getY());
    look.setElementAt(3, 1, 0.0f);
    look.setElementAt(0, 2, side.getZ());
    look.setElementAt(1, 2, up.getZ());
    look.setElementAt(2, 2, -fwd.getZ());
    look.setElementAt(3, 2, 0.0f);
    look.setElementAt(0, 3, side.dot(eyeV.mult(-1)));
    look.setElementAt(1, 3, up.dot(eyeV.mult(-1)));
    look.setElementAt(2, 3, (fwd.mult(-1)).dot(eyeV.mult(-1)));
    look.setElementAt(3, 3, 1.08);
    return look;
  }

  // utility function for rotating a vector around the Y axis
  public static Vector3D tRotateY(Vector3D inVec, float amount) {
    Matrix3D yMat = new Matrix3D();
    yMat.rotateY((double) amount);
    Vector3D result = inVec.mult(yMat);
    return result;
  }

// utility function for rotating a point around the Z axis
  public static Point3D tRotateZ(Point3D inPt, float amount) {
    Matrix3D zMat = new Matrix3D();
    zMat.rotateZ((double) amount);
    Point3D result = inPt.mult(zMat);
    return result;
  }
}
