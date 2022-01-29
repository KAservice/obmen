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
                String fieldDomain = field.getDomain();
                if (!checkFieldForInsert(fieldName)){
                    continue;
                }
                String fieldValue = field.getValue();
                int fieldType = field.getType();
                if (fieldType == 3){ //BLOB
                    byte[] decodedBlob = Base64.getDecoder().decode(fieldValue);
                    preparedStatement.setBytes(i++, decodedBlob);
                }
                else if (fieldDomain.equalsIgnoreCase("domain_int")){
                    if (fieldValue.equals("")){
                        preparedStatement.setInt(i++, 0);
                    }
                    else
                        preparedStatement.setInt(i++, Integer.parseInt(fieldValue));

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
                " and operation_xtism=3 and value_field_id_xtism=" + PKValue;
        String deleteKvitanQuery = "insert into xtism  (operation_xtism, name_table_xtism, name_field_id_xtism," +
                " value_field_id_xtism, idext_base_xtism, idext_dataout_xtism  ) values ( 3, '" + tableName + "', '" + PKName + "', " + PKValue +
                ", " + idExtBaseValue + ", " + idExtDataOutValue + ")";
        logger.info("изменение таблицы xtizm = {}", deleteKvitanQuery);

        try(Connection connection = new ConnectionCreator().getPostgresConnection();
            Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(checkQuery);
            if (!resultSet.next()){ // запись не найдена
                try (Connection xtismConnection = new ConnectionCreator().getPostgresConnection();//удаляем запись
                     Statement xtismStatement = xtismConnection.createStatement()){
                    xtismStatement.executeUpdate(deleteKvitanQuery);
                }
                catch (SQLException ex){
                    logger.error(ex);
                }
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
        String procedureName = "";

        if(namePK.equalsIgnoreCase("IDDOC")){
            docType = getTypeDoc(row);
            procedureName = getNameProcedureCancelDvReg(docType);
        }

        if(namePK.equalsIgnoreCase("ID_REM_GALLDOC")){
            docType = getTypeDocRemontSystem(row);
            procedureName = getNameProcedureCancelDvRegRemontSystem(docType);
        }

        if(namePK.equalsIgnoreCase("ID_HOT_GALLDOC")){
            docType = getTypeDocHotelSystem(row);
            procedureName = getNameProcedureCancelDvRegHotelSystem(docType);
        }

        if (procedureName.equals("")){
            logger.error("нет процедуры для отмены проведения документа = {}", tableName);
            try(Connection connection = new ConnectionCreator().getPostgresConnection();
                Statement statement = connection.createStatement()) {
                String procedureQuery = "SELECT GLPROC_ADD_DVREGDOC_IN_TISM (" + valuePK + " :: domain_fk_key, 0 :: domain_fk_key,4 :: domain_int, " + idExtBaseValue + " :: domain_fk_key, " + idExtDataOutValue + " :: domain_fk_key)";
                statement.execute(procedureQuery);
            }
            catch (SQLException ex){
                logger.error("ошибка при отмене проведения документа для которого нет своей процедуры", ex);
            }
        }
        else {

            String procedureQuery = "SELECT " + procedureName + " (" + valuePK + " :: domain_iddoc, 0 :: domain_bool, " + idExtBaseValue + " :: domain_fk_key," + idExtDataOutValue + " :: domain_fk_key)";
            logger.info(procedureQuery);
            try (Connection connection = new ConnectionCreator().getPostgresConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(procedureQuery);
                logger.info("отмена проведения документа = {} успешно завершено", tableName);
            } catch (SQLException ex) {
                logger.error("ошибка при отмене проведения документа", ex);
            }
        }

    }

    public void runCommandDvRegDoc(RowEntity row){
        String tableName = row.getName();
        int idExtBaseValue = row.getIdBase();
        long idExtDataOutValue = row.getIdBaseDataOut();
        String namePK = row.getNamePK();
        long valuePK = row.getValuePK();
        String docType = "";
        String procedureName = "";

        if(namePK.equalsIgnoreCase("IDDOC")){
            docType = getTypeDoc(row);
            procedureName = getNameProcedureDvReg(docType);
        }

        if(namePK.equalsIgnoreCase("ID_REM_GALLDOC")){
            docType = getTypeDocRemontSystem(row);
            procedureName = getNameProcedureDvRegRemontSystem(docType);
        }

        if(namePK.equalsIgnoreCase("ID_HOT_GALLDOC")){
            docType = getTypeDocHotelSystem(row);
            procedureName = getNameProcedureDvRegHotelSystem(docType);
        }

        if (procedureName.equals("")){
            logger.error("нет процедуры для проведения документа = {}", tableName);
            try(Connection connection = new ConnectionCreator().getPostgresConnection();
                Statement statement = connection.createStatement()) {
                String procedureQuery = "SELECT GLPROC_ADD_DVREGDOC_IN_TISM (" + valuePK + " :: domain_fk_key, 0 :: domain_fk_key,5 :: domain_int, " + idExtBaseValue + " :: domain_fk_key, " + idExtDataOutValue + " :: domain_fk_key)";
                statement.execute(procedureQuery);
                logger.info("проведение документа = {} успешно завершено", tableName);
            }
            catch (SQLException ex){
                logger.error("ошибка при проведении документа для которого нет своей процедуры", ex);
            }
        }
        else {

            String procedureQuery = "SELECT " + procedureName + " (" + valuePK + " :: domain_iddoc, 0 :: domain_bool, " + idExtBaseValue + " :: domain_fk_key, " + idExtDataOutValue + " :: domain_fk_key)";
            logger.info(procedureQuery);
            try (Connection connection = new ConnectionCreator().getPostgresConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(procedureQuery);
                logger.info("проведение документа = {} успешно завершено", tableName);
            } catch (SQLException ex) {
                logger.error("ошибка при проведении документа", ex);
            }
        }
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
            String domainName = fieldEntity.getDomain();
            String fieldName = fieldEntity.getName();
            if (checkFieldForInsert(fieldName)){
                fields = fields + ", " + fieldName + " = ? :: " + domainName;
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

    private String getNameProcedureCancelDvReg(String docType){
        String result = "";

        if (docType.equals("CHK")){result="DOC_CHK_CANCEL_DVREG";}
        if (docType.equals("PRN")){result="DOC_PRN_CANCEL_DVREG";}//{Text="Прих. накл.";}
        if (docType.equals("SCH")){result="DOC_SCH_CANCEL_DVREG";}//{Text="Счет на оплату";}
        if (docType.equals("OSN")){result="DOC_OSN_CANCEL_DVREG";}//{Text="Остатки номенкл.";}
        if (docType.equals("PER")){result="DOC_PER_CANCEL_DVREG";}//{Text="Перемещение";}
        if (docType.equals("REA")){result="DOC_REA_CANCEL_DVREG";}//{Text="Реализация";}
        if (docType.equals("SPN")){result="DOC_SPN_CANCEL_DVREG";}//{Text="Акт списания";}
        if (docType.equals("VPO")){result="DOC_VPO_CANCEL_DVREG";}//{Text="Возврат поставщику";}
        if (docType.equals("PKO")){result="DOC_PKO_CANCEL_DVREG";}//{Text="Прих. касс. ордер";}
        if (docType.equals("RKO")){result="DOC_RKO_CANCEL_DVREG";}//{Text="Расх. касс. ордер";}
        if (docType.equals("PLP")){result="DOC_PLP_CANCEL_DVREG";}//{Text="Платежное поручение";}
        if (docType.equals("PNS")){result="DOC_PNS_CANCEL_DVREG";}//{Text="Поступление на счет";}
        if (docType.equals("INV")){result="DOC_INV_CANCEL_DVREG";}//{Text="Инвентаризация";}
        if (docType.equals("REPKKM")){result="DOC_REPKKM_CANCEL_DVREG";}//{Text="Отчет ККМ";}
        if (docType.equals("OSTVS")){result="DOC_OSTVS_CANCEL_DVREG";}//{Text="Корр. взаиморасчетов";}
        if (docType.equals("SCHFACT")){result="DOC_SCHFACT_CANCEL_DVREG";}//{Text="Счет-фактура";}
        if (docType.equals("REALROZN")){result="DOC_REALROZN_CANCEL_DVREG";}//{Text="Реализация розн.";}
        if (docType.equals("PKOROZN")){result="DOC_PKOROZN_CANCEL_DVREG";}//{Text="Приход нал.";}
        if (docType.equals("RKOROZN")){result="DOC_RKOROZN_CANCEL_DVREG";}//{Text="Расход нал.";}

        if (docType.equals("VPOK")){result="DOC_VPOK_CANCEL_DVREG";}//{Text="Возврат покупателя";}

        if (docType.equals("ISMPRICE")){result="DOC_ISMPRICE_CANCEL_DVREG";}//{Text="Переоценка";}
        if (docType.equals("SPOTROST")){result="DOC_SPOTROST_CANCEL_DVREG";}//{Text="Списание отр. остатков";}
        if (docType.equals("REV")){result="DOC_REV_CANCEL_DVREG";}//{Text="Ревизия";}

        if (docType.equals("SBKOMPL")){result="DOC_SBKOMPL_CANCEL_DVREG";}//
        if (docType.equals("RASBKOMPL")){result="DOC_RASBKOMPL_CANCEL_DVREG";}//
        if (docType.equals("ZAMENA")){result="DOC_ZAMENA_CANCEL_DVREG";}//

        if (docType.equals("KORRVS")){result="DOC_KORRVS_CANCEL_DVREG";}
        if (docType.equals("VIPBANK")){result="DOC_VIPBANK_CANCEL_DVREG";}
        if (docType.equals("OTCHPOST")){result="DOC_OTCHPOST_CANCEL_DVREG";}

//служебные документы
        if (docType.equals("RG_ADVCUST")){result="DOC_RGADVCUST_CANCEL_DVREG";}
        if (docType.equals("RG_BANK")){result="DOC_RGBANK_CANCEL_DVREG";}
        if (docType.equals("RG_CARDBAL")){result="DOC_RGCARD_BALANCE_CANCEL_DVREG";}
        if (docType.equals("RG_OSTNOM")){result="DOC_RGGOODS_CANCEL_DVREG";}
        if (docType.equals("RG_KASSA")){result="DOC_RGKASSA_CANCEL_DVREG";}
        if (docType.equals("RG_OSTNOM")){result="DOC_RGOSTNOM_CANCEL_DVREG";}
        if (docType.equals("RG_PAYSALE")){result="DOC_RGPAYSALE_CANCEL_DVREG";}
        if (docType.equals("RG_VSRASCH")){result="DOC_RGVSRASCH_CANCEL_DVREG";}
        if (docType.equals("RG_OTCHPOST")){result="DOC_RGOTCHPOST_CANCEL_DVREG";}
        return result;
    }

    private String getNameProcedureCancelDvRegRemontSystem(String docType){
        String result = "";
        if (docType.equals("KM1")){result="REM_DOC_KM1_CANCEL_DVREG";}
        if (docType.equals("KM2")){result="REM_DOC_KM2_CANCEL_DVREG";}
        if (docType.equals("REMONTHW")){result="REM_DOC_REMONTHW_CANCEL_DVREG";}
        if (docType.equals("REMONTKKT")){result="REM_DOC_REMONTKKT_CANCEL_DVREG";}
        if (docType.equals("REMONT")){result="REM_DOC_REMONT_CANCEL_DVREG";}
        if (docType.equals("SETSERVHW")){result="REM_DOC_SETSERVHW_CANCEL_DVREG";}
        if (docType.equals("SETSERVKKT")){result="REM_DOC_SETSERVKKT_CANCEL_DVREG";}


        if (docType.equals("REMPRN")){result="REM_DOC_PRN_CANCEL_DVREG";}
        if (docType.equals("REMREAL")){result="REM_DOC_REAL_CANCEL_DVREG";}
        if (docType.equals("REMOSN")){result="REM_DOC_OSN_CANCEL_DVREG";}
        if (docType.equals("REMSBKOMPL")){result="REM_DOC_SBKOMPL_CANCEL_DVREG";}
        if (docType.equals("POSTOTDIL")){result="REM_DOC_DILPOST_CANCEL_DVREG";}
        if (docType.equals("VOSDILERU")){result="REM_DOC_DILRET_CANCEL_DVREG";}
        if (docType.equals("PERSC")){result="REM_DOC_SCPER_CANCEL_DVREG";}
        if (docType.equals("VOSVISSC")){result="REM_DOC_SCRET_CANCEL_DVREG";}
        if (docType.equals("REMVPOK")){result="REM_DOC_VPOK_CANCEL_DVREG";}
        if (docType.equals("REMAKT")){result="REM_DOC_DAKT_CANCEL_DVREG";}
        if (docType.equals("REMPER")){result="REM_DOC_PER_CANCEL_DVREG";}

        if (docType.equals("REMZPOST")){result="REM_DOC_ZPOST_CANCEL_DVREG";}
        if (docType.equals("REMZVID")){result="REM_DOC_ZVID_CANCEL_DVREG";}
        if (docType.equals("REMZPER")){result="REM_DOC_ZPER_CANCEL_DVREG";}
        if (docType.equals("REMZSTART")){result="REM_DOC_ZSTART_CANCEL_DVREG";}
        if (docType.equals("REMZEND")){result="REM_DOC_ZEND_CANCEL_DVREG";}
        if (docType.equals("REMZOPER")){result="REM_DOC_ZOPER_CANCEL_DVREG";}
        if (docType.equals("REMZTREB")){result="REM_DOC_ZTREB_CANCEL_DVREG";}
        if (docType.equals("REMZAKTPR")){result="REM_DOC_ZAKTPR_CANCEL_DVREG";}
        if (docType.equals("REMZVZAP")){result="REM_DOC_ZVZAP_CANCEL_DVREG";}


        if (docType.equals("REMSPN")){result="REM_DOC_SPN_CANCEL_DVREG";}

        if (docType.equals("REM_RGOTCHPOST")){result="REM_DOC_RGOTCHPOST_CANCEL_DVREG";}

        if (docType.equals("REM_VIDACHA_HW")){result="REM_DOC_VIDACHA_HW_CANCEL_DVREG";}
        if (docType.equals("REM_VOSVRAT_HW")){result="REM_DOC_VOSVRAT_HW_CANCEL_DVREG";}

        if (docType.equals("REM_ZAKPOST")){result="REM_DOC_ZAKPOST_CANCEL_DVREG";}
        if (docType.equals("REM_OTPRPOST")){result="REM_DOC_OTPRPOST_CANCEL_DVREG";}


        if (docType.equals("REM_VZPER")){result="REM_DOC_VZPER_CANCEL_DVREG";}
        if (docType.equals("REM_VZVPO")){result="REM_DOC_VZVPO_CANCEL_DVREG";}
        return result;
    }

    private String getNameProcedureCancelDvRegHotelSystem(String docType) {
        String result = "";
        if (docType.equals("BRON")){result="HOT_DOC_BRON_CANCEL_DVREG";}
        if (docType.equals("OTMBRON")){result="HOT_DOC_OTMBRON_CANCEL_DVREG";}
        if (docType.equals("RASCHET")){result="HOT_DOC_RASCHET_CANCEL_DVREG";}
        if (docType.equals("RASM")){result="HOT_DOC_RASM_CANCEL_DVREG";}
        if (docType.equals("REAL")){result="HOT_DOC_REAL_CANCEL_DVREG";}
        if (docType.equals("VIEZD")){result="HOT_DOC_VIESD_CANCEL_DVREG";}
        return result;
    }

    private String getNameProcedureDvReg(String docType) {
        String result = "";
        if (docType.equals("CHK")){result="DOC_CHK_DVREG";}
        if (docType.equals("PRN")){result="DOC_PRN_DVREG";}//{Text="Прих. накл.";}
        if (docType.equals("SCH")){result="DOC_SCH_DVREG";}//{Text="Счет на оплату";}
        if (docType.equals("OSN")){result="DOC_OSN_DVREG";}//{Text="Остатки номенкл.";}
        if (docType.equals("PER")){result="DOC_PER_DVREG";}//{Text="Перемещение";}
        if (docType.equals("REA")){result="DOC_REA_DVREG";}//{Text="Реализация";}
        if (docType.equals("SPN")){result="DOC_SPN_DVREG";}//{Text="Акт списания";}
        if (docType.equals("VPO")){result="DOC_VPO_DVREG";}//{Text="Возврат поставщику";}
        if (docType.equals("PKO")){result="DOC_PKO_DVREG";}//{Text="Прих. касс. ордер";}
        if (docType.equals("RKO")){result="DOC_RKO_DVREG";}//{Text="Расх. касс. ордер";}
        if (docType.equals("PLP")){result="DOC_PLP_DVREG";}//{Text="Платежное поручение";}
        if (docType.equals("PNS")){result="DOC_PNS_DVREG";}//{Text="Поступление на счет";}
        if (docType.equals("INV")){result="DOC_INV_DVREG";}//{Text="Инвентаризация";}
        if (docType.equals("REPKKM")){result="DOC_REPKKM_DVREG";}//{Text="Отчет ККМ";}
        if (docType.equals("OSTVS")){result="DOC_OSTVS_DVREG";}//{Text="Корр. взаиморасчетов";}
        if (docType.equals("SCHFACT")){result="DOC_SCHFACT_DVREG";}//{Text="Счет-фактура";}
        if (docType.equals("REALROZN")){result="DOC_REALROZN_DVREG";}//{Text="Реализация розн.";}
        if (docType.equals("PKOROZN")){result="DOC_PKOROZN_DVREG";}//{Text="Приход нал.";}
        if (docType.equals("RKOROZN")){result="DOC_RKOROZN_DVREG";}//{Text="Расход нал.";}

        if (docType.equals("VPOK")){result="DOC_VPOK_DVREG";}//{Text="Возврат покупателя";}

        if (docType.equals("ISMPRICE")){result="DOC_ISMPRICE_DVREG";}//{Text="Переоценка";}
        if (docType.equals("SPOTROST")){result="DOC_SPOTROST_DVREG";}//{Text="Списание отр. остатков";}
        if (docType.equals("REV")){result="DOC_REV_DVREG";}//{Text="Ревизия";}

        if (docType.equals("SBKOMPL")){result="DOC_SBKOMPL_DVREG";}//{Text="Переоценка";}
        if (docType.equals("RASBKOMPL")){result="DOC_RASBKOMPL_DVREG";}//{Text="Списание отр. остатков";}
        if (docType.equals("ZAMENA")){result="DOC_ZAMENA_DVREG";}//{Text="Ревизия";}

        if (docType.equals("KORRVS")){result="DOC_KORRVS_DVREG";}
        if (docType.equals("VIPBANK")){result="DOC_VIPBANK_DVREG";}
        if (docType.equals("OTCHPOST")){result="DOC_OTCHPOST_DVREG";}

//служебные документы
        if (docType.equals("RG_ADVCUST")){result="DOC_RGADVCUST_DVREG";}
        if (docType.equals("RG_BANK")){result="DOC_RGBANK_DVREG";}
        if (docType.equals("RG_CARDBAL")){result="DOC_RGCARD_BALANCE_DVREG";}
        if (docType.equals("RG_OSTNOM")){result="DOC_RGGOODS_DVREG";}
        if (docType.equals("RG_KASSA")){result="DOC_RGKASSA_DVREG";}
        if (docType.equals("RG_OSTNOM")){result="DOC_RGOSTNOM_DVREG";}
        if (docType.equals("RG_PAYSALE")){result="DOC_RGPAYSALE_DVREG";}
        if (docType.equals("RG_VSRASCH")){result="DOC_RGVSRASCH_DVREG";}
        if (docType.equals("RG_OTCHPOST")){result="DOC_RGOTCHPOST_DVREG";}
        return result;
    }

    private String getNameProcedureDvRegRemontSystem(String docType) {
        String result = "";
        if (docType.equals("REMONT")){result="REM_DOC_REMONT_DVREG";}
        if (docType.equals("REMONTHW")){result="REM_DOC_REMONTHW_DVREG";}
        if (docType.equals("REMONTKKT")){result="REM_DOC_REMONTKKT_DVREG";}
        if (docType.equals("KM1")){result="REM_DOC_KM1_DVREG";}
        if (docType.equals("KM2")){result="REM_DOC_KM2_DVREG";}
        if (docType.equals("SETSERVKKT")){result="REM_DOC_SETSERVKKT_DVREG";}
        if (docType.equals("SETSERVHW")){result="REM_DOC_SETSERVHW_DVREG";}

        if (docType.equals("REMPRN")){result="REM_DOC_PRN_DVREG";}
        if (docType.equals("REMREAL")){result="REM_DOC_REAL_DVREG";}
        if (docType.equals("REMOSN")){result="REM_DOC_OSN_DVREG";}
        if (docType.equals("REMSBKOMPL")){result="REM_DOC_SBKOMPL_DVREG";}
        if (docType.equals("POSTOTDIL")){result="REM_DOC_DILPOST_DVREG";}
        if (docType.equals("VOSDILERU")){result="REM_DOC_DILRET_DVREG";}
        if (docType.equals("PERSC")){result="REM_DOC_SCPER_DVREG";}
        if (docType.equals("VOSVISSC")){result="REM_DOC_SCRET_DVREG";}
        if (docType.equals("REMVPOK")){result="REM_DOC_VPOK_DVREG";}
        if (docType.equals("REMAKT")){result="REM_DOC_DAKT_DVREG";}

        if (docType.equals("REMPER")){result="REM_DOC_PER_DVREG";}

        if (docType.equals("REMZPOST")){result="REM_DOC_ZPOST_DVREG";}
        if (docType.equals("REMZVID")){result="REM_DOC_ZVID_DVREG";}
        if (docType.equals("REMZPER")){result="REM_DOC_ZPER_DVREG";}
        if (docType.equals("REMZSTART")){result="REM_DOC_ZSTART_DVREG";}
        if (docType.equals("REMZEND")){result="REM_DOC_ZEND_DVREG";}
        if (docType.equals("REMZOPER")){result="REM_DOC_ZOPER_DVREG";}
        if (docType.equals("REMZTREB")){result="REM_DOC_ZTREB_DVREG";}
        if (docType.equals("REMZAKTPR")){result="REM_DOC_ZAKTPR_DVREG";}
        if (docType.equals("REMZVZAP")){result="REM_DOC_ZVZAP_DVREG";}

        if (docType.equals("REMSPN")){result="REM_DOC_SPN_DVREG";}
        if (docType.equals("REM_RGOTCHPOST")){result="REM_DOC_RGOTCHPOST_DVREG";}

        if (docType.equals("REM_VIDACHA_HW")){result="REM_DOC_VIDACHA_HW_DVREG";}
        if (docType.equals("REM_VOSVRAT_HW")){result="REM_DOC_VOSVRAT_HW_DVREG";}

        if (docType.equals("REM_ZAKPOST")){result="REM_DOC_ZAKPOST_DVREG";}
        if (docType.equals("REM_OTPRPOST")){result="REM_DOC_OTPRPOST_DVREG";}

        if (docType.equals("REM_VZPER")){result="REM_DOC_VZPER_DVREG";}
        if (docType.equals("REM_VZVPO")){result="REM_DOC_VZVPO_DVREG";}
        return result;
    }

    private String getNameProcedureDvRegHotelSystem(String docType) {
        String result = "";
        if (docType.equals("BRON")){result="HOT_DOC_BRON_DVREG";}
        if (docType.equals("OTMBRON")){result="HOT_DOC_OTMBRON_DVREG";}
        if (docType.equals("RASCHET")){result="HOT_DOC_RASCHET_DVREG";}
        if (docType.equals("RASM")){result="HOT_DOC_RASM_DVREG";}
        if (docType.equals("REAL")){result="HOT_DOC_REAL_DVREG";}
        if (docType.equals("VIEZD")){result="HOT_DOC_VIESD_DVREG";}
        return result;
    }
}