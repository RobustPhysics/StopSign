import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePanel extends JPanel
{
	private BufferedImage[] baseImages;
	private BufferedImage baseImage;
	private int imageIndex;
	
	private byte[][] rawLayerImages;
	private WritableRaster[] rawRasters;
	private BufferedImage[] layerImages;
	
	private ArrayList<LineSegment>[] lineSegments = new ArrayList[6];
	
	private boolean[] enabledLayers;
	private boolean[] modifiedLayers;
	private boolean wasModified = false;
	private int numEnabled = 0;
	
	private int canvasWidth;
	private int canvasHeight;
	
	private int cellWidth = 50;
	private int cellHeight = 50;
	
	private int gapWidth = 5;
	private int gapHeight = 5;
	
	private float imageAspect = 1.0f; //1:1
	private int imageWidth = 50;
	private int imageHeight = 50;
	private int imageOffsetX = 0; //offset from cellX
	private int imageOffsetY = 0; //offset from cellY
	
	private int realImageWidth;
	private int realImageHeight;
	
	class ResizeListener extends ComponentAdapter
	{
		public void componentResized(ComponentEvent e)
		{
			canvasWidth = getWidth();
			canvasHeight = getHeight();
			updateSize();
		}
	}
	
	public ImagePanel(File[] imagePaths)
	{
		//System.out.println(canvasWidth);
		baseImages = new BufferedImage[11];
		layerImages = new BufferedImage[6];
		rawLayerImages = new byte[6][];
		rawRasters = new WritableRaster[6];
		for (int i = 0; i < 6; i++)
		{
			lineSegments[i] = new ArrayList<LineSegment>();
		}
		
		for (int i = 0; i < baseImages.length; i++)
		{
			
			try
			{
				BufferedImage temp = ImageIO.read(imagePaths[i]);
				BufferedImage newImage = new BufferedImage(temp.getWidth(), temp.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
				
				Graphics2D g = newImage.createGraphics();
				g.drawImage(temp, 0, 0, temp.getWidth(), temp.getHeight(), null);
				g.dispose();
				
				baseImages[i] = newImage;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		
		enabledLayers = new boolean[6];
		modifiedLayers = new boolean[6];
		for (int i = 0; i < enabledLayers.length; i++)
		{
			
			
			enabledLayers[i] = true;
			if (i > 3)
				enabledLayers[i] = false;
			modifiedLayers[i] = false;
			//layerImages[i] = baseImage;
		}
		numEnabled = 6;
		setImageIndex(10);
		
		//updateSize(canvasWidth, canvasHeight);
		addComponentListener(new ResizeListener());
		
		//changeBrightness(0, 100);
		//changeBrightness(1, 100);
		//changeContrast(2, 1.5f);
		//smoothImage(3, 1);
		//smoothImage(4, 3);
		//equalizeImage(1);
		
		gaussianBlur(1, 7, 1.5);
		cannyEdgeDetection(2, 7, 1.5, 150, 90);
		
		cannyEdgeDetection2(3, 7, 1.5, 150, 90);
		//houghTransform(2, 1000);
		//cannyEdgeDetection2(3, 7, 1.5, 150, 90);
		//detectEdges(3, 1, 100);
		//detectEdges(4, 1, 100);
		//cleanEdges(4);
		//detectEdges(5, 1, 100);
		//cleanEdges(5);
		//thinEdges(5);
	}
	
	public void setImageIndex(int index)
	{
		imageIndex = index;
		baseImage = baseImages[imageIndex];
		
		realImageWidth = baseImage.getWidth();
		realImageHeight = baseImage.getHeight();
		imageAspect = ((float) realImageHeight) / ((float) realImageWidth);
		
		for (int layer = 0; layer < enabledLayers.length; layer++)
		{
			BufferedImage temp = baseImage;
			BufferedImage newImage = new BufferedImage(temp.getWidth(), temp.getHeight(), temp.getType());
			
			Graphics2D g = newImage.createGraphics();
			g.drawImage(temp, 0, 0, temp.getWidth(), temp.getHeight(), null);
			g.dispose();
			
			rawRasters[layer] = newImage.getRaster();
			rawLayerImages[layer] = ((DataBufferByte) newImage.getRaster().getDataBuffer()).getData();
			layerImages[layer] = newImage;
			
			//System.out.println("layerImages[" + layer + "] = " + layerImages[layer]);
			
			int[] vals = baseImage.getRaster().getPixel(1, 1, (int[]) null);
		}
	}
	
	public void changeBrightness(int layer, int bOffset)
	{
		//if (layer==0)
			PreProcessing.changeBrightness(rawRasters[layer], bOffset);
		//else
			//PreProcessing.changeBrightness2(rawRasters[layer], bOffset);
		
	}
	
	public void changeContrast(int layer, float scale)
	{
		PreProcessing.changeContrast(rawRasters[layer], scale);
	}
	
	public void smoothImage(int layer, int scheme)
	{
		if (scheme == 1)
			PreProcessing.localAverage(rawRasters[layer]);
		else if (scheme == 2)
			PreProcessing.gaussian(rawRasters[layer]);
		else if (scheme == 3)
			PreProcessing.medianSmooth(rawRasters[layer]);
	}
	
	public void gaussianBlur(int layer, int radius, double intensity)
	{
		PreProcessing.gaussianBlur(rawRasters[layer], radius, intensity);
	}
	
	public void equalizeImage(int layer)
	{
		PreProcessing.histogramEqualize(rawRasters[layer]);
	}
	
	public void detectEdges(int layer, int scheme, int threshold)
	{
		EdgeDetection.detectEdges(rawRasters[layer], scheme, threshold);
	}
	
	public void cannyEdgeDetection(int layer, int radius, double intensity, int strongT, int weakT)
	{
		PreProcessing.gaussianBlur(rawRasters[layer], radius, intensity);
		EdgeDetection.cannyEdgeDetect(rawRasters[layer], strongT, weakT);
	}
	
	public void cannyEdgeDetection2(int layer, int radius, double intensity, int strongT, int weakT)
	{
		BufferedImage output = JCanny.CannyEdges(layerImages[layer], 1, 0.2);
		layerImages[layer] = output;
	}
	
	public void houghTransform(int layer, int threshold)
	{
		ArrayList<LineSegment> lines = HoughTransform.transform(rawRasters[layer], threshold);
		lineSegments[layer] = lines;
	}
	
	public void cleanEdges(int layer)
	{
		EdgeDetection.cleanEdges(rawRasters[layer]);
	}
	
	public void thinEdges(int layer)
	{
		EdgeDetection.thinEdges(rawRasters[layer]);
	}
	
	public void updateSize()
	{
		canvasWidth = getWidth();
		canvasHeight = getHeight();
		
		cellWidth = canvasWidth/2;
		if (numEnabled == 1)
			cellWidth = canvasWidth;
		
		gapWidth = (int) (canvasWidth*0.05f);
		cellWidth = (int) (cellWidth*0.95);
		//gapWidth -= cellWidth;
		
		int numRows = (numEnabled+1)/2;
		cellHeight = canvasHeight / numRows;
		gapHeight = (int) (canvasHeight*0.05f);
		cellHeight = (int) (cellHeight*0.95);
		//gapHeight -= cellHeight;
		
		imageWidth = cellWidth;
		imageHeight = cellHeight;
		
		float widthRatio = ((float) cellWidth) / realImageWidth;
		float heightRatio = ((float) cellHeight) / realImageHeight;
		float ratio = Math.min(widthRatio, heightRatio);
		
		imageWidth = (int) (realImageWidth*ratio);
		imageHeight = (int) (realImageHeight*ratio);
		
		if (widthRatio < heightRatio)
		{
			imageOffsetX = 0;
			imageOffsetY = (cellHeight - imageHeight) / 2;
		}
		else
		{
			imageOffsetX = (cellWidth - imageWidth) / 2;
			imageOffsetY = 0;
		}
	}
	
	private void updateLayers()
	{
		/*
		for (int layer = 0; layer < modifiedLayers.length; layer++)
		{
			if (modifiedLayers[layer])
			{
				ByteArrayInputStream bais = new ByteArrayInputStream(rawLayerImages[layer]);
				try
				{
					layerImages[layer] = ImageIO.read(bais);
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
				System.out.println("Updated layer " + layer);
				modifiedLayers[layer] = false;
			}
		}
		*/
		
		wasModified = false;
	}
	
	public void drawImage(Graphics g)
	{
		g.setColor(Color.RED);
		/*
		System.out.println("-----------------");
		System.out.println("Window size: (" + canvasWidth + ", " + canvasHeight + ")");
		System.out.println("Gap size: (" + gapWidth + ", " + gapHeight + ")");
		System.out.println("CellSize = (" + cellWidth + ", " + cellHeight + ")");
		*/
		
		if (wasModified)
			updateLayers();
		
		int index = 0;
		for (int i = 0; i < enabledLayers.length; i++)
		{
			if (enabledLayers[i])
			{
				
				/*
				if one cell, then
					cellX = gapWidth/2
				elseif 2+ cells, then...
					if odd number cells and last cell
						cellX = gapWidth/2 + cellWidth/2
					else
						column = 1 or 2
						cellX = column*gapWidth/3 + (column-1)*cellWidth
				*/
				
				int column = (index%2)+1;
				int cellX = gapWidth/2; //default (if numEnabled == 1)
				if (numEnabled > 1)
				{
					if (numEnabled%2 == 1 && index+1 == numEnabled) //if odd # of cells and last one, center it
					{
						cellX = gapWidth/2 + cellWidth/2;
					}
					else //even number of cells or not last cell, so find startX for cell
					{
						//column is either 1 or 2
						
						
						cellX = (int) ((float) column*gapWidth/3);
						if (column == 2)
							cellX += cellWidth;
					}
				}
				
				int rows = (index+2)/2;
				int cellY = gapHeight/(rows + 1);
				cellY = cellY*rows + cellHeight*(rows-1);
				
				/*
				System.out.println("Drawing at row " + rows + " column " + column);
				System.out.println("CellX = " + cellX);
				System.out.println("CellY = " + cellY);
				*/
				
				g.fillRect(cellX, cellY, cellWidth, cellHeight);
				//System.out.println(layerImages[i]);
				//System.out.println("Index (" + i + " ) = " + layerImages[i]);
				
				for (LineSegment line : lineSegments[i])
				{
					g.setColor(Color.BLUE);
					g.drawLine(cellX + (int) line.getX1(), cellY + (int) line.getY1(), cellX + (int) line.getX2(), cellY + (int) line.getY2());
					g.setColor(Color.RED);
				}
				if (lineSegments[i].size() == 00)
				{
					g.drawImage(layerImages[i], cellX + imageOffsetX, cellY + imageOffsetY, imageWidth, imageHeight, null);
				}
				else
					System.out.println("Num segments for layer " + i + " = " + lineSegments[i].size());
				
				index++;
			}
			
		}
		
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.setColor(Color.RED);
		
		drawImage(g);
	}
}
