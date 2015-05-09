package edu.columbia.quidditch.render.screen;

import java.util.Vector;

import org.lwjgl.input.Mouse;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.render.Button;
import edu.columbia.quidditch.render.Model;

public abstract class Screen extends Model
{
	protected Vector<Model> children;
	protected Vector<Button> buttons;
	
	public Screen(MainGame game)
	{
		super(game);
		
		children = new Vector<Model>();
		buttons = new Vector<Button>();
	}
	
	public void addButton(Button button)
	{
		children.add(button);
		buttons.add(button);
		button.setScreen(this);
	}

	@Override
	public void render()
	{
		super.render();

		for (Model child : children)
		{
			child.render();
		}
	}

	public boolean checkMouseInput(float delta)
	{
		boolean click = false;
		
		while (Mouse.next())
		{
			if (!Mouse.getEventButtonState() && Mouse.getEventButton() == 0)
			{
				for (Button button : buttons)
				{
					if (button.mouseInside())
					{
						button.click();
						click = true;
						break;
					}
				}
			}
		}
		
		return click;
	}
	
	public abstract boolean checkKeyboardInput(float delta);
}
