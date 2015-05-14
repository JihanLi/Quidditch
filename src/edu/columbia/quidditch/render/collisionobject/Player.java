package edu.columbia.quidditch.render.collisionobject;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Material;
import edu.columbia.quidditch.basic.ShaderProgram;
import edu.columbia.quidditch.basic.Texture;
import edu.columbia.quidditch.render.Broom;
import edu.columbia.quidditch.render.Model;
import edu.columbia.quidditch.render.link.Link;
import edu.columbia.quidditch.render.screen.PlayScreen;
import edu.columbia.quidditch.util.IQELoader;

/**
 * 
 * @author Yuqing Guan
 *
 */
public class Player extends CollisionObject
{
	public static final int TEAM_GRYFFINDOR = 0;
	public static final int TEAM_SLYTHERIN = 1;
	public static final int TEAM_RAVENCLAW = 2;
	public static final int TEAM_HUFFLEPUFF = 3;
	
	private static final float SHINE = 25;
	private static final float SCALE = 21;

	private static final float RADIUS = 42;
	private static final float SECOND_RADIUS = 32;

	private static final float W = 0.25f;
	private static final float ACCELERATOR = 0.002f;
	private static final float GRAVITY = -0.02f;
	
	private static final float MIN_V = 0.01f;
	private static final float MAX_V = 0.2f;

	private static final float MAX_LOOK = 60;

	private static final String MODEL_NAME = "res/char/char.iqe";

	private static ArrayList<Integer> jointParentList;
	private static ArrayList<float[]> jointPQList;

	private static ArrayList<Vector3f> verList, texList, norList;
	private static ArrayList<ArrayList<ArrayList<Integer>>> meshList;

	private static ArrayList<String> mtlList;
	private static HashMap<String, Material> mtlMap;

	private static ArrayList<HashMap<Integer, Float>> weightList;

	private static int linksize, verSize;

	private static FloatBuffer specularBuffer;
	
	private boolean controllable = true;
	private boolean inUserTeam = true;
	
	private static HashMap<String, Material[]> mutableMtls;
	
