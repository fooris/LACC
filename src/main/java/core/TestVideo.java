package core;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.List;

public class TestVideo {


    public static void main(String args[]) throws UnsupportedAudioFileException {

        String audioPath = null;
        try {
            audioPath = FFMPEG.convertToAudioAndGetPath(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        double threshold = 0.1;
        if (args.length > 1) {
            threshold = Double.parseDouble(args[1]);
        }

        double minCutLength = 0.5;
        if (args.length > 2) {
            minCutLength = Double.parseDouble(args[2]);
        }

        double[] samples = AudioIO.load(audioPath);

        SilenceDetector sl = new SilenceDetector(samples, threshold, minCutLength);
        sl.detectNotSilence();

        List<Interval> paddedCutSequence = AudioTools.pad(sl.getCutSequence(), 0.1);
        CutStatistics.report(paddedCutSequence, samples.length);

        double[] smoothedSamples = AudioTools.smoothCuts(paddedCutSequence, samples, 0.01);
        double[] newSamples = AudioTools.cut(paddedCutSequence, smoothedSamples);

        try {
            FFMPEG.cut(args[0], "/tmp/out.mp4", sl.getCutSequence(), true, 0.0f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
