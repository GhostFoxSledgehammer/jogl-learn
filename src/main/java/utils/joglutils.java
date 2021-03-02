/*
 * 
 */
package utils;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import java.util.Scanner;
import java.util.Vector;
import static utils.IOutils.getBufferedImage;
import static utils.IOutils.getFileFromResource;
import static utils.IOutils.getFileFromResourceAsStream;

import static utils.imageutils.getRGBAPixelData;

/**
 *
 * @author kshan
 */
public class joglutils {

  public static int loadTextureID(String textureFileName) {
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

  public static Texture loadTexture(String textureFileName) {
    Texture tex = null;
    try {
      tex = TextureIO.newTexture(getFileFromResource(textureFileName), false);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return tex;
  }

  public static String[] readShaderSource(String filename) {

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
}
