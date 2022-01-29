package Obmen;

import Obmen.Util.ObmenExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.TimerTask;

public class AppTask extends TimerTask {
    private static final Logger logger =  LogManager.getLogger();
    public  String url;
    public  int baseID;
    public int outID;


    public AppTask(){

    }
    public AppTask(String url, int baseID, int outID){
        this.url = url;
        this.baseID = baseID;
        this.outID = outID;
    }

    @Override
    public void run() {
        logger.info("start app");
        String filePath = url + "/Run" + baseID + ".kas";
        File file = new File(filePath);
        if (file.exists()) {
            logger.info("start import data");
            ObmenExecutor.inputChanges();
            logger.info("end import data");
            file.delete();
            logger.info("start export data");
            ObmenExecutor.outputChanges();
            logger.info("end export data");
            try {
                File newFile = new File(url + "/Run" + outID + ".kas");
                newFile.createNewFile();
            }
            catch (IOException ex){
                logger.error("ошибка при создании файла Run", ex);
            }
        }
        logger.info("end app");
    }
}
