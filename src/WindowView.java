import java.io.File;
import javax.swing.*;

public class WindowView
{
	private WindowController controller;
	private MainPanel mainPanel;
	private File[] images;
	JFrame frame;
	
	public WindowView(WindowController c, File[] imagePaths)
	{
		images = imagePaths;
		controller = c;
		createGUI();
	}
	
	private void createGUI()
	{
		frame = new JFrame("Stop Sign Identification");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1800, 1400);
		mainPanel = new MainPanel(controller, images);
		frame.add(mainPanel);
		
		
		
		JMenuBar menuBar = new JMenuBar();
		
		//FILE
		JMenu file = new JMenu("File");
		JMenuItem open = new JMenuItem("Open");
		JMenuItem exit = new JMenuItem("Exit");
		file.add(open);
		file.add(exit);
		menuBar.add(file);
		
		//LAYERS
		JMenu layers = new JMenu("Layers");
		
		JMenu view = new JMenu("View");
		ButtonGroup viewGroup = new ButtonGroup();
		JRadioButtonMenuItem gridView = new JRadioButtonMenuItem("Grid");
		gridView.setSelected(true);
		viewGroup.add(gridView);
		view.add(gridView);
		JRadioButtonMenuItem overlayView = new JRadioButtonMenuItem("Overlay");
		viewGroup.add(overlayView);
		view.add(overlayView);
		
		JMenu images = new JMenu("Images");
		JCheckBoxMenuItem origLayer = new JCheckBoxMenuItem("Original");
		images.add(origLayer);
		JCheckBoxMenuItem processedLayer = new JCheckBoxMenuItem("Processed");
		images.add(processedLayer);
		JCheckBoxMenuItem edgesLayer = new JCheckBoxMenuItem("Edges");
		images.add(edgesLayer);
		JCheckBoxMenuItem linesLayer = new JCheckBoxMenuItem("Lines");
		images.add(linesLayer);
		JCheckBoxMenuItem assembledLayer = new JCheckBoxMenuItem("Assembled");
		images.add(assembledLayer);
		JCheckBoxMenuItem regionsLayer = new JCheckBoxMenuItem("Regions");
		images.add(regionsLayer);
		
		layers.add(view);
		layers.add(images);
		menuBar.add(layers);
		
		//PROCESSING
		JMenu processing = new JMenu("Processing");
		JMenuItem brightness = new JMenuItem("Brightness");
		processing.add(brightness);
		JMenuItem contrast = new JMenuItem("Contrast");
		processing.add(contrast);
		
		JMenu smoothing = new JMenu("Smoothing");
		JMenuItem localAvg = new JMenuItem("Local Averaging");
		smoothing.add(localAvg);
		JMenuItem gauss = new JMenuItem("Gaussian");
		smoothing.add(gauss);
		JMenuItem median = new JMenuItem("Median");
		smoothing.add(median);
		processing.add(smoothing);
		
		JMenu sharpening = new JMenu("Sharpening");
		JMenuItem laplacianSharp = new JMenuItem("Laplacian");
		laplacianSharp.add(sharpening);
		processing.add(sharpening);
		
		JMenu equalize = new JMenu("Equalize");
		JMenuItem histEqualize = new JMenuItem("Histogram Equalization");
		equalize.add(histEqualize);
		processing.add(equalize);
		
		menuBar.add(processing);
		
		//EDGE DETECT
		JMenu edgeDetect = new JMenu ("Edge Detect");
		JMenuItem normalEdge = new JMenuItem("Normal");
		edgeDetect.add(normalEdge);
		JMenuItem sobelEdge = new JMenuItem("Sobel");
		edgeDetect.add(sobelEdge);
		JMenuItem kirshEdge = new JMenuItem("Kirsh");
		edgeDetect.add(kirshEdge);
		JMenuItem heukelEdge = new JMenuItem("Heukel");
		edgeDetect.add(heukelEdge);
		JCheckBoxMenuItem noiseRemoval = new JCheckBoxMenuItem("Enable Noise Removal");
		edgeDetect.add(noiseRemoval);
		
		menuBar.add(edgeDetect);
		
		
		//LINES
		JMenu lines = new JMenu("Line Extract");
		JMenuItem houghTrans = new JMenuItem("Hough Transform");
		lines.add(houghTrans);
		
		menuBar.add(lines);
		
		//ASSEMBLY
		JMenu assembly = new JMenu("Assembly");
		
		menuBar.add(assembly);
		
		//REGIONS
		JMenu regions = new JMenu("Regions");
		
		menuBar.add(regions);
		
		
		frame.setJMenuBar(menuBar);
		
		frame.setVisible(true);
	}
}
