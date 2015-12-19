package com.oxf1.myspider.pipline.impl;

import com.alibaba.fastjson.JSON;
import com.oxf1.myspider.TaskConfig;
import com.oxf1.myspider.config.ConfigKeys;
import com.oxf1.myspider.data.DataItem;
import com.oxf1.myspider.exception.MySpiderExceptionCode;
import com.oxf1.myspider.exception.MySpiderFetalException;
import com.oxf1.myspider.exception.MySpiderRecoverableException;
import com.oxf1.myspider.pipline.Pipline;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by cxu on 2015/6/21.
 */
public class LocalFilePipline extends Pipline {
    final static Logger logger = LoggerFactory.getLogger(LocalFilePipline.class);
    private String dataFilePath;//物理的数据文件位置path+file

    /**
     * @param taskConfig
     * @throws IOException
     */
    public LocalFilePipline(TaskConfig taskConfig) throws MySpiderFetalException {

        super(taskConfig);
        String spiderWorkDir = taskConfig.getSpiderWorkDir();

        this.dataFilePath = spiderWorkDir + taskConfig.getTaskFp() + File.separator + "pipline" + File.separator + taskConfig.getTaskName() + ".json";//完整的目录+文件名字。解析之后的数据保存的位置
        taskConfig.put(ConfigKeys.RT_LOCAL_FILE_PIPLINE_DATA_FILE, this.dataFilePath);
        String baseDir = FilenameUtils.getFullPath(dataFilePath);
        try {
            FileUtils.forceMkdir(new File(baseDir));
        } catch (IOException e) {
            log(logger, "error", "创建目录{}失败 {}", baseDir, e);
            MySpiderFetalException exp = new MySpiderFetalException(MySpiderExceptionCode.LOCAL_PIPLINE_MK_DIR_ERROR);
            exp.setErrorMessage(e.getLocalizedMessage());
            throw exp;
        }
    }

    @Override
    public void save(List<DataItem> dataItems) throws MySpiderFetalException {
        if (dataItems != null && dataItems.size()>0) {
            for (DataItem dataItem : dataItems) {
                try {
                    File dataFile = new File(dataFilePath);
                    String data = JSON.toJSONString(dataItem.getDataItem());
                    synchronized (super.getTaskConfig()) {//任务级别的锁，只锁住同一个任务的多个线程
                        FileUtils.writeStringToFile(dataFile, data + "\n", StandardCharsets.UTF_8.name(), true);
                    }
                } catch (IOException e) {
                    log(logger, "error", "写文件{}失败{}", dataFilePath, e);
                    MySpiderFetalException exp = new MySpiderFetalException(MySpiderExceptionCode.LOCAL_PIPLINE_WRITE_FILE_ERROR);
                    exp.setErrorMessage(e.getLocalizedMessage());
                    throw exp;
                }
            }
        }
    }

    @Override
    public void close() throws MySpiderRecoverableException {
        String baseDir = FilenameUtils.getFullPath(dataFilePath);
        try {
            FileUtils.deleteDirectory(new File(baseDir));
        } catch (IOException e) {
            log(logger, "error", "删除目录{}时失败{}", baseDir, e);
            MySpiderRecoverableException exp = new MySpiderRecoverableException(MySpiderExceptionCode.LOCAL_PIPLINE_DEL_DIR_ERROR);
            exp.setErrorMessage(e.getLocalizedMessage());
            throw exp;
        }
    }
}