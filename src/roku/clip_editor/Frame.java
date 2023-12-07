package roku.clip_editor;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//ver. 2.2.0.0
@SuppressWarnings("serial")
public class Frame extends JFrame{
	
	//main menu components and fields
	private static JLabel[] buttons = new JLabel[7];
	private static JLabel[] titleBarButtons = new JLabel[2];
	private static JLabel[] verifLabel = new JLabel[6];
	private static JLabel[] functionLabels = new JLabel[5];
	private static JLabel[] units = new JLabel[4];
	private static JLabel[] brackets = new JLabel[2];
	private static JLabel[] icons = new JLabel[2];
	private static JLabel titleBarTitle = new JLabel();
	private static JLabel title = new JLabel();
	private static JLabel divider = new JLabel();
	private static JLabel about = new JLabel();
	private static RoundedJTextField[] numInputs = new RoundedJTextField[4];
	private static JSlider speedSlider = new JSlider();
	private static ScrollTextArea selectionNotif = new ScrollTextArea();
	private static boolean[] verified = new boolean[6];
	private static int silenceOptionNum = -1;
	
	//loading screen components and fields
	private static JLabel loadingLabel = new JLabel();
	private static JProgressBar progressBar = new JProgressBar();
	private static int progress = 0;
	
	//cut selection menu components and fields
	private JLabel titleOfMenu3 = new JLabel();
	private JLabel instruction = new JLabel();
	private JLabel pageNum = new JLabel();
	private JLabel runButton = new JLabel();
	private JLabel nextButton = new JLabel();
	private JLabel previousButton = new JLabel();
	private static ArrayList<JCheckBox> cutsList = new ArrayList<JCheckBox>();
	private static ArrayList<JPanel> checkBoxPanels = new ArrayList<JPanel>();
	private int currentPageNum = 1;
	
	//general components and fields
	private static CardLayout cardLayout = new CardLayout();
	private static JPanel contPanel = new JPanel(cardLayout);
	private static JPanel titleBar = new JPanel();
	private static JPanel[] menuPanels = new JPanel[4];
	private static Point compCoords = new Point();
	
	CardLayout checkCardLayout = new CardLayout();
	JPanel checkContPanel = new JPanel(checkCardLayout);
	//"Arial Unicode MS" is a godly Unicode font
	
