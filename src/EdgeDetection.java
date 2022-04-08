import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Objects;

public class EdgeDetection
{
	private static final int[][][] defaultWeights = 
	{
			{
				{-1, 0, 1},
				{-1, 0, 1},
				{-1, 0, 1}
			},
			{
				{1, 1, 1},
				{0, 0, 0},
				{-1, -1, -1}
			},
			{
				{1, 1, 0},
				{1, 0, -1},
				{0, -1, -1}
			},
			{
				{0, 1, 1},
				{-1, 0, 1},
				{-1, -1, 0}
			}
	};
	
	private static final int[][][] sobelWeights = 
	{
			{ //Gradient_X
				{-1, 0, 1},
				{-2, 0, 2},
				{-1, 0, 1}
			},
			{ //Gradient_Y
				{1, 2, 1},
				{0, 0, 0},
				{-1, -2, -1}
			}
	};
	
	private static int getBounded(int pixel)
	{
		if (pixel > 255)
			pixel = 255;
		else if (pixel < 0)
			pixel = 0;
		
		return pixel;
	}
	
	private static void bound(int[] pixel)
	{
		pixel[0] = getBounded(pixel[0]);
	}
	
	private static int getIndexFromArray(int[] pixels, int width, int x, int y)
	{
		int pos = y*width + x;
		if (pos >= pixels.length || pos < 0 || x < 0 || y < 0 || x >= width)
		{
			//System.out.println("ERROR! Out of bounds!");
			return -1;
		}
		
		return pos;
	}
	
	private static int getPixelFromArray(int[] pixels, int width, int x, int y)
	{
		int pos = y*width + x;
		if (pos >= pixels.length || pos < 0 || x < 0 || y < 0 || x >= width)
		{
			//System.out.println("ERROR! Out of bounds!");
			return -1;
		}
		
		return pixels[pos];
	}
	
	
	private static int[] getGradientSums(int[] pixels, int width, int x, int y, int[][][] weights)
	{
		int numOps = weights.length;
		int winY = weights[0].length;
		int winX = weights[0][0].length;
		
		int[] sums = new int[numOps];
		for (int i = 0; i < numOps; i++)
			sums[i] = 0;
		
		for (int offsetX = -winX/2; offsetX <= winX/2; offsetX++)
		{
			for (int offsetY = -winY/2; offsetY <= winX/2; offsetY++)
			{
				if (offsetX != 0 || offsetY != 0)
				{
					int pVal = getPixelFromArray(pixels, width, x+offsetX, y+offsetY);
					if (pVal != -1)
					{
						for (int i = 0; i < numOps; i++)
						{
							double weight = weights[i][offsetY+winY/2][offsetX+winX/2];
							//System.out.println("Weight = " + weight);
							//System.out.println("pVal = " + pVal);
							sums[i] += (int) (pVal*weight);
							//System.out.println("Sum[" + i + "] = " + sums[i]);
						}
					}
				}
			}
		}
		
		return sums;
	}
	
	private static class CannyPoint
	{
		/*
		I used the following as a reference: https://github.com/rstreet85/JCanny/tree/master/src/jcanny
		
		(Note that code from this person had some bugs, mainly bad boundary conditions.
		 Not much else was changed, however.)
		 */
		double g = 0;
		double theta = 0;
		boolean isStrong = false;
		boolean isWeak = false;
		
		public void setTheta(double theta)
		{
			if (theta < 0)
				theta += 360;
			
			theta = Math.round(theta/45.0) * 45.0;
			if (theta == 0 || theta == 180 || theta == 360)
				theta = 0;
			else if (theta == 45 || theta == 225)
				theta = 45;
			else if (theta == 90 || theta == 270)
				theta = 90;
			else if (theta == 135 || theta == 315)
				theta = 135;
		}
	}
	
