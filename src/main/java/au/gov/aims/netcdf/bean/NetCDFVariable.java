/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.netcdf.bean;

public class NetCDFVariable extends AbstractNetCDFVariable {
    public NetCDFVariable(String name, String units) {
        super(name, units);
    }

    public Double getValue(float lat, float lon) {
        return this.getValue(new NetCDFPointCoordinate(lat, lon));
    }

    public void addDataPoint(float lat, float lon, double value) {
        this.addDataPoint(new NetCDFPointCoordinate(lat, lon), value);
    }
}
