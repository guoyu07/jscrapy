package org.jscrapy.core;

import org.jscrapy.common.log.MyLoggerFactory;
import org.jscrapy.core.pagecache.Cacher;
import org.jscrapy.core.component.MyspiderComponent;
import org.jscrapy.core.status.TaskStatus;
import org.jscrapy.core.dedup.DeDup;
import org.jscrapy.core.downloader.Downloader;
import org.jscrapy.core.exception.MySpiderFetalException;
import org.jscrapy.core.pipline.Pipline;
import org.jscrapy.core.processor.Processor;
import org.jscrapy.core.scheduler.Scheduler;
import org.jscrapy.core.util.ClassLoadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 对线程池的抽象
 * Created by cxu on 2015/6/20.
 */
public class Task extends MyspiderComponent {
    final static Logger logger = LoggerFactory.getLogger(Task.class);
    private ExecutorService threads;

    public Task(TaskConfig taskConfig) {
        super(taskConfig);
        int threadCount = taskConfig.getThreadCount();
        threads = Executors.newFixedThreadPool(threadCount);
    }

    public void cancel() throws MySpiderFetalException {
        getTaskConfig().setTaskStatus(TaskStatus.Status.CANCEL);
        threads.shutdown();
    }

    public void run() throws MySpiderFetalException {
        TaskConfig cfg = getTaskConfig();
        cfg.setTaskStatus(TaskStatus.Status.RUN);

        String schedulerClass = cfg.getSchedulerClassName();
        String dedupClass = cfg.getDedupClassName();
        String downloaderClass = cfg.getDownloaderClassName();

        String processorClass = cfg.getProcessorClassName();
        String cacherClass = cfg.getCacherClassName();
        String[] piplineClass = cfg.getPiplineClassName().split(",");

        Scheduler scheduler = ClassLoadUtil.loadScheduler(schedulerClass, cfg);
        cfg.setSchedulerObject(scheduler);

        String logPath  = cfg.getTaskLogDir();
        org.apache.log4j.Logger logger = MyLoggerFactory.getModuleLogger(cfg.getTaskFp(), logPath);
        cfg.setTaskLogger(logger);
        int threadCount = getTaskConfig().getThreadCount();
        for (int i = 0; i < threadCount; i++) {
            Spider sp = new Spider(getTaskConfig());
            DeDup dedup = ClassLoadUtil.loadDedup(dedupClass, cfg);
            Downloader downloader = ClassLoadUtil.loadDownloader(downloaderClass, cfg);
            Processor processor = ClassLoadUtil.loadProcessor(processorClass, cfg);
            Cacher cacher = ClassLoadUtil.loadCacher(cacherClass, cfg);
            sp.setDedup(dedup);
            sp.setDownloader(downloader);
            sp.setProcessor(processor);
            sp.setCacher(cacher);
            for (String p : piplineClass) {
                Pipline pipline = ClassLoadUtil.loadPipline(p, cfg);
                sp.addPipline(pipline);
            }

            threads.submit(sp);
        }
    }

    public void pause() throws MySpiderFetalException {
        getTaskConfig().setTaskStatus(TaskStatus.Status.PAUSE);
        threads.shutdown();
    }

    @Override
    public void close() {

    }

    public TaskConfig getTaskConfig() {
        return getTaskConfig();
    }

    @Override
    public String toString() {
        TaskConfig cfg = getTaskConfig();
        String taskName = cfg.getTaskName();
        String taskFp = cfg.getTaskFp();
        return "Task{" +
                "tasiName=" + taskName +
                "taskFp=" + taskFp +
                '}';
    }
}