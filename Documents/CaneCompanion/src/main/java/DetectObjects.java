import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.LocalizedObjectAnnotation;
import com.google.protobuf.ByteString;

import java.io.*;

import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DetectObjects {

    File filePath;

    public DetectObjects(File filePath) {
        this.filePath = filePath;
    }

    /**
     * Detects localized objects in the specified local image.
     * @throws Exception on errors while closing the client.
     * @throws IOException on Input/Output errors.
     */

    public String detectLocalizedObjects() {
        String objectName = null;
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ByteString imgBytes = null;
        try {
            imgBytes = ByteString.readFrom(new FileInputStream(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Image img = Image.newBuilder().setContent(imgBytes).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder()
                        .addFeatures(Feature.newBuilder().setType(Type.OBJECT_LOCALIZATION))
                        .setImage(img)
                        .build();
        requests.add(request);

        // Perform the request
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            // Display the results
            for (AnnotateImageResponse res : responses) {
                for (LocalizedObjectAnnotation entity : res.getLocalizedObjectAnnotationsList()) {
                    objectName = entity.getName();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return objectName;
    }

    public void textToSpeech(File mp3File, String text, OutputStream os) {


        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {

            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(text)
                    .build();

            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("en-US")
                    .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                    .build();

            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .build();

            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice,
                    audioConfig);

            ByteString audioContents = response.getAudioContent();

            try (OutputStream out = new FileOutputStream(mp3File)) {
                out.write(audioContents.toByteArray());
                System.out.println("Audio content written to file \"audio.mp3\"");
            } catch (IOException e) {

                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] buffer = new byte[10000];


        System.out.println("Sending image...");
        try {
            FileInputStream fis = new FileInputStream(mp3File);

            DataInputStream dis = new DataInputStream(fis);
            OutputStream dos = new DataOutputStream(os);

            while (true) {
                int count = dis.read(buffer);
                System.out.println(count + " bytes");
                if (count == -1) {
                    Thread.sleep(500);
                    dos.write(new byte[1], 0, 1);
                    break;
                } else {
                    dos.write(buffer, 0, count);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Sent MP3 to RPi");
    }

}
