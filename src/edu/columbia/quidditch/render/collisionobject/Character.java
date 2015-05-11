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
import edu.columbia.quidditch.util.IQELoader;

public class Character extends CollisionObject
{
	private static final float SHINE = 25;
	private static final float SCALE = 40;
	private static final float RADIUS = 85;

	private static final String MODEL_NAME = "res/char/char.iqe";

	private static ArrayList<Integer> jointParentList;
	private static ArrayList<float[]> jointPQList;

	private static ArrayList<Vector3f> verList, texList, norList;
	private static ArrayList<ArrayList<ArrayList<Integer>>> meshList;

	private static ArrayList<String> mtlList;
	private static HashMap<String, Material> mtlMap;

	private static ArrayList<HashMap<Integer, Float>> weightList;

	private static int linksize, verSize;
	
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

	private FloatBuffer specularBuffer;

	private Link[] links;
	private Model broom;

	public Character(MainGame game)
	{
		super(game, RADIUS);
		
		broom = Broom.create(game);

		inBound = true;

		specularBuffer = BufferUtils.createFloatBuffer(4);
		specularBuffer.put(0.6f).put(0.6f).put(0.6f).put(0.6f).flip();

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
		handDown();
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
		links[60].setTheta(0);
		createList();
	}
	
	public void handUp()
	{
		links[60].setTheta(90);
		createList();
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

	@Override
	protected void createList()
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

		glNewList(list, GL_COMPILE);
		{
			glPushMatrix();

			glMaterialf(GL_FRONT, GL_SHININESS, SHINE);

			glMaterial(GL_FRONT, GL_SPECULAR, specularBuffer);
			glMaterialf(GL_FRONT, GL_SHININESS, SHINE);

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

				Material material = mtlMap.get(mtlName);
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
		glRotatef(rot.x, 1.0f, 0.0f, 0.0f);
		glRotatef(rot.y, 0.0f, 1.0f, 0.0f);
		glRotatef(rot.z, 0.0f, 0.0f, 1.0f);

		glCallList(list);
		
		broom.render();
		
		glDisable(GL_LIGHTING);
		Sphere sphere = new Sphere();
		
		glColor4f(0, 0, 1, 0.25f);
		sphere.draw(RADIUS, 32, 32);

		glEnable(GL_LIGHTING);
		
		glPopMatrix();
	}
	
	public void rotate(float x, float y, float z)
	{
		rotX(x);
		rotY(y);
		rotZ(z);
	}
	
	public void rotate(Vector3f rot)
	{
		rotX(rot.x);
		rotY(rot.y);
		rotZ(rot.z);
	}
	
	public void translate(float x, float y, float z)
	{
		transX(x);
		transY(y);
		transZ(z);
	}
	
	public void translate(Vector3f val)
	{
		transX(val.x);
		transY(val.y);
		transZ(val.z);
	}
	
	public void rotX(float delta)
	{
		rot.x += delta;
	}

	public void rotY(float delta)
	{
		rot.y += delta;
	}
	
	public void rotZ(float delta)
	{
		rot.z += delta;
	}
	
	public void transX(float delta)
	{
		pos.x += delta;
	}
	
	public void transY(float delta)
	{
		pos.y += delta;
	}
	
	public void transZ(float delta)
	{
		pos.z += delta;
	}

	public Vector3f getPosition() {
		return pos;
	}

	public void setPosition(Vector3f pos) {
		this.pos = pos;
	}

	public Vector3f getRotation() {
		return rot;
	}

	public void setRotation(Vector3f rot) {
		this.rot = rot;
	}
}
