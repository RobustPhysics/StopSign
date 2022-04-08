import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Collections;

public class PreProcessing
{
	public static byte[] changeBrightness(byte[] data, int offset)
	{
		if (offset < -127 || offset > 127)
		{
			System.out.println("ERROR! You cannot use an offset less than -127 or greater than 127!!");
			return data;
		}
		
		
		for (int i = 0; i < data.length; i++)
		{
			int newVal = data[i] + offset;
			int oldVal2 = newVal;
			if (newVal > 127)
				newVal = 127;
			else if (newVal < -128)
				newVal = -128;
			byte oldVal = data[i];
			data[i] = (byte) newVal;
			//System.out.println(data[i]);
			if (Math.abs((int) data[i] - (int) oldVal) > offset)
			{
				System.out.println("Byte went from " + oldVal + " to " + data[i]);
				System.out.println("Int went from " + oldVal2 + " to " + newVal);
			}
		}
		
		return data;
	}
	
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
	
	
	public static void changeBrightness(WritableRaster raster, int bOffset)
	{
		int[] pixel = new int[1];
		for (int i = 0; i < raster.getWidth(); i++)
		{
			for (int j = 0; j < raster.getHeight(); j++)
			{
				raster.getPixel(i, j, pixel);
				pixel[0] += bOffset;
				bound(pixel);
				raster.setPixel(i, j, pixel);
			}
		}
	}
	
