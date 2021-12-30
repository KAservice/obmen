package Obmen.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DBProperties {
    private static final Logger logger =  LogManager.getLogger();

    public static long getMaxIdTableIsm(long idBase){

        String query = "select max(IDTISM_XDATA_OUT) AS IDMAX from XDATA_OUT where IDBASE_XDATA_OUT=" + idBase;
        long result = 0;
        try(Connection connection = new ConnectionCreator().getPostgresConnection();
            Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()){
                result = resultSet.getLong("IDMAX");
            }
        }
        catch (SQLException ex){
            logger.error("ошибка в методе getMaxIdTableIsm", ex);
        }
        return result;
    }

    public static boolean addRecordDataOut(long idBase, long idTableIzm, long type, long resultDB){
        boolean result = false;
        String query = "INSERT INTO XDATA_OUT (IDTISM_XDATA_OUT, IDBASE_XDATA_OUT, TYPE_XDATA_OUT, RESULT_XDATA_OUT) VALUES (" + idTableIzm + ", " + idBase + ", " + type + ", " + resultDB + ")";
        logger.info("запрос записи в xdataout - {}", query);
        try(Connection connection = new ConnectionCreator().getPostgresConnection();
            Statement statement = connection.createStatement()) {
            statement.executeQuery(query);
            result = true;
        }
        catch (SQLException ex){
            logger.error("ошибка в методе addRecordDataOut", ex);
        }
        return result;
    }

    public static ResultSet getPrice(long idPrice) throws SQLException{
        ResultSet resultSet = null;
        String query = "select IDTYPE_PRICE from SPRICE where ID_PRICE=" + idPrice;
        Connection connection = new ConnectionCreator().getPostgresConnection();
        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery(query);

        return resultSet;
    }

    public static ResultSet getTableTypePriceForObmen(int baseID) throws SQLException{
        ResultSet resultSet = null;
        String query = "select * from XTPRICE_FOR_OBMEN where IDBASE_OBMEN_XTPRICE_FOR_OBMEN=" + baseID;
        Connection connection = new ConnectionCreator().getPostgresConnection();
        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery(query);

        return resultSet;
    }

    public static ResultSet getXSetupObmen(int baseID) throws SQLException{
        ResultSet resultSet = null;
        String query = "select * from XSETUP_OBMEN where IDBASE_OBMEN_XSETUP_OBMEN=" + baseID;
        Connection connection = new ConnectionCreator().getPostgresConnection();
        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery(query);

        return resultSet;
    }

    public static ResultSet getTableIsmFields(long idField) throws SQLException{
        ResultSet resultSet = null;
        String query = "select * from XTISM_FIELDS where IDXTISM_XTISM_FIELDS=" + idField;
        Connection connection = new ConnectionCreator().getPostgresConnection();
        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery(query);


        return resultSet;
    }

    public static ResultSet getTableBaseForObmen(int baseID) throws SQLException{
        ResultSet resultSet = null;
        String query = "select * from XBASE_FOR_OBMEN where IDBASE_OBMEN_XBASE_FOR_OBMEN=" + baseID;
        Connection connection = new ConnectionCreator().getPostgresConnection();
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

        return resultSet;
    }

    public static long getIdElement(int idBase, long idTableIsm){
        long result = 0;
        String query = "select ID_XDATA_OUT from XDATA_OUT where IDTISM_XDATA_OUT=" + idTableIsm + " and IDBASE_XDATA_OUT=" + idBase;

        try(Connection connection = new ConnectionCreator().getPostgresConnection();
            Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()){
                result = resultSet.getLong("ID_XDATA_OUT");
            }
        }
        catch (SQLException ex){
            logger.error("ошибка в методе getIdElement", ex);
        }
        return result;
    }

    public static void setFirstId(int idBase, long idFirst){
        String query = "update XSETUP_OBMEN set IDFIRST_DO_XSETUP_OBMEN=" + idFirst + " where IDBASE_OBMEN_XSETUP_OBMEN=" + idBase;
        logger.info("запрос в методе setFirstId {}", query);

        try(Connection connection = new ConnectionCreator().getPostgresConnection();
            Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        }
        catch (SQLException ex){
            logger.error("ошибка в методе setFirstId", ex);
        }
    }

    public static ResultSet getTableDataOut(int idBase) throws SQLException{
        ResultSet resultSet = null;
        String query = "select * from XDATA_OUT left outer join XTISM on IDTISM_XDATA_OUT=ID_XTISM where  (RESULT_XDATA_OUT is null or RESULT_XDATA_OUT<>1) and IDBASE_XDATA_OUT=" + idBase;
        Connection connection = new ConnectionCreator().getPostgresConnection();
        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery(query);

        return resultSet;
    }
}
