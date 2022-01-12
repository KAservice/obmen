package Obmen.Util;

import Obmen.BDEntities.ConfigurationEntity;
import Obmen.UDMUribIn;
import Obmen.UDMUribOut;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ObmenExecutor {

    private static final Logger logger =  LogManager.getLogger();
    private static int baseID;
    private static int currentBaseID;
    private static String obmenURL;
    private static String urlForOutput;
    private static String urlForInput;

    //заполняем данные из файла конфигурации
    public static void fillConfigData(){
        ConfigurationEntity configurationEntity;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        try {
            configurationEntity = mapper.readValue(new File("./Configuration.yaml"), ConfigurationEntity.class);
            ConnectionCreator.url = configurationEntity.url;
            ConnectionCreator.password = configurationEntity.password;
            ConnectionCreator.user = configurationEntity.user;
            obmenURL = configurationEntity.obmenURL;
            baseID = configurationEntity.baseID;
            getCurrentBaseNumber();
            urlForOutput = obmenURL + "/dataToBase" + baseID;
            urlForInput = obmenURL + "/dataToBase" + currentBaseID;
        }
        catch (IOException ex){
            logger.error(ex);
        }
    }

    //выгружаем данные изменений в файл
    public static void outputChanges(){
        UDMUribOut udmUribOut = new UDMUribOut();
        udmUribOut.createDataOutTable(baseID);
        udmUribOut.createXMLDoc(baseID);
        System.out.println(udmUribOut.textXML);
        writeDataToFile(udmUribOut.textXML);
    }

    //загружаем данные изменений из файла в БД
    public static void inputChanges(){
        File file = new File(urlForInput);
        if (file.exists()) {
            UDMUribIn udmUribIn = new UDMUribIn();
            udmUribIn.updateDataInfBase(urlForInput);
        }
        else
            return;
    }

    //записываем данные в файл
    private static void writeDataToFile(String data){
        File file = new File(urlForOutput);
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(data);
        }
        catch (IOException ex){
            logger.error(ex);
        }
    }

    //получаем номер текущей БД
    private static void getCurrentBaseNumber(){

        //база источник, текущая база
        try(Connection connection = new ConnectionCreator().getPostgresConnection();
            Statement statement = connection.createStatement()) {
            String query = "select setup.value_setup from setup where setup.id_setup=3";
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()){
                currentBaseID = resultSet.getInt("VALUE_SETUP");
            }
        }
        catch (SQLException ex){
            logger.error(ex);
        }

    }
}
