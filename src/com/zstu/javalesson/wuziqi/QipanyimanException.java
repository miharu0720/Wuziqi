package com.zstu.javalesson.wuziqi;

public class QipanyimanException extends Exception{
    public QipanyimanException(){
        super();
    }
    public QipanyimanException(String msg){
        super(msg);
    }
    @Override
    public String getMessage(){
        return "棋盘已满，和棋！" + super.getMessage();
    }
}
