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

import java.util.Objects;

public class NetCDFPointCoordinate implements Comparable<NetCDFPointCoordinate> {
    private static final float COORDINATE_EPSILON = 0.00001f; // about 1 metre on the equator
    private static final double HEIGHT_EPSILON = 0.0000001;

    private float lat;
    private float lon;

    private DateTime date; // Data date. Optional
    private Double height; // Vertical coordinate axis. Optional

    public NetCDFPointCoordinate(float lat, float lon) {
        this(lat, lon, null, null);
    }

    public NetCDFPointCoordinate(float lat, float lon, DateTime date) {
        this(lat, lon, date, null);
    }

    public NetCDFPointCoordinate(float lat, float lon, DateTime date, Double height) {
        this.lat = lat;
        this.lon = lon;
        this.date = date;
        this.height = height;
    }

    public float getLat() {
        return this.lat;
    }

    public float getLon() {
        return this.lon;
    }

    public DateTime getDate() {
        return this.date;
    }

    public Double getHeight() {
        return this.height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetCDFPointCoordinate that = (NetCDFPointCoordinate) o;
        return Float.compare(that.lat, this.lat) == 0 &&
                Float.compare(that.lon, this.lon) == 0 &&
                Objects.equals(this.date, that.date) &&
                Objects.equals(this.height, that.height);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.lat, this.lon, this.date, this.height);
    }

    @Override
    public int compareTo(NetCDFPointCoordinate o) {
        if (this == o) return 0;

        float latCmp = this.lat - o.lat;
        if (latCmp > COORDINATE_EPSILON) {
            return 1;
        }
        if (latCmp < -COORDINATE_EPSILON) {
            return -1;
        }

        float lonCmp = this.lon - o.lon;
        if (lonCmp > COORDINATE_EPSILON) {
            return 1;
        }
        if (lonCmp < -COORDINATE_EPSILON) {
            return -1;
        }

        if (this.date != o.date) {
            if (this.date == null) {
                return 1;
            }
            if (o.date == null) {
                return -1;
            }

            int dateCmp = this.date.compareTo(o.date);
            if (dateCmp != 0) {
                return dateCmp;
            }
        }

        if (this.height != o.height) {
            if (this.height == null) {
                return 1;
            }
            if (o.height == null) {
                return -1;
            }

            double heightCmp = this.height - o.height;
            if (heightCmp > HEIGHT_EPSILON) {
                return 1;
            }
            if (heightCmp < -HEIGHT_EPSILON) {
                return -1;
            }
        }

        return 0;
    }
}
