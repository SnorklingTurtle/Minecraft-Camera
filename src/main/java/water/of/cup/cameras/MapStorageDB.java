package main.java.water.of.cup.cameras;

import java.sql.*;
import java.time.LocalDateTime;

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
                + " id          INTEGER         PRIMARY KEY,\n"
                + " map_id      INTEGER         NOT NULL,\n"
                + " counter     INTEGER         DEFAULT 1,\n"
                + " data        BLOB            NOT NULL,\n"
                + " tag         TEXT            DEFAULT '',\n"
                + " created     INTEGER         NOT NULL,\n"
                + " UNIQUE(map_id)\n"
                + ");", MapStorageDB.tableName);

        try
        {
            Statement statement = conn.createStatement();
            statement.execute(query);
        }
        catch (SQLException e)
        {
            instance.getLogger().info(e.getMessage());
        }
    }

    public static void store(Connection conn, int id, byte[][] data) {
        Camera instance = Camera.getInstance();
        String query = String.format("INSERT INTO %s (map_id, data, created) VALUES(?,?,?)", MapStorageDB.tableName);

        try
        {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, id);
            statement.setBytes(2, serializeByteArray2d(data));
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            boolean isUniqueConstraintError = e.getErrorCode() == 19;
            if (!isUniqueConstraintError)
                instance.getLogger().info(e.getErrorCode() + ": " + e.getMessage());
        }
    }

    public static ResultSet getAll(Connection conn) {
        Camera instance = Camera.getInstance();
        String query = String.format("SELECT * FROM %s", MapStorageDB.tableName);
        ResultSet rs = null;

        try
        {
            Statement statement = conn.createStatement();
            rs = statement.executeQuery(query);
        }
        catch (SQLException e)
        {
            instance.getLogger().info(e.getMessage());
        }

        return rs;
    }

    public static ResultSet getById(Connection conn, Integer map_id) {
        Camera instance = Camera.getInstance();
        String query = String.format("SELECT * FROM %s WHERE map_id = %s", MapStorageDB.tableName, map_id);
        ResultSet rs = null;

        try
        {
            Statement statement = conn.createStatement();
            rs = statement.executeQuery(query);
        }
        catch (SQLException e)
        {
            instance.getLogger().info(e.getMessage());
        }

        return rs;
    }

    public static byte[] serializeByteArray2d(byte[][] array) {
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
        return ByteArrayCompression.compress(serializedArray);
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
