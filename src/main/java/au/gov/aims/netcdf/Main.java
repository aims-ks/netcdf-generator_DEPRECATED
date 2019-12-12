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
import au.gov.aims.netcdf.bean.NetCDFDepthVariable;
import au.gov.aims.netcdf.bean.NetCDFVariable;
import au.gov.aims.netcdf.bean.NetCDFVectorVariable;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Hours;
import ucar.ma2.InvalidRangeException;

import java.io.File;
import java.io.IOException;

/*
 * TODO
 * [X] Create simple data file covering GBR
 * [X] Create file with time gaps
 * [X] Create file with multiple hypercubes of data
 * [X] Add depth support
 * [X] Add vector variable support
 *
 * [ ] Create NetCDF files and re-write NcAnimate tests
 *
 * NetCDF file samples:
 *     https://www.unidata.ucar.edu/software/netcdf/examples/files.html
 */

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class);
    private static final DateTimeZone TIMEZONE_BRISBANE = DateTimeZone.forID("Australia/Brisbane");

    public static void main(String ... args) throws Exception {
        Generator netCDFGenerator = new Generator();

        Main.generateTest(netCDFGenerator,
                new DateTime(2019, 1, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2019, 1, 2, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/test.nc"));

/*
        Main.generateGbrRaindow(netCDFGenerator,
                new DateTime(2019, 1, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2019, 1, 10, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbrRainbow.nc"));

        Main.generateMultiHypercube(netCDFGenerator,
                new DateTime(2019, 1, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2019, 1, 10, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/multi.nc"));
*/
    }

    public static float[] getCoordinates(float min, float max, int steps) {
        float[] coordinates = new float[steps];

        for (int i=0; i<steps; i++) {
            coordinates[i] = min + (max - min) * i / (steps - 1);
        }

        return coordinates;
    }


    /**
     * Create data that shows as a linear gradient, at a given angle
     * @param lat Latitude coordinate, in degree
     * @param lon Longitude coordinate, in degree
     * @param min Minimum output value
     * @param max Maximum output value
     * @param mag Distance between gradient, in lon / lat degree
     * @param angle The angle of the gradient, in degree. 0 for horizontal, turning clockwise.
     * @param noise Level of noise, between [0, 1]
     * @return A value between [min, max] for the given coordinate.
     */
    public static double drawLinearGradient(float lat, float lon, double min, double max, double mag, double angle, double noise) {
        double noisyLat = lat + (Math.random() - 0.5) * 90 * noise;
        double noisyLon = lon + (Math.random() - 0.5) * 90 * noise;

        double radianAngle = Math.toRadians(angle);

        double latRatio = Math.cos(radianAngle);
        double lonRatio = Math.sin(radianAngle);

        // Value between [1, -1]
        double trigoValue = Math.sin(2 * Math.PI * (noisyLat * latRatio + noisyLon * lonRatio) / mag);

        // Value between [0, 1]
        double ratioValue = (trigoValue + 1) / 2;

        // Value between [min, max]
        return min + ratioValue * (max - min);
    }

    /**
     * Create data that shows as a radial gradient, with a given diameter
     * @param lat Latitude coordinate, in degree
     * @param lon Longitude coordinate, in degree
     * @param min Minimum output value
     * @param max Maximum output value
     * @param diameter Diameter of the circles
     * @param noise Level of noise, between [0, 1]
     * @return A value between [min, max] for the given coordinate.
     */
    public static double drawRadialGradient(float lat, float lon, double min, double max, double diameter, double noise) {
        double noisyLat = lat + (Math.random() - 0.5) * 90 * noise;
        double noisyLon = lon + (Math.random() - 0.5) * 90 * noise;

        // Value between [-2, 2]
        double trigoValue = Math.cos(2 * Math.PI * noisyLat / diameter) + Math.sin(2 * Math.PI * noisyLon / diameter);

        // Value between [0, 1]
        double ratioValue = (trigoValue + 2) / 4;

        return min + ratioValue * (max - min);
    }

    public static void generateTest(Generator netCDFGenerator, DateTime startDate, DateTime endDate, File outputFile) throws IOException, InvalidRangeException {
        float[] lats = getCoordinates(-50, 50, 100);
        float[] lons = getCoordinates(-50, 50, 100);

        int nbHours = Hours.hoursBetween(startDate, endDate).getHours();


        NetCDFDataset dataset = new NetCDFDataset(lats, lons);

        NetCDFVariable testLinearGradient = new NetCDFVariable("testLinearGradient", "Index");
        dataset.addVariable(testLinearGradient);

        NetCDFVariable testRadialGradient = new NetCDFVariable("testRadialGradient", "Index");
        dataset.addVariable(testRadialGradient);

        for (int hour=0; hour<nbHours; hour++) {
            DateTime frameDate = startDate.plusHours(hour);

            for (float lat : lats) {
                for (float lon : lons) {
                    double testLinearGradientValue = drawLinearGradient(lat, lon, 0, 10, 50, hour * 10, 0);
                    testLinearGradient.addDataPoint(frameDate, lat, lon, testLinearGradientValue);

                    double testRadialGradientValue = drawRadialGradient(lat, lon, -10, 2, 50, hour * 0.01);
                    testRadialGradient.addDataPoint(frameDate, lat, lon, testRadialGradientValue);
                }
            }
        }

        netCDFGenerator.generate(outputFile, dataset);
    }



    /**
     * Create simple NetCDF files that contains data that changes depending on its coordinates.
     * The data is very coarse, to produce small NetCDF file.
     *
     * @param netCDFGenerator The NetCDF file generator
     * @param startDate The start date, inclusive
     * @param endDate The end date, exclusive
     * @param outputFile The location on disk where to save the NetCDF file
     * @throws IOException
     * @throws InvalidRangeException
     */
    public static void generateGbrRaindow(Generator netCDFGenerator, DateTime startDate, DateTime endDate, File outputFile) throws IOException, InvalidRangeException {
        float[] lats = getCoordinates(-22, -10, 21);
        float[] lons = getCoordinates(142, 154, 21);

        double[] depths = {0, -1, -3, -10};

        int nbHours = Hours.hoursBetween(startDate, endDate).getHours();


        NetCDFDataset dataset = new NetCDFDataset(lats, lons, depths);

        NetCDFDepthVariable temp = new NetCDFDepthVariable("temperature", "C");
        dataset.addVariable(temp);

        NetCDFVariable salt = new NetCDFVariable("salinity", "PSU");
        dataset.addVariable(salt);

        NetCDFDepthVariable noise = new NetCDFDepthVariable("noise", "dbl");
        dataset.addVariable(noise);

        NetCDFDepthVariable currentU = new NetCDFDepthVariable("u", "ms-1");
        NetCDFDepthVariable currentV = new NetCDFDepthVariable("v", "ms-1");
        NetCDFVectorVariable<NetCDFDepthVariable> current =
                new NetCDFVectorVariable<NetCDFDepthVariable>("current", currentU, currentV);
        dataset.addVectorVariable(current);

        NetCDFVariable windU = new NetCDFVariable("wspeed_u", "ms-1");
        NetCDFVariable windV = new NetCDFVariable("wspeed_v", "ms-1");
        NetCDFVectorVariable<NetCDFVariable> wind =
                new NetCDFVectorVariable<NetCDFVariable>("wind", windU, windV);
        dataset.addVectorVariable(wind);

        for (int hour=0; hour<nbHours; hour++) {
            DateTime frameDate = startDate.plusHours(hour);

            if (hour != 2 && hour != 3) {
                for (float lat : lats) {
                    for (float lon : lons) {

                        // Variables with depths
                        for (double depth : depths) {
                            // Temperature
                            double tempValue = Math.abs((hour + lat + lon) % 40 - 20) - 10 + depth;
                            temp.addDataPoint(frameDate, lat, lon, depth, tempValue);

                            // Noise - Deeper = less noise
                            double noiseValue = Math.abs((hour - lat + lon + Math.random() * (6 + depth/2)) % (40 + depth) - (40 + depth)/2);
                            noise.addDataPoint(frameDate, lat, lon, depth, noiseValue);

                            // Current
                            double currentUValue = Math.sin((hour + lat + lon) / (-depth + 2)) / 2;
                            currentU.addDataPoint(frameDate, lat, lon, depth, currentUValue);

                            double currentVValue = Math.cos((hour + lat - lon) / (-depth + 2)) / 2;
                            currentV.addDataPoint(frameDate, lat, lon, depth, currentVValue);
                        }

                        // Salt contains holes in the data
                        if (hour != 5 && hour != 6) {
                            double saltValue = hour + (lat+22) + (-lon+154);
                            salt.addDataPoint(frameDate, lat, lon, saltValue);
                        }

                        // Wind
                        double windUValue = Math.abs((hour + lat + lon) % 40 - 20);
                        windU.addDataPoint(frameDate, lat, lon, windUValue);

                        double windVValue = Math.abs((hour - lat + lon) % 40 - 20);
                        windV.addDataPoint(frameDate, lat, lon, windVValue);
                    }
                }
            }
        }

        netCDFGenerator.generate(outputFile, dataset);
    }


    public static void generateMultiHypercube(Generator netCDFGenerator, DateTime startDate, DateTime endDate, File outputFile) throws IOException, InvalidRangeException {
        int nbHours = Hours.hoursBetween(startDate, endDate).getHours();
        int hourOffset = 3; // Number of hours to offset the second data hypercube

        float[] lats0 = getCoordinates(-22, -10, 21);
        float[] lons0 = getCoordinates(142, 154, 21);

        NetCDFDataset dataset0 = new NetCDFDataset(lats0, lons0);

        NetCDFVariable temp0 = new NetCDFVariable("temperature", "C");
        dataset0.addVariable(temp0);

        NetCDFVariable salt0 = new NetCDFVariable("salinity", "PSU");
        dataset0.addVariable(salt0);

        for (int hour=0; hour<nbHours; hour++) {
            DateTime frameDate = startDate.plusHours(hour);

            for (float lat : lats0) {
                for (float lon : lons0) {
                    double tempValue = Math.abs((hour + lat + lon) % 40 - 20) - 10;
                    temp0.addDataPoint(frameDate, lat, lon, tempValue);

                    // Salt contains holes in the data
                    double saltValue = hour + (lat+22) + (-lon+154) + Math.random() * 10;
                    salt0.addDataPoint(frameDate, lat, lon, saltValue);
                }
            }
        }


        float[] lats1 = getCoordinates(-24, -12, 21);
        float[] lons1 = getCoordinates(144, 156, 21);

        NetCDFDataset dataset1 = new NetCDFDataset(lats1, lons1);

        NetCDFVariable temp1 = new NetCDFVariable("temperature1", "C");
        dataset1.addVariable(temp1);

        NetCDFVariable salt1 = new NetCDFVariable("salinity1", "PSU");
        dataset1.addVariable(salt1);

        for (int hour=hourOffset; hour<nbHours+hourOffset; hour+=3) {
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
