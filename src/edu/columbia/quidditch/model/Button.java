package edu.columbia.quidditch.model;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.CharTexture;
import edu.columbia.quidditch.basic.Texture;

/**
 * Button
 * 
 * @author Yuqing Guan
 * 
 */
public class Button extends Model
{
	private static final float DEFAULT_WIDTH = 250;
	private static final float DEFAULT_HEIGHT = 90;

	private static final float FONT_SIZE = 25.0f;

	private static final Texture bg, activeBg;
	private static final float BASE_YOFFSET = 7.0f;

	static
	{
		bg = Texture.createFromFile("res/button/normal.png");
		activeBg = Texture.createFromFile("res/button/active.png");
	}

	private CharTexture texture;

	private String text;

	private int bgList, activeBgList, textList, name;
	private float x, y, width, height;

	private boolean visible;

	public Button(MainGame game, float x, float y, String text)
	{
		this(game, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, text);
	}

	public Button(MainGame game, float x, float y, float width, float height,
			String text)
	{
		super(game);

		this.x = x;
		this.y = y;

		this.width = width;
		this.height = height;

		this.text = text;

		texture = CharTexture.create();

		bgList = GL11.glGenLists(1);
		activeBgList = GL11.glGenLists(1);
		textList = GL11.glGenLists(1);

		createList();

		visible = true;
	}

	public void setText(String text)
	{
		this.text = text;
		createTextList();
	}

	@Override
	protected void createList()
	{
		createBackgroundList();
		createTextList();
	}

	/**
	 * Create display list for the square background
	 */
	private void createBackgroundList()
	{
		GL11.glNewList(bgList, GL11.GL_COMPILE);
		{
			bg.drawRectangle(x, y, width, height);
		}
		GL11.glEndList();

		GL11.glNewList(activeBgList, GL11.GL_COMPILE);
		{
			activeBg.drawRectangle(x, y, width, height);
		}
		GL11.glEndList();
	}

	/**
	 * Create display list for the text
	 */
	private void createTextList()
	{
		float xOffset, yOffset;
		
		xOffset = x + width / 2 - (text.length() / 2.0f - 0.5f) * FONT_SIZE;
		yOffset = y + height / 2 + BASE_YOFFSET;
		
		GL11.glNewList(textList, GL11.GL_COMPILE);
		{
			GL11.glTranslatef(xOffset, yOffset, 0.0f);

			GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.7f);

			texture.bindWithColor();
			texture.drawString(text, FONT_SIZE);

			GL11.glTranslatef(-xOffset, -yOffset, 0.0f);

			CharTexture.unbind();
		}
		GL11.glEndList();
	}

	@Override
	public void render()
	{
		if (!visible)
		{
			return;
		}

		if (!mouseInside() || !Mouse.isButtonDown(0))
		{
			GL11.glCallList(bgList);
		}
		else
		{
			GL11.glCallList(activeBgList);
		}
		
		GL11.glCallList(textList);
	}

	/**
	 * Check whether mouse is inside this button
	 * 
	 * @return
	 */
	public boolean mouseInside()
	{
		if (!visible)
		{
			return false;
		}

		float mouseX = Mouse.getX();
		float mouseY = Mouse.getY();

		mouseX = mouseX * MainGame.DEFAULT_WIDTH / Display.getWidth();
		mouseY = mouseY * MainGame.DEFAULT_HEIGHT / Display.getHeight();

		return mouseX > x && mouseY > y && mouseX < x + width
				&& mouseY < y + height;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	public int getName()
	{
		return name;
	}
}
