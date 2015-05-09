package edu.columbia.quidditch.render;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.ShaderProgram;
import edu.columbia.quidditch.basic.Texture;
import edu.columbia.quidditch.filter.CombineFilter;
import edu.columbia.quidditch.filter.Filter;
import edu.columbia.quidditch.filter.GaussianFilter;
import edu.columbia.quidditch.filter.MeanFilter;
import edu.columbia.quidditch.render.screen.LoadScreen;

/**
 * The terrain
 * 
 * @author Yuqing Guan
 * 
 */
public class Terra extends Model
{
	// Sizes
	public static final int HALF_SIZE = 4000;
	public static final int QUARTER_SIZE = HALF_SIZE / 2;

	// The highest and lowest hight
	private static final float HIGHEST = 3000.0f;
	private static final float LOWEST = -1000.0f;

	// The centering height of snow, dirt and grass
	private static final float HIGH = 2000.0f;
	private static final float MID = 1200.0f;
	private static final float LOW = 400.0f;

	private static final float SHINE = 25.0f;
	
	private static final float PITCH = -200.0f;

	// The smoothing result will be calculated by 75% Gaussian and 25% mean
	// filter
	private static final float GAUSSIAN_PROP = 0.75f;

	// Different filter sizes
	private static final int HALF_FILTER_SIZE = 30;
	private static final int HALF_SNOW_FILTER_SIZE = 2;
	private static final int HALF_GRASS_FILTER_SIZE = 8;

	// Size of cells and number of columns (rows)
	private static final int CELL_SIZE = 25;

	private static final int COLS = HALF_SIZE / CELL_SIZE * 2;
	private static final int HALF_COLS = COLS / 2;
	
	// Location of vertex attributes in my GLSL program
	private static final int SNOW_OFFSET_LOC = 6;
	private static final int GRASS_OFFSET_LOC = 7;

	// The height of snow and grass will be offseted randomly
	private static final float MAX_SNOW_OFFSET = 800.0f;
	private static final float MAX_GRASS_OFFSET = 800.0f;

	private static final int FLAT_X = 1400;
	private static final int FLAT_Y = 1400;

	private static final int FLAT_COL = FLAT_X / CELL_SIZE;
	private static final int FLAT_ROW = FLAT_Y / CELL_SIZE;

	private static final int MULTIPLE_FLAT = 3;
	private static final int MULTIPLE_FLAT_COL = FLAT_COL * MULTIPLE_FLAT;
	private static final int MULTIPLE_FLAT_ROW = FLAT_ROW * MULTIPLE_FLAT;
	
	private static final float FLAT_SLOPE = 0.3f;

	private static final String GRASS_NAME = "res/terra/grass.jpg";
	private static final String DIRT_NAME = "res/terra/dirt.jpg";
	private static final String SNOW_NAME = "res/terra/snow.jpg";

	private static final String VERTEX_SHADER_NAME = "shaders/terra.vsh";
	private static final String FRAGMENT_SHADER_NAME = "shaders/terra.fsh";

	private static final String TERRA_NAME = "res/terra/terra.map";

	private float[][] heightMap, snowMap, grassMap;

	private Texture grass, dirt, snow;
	private ShaderProgram shaderProgram;

	/**
	 * If terra.map exists, load it. Otherwise, generate a new terrain and save
	 * it.
	 * 
	 * @param game
	 * @return
	 */
	public static Terra create(MainGame game)
	{
		Terra terra;
		File terraFile = new File(TERRA_NAME);

		if (terraFile.exists())
		{
			LoadScreen.log("Loading terrain from " + TERRA_NAME);
			terra = new Terra(game, TERRA_NAME);
		}
		else
		{
			LoadScreen.log("Generating random terrain");
			terra = new Terra(game);

			LoadScreen.log("Saving terrain to " + TERRA_NAME);
			terra.save(TERRA_NAME);
		}

		return terra;
	}

