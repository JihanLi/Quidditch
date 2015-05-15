package edu.columbia.quidditch.basic;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluOrtho2D;

import java.awt.Color;
import java.awt.Font;
import java.util.Hashtable;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

import edu.columbia.quidditch.MainGame;

/**
 * Fonts
 * 
 * @author Jihan Li, Yuqing Guan
 * 
 */

public class Fonts
{
	private static Hashtable<String, UnicodeFont> fontMap;

	static
	{
		fontMap = new Hashtable<String, UnicodeFont>();
	}

	/**
	 * Create a font and save it to map, if the font exists, just load it
	 * 
	 * @param fontStyle
	 * @param color
	 * @param size
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static UnicodeFont createFont(String fontStyle, Color color,
			int size)
	{
		String key = getKey(fontStyle, color, size);

		if (fontMap.containsKey(key))
		{
			return fontMap.get(key);
		}

		Font awtFont = new Font(fontStyle, Font.BOLD, size);
		UnicodeFont font = new UnicodeFont(awtFont);

		font.getEffects().add(new ColorEffect(color));
		font.addAsciiGlyphs();

		try
		{
			font.loadGlyphs();
		}
		catch (SlickException e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		fontMap.put(key, font);

		return font;
	}

	private static String getKey(String fontStyle, Color color, int size)
	{
		return fontStyle + "\t" + color.toString() + "\t"
				+ Integer.toString(size);
	}

	/**
	 * Create / load a font and then draw string
	 * 
	 * @param x
	 * @param y
	 * @param text
	 * @param fontStyle
	 * @param color
	 * @param size
	 */
	public static void draw(float x, float y, String text, String fontStyle,
			Color color, int size)
	{
		UnicodeFont font = createFont(fontStyle, color, size);

		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();

		glLoadIdentity();

		glMatrixMode(GL_PROJECTION);
		glPushMatrix();

		glLoadIdentity();

		glDisable(GL_DEPTH_TEST);
		glDisable(GL_LIGHTING);

		gluOrtho2D(0, MainGame.DEFAULT_WIDTH, 0, MainGame.DEFAULT_HEIGHT);

		glRotatef(180, 1, 0, 0);
		font.drawString(x - font.getWidth(text) / 2, -y - font.getHeight(text),
				text);

		glEnable(GL_DEPTH_TEST);
		glEnable(GL_LIGHTING);

		glPopMatrix();

		glMatrixMode(GL_MODELVIEW);

		glPopMatrix();
	}
}
