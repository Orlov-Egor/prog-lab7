package server.utility;

import common.data.*;
import common.utility.Outputer;
import server.App;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.NavigableSet;
import java.util.TreeSet;

public class DatabaseCollectionManager {
    // MARINE_TABLE
    private final String SELECT_ALL_MARINES = "SELECT * FROM " + DatabaseHandler.MARINE_TABLE;
    private final String SELECT_MARINE_BY_ID = SELECT_ALL_MARINES + " WHERE " +
            DatabaseHandler.MARINE_TABLE_ID_COLUMN + " = ?";
    private final String INSERT_MARINE = "INSERT INTO " +
            DatabaseHandler.MARINE_TABLE + " (" +
            DatabaseHandler.MARINE_TABLE_NAME_COLUMN + ", " +
            DatabaseHandler.MARINE_TABLE_CREATION_DATE_COLUMN + ", " +
            DatabaseHandler.MARINE_TABLE_HEALTH_COLUMN + ", " +
            DatabaseHandler.MARINE_TABLE_CATEGORY_COLUMN + ", " +
            DatabaseHandler.MARINE_TABLE_WEAPON_TYPE_COLUMN + ", " +
            DatabaseHandler.MARINE_TABLE_MELEE_WEAPON_COLUMN + ", " +
            DatabaseHandler.MARINE_TABLE_CHAPTER_ID_COLUMN + ", " +
            DatabaseHandler.MARINE_TABLE_USER_ID_COLUMN + ") VALUES (?, ?, ?, ?::astartes_category," +
            "?::weapon, ?::melee_weapon, ?, ?)";
    private final String DELETE_MARINE_BY_ID = "DELETE FROM " + DatabaseHandler.MARINE_TABLE +
            " WHERE " + DatabaseHandler.MARINE_TABLE_ID_COLUMN + " = ?";
    // COORDINATES_TABLE
    private final String SELECT_ALL_COORDINATES = "SELECT * FROM " + DatabaseHandler.COORDINATES_TABLE;
    private final String SELECT_COORDINATES_BY_MARINE_ID = SELECT_ALL_COORDINATES +
            " WHERE " + DatabaseHandler.COORDINATES_TABLE_SPACE_MARINE_ID_COLUMN + " = ?";
    private final String INSERT_COORDINATES = "INSERT INTO " +
            DatabaseHandler.COORDINATES_TABLE + " (" +
            DatabaseHandler.COORDINATES_TABLE_SPACE_MARINE_ID_COLUMN + ", " +
            DatabaseHandler.COORDINATES_TABLE_X_COLUMN + ", " +
            DatabaseHandler.COORDINATES_TABLE_Y_COLUMN + ") VALUES (?, ?, ?)";
    private final String DELETE_COORDINATES_BY_MARINE_ID = "DELETE FROM " + DatabaseHandler.COORDINATES_TABLE +
            " WHERE " + DatabaseHandler.COORDINATES_TABLE_SPACE_MARINE_ID_COLUMN + " = ?";
    // CHAPTER_TABLE
    private final String SELECT_ALL_CHAPTER = "SELECT * FROM " + DatabaseHandler.CHAPTER_TABLE;
    private final String SELECT_CHAPTER_BY_ID = SELECT_ALL_CHAPTER +
            " WHERE " + DatabaseHandler.CHAPTER_TABLE_ID_COLUMN + " = ?";
    private final String INSERT_CHAPTER = "INSERT INTO " +
            DatabaseHandler.CHAPTER_TABLE + " (" +
            DatabaseHandler.CHAPTER_TABLE_NAME_COLUMN + ", " +
            DatabaseHandler.CHAPTER_TABLE_MARINES_COUNT_COLUMN + ") VALUES (?, ?)";
    private final String DELETE_CHAPTER_BY_ID = "DELETE FROM " + DatabaseHandler.CHAPTER_TABLE +
            " WHERE " + DatabaseHandler.CHAPTER_TABLE_ID_COLUMN + " = ?";

