import java.awt.image.WritableRaster;
import java.util.ArrayList;

public class HoughTransform
{
	
	
	public static double quantizationStep(double min, double max, int increments)
	{
		return (min+max)/increments;
	}
	
	public static int quantize(double val, double step, double min)
	{
		return (int) (Math.round(val)-min);
		//return (int) (Math.round(val/step)*step); //Math.round(val/step)*step;
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
	
	public static ArrayList<LineSegment> transformLine(int[] pixels, int width, int height, int dIncrements, int thetaIncrements, int threshold)
	{
		double maxD = Math.sqrt(width*width + height*height);
		double minD = -maxD;
		System.out.println(minD);
		
		int tStep = 15; //quantizationStep(0, 180, thetaIncrements);
		double dStep = 2; //quantizationStep(0, Math.max(width, height), dIncrements);
		thetaIncrements = (int) (180/tStep+1);
		dIncrements = (int) (2*maxD);
		
		int[][] accumulator = new int[thetaIncrements][dIncrements];
		//int[][][][] accumPoints = new int[thetaIncrements][dIncrements][][2];
		ArrayList<Integer[]>[][] accumPoints = new ArrayList[thetaIncrements][dIncrements];
		
		
		
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				int pVal = getPixelFromArray(pixels, width, i, j);
				if (pVal != -1)
				{
					for (int theta = 0; theta <= 180; theta += tStep)
					{
						double angle = Math.toRadians(theta);
						int d = quantize(j*Math.sin(angle) + i*Math.cos(angle), dStep, minD);
						//System.out.println(d);
						accumulator[theta/tStep][d]++;
						if (accumPoints[theta/tStep][d] == null)
						{
							accumPoints[theta/tStep][d] = new ArrayList<Integer[]>();
						}
						accumPoints[theta/tStep][d].add(new Integer[] {i, j});
						/*
						if (i <= minXPoint[d][theta/tStep][0])
						{
							minXPoint[d][theta/tStep][0] = i;
							minXPoint[d][theta/tStep][1] = j;
						}
						else if (i >= maxXPoint[d][theta/tStep][0])
						{
							maxXPoint[d][theta/tStep][0] = i;
							maxXPoint[d][theta/tStep][1] = j;
						}
						*/
					}
				}
			}
		}
		System.out.println("Min points for threshold = " + threshold);
		ArrayList<LineSegment> lines = new ArrayList<LineSegment>();
		for (int t = 0; t < accumulator.length; t++)
		{
			for (int d = 0; d < accumulator[t].length; d++)
			{
				if (accumulator[t][d] >= threshold)
				{
					System.out.println(accumulator[t][d]);
					
					LineSegment l = new LineSegment();
					int x1 = width;
					int y1 = height;
					int x2 = 0;
					int y2 = 0;
					for (Integer[] pt : accumPoints[t][d])
					{
						if (pt[0] < x1)
						{
							x1 = pt[0];
							y1 = pt[1];
						}
						if (pt[0] > x2)
						{
							x2 = pt[0];
							y2 = pt[1];
						}
					}
					l.setX1(x1);
					l.setX2(x2);
					l.setY1(y1);
					l.setY2(y2);
					System.out.println(l);
					lines.add(l);
				}
			}
		}
		
		return lines;
	}
	
	public static ArrayList<LineSegment> transform(WritableRaster raster, int threshold)
	{
		int width = raster.getWidth();
		int height = raster.getHeight();
		int[] pixels = raster.getPixels(0, 0, width, height, (int[]) null);
		
		return transformLine(pixels, width, height, 100, 100, threshold);
	}
}
