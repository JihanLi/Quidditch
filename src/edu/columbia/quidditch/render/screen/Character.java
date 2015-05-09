package edu.columbia.quidditch.render.screen;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Material;
import edu.columbia.quidditch.basic.ShaderProgram;
import edu.columbia.quidditch.basic.Texture;
import edu.columbia.quidditch.render.Model;
import edu.columbia.quidditch.render.link.Link;
import edu.columbia.quidditch.util.IQELoader;

public abstract class Character extends Model
{
	protected static final float SHINE = 25.0f;
	private static final String MODEL_NAME = "res/char/char.iqe";

	private static final float Y_OFFSET = -2.0f;
	private static final float Z_OFFSET = -7.0f;
	private static final float X_ROT = -90.0f;

	private static ArrayList<Integer> jointParentList;
	private static ArrayList<float[]> jointPQList;

	private static ArrayList<Vector3f> verList, texList, norList;
	private static ArrayList<ArrayList<ArrayList<Integer>>> meshList;

	private static ArrayList<String> mtlList;
	private static HashMap<String, Material> mtlMap;

	private static ArrayList<HashMap<Integer, Float>> weightList;

	private static int linksize, verSize;

	private static final String VERTEX_SHADER_NAME = "shaders/default.vsh";
	private static final String FRAGMENT_SHADER_NAME = "shaders/default.fsh";

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

	protected Vector3f pos, rot;

	protected FloatBuffer rotBuffer, specularBuffer;

	protected Link[] links;
	protected Matrix4f rotMatrix;

	public Character(MainGame game)
	{
		super(game);

		pos = new Vector3f();
		rot = new Vector3f();

		specularBuffer = BufferUtils.createFloatBuffer(4);
		specularBuffer.put(0.6f).put(0.6f).put(0.6f).put(0.6f).flip();

		rotBuffer = BufferUtils.createFloatBuffer(16);
		rotMatrix = new Matrix4f();

		pos.y = Y_OFFSET;
		pos.z = Z_OFFSET;

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

		rot.x = X_ROT;
		setDefaultTrans();

		updateRot();

		list = GL11.glGenLists(1);
		createList();

		shaderProgram = ShaderProgram.createFromFiles(VERTEX_SHADER_NAME,
				FRAGMENT_SHADER_NAME, null);
	}

	/**
	 * Update rotation matrix for the whole planner
	 */
	protected void updateRot()
	{
		GL11.glPushMatrix();

		GL11.glLoadIdentity();
		GL11.glRotatef(rot.x, 1.0f, 0.0f, 0.0f);
		GL11.glRotatef(rot.y, 0.0f, 1.0f, 0.0f);
		GL11.glRotatef(rot.z, 0.0f, 0.0f, 1.0f);

		rotBuffer.rewind();
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, rotBuffer);

		rotMatrix.load(rotBuffer);
		rotBuffer.flip();

		GL11.glPopMatrix();
	}

	@Override
	public void render()
	{
		GL11.glPushMatrix();

		GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, SHINE);

		GL11.glTranslatef(pos.x, pos.y, pos.z);
		GL11.glMultMatrix(rotBuffer);

		GL11.glCallList(list);

		GL11.glPopMatrix();
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

		GL11.glNewList(list, GL11.GL_COMPILE);
		{
			GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, specularBuffer);
			GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, SHINE);

			shaderProgram.bind();

			// Render each mesh with a material

			int idx = 0;
			for (ArrayList<ArrayList<Integer>> mesh : meshList)
			{
				String mtlName = mtlList.get(idx);

				Material material = mtlMap.get(mtlName);
				material.bind();

				// Draw each face

				for (ArrayList<Integer> face : mesh)
				{
					GL11.glBegin(GL11.GL_POLYGON);
					{
						int faceSize = face.size();

						for (int i = faceSize - 1; i >= 0; --i)
						{
							int point = face.get(i);

							Vector3f nor = realNorList[point];
							GL11.glNormal3f(nor.x, nor.y, nor.z);

							Vector3f tex = texList.get(point);
							GL11.glTexCoord2f(tex.x, tex.y);

							Vector3f ver = realVerList[point];
							GL11.glVertex3f(ver.x, ver.y, ver.z);
						}
					}
					GL11.glEnd();
				}

				++idx;
			}

			Texture.unbind();
			ShaderProgram.unbind();
		}
		GL11.glEndList();
	}
}
