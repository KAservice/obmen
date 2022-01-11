package Obmen;
import Obmen.BDEntities.FieldEntity;
import Obmen.BDEntities.RowEntity;
import Obmen.Util.ConnectionCreator;
import Obmen.Util.XMLProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Base64;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.List;

public class UDMUribIn {
    private static final Logger logger =  LogManager.getLogger();

    public void updateDataInfBase(String fileURL){
        File xmlFile = new File(fileURL);
        String xmlText = "";
        String line;

        try(BufferedReader reader = new BufferedReader(new FileReader(xmlFile))) {
            while ((line = reader.readLine()) != null){
                xmlText = xmlText + line + "\s";
            }
        }
        catch (Exception ex){
            logger.error(ex);
        }
        updateInfBase(xmlText);
    }

    public void updateInfBase(String xmlText){
        List<RowEntity> rowList = XMLProperties.getRowList(xmlText);

        for (RowEntity row : rowList){
            int type = row.getType();
            if (type == 1){
                int operation = row.getOperationNum();
                switch (operation) {
                    case 1 : { //вставка
                        insertRecord(row);
                    }
                    break;

                    case 2 : { //редактирование
                        editRecord(row);
                    }
                    break;
                    case 3 : { //удаление
                        deleteRecord(row);
                    }
                    break;
                    case 4 : { //отмена проведения документа
                        runCommandCancelDvRegDoc(row);
                    }
                    break;
                    case 5 : { //проведение документа
                        runCommandDvRegDoc(row);
                    }
                    break;
                    default:
                        break;
                }
            }
            else if (type == 2){
                inputKvitan(row);
            }
        }
    }

    public void insertRecord(RowEntity row){
        String tableName = row.getName();
        String namePK = row.getNamePK();
        long valuePK = row.getValuePK();
        String query= "";

        String checkQuery = "SELECT * FROM " + tableName + " WHERE " + namePK + " = " + valuePK;

        try(Connection connection = new ConnectionCreator().getPostgresConnection();
            Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(checkQuery);
            if (!resultSet.next()){
                query = createTextZaprosInsert(row);
            }
            else {
                query = createTextZaprosEdit(row);
            }
        }
        catch (SQLException ex){
            logger.error(ex);
        }

        try(Connection connection = new ConnectionCreator().getPostgresConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            List<FieldEntity> fieldList = row.getData();

            int i = 1;
            for (FieldEntity field : fieldList){
                String fieldName = field.getName();
                if (!checkFieldForInsert(fieldName)){
                    continue;
                }
                String fieldValue = field.getValue();
                int fieldType = field.getType();
                if (fieldType == 3){ //BLOB
                    byte[] decodedBlob = Base64.getDecoder().decode(fieldValue);
                    preparedStatement.setBytes(i++, decodedBlob);
                }
                else {
                    preparedStatement.setString(i++, fieldValue);
                }
            }
            preparedStatement.executeUpdate();
        }
        catch (SQLException ex){

            logger.error(ex);
        }

    }

    public void editRecord(RowEntity row){
        String tableName = row.getName();
        String namePK = row.getNamePK();
        long valuePK = row.getValuePK();
        String query= "";

        String checkQuery = "SELECT * FROM " + tableName + " WHERE " + namePK + " = " + valuePK;

        try(Connection connection = new ConnectionCreator().getPostgresConnection();
            Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(checkQuery);
            if (!resultSet.next()){
                query = createTextZaprosInsert(row);
            }
            else {
                query = createTextZaprosEdit(row);
            }
        }
        catch (SQLException ex){
            logger.error(ex);
        }

        try(Connection connection = new ConnectionCreator().getPostgresConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            List<FieldEntity> fieldList = row.getData();

            int i = 1;
            for (FieldEntity field : fieldList){
                String fieldName = field.getName();
                if (!checkFieldForInsert(fieldName)){
                    continue;
                }
                String fieldValue = field.getValue();
                int fieldType = field.getType();
                if (fieldType == 3){ //BLOB
                    byte[] decodedBlob = Base64.getDecoder().decode(fieldValue);
                    preparedStatement.setBytes(i++, decodedBlob);
                }
                else {
                    preparedStatement.setString(i++, fieldValue);
                }
            }
            preparedStatement.executeUpdate();
        }
        catch (SQLException ex){
            logger.error(ex);
        }
    }

