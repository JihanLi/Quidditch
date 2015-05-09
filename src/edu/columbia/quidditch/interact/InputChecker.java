package edu.columbia.quidditch.interact;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Camera;
import edu.columbia.quidditch.render.screen.LoadScreen;
import edu.columbia.quidditch.render.screen.Screen;

/**
 * Check the keyboard and mouse input
 * 
 * @author Yuqing Guan
 * 
 */
public class InputChecker
{
	private MainGame game;

	public InputChecker(MainGame game)
	{
		LoadScreen.increaseLoadCount();
		this.game = game;
	}

	/**
	 * Check keyboard event when the game is running
	 * 
	 * @param delta
	 *            time
	 */
	private void checkKeyboardForStatusRunning(float delta)
	{
		int wheel = Mouse.getDWheel();

		boolean keyForward = Keyboard.isKeyDown(Keyboard.KEY_W)
				|| Keyboard.isKeyDown(Keyboard.KEY_UP) || wheel > 0;
		boolean keyBack = Keyboard.isKeyDown(Keyboard.KEY_S)
				|| Keyboard.isKeyDown(Keyboard.KEY_DOWN) || wheel < 0;

		boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_A)
				|| Keyboard.isKeyDown(Keyboard.KEY_LEFT);
		boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_D)
				|| Keyboard.isKeyDown(Keyboard.KEY_RIGHT);

		boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
		boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

		boolean keyReset = Keyboard.isKeyDown(Keyboard.KEY_R);

		boolean keyFire = Keyboard.isKeyDown(Keyboard.KEY_RETURN);

		// TODO Myself
		/*
		 * Myself myself = game.getMyself();
		 * 
		 * // Turn left myself if (keyLeft) { myself.rotY(1, delta); }
		 * 
		 * // Turn right myself if (keyRight) { myself.rotY(-1, delta); }
		 * 
		 * // Turn up myself if (keyUp) { myself.rotX(1, delta); }
		 * 
		 * // Turn down myself if (keyDown) { myself.rotX(-1, delta); }
		 * 
		 * // Reset myself to a horizontal direction if (keyReset) {
		 * myself.resetRotX(); }
		 * 
		 * // Accelerate if (keyForward) { myself.accelerate(); }
		 * 
		 * // Decelerate if (keyBack) { myself.decelerate(); }
		 * 
		 * // Attack if (keyFire) { game.fire(); }
		 */
	}

	/**
	 * Check keyboard
	 * 
	 * @param delta
	 *            time
	 */
	public void checkKeyboard(float delta)
	{
		Screen screen = game.getActiveScreen();
		if (screen != null)
		{
			screen.checkKeyboardInput(delta);
		}
	}

	/**
	 * Check mouse
	 * 
	 * @param delta
	 *            time
	 */
	public void checkMouse(float delta)
	{
		Screen screen = game.getActiveScreen();
		if (screen != null)
		{
			screen.checkMouseInput(delta);
		}
	}
}
