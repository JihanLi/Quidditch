package edu.columbia.quidditch.render.screen;

import org.lwjgl.input.Keyboard;

import static org.lwjgl.opengl.GL11.*;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Texture;
import edu.columbia.quidditch.interact.ButtonListener;
import edu.columbia.quidditch.render.Button;

/**
 * Loading screen, executed in a separated new thread
 * 
 * @author Jihan Li
 * 
 */
public class StartScreen extends Screen
{
	private Texture bg, title;

	public StartScreen(final MainGame game)
	{
		super(game);

		bg = Texture.createFromFile("res/start/main.png");
		title = Texture.createFromFile("res/title/title.png");
		
		Button button0 = new Button(game, "Green", 355, 200, "Start Game", 18);
		Button button1 = new Button(game, "Green", 355, 120, "Music On/Off", 18);
		Button button2 = new Button(game, "Green", 355, 40, "Quit", 18);

		button0.setListener(new ButtonListener()
		{
			@Override
			public void onClick()
			{
				game.startGame();
			}
		});

		button2.setListener(new ButtonListener()
		{
			@Override
			public void onClick()
			{
				game.requestClose();
			}
		});
		
		addButton(button0);
		addButton(button1);
		addButton(button2);
	}

	@Override
	protected void createList()
	{
		list = glGenLists(1);

		glNewList(list, GL_COMPILE);
		{
			bg.drawRectangle(0, 0, 960, 540);
			title.drawRectangle(232, 318, 496, 172);
		}
		glEndList();
	}

	@Override
	public boolean checkKeyboardInput(float delta)
	{
		boolean keyReleased = false;
				
		while (Keyboard.next())
		{
			if (!Keyboard.getEventKeyState())
			{
				switch (Keyboard.getEventKey())
				{
				case Keyboard.KEY_Q:
				case Keyboard.KEY_ESCAPE:
					game.requestClose();
					break;
				case Keyboard.KEY_F2:
				case Keyboard.KEY_F12:
				case Keyboard.KEY_P:
					game.screenshot();
					break;
				case Keyboard.KEY_F11:
					game.toggleFullscreen();
					break;
				case Keyboard.KEY_RETURN:
					game.startGame();
				}
			}
		}
		
		return keyReleased;
	}
}
