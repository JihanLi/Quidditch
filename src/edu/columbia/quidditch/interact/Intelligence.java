package edu.columbia.quidditch.interact;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.render.collisionobject.Ball;
import edu.columbia.quidditch.render.collisionobject.Player;
import edu.columbia.quidditch.render.screen.PlayScreen;

public class Intelligence {
	
	private static final float RADIUS = 100f;
	private static final float ATTACKER = 0.5f;
	private static final float OTHER = 0.4f;
	private MainGame game;
	private PlayScreen playscreen;
	private int timer = 0;
	private int wake = 0;
	private int sleep = 500;
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
		timer++;
		for(Player player : playersUser)
		{
			if (!playscreen.getCurrentPlayer().equals(player))
			{
				nonHolderControl(player);
			}
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
				if(playscreen.getBall().isHold() && playscreen.getBall().getHolder().equals(player))
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

	private void holderControl(Player player) {

		player.accelerate(ATTACKER);
		if (player.isCollided() || player.checkBoundary() || player.avoidOval())
		{
			return;
		}
		
		if(checkComingCollision(player))
		{
			float rand = (float) Math.random();
			
			if (rand < 0.3f) // Pass the ball to nearest team mate.
			{
				float max = Float.MAX_VALUE;
				Player nearMate = null;
				for (Player teammate : playersComputer)
				{
					if (teammate.equals(player))
					{
						continue;
					}
					
					float dis = player.distance(teammate);
					if (dis < max) {
						nearMate = teammate;
					}
					
				}
				Vector3f dx = Vector3f.sub(nearMate.getPos(), player.getPos(), null);
				player.setRotBasedOnDX(dx);
				
				player.handDown();
				Vector3f vel = player.getVelocity();
				Vector3f rot = player.getRot();
				float mod = vel.length();
				if(vel.x == 0 && vel.y == 0 && vel.z == 0)
					playscreen.getBall().setVelocity(2*rot.x, 2*rot.y, 2*rot.z);
				else
					playscreen.getBall().setVelocity(0.8f*vel.x/mod, 0.8f*vel.y/mod, 0.8f*vel.z/mod);
				playscreen.getBall().clearHolder();
			}
			
			return;
		}
		
		if(sleep > 300)
		{
			Vector3f dx = new Vector3f();
			Vector3f.sub(new Vector3f(0, 75.5f, 990f), player.getPos(), dx);
			player.setRotBasedOnDX(dx);
		}
		sleep++;
		
	}
		
	private boolean checkComingCollision(Player player) {
		
		Player againstPlayer = null;
		float max = Float.MAX_VALUE;
		
		for (Player other : playersComputer)
		{
			if (other.equals(playscreen.getBall().getHolder()))
			{
				continue;
			}
			if (player.checkCollision(other, RADIUS))
			{
				float dis = player.distance(other);
				if (max > dis)
				{
					max = dis;
					againstPlayer = other;
				}
			}
		}
		
		for (Player other : playersUser)
		{
			if (other.equals(playscreen.getBall().getHolder()))
			{
				continue;
			}
			if (player.checkCollision(other, RADIUS))
			{
				float dis = player.distance(other);
				if (max > dis)
				{
					max = dis;
					againstPlayer = other;
				}
			}
		}
		
		if (againstPlayer == null)
		{
			return false;
		}
		
		Vector3f dx = Vector3f.sub(player.getPos(), againstPlayer.getPos(), null);
		player.setRotBasedOnDX(dx);
		return true;
	}

	private void nonHolderControl(Player player) {
		
		player.accelerate(OTHER);
		if (player.checkBoundary() || player.isCollided() || player.avoidOval())
		{
			return;
		}
		
		if(checkComingCollision(player))
		{
			return;
		}
		
		if(timer % player.getInterval() == 0)
		{
			Ball ball = playscreen.getBall();
			Vector3f sub = new Vector3f();
			Vector3f.sub(ball.getPos(), player.getPos(), sub);
			player.setRotBasedOnDX(sub);
		}
	}

	private void attackerControl(Player player) {
		
		player.accelerate(ATTACKER);
		if (player.isCollided() || player.checkBoundary() || player.avoidOval())
		{
			return;
		}
		
		if(checkComingCollision(player))
		{
			return;
		}
		
		if(wake > 200)
		{
			Ball ball = playscreen.getBall();
			Vector3f sub = new Vector3f();
			Vector3f.sub(ball.getPos(), player.getPos(), sub);
			player.setRotBasedOnDX(sub);
		}
		wake++;
	}

	public int getWake() {
		return wake;
	}

	public void setWake(int wake) {
		this.wake = wake;
	}

	public int getSleep() {
		return sleep;
	}

	public void setSleep(int sleep) {
		this.sleep = sleep;
	}
}
