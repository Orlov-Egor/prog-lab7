package server.utility;

import common.interaction.Request;
import common.interaction.Response;
import common.interaction.ResponseCode;
import common.utility.Outputer;
import server.App;
import server.Server;

import java.io.*;
import java.net.Socket;

public class ConnectionHandler implements Runnable {
    private Server server;
    private Socket clientSocket;
    private RequestHandler requestHandler;

    public ConnectionHandler(Server server, Socket clientSocket, RequestHandler requestHandler) {
        this.server = server;
        this.clientSocket = clientSocket;
        this.requestHandler = requestHandler;
    }

    @Override
    public void run() {
        Request userRequest = null;
        Response responseToUser = null;
        try (ObjectInputStream clientReader = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream clientWriter = new ObjectOutputStream(clientSocket.getOutputStream())) {
            do {
                userRequest = (Request) clientReader.readObject();
                responseToUser = requestHandler.handle(userRequest);
                App.logger.info("Запрос '" + userRequest.getCommandName() + "' обработан.");
                clientWriter.writeObject(responseToUser);
                clientWriter.flush();
            } while (responseToUser.getResponseCode() != ResponseCode.SERVER_EXIT &&
                    responseToUser.getResponseCode() != ResponseCode.CLIENT_EXIT);
            close();
            if (responseToUser.getResponseCode() == ResponseCode.SERVER_EXIT)
                server.stop();
        } catch (ClassNotFoundException exception) {
            Outputer.printerror("Произошла ошибка при чтении полученных данных!");
            App.logger.error("Произошла ошибка при чтении полученных данных!");
            close();
        } catch (InvalidClassException | NotSerializableException exception) {
            Outputer.printerror("Произошла ошибка при отправке данных на клиент!");
            App.logger.error("Произошла ошибка при отправке данных на клиент!");
            close();
        } catch (IOException exception) {
            Outputer.printerror("Непредвиденный разрыв соединения с клиентом!");
            App.logger.warn("Непредвиденный разрыв соединения с клиентом!");
        }
    }

    private void close()
    {
        try {
            clientSocket.close();
            Outputer.println("Клиент отключен от сервера.");
            App.logger.info("Клиент отключен от сервера.");
        } catch (IOException exception) {
            Outputer.printerror("Произошла ошибка при попытке завершить соединение с клиентом!");
            App.logger.error("Произошла ошибка при попытке завершить соединение с клиентом!");
        }
    }
}
