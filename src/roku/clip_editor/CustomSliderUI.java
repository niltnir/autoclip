package roku.clip_editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicSliderUI;

//ver. 2.2.0.0
public class CustomSliderUI extends BasicSliderUI{
	
	protected Icon thumbIcon = null;
	
	public CustomSliderUI(JSlider slider, Icon thumb) {
		super(slider);
		setThumbIcon(thumb);
	}
	
	public void setThumbIcon(Icon setThumbIcon){
		if(setThumbIcon == null){
			this.thumbIcon = UIManager.getIcon("Slider.horizontalThumbIcon");
		} else {
			this.thumbIcon = setThumbIcon;
		}
	}
	
	@Override
    protected Dimension getThumbSize() {
        return new Dimension(20, 20);
    }

    @Override
    public void paintTrack(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(new Color(43, 237, 142));
        if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
            g2d.drawLine(trackRect.x, trackRect.y + trackRect.height / 2, 
                    trackRect.x + trackRect.width, trackRect.y + trackRect.height / 2);
        } else {
            g2d.drawLine(trackRect.x + trackRect.width / 2, trackRect.y, 
                    trackRect.x + trackRect.width / 2, trackRect.y + trackRect.height);
        }
    }

    @Override
    public void paintThumb(Graphics g) {
    	
    	Rectangle knobBounds = thumbRect;
        g.translate( knobBounds.x, knobBounds.y );
        thumbIcon.paintIcon(slider, g, 0, 0);
        g.translate( -knobBounds.x, -knobBounds.y );
        
    }

}
