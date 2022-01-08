/*
 * 
 */
package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.joml.Vector2f;
import org.joml.Vector3f;
import static utils.IOutils.getFileFromResourceAsStream;

/**
 *
 * @author kshan
 */
public class ImportedModel {

  private Vector3f[] vertices;
  private Vector2f[] texCoords;
  private Vector3f[] normals;
  private int numVertices;

  public ImportedModel(String filename) {
    ModelImporter modelImporter = new ModelImporter();
    try {
      modelImporter.parseOBJ(filename); // uses modelImporter to get vertex information
      numVertices = modelImporter.getNumVertices();
      float[] verts = modelImporter.getVertices();
      float[] tcs = modelImporter.getTextureCoordinates();
      float[] norm = modelImporter.getNormals();
      vertices = new Vector3f[numVertices];
      texCoords = new Vector2f[numVertices];
      normals = new Vector3f[numVertices];
      for (int i = 0; i < vertices.length; i++) {
        vertices[i] = new Vector3f();
        vertices[i].set(verts[i * 3], verts[i * 3 + 1], verts[i * 3 + 2]);
        texCoords[i] = new Vector2f();
        texCoords[i].set(tcs[i * 2], tcs[i * 2 + 1]);
        normals[i] = new Vector3f();
        normals[i].set(norm[i * 3], norm[i * 3 + 1], norm[i * 3 + 2]);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public int getNumVertices() {
    return numVertices;
  } // accessors

  public Vector3f[] getVertices() {
    return vertices;
  }

  public Vector2f[] getTexCoords() {
    return texCoords;
  }

  public Vector3f[] getNormals() {
    return normals;
  }

  private class ModelImporter { // values as read in from OBJ file

    private final ArrayList<Float> vertVals = new ArrayList<Float>();
    private final ArrayList<Float> stVals = new ArrayList<Float>();
    private final ArrayList<Float> normVals = new ArrayList<Float>();
// values stored for later use as vertex attributes
    private final ArrayList<Float> triangleVerts = new ArrayList<Float>();
    private final ArrayList<Float> textureCoords = new ArrayList<Float>();
    private final ArrayList<Float> normals = new ArrayList<Float>();

    public void parseOBJ(String filename) throws IOException {
      InputStream input = getFileFromResourceAsStream(filename);
      BufferedReader br = new BufferedReader(new InputStreamReader(input));
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("v") && !line.startsWith("vt") && !line.startsWith("vn")) // vertex position ("v" case)
        {
          for (String s : (line.substring(2)).split(" ")) {
            if (s.isEmpty()) {
              int i = 1;
            }
            vertVals.add(Float.valueOf(s)); // extract the vertex position values
          }
        } else if (line.startsWith("vt")) // texture coordinates ("vt" case)
        {
          for (String s : (line.substring(3)).split(" ")) {
            stVals.add(Float.valueOf(s)); // extract the texture coordinate values
          }
        } else if (line.startsWith("vn")) // vertex normals ("vn" case)
        {
          for (String s : (line.substring(3)).split(" ")) {
            normVals.add(Float.valueOf(s)); // extract the normal vector values
          }
        } else if (line.startsWith("f")) // triangle faces ("f" case)
        {
          for (String s : (line.substring(2)).split(" ")) {
            String v = s.split("/")[0]; // extract triangle face references
            String vt = s.split("/")[1];
            String vn = s.split("/")[2];
            
            if (v.isEmpty() || vt.isEmpty() || vn.isEmpty()) {
              int i = 0;
            }
            int vertRef = v.isEmpty() ? 0 : (Integer.valueOf(v) - 1) * 3;
            int tcRef = vt.isEmpty() ? 0 : (Integer.valueOf(vt) - 1) * 2;
            int normRef = vn.isEmpty() ? 0 : (Integer.valueOf(vn) - 1) * 3;
            triangleVerts.add(vertVals.get(vertRef)); // build array of vertices
            triangleVerts.add(vertVals.get(vertRef + 1));
            triangleVerts.add(vertVals.get(vertRef + 2));
            textureCoords.add(stVals.get(tcRef)); // build array of
            textureCoords.add(stVals.get(tcRef + 1)); // texture coordinates.
            normals.add(normVals.get(normRef)); //… and normals
            normals.add(normVals.get(normRef + 1));
            normals.add(normVals.get(normRef + 2));
          }
        }
      }
      input.close();
    }// accessors for retrieving the number of vertices, the vertices themselves,
// and the corresponding texture coordinates and normals (only called once per model)

    public int getNumVertices() {
      return (triangleVerts.size() / 3);
    }

    public float[] getVertices() {
      float[] p = new float[triangleVerts.size()];
      for (int i = 0; i < triangleVerts.size(); i++) {
        p[i] = triangleVerts.get(i);
      }
      return p;
    }
// similar accessors for texture coordinates and normal vectors go here

    private float[] getTextureCoordinates() {
      float[] p = new float[textureCoords.size()];
      for (int i = 0; i < textureCoords.size(); i++) {
        p[i] = textureCoords.get(i);
      }
      return p;
    }

    private float[] getNormals() {
      float[] p = new float[normals.size()];
      for (int i = 0; i < normals.size(); i++) {
        p[i] = normals.get(i);
      }
      return p;
    }
  }
}
