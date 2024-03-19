// Copyright (c) 2021 Promineo Tech

package recipes.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import recipes.exception.DbException;

/**
 * This class returns a new connection when you call the static
 * {@link #getConnection()} method. To use this class, the schema must exist in
 * the MySQL database. It is best to minimize the allowed access by creating a
 * user with privileges granted to a single schema. If you use the root user,
 * you open yourself to errors in schemas that you didn't intend.
 * 
 * @author Promineo
 *
 */
public class DbConnection {
  /* This is the name of the schema. */
  private static final String SCHEMA = "recipes";

  /* This is the user name used to connect to the schema. */
  private static final String USER = "recipes";

  /* This is the password used to connect to the schema. */
  private static final String PASSWORD = "recipes";

  /*
   * This is the host name of the running MySQL server. "localhost" is the name
   * of the machine that runs the Java application.
   */
  private static final String HOST = "localhost";

  /* This is the port number. The default port for MySQL is 3306. */
  private static final int PORT = 3306;

  /**
   * 
   * @return Returns a Connection object if successful. Otherwise, the method
   *         throws an exception.
   * @throws DbException Thrown if an error occurs establishing a connection
   *         with the MySQL server and schema.
   */
  public static Connection getConnection() {
    /* This is the JDBC connection string. */
    String url =
        String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s&useSSL=false",
            HOST, PORT, SCHEMA, USER, PASSWORD);

    try {
      /*
       * DriverManager is the JDBC class that manages drivers and connections.
       */
      Connection conn = DriverManager.getConnection(url);
      System.out.println("Successfully obtained connection!");
      return conn;
    } catch (SQLException e) {
      System.out.println("Error getting connection.");
      throw new DbException(e);
    }
  }
}
