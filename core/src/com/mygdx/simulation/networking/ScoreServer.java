package com.mygdx.simulation.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Digilogue on 27/11/2016.
 */
public class ScoreServer {

    private static final int PORT = 1234;

    public ScoreServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            Socket clientSock;
            BufferedReader in;
            PrintWriter out;

            while (true) {
                System.out.println("Waiting for a client...");
                clientSock = serverSocket.accept();
                System.out.println("Client connection from " + clientSock.getInetAddress().getHostAddress());

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
