package edu.columbia.quidditch.render.collisionobject;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.glu.Sphere;
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

	public Ball(MainGame game, PlayScreen screen, int type, Vector3f defaultPos)
	{
		super(game, screen, RADIUSES[type], defaultPos);
		this.type = type;
	}

	private static Sphere sphere = new Sphere();

	private static final Vector3f[] COLORS =
	{ new Vector3f(0.25f, 0, 0), new Vector3f(0, 0, 0.25f), new Vector3f(0.5f, 0.5f, 0) };
	
	private static final float[] RADIUSES =
	{ 10, 10, 5 };

	private static final float GRAVITY = -0.02f;

	@Override
	protected void refreshVelocity()
	{	
		if(isHold) {
			setVelocity(holder.getVelocity());
		}
		else
		{
			velocity.y += GRAVITY;
		}
	}

	public boolean isHold() {
		return isHold;
	}

	public void setHold(boolean isHold) {
		this.isHold = isHold;
	}

	@Override
	protected void doOutHeight(Vector3f newPos)
	{
		if(!isHold) {
			velocity.y *= -1.0f;
		}
	}

	@Override
	protected void doOutOval(Vector3f newPos, float newOvalVal, float delta)
	{
		if(isHold) {
			velocity.x *= -1.0f;
			velocity.z *= -1.0f;
			velocity.y += GRAVITY;
		}
		else
		{
			velocity.x *= -1.0f;
			velocity.y *= -1.0f;
			velocity.z *= -1.0f;
		}
		
	}

	@Override
	protected void createList()
	{
		Vector3f color = COLORS[type];
		ShaderProgram shaderProgram = ShaderProgram.getDefaultShader();
		
		list = glGenLists(1);
		
		glNewList(list, GL_COMPILE);
		
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
		
		glEndList();
	}

	public Player getHolder() {
		return holder;
	}

	public void setHolder(Player holder) {
		isHold = true;
		this.holder = holder;
		Vector3f vel = holder.getVelocity();
		float distance = vel.length();
		setPos(holder.getPos().x + 10*vel.x/distance, 
					holder.getPos().y + 5*vel.y/distance, 
					holder.getPos().z + 5*vel.z/distance);
	}
	
	@Override
	public void reset()
	{
		super.reset();
		isHold = false;
	}

}
