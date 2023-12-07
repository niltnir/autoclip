package roku.clip_editor;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.UIManager;

//ver. 2.2.0.0
public class Executive{
	
	private static FFmpeg audioVideo = new FFmpeg();
	private static Frame frame;
	private static ExecutorService executor = Executors.newFixedThreadPool(2);
	private static Timer splashTimer;
	
	public static void main(String[] args) {
		
		//this is for the JFileChooser so that the user isn't overwhelmed by old Java graphics
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			System.out.println("Windows look and feel failed...");
		}
		
		if(!ffmpegChecker()){
			//warn user about the requirement of ffmpeg
			String options[] = {"Exit"};
			int option = JOptionPane.showOptionDialog(null, 
					"This program requires downloading ffmpeg to the C drive.\nPlease read the manual for further download instructions.",
	                "Download Required", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, new ImageIcon(Executive.class.getClass().getResource("/logo_icon_2.png")), options, options[0]);
			if(option == 0){
				Executive.getAudioVideo().deleteDir(new File(Executive.getAudioVideo().getProjectStringPath() + "/Runtime Files"));
				System.exit(0);
			};
		} else {
			frame = new Frame();
			frame.setVisible(true);
			audioVideo.deleteDir(new File(audioVideo.getProjectStringPath() + "/Runtime Files"));
			
			splashTimer = new Timer(3000, new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					frame.switchToMainMenuScreen();
				}
			});
			
			splashTimer.start();
			
			//note that swing timers automatically repeat (good for games and stuff)
			splashTimer.setRepeats(false);
		}
	}
	
	public static void latterExecution(){
		//output directory pops up after mid/final editing completion (depends on if the video has cuts)
		try {
			Desktop.getDesktop().open(new File(audioVideo.getVideoOutputStringPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//sfx after completion
		try {
			AudioInputStream in;
			try {
				in = AudioSystem.getAudioInputStream(Executive.class.getClass().getResource("/complete.wav"));
			} catch (UnsupportedAudioFileException e1) {
				in = null;
				e1.printStackTrace();
			}
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		//if the video has cuts it will proceed by going to the cut selection menu
		if(audioVideo.getFinalConcatBoolean()){
			File searchFile = new File(audioVideo.getVideoOutputStringPath());
			File[] matchingFiles = searchFile.listFiles(new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			        return name.startsWith("cut") && name.endsWith("mp4");
			    }
			});
			frame.sortByNumber(matchingFiles);
			frame.executeCutSelection(matchingFiles);
			frame.switchToSelectionScreen();
		//otherwise the process will simply end by deleting and going to the main menu screen
		} else {
			//delete the used runtime directory
			audioVideo.deleteDir(new File(audioVideo.getProjectStringPath() + "/Runtime Files/Video Segments"));
			frame.switchToMainMenuScreen();
			//resets all previously used components like the progress bar and array lists 
			Executive.getFrame().setProgress(0);
			Executive.getFrame().getProgressBar().setValue(Executive.getFrame().getProgress());
			//System.out.println(Executive.getFrame().getProgress() + "/" + Executive.maxLoadBar);
			Executive.getFrame().getProgressBar().setVisible(false);
			Executive.getFrame().getProgressBar().setIndeterminate(true);
			Executive.getAudioVideo().getSpeedTimes().clear();
			Executive.getAudioVideo().getSpeedTimesInSeconds().clear();
			Executive.getAudioVideo().getRemoveTimeStamps().clear();
		}
	}
	
	private static boolean ffmpegChecker(){
		
		boolean ffmpegExists = false;
		String testCommand[] = {"C:/ffmpeg/bin/ffmpeg.exe"};
		try {
			Process checkFFmpeg = Runtime.getRuntime().exec(testCommand);
			BufferedReader in = new BufferedReader(new InputStreamReader(checkFFmpeg.getErrorStream()));
			String line;
			while((line = in.readLine()) != null){
				if(line.contains("Hyper fast Audio and Video encoder")){
					System.out.println("ffmpeg exists!");
					ffmpegExists = true;
				}
			}
			try {
				checkFFmpeg.waitFor();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ffmpegExists;
	}
	
	public static Frame getFrame(){
		return frame;
	}
	
	public static FFmpeg getAudioVideo(){
		return audioVideo;
	}
	
	public static ExecutorService getExecutor(){
		return executor;
	}
	//gets splash timer
	public static Timer getSplashTimer(){
		return splashTimer;
	}
}