package Obmen.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Obmen.BDEntities.FieldEntity;
import Obmen.BDEntities.RowEntity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLProperties {
    private static final Logger logger =  LogManager.getLogger();

    //получение списка записей
    public static List<String> getQueryDataList(String xmlTest){
        List<String> result = new ArrayList<>();
        String regex = "(<[\\w\\s]+?(num))(.+?(</))(.+?(>))";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(xmlTest);
        while (matcher.find()){
            String dataLine = matcher.group();
            result.add(dataLine);
        }
        return result;
    }

    //получение списка данных для записи
    public static List<FieldEntity> getFieldList(String text, Map<String, String> domainMap){
        List<FieldEntity> fieldList = new ArrayList<>();

        //получение списка полей
        String fieldRegex = "<[\\w\\s]+?(t=\").+?(v=\").+?(/>)";
        //имя поля
        String nameRegex = "<\\w+?(\\s)";
        //тип поля
        String typeRegex  = "t=\"\\d\"";
        //значение поля
        String valueRegex = "v=\".+?(\"/>)";

        Pattern rowPattern = Pattern.compile(fieldRegex);
        Pattern namePattern = Pattern.compile(nameRegex);
        Pattern typePattern = Pattern.compile(typeRegex);
        Pattern valuePattern = Pattern.compile(valueRegex);

        Matcher rowMatcher = rowPattern.matcher(text);
        while (rowMatcher.find()){

            String rowText = rowMatcher.group();
            Matcher nameMatcher = namePattern.matcher(rowText);
            Matcher typeMatcher = typePattern.matcher(rowText);
            Matcher valueMatcher = valuePattern.matcher(rowText);
            nameMatcher.find();
            String name = nameMatcher.group().substring(1);


            typeMatcher.find();
            String type = typeMatcher.group().substring(3, 4);
            int intType = Integer.parseInt(type);

            String value = "";
            if (valueMatcher.find()) {
                value = valueMatcher.group();
                int valueLength = value.length();
                value = value.substring(3, valueLength - 3);
            }

            String domain = domainMap.get(name.toLowerCase().trim());

            FieldEntity fieldEntity = new FieldEntity(name, intType, value, domain);
            fieldList.add(fieldEntity);
        }
        return fieldList;
    }

    //получение объекта для одной записи в БД
    public static RowEntity getRowEntity(String text){
        String rowRegex = "(<[\\w\\s]+?(num=\")).+?(>)";
        String nameRegex = "<\\w+?(\\s)";
        String numRegex = "num=\".+?(\")";
        String typeRegex = "type=\".+?(\")";
        String operationNumRegex = "oper=\".+?(\")";
        String namePKRegex = "name_pk=\".+?(\")";
        String valuePKRegex = "value_pk=\".+?(\")";
        String idBaseRegex = "idext_base=\".+?(\")";
        String idBaseDataOutRegex = "idext_data_out=\".+?(\")";
        String idBaseIstRegex = "id_base_ist=\".+?(\")";
        String idBasePriemRegex = "id_base_priem=\".+?(\")";


        Pattern rowPattern = Pattern.compile(rowRegex);
        Pattern namePattern = Pattern.compile(nameRegex);
        Pattern typePattern = Pattern.compile(typeRegex);
        Pattern numPattern = Pattern.compile(numRegex);
        Pattern operationNumPattern = Pattern.compile(operationNumRegex);
        Pattern namePKPattern = Pattern.compile(namePKRegex);
        Pattern valuePKPattern = Pattern.compile(valuePKRegex);
        Pattern idBasePattern = Pattern.compile(idBaseRegex);
        Pattern idBaseDataOutPattern = Pattern.compile(idBaseDataOutRegex);
        Pattern idBaseIstPattern = Pattern.compile(idBaseIstRegex);
        Pattern idBasePriemPattern = Pattern.compile(idBasePriemRegex);


        Matcher rowMatcher = rowPattern.matcher(text);
        rowMatcher.find();
        String rowText = rowMatcher.group();

        Matcher nameMatcher = namePattern.matcher(rowText);
        nameMatcher.find();
        String name = nameMatcher.group().substring(1);

        Matcher typeMatcher = typePattern.matcher(rowText);
        typeMatcher.find();
        String type = typeMatcher.group();
        type = type.substring(6, type.length() - 1);
        int intType = Integer.parseInt(type);

        Matcher numMatcher = numPattern.matcher(rowText);
        numMatcher.find();
        String num = numMatcher.group();
        num = num.substring(5, num.length() - 1);
        int intNum = Integer.parseInt(num);

        Matcher operationNumMatcher = operationNumPattern.matcher(rowText);
        int intOperationNum = 0;
        if (operationNumMatcher.find()) {
            String operationNum = operationNumMatcher.group();
            operationNum = operationNum.substring(6, operationNum.length() - 1);
            intOperationNum = Integer.parseInt(operationNum);
        }

        Matcher namePKMatcher = namePKPattern.matcher(rowText);
        String namePK = null;
        if (namePKMatcher.find()) {
            namePK = namePKMatcher.group();
            namePK = namePK.substring(9, namePK.length() - 1);
        }

        Matcher valuePKMatcher = valuePKPattern.matcher(rowText);
        long intValuePK = 0;
        if (valuePKMatcher.find()) {
            String valuePK = valuePKMatcher.group();
            valuePK = valuePK.substring(10, valuePK.length() - 1);
            intValuePK = Long.parseLong(valuePK);
        }

        Matcher idBasePKMatcher = idBasePattern.matcher(rowText);
        int intIdBasePK = 0;
        if (idBasePKMatcher.find()) {
            String idBasePK = idBasePKMatcher.group();
            idBasePK = idBasePK.substring(12, idBasePK.length() - 1);
            intIdBasePK = Integer.parseInt(idBasePK);
        }

        Matcher idBaseDataOutPKMatcher = idBaseDataOutPattern.matcher(rowText);
        idBaseDataOutPKMatcher.find();
        String idBaseDataOutPK = idBaseDataOutPKMatcher.group();
        idBaseDataOutPK = idBaseDataOutPK.substring(16, idBaseDataOutPK.length() - 1);
        long intIdBaseDataOutPK = Long.parseLong(idBaseDataOutPK);

        Matcher idBaseIstMatcher = idBaseIstPattern.matcher(rowText);
        int idBaseIst = 0;
        if (idBaseIstMatcher.find()){
            String idBaseIstStr = idBaseIstMatcher.group();
            idBaseIstStr = idBaseIstStr.substring(13, idBaseIstStr.length() - 1);
            idBaseIst = Integer.parseInt(idBaseIstStr);
        }

        Matcher idBasePriemMatcher = idBasePriemPattern.matcher(rowText);
        int idBasePriem = 0;
        if (idBasePriemMatcher.find()){
            String idBasePriemStr = idBasePriemMatcher.group();
            idBasePriemStr = idBasePriemStr.substring(15, idBasePriemStr.length() - 1);
            idBasePriem = Integer.parseInt(idBasePriemStr);
        }


        Map<String, String> domainMap = getDomainList(name.toLowerCase());


        List<FieldEntity> fieldList = getFieldList(text, domainMap);


        return new RowEntity(name.trim(), intNum, intType, intOperationNum, namePK, intValuePK, intIdBasePK, intIdBaseDataOutPK, fieldList, idBaseIst, idBasePriem);
    }

    public static List<RowEntity> getRowList(String fullText){
        List<RowEntity> resultList = new ArrayList<>();
        List<String> textList = getQueryDataList(fullText);

        for (String text : textList){
            RowEntity rowEntity = getRowEntity(text);
            resultList.add(rowEntity);
        }

        return resultList;
    }

    public static Map<String, String> getDomainList(String tableName){
        Map<String, String> domainMap = new HashMap<>();
        String query = "SELECT\n" +
                "    column_name, domain_name\n" +
                "FROM\n" +
                "    information_schema.columns\n" +
                "WHERE table_name = '" + tableName.trim() + "'";

        try(Connection connection = new ConnectionCreator().getPostgresConnection();
            Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()){
                String fieldName = resultSet.getString("column_name");

                String domainName = resultSet.getString("domain_name");

                domainMap.put(fieldName, domainName);
            }
        }
        catch (SQLException ex){
            logger.error(ex);
        }
        return domainMap;
    }
}