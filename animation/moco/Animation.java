package moco;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.*;

public class Animation {

	/**
	 * @param args
	 */
	private static void createGUI(String scenario_name) {
		JFrame mainWnd = new JFrame("Trace Animation - " + scenario_name);
		mainWnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWnd.setLayout(new BoxLayout(mainWnd.getContentPane(), BoxLayout.Y_AXIS));
		
		/*JMenuBar menuBar = new JMenuBar();
		mainWnd.setJMenuBar(menuBar);
		JMenu menu = new JMenu("File");
		menuBar.add(menu);
		JMenuItem menuItem = new JMenuItem("Open Trace...");
		menu.add(menuItem);//*/

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		mainWnd.add(topPanel);
		
		GradientBar totalPower = new GradientBar(GradientBar.Orientation.VERTICAL);
		totalPower.setCutoff(0.4f); // no gain with powerctrl
		topPanel.add(totalPower);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Controls"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
		mainWnd.add(bottomPanel);

		// top panel contents
		ScenarioModel model = new ScenarioModel();
		Scenario scenario = new Scenario(model);
		scenario.setPowerIndicator(totalPower);
		model.loadNodes(scenario_name + ".pos");
		model.loadEvents(scenario_name + ".ctc");
				
		JScrollPane canvasPane = new JScrollPane(scenario);
		topPanel.add(canvasPane);
						
		// bottom panel contents
		JSlider timeSlider = new JSlider(0, (int) (model.duration + 0.5), 0);
		timeSlider.setMajorTickSpacing(60);
		timeSlider.setMinorTickSpacing(5);
		timeSlider.setPaintTicks(true);
		scenario.setTimeSlider(timeSlider);		
		bottomPanel.add(timeSlider);
		
		JPanel button_info_panel = new JPanel();
		button_info_panel.setLayout(new GridLayout(1, 2));
		bottomPanel.add(button_info_panel);
		
		JPanel button_panel = new JPanel();
		button_panel.setLayout(new GridLayout(3, 1));
		button_info_panel.add(button_panel);

		JButton playback_button = new JButton("Start/Stop");
		playback_button.addActionListener(scenario.getPlaybackHandler());
		button_panel.add(playback_button);
		
		JButton faster = new JButton("Faster");
		faster.addActionListener(scenario.getPlaybackHandler());
		button_panel.add(faster);
	
		JButton slower = new JButton("Slower");
		slower.addActionListener(scenario.getPlaybackHandler());
		button_panel.add(slower);
	
		JTextArea text_area = new JTextArea("node id: none selected\nneighbors: -\npowerlevel: -", 5, 20);
		text_area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Node Information"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
		text_area.setEditable(false);
		text_area.setBackground(UIManager.getColor("Label.background"));
		scenario.setInfoArea(text_area);
		button_info_panel.add(text_area);
		
		mainWnd.pack();
		mainWnd.setVisible(true);
	}
	
	public static void main(String[] args) {
		createGUI(args[0]);
		/*SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createGUI();
            }
        });//*/
	}
}
