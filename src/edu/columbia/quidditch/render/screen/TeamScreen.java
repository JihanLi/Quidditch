package edu.columbia.quidditch.render.screen;

import static org.lwjgl.opengl.GL11.GL_COMPILE;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glNewList;

import org.lwjgl.input.Keyboard;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Texture;
import edu.columbia.quidditch.interact.ButtonListener;
import edu.columbia.quidditch.render.Button;


/**
 * Team screen
 * 
 * @author Jihan Li
 * 
 */
public class TeamScreen extends Screen 
{
	private Texture bg, title;

	public TeamScreen(final MainGame game)
	{
		super(game);

		bg = Texture.createFromFile("res/loading/loadScreen.png");
		title = Texture.createFromFile("res/title/team.png");
		
		Button button0 = new Button(game, "Gryffindor", 50, 80, "", 18);
		Button button1 = new Button(game, "Ravenclaw", 270, 80, "", 18);
		Button button2 = new Button(game, "Hufflepuff", 490, 80, "", 18);
		Button button3 = new Button(game, "Slytherin", 710, 80, "", 18);
		Button button4 = new Button(game, "Wood", 25, 470, "Back", 20);

		button0.setListener(new ButtonListener()
		{
			@Override
			public void onClick()
			{
				int[] teamNum = {1, 2, 3};
				game.getPlayScreen().setTeamUser(0);
				game.getPlayScreen().setTeamComputer(teamNum[(int) Math.floor(Math.random()*3)]);
				game.startGame();
			}
		});
		
		button1.setListener(new ButtonListener()
		{
			@Override
			public void onClick()
			{
				int[] teamNum = {0, 1, 3};
				game.getPlayScreen().setTeamUser(2);
				game.getPlayScreen().setTeamComputer(teamNum[(int) Math.floor(Math.random()*3)]);
				game.startGame();
			}
		});

		button2.setListener(new ButtonListener()
		{
			@Override
			public void onClick()
			{
				int[] teamNum = {0, 1, 2};
				game.getPlayScreen().setTeamUser(3);
				game.getPlayScreen().setTeamComputer(teamNum[(int) Math.floor(Math.random()*3)]);
				game.startGame();
			}
		});
		
		button3.setListener(new ButtonListener()
		{
			@Override
			public void onClick()
			{
				int[] teamNum = {0, 2, 3};
				game.getPlayScreen().setTeamUser(1);
				game.getPlayScreen().setTeamComputer(teamNum[(int) Math.floor(Math.random()*3)]);
				game.startGame();
			}
		});

		button4.setListener(new ButtonListener()
		{
			@Override
			public void onClick()
			{
				game.terminate();
			}
		});
		
		addButton(button0);
		addButton(button1);
		addButton(button2);
		addButton(button3);
		addButton(button4);
	}

	@Override
	protected void createList()
	{
		list = glGenLists(1);

		glNewList(list, GL_COMPILE);
		{
			bg.drawRectangle(0, 0, 960, 540);
			title.drawRectangle(240, 400, 500, 80);
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
				}
			}
		}
		
		return keyReleased;
	}
}