	/**
	 * Generate new terrain
	 * 
	 * @param game
	 */
	private Terra(MainGame game)
	{
		super(game);

		createHeightMap();
		createOffsetMap();

		grass = Texture.createFromFile(GRASS_NAME);
		dirt = Texture.createFromFile(DIRT_NAME);
		snow = Texture.createFromFile(SNOW_NAME);

		HashMap<Integer, String> attributes = new HashMap<Integer, String>();
		attributes.put(SNOW_OFFSET_LOC, "snowOffset");
		attributes.put(GRASS_OFFSET_LOC, "grassOffset");

		shaderProgram = ShaderProgram.createFromFiles(VERTEX_SHADER_NAME,
				FRAGMENT_SHADER_NAME, attributes);

		createList();
	}

	/**
	 * Load old terrain
	 * 
	 * @param game
	 * @param terraname
	 */
	public Terra(MainGame game, String terraname)
	{
		super(game);

		try
		{
			DataInputStream istream = new DataInputStream(
					new BufferedInputStream(new FileInputStream(terraname)));

			heightMap = new float[COLS][COLS];
			loadMap(istream, heightMap);

			snowMap = new float[COLS][COLS];
			loadMap(istream, snowMap);
			grassMap = new float[COLS][COLS];
			loadMap(istream, grassMap);

			istream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();

			// If failed to load old terrain, generate new terrain and save it
			createHeightMap();
			createOffsetMap();
			save(terraname);
		}

		grass = Texture.createFromFile(GRASS_NAME);
		dirt = Texture.createFromFile(DIRT_NAME);
		snow = Texture.createFromFile(SNOW_NAME);

		HashMap<Integer, String> attributes = new HashMap<Integer, String>();
		attributes.put(SNOW_OFFSET_LOC, "snowOffset");
		attributes.put(GRASS_OFFSET_LOC, "grassOffset");

		shaderProgram = ShaderProgram.createFromFiles(VERTEX_SHADER_NAME,
				FRAGMENT_SHADER_NAME, attributes);

		createList();
	}

	/**
	 * Get element in an infinitely extended and repeated map
	 * 
	 * @param src
	 * @param row
	 * @param col
	 * @return
	 */
	private float getElement(float[][] src, int row, int col)
	{
		return src[getRealCol(row)][getRealCol(col)];
	}

	/**
	 * Map a column / row to the corresponding one inside the map
	 * 
	 * @param col
	 * @return
	 */
	private int getRealCol(int col)
	{
		col %= COLS;

		if (col < 0)
		{
			col += COLS;
		}

		return col;
	}

	/**
	 * Randomly generate a map
	 * 
	 * @param m
	 * @param n
	 * @return
	 */
	private float[][] generateRandomMap(int m, int n)
	{
		float[][] randomMap = new float[m][n];

		for (int i = 0; i < m; ++i)
		{
			for (int j = 0; j < n; ++j)
			{
				randomMap[i][j] = (float) Math.random();
			}
		}
		return randomMap;
	}

	/**
	 * Compute a height map
	 */
	private void createHeightMap()
	{
		heightMap = generateRandomMap(COLS, COLS);

		stretch();
		
		Filter gaussianFilter = new GaussianFilter(HALF_FILTER_SIZE,
				HALF_FILTER_SIZE);
		Filter meanFilter = new MeanFilter(HALF_FILTER_SIZE, HALF_FILTER_SIZE);

		CombineFilter filter = new CombineFilter(HALF_FILTER_SIZE,
				HALF_FILTER_SIZE);
		filter.addFilter(gaussianFilter, GAUSSIAN_PROP);
		filter.addFilter(meanFilter, 1.0f - GAUSSIAN_PROP);

		// Use both a Gaussian and a mean filter to smooth the map
		heightMap = filter.convolute(heightMap);

		stretch();

		for (int col = HALF_COLS - MULTIPLE_FLAT_COL; col <= HALF_COLS + MULTIPLE_FLAT_COL; ++col)
		{
			if (col < 0 || col >= COLS)
			{
				continue;
			}
			
			float colCenter = Math.abs(col - HALF_COLS) / (float) FLAT_COL;
			
			for (int row = HALF_COLS - MULTIPLE_FLAT_ROW; row <= HALF_COLS + MULTIPLE_FLAT_ROW; ++row)
			{
				if (row < 0 || row >= COLS)
				{
					continue;
				}
				
				float rowCenter = Math.abs(row - HALF_COLS) / (float) FLAT_ROW;
				float center = (float) Math.sqrt(colCenter * colCenter + rowCenter * rowCenter);
				
				if (center < 1 + 1e-6)
				{
					heightMap[row][col] = PITCH;
				}
				else
				{
					center -= 1;
					heightMap[row][col] = (float) (PITCH + (heightMap[row][col] - PITCH) * Math.pow(center / (MULTIPLE_FLAT - 1), FLAT_SLOPE));
				}
			}
		}
	}

