package roku.clip_editor;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

//ver. 2.2.0.0
public class FFmpeg {
	
	//paths have been edited so that user inputs them
	private static final String ffmpeg = "C:/ffmpeg/bin/ffmpeg.exe";
	private static final String projectStringPath = "C:/ffmpeg/bin/";
	private static String videoInputStringPath = "";
	private static String videoOutputStringPath = "";
	private static String endCardStringPath = "";
	
	private double videoLength;
	private String videoLengthString;
	private List<Double> speedTimesInSeconds = new ArrayList<>();
	private ArrayList<String> speedTimes = new ArrayList<String>();
	private String line;
	private boolean finalConcatNeeded;
	private String videoContainer;
	private ArrayList<String> removeTimeStamps = new ArrayList<String>();
	private boolean editVideo = true;
	
	//method that detects the volume by creating waveform and line images and then getting start/end times from them
	//i should probably split this into three methods but i'm too lazy...
	public void detectVolume(int volumeMargin, int silenceMargin, int fieldMargin, double videoDurationSeconds, String videoPath){
		
		//default height and width ratio of the waveforms
		int height = 500;
		int widthRatio = 5000;
		
		int numOfSegments = (int) (Math.floor(videoDurationSeconds/600))+ 1;
		String[] outputPath = new String[numOfSegments];
		
		//durationSecondsCounter is needed to split waveform into 10 minute segments
		double durationSecondsCounter = videoDurationSeconds;
		double[] segmentedDurationSeconds = new double[numOfSegments];
		String startTime;
		String endTime;
		
		//this loop splits big waveforms into multiple smaller waveforms so that image file doesn't crash
		for(int i = 0; i < numOfSegments; i++){
			
			//create the directory "Audio" if the file doesn't exist
			if(!new File(projectStringPath + "Runtime Files/Audio").exists()){
				new File(projectStringPath + "Runtime Files/Audio").mkdir();
			}
			//create the output path for where the waveform images will end up
			outputPath[i] =  projectStringPath + "Runtime Files/Audio/waveform" + i + ".png";
			
			//code below gets a 10 minute segment and creates a new waveform image accordingly
			if(durationSecondsCounter > 600){
				segmentedDurationSeconds[i] = 600;
				durationSecondsCounter = durationSecondsCounter - 600;
				
				startTime = convertSecsToTimeString(600*i);
				endTime = convertSecsToTimeString(600*(i + 1));
				ffmpegWaveForm(startTime, endTime, videoPath, widthRatio, height, segmentedDurationSeconds[i], outputPath[i]);
			//the rest is added in the remaining image file
			} else {
				segmentedDurationSeconds[i] = durationSecondsCounter;
				
				startTime = convertSecsToTimeString(600*i);
				endTime = convertSecsToTimeString(600*i + segmentedDurationSeconds[i]);
				ffmpegWaveForm(startTime, endTime, videoPath, widthRatio, height, segmentedDurationSeconds[i], outputPath[i]);
			}
			System.out.println(i + ": " + startTime + ", " + endTime);
		}
		
        //take image find the center line, move volumeMargin amount away from the center line
		//detect where it doesn't hit red and cut out until it hits red again (width margin of error from that on either sides)
		//larger the margin, the more sensitive to sound
		File[] waveFiles = new File[numOfSegments];
        BufferedImage[] waveformImgs = new BufferedImage[numOfSegments];
        int waveHeightCenter;
        int volumeMarginHeight;
        //these will be the line images used to detect silence
        BufferedImage[] detectionImgs = new BufferedImage[numOfSegments];
        //assign each waveform image we just created to waveFormImgs[i]
        for(int i = 0; i < numOfSegments; i++){
        	waveFiles[i] = new File(outputPath[i]);
        	try {
				waveformImgs[i] = ImageIO.read(waveFiles[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        //we need the center of the image and the volume margin to find where the line is
        waveHeightCenter = waveformImgs[0].getHeight()/2;
        volumeMarginHeight = waveHeightCenter - volumeMargin;
        
        for(int i = 0; i < numOfSegments; i++){
        	detectionImgs[i] = getDetectionImg(waveformImgs[i], waveformImgs[i].getWidth(), volumeMarginHeight);
        	//writes images into project file
        	try {
				ImageIO.write(detectionImgs[i], "png", new File(projectStringPath + "Runtime Files/Audio/linemargin" + i + ".png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        //increments the progress bar
        Executive.getFrame().setProgress(Executive.getFrame().getProgress() + 1);
        Executive.getFrame().getProgressBar().setValue(Executive.getFrame().getProgress());
        //System.out.println(Executive.getFrame().getProgress() + "/" + Executive.maxLoadBar);
        
        int silenceCounter = 0;
        int noiseCounter = 0;
        int p;
        int beginPixelPosition;
        int endPixelPosition;
        double beginTimePosition;
        double endTimePosition;
        boolean isSilent = false;
        
        //time --> pixels // 1 min = wdr pixels // x mins = [round(wdr/60secs)]*(x * 60sec) + i*600*(widthRatio/60) pixels
        //11 mins = round[5000/60]*(11 * 60) pixels
        //x seconds = (x/60)*wdr pixels
        //pixels --> time// x pixel = 60*(x/wdr) seconds
        //0 = empty, -65536 = red
        //we extract out the start and end times from the line images
        //the if statements take in the pixel to seconds conversion factor to do so
        for(int i = 0; i < numOfSegments; i++){
        	for(int j = 0; j < detectionImgs[i].getWidth(); j++){
        		p = detectionImgs[i].getRGB(j, 0);
        		//counter counts up the number of blank pixels so that it can get start/end times when it hits the silence margin
        		if(p == 0){
        			silenceCounter++;
        			if(silenceCounter == (int)(((double)silenceMargin/60)*widthRatio)){
        				if(i == 0 && silenceCounter == j + 1){
        					System.out.println("There is silence at the beginning.");
        				} else {
        					beginPixelPosition = j - (int)(((double)silenceMargin/60)*widthRatio) + fieldMargin;
            				beginTimePosition = 60*((double)beginPixelPosition/widthRatio) + i*600;
            				speedTimesInSeconds.add(beginTimePosition);
            				speedTimes.add(convertSecsToTimeString(beginTimePosition));
            				isSilent = true;
        				}
        			}
        		} else if(p != 0 && isSilent == true){
        			silenceCounter = 0;
        			endPixelPosition = j - fieldMargin;
        			endTimePosition = 60*((double)endPixelPosition/widthRatio) + i*600;
        			speedTimesInSeconds.add(endTimePosition);
        			speedTimes.add(convertSecsToTimeString(endTimePosition));
        			isSilent = false;
        			noiseCounter++;
        		} else if(p != 0 && isSilent == false) {
        			silenceCounter = 0;
        			noiseCounter++;
        		}
        	}
        }
        
        //anything less than 1 pixel per second will not be considered for editing
        if(noiseCounter < videoDurationSeconds){
        	editVideo = false;
        }
        System.out.println(speedTimes);
        
        //??
        //int k;
        //tests if there are used videos that are shorter than a second
        //it will remove that "chunk" if there is because it will corrupt the video file --> it turns out this is false
        /*for(int i = 0; i < speedTimes.size(); i++){
        	if(i != 0 && speedTimesInSeconds.get(i) - speedTimesInSeconds.get(i - 1) < 1){
        		speedTimesInSeconds.remove(i);
        		speedTimesInSeconds.remove(i - 1);
        		speedTimes.remove(i);
        		speedTimes.remove(i - 1);
        	}
        }*/
	}
	
	//method that returns the line images back to the previous method
	public BufferedImage getDetectionImg(BufferedImage waveformImg, int waveformWidth, int heightMargin){
		BufferedImage detectionImg = waveformImg.getSubimage(0, heightMargin, waveformWidth, 1);
		return detectionImg;
	}
	
	//method that uses ffmpeg to make the waveform images
	public void ffmpegWaveForm(String startTime, String endTime, String videoPath, int widthRatio, int height, double videoDurationSeconds, String outputPath){
		
		//height of the waveform is 500 by default
		String waveform[] = {ffmpeg, "-ss", startTime, "-to", endTime, "-i", videoPath, "-filter_complex", 
				"aformat=channel_layouts=mono,showwavespic=s=" + (int)(((double)widthRatio/60)*videoDurationSeconds) + 
				"x" + height, "-frames:v", "1", "-y",
               outputPath};
        try {
			Process getWaveform = Runtime.getRuntime().exec(waveform);
			BufferedReader in = new BufferedReader(new InputStreamReader(getWaveform.getErrorStream()));
			while((line = in.readLine()) != null){
				//System.out.println(line);
			}
			try {
			  	getWaveform.waitFor();
			  	System.out.println("Waveform retrieved!");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        //increments the progress bar
        Executive.getFrame().setProgress(Executive.getFrame().getProgress() + 1);
        Executive.getFrame().getProgressBar().setValue(Executive.getFrame().getProgress());
        //System.out.println(Executive.getFrame().getProgress() + "/" + Executive.maxLoadBar);
	}
	
	//method that takes the silent parts of the video and outputs speeded versions of it based on the speed
	public void loadSpeededSilence(double speed){
		//the size of the array lists are speedTimes.size()/2
		ArrayList<String> startTimes = new ArrayList<String>();
		ArrayList<String> endTimes = new ArrayList<String>();
		String[] segmentVideoPath = new String[speedTimes.size()/2];
		
		for(int i = 0; i < speedTimes.size()/2; i++){
			startTimes.add(null);
			startTimes.set(i, speedTimes.get(2*i));
		}
		
		for(int i = 0; i < speedTimes.size()/2; i++){
			endTimes.add(null);
			endTimes.set(i, speedTimes.get(2*i + 1));
		}
		
		//detects for possible videos that are 0 seconds --> ffmpeg can't use it
		//this will be used later when deleting videos to concatenate
		for(int i = 0; i < startTimes.size(); i++){
        	if(startTimes.get(i).equals(endTimes.get(i)) && !videoContainer.equals(".mp4")){
        		removeTimeStamps.add("file spedsilence" + i + videoContainer);
        	} else if (startTimes.get(i).equals(endTimes.get(i)) && videoContainer.equals(".mp4")){
        		removeTimeStamps.add("file spedsilence" + i + ".MTS");
        	}
        }
		
		for(int i = 0; i < speedTimes.size()/2; i++){
			
			//creates the silent parts of the video
			splitAndSaveVideos(startTimes, endTimes, segmentVideoPath, false, "silence", videoInputStringPath, i, videoContainer);
			//increments the progress bar after creating the silent video
			Executive.getFrame().setProgress(Executive.getFrame().getProgress() + 1);
            Executive.getFrame().getProgressBar().setValue(Executive.getFrame().getProgress());
            //System.out.println(Executive.getFrame().getProgress() + "/" + Executive.maxLoadBar);
            
            String[] spedSegmentPath = new String[speedTimes.size()/2];
            
            spedSegmentPath[i] = projectStringPath + "Runtime Files/Video Segments/spedsilence" + i + videoContainer;
            
            //atempo is used 6 times for a maximum of 64x speed
            String speedSilentVideoCommand[] = {ffmpeg, "-i", segmentVideoPath[i], "-filter_complex",
            		"[0:v]setpts="+ 1/speed +"*PTS[v];[0:a]atempo=" + Math.pow(speed, 1.0/6) + ",atempo=" 
            		+ Math.pow(speed, 1.0/6) + ",atempo=" + Math.pow(speed, 1.0/6) + ",atempo=" + Math.pow(speed, 1.0/6) 
            		+ ",atempo=" + Math.pow(speed, 1.0/6) + ",atempo=" + Math.pow(speed, 1.0/6) + "[a]", 
            		"-map", "[v]", "-map", "[a]", spedSegmentPath[i]};
            try {
				Process speedUp = Runtime.getRuntime().exec(speedSilentVideoCommand);
				BufferedReader in = new BufferedReader(new InputStreamReader(speedUp.getErrorStream()));
				while((line = in.readLine()) != null){
					//System.out.println(line);
				}
				try {
					speedUp.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				in.close();
				System.out.println("Processed speed up of " + i);
			} catch (IOException e) {
				e.printStackTrace();
			}
            
            Executive.getFrame().setProgress(Executive.getFrame().getProgress() + 1);
            Executive.getFrame().getProgressBar().setValue(Executive.getFrame().getProgress());
            //System.out.println(Executive.getFrame().getProgress() + "/" + Executive.maxLoadBar);
		}
		
		//only converts to MTS if we are dealing with an mp4 file
		if(videoContainer.equals(".mp4")){
			for(int i = 0; i < speedTimes.size()/2; i++){
				String[] convertToMTS = {ffmpeg, "-i", "\"" + projectStringPath + "Runtime Files/Video Segments/spedsilence" + i + ".mp4" + "\"", "-q", "0", 
						"\"" + projectStringPath + "Runtime Files/Video Segments/spedsilence" + i + ".MTS" + "\""};
				convertToMTS(convertToMTS, "spedsilence", i, false);
			}
			//free up space in the directory
			deleteVideos(projectStringPath + "Runtime Files/Video Segments/", speedTimes.size()/2, "spedsilence", videoContainer);
		} else {
			//we shouldn't forget to update the loading bar for when we don't convert 
			//the line below isn't in a for loop so we add speedTimes.size()/2
        	Executive.getFrame().setProgress(Executive.getFrame().getProgress() + speedTimes.size()/2);
            Executive.getFrame().getProgressBar().setValue(Executive.getFrame().getProgress());
            //System.out.println(Executive.getFrame().getProgress() + "/" + Executive.maxLoadBar);
		}
		//free up space in the directory
		deleteVideos(projectStringPath + "Runtime Files/Video Segments/", speedTimes.size()/2, "silence", videoContainer);
	}
	
	public void loadUsedVideo(String videoLength, boolean isSpeeded){
		
		speedTimes.add("");
		speedTimes.add("");
		
		//size is speedTimes.size()/2
		ArrayList<String> startTimes = new ArrayList<String>();
		ArrayList<String> endTimes = new ArrayList<String>();
		
		String[] segmentVideoPath = new String[speedTimes.size()/2];
		
		for(int i = speedTimes.size() - 1; i > 0; i--){
			speedTimes.set(i, speedTimes.get(i - 1));
		}
		speedTimes.set(0, "00:00:00");
		speedTimes.set(speedTimes.size() - 1, videoLength);
		
		for(int i = 0; i < speedTimes.size()/2; i++){
			startTimes.add(null);
			startTimes.set(i, speedTimes.get(2*i));
		}
		
		for(int i = 0; i < speedTimes.size()/2; i++){
			endTimes.add(null);
			endTimes.set(i, speedTimes.get(2*i + 1));
		}
		
		//System.out.println(Arrays.toString(startTimes.toArray()));
		//System.out.println(Arrays.toString(endTimes.toArray()));
		
		//detects for possible videos that are 0 seconds --> ffmpeg can't use it
		//this will be used later when deleting videos to concatenate
		for(int i = 0; i < startTimes.size(); i++){
        	if(startTimes.get(i).equals(endTimes.get(i)) && !videoContainer.equals(".mp4")){
        		removeTimeStamps.add("file used" + i + videoContainer);
        	} else if (startTimes.get(i).equals(endTimes.get(i)) && videoContainer.equals(".mp4")){
        		removeTimeStamps.add("file used" + i + ".MTS");
        	}
        }
		
		//System.out.println(Arrays.toString(removeTimeStamps.toArray()));
		
		//creates used i videos and converts them to MTS if it is an mp4
		for(int i = 0; i < speedTimes.size()/2; i++){
			splitAndSaveVideos(startTimes, endTimes, segmentVideoPath, false, "used", videoInputStringPath, i, videoContainer);
			Executive.getFrame().setProgress(Executive.getFrame().getProgress() + 1);
            Executive.getFrame().getProgressBar().setValue(Executive.getFrame().getProgress());
            //System.out.println(Executive.getFrame().getProgress() + "/" + Executive.maxLoadBar);
            //only converts to MTS if the video container is mp4
            if(videoContainer.equals(".mp4")){
            	String[] convertToMTS = {ffmpeg, "-i", "\"" + projectStringPath + "Runtime Files/Video Segments/used" + i + ".mp4" + "\"", "-max_muxing_queue_size", "1024", "-q", "0", 
    					"\"" + projectStringPath + "Runtime Files/Video Segments/used" + i + ".MTS" + "\""};
    			convertToMTS(convertToMTS, "used", i, false);
            } else {
            	//we shouldn't forget to update the loading bar for when we don't convert
            	//add only 1 because the line below is in a for loop as the above
            	Executive.getFrame().setProgress(Executive.getFrame().getProgress() + 1);
                Executive.getFrame().getProgressBar().setValue(Executive.getFrame().getProgress());
                //System.out.println(Executive.getFrame().getProgress() + "/" + Executive.maxLoadBar);
            }
		}
	}
	
	//this method might have a bug with the loading bar (ie the loading bar shouldn't reset?)
	public void loadFinalCuts(boolean isNone){
		ArrayList<String> startTimesListString = new ArrayList<>();
		ArrayList<String> endTimesListString = new ArrayList<>();
		List<Double> startTimesList = new ArrayList<>();
		List<Double> endTimesList = new ArrayList<>();
		String[] segmentVideoPaths;
		
		String editedVideoPath;
		
		//the video wouldn't be edited if the "none" silence option is selected so we have to pull the path directly from the input
		if(isNone){
			editedVideoPath = videoInputStringPath;
		} else {
			editedVideoPath = projectStringPath + "Runtime Files/Video Segments/edited.mp4";
		}
		
		//detects black from using FFmpeg and gets start and end times if there are
		try {
			String getTimesCommand = "ffmpeg -i " + "\"" + editedVideoPath + "\"" + " -vf blackdetect=d=0.1:pix_th=.1 -f rawvideo -y NUL";
			Process getTimeInSeconds = Runtime.getRuntime().exec(getTimesCommand);
			BufferedReader in = new BufferedReader(new InputStreamReader(getTimeInSeconds.getErrorStream()));
			while((line = in.readLine()) != null){
				//System.out.println(line);
				if(line.startsWith("[")){
					//finds decimals in line which are the start/end times and the duration
					Pattern decimals = Pattern.compile("(\\d+(?:\\.\\d+))");
					Matcher m = decimals.matcher(line);
					//counter counts up for each double found in each single line
					//we only want the start and end times so we take 1 and 2 
					int counter = 0;
					while(m.find()) {
						//note that we want the used portions
						//the -1 and +1 at the ends are so that we can remove the parts where we cover the camera
						switch(counter){
							case 0:
								endTimesListString.add(convertSecsToTimeString(Double.parseDouble(m.group(1)) - 1));
								endTimesList.add(Double.parseDouble(m.group(1)) - 1);
								break;
							case 1:
								startTimesListString.add(convertSecsToTimeString(Double.parseDouble(m.group(1)) + 1));
								startTimesList.add(Double.parseDouble(m.group(1)) + 1);
								break;
						}
					    counter++;
					}
				}
			}
			try {
				getTimeInSeconds.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			in.close();
			System.out.println("Scan for black frames is complete!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//this is still part of the initial processing phase
		//we set the max for the "none" silence option because this method is the only processing done and we require the number of cuts
		if(isNone){
			Executive.getFrame().getProgressBar().setMaximum(startTimesListString.size() + 2);
			Executive.getFrame().getProgressBar().setIndeterminate(false);
		}
		//increments the progress bar
		Executive.getFrame().setProgress(Executive.getFrame().getProgress() + 1);
        Executive.getFrame().getProgressBar().setValue(Executive.getFrame().getProgress());
        //System.out.println(Executive.getFrame().getProgress() + "/" + Executive.maxLoadBar);
		
        //there are cuts indicated by the user if there are start times for the black frames
		if(!startTimesListString.isEmpty()){
			//this boolean allows the user to enter the cut selection menu for further editing
			//this is why there isn't an end card method here unlike the other case
			finalConcatNeeded = true;
			
			//same sort of way of increasing the array list and adding times to get the used portions
			startTimesListString.add("");
			startTimesList.add(0.0);
			for(int i = startTimesListString.size() - 1; i > 0; i--){
				startTimesListString.set(i, startTimesListString.get(i - 1));
				startTimesList.set(i, startTimesList.get(i - 1));
			}
			startTimesListString.set(0, "00:00:00");
			startTimesList.set(0, 0.0);
			endTimesListString.add(convertSecsToTimeString(getVideoLength(new File(projectStringPath + "Runtime Files/inputaudio.wav"))));
			endTimesList.add(getVideoLength(new File(projectStringPath + "Runtime Files/inputaudio.wav")));
			
			//this is to make sure that cuts (between black frames) that are less than a second don't make it into the final edit
			for(int i = 0; i < startTimesList.size(); i++){
				//in this case, just removing elements of startTimesListString is fine because startTimesList won't be used again
				if(startTimesList.get(i) > endTimesList.get(i) || endTimesList.get(i) - startTimesList.get(i) < 1){
					startTimesListString.remove(i);
					endTimesListString.remove(i);
				}
			}
			
			//start timees array lists should start with startTimesListString.size() elements
			//end times array list should start with endTimesListString.size() elements
			ArrayList<String> startTimes = new ArrayList<String>();
			ArrayList<String> endTimes = new ArrayList<String>();
			
			for(int i = 0; i < startTimesListString.size(); i++){
				startTimes.add(null);
				endTimes.add(null);
				startTimes.set(i, startTimesListString.get(i));
				endTimes.set(i, endTimesListString.get(i));
			}
			
			//a container array to put in the video paths for which the videos will be saved to
			segmentVideoPaths = new String[2*startTimesListString.size()];
			
			for(int i = 0; i < startTimesListString.size(); i++){
				splitAndSaveVideos(startTimes, endTimes, segmentVideoPaths, true, "cut_", editedVideoPath, i, ".mp4");
				if(isNone){
					//increments the progress bar if the "none" silence option is chosen
					//this is the only processing that goes into the "none" option so it should at least show progress
					Executive.getFrame().setProgress(Executive.getFrame().getProgress() + 1);
		            Executive.getFrame().getProgressBar().setValue(Executive.getFrame().getProgress());
		            //System.out.println(Executive.getFrame().getProgress() + "/" + Executive.maxLoadBar);
				}
			}
			System.out.println("Video has cuts!");
		} else {
			//the video doesn't need to be split if the video doesn't have any cuts
			finalConcatNeeded = false;
			System.out.println("Video does not have cuts!");
			//if there is an end card video and it doesn't have any cuts
			if(!endCardStringPath.equals("")){
				//adds an end card video to the silence edited video and turns it into a final MTS video
				addEndCard("edited.MTS", "final.MTS");
				
				//the video is then converted back into mp4 and stored in the output directory referred to by the user
				String convertToMP4;
				convertToMP4 = "ffmpeg -i \"" + projectStringPath + "Runtime Files/Video Segments/final.MTS" + "\"" + 
						" -max_muxing_queue_size 1024 \"" + videoOutputStringPath + "/final.mp4" + "\"";
				
				try {
					Process convertVideo = Runtime.getRuntime().exec(convertToMP4);
					BufferedReader in = new BufferedReader(new InputStreamReader(convertVideo.getErrorStream()));
					while((line = in.readLine()) != null){
						//System.out.println(line);
					}
					try {
						convertVideo.waitFor();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					in.close();
					System.out.println("Final video conversion processed!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			//if there isn't an end card video and it doesn't have any cuts
			} else {
				//simply copy and paste the video into the user's output directory
				try {
					if(isNone){
						Files.copy(new File(videoInputStringPath).toPath(), 
								new File(videoOutputStringPath + "/final.mp4").toPath(), StandardCopyOption.REPLACE_EXISTING);
					} else {
						Files.copy(new File(projectStringPath + "Runtime Files/Video Segments/edited.mp4").toPath(), 
								new File(videoOutputStringPath + "/final.mp4").toPath(), StandardCopyOption.REPLACE_EXISTING);
					}
				} catch (IOException e) {
					e.getStackTrace();
				}
			}
		}
		//increments the value of the progress bar
		Executive.getFrame().setProgress(Executive.getFrame().getProgress() + 1);
        Executive.getFrame().getProgressBar().setValue(Executive.getFrame().getProgress());
        //System.out.println(Executive.getFrame().getProgress() + "/" + Executive.maxLoadBar);
	}
	
	//method that gets all the start/end times and outputs a series of videos based on those times into the project video segments directory
	//the ffmpeg command used below works with mp4, MOV, and MTS
	public void splitAndSaveVideos(ArrayList<String> startTimes, ArrayList<String> endTimes, String segmentVideoPaths[], boolean finalEdit, String name, String videoInputStringPath, int i, String container){
			//if it's the final edit (the final used cuts) then we want to output the videos to the output directory as well
			if(finalEdit){
				segmentVideoPaths[i] = videoOutputStringPath + "/" + name + i + container;
			} else {
				segmentVideoPaths[i] = projectStringPath + "Runtime Files/Video Segments/" + name + i + container;
			}
			//code below will copy all the silences
			//-strict -2 is used to encode the videos after splitting them
			String createSilentShotsCommand[] = {ffmpeg, "-i", videoInputStringPath, "-ss", startTimes.get(i), "-strict", "-2",
					"-to", endTimes.get(i), segmentVideoPaths[i]};
			try {
				Process createSilentShots = Runtime.getRuntime().exec(createSilentShotsCommand);
				BufferedReader in = new BufferedReader(new InputStreamReader(createSilentShots.getErrorStream()));
				while((line = in.readLine()) != null){
					//System.out.println(line);
				}
				System.out.println("Creation of " + name + i + " processed!");
				try {
					createSilentShots.waitFor();
				} catch (Throwable t) {
					t.printStackTrace();
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	//concatenates the videos back to an mp4 after the silence editing is complete
	//concatenation seems to work perfectly fine with MOV
	public void concatenateMidVideos(boolean withCuts){
		//delete all the mp4 videos that won't be used later on
		deleteVideos(projectStringPath + "Runtime Files/Video Segments/", speedTimes.size()/2, "used", ".mp4");
		
		//a txt file is needed for the concatenation process
		File file = new File(projectStringPath + "Runtime Files/Video Segments/videolist.txt");
		PrintWriter writer = null;
		
		//writes video files into videolist.txt for concat usage
		//.MTS container is used because mp4 creates a Non-Monotonous DTS error when concatenating
		//This may have some bugs because this is a workaround as many people have said on StackExchange
		try {
			writer = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			writer = null;
			e.printStackTrace();
		}
		//alternates between used and spedsilence if the speed silence option is selected
		//first if checks if the video container is an mp4
		if(videoContainer.equals(".mp4")){
			if(withCuts == false){
				for(int i = 0; i < (speedTimes.size() - 2)/2; i++){
					writer.println("file " + "used" + i + ".MTS");
					writer.println("file " + "spedsilence" + i + ".MTS");
				} writer.println("file " + "used" + (speedTimes.size() - 2)/2 + ".MTS");
				writer.close();
				//removes all the used videos with less than a second of footage
				for(int i = removeTimeStamps.size() - 1; i > -1; i--){
					deleteFromTextFile(file, removeTimeStamps.get(i));
				}
			//only used is used if the cut silence option is selected
			} else {
				for(int i = 0; i < speedTimes.size()/2; i++){
					writer.println("file " + "used" + i + ".MTS");
				}
				writer.close();
				//removes all the used videos with less than a second of footage
				for(int i = removeTimeStamps.size() - 1; i > -1; i--){
					deleteFromTextFile(file, removeTimeStamps.get(i));
				}
			}
		} else {
			if(withCuts == false){
				for(int i = 0; i < (speedTimes.size() - 2)/2; i++){
					writer.println("file " + "used" + i + videoContainer);
					writer.println("file " + "spedsilence" + i + videoContainer);
				} writer.println("file " + "used" + (speedTimes.size() - 2)/2 + videoContainer);
				writer.close();
				//removes all the used videos with less than a second of footage
				for(int i = removeTimeStamps.size() - 1; i > -1; i--){
					deleteFromTextFile(file, removeTimeStamps.get(i));
				}
			//only used is used if the cut silence option is selected
			} else {
				for(int i = 0; i < speedTimes.size()/2; i++){
					writer.println("file " + "used" + i + videoContainer);
				}
				writer.close();
				//removes all the used videos with less than a second of footage
				for(int i = removeTimeStamps.size() - 1; i > -1; i--){
					deleteFromTextFile(file, removeTimeStamps.get(i));
				}
			}
		}
		
		//OMER GERD THE WHOLE REASON WHY I WASN'T GETTING AN OUTPUT WAS BECAUSE OF NOMENCLATURE OF VIDEOS!!!! LOWER CAPS NO SPACE!!!
		//ALSO LIST MUST BE DIRECT!!! DON'T PUT QUOTATIONS OR ENTIRE FILE PATH!
		//ALSO NEED -safe 0 AND STRING PATH FOR ONLY THE TEXT FILE!
		String[] concat = {ffmpeg, "-f", "concat", "-safe", "0", "-i", "\"" + projectStringPath + "Runtime Files/Video Segments/" + "videolist.txt" + 
		"\"", "-c", "copy", "\"" + projectStringPath + "Runtime Files/Video Segments/" + "edited.MTS" + "\""};
		
		try {
			Process concatVideos = Runtime.getRuntime().exec(concat);
			BufferedReader in = new BufferedReader(new InputStreamReader(concatVideos.getErrorStream()));
			while((line = in.readLine()) != null){
				//System.out.println(line);
			}
			try {
				concatVideos.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			in.close();
			System.out.println("Processed midvideo concatenation!");
			//there is no need for the used and spedsilence videos once the videos are concatenated
			//the MTS files are only created excessively if the video container was mp4
			if(videoContainer.equals(".mp4")){
				deleteVideos(projectStringPath + "Runtime Files/Video Segments/", speedTimes.size()/2, "used", ".MTS");
				deleteVideos(projectStringPath + "Runtime Files/Video Segments/", speedTimes.size()/2, "spedsilence", ".MTS");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//sets video codec to libx264 and audio codec to aac
		codecToUsual("edited", ".MTS", "midvideos");
		
		//increments the value of the progress bar after the concatenation
		Executive.getFrame().setProgress(Executive.getFrame().getProgress() + 5);
        Executive.getFrame().getProgressBar().setValue(Executive.getFrame().getProgress());
        //System.out.println(Executive.getFrame().getProgress() + "/" + Executive.maxLoadBar);
		
		//final conversion probably takes the longest and takes the most computing power
		String[] convertToMP4 = {ffmpeg, "-i", "\"" + projectStringPath + "Runtime Files/Video Segments/edited.MTS" + "\"", "-max_muxing_queue_size", "1024",
				"\"" + projectStringPath + "Runtime Files/Video Segments/edited.mp4" + "\""};
		try {
			Process convertVideo = Runtime.getRuntime().exec(convertToMP4);
			BufferedReader in = new BufferedReader(new InputStreamReader(convertVideo.getErrorStream()));
			while((line = in.readLine()) != null){
			}
			try {
				convertVideo.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			in.close();
			System.out.println("Midvideo conversions have been processed!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//increments the value of the progress bar after the conversion
		Executive.getFrame().setProgress(Executive.getFrame().getProgress() + 10);
        Executive.getFrame().getProgressBar().setValue(Executive.getFrame().getProgress());
        //System.out.println(Executive.getFrame().getProgress() + "/" + Executive.maxLoadBar);
        
        //delete the videos that are unnecessary for further processing
        if(videoContainer.equals(".mp4")){
        	deleteVideos(projectStringPath + "Runtime Files/Video Segments/", speedTimes.size()/2, "used", ".MTS");
        	deleteVideos(projectStringPath + "Runtime Files/Video Segments/", speedTimes.size()/2, "spedsilence", ".MTS");
        } else {
        	deleteVideos(projectStringPath + "Runtime Files/Video Segments/", speedTimes.size()/2, "used", videoContainer);
        	deleteVideos(projectStringPath + "Runtime Files/Video Segments/", speedTimes.size()/2, "spedsilence", videoContainer);
        }
	}
	
	//concatenates the final used cuts back together into an mp4 video
	public void concatenateFinalVideos(int numOfFiles){
		//a strange way of detecting if end card exists
		int endCardIdentifier = 0;
		if(!endCardStringPath.equals("")){
			endCardIdentifier = 1;
		}
		
		for(int i = 0; i < numOfFiles; i++){
			String[] convertToMTS = {ffmpeg, "-i", "\"" + videoOutputStringPath + "/usedcut_" + i + ".mp4" + "\"", "-q", "0", 
					"\"" + projectStringPath + "Runtime Files/Video Segments/usedcut_" + i + ".MTS" + "\""};
			convertToMTS(convertToMTS, "usedcut_", i, true);
		}
		
		File file = new File(projectStringPath + "Runtime Files/Video Segments/finalcutlist.txt");
		PrintWriter writer = null;
		
		//writes video files into videolist.txt for concat usage
		//.MTS container is used because mp4 creates a Non-Monotonous DTS error when concatenating
		//this may have some bugs because this is a workaround as many people have said on StackExchange
		try {
			writer = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			writer = null;
			e.printStackTrace();
		}
		for(int i = 0; i < numOfFiles; i++){
			writer.println("file " + "usedcut_" + i + ".MTS");
		}
		writer.close();
		
		//concatenates final used cuts together
		String[] concat = {ffmpeg, "-f", "concat", "-safe", "0", "-i", "\"" + projectStringPath + "Runtime Files/Video Segments/" + "finalcutlist.txt" + 
		"\"", "-c", "copy", "\"" + projectStringPath + "Runtime Files/Video Segments/" + "final0.MTS" + "\""};
		
		try {
			Process concatVideos = Runtime.getRuntime().exec(concat);
			BufferedReader in = new BufferedReader(new InputStreamReader(concatVideos.getErrorStream()));
			while((line = in.readLine()) != null){
				//System.out.println(line);
			}
			try {
				concatVideos.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			in.close();
			System.out.println("Processed final video concatenation!");
			deleteVideos(projectStringPath + "Runtime Files/Video Segments/", numOfFiles, "usedcut_", ".MTS");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//sets video codec to libx264 and audio codec to aac
		codecToUsual("final0", ".MTS", "final videos");
		
		//adds end card if there exists one
		if(!endCardStringPath.equals("")){
			addEndCard("final0.MTS", "final1.MTS");
		}
		
		//System.out.println(endCardIdentifier);
		
		//converts the fully edited video back to mp4 and stores it into the output directory
		String[] convertToMP4 = {ffmpeg, "-i", "\"" + projectStringPath + "Runtime Files/Video Segments/final" + endCardIdentifier + 
				".MTS" + "\"", "-max_muxing_queue_size", "1024", "\"" + videoOutputStringPath + "/final.mp4" + "\""};
		
		try {
			Process convertVideo = Runtime.getRuntime().exec(convertToMP4);
			BufferedReader in = new BufferedReader(new InputStreamReader(convertVideo.getErrorStream()));
			while((line = in.readLine()) != null){
				//System.out.println(line);
			}
			try {
				convertVideo.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			in.close();
			System.out.println("Processed final video conversion!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//delete the used video segments directory
		deleteDir(new File(projectStringPath + "Runtime Files/Video Segments"));
	}
	
	//method that adds an end card to the video if it is specified by the user
	public void addEndCard(String inputVideoName, String outputVideoName){
		//structure is similar to the concatenate methods
		File endCard = new File(endCardStringPath);
		try {
			Files.copy(endCard.toPath(),
			        (new File(videoOutputStringPath + "/" + endCard.getName())).toPath(),
			        StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//there was a bug with the final concatenation of the edited video and the endcard... 
		//it worked with mp4 but it didn't with MOV even though they were both converted to MTS
		//the reason was because the video and audio codecs were not the same which resulted in a buggy end file
		//so in the following conversion, we added those to ensure that it does
		String[] convertToMTS = {ffmpeg, "-i", "\"" + endCardStringPath + "\"", "-acodec", "aac", "-vcodec", "libx264", "-q", "0", 
				"\"" + projectStringPath + "Runtime Files/Video Segments/endcard.MTS" + "\""};
		convertToMTS(convertToMTS, "endcard", 0, true);
		
		PrintWriter writer = null;
		File endCardTxtFile = new File(projectStringPath + "Runtime Files/Video Segments/endlist.txt");
		try {
			writer = new PrintWriter(endCardTxtFile);
		} catch (FileNotFoundException e) {
			writer = null;
			e.printStackTrace();
		}
		writer.println("file " + inputVideoName);
		writer.println("file " + "endcard.MTS");
		writer.close();
		
		String[] endCardCommand = {ffmpeg, "-f", "concat", "-safe", "0", "-i", "\"" + projectStringPath + "Runtime Files/Video Segments/" + "endlist.txt" + 
				"\"", "-c", "copy", "\"" + projectStringPath + "Runtime Files/Video Segments/" + outputVideoName + "\""};
		
		try {
			Process addEndCard = Runtime.getRuntime().exec(endCardCommand);
			BufferedReader in = new BufferedReader(new InputStreamReader(addEndCard.getErrorStream()));
			while((line = in.readLine()) != null){
				//System.out.println(line);
			}
			try {
				addEndCard.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			in.close();
			System.out.println("Processed end card addition!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//method that converts a series of mp4 videos into MTS through FFmpeg
	public void convertToMTS(String[] convertToMTS, String name, int i, boolean isFinal){
		try {
			Process convertVideo = Runtime.getRuntime().exec(convertToMTS);
			BufferedReader in = new BufferedReader(new InputStreamReader(convertVideo.getErrorStream()));
			while((line = in.readLine()) != null){
				/*if(isFinal){
					System.out.println(line);
				}*/
				//System.out.println(line);
			}
			try {
				convertVideo.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			in.close();
			System.out.println("Processed " + name + i + " for video conversion!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(!isFinal){
			//the progress bar is incremented only when it is converting the non-final cuts
			Executive.getFrame().setProgress(Executive.getFrame().getProgress() + 1);
            Executive.getFrame().getProgressBar().setValue(Executive.getFrame().getProgress());
            //System.out.println(Executive.getFrame().getProgress() + "/" + Executive.maxLoadBar);
		}
	}
	
	public void codecToUsual(String inputName, String container, String processStage){
		String[] alignCodecCommand = {ffmpeg, "-i", "\"" + projectStringPath + "Runtime Files/Video Segments/" + inputName + container +
				"\"", "-acodec", "aac", "-vcodec", "libx264", "-q", "0", "\"" + projectStringPath + "Runtime Files/Video Segments/" + inputName + "_" + container + "\""};
		
		try {
			Process alignCodec = Runtime.getRuntime().exec(alignCodecCommand);
			BufferedReader in = new BufferedReader(new InputStreamReader(alignCodec.getErrorStream()));
			while((line = in.readLine()) != null){
				//System.out.println(line);
			}
			try {
				alignCodec.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			in.close();
			System.out.println("Codec of " + processStage + " have been processed!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//self replace for adjusted video and audio codec
		File deleteVideo = new File(projectStringPath + "Runtime Files/Video Segments/" + inputName + container);
		deleteVideo.delete();
		File editedVideo = new File(projectStringPath + "Runtime Files/Video Segments/" + inputName + "_" + container);
		editedVideo.renameTo(new File(projectStringPath + "Runtime Files/Video Segments/" + inputName + container));
	}
	
	/*public void deleteFromTextFile(File file, int startLineIndex, int numOfLines)
	{
		try
		{
			BufferedReader bufferReader = new BufferedReader(new FileReader(file));
			
			//string buffer to store contents of the file
			StringBuffer stringBuffer = new StringBuffer("");
			
			//keep track of the line number
			int lineNumber=1;
			String line;
 
			while((line = bufferReader.readLine()) != null)
			{
				//store each valid line in the string buffer
				if(lineNumber < startLineIndex || lineNumber >= startLineIndex + numOfLines){
					stringBuffer.append(line+"\n");
				}
				lineNumber++;
			}
			if(startLineIndex + numOfLines > lineNumber){
				System.out.println("End of file reached.");
			}
			bufferReader.close();
			
			FileWriter fileWriter = new FileWriter(file);
			//write entire string buffer into the file
			fileWriter.write(stringBuffer.toString());
			fileWriter.close();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}*/
	
	public void deleteFromTextFile(File file, String delete)
    {
        try
        {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                String input = "";
                while ((line = br.readLine()) != null) 
                {
                    //System.out.println(line);
                    if (line.contains(delete))
                    {
                        line = "";
                        //System.out.println("Line deleted.");
                    }
                    input += line + '\n';
                }
                br.close();
                FileWriter fileWriter = new FileWriter(file);
    			//write entire string buffer into the file
    			fileWriter.write(input);
    			fileWriter.close();
        }
        catch (Exception e)
        {
                System.out.println("Problem reading file.");
        }
    }
	
	//method that deletes a series of videos
	public void deleteVideos(String path, int numOfVids, String name, String container){
		File[] videosToDelete = new File[numOfVids];
		for(int i = 0; i < numOfVids; i++){
			videosToDelete[i] = new File(path + name + i + container);
			videosToDelete[i].delete();
		}
	}
	
	//method that deletes an entire directory
	public void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            deleteDir(f);
	        }
	    }
	    file.delete();
	}
	
	//method that converts time in seconds to time in a String with the format hh:mm:ss
	public String convertSecsToTimeString(double timeSeconds) {
		int discreteTime = (int) Math.floor(timeSeconds);
        int hours = discreteTime / 3600;
        int mins = (discreteTime % 3600) / 60;
        int secs = discreteTime % 60;
        String timeString = String.format("%02d:%02d:%02d", hours, mins, secs);
        return timeString;
    }
	
	//method that converts and creates a wav file based on an input video 
	public void convertAudio(){
		//outputs audio to project folder
		//don't forget to change output file name
		new File(projectStringPath + "Runtime Files").mkdirs();
		
		//-y makes sure it replaces the audio when user switches video input
		String convertToWav[] = {ffmpeg, "-y", "-i", videoInputStringPath, "-ac", "2", "-f", "wav", projectStringPath + "Runtime Files/inputaudio.wav"};
		try{
			Process convertAudio = Runtime.getRuntime().exec(convertToWav);
			BufferedReader in = new BufferedReader(new InputStreamReader(convertAudio.getErrorStream()));
			while((line = in.readLine()) != null){
				//System.out.println(line);
			}
			try {
			  	convertAudio.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			in.close();
			System.out.println("Audio successfully retrieved!");
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	//method that retrieves the audio from the input video if it's there
	public File retrieveAudio(){
		return new File(projectStringPath + "Runtime Files/inputaudio.wav");
	}
	
	//a bunch of getters and setters
	public double getVideoLength(File file){
		//a typical audioInputStream application 
		AudioInputStream audioInputStream;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(file);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			audioInputStream = null;
		} catch (IOException e) {
			e.printStackTrace();
			audioInputStream = null;
		}
		AudioFormat format  = audioInputStream.getFormat();
		long frames = audioInputStream.getFrameLength();
		double durationInSeconds = (frames + 0.0) / format.getFrameRate();
		
		return durationInSeconds;
	}
	
	public boolean getEditVideo(){
		return editVideo;
	}
	
	public void setEditVideo(boolean ev){
		editVideo = ev;
	}
	
	public boolean getFinalConcatBoolean(){
		return finalConcatNeeded;
	}
	
	public String getProjectStringPath(){
		return projectStringPath;
	}
	
	public String getVideoInputStringPath(){
		return videoInputStringPath;
	}
	
	public void setVideoInputStringPath(String inputString){
		videoInputStringPath = inputString;
	}
	
	public String getVideoOutputStringPath(){
		return videoOutputStringPath;
	}
	
	public void setVideoOutputStringPath(String outputString){
		videoOutputStringPath = outputString;
	}
	
	public void setEndCardStringPath(String endCard){
		endCardStringPath = endCard;
	}
	
	//a faster way of getting the video length once it is initially found using the above getVideoLength method
	public double getVideoLength(){
		return videoLength;
	}
	
	public void setVideoLength(double length){
		videoLength = length;
	}
	
	public String getVideoLengthString(){
		return videoLengthString;
	}
	
	public void setVideoLengthString(String videoString){
		videoLengthString = videoString;
	}
	
	public ArrayList<String> getSpeedTimes(){
		return speedTimes;
	}
	
	public List<Double> getSpeedTimesInSeconds(){
		return speedTimesInSeconds;
	}
	
	public String getVideoContainer(){
		return videoContainer;
	}
	
	public void setVideoContainer(String videoCont){
		videoContainer = videoCont;
	}
	
	public ArrayList<String> getRemoveTimeStamps(){
		return removeTimeStamps;
	}
}
