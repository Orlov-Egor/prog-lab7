package server;

import common.exceptions.ClosingSocketException;
import common.exceptions.ConnectionErrorException;
import common.exceptions.OpeningServerSocketException;
import common.utility.Outputer;
import server.utility.CommandManager;
import server.utility.ConnectionHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Runs the server.
 */
public class Server {
    // TODO: Тут есть какая-то кривая синхронизация
    private int port;
    private ServerSocket serverSocket;
    private CommandManager commandManager;
    private boolean isStopped;
    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    public Server(int port, CommandManager commandManager) {
        this.port = port;
        this.commandManager = commandManager;
    }

    /**
    * Begins server operation.
    */
    public void run() {
        try {
            openServerSocket();
            while (!isStopped()) {
                try {
                    Socket clientSocket = connectToClient();
                    cachedThreadPool.execute(new ConnectionHandler(this, clientSocket, commandManager));
                } catch (ConnectionErrorException exception) {
                    if (!isStopped()) {
                        Outputer.printerror("Произошла ошибка при соединении с клиентом!");
                        App.logger.error("Произошла ошибка при соединении с клиентом!");
                    } else break;
                }
            }
            cachedThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            Outputer.println("Работа сервера завершена.");
        } catch (OpeningServerSocketException exception) {
            Outputer.printerror("Сервер не может быть запущен!");
            App.logger.fatal("Сервер не может быть запущен!");
        } catch (InterruptedException e) {
            Outputer.printerror("Произошла ошибка при завершении работы с уже подключенными клиентами!");
        }
    }

    /**
    * Finishes server operation.
    */
    public synchronized void stop() {
        try {
            App.logger.info("Завершение работы сервера...");
            if (serverSocket == null) throw new ClosingSocketException();
            isStopped = true;
            cachedThreadPool.shutdown();
            serverSocket.close();
            Outputer.println("Завершение работы с уже подключенными клиентами...");
            App.logger.info("Работа сервера завершена.");
        } catch (ClosingSocketException exception) {
            Outputer.printerror("Невозможно завершить работу еще не запущенного сервера!");
            App.logger.error("Невозможно завершить работу еще не запущенного сервера!");
        } catch (IOException exception) {
            Outputer.printerror("Произошла ошибка при завершении работы сервера!");
            Outputer.println("Завершение работы с уже подключенными клиентами...");
            App.logger.error("Произошла ошибка при завершении работы сервера!");
        }
    }

    private synchronized boolean isStopped()
    {
        // TODO: Заменить на атомарную переменную (?)
        return isStopped;
    }

    /**
    * Open server socket.
    */
    private void openServerSocket() throws OpeningServerSocketException {
        try {
            App.logger.info("Запуск сервера...");
            serverSocket = new ServerSocket(port);
            App.logger.info("Сервер запущен.");
        } catch (IllegalArgumentException exception) {
            Outputer.printerror("Порт '" + port + "' находится за пределами возможных значений!");
            App.logger.fatal("Порт '" + port + "' находится за пределами возможных значений!");
            throw new OpeningServerSocketException();
        } catch (IOException exception) {
            Outputer.printerror("Произошла ошибка при попытке использовать порт '" + port + "'!");
            App.logger.fatal("Произошла ошибка при попытке использовать порт '" + port + "'!");
            throw new OpeningServerSocketException();
        }
    }

    /**
    * Connecting to client.
    */
    private Socket connectToClient() throws ConnectionErrorException {
        try {
            // TODO: Изменить надпись на что-то с потоком
            Outputer.println("Прослушивание порта '" + port + "'...");
            App.logger.info("Прослушивание порта '" + port + "'...");
            Socket clientSocket = serverSocket.accept();
            Outputer.println("Соединение с клиентом установлено.");
            App.logger.info("Соединение с клиентом установлено.");
            return clientSocket;
        } catch (IOException exception) {
            throw new ConnectionErrorException();
        }
    }
}
