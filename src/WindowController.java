import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class WindowController
{
	private MainPanel mainPanel;
	private WindowView view;
	private Settings settings;
	
	String[] imagePaths = {
			"Images/image-1.bmp",
			"Images/image-2.bmp",
			"Images/image-3.bmp",
			"Images/image-4.bmp",
			"Images/image-5.bmp",
			"Images/image-6.bmp",
			"Images/image-7.bmp",
			"Images/image-8.bmp",
			"Images/image-9.bmp",
			"Images/image-10.bmp",
			"Images/image-11.bmp"
	};
	File[] images;
	
	public WindowController()
	{
		images = new File[imagePaths.length];
		for (int i = 0; i < imagePaths.length; i++)
		{
			images[i] = new File(imagePaths[i]);
		}
		
		view = new WindowView(this, images);
		settings = new Settings();
		
	}
}
