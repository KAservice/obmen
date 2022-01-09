package Obmen;

import Obmen.Util.ConnectionCreator;
import Obmen.Util.DBProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Base64;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UDMUribOut {
    private static final Logger logger =  LogManager.getLogger();
    public String textXML = "";

    public boolean createDataOutTable(int idBase){
        boolean result = false;

        long maxID = DBProperties.getMaxIdTableIsm(idBase);

        try(Connection connection = new ConnectionCreator().getPostgresConnection();
            Statement statement = connection.createStatement()) {
            String query = "select * from  XTISM where  ID_XTISM > " + maxID;
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()){
                int operation = resultSet.getInt("OPERATION_XTISM");
                if(operation < 4){
                    //операции по изменению данных
                    int idExt = resultSet.getInt("IDEXT_BASE_XTISM");
                    if (idExt == idBase){
                        //в DataOut записываем квитанцию  это внешнее изменение
                        long idTableIzm = resultSet.getLong("ID_XTISM");
                        DBProperties.addRecordDataOut(idBase, idTableIzm, 2 ,0);
                    }
                    else {
                        //изменение данных
                        addInDataOutTable(resultSet, idBase);

                    }
                }
                else if(operation == 4 || operation == 5){
                    //команда отмены проведения или проведения документа
                    if (resultSet.getLong("IDEXT_BASE_XTISM") == idBase){
                        //в DataOut записываем квитанцию  это внешнее изменение
                        long idTableIzm = resultSet.getLong("ID_XTISM");
                        DBProperties.addRecordDataOut(idBase, idTableIzm, 2 ,0);
                    }
                    else {
                        ResultSet xSetupObmen = DBProperties.getXSetupObmen(idBase);
                        if (checkBaseRecordForObmen(xSetupObmen, resultSet, resultSet.getInt("IDBASE_XTISM"), idBase)){
                            //изменение данных
                            long idTableIzm = resultSet.getLong("ID_XTISM");
                            DBProperties.addRecordDataOut(idBase, idTableIzm, 1 ,0);
                        }
                    }
                }

            }

        }
        catch (SQLException ex){
            logger.error("ошибка в методе createDataOutTable", ex);
        }

        return result;
    }

    private void addInDataOutTable(ResultSet resultSet, int idBase) {
        boolean addDataOut = true;

        try {
            if (resultSet.getString("NAME_TABLE_XTISM").equalsIgnoreCase("SPRICE")) {
                ResultSet priceResultSet = DBProperties.getPrice(resultSet.getLong("VALUE_FIELD_ID_XTISM"));
                ResultSet tableTypePriceForObmen = DBProperties.getTableTypePriceForObmen(idBase);
                priceResultSet.next();
                while (tableTypePriceForObmen.next()){
                    if (tableTypePriceForObmen.getLong("IDTPRICE_XTPRICE_FOR_OBMEN") == priceResultSet.getLong("IDTYPE_PRICE")){
                        addDataOut = true;
                        break;
                    }
                }
            }

            ResultSet xSetupObmen = DBProperties.getXSetupObmen(idBase);
            xSetupObmen.next();
            if(xSetupObmen.getLong("OUTZPRICE_XSETUP_OBMEN") != 1){
                if (resultSet.getString("NAME_TABLE_XTISM").equalsIgnoreCase("SNACENKA")){
                    addDataOut = false;
                }
            }

            if (resultSet.getString("NAME_TABLE_XTISM").equalsIgnoreCase("LOG")){
                addDataOut = false;
            }
            if (resultSet.getString("NAME_TABLE_XTISM").equalsIgnoreCase("SETUP")){
                addDataOut = false;
            }
            if (resultSet.getString("NAME_TABLE_XTISM").equalsIgnoreCase("SARM")){
                addDataOut = false;
            }
            if (resultSet.getString("NAME_TABLE_XTISM").equalsIgnoreCase("SOBORUD")){
                addDataOut = false;
            }
            if (resultSet.getString("NAME_TABLE_XTISM").equalsIgnoreCase("SVESNOM")){
                addDataOut = false;
            }
            if (resultSet.getString("NAME_TABLE_XTISM").equalsIgnoreCase("SSCALE")){
                addDataOut = false;
            }
            if (resultSet.getString("NAME_TABLE_XTISM").equalsIgnoreCase("GURNALPLAT")){
                addDataOut = false;
            }


            if (resultSet.getString("NAME_TABLE_XTISM").equalsIgnoreCase("GALLDOC")){
                //проверим какие поля изменены, если только PRDOC то запись не выгружаем
                ResultSet tableIsmFields = DBProperties.getTableIsmFields(resultSet.getLong("ID_XTISM"));
                tableIsmFields.next();
                if(tableIsmFields.getString("FIELD_NAME_XTISM_FIELDS").equalsIgnoreCase("PRDOC")){
                    if(!tableIsmFields.next()){
                        addDataOut = false;
                    }
                }
            }

            if (resultSet.getString("NAME_TABLE_XTISM").equalsIgnoreCase("REM_GALLDOC")){
                //проверим какие поля изменены, если только PRDOC то запись не выгружаем
                ResultSet tableIsmFields = DBProperties.getTableIsmFields(resultSet.getLong("ID_XTISM"));
                tableIsmFields.next();
                if(tableIsmFields.getString("FIELD_NAME_XTISM_FIELDS").equalsIgnoreCase("PR_REM_GALLDOC")){
                    if(!tableIsmFields.next()){
                        addDataOut = false;
                    }
                }
            }

            if (resultSet.getString("NAME_TABLE_XTISM").equalsIgnoreCase("HOT_GALLDOC")){
                //проверим какие поля изменены, если только PRDOC то запись не выгружаем
                ResultSet tableIsmFields = DBProperties.getTableIsmFields(resultSet.getLong("ID_XTISM"));
                tableIsmFields.next();
                if(tableIsmFields.getString("FIELD_NAME_XTISM_FIELDS").equalsIgnoreCase("PR_HOT_GALLDOC")){
                    if(!tableIsmFields.next()){
                        addDataOut = false;
                    }
                }
            }

            if (!checkBaseRecordForObmen(xSetupObmen, resultSet, resultSet.getInt("IDBASE_XTISM"), idBase)){
                return;
            }

            if (DBProperties.getIdElement(idBase, resultSet.getLong("ID_XTISM")) == 0 && addDataOut){

                if(!DBProperties.addRecordDataOut(idBase, resultSet.getLong("ID_XTISM"), 1, 0)){
                    logger.info("Ошибка при добавлении записи в таблицу XDATAOUT");
                }

            }

        }
        catch (SQLException ex){
            logger.error("ошибка в методе addInDataOutTable", ex);
        }
    }

    private boolean checkBaseRecordForObmen(ResultSet xSetupObmen, ResultSet resultSet, int idBaseObject, int idBase){
        //проверка AllDoc
        boolean result = false;

        try {
            long xSetupObmenALLDOC_XSETUP_OBMEN = xSetupObmen.getLong("ALLDOC_XSETUP_OBMEN");
            //выгружаем все документы
            if (xSetupObmenALLDOC_XSETUP_OBMEN == 1){
                result = true;
            }
            else {//фильтрация по указанным базам
                //справочники выгружаем всегда даже если задана база либо не задана
                if (resultSet.getLong("TYPE_OBJECT_XTISM") == 1){
                    result = true;
                }
                if (resultSet.getLong("TYPE_OBJECT_XTISM") == 2){
                    if (idBaseObject == 0){
                        result = false;
                    }
                }
            }

            ResultSet tableBaseForObmen = DBProperties.getTableBaseForObmen(idBase);
            while (tableBaseForObmen.next()){
                if (idBaseObject == tableBaseForObmen.getLong("IDBASE_OBJECT_XBASE_FOR_OBMEN")){
                    result = true;
                    break;
                }
            }
        }
        catch (SQLException ex){
            logger.error("ошибка в методе checkBaseRecordForObmen", ex);
        }
        return result;
    }

    public boolean createXMLDoc(int idBase){
        boolean result = false;
        int idTecBase = 0;

        //база источник, текущая база
        try(Connection connection = new ConnectionCreator().getPostgresConnection();
            Statement statement = connection.createStatement()) {

            String query = "select setup.value_setup from setup where setup.id_setup=3";
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()){
                idTecBase = resultSet.getInt("VALUE_SETUP");
            }
        }
        catch (SQLException ex){
            logger.error(ex);
        }

        try {
            ResultSet tableDataOut = DBProperties.getTableDataOut(idBase);
            if (tableDataOut.next()){
                //заполним начальное значение ID_XDATA_OUT
                DBProperties.setFirstId(idBase, tableDataOut.getLong("ID_XDATA_OUT"));
            }
        }
        catch (SQLException ex){
            logger.error("ошибка в методе createXMLDoc", ex);
        }

        try {
            createXML("<data>");
            ResultSet tableDataOut = DBProperties.getTableDataOut(idBase);
            int numberTecRecord = 1;
            while (tableDataOut.next()){
                if (tableDataOut.getLong("TYPE_XDATA_OUT") == 2){
                    //выгружаем квитанцию
                    outputKvitan(tableDataOut.getInt("IDEXT_DATAOUT_XTISM"), numberTecRecord, idTecBase, idBase);
                }
                if (tableDataOut.getLong("TYPE_XDATA_OUT") == 1){
                    //выгружаем изменения
                    //изменения записи
                    if (tableDataOut.getLong("OPERATION_XTISM") < 4){
                        outputTableInXMLFile(tableDataOut, numberTecRecord, idTecBase, idBase);
                    }
                    //команда проведения или отмены проведения документа
                    if (tableDataOut.getLong("OPERATION_XTISM") == 4 || tableDataOut.getLong("OPERATION_XTISM") == 5){
                        outputComandDvReg(tableDataOut, idTecBase, numberTecRecord);
                    }
                }
                numberTecRecord++;
            }
        }
        catch (SQLException ex){
            logger.error("ошибка в методе createXMLDoc", ex);
        }
        createXML("</data>");
        return result;
    }

    private void outputKvitan(int idExtData, int numberTecRecord, int idTecBase, int idBase){
        String result = "";
        result += "<XDATA_OUT num=\"" + numberTecRecord + "\"";
        result += " type=\"2\"";
        result += " id_base_ist=\"" + idTecBase + "\"";
        result += " id_base_priem=\"" + idBase + "\"";
        result += "idext_data_out=\"" + idExtData + "\"> \n";
        result += "</XDATA_OUT>";
        createXML(result);
    }

    private void outputTableInXMLFile(ResultSet tableDataOut, int numberTecRecord, int idTecBase, int idBase) throws SQLException{
        String result = "";

        if (tableDataOut.getLong("OPERATION_XTISM") == 3){   //удаление записи
            outputDeleteRecordInXMLFile(tableDataOut, numberTecRecord, idTecBase);
            return;
        }

        //создаем узел с названием таблицы
        result += "<" + tableDataOut.getString("NAME_TABLE_XTISM") + " num=\""  + numberTecRecord + "\"";
        result += " type=\"1\"";    // вставка записи 1, подтверждение 2
        result += " oper=\"" + tableDataOut.getString("OPERATION_XTISM") + "\"";
        result += " name_pk=\"" + tableDataOut.getString("NAME_FIELD_ID_XTISM") + "\"";
        result += " value_pk=\"" + tableDataOut.getString("VALUE_FIELD_ID_XTISM") + "\"";
        result += " idext_base=\"" + idTecBase + "\"";
        result += " idext_data_out=\"" + tableDataOut.getString("ID_XDATA_OUT") + "\">";
        result += "\n";

        ResultSet tableIsmFields = DBProperties.getTableIsmFields(tableDataOut.getLong("IDTISM_XDATA_OUT"));

        while (tableIsmFields.next()){
            String fieldName = tableIsmFields.getString("FIELD_NAME_XTISM_FIELDS");
            String fieldValue = "";

            if (!checkFieldForOutput(fieldName, fieldValue, idBase, tableDataOut)){
                continue;
            }

            //необходимо учитывать что поле может бытьSaveFileXML
            // ДЛИННАЯ СТРОКА
            // BLOB
            //в этом случае сюда помещаем только имя файла в value_field
            //либо бинарное представление файла

            String tempResult = "";
            tempResult += "<" + fieldName;
            if (tableIsmFields.getLong("TYPE_XTISM_FIELDS") == 2){ //длинная строка
                tempResult += " t=\"2\"";
                fieldValue = getTextValueFieldLongString(
                        tableDataOut.getString("NAME_TABLE_XTISM"), tableDataOut.getString("NAME_FIELD_ID_XTISM"),
                        tableDataOut.getLong("VALUE_FIELD_ID_XTISM"), fieldName);
            }

            else if (tableIsmFields.getLong("TYPE_XTISM_FIELDS") == 3){
                tempResult += " t=\"3\"";
                fieldValue = getTextValueFieldBlob(
                        tableDataOut.getString("NAME_TABLE_XTISM"), tableDataOut.getString("NAME_FIELD_ID_XTISM"),
                        tableDataOut.getLong("VALUE_FIELD_ID_XTISM"), fieldName);
            }

            else {
                tempResult += " t=\"1\"";
                fieldValue = tableIsmFields.getString("NEW_VALUE_XTISM_FIELDS");
            }

            tempResult += " v=\"" + fieldValue + "\"/> \n";
            result += tempResult;
        }
        result += "</" + tableDataOut.getString("NAME_TABLE_XTISM") + ">";
        createXML(result);
    }

    private void outputDeleteRecordInXMLFile(ResultSet tableDataOut, int numberTecRecord, int idTecBase){
        String result = "";
        try {
            //создаем узел с названием таблицы
            result += "<" + tableDataOut.getString("NAME_TABLE_XTISM") + " num=\"" + numberTecRecord + "\"";
            result += " type=\"1\"";
            result += " oper=\"" + tableDataOut.getString("OPERATION_XTISM") + "\"";
            result += " name_pk=\"" + tableDataOut.getString("OPERATION_XTISM") + "\"";
            result += " value_pk=\"" + tableDataOut.getString("OPERATION_XTISM") + "\"";
            result += " idext_base=\"" + idTecBase + "\"";
            result += " idext_data_out=\"" + tableDataOut.getString("OPERATION_XTISM") + "\"> \n";
            result += "</" + tableDataOut.getString("NAME_TABLE_XTISM") + ">";
        }
        catch (SQLException ex){
            logger.error("ошибка в методе outputDeleteRecordInXMLFile", ex);
        }

        createXML(result);
    }

    private void outputComandDvReg(ResultSet tableDataOut, int idTecBase, int numberTecRecord){
        String result = "";

        try {
            result += "<" + tableDataOut.getString("NAME_TABLE_XTISM") + " num=\"" + numberTecRecord + "\"";
            result += " type=\"1\"";
            result += "oper=\"" + tableDataOut.getString("OPERATION_XTISM") + "\"";
            result += " name_pk=\"" + tableDataOut.getString("NAME_FIELD_ID_XTISM") + "\"";
            result += " value_pk=\"" + tableDataOut.getString("VALUE_FIELD_ID_XTISM") + "\"";
            result += " idext_base=\"" + idTecBase + "\"";
            result += " idext_data_out=\"" + tableDataOut.getString("ID_XDATA_OUT") + "\">\n";
            result += "</" + tableDataOut.getString("NAME_TABLE_XTISM") + ">";
        }
        catch (SQLException ex){
            logger.error("ошибка в методе outputComandDvReg", ex);
        }
        createXML(result);
    }

    private boolean checkFieldForOutput(String fieldName, String tableName, int idBase, ResultSet tableDataOut){
        boolean result = false;

        //не выгружаем поля IDEXT_BASE и IDEXT_DOUT
        if (fieldName.equalsIgnoreCase("IDEXT_BASE_" + tableName)){
            return result;
        }
        if (fieldName.equalsIgnoreCase("IDEXT_DOUT_" + tableName)){
            return result;
        }

        if (fieldName.equalsIgnoreCase("PRDOC")){
            return result;
        }
        if (fieldName.equalsIgnoreCase("PR_REM_GALLDOC")){
            return result;
        }
        if (fieldName.equalsIgnoreCase("PR_HOT_GALLDOC")){
            return result;
        }
        if (fieldName.equalsIgnoreCase("IDPARTPERT")){//Партия в док Перемещение  такого поля нет
            return result;
        }
        if (fieldName.equalsIgnoreCase("FL_CHANGE_NOM")){//  не надо выгружать флаг изменения цен
            return result;
        }


        try {
            ResultSet xSetupObmen = DBProperties.getXSetupObmen(idBase);
            //проверим надо ли выгружать закупочные цены
            xSetupObmen.next();
            if (!(xSetupObmen.getLong("OUTZPRICE_XSETUP_OBMEN") == 1)){
                //фильтрация по закупочным ценам,
                //не надо выгружать закупочные цены
                //партии
                if (fieldName.equalsIgnoreCase("PRICEZPART")){
                    return result;
                }
                //Ввод остатков
                if (fieldName.equalsIgnoreCase("PRICEOSNT")){
                    return result;
                }
                if (fieldName.equalsIgnoreCase("SUMOSNT")){
                    return result;
                }
                //Акт списания
                if (fieldName.equalsIgnoreCase("PRICESPNT")){
                    return result;
                }
                if (fieldName.equalsIgnoreCase("SUMSPNT")){
                    return result;
                }
                //Возврат от покупателя
                if (fieldName.equalsIgnoreCase("PRICE_DVPOKT")){
                    return result;
                }
                if (fieldName.equalsIgnoreCase("SUM_DVPOKT")){
                    return result;
                }
                if (fieldName.equalsIgnoreCase("SUM_DVPOK")){
                    return result;
                }


                if (tableName.equalsIgnoreCase("GALLDOC")){
                    String dockType = "";
                    String query = "select TDOC from  GALLDOC where IDDOC=" + tableDataOut.getString("VALUE_FIELD_ID_XTISM");
                    try(Connection connection = new ConnectionCreator().getPostgresConnection();
                        Statement statement = connection.createStatement()) {
                        ResultSet resultSet = statement.executeQuery(query);
                        if (resultSet.next()){
                            dockType = resultSet.getString("TDOC");
                        }
                    }
                    catch (SQLException ex){
                        logger.error(ex);
                    }
                    if (dockType.equalsIgnoreCase("OSN")){
                        if (fieldName.equalsIgnoreCase("SUMDOC")){
                            return result;
                        }
                    }
                    if (dockType.equalsIgnoreCase("SPN")){
                        if (fieldName.equalsIgnoreCase("SUMDOC")){
                            return result;
                        }
                    }
                    if (dockType.equalsIgnoreCase("PER")){
                        if (fieldName.equalsIgnoreCase("SUMDOC")){
                            return result;
                        }
                    }
                }
            }
        }
        catch (SQLException ex){
            logger.error("ошибка в методе checkFieldForOutput", ex);
        }
        result = true;
        return result;
    }

    private String getTextValueFieldLongString(String tableName, String pkName, long pkValue, String fieldName){
        String result = "";
        String query = "select " + fieldName + " from " + tableName + " where " + pkName + " = " + pkValue;

        try(Connection connection = new ConnectionCreator().getPostgresConnection();
            Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()){
                result = resultSet.getString(fieldName);
            }
        }
        catch (SQLException ex){
            logger.error("ошибка в методе getTextValueFieldLongString", ex);
        }
        return result;
    }

    private String getTextValueFieldBlob(String tableName, String pkName, long pkValue, String fieldName){
        String result = "";
        String query = "select " + fieldName + " from " + tableName + " where " + pkName + " = " + pkValue;;

        try(Connection connection = new ConnectionCreator().getPostgresConnection();
            Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);

            if (resultSet.next()){
                byte[] bytes = resultSet.getBytes(fieldName);
                result = Base64.getEncoder().encodeToString(bytes);
            }
        }
        catch (SQLException ex){
            logger.error(ex);
        }
        return result;
    }

    public void createXML(String subtext){
        textXML = textXML + subtext + "\n";
    }
}