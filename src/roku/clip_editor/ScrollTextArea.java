package roku.clip_editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.Timer;

@SuppressWarnings("serial")
//ver. 2.2.0.0
public class ScrollTextArea extends JComponent implements MouseWheelListener {

	String outputText;
	Font f;
	
	int boundx;
	int boundy;
	int boundw;
	int boundh;
	
	int x, y;
	int endofstring;
	
	Queue<String> queueString;
	
	Timer t;

	//constructor w/ parameters
	public ScrollTextArea(int x, int y, int width, int height) {
		super();

		this.enableInputMethods(true);
		this.addMouseWheelListener(this);

		//dimensions
		this.setSize(width, height);
		this.setLocation(x, y);

		outputText = "";

		//default font
		f = new Font("Arial", Font.PLAIN, 12);

		boundx = 10;
		boundy = 10;
		boundw = this.getWidth() - 20;
		boundh = this.getHeight() - 20;
		this.x= boundx;
		this.y=boundy;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	//constructor w/o parameters
	public ScrollTextArea() {
		super();
		this.enableInputMethods(true);
		this.addMouseWheelListener(this);
		outputText = "";
		this.setVisible(true);
		
		//default font
		f = new Font("Arial", Font.PLAIN, 20);
		
		boundx = 10;
		boundy = 10;
		boundw = this.getWidth() - 20;
		boundh = this.getHeight() - 20;
		this.x= boundx;
		this.y=boundy;
		
		t = new Timer(10, new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	scrollText();
	        	repaint();
	        }
	    });
		
		t.start();
		queueString = new LinkedList();
	}
	
	//Graphics paint component method
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// background
		g.setColor(Color.WHITE);
		g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
		// turn on anti-alias mode
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.black);
		g.setFont(f);
		
		endofstring = drawString(outputText, g, boundx, y, f);
	}
	
	String outputText1 = "";
	
	public void setText(String s) {
		outputText1 = s;
		outputText = "";
		
		queueString = new LinkedList<>(Arrays.asList(s.split("")));
	}
	
	//updates graphics as text scrolls across (called using a timer)
	public void scrollText() {
		if(queueString.isEmpty()) {
			
		} else {
			outputText += (queueString.poll());
			this.y = getHeight() - endofstring + y - boundy;
		}
	}
	
	//breaks input string into individual letters/words 
	public String breakString(String str, Graphics g) {
		
		String[] words = str.split(" ");
		String finalWordsSoFar = "";
		String wordsSoFar = "";
		
		for(String s : words) {
			String temp = "";
			
			if(wordsSoFar.length() == 0) {
				temp = s;
			} else {
				temp = wordsSoFar + " " + s;
			}
			
			int width = g.getFontMetrics().stringWidth(temp);
			
			if(width>= (getWidth() - 20)) {
				finalWordsSoFar += (wordsSoFar + "\n");
				wordsSoFar = s;
			} else {
				wordsSoFar = temp;
			}
		}
		
		finalWordsSoFar += wordsSoFar;
		return finalWordsSoFar;
	}
	
	//draws the broken up letters/strings using the graphics (is set to line wrap)
	public int drawString(String s, Graphics g, int x , int y, Font f) {
        for (String line : s.split("\n")) {
        	for (String subline : breakString(line, g).split("\n")) {
        		g.drawString(subline, x, y += 5 + g.getFontMetrics(f).getAscent());
        	}
        }
        return y;
    }

	public String getText() {
		return this.outputText1;
	}

	public void setFont(Font f) {
		this.f = f;
	}

	//updates text area graphics when mouse wheel moves
	public void mouseWheelMoved(MouseWheelEvent e) {
		int omega = -30*e.getWheelRotation();
		
		//sets max and min as the bounds on when to stop scrolling
		if(omega >= 0) {
			y = (int) Math.min(y + omega, boundx - 10);
		}
		else {
			y = (int) Math.max(y + omega, getHeight() - endofstring + y - boundy);
		}
		repaint();
	}

	public Dimension getPreferredSize() {
		return new Dimension(getWidth(), getHeight());
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	public Dimension getMaximumSize() {
		return getPreferredSize();
	}
}
