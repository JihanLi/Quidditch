package edu.columbia.quidditch.render;

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Material;
import edu.columbia.quidditch.basic.ShaderProgram;
import edu.columbia.quidditch.basic.Texture;
import edu.columbia.quidditch.render.screen.LoadScreen;
import edu.columbia.quidditch.util.ObjLoader;
import edu.columbia.quidditch.util.Vector3i;

/**
 * The player itself
 * 
 * @author Yuqing Guan
 * 
 */
public class Stadium extends Model
{
	// Object file
	private static final String OBJ_NAME = "res/stadium/stadium.obj";

	private static final String VERTEX_SHADER_NAME = "shaders/default.vsh";
	private static final String FRAGMENT_SHADER_NAME = "shaders/default.fsh";

	private static final float SHINE = 10.0f;

	private static final Vector3f POS = new Vector3f(0.0f, -200.0f, 0.0f);
	private static final Vector3f ROT = new Vector3f(0.0f, 0.0f, 0.0f);

	private static ArrayList<Vector3f> verList, texList, norList;
	private static ArrayList<ArrayList<ArrayList<Vector3i>>> meshList;

	private static ArrayList<String> mtlList;
	private static HashMap<String, Material> mtlMap;

	private static ShaderProgram shaderProgram;

	private static Stadium singleton;
	private static int defaultList;

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

		shaderProgram = ShaderProgram.createFromFiles(VERTEX_SHADER_NAME,
				FRAGMENT_SHADER_NAME, null);

		createDefaultList();
	}

	/**
	 * Create display lists for the plane and the propeller
	 */
	private static void createDefaultList()
	{
		LoadScreen.log("Creating display list for stadium");

		defaultList = GL11.glGenLists(1);
		GL11.glNewList(defaultList, GL11.GL_COMPILE);
		{
			shaderProgram.bind();
			shaderProgram.setUniformi("tex", 0);

			GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, SHINE);

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
					GL11.glBegin(GL11.GL_POLYGON);
					{
						for (Vector3i point : face)
						{
							int verIdx = point.x;
							int texIdx = point.y;
							int norIdx = point.z;

							if (norIdx != NO_INDEX)
							{
								Vector3f nor = norList.get(norIdx);
								GL11.glNormal3f(nor.x, nor.y, nor.z);
							}

							if (texIdx != NO_INDEX)
							{
								Vector3f tex = texList.get(texIdx);
								GL11.glTexCoord2f(tex.x, tex.y);
							}

							Vector3f ver = verList.get(verIdx);
							GL11.glVertex3f(ver.x, ver.y, ver.z);
						}
					}
					GL11.glEnd();
				}
			}

			Texture.unbind();
			ShaderProgram.unbind();
		}
		GL11.glEndList();
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

	/**
	 * Translate and rotate, draw the spinning propeller, then the whole plane
	 */
	@Override
	public void render()
	{
		GL11.glPushMatrix();

		GL11.glTranslatef(POS.x, POS.y, POS.z);

		GL11.glRotatef(ROT.y, 0, 1, 0);
		GL11.glRotatef(ROT.x, 1, 0, 0);

		GL11.glCallList(defaultList);

		GL11.glPopMatrix();
	}
}