	/**
	 * Adjust the heights
	 */
	private void stretch()
	{
		float high = LOWEST, low = HIGHEST;

		for (int col = 0; col < COLS; ++col)
		{
			for (int row = 0; row < COLS; ++row)
			{
				if (heightMap[row][col] > high)
				{
					high = heightMap[row][col];
				}

				if (heightMap[row][col] < low)
				{
					low = heightMap[row][col];
				}
			}
		}

		float ratio = (HIGHEST - LOWEST) / (high - low);
		
		for (int col = 0; col < COLS; ++col)
		{
			for (int row = 0; row < COLS; ++row)
			{
				heightMap[row][col] = (heightMap[row][col] - low) * ratio
						+ LOWEST;
			}
		}
	}

	/**
	 * Compute the offset map so that the heights of different layers (snow,
	 * dirt, grass) will not be identical for each position
	 */
	private void createOffsetMap()
	{
		Filter meanFilter = new GaussianFilter(HALF_SNOW_FILTER_SIZE,
				HALF_SNOW_FILTER_SIZE);
		float[][] randomMap = generateRandomMap(COLS, COLS);
		snowMap = meanFilter.convolute(randomMap);

		for (int i = 0; i < COLS; ++i)
		{
			for (int j = 0; j < COLS; ++j)
			{
				snowMap[i][j] = (snowMap[i][j] - 0.5f) * MAX_SNOW_OFFSET;
			}
		}

		meanFilter = new GaussianFilter(HALF_GRASS_FILTER_SIZE,
				HALF_GRASS_FILTER_SIZE);
		randomMap = generateRandomMap(COLS, COLS);
		grassMap = meanFilter.convolute(randomMap);

		for (int i = 0; i < COLS; ++i)
		{
			for (int j = 0; j < COLS; ++j)
			{
				grassMap[i][j] = (grassMap[i][j] - 0.5f) * MAX_GRASS_OFFSET;
			}
		}
	}

	/**
	 * Compute normals on each vertex
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	private Vector3f computeNormals(int row, int col)
	{
		Vector3f normal = new Vector3f();

		normal.x = getElement(heightMap, row, col - 1)
				- getElement(heightMap, row, col + 1);
		normal.y = 2 * CELL_SIZE;
		normal.z = getElement(heightMap, row - 1, col)
				- getElement(heightMap, row + 1, col);

		float sum = (float) Math.sqrt(normal.x * normal.x + normal.y * normal.y
				+ normal.z * normal.z);

		normal.x /= sum;
		normal.y /= sum;
		normal.z /= sum;

		return normal;
	}

	/**
	 * Create 49 different display list, so that the program can select one from
	 * them based on the position of myself
	 */
	@Override
	protected void createList()
	{
		list = GL11.glGenLists(1);
		GL11.glNewList(list, GL11.GL_COMPILE);
		{
			shaderProgram.bind();

			// Set three textures
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, grass.getId());
			shaderProgram.setUniformi("grass", 0);

			GL13.glActiveTexture(GL13.GL_TEXTURE1);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, dirt.getId());
			shaderProgram.setUniformi("dirt", 1);

