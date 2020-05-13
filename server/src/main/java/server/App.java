package server;

import common.exceptions.NotInDeclaredLimitsException;
import common.exceptions.WrongAmountOfElementsException;
import common.utility.Outputer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.commands.*;
import server.utility.*;

/**
 * Main server class. Creates all server instances.
 * @author Sviridov Dmitry and Orlov Egor.
 */
public class App {
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
        // TODO: TEST
        DatabaseHandler databaseHandler = new DatabaseHandler("jdbc:postgresql://localhost:5432/studs", "s284724", "sgf353");
        DatabaseUserManager databaseUserManager = new DatabaseUserManager(databaseHandler);
        DatabaseCollectionManager databaseCollectionManager = new DatabaseCollectionManager(databaseHandler, databaseUserManager);

        CollectionManager collectionManager = new CollectionManager(databaseCollectionManager);
        CommandManager commandManager = new CommandManager(
                new HelpCommand(),
                new InfoCommand(collectionManager),
                new ShowCommand(collectionManager),
                new AddCommand(collectionManager, databaseCollectionManager),
                new UpdateCommand(collectionManager, databaseCollectionManager),
                new RemoveByIdCommand(collectionManager, databaseCollectionManager),
                new ClearCommand(collectionManager, databaseCollectionManager),
                new ExitCommand(),
                new ExecuteScriptCommand(),
                new AddIfMinCommand(collectionManager, databaseCollectionManager),
                new RemoveGreaterCommand(collectionManager, databaseCollectionManager),
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
