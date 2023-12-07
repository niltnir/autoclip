package roku.clip_editor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileNameExtensionFilter;

//ver. 2.2.0.0
public class ButtonMouseEvent extends MouseAdapter{
	
	private static JFileChooser fileChooser = new JFileChooser();
	JLabel button[];
	int buttonNum;
	
	//constructor of the Mouse Event
	public ButtonMouseEvent(JLabel[] buttonLabel, int number){
		button = buttonLabel;
		buttonNum = number;
		
		for(int i = 0; i < buttonLabel.length; i++){
			button[i] = buttonLabel[i];
		}
	}
	
	public void mousePressed(MouseEvent e){
		Executive.getFrame().checkAllVerif();
		//buttonNum 1 to 3 are the silence options
		if((0 < buttonNum) && (buttonNum < 4)){
			//verified 2 is the silence option verification boolean
			Executive.getFrame().getVerified()[2] = true;
			//selecting an option will not allow the user to select more
			//clicking the buttons will also check the verification boolean array as well
			switch(buttonNum){
				case 1:
					button[buttonNum].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button1_clicked.png")));
					button[2].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button2.png")));
					button[3].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button3.png")));
					Executive.getFrame().setSilenceOptionNum(1);
					Executive.getFrame().checkAllVerif();
					break;
				case 2:
					button[1].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button1.png")));
					button[buttonNum].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button2_clicked.png")));
					button[3].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button3.png")));
					Executive.getFrame().setSilenceOptionNum(2);
					Executive.getFrame().checkAllVerif();
					break;
				case 3:
					button[1].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button1.png")));
					button[2].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button2.png")));
					button[buttonNum].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button3_clicked.png")));
					Executive.getFrame().setSilenceOptionNum(3);
					Executive.getFrame().checkAllVerif();
					break;
			}
			//we change the color of the RUN button if choosing the silence option completes the verification process
			if(Executive.getFrame().checkAllVerif()){
				button[6].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button6.png")));
			}
		//select input video
		} else if (buttonNum == 0) {
			//find and get input video path through a JFileChooser pop up
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop"));
			fileChooser.setDialogTitle("Input Video");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
	                "Videos", "mp4", "MOV", "MTS");
	        fileChooser.setFileFilter(filter);
	        int returnVal = fileChooser.showOpenDialog(Executive.getFrame());
	        if(returnVal == JFileChooser.APPROVE_OPTION) {
	            System.out.println(fileChooser.getSelectedFile().getAbsolutePath().replace("\\", "/"));
	            Executive.getAudioVideo().setVideoInputStringPath(fileChooser.getSelectedFile().getAbsolutePath().replace("\\", "/"));
	            Executive.getFrame().checkPathSelectionVerif();
	            Executive.getFrame().switchToLoadingScreen();
	            //executor allows for multiple processes to go on at once (Runtime.exec() for both ffmpeg and frame)
	            //the mini-program below gets us all the initial info needed about the video file
	            Executive.getExecutor().execute(new Runnable(){
		        	public void run(){
		        		System.out.println(Thread.currentThread());
		        		//converts the input mp4 to wav
		        		Executive.getAudioVideo().convertAudio();
		    			//gets the audio file
		    			File audio = Executive.getAudioVideo().retrieveAudio();
		    			//set the videoLength variable through the main getVideoLength method 
		    			Executive.getAudioVideo().setVideoLength(Executive.getAudioVideo().getVideoLength(audio));
		    			//get set the String equivalent in hh:mm:ss
		    			Executive.getAudioVideo().setVideoLengthString(Executive.getAudioVideo().convertSecsToTimeString(Executive.getAudioVideo().getVideoLength()));
		    			checkVideoContainer(Executive.getAudioVideo().getVideoInputStringPath());
		    			//go back to the main menu screen
		    			Executive.getFrame().switchToMainMenuScreen();
		        	}
		        });
	        }
	        fileChooser.removeChoosableFileFilter(filter);
	        //checkAllVerif should update after selection
	        Executive.getFrame().checkAllVerif();
	    //select output directory
		} else if (buttonNum == 4) {
			//find and get directory path through a JFileChooser pop up
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop"));
			fileChooser.setDialogTitle("Output Directory");
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	        int returnVal = fileChooser.showOpenDialog(Executive.getFrame());
	        if(returnVal == JFileChooser.APPROVE_OPTION) {
	        	System.out.println(fileChooser.getSelectedFile().getAbsolutePath().replace("\\", "/"));
	            Executive.getAudioVideo().setVideoOutputStringPath(fileChooser.getSelectedFile().getAbsolutePath().replace("\\", "/"));
	            Executive.getFrame().checkPathSelectionVerif();
	        }
	        //checkAllVerif should update after selection
	        Executive.getFrame().checkAllVerif();
	    //select end card video
		} else if (buttonNum == 5) {
			//find and get input video path through a JFileChooser pop up
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop"));
			fileChooser.setDialogTitle("Input Video");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
	                "Videos", "mp4");
	        fileChooser.setFileFilter(filter);
	        int returnVal = fileChooser.showOpenDialog(Executive.getFrame());
	        if(returnVal == JFileChooser.APPROVE_OPTION) {
	        	System.out.println(fileChooser.getSelectedFile().getAbsolutePath().replace("\\", "/"));
	            Executive.getAudioVideo().setEndCardStringPath(fileChooser.getSelectedFile().getAbsolutePath().replace("\\", "/"));
	        }
	        fileChooser.removeChoosableFileFilter(filter);
	    //run is clicked when the necessary inputs are invalid or not inputed
		} else if (buttonNum == 6 && !Executive.getFrame().checkAllVerif()) {
			System.out.println("Selection invalid!");
			System.out.println(Thread.currentThread());
		//run is clicked when all necessary inputs are valid
		} else if (buttonNum == 6 && Executive.getFrame().checkAllVerif()) {
			//create the video segments directory if it doesn't exist
			new File(Executive.getAudioVideo().getProjectStringPath() + "/Runtime Files/Video Segments").mkdirs();
			//the loading screen is reset
			Executive.getFrame().getLoadingAnim().setBounds(1100/2 - Executive.getFrame().getLoadingAnim().getPreferredSize().width/2, 
					270 - Executive.getFrame().getLoadingAnim().getPreferredSize().height/2, 
	    			Executive.getFrame().getLoadingAnim().getPreferredSize().width, Executive.getFrame().getLoadingAnim().getPreferredSize().height);
			Executive.getFrame().switchToLoadingScreen();
			Executive.getFrame().getProgressBar().setVisible(true);
			Executive.getFrame().getProgressBar().setIndeterminate(true);
			//executor allows for multiple processes to go on at once (Runtime.exec() for both ffmpeg and frame)
			//execution changes depending on the silence option
			switch(Executive.getFrame().getSilenceOptionNum()){
			case 1:
				//cuts out silences
				Executive.getExecutor().execute(new Runnable(){
		        	public void run(){
		        		System.out.println(Thread.currentThread());
		        		//detect volume, set up progress bar, load the speeded silences, load the used videos, 
		        		//concatenate them, load the final cuts, reset the progress bar, and go to the latter execution
		        		Executive.getAudioVideo().detectVolume(Executive.getFrame().getVolumeSensitivity(), Executive.getFrame().getSilenceMargin(), 
		        				Executive.getFrame().getFieldMargin(), Executive.getAudioVideo().getVideoLength(), Executive.getAudioVideo().getVideoInputStringPath());
		        		
		        		if(Executive.getAudioVideo().getEditVideo()){
		        			//setting maximum of loading bar accordingly
			        		if(Executive.getAudioVideo().getSpeedTimes().size() % 2 == 1){
			        			Executive.getFrame().getProgressBar().setMaximum((int)(Executive.getAudioVideo().getSpeedTimes().size() - 1
				        				+ (int)Executive.getAudioVideo().getVideoLength()/600 + 19 + 2));
				        		/*Executive.maxLoadBar = (int)(Executive.getAudioVideo().getSpeedTimes().size() - 1
				        				+ (int)Executive.getAudioVideo().getVideoLength()/600 + 19 + 2);*/
			        		} else {
			        			Executive.getFrame().getProgressBar().setMaximum((int)(Executive.getAudioVideo().getSpeedTimes().size()
				        				+ (int)Executive.getAudioVideo().getVideoLength()/600 + 19 + 2));
				        		/*Executive.maxLoadBar = (int)(Executive.getAudioVideo().getSpeedTimes().size() 
				        				+ (int)Executive.getAudioVideo().getVideoLength()/600 + 19 + 2);*/
			        		}
			        		
			        		Executive.getFrame().getProgressBar().setIndeterminate(false);
							Executive.getAudioVideo().loadUsedVideo(Executive.getAudioVideo().getVideoLengthString(), false);
							Executive.getAudioVideo().concatenateMidVideos(true);
							Executive.getAudioVideo().loadFinalCuts(false);
							Executive.latterExecution();
		        		} else {
		        			resetToDefault();
		        			Executive.getFrame().switchToMainMenuScreen();
		        		}
		        	}
		        });
				break;
			case 2:
				//speeds up silences
				Executive.getExecutor().execute(new Runnable(){
		        	public void run(){
		        		System.out.println(Thread.currentThread());
		        		//detect volume, set up progress bar, load the speeded silences, load the used videos, 
		        		//concatenate them, load the final cuts, reset the progress bar, and go to the latter execution
		        		Executive.getAudioVideo().detectVolume(Executive.getFrame().getVolumeSensitivity(), Executive.getFrame().getSilenceMargin(), 
		        				Executive.getFrame().getFieldMargin(), Executive.getAudioVideo().getVideoLength(), Executive.getAudioVideo().getVideoInputStringPath());
		        		
		        		if(Executive.getAudioVideo().getEditVideo()){
		        			//setting maximum of loading bar accordingly
			        		if(Executive.getAudioVideo().getSpeedTimes().size() % 2 == 1){
			        			Executive.getFrame().getProgressBar().setMaximum((int)(2.5*(Executive.getAudioVideo().getSpeedTimes().size() - 1) 
				        				+ (int)Executive.getAudioVideo().getVideoLength()/600 + 19 + 2));
			        			/*Executive.maxLoadBar = (int)(2.5*(Executive.getAudioVideo().getSpeedTimes().size() - 1) 
				        				+ (int)Executive.getAudioVideo().getVideoLength()/600 + 19 + 2);*/
			        		} else {
			        			Executive.getFrame().getProgressBar().setMaximum((int)(2.5*Executive.getAudioVideo().getSpeedTimes().size()
				        				+ (int)Executive.getAudioVideo().getVideoLength()/600 + 19 + 2));
			        			/*Executive.maxLoadBar = (int)(2.5*Executive.getAudioVideo().getSpeedTimes().size() 
				        				+ (int)Executive.getAudioVideo().getVideoLength()/600 + 19 + 2);*/
			        		}
			        		
			        		Executive.getFrame().getProgressBar().setIndeterminate(false);
			        		Executive.getAudioVideo().loadSpeededSilence(Executive.getFrame().getSpeed());
			        		Executive.getAudioVideo().loadUsedVideo(Executive.getAudioVideo().getVideoLengthString(), true);
							Executive.getAudioVideo().concatenateMidVideos(false);
							Executive.getAudioVideo().loadFinalCuts(false);
							Executive.latterExecution();
		        		} else {
		        			resetToDefault();
		        			Executive.getFrame().switchToMainMenuScreen();
		        		}
		        	}
		        });
				break;
			case 3:
				//nothing is done to the silences
				Executive.getFrame().getProgressBar().setVisible(false);
				Executive.getExecutor().execute(new Runnable(){
		        	public void run(){
		        		System.out.println(Thread.currentThread());
		        		Executive.getAudioVideo().loadFinalCuts(true);
						Executive.latterExecution();
		        	}
		        });
				break;
			}
		}
		//we want to update the selection verification text area no matter what button is pressed
		Executive.getFrame().updateSelectionNotif();
	}
	
	//change the button image to the hovered version when the cursor enters the button
	public void mouseEntered(MouseEvent e){
		//buttonNum 6 is the RUN button so there is a bit of complication there where verification is needed
		if(Executive.getFrame().getSilenceOptionNum() != buttonNum && buttonNum != 6){
			button[buttonNum].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button" + buttonNum + "_hover.png")));
		} else if (!Executive.getFrame().checkAllVerif() && buttonNum == 6) {
			button[buttonNum].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button6_notready_hover.png")));
		} else if (Executive.getFrame().getSilenceOptionNum() != buttonNum && Executive.getFrame().checkAllVerif() && buttonNum == 6){
			button[buttonNum].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button" + buttonNum + "_hover.png")));
		}
	}
	
	//change the button image back to normal when the cursor exits the button
	public void mouseExited(MouseEvent e){
		//buttonNum 6 is the RUN button so there is a bit of complication there where verification is needed
		if(Executive.getFrame().getSilenceOptionNum() != buttonNum && buttonNum != 6){
			button[buttonNum].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button" + buttonNum + ".png")));
		} else if (!Executive.getFrame().checkAllVerif() && buttonNum == 6) {
			button[buttonNum].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button6_notready.png")));
		} else if (Executive.getFrame().getSilenceOptionNum() != buttonNum && Executive.getFrame().checkAllVerif() && buttonNum == 6){
			button[buttonNum].setIcon(new javax.swing.ImageIcon(getClass().getResource("/button" + buttonNum + ".png")));
		}
	}
	
	public void checkVideoContainer(String path){
		if(path.endsWith("mp4")){
			System.out.println("It's an mp4 file!");
			Executive.getAudioVideo().setVideoContainer(".mp4");
		} else if (path.endsWith("MOV")){
			System.out.println("It's an MOV file!");
			Executive.getAudioVideo().setVideoContainer(".MOV");
		} else if (path.endsWith("mts")){
			System.out.println("It's an MTS file!");
			Executive.getAudioVideo().setVideoContainer(".MTS");
		}
	}
	
	public void resetToDefault(){
		Executive.getAudioVideo().setEditVideo(true);
		Executive.getAudioVideo().getSpeedTimesInSeconds().clear();
		Executive.getAudioVideo().getSpeedTimes().clear();
		Executive.getFrame().setProgress(0);
		Executive.getFrame().getProgressBar().setValue(0);
	}
	
}
