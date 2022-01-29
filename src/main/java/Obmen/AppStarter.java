package Obmen;

import Obmen.Util.ObmenExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

public class AppStarter {

    private static final Logger logger =  LogManager.getLogger();
    public static long delay = 0;
    public static long period = 300000;
    public static String url;
    public static int baseID;
    public static int outID;


    public static void main(String[] args) {
        ObmenExecutor.fillConfigData();


        logger.info("app start");
        TimerTask timerTask = new AppTask(url, baseID, outID);
        Timer timer = new Timer();

        timer.schedule(timerTask, delay, period);
    }
}

