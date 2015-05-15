package edu.columbia.quidditch.render.collisionobject;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.ShaderProgram;
import edu.columbia.quidditch.render.screen.PlayScreen;

public class Ball extends CollisionObject
{
	private static FloatBuffer specularBuffer;
	private static final float SHINE = 25;

	static
	{
		specularBuffer = BufferUtils.createFloatBuffer(4);
		specularBuffer.put(0.6f).put(0.6f).put(0.6f).put(0.6f).flip();
	}

	private int type;
	private boolean isHold = false;
	private Player holder;
	private float BETA = 0.8f; //Lost energy during collision.

	public Ball(MainGame game, PlayScreen screen, int type, Vector3f defaultPos)
	{
		super(game, screen, RADIUSES[type], defaultPos);
		this.type = type;
	}

	private static Sphere sphere = new Sphere();

	private static final Vector3f[] COLORS =
	{ new Vector3f(0.25f, 0, 0), new Vector3f(0, 0, 0.25f),
			new Vector3f(0.5f, 0.5f, 0) };

	private static final float[] RADIUSES =
	{ 10, 10, 5 };

	private static final float GRAVITY = -0.02f;

	@Override
	protected void refreshVelocity()
	{
		if (isHold)
		{
			setVelocity(holder.getVelocity());
		}
		else
		{
			velocity.y += GRAVITY;
		}
	}
	
	public void move(float delta)
	{
		if(isHold)
		{
			Matrix4f ballPos = new Matrix4f();
			ballPos.setIdentity();

			ballPos.m03 = -10;
			ballPos.m13 = 30;
			ballPos.m23 = -25;
			
			Matrix4f.rotate((float) Math.toRadians(-holder.getRot().x), new Vector3f(1, 0, 0), ballPos, ballPos);
			//Matrix4f.rotate((float) Math.toRadians(holder.getRot().z), new Vector3f(0, 0, 1), ballPos, ballPos);
			Matrix4f.rotate((float) Math.toRadians(-holder.getRot().y), new Vector3f(0, 1, 0), ballPos, ballPos);
			

			setPos(holder.getPos().x + ballPos.m03, holder.getPos().y + ballPos.m13, holder.getPos().z + ballPos.m23);
		}
		else
		{
			super.move(delta);
		}
	}

	public boolean isHold()
	{
		return isHold;
	}

	public void setHold(boolean isHold)
	{
		this.isHold = isHold;
	}

	@Override
	protected void doOutHeight(Vector3f newPos)
	{
		System.out.println(newPos);
		System.out.println(velocity);
		if (!isHold)
		{
			velocity.y *= -BETA;
		}
	}
	
	@Override
	protected boolean checkOval(Vector3f pos)
	{
		if (pos.x < - PlayScreen.SHORT_AXIS * COFF || pos.x > PlayScreen.SHORT_AXIS * COFF)
		{
			return false;
		}
		
		if (pos.z > PlayScreen.LONG_AXIS * COFF || pos.z < - PlayScreen.LONG_AXIS * COFF)
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	protected void doOutOval(Vector3f newPos, float delta)
	{
		if (isHold)
		{
			velocity.x *= -BETA;
			velocity.z *= -BETA;
			velocity.y += GRAVITY;
		}
		else
		{
			velocity.x *= -BETA;
			velocity.y *= -BETA;
			velocity.z *= -BETA;
		}

	}

	@Override
	protected void createList()
	{
		Vector3f color = COLORS[type];
		ShaderProgram shaderProgram = ShaderProgram.getDefaultShader();

		list = glGenLists(1);

		glNewList(list, GL_COMPILE);
		{
			shaderProgram.bind();
			shaderProgram.setUniformi("tex", 0);
			shaderProgram.setUniformi("hasTex", 0);

			glMaterialf(GL_FRONT, GL_SHININESS, SHINE);
			glMaterial(GL_FRONT, GL_SPECULAR, specularBuffer);

			glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT);
			glColor3f(color.x, color.y, color.z);

			glColorMaterial(GL_FRONT_AND_BACK, GL_DIFFUSE);
			glColor3f(color.x, color.y, color.z);

			glColorMaterial(GL_FRONT_AND_BACK, GL_SPECULAR);
			glColor3f(color.x, color.y, color.z);

			sphere.draw(radius, 64, 64);

			ShaderProgram.unbind();
		}
		glEndList();
	}

	public Player getHolder()
	{
		return holder;
	}

	public void setHolder(Player holder)
	{
		isHold = true;
		this.holder = holder;
	}
	
	public void clearHolder()
	{
		isHold = false;
		this.holder = null;
	}
	
	@Override
	public void reset()
	{
		super.reset();
		isHold = false;
	}


}
