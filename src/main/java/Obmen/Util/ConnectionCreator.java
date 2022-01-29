package Obmen.Util;

import java.sql.Connection;
import java.sql.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.SQLException;

public class ConnectionCreator {
    private static final Logger logger =  LogManager.getLogger();
    public static String url;
    public static String user;
    public static String password;

    public Connection getPostgresConnection(){
        Connection connection = null;


        try {
            Class.forName("org.postgresql.Driver");

            connection = DriverManager.getConnection(
                    url,user, password);
        }
        catch (Exception ex){
            logger.error("ошибка при создании подключения к базе данных password", ex);
        }

        return connection;
    }

}
