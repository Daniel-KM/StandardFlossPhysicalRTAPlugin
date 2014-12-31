package com.exlibris.primo.api.common;

public interface IPrimoLogger {
    public void setClass(Class<?> clazz);

    public void info(String msg);

    public void warn(String msg);

    public void warn(String msg, Exception e);

    public void error(String msg);

    public void error(String msg, Exception e);
}
