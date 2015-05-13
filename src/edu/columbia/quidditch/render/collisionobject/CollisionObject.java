package edu.columbia.quidditch.render.collisionobject;

import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

import org.lwjgl.util.vector.Vector3f;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.render.Model;
import edu.columbia.quidditch.render.screen.PlayScreen;

public abstract class CollisionObject extends Model
{
	private static final float COLLISION_DELTA = 1e-3f;
	
	protected PlayScreen screen;
	
	protected float radius, speed;
	protected Vector3f defaultPos, lastPos, pos, velocity;
	
	public CollisionObject(MainGame game, PlayScreen screen, float radius, Vector3f defaultPos)
	{
		super(game);
		
		this.radius = radius;
		this.defaultPos = defaultPos;
		
		lastPos = new Vector3f();
		pos = new Vector3f(defaultPos);
		velocity = new Vector3f();
		
		this.screen = screen;
	}
	
	@Override
	public void render()
	{
		glPushMatrix();
		
		glTranslatef(pos.x, pos.y, pos.z);
		
		if (list == NO_LIST)
		{
			createList();
		}
		
		glCallList(list);
		
		glPopMatrix();
	}
	
	public boolean checkCollision(CollisionObject other)
	{
		Vector3f vec = new Vector3f();
		Vector3f.sub(pos, other.pos, vec);
		
		return vec.length() < radius + other.radius + COLLISION_DELTA;
	}
	
	public void move(float delta)
	{
		lastPos.set(pos);
		
		Vector3f newPos = new Vector3f();

		refreshVelocity();
		newPos.set(pos);
		
		newPos.x += velocity.x * delta;
		newPos.y += velocity.y * delta;
		newPos.z += velocity.z * delta;
		
		if (!checkHeight(newPos))
		{
			doOutHeight(newPos);
			return;
		}
		
		float newOvalVal = checkOval(newPos);
		
		if (newOvalVal > 1)
		{
			doOutOval(newPos, newOvalVal, delta);
			return;
		}
		
		pos.set(newPos);
	}
	
	protected abstract void refreshVelocity();

	protected float checkOval(Vector3f pos)
	{
		return (float) (Math.pow(pos.x / PlayScreen.SHORT_AXIS, 2) + Math.pow(pos.z / PlayScreen.LONG_AXIS, 2));
	}
	
	protected boolean checkHeight(Vector3f pos)
	{
		return (pos.y < PlayScreen.TOP && pos.y > PlayScreen.BOTTOM);
	}
	
	public void setPos(Vector3f newPos)
	{
		pos.set(newPos);
	}
	
	public void setPos(float x, float y, float z)
	{
		pos.set(x, y, z);
	}
	
	public Vector3f getPos()
	{
		return pos;
	}
	
	public void setVelocity(Vector3f vec)
	{
		velocity.x = vec.x;
		velocity.y = vec.y;
		velocity.z = vec.z;
	}
	
	public void setVelocity(float x, float y, float z)
	{
		velocity.x = x;
		velocity.y = y;
		velocity.z = z;
	}
	
	public Vector3f getVelocity()
	{
		return velocity;
	}
	
	public void reset()
	{
		pos.set(defaultPos);
		lastPos.set(defaultPos);
		velocity.set(0, 0, 0);
		speed = 0;
	}

	protected abstract void doOutHeight(Vector3f newPos);
	protected abstract void doOutOval(Vector3f newPos, float newOvalVal, float delta);
}
