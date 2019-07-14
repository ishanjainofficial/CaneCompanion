import com.hopding.jrpicam.*;
import com.pi4j.io.gpio.*;
import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;

public class Main {
    public static void main(String[] args) throws Exception {

        String ipAddress = "";


        GpioController gpioController = GpioFactory.getInstance();
        GpioPinDigitalInput sig = gpioController.provisionDigitalInputPin(RaspiPin.GPIO_08);

        RPiCamera rPiCamera = new RPiCamera().setTimeout(1);
        Thread.sleep(2000);

        Socket socket = new Socket(ipAddress, 55554);

        OutputStream socketOutputStream = socket.getOutputStream();
        InputStream socketInputStream = socket.getInputStream();
        DataInputStream socketDataInputStream = new DataInputStream(socketInputStream);

        System.out.println("Ready");

        final String mp3Path  = "Resources/Receive.mp3";
        final int mp3Size = 100000;

        boolean lastState = false;
        while (true) {
            boolean curState = !sig.isHigh();

            if (lastState != curState) {
                lastState = curState;

                if (curState) {
                    playNoise("TakingPicture");
                    System.out.println("Taking Picture");
                    BufferedImage image = rPiCamera.takeBufferedStill();
                    System.out.println("Took Picture");

                    System.out.println("Sending iamge");
                    ImageIO.write(image, "png", socketOutputStream);
                    System.out.println("Sent image");

                    byte[] buffer = new byte[mp3Size];

                    DataOutputStream fileDataOutputStream = new DataOutputStream(new FileOutputStream(mp3Path));

                    System.out.println("Receiving mp3");
                    while (true) {
                        int count = socketDataInputStream.read(buffer);
                        System.out.println(count + "bytes");
                        if (count <= 1) { break; }
                        fileDataOutputStream.write(buffer, 0, count);
                    }

                    fileDataOutputStream.close();

                    System.out.println("Received mp3");

                    System.out.println("Saying received");
                    playNoise("Receive");
                    System.out.println("Said received");
                }
            }
        }
    }

    static void playNoise(String name) throws Exception {
        String cmd = "omxplayer -o local Resources/" + name + ".mp3";
        Runtime.getRuntime().exec(cmd).waitFor();
    }
}
