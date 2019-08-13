package com.write.bmybatis.domain;

public class TJson  {

    private int id;
    private  String info;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Object getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "TJson{" +
                "id=" + id +
                ", info=" + info +
                '}';
    }
}
