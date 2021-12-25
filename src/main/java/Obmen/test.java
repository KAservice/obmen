package Obmen;

import Obmen.BDEntities.RowEntity;
import Obmen.Util.ConnectionCreator;
import Obmen.Util.XMLProperties;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public class test {

    public static void main(String[] args) {
        UDMUribIn udmUribIn = new UDMUribIn();
        udmUribIn.updateDataInfBase("C:\\Users\\kaserv\\Desktop\\BD\\DataFor2.kas");
    }
}
