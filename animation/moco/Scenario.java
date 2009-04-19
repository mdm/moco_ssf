package moco;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

public class Scenario extends javax.swing.JComponent {
	
	ScenarioModel model;
	double zoom = 1; // 100%
	double speed = 1;
	int fps = 10;
	int node_size = 20;
	int node_size_2 = (node_size * node_size) / 4;
	int selected_node = -1;
	boolean playing = false;
	
	// additional colors
	Color red_stroke = new Color(100, 0, 0);
	Color red_fill = Color.red;
	Color green_stroke = new Color(0, 100, 0);
	Color green_fill = Color.green;
	Color blue_stroke = new Color(0, 0, 100);
	Color blue_fill = Color.blue;
	
	Color faded_black = new Color(0, 0, 0, 64);
	
	Timer timer;
	JSlider time_slider;
	GradientBar power_indicator;
	JTextArea info_area;
	
	private class MouseHandler extends MouseInputAdapter {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
			super.mouseClicked(arg0);
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			int x_2, y_2;
			
			selected_node = -1;
			
			for (int i = 0; i < model.nodes.length; i++) {
				x_2 = (int) (zoom * model.nodes[i].posX + node_size - arg0.getX());
				x_2 *= x_2;
				y_2 = (int) (zoom * model.nodes[i].posY + node_size - arg0.getY());
				y_2 *= y_2;
				if ((x_2 + y_2) < node_size_2) {
					selected_node = i;
					break;
				}
			}
			
			if (selected_node == -1) {
				info_area.setText("node id: none selected\nneighbors: -\npowerlevel: -\n");
			}
			else {
				info_area.setText("node id: " + String.valueOf(selected_node) + "\nneighbors: " + String.valueOf(model.nodes[selected_node].neighbors) + "\npowerlevel: " + String.valueOf(model.nodes[selected_node].peekPower()) + "mW");
			}
			
