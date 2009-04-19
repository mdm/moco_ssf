package moco;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.color.ColorSpace;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

public class GradientBar extends JComponent {
	enum Orientation {HORIZONTAL, VERTICAL};
	
	Orientation orientation;
	float level;
	float cutoff;

	public GradientBar(Orientation orientation) {
		super();
		this.setBorder(BorderFactory.createLoweredBevelBorder());
		this.orientation = orientation;
		this.level = 0.0f;
		this.cutoff = 1.0f;
	}

	@Override
	public void paint(Graphics arg0) {
		float[] start = new float[3];
		Color.green.getRGBColorComponents(start);
		float[] end = new float[3];
		Color.red.getRGBColorComponents(end);
		float[] mix = new float[3];
		float percent;

		if (level > cutoff) {
			percent = (level - cutoff) / (1 - cutoff);
		}
		else{
			percent = 0;
		}
		for (int j = 0; j < 3; j++) {
			mix[j] = percent * end[j] + (1 - percent) * start[j];
		}
		arg0.setColor(new Color(mix[0], mix[1], mix[2]));
		
		switch (orientation) {
		case HORIZONTAL:
			/*for (int i = 0; i < (int) (level * this.getWidth()); i++) {
				percent = 1.0f * i / this.getWidth();
				for (int j = 0; j < 3; j++) {
					mix[j] = percent * end[j] + (1 - percent) * start[j];
				}
				arg0.setColor(new Color(mix[0], mix[1], mix[2]));
				arg0.drawLine(i, 0, i, this.getHeight() - 1);
			}//*/
			arg0.fillRect(0, 0, (int) (level * this.getWidth()), this.getHeight());
			arg0.setColor(Color.black);
			arg0.drawLine((int) (cutoff * this.getWidth()), 0, (int) (cutoff * this.getWidth()), this.getHeight() - 1);
			break;
		case VERTICAL:
			/*for (int i = 0; i < this.getHeight(); i++) {
				percent = i / this.getHeight();
				for (int j = 0; j < 3; j++) {
					mix[j] = percent * end[j] + (1 - percent) * start[j];
				}
				arg0.setColor(new Color(mix[0], mix[1], mix[2]));
				arg0.drawLine(0, i, this.getWidth() - 1, i);
			}//*/
			arg0.fillRect(0, this.getHeight() - (int) (level * this.getHeight()), this.getWidth(), (int) (level * this.getHeight()));
			arg0.setColor(Color.black);
			arg0.drawLine(0, this.getHeight() - (int) (cutoff * this.getHeight()), this.getWidth() - 1, this.getHeight() - (int) (cutoff * this.getHeight()));
			break;
		}
		super.paint(arg0);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		switch (orientation) {
		case HORIZONTAL:
			size = new Dimension(this.getParent().getWidth(), 20);
			break;
		case VERTICAL:
			size = new Dimension(20, this.getParent().getHeight());
			break;
		}
		return size;
	}

	@Override
	public Dimension getMaximumSize() {
		return this.getPreferredSize();
	}

	@Override
	public Dimension getMinimumSize() {
		return this.getPreferredSize();
	}
	
	public void setLevel(float level) {
		this.level = level;
		repaint();
	}

	public void setCutoff(float cutoff) {
		this.cutoff = cutoff;
	}

}
