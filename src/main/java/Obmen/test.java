package Obmen;

import Obmen.BDEntities.RowEntity;
import Obmen.Util.ObmenExecutor;
import Obmen.Util.XMLProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {
    private static final Logger logger =  LogManager.getLogger();


    public static void main(String[] args) {
//        ObmenExecutor.outputChanges(1);

        ObmenExecutor.inputChanges("C:\\Users\\kaserv\\Desktop\\test.txt");
    }
}
