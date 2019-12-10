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
package au.gov.aims.netcdf;

import au.gov.aims.netcdf.bean.NetCDFDataset;
import au.gov.aims.netcdf.bean.NetCDFVariable;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import ucar.ma2.InvalidRangeException;

import java.io.File;
import java.io.IOException;

/*
 * TODO
 * 1. Create simple data file covering GBR
 * 2. Create file with time gaps
 * 3. Create file with multiple hypercubes of data
 */

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class);
    private static final DateTimeZone TIMEZONE_BRISBANE = DateTimeZone.forID("Australia/Brisbane");

    public static void main(String ... args) throws Exception {
        Generator netCDFGenerator = new Generator();

        Main.generateGbrRaindow(netCDFGenerator, new DateTime(2019, 1, 1, 0, 0, TIMEZONE_BRISBANE), new File("/tmp/gbrRainbow.nc"));
    }

    /**
     * Create simple NetCDF files that contains data that changes depending on its coordinates.
     * The data is very coarse, to produce small NetCDF file.
     *
     * @param netCDFGenerator
     * @param startDate
     * @param outputFile
     * @throws IOException
     * @throws InvalidRangeException
     */
    public static void generateGbrRaindow(Generator netCDFGenerator, DateTime startDate, File outputFile) throws IOException, InvalidRangeException {
        float[] lats = new float[] {-22, -20.8f, -19.6f, -18.4f, -17.2f, -16, -14.8f, -13.6f, -12.4f, -11.2f, -10};
        float[] lons = {142, 143.2f, 144.4f, 145.6f, 146.8f, 148, 149.2f, 150.4f, 151.6f, 152.8f, 154};
        NetCDFDataset dataset = new NetCDFDataset(lats, lons);

        NetCDFVariable temp = new NetCDFVariable("temperature", "C");
        dataset.addVariable(temp);

        NetCDFVariable salt = new NetCDFVariable("salinity", "PSU");
        dataset.addVariable(salt);

        for (int hour=0; hour<20; hour++) {
            for (float lat : lats) {
                for (float lon : lons) {
                    double tempValue = Math.abs((hour + lat + lon) % 40 - 20) - 10;
                    temp.addDataPoint(startDate.plusHours(hour), lat, lon, tempValue);

                    // Salt contains holes in the data
                    if (hour != 5 && hour != 6) {
                        double saltValue = hour + (lat+22) + (-lon+154);
                        salt.addDataPoint(startDate.plusHours(hour), lat, lon, saltValue);
                    }
                }
            }
        }

        netCDFGenerator.generate(dataset, outputFile);
    }
}