	//constructor
	public Frame(){
		//frame stuffs
		//
		//set frame on center of screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int)screenSize.getWidth();
        int height = (int)screenSize.getHeight();
        //rounded edges on frame
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 1100, 730, 30, 30));
        //not resizable
        setResizable(false);
        //set location takes the information about the screen and the fixed size of the frame
        setLocation(((width-1100)/2), ((height-730)/2));
        setSize(1100, 730);
        //sets the logo of the program
        ImageIcon logo = new ImageIcon(getClass().getResource("/logo_circle.png"));
        setIconImage(logo.getImage());
        //frame itself doesn't have any layouts
        setLayout(null);
        
        //card layout makes for an effective menu system including load screens
        //initializes and sets properties of the menu panels
        for(int i = 0; i < menuPanels.length; i++){
        	menuPanels[i] = new JPanel();
        	menuPanels[i].setLayout(null);
        	menuPanels[i].setBackground(Color.BLACK);
        	contPanel.add(menuPanels[i], Integer.toString(i));
        }
        
        //title bar stuffs
        //
        //sets up all the title bar buttons
        for(int i = 0; i < titleBarButtons.length; i++){
        	titleBarButtons[i] = new JLabel();
        	titleBarButtons[i].setIcon(new javax.swing.ImageIcon(getClass().getResource("/titleBarButtons" + i 
        			+ ".png")));
        	titleBarButtons[i].setBounds(1100 - (i + 1)*titleBarButtons[i].getPreferredSize().width, 0, 
        			titleBarButtons[i].getPreferredSize().width, titleBarButtons[i].getPreferredSize().height);
        	titleBar.add(titleBarButtons[i]);
        }
        //adds a mouse listener to the custom exit button
        titleBarButtons[0].addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {
			}
			public void mouseEntered(MouseEvent e) {
				titleBarButtons[0].setIcon(new javax.swing.ImageIcon(getClass().getResource("/titleBarButtons0_hover.png")));
			}
			public void mouseExited(MouseEvent e) {
				titleBarButtons[0].setIcon(new javax.swing.ImageIcon(getClass().getResource("/titleBarButtons0.png")));
			}
			public void mousePressed(MouseEvent e) {
				//warn user when they exit using a JOptionPane
				String options[] = {"Yes", "No"};
				int option = JOptionPane.showOptionDialog(null, 
						"Exiting will remove all created files.\nAre you sure you still want to leave?",
		                "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, new ImageIcon(getClass().getResource("/logo_icon_2.png")), options, options[1]);
				if(option == 0){
					Executive.getAudioVideo().deleteDir(new File(Executive.getAudioVideo().getProjectStringPath() + "/Runtime Files"));
					System.exit(0);
				};
			}
			public void mouseReleased(MouseEvent arg0) {
			}
    	});
        
        //adds a mouse listener to the custom minimize button
        titleBarButtons[1].addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {
				//minimize
				setState(JFrame.ICONIFIED);
			}
			public void mouseEntered(MouseEvent e) {
				titleBarButtons[1].setIcon(new javax.swing.ImageIcon(getClass().getResource("/titleBarButtons1_hover.png")));
			}
			public void mouseExited(MouseEvent e) {
				titleBarButtons[1].setIcon(new javax.swing.ImageIcon(getClass().getResource("/titleBarButtons1.png")));
			}
			public void mousePressed(MouseEvent e) {
				setState(JFrame.ICONIFIED);
			}
			public void mouseReleased(MouseEvent arg0) {
			}
    	});
        //adds a mouse listener to the title bar itself; this allows the user to move the screen around
    	compCoords = null;
    	titleBar.addMouseListener(new MouseListener(){
			public void mousePressed(MouseEvent e) {
				compCoords = e.getPoint();
			}
			public void mouseReleased(MouseEvent e) {
				compCoords = null;
			}
			public void mouseClicked(MouseEvent e) {
			}
			public void mouseEntered(MouseEvent e) {
			}
			public void mouseExited(MouseEvent e) {
			}
    	});
    	titleBar.addMouseMotionListener(new MouseMotionListener(){
			public void mouseDragged(MouseEvent e) {
				Point currCoords = e.getLocationOnScreen();
				setLocation(currCoords.x - compCoords.x, currCoords.y - compCoords.y);
			}
			public void mouseMoved(MouseEvent e) {
			}
    	});
    	//sets up title bar properties and title bar labels
    	titleBar.setLayout(null);
    	titleBar.setBackground(new Color(20, 118, 71));
    	titleBarTitle.setForeground(Color.WHITE);
    	titleBarTitle.setText("Autoclip Version 2.2.0.0");
    	titleBarTitle.setFont(new Font("Calibri Light", Font.PLAIN, (int)Math.round(this.getHeight()*0.03)));
        contPanel.setBounds(0, 30, 1100, 730);
        titleBar.setBounds(0, 0, 1100, 30);
        titleBarTitle.setBounds(43, 7, titleBarTitle.getPreferredSize().width, 18);
        titleBar.add(titleBarTitle);
        
        //frame adds the title bar and the menu panels in a card layout
        add(titleBar);
        add(contPanel);
        
        //main panel stuff (menuPanels[0])
        //
        //loops initialize all the label used in the UI
        //initializes and sets the images for the logo icons used in the UI
    	for(int i = 0; i < icons.length; i++){
        	icons[i] = new JLabel();
        	icons[i].setIcon(new javax.swing.ImageIcon(getClass().getResource("/logo_icon_" + i + ".png")));
        }
        //initializes all the necessary user input labels in the UI
        for(int i = 0; i < functionLabels.length; i++){
        	functionLabels[i] = new JLabel();
        }
        //initializes all the labels that represent the units after the text fields
        for(int i = 0; i < units.length; i++){
        	units[i] = new JLabel();
        }
        
        int fontsize_numinputs = (int)Math.round(this.getHeight()*0.04);
        int fontsize_scrolltext = (int)Math.round(this.getHeight()*0.028);
        int fontsize_silencetitle = (int)Math.round(this.getHeight()*0.07);
        int fontsize_functionlabels = (int)Math.round(this.getHeight()*0.05);
        
        //initializes and sets properties of the text fields that the user enters numbers into
        for(int i = 0; i < numInputs.length; i++){
        	numInputs[i] = new RoundedJTextField(3);
        	numInputs[i].setFont(new Font("Calibri Light", Font.PLAIN, fontsize_numinputs));
        	numInputs[i].setForeground(Color.BLACK);
        	numInputs[i].setHorizontalAlignment(JTextField.CENTER);
        }
        //adds a document listener to all those text fields in the UI
        //this detects real time change of those text fields
        for(int i = 0; i < numInputs.length - 1; i++){
        	numInputs[i].getDocument().addDocumentListener(new DocumentListener() {
        		public void changedUpdate(DocumentEvent e) {
        		}
        		//the verification icons and selection text get updated when the text fields are changed
        		public void removeUpdate(DocumentEvent e) {
        			checkTextFieldVerif();
        			checkAllVerif();
        			updateSelectionNotif();
        		}
        		public void insertUpdate(DocumentEvent e) {
        			checkTextFieldVerif();
        			checkAllVerif();
        			updateSelectionNotif();
        		}
        	});
        }
        //the text field responsible for speed is linked with the slider
        numInputs[3].addKeyListener(new KeyAdapter(){
            public void keyReleased(KeyEvent ke) {
            	updateSelectionNotif();
            	//the code below makes sure that numbers within the range are typed
                String typed = numInputs[3].getText();
                numInputs[3].setText(typed.replaceAll("[^\\d.]", ""));
                typed = typed.replaceAll("[^\\d.]", "");
                if(typed.endsWith(".") || typed.equals("")){
                	return;
                }
                //typing a number greater than 32.0 will set it to 32.0
                if((int)(textFieldToDouble(typed)*100) > 6400){
                	numInputs[3].setText("64.0");
                	typed = "64.0";
                }
                int value = (int)(textFieldToDouble(typed)*100);
                speedSlider.setValue(value);
            }
        });
        //initializes and sets the images for the custom buttons (JLabels)
        for(int i = 0; i < buttons.length - 1; i++){
        	buttons[i] = new JLabel();
        	buttons[i].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button" + i + ".png")));
        }
        buttons[6] = new JLabel();
        buttons[6].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button6_notready.png")));
        //initializes and sets the images for the verification icons to the left of necessary user inputs
        for(int i = 0; i < verifLabel.length; i++){
        	verifLabel[i] = new JLabel();
        	verifLabel[i].setIcon(new javax.swing.ImageIcon(getClass().getResource("/unverified.png")));
        }
        //initializes and sets the images for the brackets surrounding the silence options
        for(int i = 0; i < brackets.length; i++){
        	brackets[i] = new JLabel();
        	brackets[i].setIcon(new javax.swing.ImageIcon(getClass().getResource("/sqrbracket" + i + ".png")));
        }
        //sets the text for all the necessary user input labels 
        functionLabels[0].setText("Silence Options:");
    	functionLabels[1].setText("Volume Sensitivity:");
    	functionLabels[2].setText("Silence Margin:");
    	functionLabels[3].setText("Field Margin:");
    	functionLabels[4].setText("Speed Factor:");
        //the "Silence Options:" font is different from the rest due to its size
    	functionLabels[0].setFont(new Font("Calibri Light", Font.PLAIN, fontsize_silencetitle));
		functionLabels[0].setForeground(Color.WHITE);
    	//sets the font, color, and size for the rest of the text
    	for(int i = 1; i < functionLabels.length; i++){
    		functionLabels[i].setFont(new Font("Calibri Light", Font.PLAIN, fontsize_functionlabels));
    		functionLabels[i].setForeground(Color.WHITE);
    	}
    	//sets the text for all the labels describing the units
    	for(int i = 1; i < units.length - 1; i++){
    		units[i].setText("sec");
    	}
    	units[0].setText(""); //idk what to name the unit
    	units[3].setText("✕");
    	//sets the font, color, and size for each label describing the units
    	for(int i = 0; i < units.length - 1; i++){
    		units[i].setFont(new Font("Calibri Light", Font.PLAIN, fontsize_functionlabels));
    		units[i].setForeground(Color.WHITE);
    	}
    	units[3].setFont(new Font("Arial Unicode MS", Font.PLAIN, (int)(0.8*fontsize_functionlabels)));
		units[3].setForeground(Color.WHITE);
    	//sets the images for the title logo, the divider, and the about (my Twitter handle name)
    	title.setIcon(new javax.swing.ImageIcon(getClass().getResource("/textlogo.png")));
        divider.setIcon(new javax.swing.ImageIcon(getClass().getResource("/divider.png")));
        about.setIcon(new javax.swing.ImageIcon(getClass().getResource("/about.png")));
        //sets the properties and font for the text area indicating which selections are valid
        selectionNotif.setFont(new Font("Calibri Light", Font.PLAIN, (int)Math.round(fontsize_scrolltext)));
        
        //loading screen stuffs (menuPanels[1])
        //
        //sets the gif and initial position of the loading screen animation
        ImageIcon loadingAnim = new javax.swing.ImageIcon(getClass().getResource("/loading_anim.gif"));
    	loadingAnim.getImage().flush();
        loadingLabel.setIcon(loadingAnim);
    	loadingLabel.setBounds(1100/2 - loadingLabel.getPreferredSize().width/2, 730/2 - 50 - loadingLabel.getPreferredSize().height/2, 
    			loadingLabel.getPreferredSize().width, loadingLabel.getPreferredSize().height);
    	menuPanels[1].add(loadingLabel);
        
    	//startup screen stuffs (menuPanels[2])
    	//
    	//sets the gif and position of the splash screen animation
    	JLabel startUpAnim = new JLabel();
    	startUpAnim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/logo_anim.gif")));
    	startUpAnim.setBounds(1100/2 - startUpAnim.getPreferredSize().width/2, 730/2 - 50 - startUpAnim.getPreferredSize().height/2, 
    			startUpAnim.getPreferredSize().width, startUpAnim.getPreferredSize().height);
    	menuPanels[2].add(startUpAnim);
    	//background is set to white
    	menuPanels[2].setBackground(Color.WHITE);
    	
    	//sets up more properties for the already initialized components
    	initComponents();
	}
    
	//sets up more properties for the already initialized components
    private void initComponents(){
    	//adds mouseListeners with custom mouse events to the buttons that have important functions
    	for(int i = 0; i < buttons.length; i++){
    		buttons[i].addMouseListener(new ButtonMouseEvent(buttons, i));
    	}
    	
    	//sets absolute bounds for all the labels, buttons, icons, and text used in the UI
    	buttons[0].setBounds(47, 20, buttons[0].getPreferredSize().width, buttons[0].getPreferredSize().height);
    	buttons[1].setBounds(80, 200, buttons[1].getPreferredSize().width, buttons[1].getPreferredSize().height);
    	buttons[2].setBounds(80, 260, buttons[2].getPreferredSize().width, buttons[2].getPreferredSize().height);
    	buttons[3].setBounds(80, 320, buttons[3].getPreferredSize().width, buttons[3].getPreferredSize().height);
    	buttons[4].setBounds(500, 20, buttons[4].getPreferredSize().width, buttons[4].getPreferredSize().height);
    	buttons[5].setBounds(20, 440, buttons[5].getPreferredSize().width, buttons[5].getPreferredSize().height);
    	buttons[6].setBounds(884, 319, buttons[6].getPreferredSize().width, buttons[6].getPreferredSize().height);
    	
    	verifLabel[0].setBounds(20, 44, verifLabel[0].getPreferredSize().width, verifLabel[0].getPreferredSize().height);
    	verifLabel[1].setBounds(473, 44, verifLabel[1].getPreferredSize().width, verifLabel[1].getPreferredSize().height);
    	verifLabel[2].setBounds(20, 121, verifLabel[2].getPreferredSize().width, verifLabel[2].getPreferredSize().height);
    	verifLabel[3].setBounds(473, 121, verifLabel[3].getPreferredSize().width, verifLabel[3].getPreferredSize().height);
    	verifLabel[4].setBounds(473, 171, verifLabel[4].getPreferredSize().width, verifLabel[4].getPreferredSize().height);
    	verifLabel[5].setBounds(473, 221, verifLabel[5].getPreferredSize().width, verifLabel[5].getPreferredSize().height);
    	
    	brackets[0].setBounds(51, 176, brackets[0].getPreferredSize().width, brackets[0].getPreferredSize().height);
    	brackets[1].setBounds(350, 176, brackets[1].getPreferredSize().width, brackets[1].getPreferredSize().height);
    	
    	functionLabels[0].setBounds(60, 110, 370, 54);
    	functionLabels[1].setBounds(515, 115, 310, 40);
    	functionLabels[2].setBounds(515, 165, 250, 40);
    	functionLabels[3].setBounds(515, 215, 250, 40);
    	functionLabels[4].setBounds(515, 265, 250, 40);
    	
    	units[0].setBounds(1020, 113, units[0].getPreferredSize().width, units[0].getPreferredSize().height);
    	units[1].setBounds(1020, 159, units[1].getPreferredSize().width, units[1].getPreferredSize().height);
    	units[2].setBounds(1020, 210, units[2].getPreferredSize().width, units[2].getPreferredSize().height);
    	units[3].setBounds(1020, 263, units[3].getPreferredSize().width, units[3].getPreferredSize().height);
    	
    	numInputs[0].setBounds(950, 115, 60, 36);
    	numInputs[1].setBounds(950, 165, 60, 36);
    	numInputs[2].setBounds(950, 215, 60, 36);
    	numInputs[3].setBounds(920, 265, 90, 36);
    	
    	icons[0].setBounds(13, 6, icons[0].getPreferredSize().width, icons[0].getPreferredSize().height);
    	icons[1].setBounds(20, 578, icons[1].getPreferredSize().width, icons[1].getPreferredSize().height);
    	
    	title.setBounds(138, 575, 600, 120);
    	divider.setBounds(0, 555, divider.getPreferredSize().width, divider.getPreferredSize().height);
    	about.setBounds(680, 578, about.getPreferredSize().width, about.getPreferredSize().height);
    	selectionNotif.setBounds(550, 400, 513, 133);
    	progressBar.setBounds(300, 490, 500, 50);
    	speedSlider.setBounds(515, 310, 223, 40);
    	
    	//sets up all the initial properties of the progress bar
    	progressBar.setMinimum(0);
    	progressBar.setFont(new Font("Calibri Light", Font.PLAIN, 20));
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(43, 237, 142));
    	progressBar.setVisible(false);
    	
    	//sets up all the initial properties of the speed slider
    	speedSlider.setBackground(Color.BLACK);
    	Icon thumb = new ImageIcon(getClass().getResource("/sliderIcon.png"));
    	speedSlider.setUI(new CustomSliderUI(speedSlider, thumb));
    	speedSlider.setMaximum(6400);
    	speedSlider.setMinimum(100);
    	//adds a change listener so that it gets real time change of the JSlider
    	speedSlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				//the speed text field is changed when the slider changes
				numInputs[3].setText(Double.toString((double)speedSlider.getValue()/100));
			}
    	});
    	
    	//set up all the initial properties of the components in the cut selection menu
    	titleOfMenu3.setForeground(Color.WHITE);
		instruction.setForeground(Color.WHITE);
		pageNum.setForeground(Color.WHITE);
		titleOfMenu3.setText("Cut Selection Menu");
		instruction.setText("Please select the cuts you would like to keep and concatenate:");
		titleOfMenu3.setFont(new Font("Calibri Light", Font.PLAIN, 65));
		instruction.setFont(new Font("Calibri Light", Font.PLAIN, 40));
		pageNum.setFont(new Font("Calibri Light", Font.PLAIN, 45));
		titleOfMenu3.setBounds(25, 5, titleOfMenu3.getPreferredSize().width, titleOfMenu3.getPreferredSize().height);
		instruction.setBounds(27, 75, instruction.getPreferredSize().width, instruction.getPreferredSize().height);
		pageNum.setBounds(825, 335, pageNum.getPreferredSize().width, pageNum.getPreferredSize().height);
		runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/button6.png")));
		nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/nextButton.png")));
		previousButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/previousButton.png")));
		checkContPanel.setBackground(Color.BLACK);
		runButton.setBounds(825, 605, runButton.getPreferredSize().width, runButton.getPreferredSize().height);
		nextButton.setBounds(825, 145, 230, 50);
		previousButton.setBounds(825, 240, 230, 50);
		checkContPanel.setBounds(30, 130, 700, 550);
    	
    	//adds all the components to the main menu panel
    	for(int i = 0; i < buttons.length; i++){
    		menuPanels[0].add(buttons[i]);
    	}
    	
    	for(int i = 0; i < verifLabel.length; i++){
    		menuPanels[0].add(verifLabel[i]);
    	}
    	
    	for(int i = 0; i < brackets.length; i++){
    		menuPanels[0].add(brackets[i]);
    	}
    	
    	for(int i = 0; i < functionLabels.length; i++){
    		menuPanels[0].add(functionLabels[i]);
    	}
    	
    	for(int i = 0; i < units.length; i++){
    		menuPanels[0].add(units[i]);
    	}
    	
    	for(int i = 0; i < numInputs.length; i++){
    		menuPanels[0].add(numInputs[i]);
    	}
    	
    	titleBar.add(icons[0]);
    	menuPanels[0].add(icons[1]);
    	menuPanels[0].add(title);
    	menuPanels[0].add(divider);
    	menuPanels[0].add(about);
    	menuPanels[0].add(selectionNotif);
    	menuPanels[0].add(speedSlider);
    	
    	//the progress bar is only needed for the loading screen
    	menuPanels[1].add(progressBar);
    	
    	//add all components required for the cut selection panel
    	menuPanels[3].add(titleOfMenu3);
    	menuPanels[3].add(instruction);
    	menuPanels[3].add(pageNum);
    	menuPanels[3].add(nextButton);
    	menuPanels[3].add(previousButton);
    	menuPanels[3].add(runButton);
    	
    	//menuPanels[2] is the startup screen so when that it shows the splash screen when the program starts
        cardLayout.show(contPanel, "2");
    }
    
    //method that checks whether all the necessary user inputs are filled
    public boolean checkAllVerif(){
    	//i should use switch but didn't bother too...
    	//the "none" silence option
    	if(silenceOptionNum == 3){
    		//the none option doesn't require the text fields to be filled
    		for(int i = 2; i < verifLabel.length; i++){
    			verified[i] = true;
    			verifLabel[i].setIcon(new javax.swing.ImageIcon(getClass().getResource("/verified.png")));
    		}
    	}
    	//the "cut" silence option
    	if(silenceOptionNum == 2){
			verifLabel[2].setIcon(new javax.swing.ImageIcon(getClass().getResource("/verified.png")));
    		checkTextFieldVerif();
    	}
    	//the "speed up" silence option
    	if(silenceOptionNum == 1){
			verifLabel[2].setIcon(new javax.swing.ImageIcon(getClass().getResource("/verified.png")));
    		checkTextFieldVerif();
    	}
    	//the loop goes through a boolean array to check whether everything is ready for the user to be able to run the program
    	for(int i = 0; i < verified.length; i++){
    		if(!verified[i]){
    			buttons[6].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button6_notready.png")));
    			return false;
    		} else {
    			buttons[6].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button6.png")));
    		}
    	}
    	return true;
    }
    
    //method that checks whether the input video and output directory are selected 
    public void checkPathSelectionVerif(){
    	changeVerif(!Executive.getAudioVideo().getVideoInputStringPath().isEmpty(), 0);
    	changeVerif(!Executive.getAudioVideo().getVideoOutputStringPath().isEmpty(), 1);
    }
    
    //method that checks whether the necessary text fields are all inputed
    public void checkTextFieldVerif(){
    	//initializes the margins from the text fields
    	double volumeMargin = textFieldToDouble(numInputs[0].getText());
    	int silenceMargin = textFieldToInt(numInputs[1].getText(), 1);
    	int fieldMargin = textFieldToInt(numInputs[2].getText(), 2);
    	
    	//volume waveform pixel height/2 = 250 so just set less than that to max
    	changeVerif(volumeMargin < 24 && volumeMargin > 0, 3);
    	
    	//verification is based on whether the input video is selected
    	if(Executive.getAudioVideo().getVideoLength() != 0){
    		//silence margin must be less than the video length but must be at least 1 second
    		changeVerif((silenceMargin <= Executive.getAudioVideo().getVideoLength()) && (silenceMargin > 0), 4);
    	} else {
    		//default is 1 second to 1 minute silence margin
    		changeVerif((silenceMargin <= 60) && (silenceMargin > 0), 4);
    	}
    	
    	//field margin is at least 0 seconds and less than half of the silence margin
    	changeVerif(fieldMargin <= (double)silenceMargin/2 && fieldMargin >= 0, 5);
    }
    
    //method that changes the state of a particular value in our boolean array and sets the icons accordingly
    private void changeVerif(boolean b, int i){
    	if(!b){
    		verified[i] = false;
    		verifLabel[i].setIcon(new javax.swing.ImageIcon(getClass().getResource("/unverified.png")));
    	} else {
    		verified[i] = true;
    		verifLabel[i].setIcon(new javax.swing.ImageIcon(getClass().getResource("/verified.png")));
    	}
    }
    
    //method that turns a text field into an integer given the inside text and the actual text field index
    private Integer textFieldToInt(String text, int i) {
    	  try {
    	    return Integer.parseInt(text);
    	  } catch (NumberFormatException e) {
    		verified[i + 3] = false;
        	verifLabel[i + 3].setIcon(new javax.swing.ImageIcon(getClass().getResource("/unverified.png")));
    		return -1;
    	  }
    }
    
    //method that turns a text field into a double given the inside text
    private Double textFieldToDouble(String text) {
  	  try {
  	    return Double.parseDouble(text);
  	  } catch (NumberFormatException e) {
  		//-1 isn't used and is therefore useful for the "null" case
  		return -1.0;
  	  }
    }
    
    //method that updates the text area that notifies which selections are valid and which are not
    public void updateSelectionNotif(){
    	selectionNotif.setText("");
		String[] errNotif = {"Please specify an mp4 video to input!", "Please specify an output directory!", 
				"Please select a silence option!", 
				"Please input a volume sensitivity between 0 and 24 exclusive! Volume sensitivity input must be a real number.", 
				"Please input a silence margin between 1 and 60 seconds inclusive! Silence margin input must be a positive integer.", 
				"Please input a field margin that is at most half the silence margin! Field margin input must be a positive integer."};
		String[] completeNotif = {"The input video has been specified!", "The output directory has been specified!", 
				"The silence option has been selected!", "The volume sensitivity is valid!", "The silence margin is valid!", 
				"The field margin is valid!"};
		//goes through each value in our verification boolean array and updates the lines accordingly
		for(int i = 0; i < verified.length; i++){
			if(!verified[i]){
				if(i == 0){
					selectionNotif.setText(errNotif[i]);
				} else {
					selectionNotif.setText(selectionNotif.getText() + "\n" + errNotif[i]);
				}
			} else {
				if (i == 0){
					selectionNotif.setText(completeNotif[i]);
				} else {
					selectionNotif.setText(selectionNotif.getText() + "\n" + completeNotif[i]);
				}
			}
		}
    }
    
    //creates all the graphics for the cut selection menu panels
    public void executeCutSelection(File[] matchingFiles){
    	//only the important changes are made here
    	int numOfNewPanels = matchingFiles.length/16 + 1;
    	pageNum.setText("Page " + currentPageNum + "/" + numOfNewPanels);
    	
		//the next and previous buttons get mouse listeners so that clicking allows the user to go from panel to panel
		nextButton.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {
			}
			public void mouseEntered(MouseEvent e) {
				nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/nextButton_hover.png")));
			}
			public void mouseExited(MouseEvent e) {
				nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/nextButton.png")));
			}
			public void mousePressed(MouseEvent e) {
				System.out.println(Thread.currentThread());
				if(currentPageNum == numOfNewPanels){
					currentPageNum = 1;
					pageNum.setText("Page 1/" + numOfNewPanels);
					checkCardLayout.show(checkContPanel, "0");
				} else {
					currentPageNum++;
					pageNum.setText("Page " + currentPageNum + "/" + numOfNewPanels);
					checkCardLayout.show(checkContPanel, String.valueOf(currentPageNum - 1));
				}
			}
			public void mouseReleased(MouseEvent e) {
			}
		});
		previousButton.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {
			}
			public void mouseEntered(MouseEvent e) {
				previousButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/previousButton_hover.png")));
			}
			public void mouseExited(MouseEvent e) {
				previousButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/previousButton.png")));
			}
			public void mousePressed(MouseEvent e) {
				System.out.println(Thread.currentThread());
				if(currentPageNum == 1){
					currentPageNum = numOfNewPanels;
					pageNum.setText("Page " + numOfNewPanels + "/" + numOfNewPanels);
					checkCardLayout.show(checkContPanel, String.valueOf(numOfNewPanels - 1));
				} else {
					currentPageNum--;
					pageNum.setText("Page " + currentPageNum + "/" + numOfNewPanels);
					checkCardLayout.show(checkContPanel, String.valueOf(currentPageNum - 1));
				}
			}
			public void mouseReleased(MouseEvent e) {
			}
		});
		//the run buttons get mouse listeners so that we can continue running the program after the selections
		runButton.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {
			}
			public void mouseEntered(MouseEvent e) {
				runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/button6_hover.png")));
			}
			public void mouseExited(MouseEvent e) {
				runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/button6.png")));
			}
			public void mousePressed(MouseEvent e) {
				//switches to the loading screen
				cardLayout.show(contPanel,  "1");
				Executive.getExecutor().execute(new Runnable(){
		        	public void run(){
						int numOfFiles = 0;
						//renames all the files
						//note that we have to stack the loops because renaming requires uniqueness
						for(int i = 0; i < cutsList.size(); i++){
							if(!cutsList.get(i).isSelected()){
								matchingFiles[i].delete();
							}
						}
						for(int i = 0; i < cutsList.size(); i++){
							if(cutsList.get(i).isSelected()){
								matchingFiles[i].renameTo(new File(Executive.getAudioVideo().getVideoOutputStringPath() + "/usedcut_" + numOfFiles + ".mp4"));
								numOfFiles++;
							}
						}
						//resets the position of the loading animation
						loadingLabel.setBounds(1100/2 - loadingLabel.getPreferredSize().width/2, 730/2 - 50 - loadingLabel.getPreferredSize().height/2, 
				    			loadingLabel.getPreferredSize().width, loadingLabel.getPreferredSize().height);
						progressBar.setVisible(false);
						//concatenates the final cuts together
						//this is only accessed when the video has cuts
						Executive.getAudioVideo().concatenateFinalVideos(numOfFiles);
						//pops up the output directory for user to check once the final process has finished
						try {
							Desktop.getDesktop().open(new File(Executive.getAudioVideo().getVideoOutputStringPath()));
						} catch (IOException e) {
							e.printStackTrace();
						}
						//sfx after completion
						try {
							AudioInputStream in = AudioSystem.getAudioInputStream(getClass().getResource("/complete.wav"));
							Clip clip = null;
							try {
								clip = AudioSystem.getClip();
							} catch (LineUnavailableException e) {
								e.printStackTrace();
							}
							try {
								clip.open(in);
							} catch (LineUnavailableException e) {
								e.printStackTrace();
							}
							clip.start();
						} catch (UnsupportedAudioFileException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						//resets the progress bar, arrays, etc.
						//it then switches back to the main menu again
						progress = 0;
						progressBar.setValue(0);
						progressBar.setVisible(false);
						progressBar.setIndeterminate(true);
						Executive.getAudioVideo().getSpeedTimes().clear();
						Executive.getAudioVideo().getSpeedTimesInSeconds().clear();
						Executive.getAudioVideo().getRemoveTimeStamps().clear();
						cutsList.clear();
						checkBoxPanels.clear();
						menuPanels[3].remove(checkContPanel);
						for(int i = 0; i < numOfNewPanels; i++){
							checkContPanel.remove(i);
						}
						nextButton.removeMouseListener(nextButton.getMouseListeners()[0]);
						previousButton.removeMouseListener(previousButton.getMouseListeners()[0]);
						runButton.removeMouseListener(runButton.getMouseListeners()[0]);
						cardLayout.show(contPanel, "0");
		        	}
		        });
			}
			@Override
			public void mouseReleased(MouseEvent e) {
			}
		});
		
		//creates the needed panels for the check box list
		for(int i = 0; i < numOfNewPanels; i++){
			checkBoxPanels.add(new JPanel());
			checkBoxPanels.get(i).setLayout(null);
			checkBoxPanels.get(i).setBackground(Color.BLACK);
			checkBoxPanels.get(i).setBounds(30, 130, 700, 550);
        	checkContPanel.add(checkBoxPanels.get(i), String.valueOf(i));
		}
		menuPanels[3].add(checkContPanel);
		for(int i = 0; i < matchingFiles.length; i++){
			//creates and organizes the JCheckBox list so that each "page" has a max of 16 files
			cutsList.add(new JCheckBox());
			cutsList.get(i).setText(" " + matchingFiles[i].getName());
			cutsList.get(i).setFont(new Font("Calibri Light", Font.PLAIN, 50));
			cutsList.get(i).setForeground(Color.WHITE);
			cutsList.get(i).setBackground(Color.BLACK);
			cutsList.get(i).setIcon(new javax.swing.ImageIcon(getClass().getResource("/unverified.png")));
			cutsList.get(i).setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/verified.png")));
			if(i % 16 < 8){
				cutsList.get(i).setBounds(0, 70*(i % 16), 350, 60);
			} else {
				cutsList.get(i).setBounds(350, 70*(i % 16 - 8), 350, 60);
			}
			checkBoxPanels.get(i/16).add(cutsList.get(i));
		}
    }
    
    //method that sorts the files based on number //code below is copied and pasted btw
    public void sortByNumber(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File o1, File o2) {
                int n1 = extractNumber(o1.getName());
                int n2 = extractNumber(o2.getName());
                return n1 - n2;
            }
            private int extractNumber(String name) {
                int i = 0;
                try {
                    int s = name.indexOf('_')+1;
                    int e = name.lastIndexOf('.');
                    String number = name.substring(s, e);
                    i = Integer.parseInt(number);
                } catch(Exception e) {
                    i = 0;
                }
                return i;
            }
        });
    }
    
    //methods that switch the screen state
    public void switchToLoadingScreen(){
    	cardLayout.show(contPanel, "1");
    }
    
    public void switchToMainMenuScreen(){
    	cardLayout.show(contPanel, "0");
    	Executive.getSplashTimer().stop();
    }
    
    public void switchToSelectionScreen(){
    	cardLayout.show(contPanel, "3");
    }
    
    //a bunch of getters and setters
    public double getSpeed(){
    	return (double)speedSlider.getValue()/100;
    }
    
    public int getVolumeSensitivity(){
    	if(Double.parseDouble(numInputs[0].getText()) < 0.1){
    		return 1;
    	} else {
    		return (int) Math.round(10*Double.parseDouble(numInputs[0].getText()));
    	}
    }
    
    //silence margin will be used in its converted form in detectVolume method
    public int getSilenceMargin(){
    	return Integer.parseInt(numInputs[1].getText());
    }
    
    //we better convert seconds into pixels
    public int getFieldMargin(){
    	return (int)(((double)5000/60)*Integer.parseInt(numInputs[2].getText()));
    }
    
    public JPanel getMenuPanel(int i){
    	return menuPanels[i];
    }
    
    public ArrayList<JPanel> getCheckBoxPanels(){
    	return checkBoxPanels;
    }
    
    public JLabel getLoadingAnim(){
    	return loadingLabel;
    }
	
    public JProgressBar getProgressBar(){
    	return progressBar;
    }
    
    public int getProgress(){
    	return progress;
    }
    
    public void setProgress(int value){
    	progress = value;
    }
    
    public boolean[] getVerified(){
    	return verified;
    }
    
    public int getSilenceOptionNum(){
    	return silenceOptionNum;
    }
    
    public void setSilenceOptionNum(int value){
    	silenceOptionNum = value;
    }
}
