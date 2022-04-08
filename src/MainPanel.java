import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

import javax.swing.JPanel;

public class MainPanel extends JPanel implements ActionListener
{
	private ImagePanel imagePanel;
	
	public MainPanel(WindowController controller, File[] images)
	{
		imagePanel = new ImagePanel(images);
		//imagePanel.updateSize(getWidth(), getHeight());
		
		setLayout(new BorderLayout(2,2));
		add(imagePanel, BorderLayout.CENTER);
		
		imagePanel.updateSize();
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.setColor(Color.RED);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// TODO Auto-generated method stub
		
	}
}
