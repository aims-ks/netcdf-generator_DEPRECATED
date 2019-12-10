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
 * [X] Create simple data file covering GBR
 * [X] Create file with time gaps
 * [X] Create file with multiple hypercubes of data
 * [ ] Add depth support
 * [ ] Add vector variable support
 */

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class);
    private static final DateTimeZone TIMEZONE_BRISBANE = DateTimeZone.forID("Australia/Brisbane");

    public static void main(String ... args) throws Exception {
        Generator netCDFGenerator = new Generator();
        Main.generateGbrRaindow(netCDFGenerator, new DateTime(2019, 1, 1, 0, 0, TIMEZONE_BRISBANE), new File("/tmp/gbrRainbow.nc"));

        Main.generateMultiHypercube(netCDFGenerator, new DateTime(2019, 1, 1, 0, 0, TIMEZONE_BRISBANE), new File("/tmp/multi.nc"));
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

        for (int hour=0; hour<10*24; hour++) {
            DateTime frameDate = startDate.plusHours(hour);

            if (hour != 2 && hour != 3) {
                for (float lat : lats) {
                    for (float lon : lons) {

                        double tempValue = Math.abs((hour + lat + lon) % 40 - 20) - 10;
                        temp.addDataPoint(frameDate, lat, lon, tempValue);

                        // Salt contains holes in the data
                        if (hour != 5 && hour != 6) {
                            double saltValue = hour + (lat+22) + (-lon+154);
                            salt.addDataPoint(frameDate, lat, lon, saltValue);
                        }
                    }
                }
            }
        }

        netCDFGenerator.generate(outputFile, dataset);
    }


    public static void generateMultiHypercube(Generator netCDFGenerator, DateTime startDate, File outputFile) throws IOException, InvalidRangeException {
        float[] lats0 = new float[] {-22, -20.8f, -19.6f, -18.4f, -17.2f, -16, -14.8f, -13.6f, -12.4f, -11.2f, -10};
        float[] lons0 = {142, 143.2f, 144.4f, 145.6f, 146.8f, 148, 149.2f, 150.4f, 151.6f, 152.8f, 154};

        NetCDFDataset dataset0 = new NetCDFDataset(lats0, lons0);

        NetCDFVariable temp0 = new NetCDFVariable("temperature", "C");
        dataset0.addVariable(temp0);

        NetCDFVariable salt0 = new NetCDFVariable("salinity", "PSU");
        dataset0.addVariable(salt0);

        for (int hour=0; hour<10*24; hour++) {
            DateTime frameDate = startDate.plusHours(hour);

            for (float lat : lats0) {
                for (float lon : lons0) {
                    double tempValue = Math.abs((hour + lat + lon) % 40 - 20) - 10;
                    temp0.addDataPoint(frameDate, lat, lon, tempValue);

                    // Salt contains holes in the data
                    double saltValue = hour + (lat+22) + (-lon+154);
                    salt0.addDataPoint(frameDate, lat, lon, saltValue);
                }
            }
        }



        float[] lats1 = new float[] {-24, -22.8f, -21.6f, -20.4f, -19.2f, -18, -16.8f, -15.6f, -14.4f, -13.2f, -12};
        float[] lons1 = {144, 145.2f, 146.4f, 147.6f, 148.8f, 150, 151.2f, 152.4f, 153.6f, 154.8f, 155};

        NetCDFDataset dataset1 = new NetCDFDataset(lats1, lons1);

        NetCDFVariable temp1 = new NetCDFVariable("temperature1", "C");
        dataset1.addVariable(temp1);

        NetCDFVariable salt1 = new NetCDFVariable("salinity1", "PSU");
        dataset1.addVariable(salt1);

        for (int hour=13; hour<5*24; hour+=3) {
            DateTime frameDate = startDate.plusHours(hour);

            for (float lat : lats1) {
                for (float lon : lons1) {
                    double tempValue = Math.abs((hour + lat + lon) % 40 - 20) - 10;
                    temp1.addDataPoint(frameDate, lat, lon, tempValue);

                    // Salt contains holes in the data
                    double saltValue = hour + (lat+22) + (-lon+154);
                    salt1.addDataPoint(frameDate, lat, lon, saltValue);
                }
            }
        }


        netCDFGenerator.generate(outputFile, dataset0, dataset1);
    }
}
