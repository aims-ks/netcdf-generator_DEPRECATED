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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NetCDFDataset implements Iterable<NetCDFVariable> {
    private float[] latitudes;
    private float[] longitudes;

    private List<NetCDFVariable> variables;

    public NetCDFDataset(float[] latitudes, float[] longitudes) {
        this.latitudes = latitudes;
        this.longitudes = longitudes;
        this.variables = new ArrayList<NetCDFVariable>();
    }

    public float[] getLatitudes() {
        return this.latitudes;
    }

    public float[] getLongitudes() {
        return this.longitudes;
    }

    public List<NetCDFVariable> getVariables() {
        return this.variables;
    }

    public void setVariables(List<NetCDFVariable> variables) {
        if (variables == null) {
            this.variables.clear();
        } else {
            this.variables = variables;
        }
    }

    public void addVariable(NetCDFVariable variable) {
        this.variables.add(variable);
    }

    @Override
    public Iterator<NetCDFVariable> iterator() {
        return this.variables.iterator();
    }
}
