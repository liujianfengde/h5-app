package cn.liuxiaoer.net;

import com.google.gson.Gson;

import java.io.Serializable;

public class BaseModel implements Serializable {

    private String status;
    private String msg;

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.msg = message;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return msg;
    }


    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
