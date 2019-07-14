
import io.grpc.netty.shaded.io.netty.handler.codec.base64.Base64Decoder;

import javax.imageio.ImageIO;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;

public class DataHandler {

    int rpiServerSocketPort;
    int webServerSocketPort;
    ServerSocket webServerSocket;
    ServerSocket rpiServerSocket;
    Socket rpiSocket;
    Socket webSocket;
    DataOutputStream rpiOutputStream;
    DataOutputStream webServerOutputStream;
    DataInputStream rpiInputStream;
    OutputStream rpiOS;

    public DataHandler(int webServerSocketPort, int rpiServerSocketPort) {
        this.webServerSocketPort = webServerSocketPort;
        this.rpiServerSocketPort = rpiServerSocketPort;
        try {
            webServerSocket = new ServerSocket(webServerSocketPort);
            rpiServerSocket = new ServerSocket(rpiServerSocketPort);
            System.out.println("Opened up server sockets");
        } catch (IOException e) {
            System.err.println("Couldn't make the serverSocket");
            e.printStackTrace();
        }
        try {
            rpiSocket = rpiServerSocket.accept();
            System.out.println("Found RPi connection");
            webSocket = webServerSocket.accept();
            System.out.println("Got webserver connection");
            rpiOutputStream = new DataOutputStream(rpiSocket.getOutputStream());
            webServerOutputStream = new DataOutputStream(webSocket.getOutputStream());
            rpiInputStream = new DataInputStream(rpiSocket.getInputStream());
            rpiOS = rpiSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DataHandler(int rpiServerSocketPort) {
        try {
            rpiServerSocket = new ServerSocket(rpiServerSocketPort);
            System.out.println("Opened up serverSocket");
            rpiSocket = rpiServerSocket.accept();
            System.out.println("Got RPi socket");
            rpiOutputStream = new DataOutputStream(rpiSocket.getOutputStream());
            rpiInputStream = new DataInputStream(rpiSocket.getInputStream());
            rpiOS = rpiSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void receiveImage(File savePath) {
        BufferedImage recievedImage;
        try {
            recievedImage = ImageIO.read(rpiSocket.getInputStream());
            ImageIO.read(rpiSocket.getInputStream());
            ImageIO.read(rpiSocket.getInputStream());
            try {
                ImageIO.write(recievedImage, "jpg", savePath);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            System.out.println("Received Image from RPi");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Error receiving BufferedImage");
        }
    }

    public void sendTextWebServer(String text) {
        try {
            webServerOutputStream.writeUTF(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendTextRPi(String text) {
        try {
            rpiOutputStream.writeUTF(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DataOutputStream getRPiOutputStream() {
        return rpiOutputStream;
    }

    public DataInputStream getRPiInputStream() {
        return rpiInputStream;
    }

    public void sendImageWebServer(File file) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ImageIO.write(img, "jpg", webServerOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveStringRPI() throws Exception{
        String message = rpiInputStream.readUTF();
        System.out.println(message);
    }

    public OutputStream getRPiOS() {
        return this.rpiOS;
    }




}
