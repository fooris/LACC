import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class SoundLoader {


    public static final int SAMPLE_RATE = 16000;

    public static void main(String[] args) throws UnsupportedAudioFileException {
        double[] sound = read("./temp/audio.wav");
        System.out.println(sound.length);
        List<Interval> fupelList = SilenceDetector.detectSilence(sound, 0.1, SAMPLE_RATE / 2);
        double secondsSaved = fupelList.stream()
                .map(i -> i.getTimeEnd() - i.getTimeStart())
                .reduce(Double::sum)
                .orElse(0.0);

        System.out.println("Number of cuts: " + fupelList.size());
        System.out.printf("Seconds saved: %.2f\n", secondsSaved);
        System.out.printf("Percent saved: %.2f\n", 100.0*secondsSaved/(sound.length/SoundLoader.SAMPLE_RATE));

    }

    private static final double MAX_16_BIT = Short.MAX_VALUE;     // 32,767

    /**
     * Reads audio samples from a file (in .wav or .au format) and returns
     * them as a double array with values between -1.0 and +1.0.
     *
     * @param filename the name of the audio file
     * @return the array of samples
     */
    public static double[] read(String filename) throws UnsupportedAudioFileException {
        byte[] data = readByte(filename);
        int n = data.length;
        double[] d = new double[n / 2];
        for (int i = 0; i < n / 2; i++) {
            d[i] =  ((short) (((data[2 * i + 1] & 0xFF) << 8) + (data[2 * i] & 0xFF))) / ((double) MAX_16_BIT);
        }
        return d;
    }

    // return data as a byte array
    private static byte[] readByte(String filename) {
        byte[] data;
        AudioInputStream ais;
        try {
            // try to read from file
            File file = new File(filename);
            if (file.exists()) {
                ais = AudioSystem.getAudioInputStream(file);
                int bytesToRead = ais.available();
                data = new byte[bytesToRead];
                int bytesRead = ais.read(data);
                if (bytesToRead != bytesRead)
                    throw new IllegalStateException("read only " + bytesRead + " of " + bytesToRead + " bytes");
            }

            // try to read from URL
            else {
                URL url = new URL(filename);
                ais = AudioSystem.getAudioInputStream(url);
                int bytesToRead = ais.available();
                data = new byte[bytesToRead];
                int bytesRead = ais.read(data);
                if (bytesToRead != bytesRead)
                    throw new IllegalStateException("read only " + bytesRead + " of " + bytesToRead + " bytes");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("could not read '" + filename + "'", e);
        } catch (UnsupportedAudioFileException e) {
            throw new IllegalArgumentException("unsupported audio format: '" + filename + "'", e);
        }

        return data;
    }


}
