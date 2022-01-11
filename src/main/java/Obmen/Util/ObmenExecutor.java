package Obmen.Util;

import Obmen.UDMUribIn;
import Obmen.UDMUribOut;

public class ObmenExecutor {
    public static void outputChanges(int idBase){
        UDMUribOut udmUribOut = new UDMUribOut();
        udmUribOut.createDataOutTable(idBase);
        udmUribOut.createXMLDoc(idBase);
        System.out.println(udmUribOut.textXML);
    }
    public static void inputChanges(String url){
        UDMUribIn udmUribIn = new UDMUribIn();
        udmUribIn.updateDataInfBase(url);
    }
}
