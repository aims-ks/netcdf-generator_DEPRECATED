/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.netcdf.bean;

import org.joda.time.DateTime;

public class NetCDFTimeVariable extends AbstractNetCDFVariable {
    public NetCDFTimeVariable(String name, String units) {
        super(name, units);
    }

    public Double getValue(float lat, float lon, DateTime date) {
        return this.getValue(new NetCDFPointCoordinate(lat, lon, date));
    }

    public void addDataPoint(float lat, float lon, DateTime date, double value) {
        this.addDataPoint(new NetCDFPointCoordinate(lat, lon, date), value);
    }
}