	public static void changeContrast(WritableRaster raster, float scale)
	{
		int[] pixel = new int[1];
		for (int i = 0; i < raster.getWidth(); i++)
		{
			for (int j = 0; j < raster.getHeight(); j++)
			{
				raster.getPixel(i, j, pixel);
				pixel[0] = (int) (127 + scale*(pixel[0]-127));
				bound(pixel);
				raster.setPixel(i, j, pixel);
			}
		}
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
	
	public static void localAverage(WritableRaster raster, int windowX, int windowY)
	{
		int[] pixels = raster.getPixels(0, 0, raster.getWidth(), raster.getHeight(), (int[]) null);
		int[] pixel = new int[1];
		for (int i = 0; i < raster.getWidth(); i++)
		{
			for (int j = 0; j < raster.getHeight(); j++)
			{
				raster.getPixel(i, j, pixel);
				int sum = pixel[0];
				int numPixels = 0;
				for (int cellOffsetX = -windowX/2; cellOffsetX <= windowX/2; cellOffsetX++)
				{
					for (int cellOffsetY = -windowY/2; cellOffsetY <= windowX/2; cellOffsetY++)
					{
						//if (numPixels == 0)
							//System.out.println("Offset = (" + cellOffsetX + ", " + cellOffsetY + ")");
						if (cellOffsetY != 0 || cellOffsetX != 0)
						{
							int pVal = getPixelFromArray(pixels, raster.getWidth(), i+cellOffsetX, j+cellOffsetY);
							if (pVal != 0)
							{
								sum += pVal;
								numPixels++;
								//System.out.println(numPixels);
							}
							else if (numPixels == 0)
							{
								//System.out.println("pVal is -1 at " + (i+cellOffsetX) + ", " + (j+cellOffsetY));
							}
						}
						//else
							//System.out.println("x = " + cellOffsetX + " , y = " + cellOffsetY);
							
					}
				}
				sum /= (numPixels);
				pixel[0] = sum;
				bound(pixel);
				raster.setPixel(i, j, pixel);
			}
		}
	}
	
	public static void medianSmooth(WritableRaster raster, int windowX, int windowY)
	{
		int[] pixels = raster.getPixels(0, 0, raster.getWidth(), raster.getHeight(), (int[]) null);
		int[] pixel = new int[1];
		for (int i = 0; i < raster.getWidth(); i++)
		{
			for (int j = 0; j < raster.getHeight(); j++)
			{
				raster.getPixel(i, j, pixel);
				ArrayList<Integer> nearbyValues = new ArrayList<Integer>();
				nearbyValues.add(pixel[0]);
				for (int cellOffsetX = -windowX/2; cellOffsetX <= windowX/2; cellOffsetX++)
				{
					for (int cellOffsetY = -windowY/2; cellOffsetY <= windowX/2; cellOffsetY++)
					{
						if (cellOffsetY != 0 || cellOffsetX != 0)
						{
							int pVal = getPixelFromArray(pixels, raster.getWidth(), i+cellOffsetX, j+cellOffsetY);
							if (pVal != -1)
								nearbyValues.add(pVal);
						}
					}
				}
				
				Collections.sort(nearbyValues);
				
				int median = (nearbyValues.get(nearbyValues.size()/2) + nearbyValues.get(nearbyValues.size()/2-1))/2;
				
				pixel[0] = median;
				bound(pixel);
				raster.setPixel(i, j, pixel);
			}
		}
	}
	
	public static void localAverage(WritableRaster raster)
	{
		localAverage(raster, 3, 3);
	}
	
	private static final double SQRT2PI = Math.sqrt(2*Math.PI);
	
	public static void gaussianBlur(WritableRaster raster, int radius, double intensity)
	{
		//For this function, I used the following as a heavy reference: https://github.com/rstreet85/JCanny/blob/master/src/jcanny/Gaussian.java
		int width = raster.getWidth();
		int height = raster.getHeight();
		
		int[] rawPixels = raster.getPixels(0, 0, width, height, (int[]) null);
		double intensitySq = intensity*intensity;
		double normal = 0;
		double invIntensSqrPi = 1 / (SQRT2PI * intensity);
		double[] mask = new double[2*radius+1];
		int[][] newPixels = new int[height-2*radius][width-2*radius];
		
		//Produces something called a kernal...would have been helpful for other functions.
		for (int x = -radius; x < radius + 1; x++)
		{
			double e = Math.exp(-((x^2) / intensitySq));
			
			mask[x+radius] = invIntensSqrPi * e;
			normal += mask[x+radius];
		}
		
		//convolve image horizontally
		for (int r = radius; r < height - radius; r++)
		{
			for (int c = radius; c < width - radius; c++)
			{
				double sum = 0;
				
				for (int r2 = -radius; r2 < radius+1; r2++)
				{
					int index = getIndexFromArray(rawPixels, width, c+r2, r);
					sum += (mask[r2 + radius] * rawPixels[index]);
				}
				
				sum /= normal;
				newPixels[r-radius][c-radius] = (int) Math.round(sum);
			}
		}
		
		
		//vertically
		for (int r = radius; r < height - radius; r++)
		{
			for (int c = radius; c < width - radius; c++)
			{
				double sum = 0;
				
				for (int r2 = -radius; r2 < radius+1; r2++)
				{
					int index = getIndexFromArray(rawPixels, width, c, r+r2);
					sum += (mask[r2 + radius] * rawPixels[index]);
				}
				
				sum /= normal;
				newPixels[r-radius][c-radius] = (int) Math.round(sum);
			}
		}
		
		for (int i = 0; i < width - 2 * radius; i++)
		{
			for (int j = 0; j < height - 2*radius; j++)
			{
				int index = getIndexFromArray(rawPixels, width, i, j);
				rawPixels[index] = newPixels[j][i];
			}
		}
		
		raster.setPixels(0, 0, raster.getWidth(), raster.getHeight(), rawPixels);
	}
	
	public static void gaussian(WritableRaster raster)
	{
		System.out.println("Unimplemented!");
	}
	
	public static void medianSmooth(WritableRaster raster)
	{
		medianSmooth(raster, 3, 3);
	}
	
	public static void laplacianSharpen(WritableRaster raster)
	{
		System.out.println("Unimplemented!");
	}
	
	public static void histogramEqualize(WritableRaster raster)
	{
		int[] histogram = new int[256];
		int[] pixels = raster.getPixels(0, 0, raster.getWidth(), raster.getHeight(), (int[]) null);
		for (int i = 0; i < pixels.length; i++)
		{
			histogram[pixels[i]] += 1;
		}
		
		for (int i = 1; i < 256; i++)
		{
			//System.out.println(histogram[i]);
			histogram[i] = histogram[i-1] + histogram[i];
			
		}
		
		float[] normHistogram = new float[256];
		for (int i = 0; i < 256; i++)
		{
			normHistogram[i] = ((float) histogram[i]*255.0f)/pixels.length;
		}
		
		
		for (int i = 0; i < pixels.length; i++)
		{
			int newPixel = getBounded((int) normHistogram[pixels[i]]);
			pixels[i] = newPixel;
		}
		
		raster.setPixels(0, 0, raster.getWidth(), raster.getHeight(), pixels);
	}
	
	
	
	/*
	private static int getPixel(WritableRaster raster, int x, int y)
	{
		return raster.getPixel(x, y, (int[]) null)[0];
	}
	
	private static void setPixel(WritableRaster raster, int x, int y, int val)
	{
		int[] pixel = {val};
		raster.setPixel(x, y, pixel);
	}
	*/
	
	/*
	public static void changeBrightness2(WritableRaster raster, int bOffset)
	{
		int[] pixels = raster.getPixels(0, 0, raster.getWidth(), raster.getHeight(), (int[]) null);
		
		for (int i = 0; i < pixels.length; i++)
		{
			pixels[i] += bOffset;
			pixels[i] = getBounded(pixels[i]);
		}
		
		raster.setPixels(0, 0, raster.getWidth(), raster.getHeight(), pixels);
	}
	*/
}
