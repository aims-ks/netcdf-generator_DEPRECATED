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
import au.gov.aims.netcdf.bean.NetCDFTimeDepthVariable;
import au.gov.aims.netcdf.bean.NetCDFTimeVariable;
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
                new DateTime(2019, 1, 3, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/test.nc"));

        Main.generateAllGbr4v2(netCDFGenerator);
    }

    public static void generateAllGbr4v2(Generator netCDFGenerator) throws IOException, InvalidRangeException {
        Main.generateGbr4v2(netCDFGenerator,
                new DateTime(2018, 11, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2018, 12, 1, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_2018-11.nc"));

        Main.generateGbr4v2(netCDFGenerator,
                new DateTime(2018, 12, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2019, 1, 1, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_2018-12.nc"));

        Main.generateGbr4v2(netCDFGenerator,
                new DateTime(2019, 1, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2019, 2, 1, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_2019-01.nc"));

        Main.generateGbr4v2(netCDFGenerator,
                new DateTime(2019, 2, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2019, 3, 1, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_2019-02.nc"));

        Main.generateGbr4v2(netCDFGenerator,
                new DateTime(2019, 3, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2019, 4, 1, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_2019-03.nc"));

        Main.generateGbr4v2(netCDFGenerator,
                new DateTime(2019, 4, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2019, 5, 1, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_2019-04.nc"));
    }



    public static void generateGbr4v2(Generator netCDFGenerator, DateTime startDate, DateTime endDate, File outputFile) throws IOException, InvalidRangeException {
        float[] lats = getCoordinates(-22, -10, 21); // y
        float[] lons = getCoordinates(142, 154, 21); // x

        // List of all depths found in GBR4 v2 files
        double[] allDepths = {-3890, -3680, -3480, -3280, -3080, -2880, -2680, -2480, -2280, -2080, -1880, -1680, -1480, -1295, -1135, -990, -865, -755, -655, -570, -495, -430, -370, -315, -270, -235, -200, -170, -145, -120, -103, -88, -73, -60, -49, -39.5, -31, -23.75, -17.75, -12.75, -8.8, -5.55, -3, -1.5, -0.5, 0.5, 1.5};

        // List of depths used in configs
        double[] usedDepths = {-1.5, -17.75, -49, -103, -200, -315};

        double[] depths = usedDepths;


        NetCDFDataset dataset = new NetCDFDataset(lats, lons, depths);

        NetCDFTimeDepthVariable tempVar = new NetCDFTimeDepthVariable("temp", "degrees C");
        dataset.addVariable(tempVar);

        NetCDFTimeDepthVariable saltVar = new NetCDFTimeDepthVariable("salt", "PSU");
        dataset.addVariable(saltVar);

        //NetCDFTimeDepthVariable RT_exposeVar = new NetCDFTimeDepthVariable("RT_expose", "DegC week");
        //dataset.addVariable(RT_exposeVar);

        NetCDFTimeVariable wspeed_uVar = new NetCDFTimeVariable("wspeed_u", "ms-1");
        NetCDFTimeVariable wspeed_vVar = new NetCDFTimeVariable("wspeed_v", "ms-1");
        dataset.addVectorVariable(new NetCDFVectorVariable<NetCDFTimeVariable>("wind", wspeed_uVar, wspeed_vVar));

        NetCDFTimeDepthVariable uVar = new NetCDFTimeDepthVariable("u", "ms-1");
        NetCDFTimeDepthVariable vVar = new NetCDFTimeDepthVariable("v", "ms-1");
        dataset.addVectorVariable(new NetCDFVectorVariable<NetCDFTimeDepthVariable>("sea_water_velocity", uVar, vVar));

        //NetCDFTimeDepthVariable dhwVar = new NetCDFTimeDepthVariable("dhw", "DegC-week");
        //dataset.addVariable(dhwVar);

        //NetCDFTimeVariable etaVar = new NetCDFTimeVariable("eta", "metre");
        //dataset.addVariable(etaVar);

        //NetCDFTimeDepthVariable temp_exposeVar = new NetCDFTimeDepthVariable("temp_expose", "DegC week");
        //dataset.addVariable(temp_exposeVar);

        NetCDFVariable botzVar = new NetCDFVariable("botz", "metre");
        dataset.addVariable(botzVar);


        int nbHours = Hours.hoursBetween(startDate, endDate).getHours();
        for (float lat : lats) {
            for (float lon : lons) {
                // Set data for NetCDFVariable
                double botzValue = lat % 10 + lon % 10;
                botzVar.addDataPoint(lat, lon, botzValue);

                for (int hour=0; hour<nbHours; hour++) {
                    DateTime frameDate = startDate.plusHours(hour);

                    // Set data for NetCDFTimeVariable

                    // Wind
                    double windUValue = Main.drawLinearGradient(lat, lon - hour, -10, -8, 100, 70, 0);
                    double windVValue = Main.drawLinearGradient(lat - hour, lon, 2, 17, 50, -20, 0);
                    wspeed_uVar.addDataPoint(lat, lon, frameDate, windUValue);
                    wspeed_vVar.addDataPoint(lat, lon, frameDate, windVValue);

                    for (double depth : depths) {
                        // Set data for NetCDFTimeDepthVariable

                        // Temperature
                        double worldTempValue = Main.drawLinearGradient(lat+45, lon, 0, 30, 180, 0, (-depth + 2) / 5000); // Hot at the equator, cold at the poles
                        double qldTempValue = Main.drawLinearGradient(lat, lon+31, -4, 4, 20, 60, (-depth + 2) / 5000); // Hotter closer to the coastline
                        double dayNight = (Math.abs((hour + 12) % 24 - 12) - 6) / 4.0; // Temperature varies +/- 1 degree between day and night
                        tempVar.addDataPoint(lat, lon, frameDate, depth, worldTempValue + qldTempValue + dayNight + depth/10);

                        // Salt
                        double saltValue = Main.drawRadialGradient(lat+(hour/4.0f), lon-(hour/4.0f), 32, 36, 10, (-depth + 2) / 5000);
                        saltVar.addDataPoint(lat, lon, frameDate, depth, saltValue);

                        // Current
                        double currentUValue = Main.drawRadialGradient(lat-(hour/4.0f), lon+(hour/4.0f), -0.6, 0.6, 15, (-depth + 2) / 5000);
                        double currentVValue = Main.drawRadialGradient(lat+(hour/4.0f), lon+(hour/4.0f), -0.6, 0.6, 15, (-depth + 2) / 5000);
                        uVar.addDataPoint(lat, lon, frameDate, depth, currentUValue);
                        vVar.addDataPoint(lat, lon, frameDate, depth, currentVValue);
                    }
                }
            }
        }

        netCDFGenerator.generate(outputFile, dataset);
    }




    public static void generateGbr1v2(Generator netCDFGenerator, DateTime startDate, DateTime endDate, File outputFile) throws IOException, InvalidRangeException {
        float[] lats = getCoordinates(-22, -10, 21);
        float[] lons = getCoordinates(142, 154, 21);

        // List of all depths found in GBR1 2.0 files
        double[] allDepths = {-3885, -3660, -3430, -3195, -2965, -2730, -2495, -2265, -2035, -1805, -1575, -1345, -1115, -960, -860, -750, -655, -570, -495, -430, -370, -315, -270, -230, -195, -165, -140, -120, -103, -88, -73, -60, -49, -39.5, -31, -24, -18, -13, -9, -5.35, -2.35, -0.5, 0.5, 1.5};

        // List of depths used in configs
        double[] usedDepths = {-2.35, -5.35, -18};

        double[] depths = usedDepths;
    }

    public static void generateGbr4bgc(Generator netCDFGenerator, DateTime startDate, DateTime endDate, File outputFile) throws IOException, InvalidRangeException {
        float[] lats = getCoordinates(-22, -10, 21);
        float[] lons = getCoordinates(142, 154, 21);

        // List of all depths found in GBR4 BGC files
        double[] allDepths = {-3890, -3680, -3480, -3280, -3080, -2880, -2680, -2480, -2280, -2080, -1880, -1680, -1480, -1295, -1135, -990, -865, -755, -655, -570, -495, -430, -370, -315, -270, -235, -200, -170, -145, -120, -103, -88, -73, -60, -49, -39.5, -31, -23.75, -17.75, -12.75, -8.8, -5.55, -3, -1.5, -0.5, 0.5, 1.5};

        // List of depths used in configs
        double[] usedDepths = {-1.5};

        double[] depths = usedDepths;
    }


    /**
     * Used to test this library
     * @param netCDFGenerator The NetCDF file generator
     * @param startDate The start date, inclusive
     * @param endDate The end date, exclusive
     * @param outputFile The location on disk where to save the NetCDF file
     * @throws IOException
     * @throws InvalidRangeException
     */
    public static void generateTest(Generator netCDFGenerator, DateTime startDate, DateTime endDate, File outputFile) throws IOException, InvalidRangeException {
        float[] lats = getCoordinates(-50, 50, 100);
        float[] lons = getCoordinates(-50, 50, 100);

        NetCDFDataset dataset = new NetCDFDataset(lats, lons);

        NetCDFVariable botzVar = new NetCDFVariable("botz", "metre");
        dataset.addVariable(botzVar);

        NetCDFVariable botz2Var = new NetCDFVariable("botz2", "metre");
        dataset.addVariable(botz2Var);

        NetCDFTimeVariable testLinearGradient = new NetCDFTimeVariable("testLinearGradient", "Index");
        dataset.addVariable(testLinearGradient);

        NetCDFTimeVariable testRadialGradient = new NetCDFTimeVariable("testRadialGradient", "Index");
        dataset.addVariable(testRadialGradient);

        NetCDFTimeVariable testWaveU  = new NetCDFTimeVariable("testWaveU", "m");
        NetCDFTimeVariable testWaveV  = new NetCDFTimeVariable("testWaveV", "m");
        dataset.addVectorVariable(new NetCDFVectorVariable<NetCDFTimeVariable>("testWave", testWaveU, testWaveV));

        int nbHours = Hours.hoursBetween(startDate, endDate).getHours();
        for (float lat : lats) {
            for (float lon : lons) {
                double botzValue = lat % 10 + lon % 10;
                botzVar.addDataPoint(lat, lon, botzValue);
                botz2Var.addDataPoint(lat, lon, -botzValue);

                for (int hour=0; hour<nbHours; hour++) {
                    DateTime frameDate = startDate.plusHours(hour);

                    double testLinearGradientValue = Main.drawLinearGradient(lat, lon, 0, 10, 50, hour * (360.0/nbHours), 0);
                    testLinearGradient.addDataPoint(lat, lon, frameDate, testLinearGradientValue);

                    double testRadialGradientValue = Main.drawRadialGradient(lat, lon, -10, 2, 50, Math.abs(Math.abs(hour-nbHours/2.0)-nbHours/2.0) * 0.01);
                    testRadialGradient.addDataPoint(lat, lon, frameDate, testRadialGradientValue);

                    double testWaveUValue = Main.drawLinearGradient(lat, lon - hour, -4, 0, 100, 70, 0);;
                    double testWaveVValue = Main.drawLinearGradient(lat - hour, lon, 2, 10, 50, -20, 0);
                    testWaveU.addDataPoint(lat, lon, frameDate, testWaveUValue);
                    testWaveV.addDataPoint(lat, lon, frameDate, testWaveVValue);
                }
            }
        }

        netCDFGenerator.generate(outputFile, dataset);
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
     * @param frequency Distance between gradient, in lon / lat degree
     * @param angle The angle of the gradient, in degree. 0 for horizontal, turning clockwise.
     * @param noise Level of noise, between [0, 1]
     * @return A value between [min, max] for the given coordinate.
     */
    public static double drawLinearGradient(float lat, float lon, double min, double max, double frequency, double angle, double noise) {
        double noisyLat = lat + (Math.random() - 0.5) * 90 * noise;
        double noisyLon = lon + (Math.random() - 0.5) * 90 * noise;

        double radianAngle = Math.toRadians(angle);

        double latRatio = Math.cos(radianAngle);
        double lonRatio = Math.sin(radianAngle);

        // Value between [1, -1]
        double trigoValue = Math.sin(2 * Math.PI * (noisyLat * latRatio + noisyLon * lonRatio) / frequency);

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

        // Value between [min, max]
        return min + ratioValue * (max - min);
    }













    // TODO DELETE

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

        NetCDFTimeDepthVariable temp = new NetCDFTimeDepthVariable("temperature", "C");
        dataset.addVariable(temp);

        NetCDFTimeVariable salt = new NetCDFTimeVariable("salinity", "PSU");
        dataset.addVariable(salt);

        NetCDFTimeDepthVariable noise = new NetCDFTimeDepthVariable("noise", "dbl");
        dataset.addVariable(noise);

        NetCDFTimeDepthVariable currentU = new NetCDFTimeDepthVariable("u", "ms-1");
        NetCDFTimeDepthVariable currentV = new NetCDFTimeDepthVariable("v", "ms-1");
        NetCDFVectorVariable<NetCDFTimeDepthVariable> current =
                new NetCDFVectorVariable<NetCDFTimeDepthVariable>("current", currentU, currentV);
        dataset.addVectorVariable(current);

        NetCDFTimeVariable windU = new NetCDFTimeVariable("wspeed_u", "ms-1");
        NetCDFTimeVariable windV = new NetCDFTimeVariable("wspeed_v", "ms-1");
        NetCDFVectorVariable<NetCDFTimeVariable> wind =
                new NetCDFVectorVariable<NetCDFTimeVariable>("wind", windU, windV);
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
                            temp.addDataPoint(lat, lon, frameDate, depth, tempValue);

                            // Noise - Deeper = less noise
                            double noiseValue = Math.abs((hour - lat + lon + Math.random() * (6 + depth/2)) % (40 + depth) - (40 + depth)/2);
                            noise.addDataPoint(lat, lon, frameDate, depth, noiseValue);

                            // Current
                            double currentUValue = Math.sin((hour + lat + lon) / (-depth + 2)) / 2;
                            currentU.addDataPoint(lat, lon, frameDate, depth, currentUValue);

                            double currentVValue = Math.cos((hour + lat - lon) / (-depth + 2)) / 2;
                            currentV.addDataPoint(lat, lon, frameDate, depth, currentVValue);
                        }

                        // Salt contains holes in the data
                        if (hour != 5 && hour != 6) {
                            double saltValue = hour + (lat+22) + (-lon+154);
                            salt.addDataPoint(lat, lon, frameDate, saltValue);
                        }

                        // Wind
                        double windUValue = Math.abs((hour + lat + lon) % 40 - 20);
                        windU.addDataPoint(lat, lon, frameDate, windUValue);

                        double windVValue = Math.abs((hour - lat + lon) % 40 - 20);
                        windV.addDataPoint(lat, lon, frameDate, windVValue);
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

        NetCDFTimeVariable temp0 = new NetCDFTimeVariable("temperature", "C");
        dataset0.addVariable(temp0);

        NetCDFTimeVariable salt0 = new NetCDFTimeVariable("salinity", "PSU");
        dataset0.addVariable(salt0);

        for (int hour=0; hour<nbHours; hour++) {
            DateTime frameDate = startDate.plusHours(hour);

            for (float lat : lats0) {
                for (float lon : lons0) {
                    double tempValue = Math.abs((hour + lat + lon) % 40 - 20) - 10;
                    temp0.addDataPoint(lat, lon, frameDate, tempValue);

                    // Salt contains holes in the data
                    double saltValue = hour + (lat+22) + (-lon+154) + Math.random() * 10;
                    salt0.addDataPoint(lat, lon, frameDate, saltValue);
                }
            }
        }


        float[] lats1 = getCoordinates(-24, -12, 21);
        float[] lons1 = getCoordinates(144, 156, 21);

        NetCDFDataset dataset1 = new NetCDFDataset(lats1, lons1);

        NetCDFTimeVariable temp1 = new NetCDFTimeVariable("temperature1", "C");
        dataset1.addVariable(temp1);

        NetCDFTimeVariable salt1 = new NetCDFTimeVariable("salinity1", "PSU");
        dataset1.addVariable(salt1);

        for (int hour=hourOffset; hour<nbHours+hourOffset; hour+=3) {
            DateTime frameDate = startDate.plusHours(hour);

            for (float lat : lats1) {
                for (float lon : lons1) {
                    double tempValue = Math.abs((hour + lat + lon) % 40 - 20) - 10;
                    temp1.addDataPoint(lat, lon, frameDate, tempValue);

                    // Salt contains holes in the data
                    double saltValue = hour + (lat+22) + (-lon+154);
                    salt1.addDataPoint(lat, lon, frameDate, saltValue);
                }
            }
        }


        netCDFGenerator.generate(outputFile, dataset0, dataset1);
    }
}