    private DatabaseHandler databaseHandler;
    private DatabaseUserManager databaseUserManager;

    public DatabaseCollectionManager(DatabaseHandler databaseHandler, DatabaseUserManager databaseUserManager) {
        this.databaseHandler = databaseHandler;
        this.databaseUserManager = databaseUserManager;
    }

    private SpaceMarine createMarine(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong(DatabaseHandler.MARINE_TABLE_ID_COLUMN);
        String name = resultSet.getString(DatabaseHandler.MARINE_TABLE_NAME_COLUMN);
        LocalDateTime creationDate = resultSet.getTimestamp(DatabaseHandler.MARINE_TABLE_CREATION_DATE_COLUMN).toLocalDateTime();
        double health = resultSet.getDouble(DatabaseHandler.MARINE_TABLE_HEALTH_COLUMN);
        AstartesCategory category = AstartesCategory.valueOf(resultSet.getString(DatabaseHandler.MARINE_TABLE_CATEGORY_COLUMN));
        Weapon weaponType = Weapon.valueOf(resultSet.getString(DatabaseHandler.MARINE_TABLE_WEAPON_TYPE_COLUMN));
        MeleeWeapon meleeWeapon = MeleeWeapon.valueOf(resultSet.getString(DatabaseHandler.MARINE_TABLE_MELEE_WEAPON_COLUMN));
        Coordinates coordinates = getCoordinatesByMarineId(id);
        Chapter chapter = getChapterById(resultSet.getLong(DatabaseHandler.MARINE_TABLE_CHAPTER_ID_COLUMN));
        String owner = databaseUserManager.getUserById(resultSet.getLong(DatabaseHandler.MARINE_TABLE_USER_ID_COLUMN)).getUsername();
        return new SpaceMarine(
                id,
                name,
                coordinates,
                creationDate,
                health,
                category,
                weaponType,
                meleeWeapon,
                chapter,
                owner
        );
    }

    public NavigableSet<SpaceMarine> getCollection() {
        NavigableSet<SpaceMarine> marineList = new TreeSet<>();
        PreparedStatement preparedSelectAllStatement = null;
        try {
            preparedSelectAllStatement = databaseHandler.getPreparedStatement(SELECT_ALL_MARINES, false);
            ResultSet resultSet = preparedSelectAllStatement.executeQuery();
            while (resultSet.next()) {
                marineList.add(createMarine(resultSet));
            }
            Outputer.println("Коллекция загружена.");
            App.logger.info("Коллекция загружена.");
        } catch (SQLException exception) {
            Outputer.printerror("Коллекция не может быть загружена!");
            App.logger.error("Коллекция не может быть загружена!");
        } finally {
            databaseHandler.closePreparedStatement(preparedSelectAllStatement);
        }
        return marineList;
    }
    public long getChapterIdByMarineId(long marineId) throws SQLException {
        long chapterId;
        PreparedStatement preparedSelectMarineByIdStatement = null;
        try {
            preparedSelectMarineByIdStatement = databaseHandler.getPreparedStatement(SELECT_MARINE_BY_ID, false);
            preparedSelectMarineByIdStatement.setLong(1, marineId);
            ResultSet resultSet = preparedSelectMarineByIdStatement.executeQuery();
            if (resultSet.next()) {
                chapterId = resultSet.getLong(DatabaseHandler.MARINE_TABLE_CHAPTER_ID_COLUMN);
            } else throw new SQLException();
            App.logger.info("Выполнен запрос SELECT_MARINE_BY_ID.");
        } catch (SQLException exception) {
            App.logger.error("Произошла ошибка при выполнении запроса SELECT_MARINE_BY_ID!");
            throw new SQLException(exception);
        } finally {
            databaseHandler.closePreparedStatement(preparedSelectMarineByIdStatement);
        }
        return chapterId;
    }


