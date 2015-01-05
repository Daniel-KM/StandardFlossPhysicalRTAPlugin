package net.berthereau.exlibris.rta.test;

import com.exlibris.primo.api.common.IPrimoLogger;

/**
 * This class implements IPrimoLogger interface as a console logger.
 *
 * This class is made for testing purpose only.
 */
public class PrimoLoggerTestUse implements IPrimoLogger {

    @Override
    public void setClass(Class<?> clazz) {
        // TODO Auto-generated method stub
    }

    @Override
    public void info(String msg) {
        System.out.println("Logger Info: " + msg);
    }

    @Override
    public void warn(String msg) {
        System.out.println("Logger warn: " + msg);
    }

    @Override
    public void warn(String msg, Exception e) {
        System.out.println("Logger warn: " + msg);
    }

    @Override
    public void error(String msg) {
        System.out.println("Logger error: " + msg);
    }

    @Override
    public void error(String msg, Exception e) {
        System.out.println("Logger error: " + msg);
    }

}
