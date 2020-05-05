package server;

import common.exceptions.NotInDeclaredLimitsException;
import common.exceptions.WrongAmountOfElementsException;
import common.utility.Outputer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.commands.*;
import server.utility.CollectionFileManager;
import server.utility.CollectionManager;
import server.utility.CommandManager;

/**
 * Main server class. Creates all server instances.
 * @author Sviridov Dmitry and Orlov Egor.
 */
public class App {
    public static final String ENV_VARIABLE = "LABA";
    public static Logger logger = LogManager.getLogger("ServerLogger");

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
            Outputer.println("Использование: 'java -jar " + jarName + " <host> <port>'");
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
        CollectionManager collectionManager = new CollectionManager(collectionFileManager);
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
        Server server = new Server(port, commandManager);
        server.run();
    }
}
