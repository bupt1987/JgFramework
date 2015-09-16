package com.zhaidaosi.game.jgframework.common;

import com.zhaidaosi.game.jgframework.common.excption.MessageException;
import com.zhaidaosi.game.jgframework.message.InMessage;
import com.zhaidaosi.game.jgframework.message.OutMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class BaseSocket {

    private String host;
    private int port;
    private Socket socket;

    private static HashMap<String, BaseSocket> socketMap = new HashMap<String, BaseSocket>();

    private BaseSocket(String host, int port) throws UnknownHostException, IOException {
        this.socket = new Socket(host, port);
        this.host = host;
        this.port = port;
    }

    public static BaseSocket getNewInstance(String host, int port)
            throws UnknownHostException, IOException {
        return new BaseSocket(host, port);
    }

    public static BaseSocket getInstance(String host, int port)
            throws UnknownHostException, IOException {
        BaseSocket mySocket = socketMap.get(host + ":" + port);
        if (mySocket == null || mySocket.socket.isClosed()) {
            mySocket = new BaseSocket(host, port);
            socketMap.put(host + ":" + port, mySocket);
        }
        return mySocket;
    }

    public void reconnect() throws IOException {
        if (!socket.isClosed()) {
            socket.close();
        }
        socket = new Socket(host, port);
        socketMap.put(host + ":" + port, this);
    }

    public OutMessage request(InMessage msg) throws IOException, MessageException {
        send(msg);
        OutMessage om = receive();
        if (om == null) {
            throw new IOException("连接已被强制中断");
        }
        return om;
    }

    public void heartBeat() throws IOException {
        send(new InMessage());
    }

    public void send(InMessage msg) throws IOException {
        String out = msg.toString();
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        printWriter.write(out);
        printWriter.flush();
    }

    private OutMessage receive() throws IOException, MessageException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return OutMessage.getMessage(bufferedReader.readLine());
    }

    public void close() throws IOException {
        socketMap.remove(socket.getLocalAddress() + ":" + socket.getPort());
        if (!socket.isClosed()) {
            socket.close();
        }
    }

    public void closeNew() throws IOException {
        if (!socket.isClosed()) {
            socket.close();
        }
    }

}
