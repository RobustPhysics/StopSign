import java.io.File;

public class Settings
{
	File startImage = null;
	
	//PRE-PROCESSING
	float brightness = 0.0f;
	float contrast = 0.0f;
	
	int smoothingScheme = 0;
	int smoothingWindowX = 3;
	int smoothingWindowY = 3;
	
	int sharpenScheme = 0;
	int sharpenWindowX = 3;
	int sharpenWindowY = 3;
	
	int equalizeScheme = 0;
	int equalizeWindowX = 3;
	int equalizeWindowY = 3;
	
	
	//EDGE DETECTION
	int edgeDetectionScheme = 0;
	float edgeThreshold = 2;
	int edgeWindowX = 3;
	int edgeWindowY = 3;
	boolean edgeNoiseRemoval = false;
	
	boolean useCannyEdge = false;
	float cannyStrongThreshold = 3.0f;
	float cannyWeakThreshold = 2.0f;
	int cannyEdgeDist = 1;
	
	boolean applyThinning = false;
	
	
	
	public Settings()
	{
		
	}
}
