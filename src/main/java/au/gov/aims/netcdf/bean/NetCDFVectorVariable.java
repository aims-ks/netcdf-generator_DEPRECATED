/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.netcdf.bean;

public class NetCDFVectorVariable<V extends AbstractNetCDFVariable> {
    private String groupName;
    private V u;
    private V v;

    public NetCDFVectorVariable(String groupName, V u, V v) {
        this.groupName = groupName;

        this.u = u;
        this.u.setAttribute("standard_name", String.format("eastward_%s", this.groupName));

        this.v = v;
        this.v.setAttribute("standard_name", String.format("northward_%s", this.groupName));
    }

    public V getU() {
        return this.u;
    }

    public V getV() {
        return this.v;
    }
}
