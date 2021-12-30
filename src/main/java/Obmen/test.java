package Obmen;

import Obmen.BDEntities.RowEntity;
import Obmen.Util.ConnectionCreator;
import Obmen.Util.DBProperties;
import Obmen.Util.XMLProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public class test {
    private static final Logger logger =  LogManager.getLogger();


    public static void main(String[] args) {
        UDMUribOut udmUribOut = new UDMUribOut();
        udmUribOut.createDataOutTable(1);
        udmUribOut.createXMLDoc(1);
        System.out.println(udmUribOut.textXML);


    }
}
