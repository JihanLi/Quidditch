package edu.columbia.quidditch.filter;

/**
 * Gaussian fitler
 * @author Yuqing Guan
 *
 */
public class GaussianFilter extends Filter
{
	/**
	 * Compute two-dimensional Gaussian distribution
	 * @param halfWidth
	 * @param halfHeight
	 */
	public GaussianFilter(int halfWidth, int halfHeight)
	{
		super(halfWidth, halfHeight);
		
		float sigmaWidth = halfWidth / 3.0f;
		float sigmaHeight = halfHeight / 3.0f;
		
		float prefix = (float) (0.5 / Math.PI / (sigmaWidth * sigmaHeight));
		
		float sigmaWidthSquare = sigmaWidth * sigmaWidth;
		float sigmaHeightSquare = sigmaHeight * sigmaHeight;
		
		float sum = 0;
		
		for (int j = 0; j < height; ++j)
		{
			int y = j - halfHeight;
			
			for (int i = 0; i < width; ++i)
			{
				int x = i - halfWidth;
				
				matrix[j][i] = (float) (prefix * Math.exp(- (x * x) / (2 * sigmaWidthSquare) - (y * y) / (2 * sigmaHeightSquare)));
				sum += matrix[j][i];
			}
		}
		
		// Normalize the matrix
		for (int j = 0; j < height; ++j)
		{
			for (int i = 0; i < width; ++i)
			{
				matrix[j][i] /= sum;
			}
		}
	}

}
