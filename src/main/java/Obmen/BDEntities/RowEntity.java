package Obmen.BDEntities;

import java.util.List;

public class RowEntity {
    private String name;
    private int num;
    private int type;
    private int operationNum;
    private String namePK;
    private long valuePK;
    private int idBase;
    private long idBaseDataOut;
    private List<FieldEntity> data;

    public RowEntity(String name, int num, int type, int operationNum, String namePK, long valuePK, int idBase, long idBaseDataOut, List<FieldEntity> data) {
        this.name = name;
        this.num = num;
        this.type = type;
        this.operationNum = operationNum;
        this.namePK = namePK;
        this.valuePK = valuePK;
        this.idBase = idBase;
        this.idBaseDataOut = idBaseDataOut;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getOperationNum() {
        return operationNum;
    }

    public void setOperationNum(int operationNum) {
        this.operationNum = operationNum;
    }

    public String getNamePK() {
        return namePK;
    }

    public void setNamePK(String namePK) {
        this.namePK = namePK;
    }

    public long getValuePK() {
        return valuePK;
    }

    public void setValuePK(int valuePK) {
        this.valuePK = valuePK;
    }

    public int getIdBase() {
        return idBase;
    }

    public void setIdBase(int idBase) {
        this.idBase = idBase;
    }

    public long getIdBaseDataOut() {
        return idBaseDataOut;
    }

    public void setIdBaseDataOut(int idBaseDataOut) {
        this.idBaseDataOut = idBaseDataOut;
    }

    public List<FieldEntity> getData() {
        return data;
    }

    public void setData(List<FieldEntity> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RowEntity{" +
                "name='" + name + '\'' +
                ", num=" + num +
                ", type=" + type +
                ", operationNum=" + operationNum +
                ", namePK='" + namePK + '\'' +
                ", valuePK=" + valuePK +
                ", idBase=" + idBase +
                ", idBaseDataOut=" + idBaseDataOut +
                ", data=" + data +
                '}';
    }
}
