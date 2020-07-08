package com.fr.tw.util;

public class JSONResult {
    //标识当前请求是否正常处理
    private boolean success=true;
    //存储提示信息
    private String msg;
    //存储携带数据
    private Object result;

    private Integer total;

    private Integer yeshu;

    public Integer getYeshu() {
        return yeshu;
    }

    public void setYeshu(Integer yeshu) {
        this.yeshu = yeshu;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public Object getResult() {
        return result;
    }
    public void setResult(Object result) {
        this.result = result;
    }

}
