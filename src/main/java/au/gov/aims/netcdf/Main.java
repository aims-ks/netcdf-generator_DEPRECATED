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
import java.util.Random;

/*
 * TODO
 * [X] Create simple data file covering GBR
 * [X] Create file with time gaps
 * [X] Create file with multiple hypercubes of data
 * [X] Add depth support
 * [X] Add vector variable support
 *
 * [~] Create NetCDF files and re-write NcAnimate tests
 *
 * NetCDF file samples:
 *     https://www.unidata.ucar.edu/software/netcdf/examples/files.html
 */

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class);
    private static final DateTimeZone TIMEZONE_BRISBANE = DateTimeZone.forID("Australia/Brisbane");

    public static void main(String ... args) throws Exception {
        Generator netCDFGenerator = new Generator();


        // Test file
        Main.generateTest(netCDFGenerator,
                new DateTime(2019, 1, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2019, 1, 3, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/test.nc"));


        // GBR4 Hydro v2
        Main.generateGbr4v2(netCDFGenerator,
                new DateTime(2014, 12, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2014, 12, 2, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_v2_2014-12-01.nc"), false);

        Main.generateGbr4v2(netCDFGenerator,
                new DateTime(2014, 12, 2, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2014, 12, 3, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_v2_2014-12-02_missingFrames.nc"), true);


        // Multi-hypercubes of data
        Main.generateGbr4v2MultiHypercubes(netCDFGenerator,
                new DateTime(2000, 1, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2000, 1, 2, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_v2_2000-01-01_multiHypercubes.nc"));


        // GBR4 BGC
        Main.generateGbr4bgc(netCDFGenerator,
                new DateTime(2014, 12, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2015, 1, 1, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_bgc_2014-12.nc"));
    }


    public static void generateGbr4v2(Generator netCDFGenerator, DateTime startDate, DateTime endDate, File outputFile, boolean missingData) throws IOException, InvalidRangeException {
        Random rng = new Random(4280);

        float[] lats = getCoordinates(-28, -7.6f, 15); // y
        float[] lons = getCoordinates(142, 156, 10); // x

        // List of all depths found in GBR4 v2 files
        double[] allDepths = {-3890, -3680, -3480, -3280, -3080, -2880, -2680, -2480, -2280, -2080, -1880, -1680, -1480, -1295, -1135, -990, -865, -755, -655, -570, -495, -430, -370, -315, -270, -235, -200, -170, -145, -120, -103, -88, -73, -60, -49, -39.5, -31, -23.75, -17.75, -12.75, -8.8, -5.55, -3, -1.5, -0.5, 0.5, 1.5};

        // List of depths used in configs
        double[] usedDepths = {-1.5, -17.75, -49, -103, -200, -315};

        double[] depths = usedDepths;


        NetCDFDataset dataset = new NetCDFDataset();
        dataset.setGlobalAttribute("metadata_link", "http://marlin.csiro.au/geonetwork/srv/eng/search?&uuid=72020224-f086-434a-bbe9-a222c8e5cf0d");
        dataset.setGlobalAttribute("title", "GBR4 Hydro");
        dataset.setGlobalAttribute("paramhead", "GBR 4km resolution grid");

        NetCDFTimeDepthVariable tempVar = new NetCDFTimeDepthVariable("temp", "degrees C");
        tempVar.setAttribute("long_name", "Temperature");
        dataset.addVariable(tempVar);

        NetCDFTimeDepthVariable saltVar = new NetCDFTimeDepthVariable("salt", "PSU");
        saltVar.setAttribute("long_name", "Salinity");
        dataset.addVariable(saltVar);

        //NetCDFTimeDepthVariable RT_exposeVar = new NetCDFTimeDepthVariable("RT_expose", "DegC week");
        //dataset.addVariable(RT_exposeVar);

        NetCDFTimeVariable wspeed_uVar = new NetCDFTimeVariable("wspeed_u", "ms-1");
        wspeed_uVar.setAttribute("long_name", "eastward_wind");
        NetCDFTimeVariable wspeed_vVar = new NetCDFTimeVariable("wspeed_v", "ms-1");
        wspeed_vVar.setAttribute("long_name", "northward_wind");
        dataset.addVectorVariable(new NetCDFVectorVariable<NetCDFTimeVariable>("wind", wspeed_uVar, wspeed_vVar));

        NetCDFTimeDepthVariable uVar = new NetCDFTimeDepthVariable("u", "ms-1");
        uVar.setAttribute("long_name", "Eastward current");
        NetCDFTimeDepthVariable vVar = new NetCDFTimeDepthVariable("v", "ms-1");
        vVar.setAttribute("long_name", "Northward current");
        dataset.addVectorVariable(new NetCDFVectorVariable<NetCDFTimeDepthVariable>("sea_water_velocity", uVar, vVar));

        //NetCDFTimeDepthVariable dhwVar = new NetCDFTimeDepthVariable("dhw", "DegC-week");
        //dataset.addVariable(dhwVar);

        //NetCDFTimeVariable etaVar = new NetCDFTimeVariable("eta", "metre");
        //dataset.addVariable(etaVar);

        //NetCDFTimeDepthVariable temp_exposeVar = new NetCDFTimeDepthVariable("temp_expose", "DegC week");
        //dataset.addVariable(temp_exposeVar);

        NetCDFVariable botzVar = new NetCDFVariable("botz", "metre");
        botzVar.setAttribute("long_name", "Depth of sea-bed");
        dataset.addVariable(botzVar);


        int nbHours = Hours.hoursBetween(startDate, endDate).getHours();
        for (float lat : lats) {
            for (float lon : lons) {
                // Set data for NetCDFVariable
                double botzValue = lat % 10 + lon % 10;
                botzVar.addDataPoint(lat, lon, botzValue);

                for (int hour=0; hour<nbHours; hour++) {
                    DateTime frameDate = startDate.plusHours(hour);

                    // Skip some frames (if needed)
                    // NOTE: Skipped frames were chosen to highlight different scenarios, verified in tests.
                    boolean skipTemp = false;
                    boolean skipWind = false;
                    boolean skipSalt = false;
                    boolean skipCurrent = false;
                    if (missingData) {
                        if (hour == 2 || hour == 3) {
                            continue;
                        }

                        if (hour == 5) {
                            skipTemp = true;
                        }
                        if (hour == 1) {
                            skipWind = true;
                        }
                        if (hour == 7 || hour == 8) {
                            skipSalt = true;
                        }
                        if (hour == 8 || hour == 9) {
                            skipCurrent = true;
                        }
                    }

                    // Set data for NetCDFTimeVariable

                    // Wind
                    if (!skipWind) {
                        double windUValue = Main.drawLinearGradient(rng, lat, lon - hour, -10, -8, 100, 70, 0);
                        double windVValue = Main.drawLinearGradient(rng, lat - hour, lon, 2, 17, 50, -20, 0);
                        wspeed_uVar.addDataPoint(lat, lon, frameDate, windUValue);
                        wspeed_vVar.addDataPoint(lat, lon, frameDate, windVValue);
                    }

                    for (double depth : depths) {
                        // Set data for NetCDFTimeDepthVariable

                        // Temperature
                        if (!skipTemp) {
                            double worldTempValue = Main.drawLinearGradient(rng, lat+45, lon, 0, 30, 180, 0, (-depth + 2) / 5000); // Hot at the equator, cold at the poles
                            double qldTempValue = Main.drawLinearGradient(rng, lat, lon+31, -4, 4, 20, 60, (-depth + 2) / 5000); // Hotter closer to the coastline
                            double dayNight = (Math.abs((hour + 12) % 24 - 12) - 6) / 4.0; // Temperature varies +/- 1 degree between day and night
                            tempVar.addDataPoint(lat, lon, frameDate, depth, worldTempValue + qldTempValue + dayNight + depth/10);
                        }

                        // Salt
                        if (!skipSalt) {
                            double saltValue = Main.drawRadialGradient(rng, lat+(hour/4.0f), lon-(hour/4.0f), 32, 36, 10, (-depth + 2) / 5000);
                            saltVar.addDataPoint(lat, lon, frameDate, depth, saltValue);
                        }

                        // Current
                        if (!skipCurrent) {
                            double currentUValue = Main.drawRadialGradient(rng, lat-(hour/4.0f), lon+(hour/4.0f), -0.6, 0.6, 15, (-depth + 2) / 5000);
                            double currentVValue = Main.drawRadialGradient(rng, lat+(hour/4.0f), lon+(hour/4.0f), -0.6, 0.6, 15, (-depth + 2) / 5000);
                            uVar.addDataPoint(lat, lon, frameDate, depth, currentUValue);
                            vVar.addDataPoint(lat, lon, frameDate, depth, currentVValue);
                        }
                    }
                }
            }
        }

        netCDFGenerator.generate(outputFile, dataset);
    }

    public static void generateGbr4v2MultiHypercubes(Generator netCDFGenerator, DateTime startDate, DateTime endDate, File outputFile) throws IOException, InvalidRangeException {
        Random rng = new Random(5610);

        float[] lats0 = getCoordinates(-26, -7.6f, 15); // y
        float[] lons0 = getCoordinates(142, 154, 10); // x
        double[] depths0 = {-1.5, -17.75, -49};

        float[] lats1 = getCoordinates(-28, -9.6f, 30); // y
        float[] lons1 = getCoordinates(144, 156, 20); // x

        double[] depths1 = {-2.35, -18, -50};


        NetCDFDataset dataset0 = new NetCDFDataset();
        dataset0.setGlobalAttribute("metadata_link", "http://marlin.csiro.au/geonetwork/srv/eng/search?&uuid=72020224-f086-434a-bbe9-a222c8e5cf0d");
        dataset0.setGlobalAttribute("title", "Multi Hypercube");
        dataset0.setGlobalAttribute("paramhead", "GBR 4km and 1km resolution grid");

        NetCDFTimeDepthVariable tempVar = new NetCDFTimeDepthVariable("temp", "degrees C");
        tempVar.setAttribute("long_name", "Temperature");
        dataset0.addVariable(tempVar);

        NetCDFTimeVariable wspeed_uVar = new NetCDFTimeVariable("wspeed_u", "ms-1");
        wspeed_uVar.setAttribute("long_name", "eastward_wind");
        NetCDFTimeVariable wspeed_vVar = new NetCDFTimeVariable("wspeed_v", "ms-1");
        wspeed_vVar.setAttribute("long_name", "northward_wind");
        dataset0.addVectorVariable(new NetCDFVectorVariable<NetCDFTimeVariable>("wind", wspeed_uVar, wspeed_vVar));


        NetCDFDataset dataset1 = new NetCDFDataset();

        NetCDFTimeDepthVariable saltVar = new NetCDFTimeDepthVariable("salt", "PSU");
        saltVar.setAttribute("long_name", "Salinity");
        dataset1.addVariable(saltVar);

        NetCDFTimeDepthVariable uVar = new NetCDFTimeDepthVariable("u", "ms-1");
        uVar.setAttribute("long_name", "Eastward current");
        NetCDFTimeDepthVariable vVar = new NetCDFTimeDepthVariable("v", "ms-1");
        vVar.setAttribute("long_name", "Northward current");
        dataset1.addVectorVariable(new NetCDFVectorVariable<NetCDFTimeDepthVariable>("sea_water_velocity", uVar, vVar));



        int nbHours0 = Hours.hoursBetween(startDate, endDate).getHours();

        for (float lat : lats0) {
            for (float lon : lons0) {
                for (int hour=0; hour<nbHours0; hour++) {
                    DateTime frameDate = startDate.plusHours(hour);

                    // Set data for NetCDFTimeVariable

                    // Wind
                    double windUValue = Main.drawLinearGradient(rng, lat, lon - hour, -10, -8, 100, 70, 0);
                    double windVValue = Main.drawLinearGradient(rng, lat - hour, lon, 2, 17, 50, -20, 0);
                    wspeed_uVar.addDataPoint(lat, lon, frameDate, windUValue);
                    wspeed_vVar.addDataPoint(lat, lon, frameDate, windVValue);

                    for (double depth : depths0) {
                        // Set data for NetCDFTimeDepthVariable

                        // Temperature
                        double worldTempValue = Main.drawLinearGradient(rng, lat+45, lon, 0, 30, 180, 0, (-depth + 2) / 5000); // Hot at the equator, cold at the poles
                        double qldTempValue = Main.drawLinearGradient(rng, lat, lon+31, -4, 4, 20, 60, (-depth + 2) / 5000); // Hotter closer to the coastline
                        double dayNight = (Math.abs((hour + 12) % 24 - 12) - 6) / 4.0; // Temperature varies +/- 1 degree between day and night
                        tempVar.addDataPoint(lat, lon, frameDate, depth, worldTempValue + qldTempValue + dayNight + depth/10);
                    }
                }
            }
        }

        int nbHours1 = Hours.hoursBetween(startDate, endDate).getHours();
        for (float lat : lats1) {
            for (float lon : lons1) {
                for (int hour=2; hour<nbHours1; hour+=3) {
                    DateTime frameDate = startDate.plusHours(hour);

                    for (double depth : depths1) {
                        // Set data for NetCDFTimeDepthVariable

                        // Salt
                        double saltValue = Main.drawRadialGradient(rng, lat+(hour/4.0f), lon-(hour/4.0f), 32, 36, 10, (-depth + 2) / 5000);
                        saltVar.addDataPoint(lat, lon, frameDate, depth, saltValue);

                        // Current
                        double currentUValue = Main.drawRadialGradient(rng, lat-(hour/4.0f), lon+(hour/4.0f), -0.6, 0.6, 15, (-depth + 2) / 5000);
                        double currentVValue = Main.drawRadialGradient(rng, lat+(hour/4.0f), lon+(hour/4.0f), -0.6, 0.6, 15, (-depth + 2) / 5000);
                        uVar.addDataPoint(lat, lon, frameDate, depth, currentUValue);
                        vVar.addDataPoint(lat, lon, frameDate, depth, currentVValue);
                    }
                }
            }
        }

        netCDFGenerator.generate(outputFile, dataset0, dataset1);
    }

    /**
     * Daily data
     * @param netCDFGenerator
     * @param startDate
     * @param endDate
     * @param outputFile
     * @throws IOException
     * @throws InvalidRangeException
     */
    public static void generateGbr4bgc(Generator netCDFGenerator, DateTime startDate, DateTime endDate, File outputFile) throws IOException, InvalidRangeException {
        Random rng = new Random(3958);

        float[] lats = getCoordinates(-28, -7.6f, 15); // y
        float[] lons = getCoordinates(142, 156, 10); // x

        // List of all depths found in GBR4 BGC files
        double[] allDepths = {-3890, -3680, -3480, -3280, -3080, -2880, -2680, -2480, -2280, -2080, -1880, -1680, -1480, -1295, -1135, -990, -865, -755, -655, -570, -495, -430, -370, -315, -270, -235, -200, -170, -145, -120, -103, -88, -73, -60, -49, -39.5, -31, -23.75, -17.75, -12.75, -8.8, -5.55, -3, -1.5, -0.5, 0.5, 1.5};

        // List of depths used in configs
        double[] usedDepths = {-1.5};

        double[] depths = usedDepths;


        NetCDFDataset dataset = new NetCDFDataset();
        dataset.setGlobalAttribute("title", "GBR4 BGC (Spectral) Transport");
        dataset.setGlobalAttribute("paramhead", "GBR 4km resolution grid");

        // True colour variables
        NetCDFTimeVariable r470Var = new NetCDFTimeVariable("R_470", "sr-1");
        r470Var.setAttribute("long_name", "Rrs_470 nm");
        dataset.addVariable(r470Var);

        NetCDFTimeVariable r555Var = new NetCDFTimeVariable("R_555", "sr-1");
        r555Var.setAttribute("long_name", "Rrs_555 nm");
        dataset.addVariable(r555Var);

        NetCDFTimeVariable r645Var = new NetCDFTimeVariable("R_645", "sr-1");
        r645Var.setAttribute("long_name", "Rrs_645 nm");
        dataset.addVariable(r645Var);


        int nbHours = Hours.hoursBetween(startDate, endDate).getHours();
        for (float lat : lats) {
            for (float lon : lons) {
                for (int hour=0; hour<nbHours; hour+=24) {
                    DateTime frameDate = startDate.plusHours(hour);

                    // Set data for NetCDFTimeVariable

                    // True colour variables
                    // NOTE: The colour turned out to be quite good with those ratios:
                    //     Blue wavelength goes deep:                  values [0, 1]
                    //     Green penetrate about half as deep as blue: values [0, 0.5]
                    //     Red get pretty much all absorb:             values [0, 0.1]
                    double r470Value = Main.drawLinearGradient(rng, lat, lon+102, 0, 1, 360, 90, 0.05); // Violet (used for Blue)
                    double r555Value = Main.drawLinearGradient(rng, lat, lon+102, 0, 0.5, 360, 90, 0.05); // Green
                    double r645Value = Main.drawLinearGradient(rng, lat, lon+102, 0, 0.1, 360, 90, 0.05); // Red
                    r470Var.addDataPoint(lat, lon, frameDate, r470Value);
                    r555Var.addDataPoint(lat, lon, frameDate, r555Value);
                    r645Var.addDataPoint(lat, lon, frameDate, r645Value);

                    for (double depth : depths) {
                        // Set data for NetCDFTimeDepthVariable

                        // TODO
                    }
                }
            }
        }

        netCDFGenerator.generate(outputFile, dataset);
    }




    public static void generateGbr1v2(Generator netCDFGenerator, DateTime startDate, DateTime endDate, File outputFile) throws IOException, InvalidRangeException {
        Random rng = new Random(9833);

        float[] lats = getCoordinates(-28, -7.6f, 30); // y
        float[] lons = getCoordinates(142, 156, 20); // x

        // List of all depths found in GBR1 2.0 files
        double[] allDepths = {-3885, -3660, -3430, -3195, -2965, -2730, -2495, -2265, -2035, -1805, -1575, -1345, -1115, -960, -860, -750, -655, -570, -495, -430, -370, -315, -270, -230, -195, -165, -140, -120, -103, -88, -73, -60, -49, -39.5, -31, -24, -18, -13, -9, -5.35, -2.35, -0.5, 0.5, 1.5};

        // List of depths used in configs
        double[] usedDepths = {-2.35, -5.35, -18};

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
        Random rng = new Random(6930);

        float[] lats = getCoordinates(-50, 50, 100);
        float[] lons = getCoordinates(-50, 50, 100);

        NetCDFDataset dataset = new NetCDFDataset();

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

                    double testLinearGradientValue = Main.drawLinearGradient(rng, lat, lon, 0, 10, 50, hour * (360.0/nbHours), 0);
                    testLinearGradient.addDataPoint(lat, lon, frameDate, testLinearGradientValue);

                    double testRadialGradientValue = Main.drawRadialGradient(rng, lat, lon, -10, 2, 50, Math.abs(Math.abs(hour-nbHours/2.0)-nbHours/2.0) * 0.01);
                    testRadialGradient.addDataPoint(lat, lon, frameDate, testRadialGradientValue);

                    double testWaveUValue = Main.drawLinearGradient(rng, lat, lon - hour, -4, 0, 100, 70, 0);;
                    double testWaveVValue = Main.drawLinearGradient(rng, lat - hour, lon, 2, 10, 50, -20, 0);
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
     * @param rng Random number generator
     * @param lat Latitude coordinate, in degree
     * @param lon Longitude coordinate, in degree
     * @param min Minimum output value
     * @param max Maximum output value
     * @param frequency Distance between gradient, in lon / lat degree
     * @param angle The angle of the gradient, in degree. 0 for horizontal, turning clockwise.
     * @param noise Level of noise, between [0, 1]
     * @return A value between [min, max] for the given coordinate.
     */
    public static double drawLinearGradient(Random rng, float lat, float lon, double min, double max, double frequency, double angle, double noise) {
        double noisyLat = lat + (rng.nextDouble() - 0.5) * 90 * noise;
        double noisyLon = lon + (rng.nextDouble() - 0.5) * 90 * noise;

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
     * @param rng Random number generator
     * @param lat Latitude coordinate, in degree
     * @param lon Longitude coordinate, in degree
     * @param min Minimum output value
     * @param max Maximum output value
     * @param diameter Diameter of the circles
     * @param noise Level of noise, between [0, 1]
     * @return A value between [min, max] for the given coordinate.
     */
    public static double drawRadialGradient(Random rng, float lat, float lon, double min, double max, double diameter, double noise) {
        double noisyLat = lat + (rng.nextDouble() - 0.5) * 90 * noise;
        double noisyLon = lon + (rng.nextDouble() - 0.5) * 90 * noise;

        // Value between [-2, 2]
        double trigoValue = Math.cos(2 * Math.PI * noisyLat / diameter) + Math.sin(2 * Math.PI * noisyLon / diameter);

        // Value between [0, 1]
        double ratioValue = (trigoValue + 2) / 4;

        // Value between [min, max]
        return min + ratioValue * (max - min);
    }
}
