import java.io.File;
public class Main {

    public static void main(String[] args) {
        File file = new File("/home/aditya/tests/pictureTests/buffer.jpg");
        File mp3 = new File("/home/aditya/audio.mp3");
        DetectObjects objects = new DetectObjects(file);
//        DataHandler dataHandler = new DataHandler(55554);

        DataHandler dataHandler = new DataHandler(55555,55554);
        while (true) {
            dataHandler.receiveImage(file);
            String text = objects.detectLocalizedObjects();
            System.out.println(text);
            try {
                objects.textToSpeech(mp3, text, dataHandler.getRPiOS());
            } catch (Exception e) {
                objects.textToSpeech(mp3, "Nothing detected", dataHandler.getRPiOS());
            }
            dataHandler.sendTextWebServer(text);
            System.out.println("Sent text to Web Server");
            dataHandler.sendImageWebServer(file);
            System.out.println("Sent image to Web Server");
        }


    }
}
