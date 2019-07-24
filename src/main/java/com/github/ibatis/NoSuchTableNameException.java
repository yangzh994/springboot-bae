package com.github.ibatis;

/**
 * author: yangzh
 * desc: 找不到表异常
 */
public class NoSuchTableNameException extends RuntimeException{

    private static final long serialVersionUID = 3880206498166270512L;

    public NoSuchTableNameException() {
        super();
    }

    public NoSuchTableNameException(String message) {
        super(message);
    }

    public NoSuchTableNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchTableNameException(Throwable cause) {
        super(cause);
    }
}
