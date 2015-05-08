package edu.columbia.quidditch.basic;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

/**
 * Character Texture
 * @author Yuqing Guan
 * Used for displaying text for OpenGL
 * 
 */
public class CharTexture extends Texture
{
	private static final String TEXT_IMAGE = "res/chars.png";
	private static final String FONT_NAME = "res/font.ttf";
	
	// Default font size is 72
	private static final int FONT_SIZE = 72;
	private static final int COLS = 10;
	
	// Store all visible standard ASCII characters in a 10x10 grid
	private static final float CELL_PROP = 0.5f / COLS;
	
	// Background and foreground color
	private static final Color BG_COLOR = new Color(0x00000000, true);
	private static final Color TEXT_COLOR = new Color(0xFFFFFFFF, true);
	
	// The size is the bigger one among the height of a character
	// and the width of 'W', as W is the widest character in ASCII
	private static int cellSize;
	
	// Just need to load the texture once
 	private static CharTexture singleton;
	private static BufferedImage image;
	
	/**
	 * Check whether chars.png already exists, if not exists, create it
	 * @return
	 */
	public static CharTexture create()
	{
		if (singleton == null)
		{
			File textImage = new File(TEXT_IMAGE);
			if (textImage.exists())
			{
				System.out.println("Loading texture for all visible standard ASCII characters");
				image = loadTextImage();
			}
			else
			{
				System.out.println("Creating texture for all visible standard ASCII characters");
				image = createTextImage();
				System.out.println("Saving texture for all visible standard ASCII characters");
				saveTextImage();
			}
			
			singleton = new CharTexture();
		}
		
		return singleton;
	}
	
	private CharTexture()
	{
		super(image);
	}
	
	/**
	 * Load texture from existed image file
	 * @return
	 */
	private static BufferedImage loadTextImage()
	{
		BufferedImage image;
		
		try
		{
			image = ImageIO.read(new File(TEXT_IMAGE));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			image = createTextImage();
			saveTextImage();
		}
		
		return image;
	}
	
	/**
	 * Create a new image, print all characters on a 10x10 grid and then store it
	 * @return
	 */
	private static BufferedImage createTextImage()
	{
		BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = image.createGraphics();
		
		Font font;
		
		// Try to load a LED-like font
		try
		{
			font = Font.createFont(Font.TRUETYPE_FONT, new File(FONT_NAME));
			font = font.deriveFont((float) FONT_SIZE);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			font = new Font(null, Font.PLAIN, FONT_SIZE);
		}
		catch (FontFormatException e)
		{
			e.printStackTrace();
			font = new Font(null, Font.PLAIN, FONT_SIZE);
		}
		
		// Measure the size of characters
		FontMetrics metrics = g.getFontMetrics(font);
		
		int width = metrics.charWidth('W');
		int height = metrics.getHeight();
		
		cellSize = Math.max(width, height);
		image = new BufferedImage(2 * COLS * cellSize, 2 * COLS * cellSize, BufferedImage.TYPE_4BYTE_ABGR);
		
		g = image.createGraphics();
		
		// Add antialiasing
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setFont(font);
		
		g.setColor(TEXT_COLOR);
		g.setBackground(BG_COLOR);
		
		metrics = g.getFontMetrics();
		int ascent = metrics.getAscent();
		
		// Print all characters on the grid
		for (char c = ' '; c <= '~'; ++c)
		{
			int offset = c - ' ';
			
			int i = offset / COLS;
			int j = offset % COLS;
			
			String s = Character.toString(c);
			
			int x = 2 * j * cellSize + (cellSize - metrics.charWidth(c)) / 2;
			int y = 2 * i * cellSize + ascent;
			
			g.drawString(s, x, y);
		}
		
		// Vertically flip the image
		AffineTransform transform = AffineTransform.getScaleInstance(1, -1);
		transform.translate(0, -image.getHeight());
		
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		image = op.filter(image, null);
		
		return image;
	}
	
	// Save the generated image to file
	private static void saveTextImage()
	{
		try
		{
			ImageIO.write(image, "png", new File(TEXT_IMAGE));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the corresponding position 
	 * @param c char
	 * @return the char's position on the grid
	 */
	public float[] getCoords(char c)
	{
		int offset = c - ' ';
		
		int i = COLS - 1 - offset / COLS;
		int j = offset % COLS;
		
		float[] coords = new float[2];
		
		coords[0] = 2 * j * CELL_PROP;
		coords[1] = 2 * i * CELL_PROP + CELL_PROP;
		
		return coords;
	}
	
	/**
	 * Draw a character of the specified size
	 * @param c
	 * @param size
	 */
	public void drawChar(char c, float size)
	{
		float[] coords = getCoords(c);
		float halfSize = size / 2;
		
		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glTexCoord2f(coords[0], coords[1] + CELL_PROP);
		GL11.glVertex2f(-halfSize, halfSize);
		
		GL11.glTexCoord2f((coords[0] + CELL_PROP), coords[1] + CELL_PROP);
		GL11.glVertex2f(halfSize, halfSize);
		
		GL11.glTexCoord2f((coords[0] + CELL_PROP), coords[1]);
		GL11.glVertex2f(halfSize, -halfSize);
		
		GL11.glTexCoord2f(coords[0], coords[1]);
		GL11.glVertex2f(-halfSize, -halfSize);
				
		GL11.glEnd();
	}
	
	/**
	 * Draw a character of the specified character size
	 * @param s
	 * @param charSize
	 */
	public void drawString(String s, float charSize)
	{
		int len = s.length();
		
		for (int i = 0; i < len; ++i)
		{
			drawChar(s.charAt(i), charSize);
			GL11.glTranslatef(charSize, 0.0f, 0.0f);
		}
		
		// Translate back to the start position
		GL11.glTranslatef(-len * charSize, 0.0f, 0.0f);
	}
}
