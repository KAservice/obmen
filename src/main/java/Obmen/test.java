package Obmen;

import Obmen.BDEntities.ConfigurationEntity;
import Obmen.BDEntities.RowEntity;
import Obmen.Util.ConnectionCreator;
import Obmen.Util.ObmenExecutor;
import Obmen.Util.XMLProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {
    private static final Logger logger =  LogManager.getLogger();


    public static void main(String[] args) {
        ConfigurationEntity configurationEntity;
        int baseID;
        String obmenURL;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();

        try {
            configurationEntity = mapper.readValue(new File("./Configuration.yaml"), ConfigurationEntity.class);
            ConnectionCreator.url = configurationEntity.url;
            ConnectionCreator.password = configurationEntity.password;
            ConnectionCreator.user = configurationEntity.user;
            obmenURL = configurationEntity.obmenURL;
            baseID = configurationEntity.baseID;
            ObmenExecutor.outputChanges(baseID);

//            ObmenExecutor.inputChanges(obmenURL);
        }
        catch (IOException ex){
            logger.error(ex);
        }



    }
}
