package com.nestnav.mobile.models;

import Data.Request;

import java.io.Serializable;
import java.util.Date;

public class SearchRequest extends Request implements Serializable {
    private String area;
    private Date startDate;
    private Date endDate;
    private int minCapacity;
    private int minStars;
    private double maxPrice;

    public SearchRequest(String area, Date startDate, Date endDate, int minCapacity, int minStars, double maxPrice) {
        super(RequestType.SEARCH);
        this.area = area;
        this.startDate = startDate;
        this.endDate = endDate;
        this.minCapacity = minCapacity;
        this.minStars = minStars;
        this.maxPrice = maxPrice;
    }

    public String getArea() { return area; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public int getMinCapacity() { return minCapacity; }
    public int getMinStars() { return minStars; }
    public double getMaxPrice() { return maxPrice; }
}