	static
	{
		try
		{
			IQELoader loader = new IQELoader(MODEL_NAME);

			jointParentList = loader.getJointParentList();
			jointPQList = loader.getJointPQList();

			linksize = jointParentList.size();

			verList = loader.getVerList();
			texList = loader.getTexList();
			norList = loader.getNorList();

			verSize = verList.size();

			meshList = loader.getMeshList();
			mtlList = loader.getMtlList();

			mtlMap = loader.getMtlMap();

			weightList = loader.getWeightList();

			specularBuffer = BufferUtils.createFloatBuffer(4);
			specularBuffer.put(0.6f).put(0.6f).put(0.6f).put(0.6f).flip();
			
			Texture[] coats, emblems, shirts;
			
			coats = new Texture[4];
			emblems = new Texture[4];
			shirts = new Texture[4];
			
			for (int i = 0; i < 4; ++i)
			{
				coats[i] = Texture.createFromFile("res/char/textures/coat" + i + ".png");
				emblems[i] = Texture.createFromFile("res/char/textures/emblem" + i + ".png");
				shirts[i] = Texture.createFromFile("res/char/textures/shirt" + i + ".png");
			}
			
			mutableMtls = new HashMap<String, Material[]>();
			
			Material[] mutableMtl;
			String mtlName;
			Material originMtl;
			
			mutableMtl = new Material[4];
			mtlName = "Material7";
			originMtl = mtlMap.get(mtlName);
			mutableMtls.put(mtlName, mutableMtl);
			
			for (int i = 0; i < 4; ++i)
			{
				mutableMtl[i] = originMtl.copy();
				mutableMtl[i].setTexture(coats[i]);
			}
			
			mutableMtl = new Material[4];
			mtlName = "Material8";
			originMtl = mtlMap.get(mtlName);
			mutableMtls.put(mtlName, mutableMtl);
			
			for (int i = 0; i < 4; ++i)
			{
				mutableMtl[i] = originMtl.copy();
				mutableMtl[i].setTexture(coats[i]);
			}
			
			mutableMtl = new Material[4];
			mtlName = "Material9";
			originMtl = mtlMap.get(mtlName);
			mutableMtls.put(mtlName, mutableMtl);
			
			for (int i = 0; i < 4; ++i)
			{
				mutableMtl[i] = originMtl.copy();
				mutableMtl[i].setTexture(coats[i]);
			}
			
			mutableMtl = new Material[4];
			mtlName = "Material10";
			originMtl = mtlMap.get(mtlName);
			mutableMtls.put(mtlName, mutableMtl);
			
			for (int i = 0; i < 4; ++i)
			{
				mutableMtl[i] = originMtl.copy();
				mutableMtl[i].setTexture(emblems[i]);
			}
			
			mutableMtl = new Material[4];
			mtlName = "Material11";
			originMtl = mtlMap.get(mtlName);
			mutableMtls.put(mtlName, mutableMtl);
			
			for (int i = 0; i < 4; ++i)
			{
				mutableMtl[i] = originMtl.copy();
				mutableMtl[i].setTexture(coats[i]);
			}
			
			mutableMtl = new Material[4];
			mtlName = "Material12";
			originMtl = mtlMap.get(mtlName);
			mutableMtls.put(mtlName, mutableMtl);
			
			for (int i = 0; i < 4; ++i)
			{
				mutableMtl[i] = originMtl.copy();
				mutableMtl[i].setTexture(coats[i]);
			}
			
			mutableMtl = new Material[4];
			mtlName = "Material13";
			originMtl = mtlMap.get(mtlName);
			mutableMtls.put(mtlName, mutableMtl);
			
			for (int i = 0; i < 4; ++i)
			{
				mutableMtl[i] = originMtl.copy();
				mutableMtl[i].setTexture(coats[i]);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	private ArrayList<HashMap<Integer, Vector4f>> verLinkOffsetsList,
			norLinkOffsetsList;

	private ShaderProgram shaderProgram;

	private Link[] links;
	private Model broom;

	private Vector3f rot;
	
	private int team, handUpList;
	private boolean handDown;
	
	public Player(MainGame game, PlayScreen screen, int team, boolean inUserTeam, Vector3f defaultPos)
	{
		super(game, screen, RADIUS, defaultPos);
		
		this.inUserTeam = inUserTeam;

		broom = Broom.create(game);

		rot = new Vector3f();
		if(!inUserTeam) {
			rot.y = 180;
		}
		
		handDown = true;
		this.team = team;
		
		handUpList = glGenLists(1);

		links = new Link[linksize];
		links = new Link[linksize];

		int[] parentIndices = new int[linksize];

		// Set joints

		for (int i = 0; i < linksize; ++i)
		{
			float[] pq = jointPQList.get(i);
			links[i] = links[i] = new Link(pq[0], pq[1], pq[2], pq[3], pq[4],
					pq[5], pq[6]);
			parentIndices[i] = jointParentList.get(i);
		}

		for (int i = 0; i < linksize; ++i)
		{
			int parent = parentIndices[i];

			if (parent >= 0)
			{
				links[parent].addChild(links[i]);
			}
		}

		setDefaultTrans();
		initFixedPosture();

		shaderProgram = ShaderProgram.getDefaultShader();

		list = glGenLists(1);
		createList();
	}
	
	public int getTeam()
	{
		return team;
	}

	private void initFixedPosture()
	{
		links[18].setTheta(45);
		links[26].setTheta(45);

		links[30].setTheta(15);
		links[31].setTheta(15);

		links[42].setTheta(-15);
		links[43].setTheta(-15);

		links[27].setTheta(-165);
		links[39].setTheta(-80);

		links[28].setTheta(-165);
		links[40].setTheta(-80);

		links[56].setTheta(0);
	}

	public void handDown()
	{
		handDown = true;
	}

	public void handUp()
	{
		handDown = false;
	}
	
	public void setTeam(int team)
	{
		this.team = team;
		createList();
	}
	
	@Override
	protected void createList()
	{
		links[60].setTheta(0);
		draw(list);		
		links[60].setTheta(90);
		draw(handUpList);
	}

	private void setDefaultTrans()
	{
		Matrix4f[] invTrans = new Matrix4f[linksize];
		Matrix4f[] tranTrans = new Matrix4f[linksize];

		for (int i = 0; i < linksize; ++i)
		{
			Matrix4f globalTrans = links[i].getGlobalTrans();

			invTrans[i] = new Matrix4f();
			tranTrans[i] = new Matrix4f();

			Matrix4f.invert(globalTrans, invTrans[i]);
			Matrix4f.transpose(globalTrans, tranTrans[i]);
		}

		// Convert the vertex positions to links' local coordinates

		verLinkOffsetsList = new ArrayList<HashMap<Integer, Vector4f>>();

		for (int i = 0; i < verSize; ++i)
		{
			HashMap<Integer, Vector4f> verLinkOffsets = new HashMap<Integer, Vector4f>();
			verLinkOffsetsList.add(verLinkOffsets);

			HashMap<Integer, Float> weightMap = weightList.get(i);
			for (int index : weightMap.keySet())
			{
				Vector3f ver = verList.get(i);

				Vector4f extVer = new Vector4f();
				extVer.x = ver.x;
				extVer.y = ver.y;
				extVer.z = ver.z;
				extVer.w = 1;

				Vector4f offset = new Vector4f();
				Matrix4f.transform(invTrans[index], extVer, offset);

				verLinkOffsets.put(index, offset);
			}
		}

		// Convert the normals to links' local coordinates

		norLinkOffsetsList = new ArrayList<HashMap<Integer, Vector4f>>();

		for (int i = 0; i < verSize; ++i)
		{
			HashMap<Integer, Vector4f> norLinkOffsets = new HashMap<Integer, Vector4f>();
			norLinkOffsetsList.add(norLinkOffsets);

			HashMap<Integer, Float> weightMap = weightList.get(i);
			for (int index : weightMap.keySet())
			{
				Vector3f nor = norList.get(i);

				Vector4f extNor = new Vector4f();
				extNor.x = nor.x;
				extNor.y = nor.y;
				extNor.z = nor.z;
				extNor.w = 0;

				Vector4f offset = new Vector4f();
				Matrix4f.transform(tranTrans[index], extNor, offset);

				norLinkOffsets.put(index, offset);
			}
		}
	}

	private void draw(int currentList)
	{
		Matrix4f[] globalTrans = new Matrix4f[linksize];
		Matrix4f[] invTranTrans = new Matrix4f[linksize];

		// Compute inverted transposed matrices for normals

		for (int i = 0; i < linksize; ++i)
		{
			globalTrans[i] = links[i].getGlobalTrans();

			invTranTrans[i] = new Matrix4f();
			Matrix4f.invert(globalTrans[i], invTranTrans[i]);
			invTranTrans[i].m30 = invTranTrans[i].m31 = invTranTrans[i].m32 = 0;

			Matrix4f.transpose(invTranTrans[i], invTranTrans[i]);
		}

		// Compute the weighted-average positions of vertices

		Vector3f[] realVerList = new Vector3f[verSize];

		for (int i = 0; i < verSize; ++i)
		{
			realVerList[i] = new Vector3f();

			HashMap<Integer, Float> weightMap = weightList.get(i);
			HashMap<Integer, Vector4f> verLinkOffsetsMap = verLinkOffsetsList
					.get(i);

			for (int index : weightMap.keySet())
			{
				float weight = weightMap.get(index);
				Vector4f offset = verLinkOffsetsMap.get(index);

				Vector4f linkVer = new Vector4f();
				Matrix4f.transform(globalTrans[index], offset, linkVer);

				realVerList[i].x += linkVer.x * weight;
				realVerList[i].y += linkVer.y * weight;
				realVerList[i].z += linkVer.z * weight;
			}
		}

		// Compute the weighted-average directions of normals

		Vector3f[] realNorList = new Vector3f[verSize];

		for (int i = 0; i < verSize; ++i)
		{
			realNorList[i] = new Vector3f();

			HashMap<Integer, Float> weightMap = weightList.get(i);
			HashMap<Integer, Vector4f> norLinkOffsetsMap = norLinkOffsetsList
					.get(i);

			for (int index : weightMap.keySet())
			{
				float weight = weightMap.get(index);
				Vector4f offset = norLinkOffsetsMap.get(index);

				Vector4f linkNor = new Vector4f();
				Matrix4f.transform(invTranTrans[index], offset, linkNor);

				realNorList[i].x += linkNor.x * weight;
				realNorList[i].y += linkNor.y * weight;
				realNorList[i].z += linkNor.z * weight;
			}
		}

		glNewList(currentList, GL_COMPILE);
		{
			glPushMatrix();

			glMaterialf(GL_FRONT, GL_SHININESS, SHINE);
			glMaterial(GL_FRONT, GL_SPECULAR, specularBuffer);

			glScalef(SCALE, SCALE, SCALE);
			glTranslatef(0, -1, 2);
			glRotatef(180, 0, 1, 0);
			glRotatef(-45, 1, 0, 0);

			shaderProgram.bind();
			shaderProgram.setUniformi("tex", 0);

			// Render each mesh with a material

			int idx = 0;
			for (ArrayList<ArrayList<Integer>> mesh : meshList)
			{
				String mtlName = mtlList.get(idx);
				
				Material material;
				
				if (mutableMtls.containsKey(mtlName))
				{
					material = mutableMtls.get(mtlName)[team];
				}
				else
				{
					material = mtlMap.get(mtlName);
				}
				
				material.bind();
				shaderProgram.setUniformi("hasTex", material.hasTexture() ? 1
						: 0);

				// Draw each face

				for (ArrayList<Integer> face : mesh)
				{
					glBegin(GL_POLYGON);
					{
						int faceSize = face.size();

						for (int i = faceSize - 1; i >= 0; --i)
						{
							int point = face.get(i);

							Vector3f nor = realNorList[point];
							glNormal3f(nor.x, nor.y, nor.z);

							Vector3f tex = texList.get(point);
							glTexCoord2f(tex.x, tex.y);

							Vector3f ver = realVerList[point];
							glVertex3f(ver.x, ver.y, ver.z);
						}
					}
					glEnd();
				}

				++idx;
			}

			Texture.unbind();
			ShaderProgram.unbind();

			glPopMatrix();
		}
		glEndList();
	}

	@Override
	public void render()
	{
		glPushMatrix();

		glTranslatef(pos.x, pos.y, pos.z);

		glRotatef(rot.y, 0, 1, 0);
		glRotatef(rot.x, 1, 0, 0);

		glCallList(handDown ? list : handUpList);

		broom.render();

		glDisable(GL_LIGHTING);

		glColor4f(1, 0, 0, 0.02f);

		new Sphere().draw(32, 64, 64);
		
		glEnable(GL_LIGHTING);

		glPopMatrix();
	}
	
	@Override
	public boolean checkCollision(CollisionObject other)
	{
		Vector3f dPos = new Vector3f();
		Vector3f dVel = new Vector3f();
		Vector3f.sub(pos, other.pos, dPos);
		Vector3f.sub(velocity, other.velocity, dVel);

		float dotPro = Vector3f.dot(dPos, dVel);
		
		if (dotPro >= -0.01f)
		{
			return false;
		}
		
		if (other instanceof Player)
		{
			return dPos.length() < 2 * SECOND_RADIUS + COLLISION_DELTA;
		}
		else
		{
			return dPos.length() < radius + other.radius + COLLISION_DELTA;
		}
	}

	public void rotX(int sign, float delta)
	{
		rot.x += sign * delta * W;
		rot.x = Math.max(-MAX_LOOK, Math.min(MAX_LOOK, rot.x));
	}

	public void rotY(int sign, float delta)
	{
		rot.y += sign * delta * W;

		if (rot.y > 180.0f)
		{
			rot.y -= 360.0f;
		}
		else if (rot.y < -180.0f)
		{
			rot.y += 360.0f;
		}
	}

	public void resetRotX()
	{
		rot.x = 0;
	}

	public void accelerate()
	{
		if (speed < MAX_V)
		{
			speed += ACCELERATOR;
		}
	}

	public void decelerate()
	{
		if (speed > MIN_V)
		{
			speed -= ACCELERATOR;
		}
	}

	@Override
	protected void refreshVelocity()
	{
		if (controllable)
		{
			velocity.x = (float) (-Math.sin(Math.toRadians(rot.y))
					* Math.cos(Math.toRadians(rot.x)) * speed);
			velocity.y = (float) (Math.sin(Math.toRadians(rot.x)) * speed);
			velocity.z = (float) (-Math.cos(Math.toRadians(rot.y))
					* Math.cos(Math.toRadians(rot.x)) * speed);
		}
		else
		{
			velocity.y += GRAVITY;
		}
	}

	@Override
	protected void doOutHeight(Vector3f newPos)
	{
		if (newPos.y < PlayScreen.BOTTOM)
		{
			controllable = true;
		} 
		
		rot.x = -rot.x * 0.25f;
	}

	@Override
	protected void doOutOval(Vector3f newPos, float newOvalVal, float delta)
	{
		velocity.x = 0;
		velocity.y = 0;
		velocity.z = 0;
		fall();
	}

	public Vector3f getRot()
	{
		return rot;
	}
	
	public boolean isControllable() {
		return controllable;
	}
	
	@Override
	public void reset()
	{
		super.reset();
		rot.set(0, 0, 0);
		if(!inUserTeam) 
		{
			rot.y = 180;
		}
	}
	
	public void fall() {
		
		velocity.x = velocity.y = velocity.z = 0;
		speed = 0;
		rot.x = -90;
		
		controllable = false;
	}
	
	public void setBasedOnV()
	{
		speed = velocity.length();
		if (speed < 0.001f)
		{
			return;
		}
		
		rot.y = (float) Math.asin( velocity.x / speed);
		
		if (velocity.z > 0)
		{
			rot.y -= Math.PI;
		} 
		else
		{
			rot.y *= -1;
		}
		
		rot.y = (float) Math.toDegrees(rot.y);
	}
}
