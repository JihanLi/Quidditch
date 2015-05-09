package edu.columbia.quidditch.filter;

/**
 * Combine different kinds of filters
 * @author Yuqing Guan
 *
 */
public class CombineFilter extends Filter
{
	public CombineFilter(int halfWidth, int halfHeight)
	{
		super(halfWidth, halfHeight);
	}
	
	public void addFilter(Filter otherFilter, float weight)
	{
		for (int j = 0; j < height; ++j)
		{
			for (int i = 0; i < width; ++i)
			{
				matrix[j][i] += otherFilter.matrix[j][i] * weight;
			}
		}
	}
}
