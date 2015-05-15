package edu.columbia.quidditch.interact;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.render.collisionobject.Ball;
import edu.columbia.quidditch.render.collisionobject.Player;
import edu.columbia.quidditch.render.screen.PlayScreen;

public class Intelligence {
	
	private MainGame game;
	private PlayScreen playscreen;
	private ArrayList<Player> playersUser = new ArrayList<Player>();
	private ArrayList<Player> playersComputer = new ArrayList<Player>();
	
	public Intelligence(MainGame game, PlayScreen playscreen)
	{
		this.game = game;
		this.playscreen = playscreen;
		
		playersUser = playscreen.getPlayersUser();
		playersComputer = playscreen.getPlayersComputer();
	}
	
	public void playerControl()
	{
		for(Player player : playersUser)
		{
			if(!playscreen.getCurrentPlayer().equals(player))
				nonHolderControl(player);
		}
		
		if(!playscreen.getBall().isHold() || playscreen.isHeldByUser())
		{
			Player minPlayer = null;
			float minDist = Float.MAX_VALUE;
			for(Player player : playersComputer)
			{
				float dist = player.distance(playscreen.getBall());
				if(dist < minDist)
				{
					minDist = dist;
					minPlayer = player;
				}
			}
			
			for(Player player : playersComputer)
			{
				 if(player.equals(minPlayer))
				 {
					 attackerControl(player);
				 }
				 else
				 {
					 nonHolderControl(player);
				 }
			}
		}
		else
		{
			for(Player player : playersComputer)
			{
				if(playscreen.getBall().getHolder().equals(player))
				{
					holderControl(player);
				}
				else 
				{
					nonHolderControl(player);
				}
			}
		}
		
		
	}

	private void holderControl(Player player) 
	{
		
		
	}

	private void nonHolderControl(Player player) 
	{
		Ball ball = playscreen.getBall();
		if(ball.isHold())
		{
			player.setRot(ball.getHolder().getRot());
		}
		else
		{
			Vector3f sub = new Vector3f();
			Vector3f.sub(ball.getPos(), player.getPos(), sub);
			player.setRot(sub);
		}
	}

	private void attackerControl(Player player) 
	{
		
	}
}
