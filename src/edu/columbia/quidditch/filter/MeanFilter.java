package edu.columbia.quidditch.filter;

/**
 * Mean filter
 * 
 * @author Yuqing Guan
 * 
 */
public class MeanFilter extends Filter
{
	public MeanFilter(int halfWidth, int halfHeight)
	{
		super(halfWidth, halfHeight);

		float mean = 1.0f / noElements;

		for (int j = 0; j < height; ++j)
		{
			for (int i = 0; i < width; ++i)
			{
				matrix[j][i] = mean;
			}
		}
	}

}
