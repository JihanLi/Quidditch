package edu.columbia.quidditch.render;

import java.awt.Color;
import java.util.HashMap;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import static org.lwjgl.opengl.GL11.*;
import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Fonts;
import edu.columbia.quidditch.basic.Texture;
import edu.columbia.quidditch.interact.ButtonListener;
import edu.columbia.quidditch.render.screen.LoadScreen;
import edu.columbia.quidditch.render.screen.Screen;

/**
 * Button
 * 
 * @author Jihan Li
 * 
 */
public class Button extends Model
{
	private static final String[] TYPES =
	{ "Green", "Wood", "Gryffindor", "Ravenclaw", "Hufflepuff", "Slytherin" };
	private static final HashMap<String, Texture> normalBg, pressedBg;

	private static final HashMap<String, Float> widths, heights,
			textCenterHeights;

	static
	{
		normalBg = new HashMap<String, Texture>();
		pressedBg = new HashMap<String, Texture>();

		widths = new HashMap<String, Float>();
		heights = new HashMap<String, Float>();

		textCenterHeights = new HashMap<String, Float>();

		for (String type : TYPES)
		{
			normalBg.put(type,
					Texture.createFromFile("res/button/normal" + type + ".png"));
			pressedBg.put(type, Texture.createFromFile("res/button/pressed"
					+ type + ".png"));
		}

		widths.put("Green", 250.0f);
		heights.put("Green", 90.0f);
		textCenterHeights.put("Green", 0.0f);

		widths.put("Wood", 100.0f);
		heights.put("Wood", 50.0f);
		textCenterHeights.put("Wood", -4.0f);
		
		widths.put("Gryffindor", 200.0f);
		heights.put("Gryffindor", 250.0f);
		textCenterHeights.put("Gryffindor", 0.0f);
		
		widths.put("Ravenclaw", 200.0f);
		heights.put("Ravenclaw", 250.0f);
		textCenterHeights.put("Ravenclaw", 0.0f);
		
		widths.put("Hufflepuff", 200.0f);
		heights.put("Hufflepuff", 250.0f);
		textCenterHeights.put("Hufflepuff", 0.0f);
		
		widths.put("Slytherin", 200.0f);
		heights.put("Slytherin", 250.0f);
		textCenterHeights.put("Slytherin", 0.0f);
	}

	private String text;
	private int fontSize;

	private int normalBgList, pressedBgList;
	private float x, y, width, height, textCenterHeight;

	private boolean visible;
	private String type;

	private ButtonListener listener;
	private Screen screen;

	public Button(MainGame game, String type, float x, float y, String text,
			int fontSize)
	{
		super(game);

		this.x = x;
		this.y = y;

		this.type = type;
		this.text = text;
		this.fontSize = fontSize;

		width = widths.get(type);
		height = heights.get(type);
		textCenterHeight = textCenterHeights.get(type);

		normalBgList = glGenLists(1);
		pressedBgList = glGenLists(1);

		screen = null;

		createList();

		listener = null;
		visible = true;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	@Override
	protected void createList()
	{
		LoadScreen.log("Creating display lists for button");

		glNewList(normalBgList, GL_COMPILE);
		{
			normalBg.get(type).drawRectangle(x, y, width, height);
		}
		glEndList();

		glNewList(pressedBgList, GL_COMPILE);
		{
			pressedBg.get(type).drawRectangle(x, y, width, height);
		}
		glEndList();
	}

	public void setListener(ButtonListener listener)
	{
		this.listener = listener;
	}

	public void setScreen(Screen screen)
	{
		this.screen = screen;
	}

	@Override
	public void render()
	{
		if (!visible)
		{
			return;
		}

		if (!mouseInBound() || !Mouse.isButtonDown(0))
		{
			glCallList(normalBgList);
		}
		else
		{
			glCallList(pressedBgList);
		}
		
		Fonts.draw(x + width / 2, y + height / 2 + textCenterHeight, text, "Castellar", Color.yellow, fontSize);
	}

	/**
	 * Check whether mouse is inside this button
	 * 
	 * @return
	 */
	public boolean mouseInBound()
	{
		if (!visible)
		{
			return false;
		}

		if (screen != null && screen != game.getActiveScreen())
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

	public void click()
	{
		if (listener == null)
		{
			return;
		}

		listener.onClick();
	}
}
