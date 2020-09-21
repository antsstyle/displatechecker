package antsstyle.displatechecker.db;

import antsstyle.displatechecker.datastructures.DBResponse;
import antsstyle.displatechecker.enumerations.DBResponseCode;
import antsstyle.displatechecker.enumerations.DBSyntax;
import antsstyle.displatechecker.enumerations.DBTable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.TreeMap;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Primary database class. Contains most database methods for DisplateChecker.
 *
 * @author Ant
 */
public class CoreDB {
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    private static final String CREATE_DISPLATE_TABLE = "CREATE TABLE IF NOT EXISTS DISPLATE ("
            + "ID INT IDENTITY PRIMARY KEY, "
            + "DISPLATEFILEPATH VARCHAR(255) NOT NULL, "
            + "ARTISTFILEPATH VARCHAR(255) NOT NULL, "
            + "DISPLATEURL VARCHAR(255) NOT NULL, "
            + "HISTOGRAMDIFF DOUBLE NOT NULL)";
    
    private static final String CREATE_DISPLATE_FILE_INFO_TABLE = "CREATE TABLE IF NOT EXISTS DISPLATEFILEINFO ("
            + "ID INT IDENTITY PRIMARY KEY, "
            + "FILEPATH VARCHAR(255) NOT NULL, "
            + "URL VARCHAR(255) NOT NULL)";
    
    private static final String CREATE_SETTINGS_TABLE = "CREATE TABLE IF NOT EXISTS SETTINGS ("
            + "ID INT IDENTITY PRIMARY KEY, "
            + "NAME VARCHAR(255) NOT NULL, "
            + "VALUE VARCHAR(255) NOT NULL)";
    
    private static final String DISPLATE_TABLE_ADD_UNIQUE
            = "ALTER TABLE SETTINGS ADD UNIQUE (DISPLATEFILEPATH,ARTISTFILEPATH)";
    
    private static final String SETTINGS_TABLE_ADD_UNIQUE
            = "ALTER TABLE SETTINGS ADD UNIQUE (NAME,VALUE)";

    // Database connection
    /**
     * Pool of DB connections for ArtPoster modules to make use of.
     */
    protected static BasicDataSource connectionPool = null;
    
    public static void initialiseTables() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate(CREATE_DISPLATE_TABLE);
            stmt = conn.createStatement();
            stmt.executeUpdate(CREATE_DISPLATE_FILE_INFO_TABLE);
            stmt = conn.createStatement();
            stmt.executeUpdate(CREATE_SETTINGS_TABLE);
            try {
                stmt = conn.createStatement();
                stmt.executeUpdate(DISPLATE_TABLE_ADD_UNIQUE);                
                stmt = conn.createStatement();
                stmt.executeUpdate(SETTINGS_TABLE_ADD_UNIQUE);
            } catch (Exception e1) {
                LOGGER.debug("Unique constraints already exist.");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to create and initialise tables!", e);
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
    }
    
