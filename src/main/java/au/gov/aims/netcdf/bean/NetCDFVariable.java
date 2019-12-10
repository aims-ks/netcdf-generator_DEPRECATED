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

public class NetCDFVariable extends AbstractNetCDFVariable {
    public NetCDFVariable(String name, String unit) {
        super(name, unit);
    }

    public Double getValue(DateTime date, float lat, float lon) {
        return this.getValue(new NetCDFPointCoordinate(date, lat, lon));
    }

    public void addDataPoint(DateTime date, float lat, float lon, double value) {
        this.addDataPoint(new NetCDFPointCoordinate(date, lat, lon), value);
    }
}