    public Coordinates getCoordinatesByMarineId(long marineId) throws SQLException {
        Coordinates coordinates;
        PreparedStatement preparedSelectCoordinatesByMarineIdStatement = null;
        try {
            preparedSelectCoordinatesByMarineIdStatement =
                    databaseHandler.getPreparedStatement(SELECT_COORDINATES_BY_MARINE_ID, false);
            preparedSelectCoordinatesByMarineIdStatement.setLong(1, marineId);
            ResultSet resultSet = preparedSelectCoordinatesByMarineIdStatement.executeQuery();
            if (resultSet.next()) {
                coordinates = new Coordinates(
                        resultSet.getDouble(DatabaseHandler.COORDINATES_TABLE_X_COLUMN),
                        resultSet.getFloat(DatabaseHandler.COORDINATES_TABLE_Y_COLUMN)
                );
            } else throw new SQLException();
            App.logger.info("Выполнен запрос SELECT_COORDINATES_BY_MARINE_ID.");
        } catch (SQLException exception) {
            App.logger.error("Произошла ошибка при выполнении запроса SELECT_COORDINATES_BY_MARINE_ID!");
            throw new SQLException(exception);
        } finally {
            databaseHandler.closePreparedStatement(preparedSelectCoordinatesByMarineIdStatement);
        }
        return coordinates;
    }

    public Chapter getChapterById(long chapterId) throws SQLException {
        Chapter chapter;
        PreparedStatement preparedSelectChapterByIdStatement = null;
        try {
            preparedSelectChapterByIdStatement =
                    databaseHandler.getPreparedStatement(SELECT_CHAPTER_BY_ID, false);
            preparedSelectChapterByIdStatement.setLong(1, chapterId);
            ResultSet resultSet = preparedSelectChapterByIdStatement.executeQuery();
            if (resultSet.next()) {
                chapter = new Chapter(
                        resultSet.getString(DatabaseHandler.CHAPTER_TABLE_NAME_COLUMN),
                        resultSet.getLong(DatabaseHandler.CHAPTER_TABLE_MARINES_COUNT_COLUMN)
                );
            } else throw new SQLException();
            App.logger.info("Выполнен запрос SELECT_CHAPTER_BY_ID.");
        } catch (SQLException exception) {
            App.logger.error("Произошла ошибка при выполнении запроса SELECT_CHAPTER_BY_ID!");
            throw new SQLException(exception);
        } finally {
            databaseHandler.closePreparedStatement(preparedSelectChapterByIdStatement);
        }
        return chapter;
    }

