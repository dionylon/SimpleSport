package com.dionys.mymap.entity;

import com.amap.api.location.AMapLocation;
import com.dionys.mymap.util.TimeUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *  路径的实体类
 *
 */
public class PathRecord {
    // 起点
    private AMapLocation mStartPoint;
    // 终点
    private AMapLocation mEndPoint;
    // 保存所有的点
    private List<AMapLocation> mPathLinePoints = new ArrayList<>();
    // 总距离
    private String mDistance;
    // 持续时间
    private String mDuration;
    // 平均速度
    private String mAverageSpeed;
    // 开始时间
    private String mDate;

    // id，主键
    private int mId = 0;

    public PathRecord() {

    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public AMapLocation getStartpoint() {
        return mStartPoint;
    }

    public void setStartpoint(AMapLocation startpoint) {
        this.mStartPoint = startpoint;
    }

    public AMapLocation getEndpoint() {
        return mEndPoint;
    }

    public void setEndpoint(AMapLocation endpoint) {
        this.mEndPoint = endpoint;
    }

    public List<AMapLocation> getPathline() {
        return mPathLinePoints;
    }

    public void setPathline(List<AMapLocation> pathline) {
        this.mPathLinePoints = pathline;
    }

    public String getDistance() {
        return mDistance;
    }

    public void setDistance(String distance) {
        this.mDistance = distance;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String duration) {
        this.mDuration = duration;
    }

    public String getAveragespeed() {
        return mAverageSpeed;
    }

    public void setAveragespeed(String averagespeed) {
        this.mAverageSpeed = averagespeed;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        this.mDate = date;
    }

    public void addpoint(AMapLocation point) {
        mPathLinePoints.add(point);
    }

    @Override
    public String toString() {
        StringBuilder record = new StringBuilder();
//        record.append("recordSize:" + getPathline().size() + ", ");

        DecimalFormat df = new DecimalFormat("0.00");
        double dis = Double.parseDouble(getDistance()) / 1000;
        record.append("距离:" + df.format(dis) + "公里, ");
        record.append("用时:" + TimeUtil.getFormatTime(getDuration()));
        return record.toString();
    }

    public String details(){
        StringBuilder sb = new StringBuilder();

        sb.append(toString());
        double dis = Double.parseDouble(getDistance());
        double dur = Double.parseDouble(getDuration());
        DecimalFormat df = new DecimalFormat("0.00");

        sb.append("\n平均配速:" + df.format(dis/dur) + "米/秒");

        return sb.toString();
    }

}
