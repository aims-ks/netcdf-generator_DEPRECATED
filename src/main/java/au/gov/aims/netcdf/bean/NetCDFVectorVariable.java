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
