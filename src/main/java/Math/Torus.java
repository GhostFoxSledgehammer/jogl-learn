/*
 * 
 */
package Math;

import static com.jogamp.nativewindow.util.PixelFormat.CType.Y;
import .Matrix4f;
import .Point3D;
import .Vector3f;
import .Vertex3D;

/**
 *
 * @author kshan
 */
public class Torus {

  private int numVertices, numIndices, prec;
  private int[] indices;
  private Vertex3D[] vertices;
  private float inner, outer;
  private Vector3f[] tTangent, bTangent;

  public Torus(float innerRadius, float outerRadius, int p) {
    inner = innerRadius;
    outer = outerRadius;
    prec = p;
    initTorus();
  }

  private void initTorus() {
    numVertices = (prec + 1) * (prec + 1);
    numIndices = prec * prec * 6;
    vertices = new Vertex3D[numVertices];
    indices = new int[numIndices];
    tTangent = new Vector3f[numVertices];
    bTangent = new Vector3f[numVertices];
    for (int i = 0; i < numVertices; i++) {
      vertices[i] = new Vertex3D();
    }
// calculate first ring.
    for (int i = 0; i < prec + 1; i++) { // build the ring by rotating points around the origin, then moving them outward
      Point3D initOuterPos = new Point3D(outer, 0.0, 0.0);
      Point3D rotOuterPos = tRotateZ(initOuterPos, (i * 360.0f / prec));
      Point3D initInnerPos = new Point3D(inner, 0.0, 0.0);
      Point3D ringPos = rotOuterPos.add(initInnerPos);
      vertices[i].setLocation(ringPos);
// compute texture coordinates for each vertex on the ring
      vertices[i].setS(0.0f);
      vertices[i].setT(((float) i) / ((float) prec));
// compute normal vectors for each vertex in the ring
      Vector3f negZ = new Vector3f(0.0f, 0.0f, -1.0f); // tangent vector of irst outer ring = -Z axis 
      Vector3f negY = new Vector3f(0.0f, -1.0f, 0.0f); // initial bitangent vector = -Y axis 
      tTangent[i] = negZ; // the tangent vector is saved (and thus available to the application)
// the bitangent is then rotated around the Z axis, and also saved
      bTangent[i] = new Vector3f(tRotateZ(new Point3D(negY), (i * 360.0f / (prec))));
      vertices[i].setNormal(tTangent[i].cross(bTangent[i])); // cross product produces the normal
    }
// rotate the first ring about the Y axis to generate the other rings of the torus
    for (int ring = 1; ring <= prec; ring++) {
      for (int vert = 0; vert <= prec; vert++) { // rotate the vertex positions of the original ring around the Y axis
        float rotAmt = (float) ((float) ring * 360.0f / prec);
        Vector3f vp = new Vector3f(vertices[vert].getLocation());
        Vector3f vpr = tRotateY(vp, rotAmt);
        vertices[ring * (prec + 1) + vert].setLocation(new Point3D(vpr));
// compute the texture coordinates for the vertices in the new rings
        vertices[ring * (prec + 1) + vert].setS((float) ring / (float) prec);
        vertices[ring * (prec + 1) + vert].setT(vertices[vert].getT());
// rotate the tangent and bitangent vectors around the Y axis
        tTangent[ring * (prec + 1) + vert] = tRotateY(tTangent[vert], rotAmt);
        bTangent[ring * (prec + 1) + vert] = tRotateY(bTangent[vert], rotAmt);
// rotate the normal vector around the Y axis
        Vector3f normalRotateY = tRotateY(vertices[vert].getNormal(), rotAmt);
        vertices[ring * (prec + 1) + vert].setNormal(normalRotateY);
      }
    }
    // calculate triangle indices corresponding to the two triangles built per vertex
    for (int ring = 0; ring < prec; ring++) {
      for (int vert = 0; vert < prec; vert++) {
        indices[((ring * prec + vert) * 2) * 3 + 0] = ring * (prec + 1) + vert;
        indices[((ring * prec + vert) * 2) * 3 + 1] = (ring + 1) * (prec + 1) + vert;
        indices[((ring * prec + vert) * 2) * 3 + 2] = ring * (prec + 1) + vert + 1;
        indices[((ring * prec + vert) * 2 + 1) * 3 + 0] = ring * (prec + 1) + vert + 1;
        indices[((ring * prec + vert) * 2 + 1) * 3 + 1] = (ring + 1) * (prec + 1) + vert;
        indices[((ring * prec + vert) * 2 + 1) * 3 + 2] = (ring + 1) * (prec + 1) + vert + 1;
      }
    }
  }
// utility function for rotating a vector around the Y axis

  private Vector3f tRotateY(Vector3f inVec, float amount) {
    Matrix4f yMat = new Matrix4f();
    yMat.rotateY((double) amount);
    Vector3f result = inVec.mult(yMat);
    return result;
  }
// utility function for rotating a point around the Z axis

  private Point3D tRotateZ(Point3D inPt, float amount) {
    Matrix4f zMat = new Matrix4f();
    zMat.rotateZ((double) amount);
    Point3D result = inPt.mult(zMat);
    return result;
  }
// accessors for the torus indices and vertices

  public int[] getIndices() {
    return indices;
  }

  public Vertex3D[] getVertices() {
    return vertices;
  }
}
