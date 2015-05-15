package edu.columbia.quidditch.basic;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import edu.columbia.quidditch.render.screen.LoadScreen;

/**
 * Texture class
 * 
 * @author Yuqing Guan, Jihan Li
 * 
 */
public class Texture
{
	private static final int BYTES_PER_PIXEL = 4;

	// Default method of warp and filters
	private static final int WARP_S = GL_REPEAT;
	private static final int WARP_T = GL_REPEAT;

	private static final int MAG_FILTER = GL_NEAREST;
	private static final int MIN_FILTER = GL_NEAREST;

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
			LoadScreen.log("Loading texture from " + imageName);
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

		texId = glGenTextures();

		glBindTexture(GL_TEXTURE_2D, texId);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		gluBuild2DMipmaps(GL_TEXTURE_2D, GL_RGBA, width, height, GL_RGBA,
				GL_UNSIGNED_BYTE, buffer);

		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, WARP_S);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, WARP_T);

		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, MAG_FILTER);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, MIN_FILTER);

		glBindTexture(GL_TEXTURE_2D, 0);
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
		glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
		glBindTexture(GL_TEXTURE_2D, texId);
	}

	/**
	 * Let the texture blending with a previously-set color
	 */
	public void bindWithColor()
	{
		glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_BLEND);
		glBindTexture(GL_TEXTURE_2D, texId);
	}

	public static void unbind()
	{
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	/**
	 * Draw a rectangle filled by this texture
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void drawRectangle(float x, float y, float width, float height)
	{
		bind();

		glBegin(GL_QUADS);
		{
			glTexCoord2d(0, 1);
			glVertex2d(x, y);

			glTexCoord2d(0, 0);
			glVertex2d(x, y + height);

			glTexCoord2d(1, 0);
			glVertex2d(x + width, y + height);

			glTexCoord2d(1, 1);
			glVertex2d(x + width, y);
		}
		glEnd();

		unbind();
	}
}