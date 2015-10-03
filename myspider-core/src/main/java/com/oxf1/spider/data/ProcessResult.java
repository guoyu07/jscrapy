package com.oxf1.spider.data;

import com.oxf1.spider.request.HttpRequestMethod;
import com.oxf1.spider.request.Request;
import com.oxf1.spider.request.impl.HttpRequest;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by cxu on 2015/10/1.
 */
public class ProcessResult {
    private static final String FIELD_CREATE_TIME = "_create_time";
    private static final String FIELD_URL = "_from_url";
    private static final String SCHEDULER_VERSION = "_scheduler_version";

    private Request request;
    private String schedulerVersion;
    private List<DataItem> data;
    private List<Request> links;

    public ProcessResult() {
        data = new LinkedList<>();
        links = new LinkedList<>();
    }

    public ProcessResult addDataItem(DataItem item) {
        if(item!=null && !item.isEmpty()) {
            item = addOptionField(item);
            data.add(item);
        }

        return this;
    }

    public ProcessResult addDataItem(List<DataItem> items) {
        if (items != null && items.size() > 0) {
            for (DataItem item : items) {
                if (!item.isEmpty()) {
                    item = addOptionField(item);
                    data.add(item);
                }
            }
        }
        return this;
    }

    public ProcessResult addLinks(String link) {
        if (StringUtils.isNotBlank(link)) {
            Request req = new HttpRequest(link, HttpRequestMethod.HTTP_GET, null);
            links.add(req);
        }

        return this;
    }

    public ProcessResult addLinks(List<String> links) {
        if (links != null) {
            for (String s : links) {
                Request req = new HttpRequest(s, HttpRequestMethod.HTTP_GET, null);
                this.links.add(req);
            }
        }
        return this;
    }

    public ProcessResult addRequest(Request link) {
        if (link != null) {
            links.add(link);
        }
        return this;
    }

    public ProcessResult addRequest(List<Request> links) {
        if (links != null && links.size() > 0) {
            this.links.addAll(links);
        }
        return this;
    }

    public List<DataItem> getData() {
        return data;
    }

    public List<Request> getLinks() {
        return links;
    }

    private String getTimeStrNow() {
        DateTime dateTime = new DateTime();
        String timeNow = dateTime.toString("yyyy-MM-dd HH:mm:ss");
        return timeNow;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public String getSchedulerVersion() {
        return schedulerVersion;
    }

    public void setSchedulerVersion(String schedulerVersion) {
        this.schedulerVersion = schedulerVersion;
    }

    private DataItem addOptionField(DataItem item) {
        item.put(FIELD_CREATE_TIME, getTimeStrNow());
        item.put(FIELD_URL, request.asJson());
        item.put(SCHEDULER_VERSION, schedulerVersion);
        return item;
    }
}
