/*
 *  Copyright (C) 2019 Australian Institute of Marine Science
 *
 *  Contact: Gael Lafond <g.lafond@aims.gov.au>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
