package edu.columbia.quidditch.basic;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.GLU;

/**
 * Texture class
 * 
 * @author Yuqing Guan
 * 
 */
public class Texture
{
	private static final int BYTES_PER_PIXEL = 4;

	// Default method of warp and filters
	private static final int WARP_S = GL12.GL_CLAMP_TO_EDGE;
	private static final int WARP_T = GL12.GL_CLAMP_TO_EDGE;

	private static final int MAG_FILTER = GL11.GL_NEAREST;
	private static final int MIN_FILTER = GL11.GL_NEAREST;

	private int width, height;

	protected int texId;

	/**
	 * Load texture from image file
	 * 
	 * @param imageName
	 * @return
	 */
	public static Texture createFromFile(String imageName)
	{
		try
		{
			System.out.println("Loading texture from " + imageName);
			return new Texture(imageName);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	/**
	 * Convert image to byte buffer
	 * 
	 * @param image
	 * @return
	 */
	public static ByteBuffer image2Buffer(BufferedImage image)
	{
		int width = image.getWidth();
		int height = image.getHeight();

		int[] rgbArray = image.getRGB(0, 0, width, height, null, 0, width);
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height
				* BYTES_PER_PIXEL);

		for (int y = 0; y < height; ++y)
		{
			for (int x = 0; x < width; ++x)
			{
				int argb = rgbArray[x + y * width];

				buffer.put((byte) ((argb >> 16) & 0xFF));
				buffer.put((byte) ((argb >> 8) & 0xFF));
				buffer.put((byte) (argb & 0xFF));
				buffer.put((byte) ((argb >> 24) & 0xFF));
			}
		}

		buffer.flip();

		return buffer;
	}

	private Texture(String imageName) throws IOException
	{
		File imageFile = new File(imageName);
		BufferedImage image = ImageIO.read(imageFile);
		loadImage(image);
	}

	public Texture(BufferedImage image)
	{
		loadImage(image);
	}

	/**
	 * Load image to the graphics memory
	 * 
	 * @param image
	 */
	private void loadImage(BufferedImage image)
	{
		width = image.getWidth();
		height = image.getHeight();

		ByteBuffer buffer = image2Buffer(image);

		texId = GL11.glGenTextures();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

		GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, GL11.GL_RGBA, width, height,
				GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, WARP_S);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, WARP_T);

		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				MAG_FILTER);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				MIN_FILTER);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public int getId()
	{
		return texId;
	}

	/**
	 * Bind this texture to something defaultly replace previous color
	 */
	public void bind()
	{
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE,
				GL11.GL_REPLACE);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
	}

	/**
	 * Let the texture blending with a previously-set color
	 */
	public void bindWithColor()
	{
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE,
				GL11.GL_BLEND);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
	}

	public static void unbind()
	{
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
	
	public void drawRectangle(float x, float y, float width, float height)
	{
		bind();
		
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glTexCoord2d(0, 1);
			GL11.glVertex2d(x, y);

			GL11.glTexCoord2d(0, 0);
			GL11.glVertex2d(x, y + height);

			GL11.glTexCoord2d(1, 0);
			GL11.glVertex2d(x + width, y + height);

			GL11.glTexCoord2d(1, 1);
			GL11.glVertex2d(x + width, y);
		}
		GL11.glEnd();
		
		unbind();
	}
}