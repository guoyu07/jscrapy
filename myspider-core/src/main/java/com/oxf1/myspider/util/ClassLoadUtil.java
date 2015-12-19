package com.oxf1.myspider.util;

import com.oxf1.myspider.TaskConfig;
import com.oxf1.myspider.cacher.Cacher;
import com.oxf1.myspider.dedup.DeDup;
import com.oxf1.myspider.downloader.Downloader;
import com.oxf1.myspider.exception.MySpiderExceptionCode;
import com.oxf1.myspider.exception.MySpiderFetalException;
import com.oxf1.myspider.pipline.Pipline;
import com.oxf1.myspider.processor.Processor;
import com.oxf1.myspider.scheduler.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.lang.reflect.Constructor;

/**
 * Created by cxu on 2015/10/2.
 */
public class ClassLoadUtil {
    final static Logger logger = LoggerFactory.getLogger(ClassLoadUtil.class);

    public static Scheduler loadScheduler(String className, TaskConfig arg) throws MySpiderFetalException {
        Object o = loadClass(className, arg);
        if (o != null) {
            return (Scheduler)o;
        }
        else{
            return null;
        }
    }

    public static DeDup loadDedup(String className, TaskConfig arg) throws MySpiderFetalException {
        Object o = loadClass(className, arg);
        if (o != null) {
            return (DeDup)o;
        }
        else{
            return null;
        }
    }

    public static Downloader loadDownloader(String className, TaskConfig arg) throws MySpiderFetalException {
        Object o = loadClass(className, arg);
        if (o != null) {
            return (Downloader)o;
        }
        else{
            return null;
        }
    }

    public static Processor loadProcessor(String className, TaskConfig arg) throws MySpiderFetalException {
        Object o = loadClass(className, arg);
        if (o != null) {
            return (Processor)o;
        }
        else{
            return null;
        }
    }

    public static Cacher loadCacher(String className, TaskConfig arg) throws MySpiderFetalException {
        Object o = loadClass(className, arg);
        if (o != null) {
            return (Cacher)o;
        }
        else{
            return null;
        }
    }

    public static Pipline loadPipline(String className, TaskConfig arg) throws MySpiderFetalException {
        Object o = loadClass(className, arg);
        if (o != null) {
            return (Pipline)o;
        }
        else{
            return null;
        }
    }

    private static Object loadClass(String className, TaskConfig arg) throws MySpiderFetalException {
        Object o = null;
        try {
            Class c = Class.forName(className);
            Constructor constructor = c.getConstructor(new Class[]{TaskConfig.class});
            o = constructor.newInstance(arg);
        } catch (Exception e) {
            logger.error("构造{}时出错{}", className, e);
            String errorMessage = MessageFormatter.format("构造对象{}时出错", className).getMessage();
            MySpiderFetalException exp = new MySpiderFetalException(MySpiderExceptionCode.CLASS_LOAD_ERROR);
            exp.setErrorMessage(errorMessage);
            throw  exp;
        }

        return o;
    }

}