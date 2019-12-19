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
import au.gov.aims.netcdf.bean.NetCDFTimeVariable;
import au.gov.aims.netcdf.bean.NetCDFVariable;
import au.gov.aims.netcdf.bean.NetCDFVectorVariable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Hours;
import org.junit.Assert;
import org.junit.Test;
import ucar.ma2.InvalidRangeException;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class GeneratorTest {
    private static final DateTimeZone TIMEZONE_BRISBANE = DateTimeZone.forID("Australia/Brisbane");

    @Test
    public void testGenerator() throws IOException, InvalidRangeException {
        Generator netCDFGenerator = new Generator();
        File outputFile = new File("/tmp/test.nc");
        long expectedFileSize = 10 * 1024 * 1024; // 10 MB

        // Test file
        GeneratorTest.generateTest(netCDFGenerator,
                new DateTime(2019, 1, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2019, 1, 3, 0, 0, TIMEZONE_BRISBANE),
                outputFile);

        Assert.assertTrue(String.format("The generated file doesn't exists or can not be read: %s", outputFile),
                outputFile.canRead());

        Assert.assertTrue(String.format("The generated file is smaller than expected.%n" +
            "Expected: %d%n" +
            "Actual: %d%n" +
            "File: %s",
            expectedFileSize, outputFile.length(), outputFile),
            outputFile.length() > expectedFileSize);

        System.out.println(String.format("Load the generated file (%s) in a NetCDF viewer such as Panoply (%s) to check its integrity.",
                outputFile,
                "https://www.giss.nasa.gov/tools/panoply/"));
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

        float[] lats = Generator.getCoordinates(-50, 50, 100);
        float[] lons = Generator.getCoordinates(-50, 50, 100);

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

                    double testLinearGradientValue = Generator.drawLinearGradient(rng, lat, lon, 0, 10, 50, hour * (360.0/nbHours), 0);
                    testLinearGradient.addDataPoint(lat, lon, frameDate, testLinearGradientValue);

                    double testRadialGradientValue = Generator.drawRadialGradient(rng, lat, lon, -10, 2, 50, Math.abs(Math.abs(hour-nbHours/2.0)-nbHours/2.0) * 0.01);
                    testRadialGradient.addDataPoint(lat, lon, frameDate, testRadialGradientValue);

                    double testWaveUValue = Generator.drawLinearGradient(rng, lat, lon - hour, -4, 0, 100, 70, 0);;
                    double testWaveVValue = Generator.drawLinearGradient(rng, lat - hour, lon, 2, 10, 50, -20, 0);
                    testWaveU.addDataPoint(lat, lon, frameDate, testWaveUValue);
                    testWaveV.addDataPoint(lat, lon, frameDate, testWaveVValue);
                }
            }
        }

        netCDFGenerator.generate(outputFile, dataset);
    }
}
