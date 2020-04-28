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
 * Class used to generate small NetCDF files used with NcAnimate tests
 *
 * NetCDF file samples:
 *     https://www.unidata.ucar.edu/software/netcdf/examples/files.html
 */

public class NcAnimateGenerator {
    private static final Logger LOGGER = Logger.getLogger(NcAnimateGenerator.class);
    private static final DateTimeZone TIMEZONE_BRISBANE = DateTimeZone.forID("Australia/Brisbane");

    public static void main(String ... args) throws Exception {
        Generator netCDFGenerator = new Generator();


        // GBR4 Hydro v2
        NcAnimateGenerator.generateGbr4v2(netCDFGenerator,
                new DateTime(2014, 12, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2014, 12, 2, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_v2_2014-12-01.nc"), false);

        NcAnimateGenerator.generateGbr4v2(netCDFGenerator,
                new DateTime(2014, 12, 2, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2014, 12, 3, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_v2_2014-12-02_missingFrames.nc"), true);

        // To be used as a replacement for small.nc
        NcAnimateGenerator.generateGbr4v2(netCDFGenerator,
                new DateTime(2010, 9, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2010, 9, 1, 2, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_v2_2010-09-01_00h00-02h00.nc"), true);


        // GBR1 Hydro v2
        NcAnimateGenerator.generateGbr1v2(netCDFGenerator,
                new DateTime(2014, 12, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2014, 12, 2, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr1_2014-12-01.nc"));

        NcAnimateGenerator.generateGbr1v2(netCDFGenerator,
                new DateTime(2014, 12, 2, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2014, 12, 3, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr1_2014-12-02.nc"));


        // Multi-hypercubes of data
        NcAnimateGenerator.generateGbr4v2MultiHypercubes(netCDFGenerator,
                new DateTime(2000, 1, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2000, 1, 2, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_v2_2000-01-01_multiHypercubes.nc"));


        // GBR4 BGC
        NcAnimateGenerator.generateGbr4bgc(netCDFGenerator,
                new DateTime(2014, 12, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2015, 1, 1, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_bgc_2014-12.nc"));


        // NOAA
        NcAnimateGenerator.generateNoaa(netCDFGenerator,
                new DateTime(2014, 12, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2015, 1, 1, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/multi_1.glo_30m.dp.201412.nc"),
                new File("/tmp/multi_1.glo_30m.hs.201412.nc"));
    }

    public static void generateGbr4v2(Generator netCDFGenerator, DateTime startDate, DateTime endDate, File outputFile, boolean missingData) throws IOException, InvalidRangeException {
        NcAnimateGenerator.generateGbr4v2(netCDFGenerator, startDate, endDate, outputFile, missingData, 4280);
    }

    public static void generateGbr4v2(Generator netCDFGenerator, DateTime startDate, DateTime endDate, File outputFile, boolean missingData, long seed) throws IOException, InvalidRangeException {
        Random rng = new Random(seed);

        float[] lats = Generator.getCoordinates(-28, -7.6f, 15); // y
        float[] lons = Generator.getCoordinates(142, 156, 10); // x

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


        int startHour = Hours.hoursBetween(dataset.getTimeEpoch(), startDate).getHours();
        int endHour = Hours.hoursBetween(dataset.getTimeEpoch(), endDate).getHours();
        for (float lat : lats) {
            for (float lon : lons) {
                // Set data for NetCDFVariable
                double botzValue = lat % 10 + lon % 10;
                botzVar.addDataPoint(lat, lon, botzValue);

                for (int hour=startHour; hour<endHour; hour++) {
                    DateTime frameDate = dataset.getTimeEpoch().plusHours(hour);

                    // Skip some frames (if needed)
                    // NOTE: Skipped frames were chosen to highlight different scenarios, verified in tests.
                    boolean skipTemp = false;
                    boolean skipWind = false;
                    boolean skipSalt = false;
                    boolean skipCurrent = false;
                    if (missingData) {
                        if (hour == startHour+2 || hour == startHour+3) {
                            continue;
                        }

                        if (hour == startHour+5) {
                            skipTemp = true;
                        }
                        if (hour == startHour+1) {
                            skipWind = true;
                        }
                        if (hour == startHour+7 || hour == startHour+8) {
                            skipSalt = true;
                        }
                        if (hour == startHour+8 || hour == startHour+9) {
                            skipCurrent = true;
                        }
                    }

                    // Set data for NetCDFTimeVariable

                    // Wind
                    if (!skipWind) {
                        double windUValue = Generator.drawLinearGradient(rng, lat, lon - hour, -10, -8, 100, 70, 0);
                        double windVValue = Generator.drawLinearGradient(rng, lat - hour, lon, 2, 17, 50, -20, 0);
                        wspeed_uVar.addDataPoint(lat, lon, frameDate, windUValue);
                        wspeed_vVar.addDataPoint(lat, lon, frameDate, windVValue);
                    }

                    for (double depth : depths) {
                        // Set data for NetCDFTimeDepthVariable

                        // Temperature
                        if (!skipTemp) {
                            double worldTempValue = Generator.drawLinearGradient(rng, lat+45, lon, 0, 30, 180, 0, (-depth + 2) / 5000); // Hot at the equator, cold at the poles
                            double qldTempValue = Generator.drawLinearGradient(rng, lat, lon+31, -4, 4, 20, 60, (-depth + 2) / 5000); // Hotter closer to the coastline
                            double dayNight = (Math.abs((hour + 12) % 24 - 12) - 6) / 4.0; // Temperature varies +/- 1 degree between day and night
                            tempVar.addDataPoint(lat, lon, frameDate, depth, worldTempValue + qldTempValue + dayNight + depth/10);
                        }

                        // Salt
                        if (!skipSalt) {
                            double saltValue = Generator.drawRadialGradient(rng, lat+(hour/4.0f), lon-(hour/4.0f), 32, 36, 10, (-depth + 2) / 5000);
                            saltVar.addDataPoint(lat, lon, frameDate, depth, saltValue);
                        }

                        // Current
                        if (!skipCurrent) {
                            double currentUValue = Generator.drawRadialGradient(rng, lat-(hour/4.0f), lon+(hour/4.0f), -0.6, 0.6, 15, (-depth + 2) / 5000);
                            double currentVValue = Generator.drawRadialGradient(rng, lat+(hour/4.0f), lon+(hour/4.0f), -0.6, 0.6, 15, (-depth + 2) / 5000);
                            uVar.addDataPoint(lat, lon, frameDate, depth, currentUValue);
                            vVar.addDataPoint(lat, lon, frameDate, depth, currentVValue);
                        }
                    }
                }
            }
        }

        netCDFGenerator.generate(outputFile, dataset);
    }

    public static void generateGbr1v2(Generator netCDFGenerator, DateTime startDate, DateTime endDate, File outputFile) throws IOException, InvalidRangeException {
        Random rng = new Random(9833);

        float[] lats = Generator.getCoordinates(-28, -7.6f, 30); // y
        float[] lons = Generator.getCoordinates(142, 152, 14); // x

        // List of all depths found in GBR1 2.0 files
        double[] allDepths = {-3885, -3660, -3430, -3195, -2965, -2730, -2495, -2265, -2035, -1805, -1575, -1345, -1115, -960, -860, -750, -655, -570, -495, -430, -370, -315, -270, -230, -195, -165, -140, -120, -103, -88, -73, -60, -49, -39.5, -31, -24, -18, -13, -9, -5.35, -2.35, -0.5, 0.5, 1.5};

        // List of depths used in configs
        double[] usedDepths = {-2.35, -5.35, -18};

        double[] depths = usedDepths;


        NetCDFDataset dataset = new NetCDFDataset();
        dataset.setGlobalAttribute("metadata_link", "http://marlin.csiro.au/geonetwork/srv/eng/search?&uuid=0ce4f380-ac99-46d5-a327-571bd20a0478");
        dataset.setGlobalAttribute("title", "GBR1 Hydro Transport");
        dataset.setGlobalAttribute("paramhead", "GBR 1km resolution grid");

        NetCDFTimeDepthVariable tempVar = new NetCDFTimeDepthVariable("temp", "degrees C");
        tempVar.setAttribute("long_name", "Temperature");
        dataset.addVariable(tempVar);

        NetCDFTimeDepthVariable saltVar = new NetCDFTimeDepthVariable("salt", "PSU");
        saltVar.setAttribute("long_name", "Salinity");
        dataset.addVariable(saltVar);

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

        NetCDFVariable botzVar = new NetCDFVariable("botz", "metre");
        botzVar.setAttribute("long_name", "Depth of sea-bed");
        dataset.addVariable(botzVar);


        int startHour = Hours.hoursBetween(dataset.getTimeEpoch(), startDate).getHours();
        int endHour = Hours.hoursBetween(dataset.getTimeEpoch(), endDate).getHours();
        for (float lat : lats) {
            for (float lon : lons) {
                // Set data for NetCDFVariable
                double botzValue = lat % 10 + lon % 10;
                botzVar.addDataPoint(lat, lon, botzValue);

                for (int hour=startHour; hour<endHour; hour++) {
                    DateTime frameDate = dataset.getTimeEpoch().plusHours(hour);

                    // Set data for NetCDFTimeVariable

                    // Wind
                    double windUValue = Generator.drawLinearGradient(rng, lat, lon - hour, -10, -8, 100, 70, 0);
                    double windVValue = Generator.drawLinearGradient(rng, lat - hour, lon, 2, 17, 50, -20, 0);
                    wspeed_uVar.addDataPoint(lat, lon, frameDate, windUValue);
                    wspeed_vVar.addDataPoint(lat, lon, frameDate, windVValue);

                    for (double depth : depths) {
                        // Set data for NetCDFTimeDepthVariable

                        // Temperature
                        double worldTempValue = Generator.drawLinearGradient(rng, lat+45, lon, 0, 30, 180, 0, (-depth + 2) / 5000); // Hot at the equator, cold at the poles
                        double qldTempValue = Generator.drawLinearGradient(rng, lat, lon+31, -4, 4, 20, 60, (-depth + 2) / 5000); // Hotter closer to the coastline
                        double dayNight = (Math.abs((hour + 12) % 24 - 12) - 6) / 4.0; // Temperature varies +/- 1 degree between day and night
                        tempVar.addDataPoint(lat, lon, frameDate, depth, worldTempValue + qldTempValue + dayNight + depth/10);

                        // Salt
                        double saltValue = Generator.drawRadialGradient(rng, lat+(hour/4.0f), lon-(hour/4.0f), 32, 36, 10, (-depth + 2) / 5000);
                        saltVar.addDataPoint(lat, lon, frameDate, depth, saltValue);

                        // Current
                        double currentUValue = Generator.drawRadialGradient(rng, lat-(hour/4.0f), lon+(hour/4.0f), -0.6, 0.6, 15, (-depth + 2) / 5000);
                        double currentVValue = Generator.drawRadialGradient(rng, lat+(hour/4.0f), lon+(hour/4.0f), -0.6, 0.6, 15, (-depth + 2) / 5000);
                        uVar.addDataPoint(lat, lon, frameDate, depth, currentUValue);
                        vVar.addDataPoint(lat, lon, frameDate, depth, currentVValue);
                    }
                }
            }
        }

        netCDFGenerator.generate(outputFile, dataset);
    }

    public static void generateGbr4v2MultiHypercubes(Generator netCDFGenerator, DateTime startDate, DateTime endDate, File outputFile) throws IOException, InvalidRangeException {
        Random rng = new Random(5610);

        float[] lats0 = Generator.getCoordinates(-26, -7.6f, 15); // y
        float[] lons0 = Generator.getCoordinates(142, 154, 10); // x
        double[] depths0 = {-1.5, -17.75, -49};

        float[] lats1 = Generator.getCoordinates(-28, -9.6f, 30); // y
        float[] lons1 = Generator.getCoordinates(144, 156, 20); // x

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


        int startHour0 = Hours.hoursBetween(dataset0.getTimeEpoch(), startDate).getHours();
        int endHour0 = Hours.hoursBetween(dataset0.getTimeEpoch(), endDate).getHours();
        for (float lat : lats0) {
            for (float lon : lons0) {
                for (int hour=startHour0; hour<endHour0; hour++) {
                    DateTime frameDate = dataset0.getTimeEpoch().plusHours(hour);

                    // Set data for NetCDFTimeVariable

                    // Wind
                    double windUValue = Generator.drawLinearGradient(rng, lat, lon - hour, -10, -8, 100, 70, 0);
                    double windVValue = Generator.drawLinearGradient(rng, lat - hour, lon, 2, 17, 50, -20, 0);
                    wspeed_uVar.addDataPoint(lat, lon, frameDate, windUValue);
                    wspeed_vVar.addDataPoint(lat, lon, frameDate, windVValue);

                    for (double depth : depths0) {
                        // Set data for NetCDFTimeDepthVariable

                        // Temperature
                        double worldTempValue = Generator.drawLinearGradient(rng, lat+45, lon, 0, 30, 180, 0, (-depth + 2) / 5000); // Hot at the equator, cold at the poles
                        double qldTempValue = Generator.drawLinearGradient(rng, lat, lon+31, -4, 4, 20, 60, (-depth + 2) / 5000); // Hotter closer to the coastline
                        double dayNight = (Math.abs((hour + 12) % 24 - 12) - 6) / 4.0; // Temperature varies +/- 1 degree between day and night
                        tempVar.addDataPoint(lat, lon, frameDate, depth, worldTempValue + qldTempValue + dayNight + depth/10);
                    }
                }
            }
        }

        int startHour1 = Hours.hoursBetween(dataset1.getTimeEpoch(), startDate).getHours();
        int endHour1 = Hours.hoursBetween(dataset1.getTimeEpoch(), endDate).getHours();
        for (float lat : lats1) {
            for (float lon : lons1) {
                for (int hour=startHour1+2; hour<endHour1; hour+=3) {
                    DateTime frameDate = dataset1.getTimeEpoch().plusHours(hour);

                    for (double depth : depths1) {
                        // Set data for NetCDFTimeDepthVariable

                        // Salt
                        double saltValue = Generator.drawRadialGradient(rng, lat+(hour/4.0f), lon-(hour/4.0f), 32, 36, 10, (-depth + 2) / 5000);
                        saltVar.addDataPoint(lat, lon, frameDate, depth, saltValue);

                        // Current
                        double currentUValue = Generator.drawRadialGradient(rng, lat-(hour/4.0f), lon+(hour/4.0f), -0.6, 0.6, 15, (-depth + 2) / 5000);
                        double currentVValue = Generator.drawRadialGradient(rng, lat+(hour/4.0f), lon+(hour/4.0f), -0.6, 0.6, 15, (-depth + 2) / 5000);
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

        float[] lats = Generator.getCoordinates(-28, -7.6f, 15); // y
        float[] lons = Generator.getCoordinates(142, 156, 10); // x

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


        int startHour = Hours.hoursBetween(dataset.getTimeEpoch(), startDate).getHours();
        int endHour = Hours.hoursBetween(dataset.getTimeEpoch(), endDate).getHours();
        for (float lat : lats) {
            for (float lon : lons) {
                for (int hour=startHour; hour<endHour; hour+=24) {
                    DateTime frameDate = dataset.getTimeEpoch().plusHours(hour);

                    // Set data for NetCDFTimeVariable

                    // True colour variables
                    // NOTE: The colour turned out to be quite good with those ratios:
                    //     Blue wavelength goes deep:                  values [0, 1]
                    //     Green penetrate about half as deep as blue: values [0, 0.5]
                    //     Red get pretty much all absorb:             values [0, 0.1]
                    double r470Value = Generator.drawLinearGradient(rng, lat, lon+102, 0, 1, 360, 90, 0.05); // Violet (used for Blue)
                    double r555Value = Generator.drawLinearGradient(rng, lat, lon+102, 0, 0.5, 360, 90, 0.05); // Green
                    double r645Value = Generator.drawLinearGradient(rng, lat, lon+102, 0, 0.1, 360, 90, 0.05); // Red
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


    public static void generateNoaa(Generator netCDFGenerator, DateTime startDate, DateTime endDate, File outputWaveDirFile, File outputWaveHeightFile) throws IOException, InvalidRangeException {
        Random rng = new Random(9584);

        float[] lats = Generator.getCoordinates(-90, 0, 45); // y
        float[] lons = Generator.getCoordinates(0, 180, 90); // x

        String timeUnit = "Hour since " + startDate.toString();


        // multi_1.glo_30m.dp.201412.grb2
        NetCDFDataset waveDirDataset = new NetCDFDataset();
        waveDirDataset.setTimeUnit(timeUnit, startDate);

        NetCDFTimeVariable waveDirVar = new NetCDFTimeVariable("Primary_wave_direction_surface", "degree.true");
        waveDirDataset.addVariable(waveDirVar);


        // multi_1.glo_30m.hs.201412.grb2
        NetCDFDataset waveHeightDataset = new NetCDFDataset();
        waveHeightDataset.setTimeUnit(timeUnit, startDate);

        NetCDFTimeVariable waveHeightVar = new NetCDFTimeVariable("Significant_height_of_combined_wind_waves_and_swell_surface", "m");
        waveHeightDataset.addVariable(waveHeightVar);


        int endHour = Hours.hoursBetween(startDate, endDate).getHours();
        int hourOffset = Hours.hoursBetween(new DateTime(1990, 1, 1, 0, 0), startDate).getHours();
        for (float lat : lats) {
            for (float lon : lons) {
                for (int hour=0; hour<endHour; hour+=3) {
                    DateTime frameDate = startDate.plusHours(hour);

                    // Set data for NetCDFTimeVariable

                    // Wave direction: pointing towards Qld coastline (50 deg) with some random variations (+/-20 deg)
                    // In NOAA datasets, 0 deg is pointing South (for some reason...)
                    double waveDirValue = 50 + (rng.nextDouble() * 40 - 5);
                    waveDirVar.addDataPoint(lat, lon, frameDate, waveDirValue);

                    // Wave height varies between [0, 4] and moves up and down like the tides (sort of)
                    double waveHeightValue = Generator.drawLinearGradient(rng, lat, lon+(hourOffset+hour)/3.0f, 0, 4, 60, 70.0, 0.05);
                    waveHeightVar.addDataPoint(lat, lon, frameDate, waveHeightValue);
                }
            }
        }

        netCDFGenerator.generate(outputWaveDirFile, waveDirDataset);
        netCDFGenerator.generate(outputWaveHeightFile, waveHeightDataset);
    }
}
