package server;

import common.data.*;
import common.exceptions.NotInDeclaredLimitsException;
import common.exceptions.WrongAmountOfElementsException;
import common.utility.Outputer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.commands.*;
import server.utility.*;

import java.time.LocalDateTime;

/**
 * Main server class. Creates all server instances.
 * @author Sviridov Dmitry and Orlov Egor.
 */
public class App {
    public static final String ENV_VARIABLE = "LABA";
    public static Logger logger = LogManager.getLogger("ServerLogger");

    private static final int MAX_CLIENTS = 1000;

    private static int port;

    private static boolean initializePort(String[] portArgs) {
        try {
            if (portArgs.length != 1) throw new WrongAmountOfElementsException();
            port = Integer.parseInt(portArgs[0]);
            if (port < 0) throw new NotInDeclaredLimitsException();
            return true;
        } catch (WrongAmountOfElementsException exception) {
            String jarName = new java.io.File(App.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath())
                    .getName();
            Outputer.println("Использование: 'java -jar " + jarName + " <port>'");
        } catch (NumberFormatException exception) {
            Outputer.printerror("Порт должен быть представлен числом!");
            App.logger.fatal("Порт должен быть представлен числом!");
        } catch (NotInDeclaredLimitsException exception) {
            Outputer.printerror("Порт не может быть отрицательным!");
            App.logger.fatal("Порт не может быть отрицательным!");
        }
        App.logger.fatal("Ошибка инициализации порта запуска!");
        return false;
    }

    public static void main(String[] args) {
        if (!initializePort(args)) return;
        CollectionFileManager collectionFileManager = new CollectionFileManager(ENV_VARIABLE);

        // TODO: TEST
        DatabaseHandler databaseHandler = new DatabaseHandler("jdbc:postgresql://localhost:5432/studs", "s284724", "sgf353");
        DatabaseUserManager databaseUserManager = new DatabaseUserManager(databaseHandler);
        DatabaseCollectionManager databaseCollectionManager = new DatabaseCollectionManager(databaseHandler, databaseUserManager);

        CollectionManager collectionManager = new CollectionManager(collectionFileManager, databaseCollectionManager);
        CommandManager commandManager = new CommandManager(
                new HelpCommand(),
                new InfoCommand(collectionManager),
                new ShowCommand(collectionManager),
                new AddCommand(collectionManager),
                new UpdateCommand(collectionManager),
                new RemoveByIdCommand(collectionManager),
                new ClearCommand(collectionManager),
                new SaveCommand(collectionManager),
                new ExitCommand(),
                new ExecuteScriptCommand(),
                new AddIfMinCommand(collectionManager),
                new RemoveGreaterCommand(collectionManager),
                new HistoryCommand(),
                new SumOfHealthCommand(collectionManager),
                new MaxByMeleeWeaponCommand(collectionManager),
                new FilterByWeaponTypeCommand(collectionManager),
                new ServerExitCommand()
        );
        Server server = new Server(port, MAX_CLIENTS, commandManager);
        server.run();

        // TODO: TEST
        databaseHandler.closeConnection();
    }
}
