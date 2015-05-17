package edu.columbia.quidditch.render.screen;

import org.lwjgl.input.Keyboard;

import static org.lwjgl.opengl.GL11.*;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Texture;
import edu.columbia.quidditch.interact.ButtonListener;
import edu.columbia.quidditch.render.Button;

/**
 * Start screen
 * 
 * @author Jihan Li
 * 
 */
public class StartScreen extends Screen
{
	private Texture bg, title;

	/**
	 * Start screen
	 */
	public StartScreen(final MainGame game)
	{
		super(game);

		bg = Texture.createFromFile("res/start/main.png");
		title = Texture.createFromFile("res/title/title.png");

		Button button0 = new Button(game, "Green", 355, 170, "Start Game", 18);
		Button button1 = new Button(game, "Green", 355, 70, "Quit", 18);

		button0.setListener(new ButtonListener()
		{
			@Override
			public void onClick()
			{
				game.chooseTeam();
			}
		});

		button1.setListener(new ButtonListener()
		{
			@Override
			public void onClick()
			{
				game.requestClose();
			}
		});

		addButton(button0);
		addButton(button1);
	}

	/**
	 * Create list of start screen.
	 */
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

	
	/**
	 * Check keyboard input.
	 */
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
					keyReleased = true;
					game.requestClose();
					break;
				case Keyboard.KEY_F2:
				case Keyboard.KEY_F12:
				case Keyboard.KEY_P:
					keyReleased = true;
					game.screenshot();
					break;
				case Keyboard.KEY_F11:
					keyReleased = true;
					game.toggleFullscreen();
					break;
				case Keyboard.KEY_RETURN:
					keyReleased = true;
					game.chooseTeam();
				}
			}
		}

		return keyReleased;
	}
}
