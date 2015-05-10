package edu.columbia.quidditch.render;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Material;
import edu.columbia.quidditch.basic.ShaderProgram;
import edu.columbia.quidditch.basic.Texture;
import edu.columbia.quidditch.render.screen.LoadScreen;
import edu.columbia.quidditch.util.ObjLoader;
import edu.columbia.quidditch.util.Vector3i;

/**
 * Characters
 * 
 * @author Yuqing Guan
 * 
 */
public class Stadium extends Model
{
	// Object file
	private static final String OBJ_NAME = "res/stadium/stadium.obj";

	private static final float SHINE = 10.0f;

	private static ArrayList<Vector3f> verList, texList, norList;
	private static ArrayList<ArrayList<ArrayList<Vector3i>>> meshList;

	private static ArrayList<String> mtlList;
	private static HashMap<String, Material> mtlMap;

	private static ShaderProgram shaderProgram;

	private static Stadium singleton;
	private static int defaultList;

	private static FloatBuffer specularBuffer;

	/**
	 * Load obj file
	 */
	static
	{
		ObjLoader loader = ObjLoader.create(OBJ_NAME);

		verList = loader.getVerList();
		texList = loader.getTexList();
		norList = loader.getNorList();
		mtlMap = loader.getMtlMap();

		meshList = loader.getMeshList();
		mtlList = loader.getMtlList();

		mtlMap = loader.getMtlMap();

		shaderProgram = ShaderProgram.getDefaultShader();

		specularBuffer = BufferUtils.createFloatBuffer(4);
		specularBuffer.put(0.6f).put(0.6f).put(0.6f).put(0.6f).flip();
		
		createDefaultList();
	}

	/**
	 * Create display lists for the stadium
	 */
	private static void createDefaultList()
	{
		LoadScreen.log("Creating display list for stadium");

		defaultList = glGenLists(1);
		glNewList(defaultList, GL_COMPILE);
		{
			glPushMatrix();
			
			glTranslatef(5, -200, -22);
			glRotatef(59.4f, 0, 1, 0);
			
			shaderProgram.bind();
			shaderProgram.setUniformi("tex", 0);

			glMaterial(GL_FRONT, GL_SPECULAR, specularBuffer);
			glMaterialf(GL_FRONT, GL_SHININESS, SHINE);

			// Draw meshes with corresponding materials
			for (int i = meshList.size() - 1; i >= 0; --i)
			{
				ArrayList<ArrayList<Vector3i>> mesh = meshList.get(i);

				String mtlName = mtlList.get(i);

				Material material = mtlMap.get(mtlName);
				material.bind();
				shaderProgram.setUniformi("hasTex", material.hasTexture() ? 1
						: 0);

				for (ArrayList<Vector3i> face : mesh)
				{
					glBegin(GL_POLYGON);
					{
						for (Vector3i point : face)
						{
							int verIdx = point.x;
							int texIdx = point.y;
							int norIdx = point.z;

							if (norIdx != NO_INDEX)
							{
								Vector3f nor = norList.get(norIdx);
								glNormal3f(nor.x, nor.y, nor.z);
							}

							if (texIdx != NO_INDEX)
							{
								Vector3f tex = texList.get(texIdx);
								glTexCoord2f(tex.x, tex.y);
							}

							Vector3f ver = verList.get(verIdx);
							glVertex3f(ver.x, ver.y, ver.z);
						}
					}
					glEnd();
				}
			}

			Texture.unbind();
			ShaderProgram.unbind();
			
			glPopMatrix();
		}
		glEndList();
	}

	public static Stadium create(MainGame game)
	{
		if (singleton == null)
		{
			singleton = new Stadium(game);
		}

		return singleton;
	}

	/**
	 * Initialize with default position and rotation
	 * 
	 * @param game
	 */
	private Stadium(MainGame game)
	{
		super(game);
	}

	@Override
	protected void createList()
	{
		list = defaultList;
	}
}
