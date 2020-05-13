package server.commands;

import common.data.SpaceMarine;
import common.exceptions.CollectionIsEmptyException;
import common.exceptions.DatabaseHandlingException;
import common.exceptions.MarineNotFoundException;
import common.exceptions.WrongAmountOfElementsException;
import common.interaction.MarineRaw;
import server.utility.CollectionManager;
import server.utility.DatabaseCollectionManager;
import server.utility.ResponseOutputer;

import java.time.LocalDateTime;

/**
 * Command 'remove_greater'. Removes elements greater than user entered.
 */
public class RemoveGreaterCommand extends AbstractCommand {
    private CollectionManager collectionManager;
    private DatabaseCollectionManager databaseCollectionManager;

    public RemoveGreaterCommand(CollectionManager collectionManager, DatabaseCollectionManager databaseCollectionManager) {
        super("remove_greater", "{element}", "удалить из коллекции все элементы, превышающие заданный");
        this.collectionManager = collectionManager;
        this.databaseCollectionManager = databaseCollectionManager;
    }

    /**
     * Executes the command.
     * @return Command exit status.
     */
    @Override
    public boolean execute(String stringArgument, Object objectArgument) {
        // TODO: При создании объекта нужен пользователь
        try {
            if (!stringArgument.isEmpty() || objectArgument == null) throw new WrongAmountOfElementsException();
            if (collectionManager.collectionSize() == 0) throw new CollectionIsEmptyException();
            MarineRaw marineRaw = (MarineRaw) objectArgument;
            SpaceMarine marineToFind = new SpaceMarine(
                    0L,
                    marineRaw.getName(),
                    marineRaw.getCoordinates(),
                    LocalDateTime.now(),
                    marineRaw.getHealth(),
                    marineRaw.getCategory(),
                    marineRaw.getWeaponType(),
                    marineRaw.getMeleeWeapon(),
                    marineRaw.getChapter(),
                    "slamach"
            );
            SpaceMarine marineFromCollection = collectionManager.getByValue(marineToFind);
            if (marineFromCollection == null) throw new MarineNotFoundException();
            databaseCollectionManager.deleteMarineById(marineFromCollection.getId());
            collectionManager.removeGreater(marineFromCollection);
            ResponseOutputer.appendln("Солдаты успешно удалены!");
            return true;
        } catch (WrongAmountOfElementsException exception) {
            ResponseOutputer.appendln("Использование: '" + getName() + " " + getUsage() + "'");
        } catch (CollectionIsEmptyException exception) {
            ResponseOutputer.appenderror("Коллекция пуста!");
        } catch (MarineNotFoundException exception) {
            ResponseOutputer.appenderror("Солдата с такими характеристиками в коллекции нет!");
        } catch (ClassCastException exception) {
            ResponseOutputer.appenderror("Переданный клиентом объект неверен!");
        } catch (DatabaseHandlingException exception) {
            ResponseOutputer.appenderror("Произошла ошибка при обращении к базе данных!");
        }
        return false;
    }
}