    public void deleteRecord(RowEntity row){
        String tableName = row.getName();
        String PKName = row.getNamePK();
        long PKValue = row.getValuePK();

        String idExtBaseName = "IDEXT_BASE_" + tableName;
        int idExtBaseValue = row.getIdBase();

        String idExtDataOutName = "IDEXT_DOUT_" + tableName;
        long idExtDataOutValue = row.getIdBaseDataOut();

        String checkQuery = "SELECT * FROM " + tableName + " WHERE " + PKName + " = '" + PKValue + "'";
        String deleteQuery = "DELETE FROM " + tableName + " WHERE " + PKName + " = '" + PKValue + "'";
        String changeQuery = "update xtism set idext_base_xtism=" + idExtBaseValue +
                ", idext_dataout_xtism=" + idExtDataOutValue +
                " where  name_table_xtism='" + tableName + "'" +
                " and operation_xtism=3 and xtism.value_field_id_xtism=" + PKValue;
        logger.info("изменение таблицы xtizm = {}", changeQuery);

        try(Connection connection = new ConnectionCreator().getPostgresConnection();
            Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(checkQuery);
            if (!resultSet.next()){ // запись не найдена

            }
            else {
                try(Connection deleteConnection = new ConnectionCreator().getPostgresConnection();//удаляем запись
                    Statement deleteStatement = deleteConnection.createStatement()) {
                    deleteStatement.executeUpdate(deleteQuery);
                }
                catch (SQLException ex){
                    logger.error(ex);
                }
                try(Connection changeConnection = new ConnectionCreator().getPostgresConnection();//запись о удалении в таблицу изменений
                    Statement changeStatement = changeConnection.createStatement()) {
                    changeStatement.executeUpdate(changeQuery);
                }
                catch (SQLException ex){
                    logger.error(ex);
                }
            }
        }
        catch (SQLException ex){
            logger.error(ex);
        }

    }

