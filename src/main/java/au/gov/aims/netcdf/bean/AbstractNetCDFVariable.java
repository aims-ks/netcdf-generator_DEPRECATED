/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.netcdf.bean;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class AbstractNetCDFVariable {
    private String name;

    private Map<String, String> attributes;

    private Map<NetCDFPointCoordinate, Double> data;

    protected AbstractNetCDFVariable(String name, String units) {
        this.name = name;
        this.attributes = new HashMap<String, String>();
        this.data = new HashMap<NetCDFPointCoordinate, Double>();

        this.setAttribute("units", units);
    }

    public String getName() {
        return this.name;
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public void setAttribute(String key, String value) {
        this.attributes.put(key, value);
    }


    public Map<NetCDFPointCoordinate, Double> getData() {
        return this.data;
    }

    public Double getValue(NetCDFPointCoordinate coordinate) {
        return this.data.get(coordinate);
    }

    public SortedSet<DateTime> getDates() {
        SortedSet<DateTime> dates = new TreeSet<DateTime>();
        for (NetCDFPointCoordinate dataPoint : this.data.keySet()) {
            if (dataPoint.getDate() != null) {
                dates.add(dataPoint.getDate());
            }
        }
        return dates;
    }

    public void addDataPoint(NetCDFPointCoordinate coordinate, Double value) {
        this.data.put(coordinate, value);
    }
}
