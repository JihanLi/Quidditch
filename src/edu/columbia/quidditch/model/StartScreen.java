package edu.columbia.quidditch.model;

import org.lwjgl.opengl.GL11;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Texture;

/**
 * Loading screen, executed in a separated new thread
 * 
 * @author Yuqing Guan
 * 
 */
public class StartScreen extends Model
{
	private Texture bg, title;
	
	private Button[] buttons;

	public StartScreen(MainGame game)
	{
		super(game);

		bg = Texture.createFromFile("res/start/main.png");
		title = Texture.createFromFile("res/title.png");
		
		buttons = new Button[3];
		
		buttons[0] = new Button(game, "Green", 355, 200, "Start!");
		buttons[1] = new Button(game, "Green", 355, 120, "?");
		buttons[2] = new Button(game, "Green", 355, 40, "Quit");
	}

	@Override
	protected void createList()
	{
		list = GL11.glGenLists(1);

		GL11.glNewList(list, GL11.GL_COMPILE);
		{
			bg.drawRectangle(0, 0, 960, 540);
			title.drawRectangle(232, 318, 496, 172);
		}
		GL11.glEndList();
	}
	
	@Override
	public void render()
	{
		super.render();
		
		for (Button button: buttons)
		{
			button.render();
		}
	}

	public void checkMouseInput()
	{
		if (buttons[0].mouseInside())
		{
			game.startGame();
		}
		else if (buttons[2].mouseInside())
		{
			game.requestClose();
		}
		
	}
}
