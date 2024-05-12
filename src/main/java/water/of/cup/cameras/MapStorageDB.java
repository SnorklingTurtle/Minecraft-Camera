package main.java.water.of.cup.cameras;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class MapStorageDB {

    final static String tableName = "pictures";

    public static Connection connect() {
        Camera instance = Camera.getInstance();
        String folder = instance.getDataFolder().getAbsolutePath();
        Connection conn = null;

        try
        {
            String url = String.format("jdbc:sqlite:%s/pictures.db", folder) ;
            conn = DriverManager.getConnection(url);
        }
        catch (SQLException e)
        {
            instance.getLogger().info(e.getMessage());
        }

        return conn;
    }

    public static void disconnect(Connection conn) {
        Camera instance = Camera.getInstance();
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            instance.getLogger().info(ex.getMessage());
        }
    }

    public static void createTable(Connection conn) {
        Camera instance = Camera.getInstance();
        String query = String.format( "CREATE TABLE IF NOT EXISTS %s (\n"
                + " id              INTEGER         PRIMARY KEY,\n"
                + " map_id          INTEGER         NOT NULL,\n"
                + " seed            INTEGER         NOT NULL,\n"
                + " counter         INTEGER         DEFAULT 1,\n"
                + " data            BLOB            NOT NULL,\n"
                + " photographer    TEXT,\n"
                + " tag             TEXT,\n"
                + " tagger          TEXT,\n"
                + " created         INTEGER         NOT NULL,\n"
                + " UNIQUE(map_id, seed) ON CONFLICT IGNORE,\n"
                + " UNIQUE(tag) ON CONFLICT IGNORE\n"
                + ");", MapStorageDB.tableName);

        try
        {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.execute();
        }
        catch (SQLException e)
        {
            instance.getLogger().info(e.getMessage());
        }
    }

    public static void createCleanUpTrigger(Connection conn) {
        Camera instance = Camera.getInstance();
        String query = String.format("CREATE TRIGGER IF NOT EXISTS picture_cleanup\n" +
                "   AFTER UPDATE\n" +
                "   ON pictures\n" +
                "   WHEN NEW.counter <= 0\n" +
                " BEGIN\n" +
                "   DELETE FROM %s WHERE id=NEW.id;\n" +
                " END;\n", MapStorageDB.tableName);

        try
        {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.execute();
        }
        catch (SQLException e)
        {
            instance.getLogger().info(e.getMessage());
        }
    }

    public static void store(Connection conn, int id, long seed, byte[][] data, UUID photographer, int counter) {
        store(conn, id, seed, data, photographer, counter, null, null);
    }

    public static void store(Connection conn, int id, long seed, byte[][] data, UUID photographer, int counter, String tag, UUID tagger) {
        Camera instance = Camera.getInstance();
        String query = String.format("INSERT INTO %s (map_id, seed, data, created, tag, tagger, photographer, counter) VALUES(?,?,?,?,?,?,?,?);", MapStorageDB.tableName);

        try
        {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, id);
            statement.setLong(2, seed);
            statement.setBytes(3, serializeByteArray2d(data));
            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(5, tag);
            statement.setString(6, tagger != null ? tagger.toString() : null);
            statement.setString(7, photographer != null ? photographer.toString() : null);
            statement.setInt(8, counter);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            boolean isUniqueConstraintError = e.getErrorCode() == 19;
            if (!isUniqueConstraintError)
                instance.getLogger().info(e.getErrorCode() + ": " + e.getMessage());
        }
    }

    public static ResultSet getBySeed(Connection conn, long seed) {
        Camera instance = Camera.getInstance();
        String query = String.format("SELECT map_id,data FROM %s WHERE seed=?;", MapStorageDB.tableName);
        ResultSet rs = null;

        try
        {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setLong(1, seed);
            rs = statement.executeQuery();
        }
        catch (SQLException e)
        {
            instance.getLogger().info(e.getMessage());
        }

        return rs;
    }

    public static void updateCounter(Connection conn, Integer map_id, long world_seed, int amount) {
        Camera instance = Camera.getInstance();
        String query = String.format("UPDATE %s SET counter=(counter+?) WHERE map_id=? AND seed=?;", MapStorageDB.tableName);

        try
        {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, amount);
            statement.setInt(2, map_id);
            statement.setLong(3, world_seed);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            instance.getLogger().info(e.getMessage());
        }
    }

    public static boolean updateTag(Connection conn, Integer map_id, long world_seed, String tag, UUID taggerUUID) {
        Camera instance = Camera.getInstance();
        String query = String.format("UPDATE %s SET tag=?, tagger=? WHERE map_id=? AND seed=?;", MapStorageDB.tableName);

        try
        {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, tag != null ? tag.toLowerCase() : null);
            statement.setString(2, taggerUUID != null ? taggerUUID.toString() : null);
            statement.setInt(3, map_id);
            statement.setLong(4, world_seed);
            return statement.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            instance.getLogger().info(e.getMessage());
        }
        return false;
    }

    public static ResultSet getById(Connection conn, Integer map_id, long world_seed) {
        Camera instance = Camera.getInstance();
        String query = String.format("SELECT map_id,data,tag FROM %s WHERE map_id=? AND seed=? LIMIT 1;", MapStorageDB.tableName);
        ResultSet rs = null;

        try
        {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, map_id);
            statement.setLong(2, world_seed);
            rs = statement.executeQuery();
        }
        catch (SQLException e)
        {
            instance.getLogger().info(e.getMessage());
        }

        return rs;
    }

    public static ResultSet getByTag(Connection conn, String tag) {
        Camera instance = Camera.getInstance();
        String query = String.format("SELECT map_id,data,tag,seed,photographer FROM %s WHERE tag=? LIMIT 1;", MapStorageDB.tableName);
        ResultSet rs = null;

        try
        {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, tag.toLowerCase());
            rs = statement.executeQuery();
        }
        catch (SQLException e)
        {
            instance.getLogger().info(e.getMessage());
        }

        return rs;
    }

    public static ResultSet getTagsByPlayer(Connection conn, UUID playerUUID, int amount) {
        Camera instance = Camera.getInstance();
        String query = String.format("SELECT tag FROM %s WHERE tagger=? ORDER BY RANDOM() LIMIT ?;", MapStorageDB.tableName);
        ResultSet rs = null;

        try
        {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, playerUUID.toString());
            statement.setInt(2, amount);
            rs = statement.executeQuery();
        }
        catch (SQLException e)
        {
            instance.getLogger().info(e.getMessage());
        }

        return rs;
    }

    public static byte[] serializeByteArray2d(byte[][] array, boolean useCompression) {
        int totalLength = 0;
        for (byte[] row : array) {
            totalLength += row.length;
        }
        byte[] serializedArray = new byte[totalLength];
        int index = 0;
        for (byte[] row : array) {
            System.arraycopy(row, 0, serializedArray, index, row.length);
            index += row.length;
        }
        return useCompression ? ByteArrayCompression.compress(serializedArray) : serializedArray;
    }

    public static byte[] serializeByteArray2d(byte[][] array) {
        return serializeByteArray2d(array, true);
    }

    public static byte[][] deserializeByteArray2d(byte[] serializedCompressedArray) {
        int rows = 128;
        int cols = 128;

        if (serializedCompressedArray == null) //  || serializedCompressedArray.length != rows * cols
            return null;

        byte[] serializedArray = null;
        try {
            serializedArray = ByteArrayCompression.decompress(serializedCompressedArray);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (serializedArray == null)
            return null;

        byte[][] deserializedArray = new byte[rows][cols];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(serializedArray, i * cols, deserializedArray[i], 0, cols);
        }

        return deserializedArray;
    }
}
