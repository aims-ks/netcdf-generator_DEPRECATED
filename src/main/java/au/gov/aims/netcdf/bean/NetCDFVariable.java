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

public class NetCDFVariable {
    private String name;
    private String unit;
    private Map<NetCDFPointCoordinate, Double> data;

    public NetCDFVariable(String name, String unit) {
        this.name = name;
        this.unit = unit;
        this.data = new HashMap<NetCDFPointCoordinate, Double>();
    }

    public String getName() {
        return this.name;
    }

    public String getUnit() {
        return this.unit;
    }

    public Map<NetCDFPointCoordinate, Double> getData() {
        return this.data;
    }

    public Double getData(DateTime date, float lat, float lon) {
        return this.getData(new NetCDFPointCoordinate(date, lat, lon));
    }

    public Double getData(NetCDFPointCoordinate coordinate) {
        return this.data.get(coordinate);
    }

    public SortedSet<DateTime> getDates() {
        SortedSet<DateTime> dates = new TreeSet<DateTime>();
        for (NetCDFPointCoordinate dataPoint : this.data.keySet()) {
            dates.add(dataPoint.getDate());
        }
        return dates;
    }

    public void addDataPoint(DateTime date, float lat, float lon, double value) {
        this.addDataPoint(new NetCDFPointCoordinate(date, lat, lon), value);
    }

    public void addDataPoint(NetCDFPointCoordinate coordinate, Double value) {
        this.data.put(coordinate, value);
    }
}
