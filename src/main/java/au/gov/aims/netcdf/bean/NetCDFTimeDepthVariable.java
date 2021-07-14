/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.netcdf.bean;

import org.joda.time.DateTime;

public class NetCDFTimeDepthVariable extends AbstractNetCDFVariable {

    public NetCDFTimeDepthVariable(String name, String units) {
        super(name, units);
    }

    public Double getValue(float lat, float lon, DateTime date, double height) {
        return this.getValue(new NetCDFPointCoordinate(lat, lon, date, height));
    }

    public void addDataPoint(float lat, float lon, DateTime date, double height, double value) {
        this.addDataPoint(new NetCDFPointCoordinate(lat, lon, date, height), value);
    }
}