    public long insertMarine(SpaceMarine marine) {
        // TODO: Реализовать вставку создателя
        // TODO: Если делаем орден уникальным, тут че-то много всего менять
        PreparedStatement preparedInsertMarineStatement = null;
        PreparedStatement preparedInsertCoordinatesStatement = null;
        PreparedStatement preparedInsertChapterStatement = null;
        try {
            databaseHandler.setCommitMode();
            databaseHandler.setSavepoint();

            preparedInsertMarineStatement = databaseHandler.getPreparedStatement(INSERT_MARINE, true);
            preparedInsertCoordinatesStatement = databaseHandler.getPreparedStatement(INSERT_COORDINATES, true);
            preparedInsertChapterStatement = databaseHandler.getPreparedStatement(INSERT_CHAPTER, true);

            preparedInsertChapterStatement.setString(1, marine.getChapter().getName());
            preparedInsertChapterStatement.setLong(2, marine.getChapter().getMarinesCount());
            if (preparedInsertChapterStatement.executeUpdate() == 0) throw new SQLException();
            ResultSet generatedChapterKeys = preparedInsertChapterStatement.getGeneratedKeys();
            long chapterId;
            if (generatedChapterKeys.next()) {
                chapterId = generatedChapterKeys.getLong(1);
            } else throw new SQLException();
            App.logger.info("Выполнен запрос INSERT_CHAPTER.");

            preparedInsertMarineStatement.setString(1, marine.getName());
            preparedInsertMarineStatement.setTimestamp(2, Timestamp.valueOf(marine.getCreationDate()));
            preparedInsertMarineStatement.setDouble(3, marine.getHealth());
            preparedInsertMarineStatement.setString(4, marine.getCategory().toString());
            preparedInsertMarineStatement.setString(5, marine.getWeaponType().toString());
            preparedInsertMarineStatement.setString(6, marine.getMeleeWeapon().toString());
            preparedInsertMarineStatement.setLong(7, chapterId);
            // TODO: Тут должна быть вставка пользователя
            preparedInsertMarineStatement.setLong(8, 1L);
            if (preparedInsertMarineStatement.executeUpdate() == 0) throw new SQLException();
            ResultSet generatedMarineKeys = preparedInsertMarineStatement.getGeneratedKeys();
            long spaceMarineId;
            if (generatedMarineKeys.next()) {
                spaceMarineId = generatedMarineKeys.getLong(1);
            } else throw new SQLException();
            App.logger.info("Выполнен запрос INSERT_MARINE.");

            preparedInsertCoordinatesStatement.setLong(1, spaceMarineId);
            preparedInsertCoordinatesStatement.setDouble(2, marine.getCoordinates().getX());
            preparedInsertCoordinatesStatement.setFloat(3, marine.getCoordinates().getY());
            if (preparedInsertCoordinatesStatement.executeUpdate() == 0) throw new SQLException();
            App.logger.info("Выполнен запрос INSERT_COORDINATES.");

            databaseHandler.commit();
            return spaceMarineId;
        } catch (SQLException exception) {
            App.logger.error("Произошла ошибка при выполнении группы запросов на добавление нового объекта!");
            databaseHandler.rollback();
        } finally {
            databaseHandler.closePreparedStatement(preparedInsertMarineStatement);
            databaseHandler.closePreparedStatement(preparedInsertCoordinatesStatement);
            databaseHandler.closePreparedStatement(preparedInsertChapterStatement);
            databaseHandler.setNormalMode();
        }
        return -1L;
    }

    public boolean deleteMarineById(long marineId) {
        // TODO: Орден так удалять нельзя, нужно либо уже делать его уникальным, либо автоудаление, как у координат
        PreparedStatement preparedDeleteMarineByIdStatement = null;
        PreparedStatement preparedDeleteChapterByIdStatement = null;
        try {
            databaseHandler.setCommitMode();
            databaseHandler.setSavepoint();

            preparedDeleteMarineByIdStatement = databaseHandler.getPreparedStatement(DELETE_MARINE_BY_ID, false);
            preparedDeleteChapterByIdStatement = databaseHandler.getPreparedStatement(DELETE_CHAPTER_BY_ID, false);

            preparedDeleteMarineByIdStatement.setLong(1, marineId);
            preparedDeleteChapterByIdStatement.setLong(1, getChapterIdByMarineId(marineId));

            if (preparedDeleteMarineByIdStatement.executeUpdate() == 0) Outputer.println(1);
            App.logger.info("Выполнен запрос DELETE_MARINE_BY_ID.");
            if (preparedDeleteChapterByIdStatement.executeUpdate() == 0) Outputer.println(3);
            App.logger.info("Выполнен запрос DELETE_CHAPTER_BY_ID.");

            databaseHandler.commit();
            return true;
        } catch (SQLException exception) {
            App.logger.error("Произошла ошибка при выполнении группы запросов на удаление объекта!");
            databaseHandler.rollback();
        } finally {
            databaseHandler.closePreparedStatement(preparedDeleteMarineByIdStatement);
            databaseHandler.closePreparedStatement(preparedDeleteChapterByIdStatement);
            databaseHandler.setNormalMode();
        }
        return false;
    }
}
