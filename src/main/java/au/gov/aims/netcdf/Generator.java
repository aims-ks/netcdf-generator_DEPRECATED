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
import org.joda.time.Hours;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Create a simple NetCDF dataset that contains data that changes depending on its coordinates.
 * The data is very coarse, to product small NetCDF file.
 *
 * Old Unidata example:
 *     https://www.unidata.ucar.edu/software/netcdf-java/current/tutorial/NetcdfFileWriteable.html
 *
 * Java DOC:
 *     https://www.unidata.ucar.edu/software/netcdf-java/v4.3/v4.3/javadoc/ucar/nc2/NetcdfFileWriter.html
 */
public class Generator {
    private static final Logger LOGGER = Logger.getLogger(Generator.class);
    private static final NetcdfFileWriter.Version NETCDF_VERSION = NetcdfFileWriter.Version.netcdf4;

    private static final DateTime NETCDF_EPOCH = new DateTime(1990, 1, 1, 0, 0, DateTimeZone.UTC);
    private static final DateTimeZone TIMEZONE_BRISBANE = DateTimeZone.forID("Australia/Brisbane");

    public static void main(String ... args) throws Exception {
        File outputFile = new File("/tmp/gbrRainbow.nc");
        Generator netCDFGenerator = new Generator();
        netCDFGenerator.generateGbrRaindow(new DateTime(2019, 1, 1, 0, 0, TIMEZONE_BRISBANE), outputFile);
    }

    public void generateGbrRaindow(DateTime startDate, File outputFile) throws IOException, InvalidRangeException {
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

//                    double saltValue = hour + (-10-lat) + (lon-142);
                    double saltValue = hour + (lat+22) + (-lon+154);
                    salt.addDataPoint(startDate.plusHours(hour), lat, lon, saltValue);
                }
            }
        }

        this.generate(dataset, outputFile);
    }

    public void generate(NetCDFDataset dataset, File outputFile) throws IOException, InvalidRangeException {
        try (NetcdfFileWriter writer = NetcdfFileWriter.createNew(
            NETCDF_VERSION,
            outputFile.getAbsolutePath()
        )) {
            float[] lats = dataset.getLatitudes();
            float[] lons = dataset.getLongitudes();

            Dimension latDimension = writer.addDimension("lat", lats.length);
            Dimension lonDimension = writer.addDimension("lon", lons.length);
            Dimension timeDimension = writer.addUnlimitedDimension("time");

            List<Dimension> latDimensions = new ArrayList<Dimension>();
            latDimensions.add(latDimension);
            Variable latVariable = writer.addVariable("lat", DataType.FLOAT, latDimensions);
            writer.addVariableAttribute("lat", "units", "degrees_north");

            List<Dimension> lonDimensions = new ArrayList<Dimension>();
            lonDimensions.add(lonDimension);
            Variable lonVariable = writer.addVariable("lon", DataType.FLOAT, lonDimensions);
            writer.addVariableAttribute("lon", "units", "degrees_east");

            List<Dimension> timeDimensions = new ArrayList<Dimension>();
            timeDimensions.add(timeDimension);
            writer.addVariable("time", DataType.INT, timeDimensions);
            writer.addVariableAttribute("time", "units", "hours since 1990-01-01");

            for (NetCDFVariable variable : dataset) {
                String shortName = variable.getName();
                DataType dataType = DataType.DOUBLE;
                List<Dimension> varDimensions = new ArrayList<Dimension>();
                varDimensions.add(timeDimension);
                varDimensions.add(latDimension);
                varDimensions.add(lonDimension);

                writer.addVariable(shortName, dataType, varDimensions);
                writer.addVariableAttribute(shortName, "units", variable.getUnit());
            }

            writer.create();

            writer.write(latVariable, Array.factory(DataType.FLOAT, new int [] {lats.length}, lats));
            writer.write(lonVariable, Array.factory(DataType.FLOAT, new int [] {lons.length}, lons));
            // Do not write time dimension. It will get added "frame" by "frame"

            Map<String, ArrayDouble.D3> dataMap = new HashMap<String, ArrayDouble.D3>();
            Set<DateTime> allDateTime = new TreeSet<DateTime>();
            for (NetCDFVariable variable : dataset) {
                dataMap.put(
                        variable.getName(),
                        new ArrayDouble.D3(1, latDimension.getLength(), lonDimension.getLength())
                );
                allDateTime.addAll(variable.getDates());
            }

            Array timeData = Array.factory(DataType.INT, new int[] {1});

            int[] origin = new int[] {0, 0, 0};
            int[] timeOrigin = new int[] {0};

            // loop over each record
            Index timeIndex = timeData.getIndex();
            int dateCount = 0;
            for (DateTime date : allDateTime) {
                int timeOffset = Hours.hoursBetween(NETCDF_EPOCH, date).getHours();
                timeData.setInt(timeIndex, timeOffset);

                for (int lat=0; lat<latDimension.getLength(); lat++) {
                    float latValue = lats[lat];
                    for (int lon=0; lon<lonDimension.getLength(); lon++) {
                        float lonValue = lons[lon];
                        for (NetCDFVariable variable : dataset) {
                            ArrayDouble.D3 variableData = dataMap.get(variable.getName());
                            if (variableData != null) {
                                Double value = variable.getData(date, latValue, lonValue);
                                if (value != null) {
                                    variableData.set(0, lat, lon, value);
                                }
                            }
                        }
                    }
                }

                // write the data out for one record
                // set the origin here
                timeOrigin[0] = dateCount;
                origin[0] = dateCount;

                for (Map.Entry<String, ArrayDouble.D3> dataEntry : dataMap.entrySet()) {
                    writer.write(dataEntry.getKey(), origin, dataEntry.getValue());
                }
                writer.write("time", timeOrigin, timeData);

                dateCount++;
            }

            writer.flush();
        }
    }

    /*
     * TODO
     * 1. Create simple data file covering GBR
     * 2. Create file with time gaps
     * 3. Create file with multiple hypercubes of data
     */
}