			GL13.glActiveTexture(GL13.GL_TEXTURE2);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, snow.getId());
			shaderProgram.setUniformi("snow", 2);

			GL13.glActiveTexture(GL13.GL_TEXTURE0);

			// Set three heights of layers
			shaderProgram.setUniformf("low", LOW);
			shaderProgram.setUniformf("mid", MID);
			shaderProgram.setUniformf("high", HIGH);

			// Set the shininess, which will only be used for snow
			GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, SHINE);

			int lastCol = 0;
			float lastX = lastCol * CELL_SIZE - HALF_SIZE;
			float lastTexX = lastX / (HALF_SIZE * 2);

			// Draw quads to build terrain
			for (int col = 1; col < COLS; ++col)
			{
				float x = col * CELL_SIZE - HALF_SIZE;
				float texX = x / (HALF_SIZE * 2);

				GL11.glBegin(GL11.GL_QUAD_STRIP);
				{
					for (int row = 0; row < COLS; ++row)
					{
						float z = row * CELL_SIZE - HALF_SIZE;
						float texZ = z / (HALF_SIZE * 2);

						// Set the normal, UV coordinate, vertex position and
						// random
						// offsets of heights of snow and grass layers for each
						// vertex
						Vector3f normalLeft = computeNormals(row, lastCol);
						GL11.glNormal3f(normalLeft.x, normalLeft.y,
								normalLeft.z);
						GL11.glTexCoord2f(lastTexX, texZ);
						GL20.glVertexAttrib1f(SNOW_OFFSET_LOC,
								getElement(snowMap, row, lastCol));
						GL20.glVertexAttrib1f(GRASS_OFFSET_LOC,
								getElement(grassMap, row, lastCol));
						GL11.glVertex3f(lastX,
								getElement(heightMap, row, lastCol), z);

						Vector3f normalRight = computeNormals(row, col);
						GL11.glNormal3f(normalRight.x, normalRight.y,
								normalRight.z);
						GL11.glTexCoord2f(texX, texZ);
						GL20.glVertexAttrib1f(SNOW_OFFSET_LOC,
								getElement(snowMap, row, col));
						GL20.glVertexAttrib1f(GRASS_OFFSET_LOC,
								getElement(grassMap, row, col));
						GL11.glVertex3f(x, getElement(heightMap, row, col), z);
					}
				}
				GL11.glEnd();

				lastCol = col;
				lastX = x;
				lastTexX = texX;
			}

			Texture.unbind();
			ShaderProgram.unbind();
		}
		GL11.glEndList();
	}

	/**
	 * Get the height of a specified position by linear interpolation
	 * 
	 * @param x
	 * @param z
	 * @return
	 */
	public float getHeight(float x, float z)
	{
		int col, col0, col1, row, row0, row1;

		col = (int) (x / CELL_SIZE);
		col0 = x > 0 ? col : col - 1;
		col1 = col0 + 1;

		row = (int) (z / CELL_SIZE);
		row0 = z > 0 ? row : row - 1;
		row1 = row0 + 1;

		float h00, h01, h10, h11;

		h00 = getElement(heightMap, row0, col0);
		h01 = getElement(heightMap, row0, col1);
		h10 = getElement(heightMap, row1, col0);
		h11 = getElement(heightMap, row1, col1);

		float pX, pZ;

		pX = (x - col0 * CELL_SIZE) / CELL_SIZE;
		pZ = (z - row0 * CELL_SIZE) / CELL_SIZE;

		float height = h00 * (1.0f - pX) * (1.0f - pZ) + h11 * pX * pZ + h01
				* pX * (1.0f - pZ) + h10 * pZ * (1.0f - pX);

		return height;
	}

	/**
	 * Save generated terrain to file
	 * 
	 * @param terraname
	 */
	public void save(String terraname)
	{
		try
		{
			DataOutputStream ostream = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(terraname)));

			saveMap(ostream, heightMap);

			saveMap(ostream, snowMap);
			saveMap(ostream, grassMap);

			ostream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Load a two-dimensional float array to file
	 * 
	 * @param istream
	 * @param map
	 * @throws IOException
	 */
	private void loadMap(DataInputStream istream, float[][] map)
			throws IOException
	{
		for (int i = 0; i < COLS; ++i)
		{
			for (int j = 0; j < COLS; ++j)
			{
				map[i][j] = istream.readFloat();
			}
		}
	}

	/**
	 * Save a two-dimensional float array to file
	 * 
	 * @param ostream
	 * @param map
	 * @throws IOException
	 */
	private void saveMap(DataOutputStream ostream, float[][] map)
			throws IOException
	{
		for (int i = 0; i < COLS; ++i)
		{
			for (int j = 0; j < COLS; ++j)
			{
				ostream.writeFloat(map[i][j]);
			}
		}
	}
}