			if (!playing) { 
				repaint();
			}
			//System.out.println(selected_node);
			super.mouseMoved(arg0);
		}
		
	}
	
	private class TimeoutHandler implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			setTime(model.time + (speed / fps));
			time_slider.setValueIsAdjusting(true);
			time_slider.setValue((int) model.time);
			if (playing) {
				timer.restart();
			}
		}
		
	}

	private class PlaybackHandler implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			JButton source = (JButton) arg0.getSource();
			if (source.getText().compareTo("Start/Stop") == 0){
				if (!playing) {
					time_slider.setEnabled(false);
					timer = new Timer(1000 / fps, new TimeoutHandler());
					timer.start();
				}
				else {
					timer.stop();
					time_slider.setEnabled(true);
				}
				playing = !playing;
			}
			else if (source.getText().compareTo("Faster") == 0){
				speed *= 2;
			}
			else if (source.getText().compareTo("Slower") == 0){
				speed /= 2;
			}
		}
		
	}
	
	private class JumpHandler implements ChangeListener {

		public void stateChanged(ChangeEvent arg0) {
			JSlider source = (JSlider) arg0.getSource();
		    if (!source.getValueIsAdjusting()) {
		    	System.out.println("Jumping to simulation time " + String.valueOf(source.getValue()) + " sec.");
		    	setTime(source.getValue());
		    }
		}
		
	}

	public Scenario(ScenarioModel model) {
		super();
		this.model = model;
		
		addMouseMotionListener(new MouseHandler());
	}

	public void paint(Graphics arg0) {
		//super.paint(arg0);
		Graphics2D g2 = (Graphics2D) arg0;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (playing) {
			g2.drawString("Animation speed: " + String.valueOf(speed) + "x", node_size, node_size - 5);
		}
		
		// draw area border
		g2.setColor(Color.white);
		g2.fillRect(node_size, node_size, (int) (zoom * model.area_width), (int) (zoom * model.area_height));
		g2.setColor(Color.darkGray);
		g2.drawRect(node_size, node_size, (int) (zoom * model.area_width), (int) (zoom * model.area_height));
			
		g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		
		// draw links
		if (selected_node != -1) {
			g2.setColor(faded_black);
		}
		else {
			g2.setColor(Color.black);
		}
		for (int source_id: model.links.keySet()) {
			for (int destination_id: model.links.get(source_id).keySet()) {
				if (model.links.get(source_id).get(destination_id) >= 2) {
					g2.drawLine((int) (zoom * model.nodes[source_id].posX + node_size), (int) (zoom * model.nodes[source_id].posY + node_size), (int) (zoom * model.nodes[destination_id].posX + node_size), (int) (zoom * model.nodes[destination_id].posY + node_size));
				}
			}
		}

		// draw nodes
		Color node_fill;
		Color node_stroke;
		for (Node node: model.nodes) {
			if (node.neighbors < model.min_nb) {
				node_fill = blue_fill;
				node_stroke = blue_stroke;
			} else if (node.neighbors > model.max_nb) {
				node_fill = red_fill;
				node_stroke = red_stroke;
			} else {
				node_fill = green_fill;
				node_stroke = green_stroke;			
			}
			 
			if (selected_node != -1) {
				node_fill = new Color(node_fill.getRed(), node_fill.getGreen(), node_fill.getBlue(), 64);
				node_stroke = new Color(node_stroke.getRed(), node_stroke.getGreen(), node_stroke.getBlue(), 64);
			}
		
			g2.setColor(node_fill);
			g2.fillOval((int) (zoom * node.posX + node_size / 2.0), (int) (zoom * node.posY + node_size / 2.0), node_size, node_size);
			g2.setColor(node_stroke);
			g2.drawOval((int) (zoom * node.posX + node_size / 2.0), (int) (zoom * node.posY + node_size / 2.0), node_size, node_size);
		}

		if (selected_node != -1) {
			// draw links to selected node
			g2.setColor(Color.black);
			for (int source_id: model.links.keySet()) {
				for (int destination_id: model.links.get(source_id).keySet()) {
					if (model.links.get(source_id).get(destination_id) >= 2) {
						if ((source_id == selected_node) || (destination_id == selected_node)) {
							g2.drawLine((int) (zoom * model.nodes[source_id].posX + node_size), (int) (zoom * model.nodes[source_id].posY + node_size), (int) (zoom * model.nodes[destination_id].posX + node_size), (int) (zoom * model.nodes[destination_id].posY + node_size));
						}
					}
				}
			}
			
			// draw selected node
			if (model.nodes[selected_node].neighbors < model.min_nb) {
				node_fill = blue_fill;
				node_stroke = blue_stroke;
			} else if (model.nodes[selected_node].neighbors > model.max_nb) {
				node_fill = red_fill;
				node_stroke = red_stroke;
			} else {
				node_fill = green_fill;
				node_stroke = green_stroke;			
			}
			g2.setColor(node_fill);
			g2.fillOval((int) (zoom * model.nodes[selected_node].posX + node_size / 2.0), (int) (zoom * model.nodes[selected_node].posY + node_size / 2.0), node_size, node_size);
			g2.setColor(node_stroke);
			g2.drawOval((int) (zoom * model.nodes[selected_node].posX + node_size / 2.0), (int) (zoom * model.nodes[selected_node].posY + node_size / 2.0), node_size, node_size);
		}
	
	}

	public Dimension getPreferredSize() {
		// TODO Auto-generated method stub
		Dimension preferredSize = new Dimension();
		
		zoom = Math.min((this.getParent().getWidth() - 2 * node_size) / model.area_width, (this.getParent().getHeight() - 2 * node_size) / model.area_height);
		if (zoom < 1) {
			zoom = 1;
		}
		
		preferredSize.setSize(zoom * model.area_width + 2 * node_size, zoom * model.area_height + 2 * node_size);
		return preferredSize;
	}

	public void setTime(double time) {
		if (time > model.duration) {
			playing = false;
			timer.stop();
			time_slider.setEnabled(true);
		}

		model.progressTime(time);
		
		// recalc total power
		float total = 0;
		for (Node node: model.nodes) {
			total += node.peekPower();
		}
		power_indicator.setLevel((1.0f * total) / (model.nodes.length * 100));
		//System.out.println((Math.round(1000 * total) / 1000.0f));
		
		if (selected_node != -1) {
			info_area.setText("node id: " + String.valueOf(selected_node) + "\nneighbors: " + String.valueOf(model.nodes[selected_node].neighbors) + "\npowerlevel: " + String.valueOf(model.nodes[selected_node].peekPower()) + "mW");
		}

		repaint();
	}

	public ActionListener getPlaybackHandler() {
		return new PlaybackHandler();
	}
	
	public void setTimeSlider(JSlider time_slider) {
		time_slider.addChangeListener(new JumpHandler());
		this.time_slider = time_slider;	
	}
	
	public void setPowerIndicator(GradientBar power_indicator) {
		this.power_indicator = power_indicator;
	}

	public void setInfoArea(JTextArea info_area) {
		this.info_area = info_area;
	}
	
}

