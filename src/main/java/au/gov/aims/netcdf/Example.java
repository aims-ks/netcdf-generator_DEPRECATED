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

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


// NOTE: ucar.nc2.Structure only works with NetCDF 3


/**
 * Example of NetCDF file creation, inspired from an Unidata example:
 *     https://www.unidata.ucar.edu/software/netcdf-java/current/tutorial/NetcdfWriting.html
 * Old example:
 *     https://www.unidata.ucar.edu/software/netcdf-java/current/tutorial/NetcdfFileWriteable.html
 *
 * Java DOC:
 *     https://www.unidata.ucar.edu/software/netcdf-java/v4.3/v4.3/javadoc/ucar/nc2/NetcdfFileWriter.html
 */
public class Example implements Closeable {
    private static final NetcdfFileWriter.Version NETCDF_VERSION = NetcdfFileWriter.Version.netcdf4;

    public NetcdfFileWriter writer;

    public static void main(String ... args) throws Exception {
        File outputFile = new File("/tmp/example.nc");
        try (Example netCDFGenerator = new Example(outputFile)) {
            //netCDFGenerator.generateGbrRaindow();
            netCDFGenerator.generateWind();
        }
    }

    public Example(File outputFile) throws IOException {
        this.writer = NetcdfFileWriter.createNew(
            NETCDF_VERSION,
            outputFile.getAbsolutePath()
        );
    }

    public void close() throws IOException {
        if (this.writer != null) {
            IOException firstException = null;

            try {
                this.writer.flush();
            } catch (IOException ex) {
                firstException = ex;
            }

            try {
                this.writer.close();
            } catch (IOException ex) {
                if (firstException == null) {
                    firstException = ex;
                }
            }

            this.writer = null;

            if (firstException != null) {
                throw firstException;
            }
        }
    }

    public void generateGbrRaindow() throws IOException, InvalidRangeException {
        Dimension latDimension = this.writer.addDimension("lat", 3);
        Dimension lonDimension = this.writer.addDimension("lon", 4);
        Dimension timeDimension = this.writer.addUnlimitedDimension("time");

        List<Dimension> latDimensions = new ArrayList<Dimension>();
        latDimensions.add(latDimension);

        Variable latVariable = this.writer.addVariable("lat", DataType.FLOAT, latDimensions);
        this.writer.addVariableAttribute("lat", "units", "degrees_north");
        this.writer.addVariableAttribute("lat", "_CoordinateAxisType", "Lat");

        List<Dimension> lonDimensions = new ArrayList<Dimension>();
        lonDimensions.add(lonDimension);
        Variable lonVariable = this.writer.addVariable("lon", DataType.FLOAT, lonDimensions);
        this.writer.addVariableAttribute("lon", "units", "degrees_east");
        this.writer.addVariableAttribute("lon", "_CoordinateAxisType", "Lon");

        List<Dimension> timeDimensions = new ArrayList<Dimension>();
        timeDimensions.add(timeDimension);
        this.writer.addVariable("time", DataType.INT, timeDimensions);
        this.writer.addVariableAttribute("time", "units", "hours since 1990-01-01");
        this.writer.addVariableAttribute("time", "_CoordinateAxisType", "Time");


        String tempShortName = "temperature";
        DataType tempDataType = DataType.DOUBLE;
        List<Dimension> tempDimensions = new ArrayList<Dimension>();
        tempDimensions.add(timeDimension);
        tempDimensions.add(latDimension);
        tempDimensions.add(lonDimension);

        Variable tempVariable = this.writer.addVariable(tempShortName, tempDataType, tempDimensions);
        this.writer.addVariableAttribute(tempShortName, "units", "C");

        this.writer.create();

        this.writer.write(latVariable, Array.factory(DataType.FLOAT, new int [] {3}, new float[] {41, 40, 39}));
        this.writer.write(lonVariable, Array.factory(DataType.FLOAT, new int [] {4}, new float[] {-109, -107, -105, -103}));
        // Do not write time dimension. It will get added "frame" by "frame"

        ArrayDouble.D3 tempData = new ArrayDouble.D3(1, latDimension.getLength(), lonDimension.getLength());
        Array timeData = Array.factory(DataType.INT, new int[] {1});

        // loop over each record
        for (int time=0; time<10; time++) {
            // make up some data for this record, using different ways to fill the data arrays.
            timeData.setInt(timeData.getIndex(), time * 12); // 12 hours

            for (int lat=0; lat<latDimension.getLength(); lat++) {
                for (int lon=0; lon<lonDimension.getLength(); lon++) {
                    tempData.set(0, lat, lon, time * lat * lon / 3.14159);
                }
            }

            this.writer.write(tempShortName, new int[] {time, 0, 0}, tempData);
            this.writer.write("time", new int[] {time}, timeData);
        }
    }

