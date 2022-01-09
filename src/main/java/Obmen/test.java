package Obmen;

import Obmen.BDEntities.RowEntity;
import Obmen.Util.XMLProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {
    private static final Logger logger =  LogManager.getLogger();


    public static void main(String[] args) {
//        UDMUribOut udmUribOut = new UDMUribOut();
//        udmUribOut.createDataOutTable(1);
//        udmUribOut.createXMLDoc(1);
//        System.out.println(udmUribOut.textXML);

        UDMUribIn udmUribIn = new UDMUribIn();
        udmUribIn.updateDataInfBase("/home/andrew/DB/testObmen.txt");


    }
}
