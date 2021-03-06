package Obmen;

import Obmen.Util.ObmenExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class test {
    private static final Logger logger =  LogManager.getLogger();
    public static long delay = 0;
    public static long period = 300000;
    public static String url;
    public static int baseID;
    public static int outID;

    public static void main(String[] args) {
        ObmenExecutor.fillConfigData();
        String filePath = url + "/Run" + baseID + ".kas";
        File file = new File(filePath);
        if (file.exists()) {
            ObmenExecutor.inputChanges();
            file.delete();
            ObmenExecutor.outputChanges();
        }
    }
}
