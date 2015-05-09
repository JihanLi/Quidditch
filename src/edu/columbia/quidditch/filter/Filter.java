package edu.columbia.quidditch.filter;

/**
 * Abstract filter class
 * @author Yuqing Guan
 *
 */
public abstract class Filter
{
	protected int width, height, halfWidth, halfHeight, noElements;
	protected float[][] matrix;
	
	public Filter(int halfWidth, int halfHeight)
	{
		this.halfWidth = halfWidth;
		this.halfHeight = halfHeight;
		
		width = halfWidth * 2 + 1;
		height = halfHeight * 2 + 1;
		
		noElements = width * height;
		
		matrix = new float[height][width];
	}
	
	/**
	 * Use neighboring elements to compute filtered value
	 * If an element is at a corner or an edge,
	 * I will use elements on the opposite corner or edge as
	 * its neighboring elements, so the smoothed map can be repeated seamlessly
	 * @param src
	 * @return
	 */
	public float[][] convolute(float[][] src)
	{
		int srcHeight = src.length;
		int srcWidth = src[0].length;
		
		float[][] dst = new float[srcHeight][srcWidth];
		
		for (int y = 0; y < srcHeight; ++y)
		{
			for (int x = 0; x < srcWidth; ++x)
			{
				for (int j = 0; j < height; ++j)
				{
					int offsetY = j - halfHeight;
					
					for (int i = 0; i < width; ++i)
					{
						int offsetX = i - halfWidth;
						
						dst[y][x] += matrix[j][i] * getElement(src, y - offsetY, x - offsetX);
					}
				}
			}
		}
		
		return dst;
	}
	
	/**
	 * Get element from the matrix
	 * if the location is out of the matrix
	 * move it to the corresponding location in the matrix
	 * @param src
	 * @param y
	 * @param x
	 * @return
	 */
	private float getElement(float[][] src, int y, int x)
	{
		int srcHeight = src.length;
		int srcWidth = src[0].length;
		
		if (x < 0)
		{
			x += srcWidth;
		}
		else if (x >= srcWidth)
		{
			x -= srcWidth;
		}
		
		if (y < 0)
		{
			y += srcHeight;
		}
		else if (y >= srcHeight)
		{
			y -= srcHeight;
		}
		
		return src[y][x];
	}
}
