package edu.columbia.quidditch.render.collisionobject;

import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glTranslatef;

import org.lwjgl.util.vector.Vector3f;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.render.Model;

public abstract class CollisionObject extends Model
{
	private static final float COLLISION_DELTA = 1e-3f;
	
	protected boolean inBound;
	
	protected float radius, speed;
	protected Vector3f pos, rot;
	
	public CollisionObject(MainGame game, float radius)
	{
		super(game);
		
		this.radius = radius;
		
		pos = new Vector3f();
		rot = new Vector3f();
	}
	
	@Override
	public void render()
	{
		glPushMatrix();
		
		glTranslatef(pos.x, pos.y, pos.z);
		glRotatef(rot.x, 1.0f, 0.0f, 0.0f);
		glRotatef(rot.y, 0.0f, 1.0f, 0.0f);
		glRotatef(rot.z, 0.0f, 0.0f, 1.0f);

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
		float dist = delta * speed;
		
		pos.x += (float) (-Math.sin(Math.toRadians(rot.y)) * Math.cos(Math.toRadians(rot.x)) * dist);
		pos.y += (float) (Math.sin(Math.toRadians(rot.x)) * dist);
		pos.z += (float) (-Math.cos(Math.toRadians(rot.y)) * Math.cos(Math.toRadians(rot.x)) * dist);
	}
}
