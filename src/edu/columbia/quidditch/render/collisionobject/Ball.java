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
	private Vector3f holderSpeed = new Vector3f();

	public Ball(MainGame game, PlayScreen screen, int type, Vector3f defaultPos)
	{
		super(game, screen, RADIUSES[type], defaultPos);
		this.type = type;
	}

	private static Sphere sphere = new Sphere();

	private static final Vector3f[] COLORS =
	{ new Vector3f(0.25f, 0, 0), new Vector3f(0, 0, 0.25f), new Vector3f(0.5f, 0.5f, 0) };
	
	private static final float[] RADIUSES =
	{ 5, 5, 2 };

	private static final float GRAVITY = -0.02f;

	@Override
	protected void refreshVelocity()
	{	
		if(isHold) {
			velocity.set(holderSpeed);
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

}