    private void inputKvitan(RowEntity row){
        long idExtDataOut = row.getIdBaseDataOut();
        String query = "select * from XDATA_OUT where ID_XDATA_OUT=" + idExtDataOut;
        String updateQuery = "UPDATE XDATA_OUT set RESULT_XDATA_OUT=1  where ID_XDATA_OUT=" + idExtDataOut;

        try(Connection connection = new ConnectionCreator().getPostgresConnection();
            Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()){
                try(Connection updateConnection = new ConnectionCreator().getPostgresConnection();
                    Statement updateStatement = updateConnection.createStatement()) {
                    updateStatement.executeUpdate(updateQuery);
                }
                catch (SQLException ex){
                    logger.error("ошибка в методе inputKvitan в запросе update", ex);
                }
            }
        }
        catch (SQLException ex){
            logger.error("ошибка в методе inputKvitan", ex);
        }
    }

    public void runCommandCancelDvRegDoc(RowEntity row){
        String tableName = row.getName();
        int idExtBaseValue = row.getIdBase();
        long idExtDataOutValue = row.getIdBaseDataOut();
        String namePK = row.getNamePK();
        long valuePK = row.getValuePK();
        String docType = "";

        if(namePK.equalsIgnoreCase("IDDOC")){
            docType = getTypeDoc(row);

        }

        if(namePK.equalsIgnoreCase("ID_REM_GALLDOC")){

        }

        if(namePK.equalsIgnoreCase("ID_HOT_GALLDOC")){

        }
    }

    public void runCommandDvRegDoc(RowEntity row){

    }

    public String createTextZaprosInsert(RowEntity row){

        String tableName = row.getName();
        String idExtBaseName = "IDEXT_BASE_" + tableName;
        int idExtBaseValue = row.getIdBase();

        String idExtDataOutName = "IDEXT_DOUT_" + tableName;
        long idExtDataOutValue = row.getIdBaseDataOut();

        String fields = idExtBaseName + ", " + idExtDataOutName;
        String values = idExtBaseValue + ", " + idExtDataOutValue;

        List<FieldEntity> fieldList = row.getData();
        for (FieldEntity fieldEntity : fieldList){
            String fieldName = fieldEntity.getName();
            String domainName = fieldEntity.getDomain();
            if (checkFieldForInsert(fieldName)){
                fields = fields + ", " + fieldName;
                values = values + ", ? :: " + domainName;
            }
        }

        String query = "INSERT INTO " + tableName + " (" + fields + ") " + " VALUES (" + values + ")";
        logger.info(query);

        return query;
    }

    public String createTextZaprosEdit(RowEntity row){
        String tableName = row.getName();
        String PKName = row.getNamePK();
        long PKValue = row.getValuePK();
        String idExtBaseName = "IDEXT_BASE_" + tableName;
        int idExtBaseValue = row.getIdBase();

        String idExtDataOutName = "IDEXT_DOUT_" + tableName;
        long idExtDataOutValue = row.getIdBaseDataOut();

        String fields = idExtBaseName + " = " + idExtBaseValue + ", " + idExtDataOutName + " = " + idExtDataOutValue;

        List<FieldEntity> fieldList = row.getData();
        for (FieldEntity fieldEntity : fieldList){
            String fieldName = fieldEntity.getName();
            if (checkFieldForInsert(fieldName)){
                fields = fields + ", " + fieldName + " = ?";
            }
        }

        String query = "UPDATE " + tableName + " SET " + fields + " WHERE " + PKName + " = " + PKValue;
        logger.info(query);

        return query;
    }

    public boolean checkFieldForInsert(String fieldName){
        boolean result = false;
        if (fieldName.equals("PRDOC")){
            return result;
        }
        if (fieldName.equals("IDPARTPERT")){
            return result;
        }
        return true;
    }

    private String getTypeDoc(RowEntity row){
        String result = "";
        String namePK = row.getNamePK();
        long valuePK = row.getValuePK();
        String query = "";

        if (namePK.equalsIgnoreCase("IDDOC")){
            query = "select TDOC AS TYPEDOC from GALLDOC where IDDOC=" + valuePK;
        }

        if (!query.equals("")){
            try(Connection connection = new ConnectionCreator().getPostgresConnection();
                Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet.next()){
                    result = resultSet.getString("TYPEDOC");
                }
            }
            catch (SQLException ex) {
                logger.error(ex);
            }
        }
        return result;
    }

    private String getTypeDocRemontSystem(RowEntity row){
        String result = "";
        String namePK = row.getNamePK();
        long valuePK = row.getValuePK();
        String query = "";

        if (namePK.equalsIgnoreCase("ID_REM_GALLDOC")){
            query = "select TDOC_REM_GALLDOC AS TYPEDOC from REM_GALLDOC where ID_REM_GALLDOC=" + valuePK;
        }

        if (!query.equals("")){
            try(Connection connection = new ConnectionCreator().getPostgresConnection();
                Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet.next()){
                    result = resultSet.getString("TYPEDOC");
                }
            }
            catch (SQLException ex) {
                logger.error(ex);
            }
        }
        return result;
    }

    private String getTypeDocHotelSystem(RowEntity row){
        String result = "";
        String namePK = row.getNamePK();
        long valuePK = row.getValuePK();
        String query = "";

        if (namePK.equalsIgnoreCase("ID_HOT_GALLDOC")){
            query = "select TDOC_HOT_GALLDOC AS TYPEDOC from HOT_GALLDOC where ID_HOT_GALLDOC=" + valuePK;
        }

        if (!query.equals("")){
            try(Connection connection = new ConnectionCreator().getPostgresConnection();
                Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet.next()){
                    result = resultSet.getString("TYPEDOC");
                }
            }
            catch (SQLException ex) {
                logger.error(ex);
            }
        }
        return result;
    }
}