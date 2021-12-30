package Obmen.Util;

import java.sql.Connection;
import java.sql.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.SQLException;

public class ConnectionCreator {
    private static final Logger logger =  LogManager.getLogger();

    public Connection getPostgresConnection(){
        Connection connection = null;


        try {
            Class.forName("org.postgresql.Driver");

            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/ObmenTest","postgres", "123456");
        }
        catch (Exception ex){
            logger.error(ex);
        }

        return connection;
    }

}
