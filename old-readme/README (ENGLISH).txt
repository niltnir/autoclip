Autoclip Installation Instructions**********************************************************

The program is currently for Windows only.

First, please install the latest version of Java.

Java Download:
https://www.java.com/en/download/

Once the installer is downloaded, open it and proceed as instructed.

Next, please download the latest version of FFmpeg.

FFmpeg Download:
https://ffmpeg.zeranoe.com/builds/

Set the version to be the one in the form X.X.X (currently 4.2.1).
Set the architechture to be what your Windows processor runs.
Set linking to static.

Then click "Download Build".

Once the build finishes downloading, unzip the zipped file and simply rename it "ffmpeg"
(without the quotations).

Now, move the ffmpeg folder into the C drive and copy (Ctrl C) the path below:

C:\ffmpeg\bin

Open File Explorer and right-click "This PC". Select properties and you should find 
"Advanced System Settings" in the left column. Open it and click the button named 
"Environment Variables...". If you can't find it, make sure you're in the "Advanced" tab.

Now inside "System variables", find "Path" and double-click on it. Click "New" and paste
the above path. Click OK for all the windows and Autoclip is now ready for use.


How To Use Autoclip*************************************************************************

Autoclip has mainly two functions.

1) Detects silences in video footage and either speeds up or cuts out the silent portions.
2) Detects black frames and splits the footage everytime it encounters those frames.

The main menu in Autoclip allows you to customize the type of edits you want.
 
"Silence Options" is what the edit will do. "CUT" will cut out the silences, "SPEED UP" 
will speed up the silent portions by the "Speed Factor" on the right, and "NONE" will not 
edit the silences in the video. The last option may be useful in situations where you may 
only want to cut up footage where the black frames are.

Currently, the input files that can be selected for the edit are mp4, MOV, and MTS videos.
More video file formats may be added in future updates upon user requests.

Now, on the right of the menu are four parameters that can be manually adjusted.

"Volume Sensitivity" is a unitless parameter that takes in a positive real number 
between 0 and 24 exclusive. The value depends on the environment the video is taken in and 
the overall volume. As a starting point, I recommend starting with values such as 0.5 and 
moving it up or down accordingly. The higher the volume sensitivity, the more it will cut 
out, the lower, the less.

"Silence Margin" is the minimum amount of time the silence needs to prevail before an edit 
is made for that portion. It takes in a positive integer in terms of seconds.

"Field Margin" is a parameter in seconds that "cushions" the edit around transitions 
between the audible and silent portions (and vice versa). For example, if there is footage 
of people talking, then the ends of quiet phrases may be cut off. To prevent that, the 
program will wait for the number of seconds specified by this parameter before starting 
the edit. As with the "Silence Margin", it takes in a positive integer, but it must be at 
most half the value of the "Silence Margin".

"Speed Factor" is the factor by which the silent portions get shortened by. It takes in a
positve real number between 1 and 64. (Yes... this maybe useful for timelapses as well)

Finally, the "Add Endcard" button will allow you to select an mp4 video that you can attach
at the end of the edited video.

A Few Notes
- Before editing a video, it is recommended that you create a folder for the final video.
  This will keep the directory with the input video organized.
- The final edited video will be named "final", so please refrain from having a video
  in the output directory with that name. (To keep things completely safe, I recommend 
  just creating a new folder for every edit.)
- Choosing the "NONE" option and setting the speed factor to some value will not speed up 
  the video. I may make that happen in a future update though...
- If the "Volume Sensitivity" is set too high, the video may not start editing, so try 
  setting it to a lower value.
- Processing time depends on the computer specs and the video format type. MTS, by 
  experiment, seems to take the least amount of time.

If you have any questions or concerns about the program, please send me a DM on Twitter 
@RKLR096 for further information. It would be greatly appreciated if you could also send 
me any feedback, bug information, update ideas, etc. I will try to get to you ASAP but 
please note that I may take several days to do so. This is a completely free program so 
feel free to share it with others! Happy video editing!

Also, a math Youtube channel called 式変形チャンネル has made a great tutorial video on how to
use Autoclip so please check it out! Unfortunately, it is in Japanese, but I'm planning to 
add English translations to it soon! Hopefully, by the time you read this, the translation 
is already out!
https://www.youtube.com/watch?v=AIcyNUiSj-4

RoKuluro