	public static void cannyEdgeDetect(WritableRaster raster, int strongT, int weakT)
	{
		int height = raster.getHeight();
		int width = raster.getWidth();
		int[] pixels = raster.getPixels(0, 0, width, height, (int[]) null);
		int[] whitePixel = {255};
		int[] blackPixel = {0};
		double sum = 0;
		int mean = 0;
		double var = 0;
		int stDev = 0;
		
		CannyPoint[][] points = new CannyPoint[height][width];
		
		//Get gradient and orientation
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				int[] sums = getGradientSums(pixels, width, i, j, sobelWeights);
				double G = Math.sqrt(sums[0]*sums[0] + sums[1]*sums[1]);
				/*
				if (Double.isNaN(G))
				{
					System.out.println(sums[0]*sums[0]);
					System.out.println(sums[1]^2);
					System.out.println((sums[0]^2 + sums[1]^2));
					System.out.println("G = " + G);
				}
				*/
				double theta = Math.atan2(sums[1], sums[0]) * 180 / Math.PI;
				if (theta < 0)
					theta += 360;
				
				CannyPoint p = new CannyPoint();
				p.g = G;
				sum += G;
				p.setTheta(theta);
				points[j][i] = p;
			}
		}
		
		System.out.println("Sum = " + sum);
		
		mean = (int) Math.round(sum/pixels.length);
		
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				double diff = points[j][i].g - mean;
				
				var += (diff*diff);
			}
		}
		
		stDev = (int) Math.sqrt(var/pixels.length);
		
		
		strongT = mean + (1 * stDev);
		weakT = (int) (strongT * 0.2);
		System.out.println("Canny 1");
		System.out.println("Mean = " + mean);
		System.out.println("stDev = " + stDev);
		System.out.println("Strong Threshold = " + strongT + " / Weak = " + weakT);
		
		//Suppress and identify strong/weak edges
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				CannyPoint p = points[j][i];
				double g = p.g;
				double g2 = 0;
				double g3 = 0;
				
				
				switch ((int) p.theta)
				{
					case 0:
						if (i-1 >= 0)
							g2 = points[j][i-1].g;
						if (i+1 < width)
							g3 = points[j][i+1].g;
							
						if (g < g2 && g < g3)
							points[j][i].g = 0;
						break;
					case 45:
						if (j-1 >= 0 && i+1 < width)
							g2 = points[j-1][i+1].g;
						if (i-1 >= 0 && j+1 < height)
							g3 = points[j+1][i-1].g;
							
						if (g < g2 && g < g3)
							points[j][i].g = 0;
						break;
					case 90:
						if (j-1 >= 0)
							g2 = points[j-1][i].g;
						if (j+1 < height)
							g3 = points[j+1][i].g;
							
						if (g < g2 && g < g3)
							points[j][i].g = 0;
						break;
					case 135:
						if (i-1 >= 0 && j-1 >= 0)
							g2 = points[j-1][i-1].g;
						if (j+1 < height && i+1 < width)
							g3 = points[j+1][i+1].g;
							
						if (g < g2 && g < g3)
							points[j][i].g = 0;
						break;
				}
				
				if (points[j][i].g >= strongT)
					points[j][i].isStrong = true;
				else if (points[j][i].g >= weakT)
					points[j][i].isWeak = true;
				else
					points[j][i].g = 0;
			}
		}
		
		//hysteresis
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				CannyPoint p = points[j][i];
				int pos = getIndexFromArray(pixels, width, i, j);
				if (p.isStrong)
					//color in black
					pixels[pos] = 0;
				else if (p.isWeak)
				{
					boolean foundStrong = false;
					for (int cellOffsetX = -1; cellOffsetX <= 1; cellOffsetX++)
					{
						for (int cellOffsetY = -1; cellOffsetY <= 1; cellOffsetY++)
						{
							if (j+cellOffsetY >= 0 && j+cellOffsetY < height && i + cellOffsetX >= 0 && i + cellOffsetX < width)
							{
								CannyPoint nearby = points[j+cellOffsetY][i+cellOffsetX];
								if (nearby.isStrong)
								{
									foundStrong = true;
									break;
								}
							}
						}
					}
					if (foundStrong)
						pixels[pos] = 0;
					else
						pixels[pos] = 255;
				}
				else
					//color in white
					pixels[pos] = 255;
			}
		}
		
		
		raster.setPixels(0,0,width,height,pixels);
	}
	
	public static void detectEdges(WritableRaster raster, int opMethod, int threshold)
	{
		int[][][] weights;
		if (opMethod == 0)
		{
			weights = defaultWeights;
		}
		else if (opMethod == 1)
			weights = sobelWeights;
		else
		{
			System.out.println("Error! No method found.");
			return;
		}
		
		int[] pixels = raster.getPixels(0, 0, raster.getWidth(), raster.getHeight(), (int[]) null);
		int[] whitePixel = {255};
		int[] blackPixel = {0};
		for (int i = 0; i < raster.getWidth(); i++)
		{
			for (int j = 0; j < raster.getHeight(); j++)
			{
				int[] sums = getGradientSums(pixels, raster.getWidth(), i, j, weights);
				int totalSum = 0;
				for (int sumIndex = 0; sumIndex < sums.length; sumIndex++)
				{
					totalSum += sums[sumIndex];
				}
				//System.out.println("Sum = " + totalSum);
				if (totalSum > threshold)
				{
					raster.setPixel(i, j, blackPixel);
				}
				else
					raster.setPixel(i, j, whitePixel);
			}
		}
	}
	
	private static class EdgePoint
	{
		public int pointType = 1; //1 is contour, 2 is final
		public int x = -1;
		public int y = -1;
		
		@Override
		public boolean equals(Object obj)
		{
			boolean val = false;
			if (obj instanceof EdgePoint)
			{
				EdgePoint p2 = (EdgePoint) obj;
				if (p2.x == this.x && p2.y == this.y && p2.pointType == this.pointType)
					val = true;
			}
			
			return val;
		}
		
		@Override
		public int hashCode()
		{
			return Objects.hash(x, y, pointType);
		}
	}
	
	/*
	1 = need black here
	0 = need white here
	-1 = doesn't matter
	2 = need at least 1 here
	3 = need at least 1 here
	 */
	private static int[][][] aiPoints = 
	{
			{
				{2,2,2},
				{0,1,0},
				{3,3,3}
			},
			{
				{2,0,3},
				{2,1,3},
				{2,0,3}
			},
			{
				{0,3,3},
				{2,1,3},
				{2,2,0}
			},
			{
				{2,2,0},
				{2,1,3},
				{0,3,3}
			}
	};
	
	private static int[][][] biPoints = 
	{
			{
				{2,2,2},
				{-1,1,0},
				{0,1,-1}
			},
			{
				{2,-1,0},
				{2,1,1},
				{2,0,-1}
			},
			{
				{-1,1,0},
				{0,1,-1},
				{2,2,2}
			},
			{
				{-1,0,2},
				{1,1,2},
				{0,-1,2}
			},
	};
	
	/*
	1 = b0 and b1
	2 = b2 and b3
	3 = b0 and b3
	4 = b1 and b2
	
	1 = b0 and b1
	2 = b2 and b3
	3 = b0 and b3
	4 = b1 and b2
	
	1 = 0 and 1
	2 = 2 and 3
	3 = 0 and 3
	4 = 1 and 2
	 */
	private static int[][] biPointPairs = {
			{0, 1},
			{2, 3},
			{0, 3},
			{1, 2}
	};
	
	private static boolean matchesPattern(int[] pixels, int width, int x, int y, int[][] offsets)
	{
		int matches2 = 0;
		int matches3 = 0;
		boolean needMatches3 = false;
		boolean matchConstants = true;
		for (int oX = 0; oX < offsets.length; oX++)
		{
			for (int oY = 0; oY < offsets[0].length; oY++)
			{
				int code = offsets[oY][oX];
				int oVal = getPixelFromArray(pixels, width, x+oX, y+oY);
				if (oVal != -1)
				{
					if (code == 2 && oVal == 0)
						matches2++;
					if (code == 3)
					{
						needMatches3 = true;
						if (oVal == 0)
							matches3++;
					}
					if (code == 1 && oVal != 0)
						matchConstants = false;
					if (code == 0 && oVal == 0)
						matchConstants = false;
				}
			}
		}
		
		if (!needMatches3)
			matches3 = 1;
		
		return (matches2 > 0 && matches3 > 0 && matchConstants);
	}
	
	private static ArrayList<EdgePoint> getPoints(int[] pixels, int width, int height, int direction, int type)
	{
		/*
		direction
		1 = lower
		2 = upper
		3 = left
		4 = right
		 */
		int xOffset = 0;
		int yOffset = -1;
		switch(direction)
		{
			case 1:
				yOffset = -1;
				break;
			case 2:
				yOffset = 1;
				break;
			case 3:
				xOffset = -1;
				break;
			case 4:
				xOffset = 1;
				break;
		}
		
		ArrayList<EdgePoint> points = new ArrayList<EdgePoint>();
		
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				int current = getPixelFromArray(pixels, width, i, j);
				if (current == 0) //there is an edge element here
				{
					int pVal = getPixelFromArray(pixels, width, i+xOffset, j+yOffset);
					if (type == 1 && pVal == 255)
					{
						//contour point
						EdgePoint pt = new EdgePoint();
						pt.x = i;
						pt.y = j;
						points.add(pt);
					}
					else if (type == 2)
					{
						boolean isConstant = matchesPattern(pixels, width, i, j, aiPoints[direction-1]);
						if (!isConstant)
						{
							for (int index = 0; index < 2; index++)
							{
								isConstant = isConstant || matchesPattern(pixels, width, i, j, biPoints[biPointPairs[direction-1][index]]);
							}
						}
						
						if (isConstant)
						{
							EdgePoint pt = new EdgePoint();
							pt.pointType = 2;
							pt.x = i;
							pt.y = j;
							points.add(pt);
						}
					}
				}
			}
		}
		
		return points;
	}
	
	public static void cleanEdges(WritableRaster raster)
	{
		int[] pixels = raster.getPixels(0,0,raster.getWidth(), raster.getHeight(), (int[]) null);
		int width = raster.getWidth();
		
		for (int i = 0; i < raster.getWidth(); i++)
		{
			for (int j = 0; j < raster.getHeight(); j++)
			{
				int neighbors = 0;
				for (int offsetX = -1; offsetX <= 1; offsetX++)
				{
					for (int offsetY = -1; offsetY <= 1; offsetY++)
					{
						if (offsetX != 0 || offsetY != 0)
						{
							int pVal = getPixelFromArray(pixels, width, i+offsetX, j+offsetY);
							if (pVal == 0)
								neighbors++;
						}
					}
				}
				
				if (neighbors <= 2)
				{
					//System.out.println("Removing at " + i + ", " + j);
					pixels[getIndexFromArray(pixels, width, i, j)] = 255;
				}
			}
		}
		
		raster.setPixels(0, 0, raster.getWidth(), raster.getHeight(), pixels);
	}
	
	public static void thinEdges(WritableRaster edgeRaster)
	{
		/*
		BufferedImage finalPts = new BufferedImage(edgeRaster.getWidth(), edgeRaster.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster finalRaster = finalPts.getRaster();
		BufferedImage contourPts = new BufferedImage(edgeRaster.getWidth(), edgeRaster.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster contourRaster = contourPts.getRaster();
		*/
		
		int[] edgePixels = edgeRaster.getPixels(0,0,edgeRaster.getWidth(), edgeRaster.getHeight(), (int[]) null);
		
		int dir = 0;
		boolean madeChange = true;
		ArrayList<EdgePoint> finalPoints = getPoints(edgePixels, edgeRaster.getWidth(), edgeRaster.getHeight(), dir+1, 2);
		int index = 0;
		
		while (madeChange)
		{
			System.out.println(index++);
			madeChange = false;
			ArrayList<EdgePoint> contourPoints = getPoints(edgePixels, edgeRaster.getWidth(), edgeRaster.getHeight(), dir+1, 1);
			for (EdgePoint cPt : contourPoints)
			{
				int pos = getIndexFromArray(edgePixels, edgeRaster.getWidth(), cPt.x, cPt.y);
				if (pos != -1 && edgePixels[pos] != 255)
				{
					edgePixels[pos] = 255; //remove
					madeChange = true;
				}
			}
			for (EdgePoint fPt : finalPoints)
			{
				int pos = getIndexFromArray(edgePixels, edgeRaster.getWidth(), fPt.x, fPt.y);
				if (pos != -1 && edgePixels[pos] != 0)
				{
					edgePixels[pos] = 0; //add
					madeChange = true;
				}
			}
			
			dir = (dir+1)%4;
			ArrayList<EdgePoint> newFinal = getPoints(edgePixels, edgeRaster.getWidth(), edgeRaster.getHeight(), dir+1, 2);
			for (EdgePoint nfPt : newFinal)
			{
				if (!finalPoints.contains(nfPt))
				{
					finalPoints.add(nfPt);
					madeChange = true;
				}
			}
		}
		
		edgeRaster.setPixels(0, 0, edgeRaster.getWidth(), edgeRaster.getHeight(), edgePixels);
	}
}