    public static void shutDown() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.createStatement();
            stmt.executeQuery("SHUTDOWN");
        } catch (Exception e) {
            LOGGER.error("Failed to execute shut down DB command!", e);
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
    }
    
    public static ArrayList<TreeMap<String, Object>> getDisplateInfoRows() {
        ArrayList<TreeMap<String, Object>> rows = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT id,filepath,url FROM displatefileinfo");
            while (rs.next()) {
                TreeMap<String, Object> r = new TreeMap<>();
                r.put("id", rs.getInt("id"));
                r.put("filepath", rs.getString("filepath"));
                r.put("url", rs.getString("url"));
                rows.add(r);
            }
            return rows;
        } catch (Exception e) {
            LOGGER.error("Failed to execute shut down DB command!", e);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
        return null;
    }
    
    public static ArrayList<TreeMap<String, Object>> getDisplateMatchesForArtistImage(String filePath) {
        String query = "SELECT id,displatefilepath,artistfilepath,displateurl,histogramdiff FROM displate"
                + " WHERE artistfilepath=?";
        ArrayList<TreeMap<String, Object>> rows = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, filePath);
            rs = stmt.executeQuery();
            while (rs.next()) {
                TreeMap<String, Object> r = new TreeMap<>();
                r.put("id", rs.getInt("id"));
                r.put("displatefilepath", rs.getString("displatefilepath"));
                r.put("artistfilepath", rs.getString("artistfilepath"));
                r.put("displateurl", rs.getString("displateurl"));
                r.put("histogramdiff", rs.getDouble("histogramdiff"));
                rows.add(r);
            }
            return rows;
        } catch (Exception e) {
            LOGGER.error("Failed to execute shut down DB command!", e);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
        return null;
    }
    
    public static String checkIfDisplateURLInInfoTable(String url) {
        String query = "SELECT filepath FROM displatefileinfo WHERE url=?";
        ArrayList<TreeMap<String, Object>> rows = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, url);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("filepath");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to execute shut down DB command!", e);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
        return null;
    }
    
    public static String getChromePathFromSettings() {
        String query = "SELECT value FROM settings WHERE name=?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, "chromepath");
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("value");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to execute shut down DB command!", e);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
        return null;
    }

    /**
     * Deletes from the given table, where the given fields in whereFields are equal to the given values in whereValues. Example usage:
     * <p>
     * deleteFromTable(DBTables.IMAGETWEETS,
     * <p>
     * new String[]{"id"},
     * <p>
     * new Object[]{1234});
     * <p>
     * The above code would execute "DELETE FROM imagetweets WHERE id=1234".
     *
     * @param table The table to delete from. Enumerated to prevent an invalid table name being submitted to this method.
     * @param whereFields The fields to check against when deleting. You cannot provide an empty array.
     * @param whereValues The value conditions for the fields. This cannot be empty, and must be of equal length to the whereFields array.
     * @return The JDBC result of executing the delete query; DB_ERROR if an error occurs.
     */
    public static DBResponse deleteFromTable(DBTable table, String[] whereFields, Object[] whereValues) {
        String tableName = table.getTableName();
        DBResponse resp = new DBResponse();
        if (whereFields.length == 0 || whereValues.length == 0) {
            LOGGER.error("This function requires all arguments - you cannot call it with an empty array.");
            resp.setStatusCode(DBResponseCode.INPUT_ERROR);
            return resp;
        }
        if (whereFields.length != whereValues.length) {
            LOGGER.error("Update and where arrays passed to this function must be of equal length.");
            resp.setStatusCode(DBResponseCode.INPUT_ERROR);
            return resp;
        }
        String query = "DELETE FROM ".concat(tableName)
                .concat(" WHERE ");
        for (String field : whereFields) {
            query = query.concat(field)
                    .concat("=? AND ");
        }
        query = query.substring(0, query.length() - 4);
        PreparedStatement stmt = null;
        Connection connection = null;
        try {
            connection = acquireConnectionFromPool();
            stmt = connection.prepareStatement(query);
            int i = 1;
            for (Object where : whereValues) {
                stmt.setObject(i, where);
                i++;
            }
            int result = stmt.executeUpdate();
            resp.setSQLResult(result);
            resp.setStatusCode(DBResponseCode.SUCCESS);
            return resp;
        } catch (Exception e) {
            LOGGER.error("Error deleting from DB!", e);
            resp.setStatusCode(DBResponseCode.DB_ERROR);
            return resp;
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(connection);
        }
    }
    
    public static void initialise() {
        connectionPool = new BasicDataSource();
        connectionPool.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
        connectionPool.setUsername("SA");
        connectionPool.setPassword("");
        String url = "jdbc:hsqldb:file:db/pimdb";
        connectionPool.setUrl(url);
        connectionPool.setInitialSize(10);
        connectionPool.setMaxOpenPreparedStatements(10);
        connectionPool.setMaxConnLifetimeMillis(1000 * 60 * 5);
        connectionPool.setMaxTotal(10);
        initialiseTables();
    }
    
    public static void getConnectionInfo() {
        LOGGER.info("Active DB connections: " + connectionPool.getNumActive() + "   Idle DB connections: " + connectionPool.getNumIdle());
    }
    
    public static boolean testDatabaseConnection() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = acquireConnectionFromPool();
            stmt = conn.prepareStatement("SELECT 1");
            return true;
        } catch (SQLException e) {
            return false;
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
        }
    }

    /**
     * Inserts data into a database table, depending on the parameters given.
     *
     * @param table The table to insert a new row into.
     * @param insertFields The fields into which to insert data.
     * @param insertValues The data values to insert into the given fields (this array must be of equal size to insertFields).
     * @return The SQL result of the statement if successful (usually 1); an error code otherwise.
     */
    public static DBResponse insertIntoTable(DBTable table, String[] insertFields, Object[] insertValues) {
        DBResponse resp = new DBResponse();
        if (insertFields.length == 0 || insertValues.length == 0) {
            LOGGER.error("You must provide at least one field to insert a value into.");
            resp.setStatusCode(DBResponseCode.INPUT_ERROR);
            return resp;
        }
        if (insertFields.length != insertValues.length) {
            LOGGER.error("Number of fields and values do not match.");
            resp.setStatusCode(DBResponseCode.INPUT_ERROR);
            return resp;
        }
        String query = "INSERT INTO ".concat(table.getTableName())
                .concat(" (");
        for (String field : insertFields) {
            query = query.concat(field)
                    .concat(",");
        }
        query = query.substring(0, query.length() - 1)
                .concat(") VALUES (");
        for (String insertField : insertFields) {
            query = query.concat("?,");
        }
        query = query.substring(0, query.length() - 1)
                .concat(")");
        PreparedStatement stmt = null;
        Connection connection;
        try {
            connection = acquireConnectionFromPool();
        } catch (SQLException sqle) {
            LOGGER.error("Failed to acquire connection!", sqle);
            resp.setStatusCode(DBResponseCode.CONNECTION_FAILURE_ERROR);
            return resp;
        }
        try {
            stmt = connection.prepareStatement(query);
            int i = 1;
            for (Object value : insertValues) {
                stmt.setObject(i, value);
                i++;
            }
            Integer result = stmt.executeUpdate();
            resp.setSQLResult(result);
            resp.setStatusCode(DBResponseCode.SUCCESS);
            return resp;
        } catch (Exception e) {
            LOGGER.error("Error inserting data into DB!", e);
            resp.setStatusCode(DBResponseCode.DB_ERROR);
            return resp;
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(connection);
        }
    }

    /**
     * Reloads the Apache DBCP.
     *
     * @throws SQLException If an error occurs reloading the DBCP.
     */
    public static void reloadConnectionPool() throws SQLException {
        connectionPool.close();
        connectionPool = new BasicDataSource();
        connectionPool.setDriverClassName("com.mysql.jdbc.Driver");
        connectionPool.setUsername("root");
        connectionPool.setPassword("");
        connectionPool.setUrl("jdbc:mysql://localhost:3306/tweetdb");
        connectionPool.addConnectionProperty("characterEncoding", "utf8");
        connectionPool.setInitialSize(10);
        connectionPool.setMaxOpenPreparedStatements(10);
        connectionPool.setMaxConnLifetimeMillis(1000 * 60 * 5);
        connectionPool.setMaxTotal(10);
    }
    
    public static DBResponse runCustomUpdate(String query, Object... parameters) {
        DBResponse resp = new DBResponse();
        PreparedStatement stmt = null;
        Connection connection;
        try {
            connection = acquireConnectionFromPool();
        } catch (SQLException sqle) {
            LOGGER.error("Failed to acquire connection!", sqle);
            resp.setStatusCode(DBResponseCode.CONNECTION_FAILURE_ERROR);
            return resp;
        }
        try {
            stmt = connection.prepareStatement(query);
            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }
            Integer result = stmt.executeUpdate();
            resp.setSQLResult(result);
            resp.setStatusCode(DBResponseCode.SUCCESS);
            return resp;
        } catch (Exception e) {
            resp.setStatusCode(DBResponseCode.DB_ERROR);
            LOGGER.error("Database error occurred whilst running query!", e);
            return resp;
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(connection);
        }
    }

    /**
     * Runs the given query against the database (update and insert queries only).
     *
     * @param query The query to run.
     * @return True if no error occurs, false otherwise.
     */
    public static DBResponse runQuery(String query) {
        DBResponse resp = new DBResponse();
        Statement stmt = null;
        Connection connection;
        try {
            connection = acquireConnectionFromPool();
        } catch (SQLException sqle) {
            LOGGER.error("Failed to acquire connection!", sqle);
            resp.setStatusCode(DBResponseCode.CONNECTION_FAILURE_ERROR);
            return resp;
        }
        try {
            stmt = connection.prepareStatement(query);
            Integer result = stmt.executeUpdate(query);
            resp.setSQLResult(result);
            resp.setStatusCode(DBResponseCode.SUCCESS);
            return resp;
        } catch (Exception e) {
            resp.setStatusCode(DBResponseCode.DB_ERROR);
            LOGGER.error("Database error occurred whilst running query!", e);
            return resp;
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(connection);
        }
    }
    
    public static boolean runParameterisedUpdateBatch(String query, ArrayList<Object[]> params) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = acquireConnectionFromPool();
            stmt = connection.prepareStatement(query);
            for (Object[] paramList : params) {
                for (int i = 0; i < paramList.length; i++) {
                    stmt.setObject(i + 1, paramList[i]);
                }
                stmt.addBatch();
            }
            stmt.executeBatch();
            return true;
        } catch (Exception e) {
            LOGGER.error("Error running batch update query!", e);
            if (stmt != null) {
                try {
                    LOGGER.error("Update batch size: " + params.size() + "     Update count: " + stmt.getUpdateCount());
                } catch (Exception e1) {
                    LOGGER.error("Error printing batch query info!", e1);
                }
            }
            return false;
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(connection);
        }
    }

    /**
     * Runs a set of update queries against the database.
     *
     * @param queries An arraylist of update queries.
     * @return True if the queries all successfully ran; false otherwise.
     */
    public static boolean runUpdateBatch(ArrayList<String> queries) {
        Statement stmt = null;
        Connection connection = null;
        try {
            connection = acquireConnectionFromPool();
            stmt = connection.createStatement();
            for (String q : queries) {
                stmt.addBatch(q);
            }
            stmt.executeBatch();
            return true;
        } catch (Exception e) {
            LOGGER.error("Error running batch update query!", e);
            if (stmt != null) {
                try {
                    LOGGER.error("Number of queries: " + queries.size() + "     Update count: " + stmt.getUpdateCount());
                } catch (Exception e1) {
                    LOGGER.error("Error printing batch query info!", e1);
                }
            }
            return false;
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(connection);
        }
    }
    
    protected static Connection acquireConnectionFromPool() throws SQLException {
        for (int i = 0; i < 10; i++) {
            try {
                Connection conn = connectionPool.getConnection();
                return conn;
            } catch (Exception e) {
                LOGGER.error("Failed to acquire database connection - retrying in 0.5 seconds.", e);
            }
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                
            }
        }
        throw new SQLException("Attempted to acquire database connection, failed 10 times - aborting operation.");
    }

    /**
     * Gets a database connection from the DBCP connection pool.
     *
     * @return A connection from the DBCP.
     * @throws SQLException If an error occurs.
     */
    protected static synchronized Connection getPoolConnection() throws SQLException {
        return acquireConnectionFromPool();
    }
    
}