    /**
     * Test vector variables
     *
     * NOTE: I have a WHOLE day, and some extra hours after work to figure out this one.
     *     To create a vector variable, you do not need to bundle them in a Group or a Structure,
     *     that breaks everything. Those hierarchies are not well supported.
     *     It all works with some "magic" regexes on some attributes:
     *     - standard_name
     *     - long_name
     *     If it matches one of the regexes, it's considered a U or a V vector.
     *
     *    Package: uk.ac.rdg.resc.edal.dataset.cdm
     *    Class: CdmDatasetFactory
     *    Source code:
     *        private IdComponentEastNorth determineVectorIdAndComponent(String stdName) {
     *            if (stdName.contains("eastward_")) {
     *                return new IdComponentEastNorth(stdName.replaceFirst("eastward_", ""), true, true);
     *            } else if (stdName.contains("northward_")) {
     *                return new IdComponentEastNorth(stdName.replaceFirst("northward_", ""), false, true);
     *            } else if (stdName.matches("u-.*component")) {
     *                return new IdComponentEastNorth(stdName.replaceFirst("u-(.*)component", "$1"), true,
     *                        false);
     *            } else if (stdName.matches("v-.*component")) {
     *                return new IdComponentEastNorth(stdName.replaceFirst("v-(.*)component", "$1"), false,
     *                        false);
     *            } else if (stdName.matches(".*x_.*velocity")) {
     *                return new IdComponentEastNorth(stdName.replaceFirst("(.*)x_(.*velocity)", "$1$2"),
     *                        true, false);
     *            } else if (stdName.matches(".*y_.*velocity")) {
     *                return new IdComponentEastNorth(stdName.replaceFirst("(.*)y_(.*velocity)", "$1$2"),
     *                        false, false);
     *            }
     *            return null;
     *        }
     *
     * @throws IOException
     * @throws InvalidRangeException
     */
    public void generateWind() throws IOException, InvalidRangeException {
        float[] lats = new float[] {-22, -20.8f, -19.6f, -18.4f, -17.2f, -16, -14.8f, -13.6f, -12.4f, -11.2f, -10};
        float[] lons = {142, 143.2f, 144.4f, 145.6f, 146.8f, 148, 149.2f, 150.4f, 151.6f, 152.8f, 154};

        Dimension latDimension = this.writer.addDimension("lat", lats.length);
        Dimension lonDimension = this.writer.addDimension("lon", lons.length);
        Dimension timeDimension = this.writer.addUnlimitedDimension("time");

        List<Dimension> latDimensions = new ArrayList<Dimension>();
        latDimensions.add(latDimension);
        Variable latVariable = this.writer.addVariable("lat", DataType.FLOAT, latDimensions);
        this.writer.addVariableAttribute("lat", "units", "degrees_north");
        this.writer.addVariableAttribute("lat", "_CoordinateAxisType", "Lat");

        List<Dimension> lonDimensions = new ArrayList<Dimension>();
        lonDimensions.add(lonDimension);
        Variable lonVariable = this.writer.addVariable("lon", DataType.FLOAT, lonDimensions);
        this.writer.addVariableAttribute("lon", "units", "degrees_east");
        this.writer.addVariableAttribute("lon", "_CoordinateAxisType", "Lon");

        List<Dimension> timeDimensions = new ArrayList<Dimension>();
        timeDimensions.add(timeDimension);
        this.writer.addVariable("time", DataType.INT, timeDimensions);
        this.writer.addVariableAttribute("time", "units", "hours since 1990-01-01");
        this.writer.addVariableAttribute("time", "_CoordinateAxisType", "Time");


        String windUShortName = "wspeed_u";
        DataType windUDataType = DataType.DOUBLE;
        List<Dimension> windUDimensions = new ArrayList<Dimension>();
        windUDimensions.add(timeDimension);
        windUDimensions.add(latDimension);
        windUDimensions.add(lonDimension);

        Variable windUVariable = this.writer.addVariable(windUShortName, windUDataType, windUDimensions);
        this.writer.addVariableAttribute(windUShortName, "units", "ms-1");
        this.writer.addVariableAttribute(windUShortName, "standard_name", "eastward_wind");


        String windVShortName = "wspeed_v";
        DataType windVDataType = DataType.DOUBLE;
        List<Dimension> windVDimensions = new ArrayList<Dimension>();
        windVDimensions.add(timeDimension);
        windVDimensions.add(latDimension);
        windVDimensions.add(lonDimension);

        Variable windVVariable = this.writer.addVariable(windVShortName, windVDataType, windVDimensions);
        this.writer.addVariableAttribute(windVShortName, "units", "ms-1");
        this.writer.addVariableAttribute(windVShortName, "standard_name", "northward_wind");


        this.writer.create();


        this.writer.write(latVariable, Array.factory(DataType.FLOAT, new int [] {lats.length}, lats));
        this.writer.write(lonVariable, Array.factory(DataType.FLOAT, new int [] {lons.length}, lons));
        // Do not write time dimension. It will get added "frame" by "frame"

        ArrayDouble.D3 windUData = new ArrayDouble.D3(1, latDimension.getLength(), lonDimension.getLength());
        ArrayDouble.D3 windVData = new ArrayDouble.D3(1, latDimension.getLength(), lonDimension.getLength());

        Array timeData = Array.factory(DataType.INT, new int[] {1});

        // loop over each record
        for (int time=0; time<10; time++) {
            // make up some data for this record, using different ways to fill the data arrays.
            timeData.setInt(timeData.getIndex(), time);

            for (int lat=0; lat<latDimension.getLength(); lat++) {
                for (int lon=0; lon<lonDimension.getLength(); lon++) {
                    windUData.set(0, lat, lon, time * lat * lon / 3.14159);
                    windVData.set(0, lat, lon, time * lat * lon / 3.14159);
                }
            }

            this.writer.write(windUShortName, new int[] {time, 0, 0}, windUData);
            this.writer.write(windVShortName, new int[] {time, 0, 0}, windVData);


            this.writer.write("time", new int[] {time}, timeData);
        }
    }
}
