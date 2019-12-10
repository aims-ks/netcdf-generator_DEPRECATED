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
 * Generate NetCDF file containing a single data hypercube.
 *
 * To simplify the library, some assumptions were made:
 * - Every variable have the following dimensions: time, lat, lon
 * - Values are type Double
 *
 * Old Unidata example:
 *     https://www.unidata.ucar.edu/software/netcdf-java/current/tutorial/NetcdfFileWriteable.html
 *
 * Java DOC:
 *     https://www.unidata.ucar.edu/software/netcdf-java/v4.3/v4.3/javadoc/ucar/nc2/NetcdfFileWriter.html
 */
public class Generator {
    // Switch to "netcdf3" if you are getting error with the generation of NetCDF4 files
    private static final NetcdfFileWriter.Version NETCDF_VERSION = NetcdfFileWriter.Version.netcdf4;

    // NOTE: Null value can be set using attribute "_FillValue", "missing_value", etc,
    //     by adding the following line (for example) in the definition of the variable:
    //         writer.addVariableAttribute(variableName, "_FillValue", 9999);
    //     https://www.unidata.ucar.edu/software/netcdf-java/current/tutorial/NetcdfDataset.html
    //     or simply using Double.NaN (as in eReefs NetCDF files).
    // "_FillValue", "missing_value" have different meanings:
    //     Sometimes there is need for more than one value to represent different kinds of missing data.
    //     In this case, the user should use one or more other variable attributes for the different kinds
    //     of missing data. For example, it might be appropriate to use _FillValue to mean that data that
    //     was expected never appeared, but missing_value where the creator of the data intends data to be
    //     missing, as around an irregular region represented by a rectangular grid.
    //     http://www.bic.mni.mcgill.ca/users/sean/Docs/netcdf/guide.txn_59.html
    private static final Double NULL_VALUE = Double.NaN;

    // The date represented by time = 0, in NetCDF files
    private static final DateTime NETCDF_EPOCH = new DateTime(1990, 1, 1, 0, 0, DateTimeZone.UTC);

    /**
     * Generate a NetCDF file containing a single data hypercube
     * @param dataset
     * @param outputFile
     * @throws IOException
     * @throws InvalidRangeException
     */
    public void generate(NetCDFDataset dataset, File outputFile) throws IOException, InvalidRangeException {
        try (NetcdfFileWriter writer = NetcdfFileWriter.createNew(NETCDF_VERSION, outputFile.getAbsolutePath())) {

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
                String variableName = variable.getName();
                DataType dataType = DataType.DOUBLE;
                List<Dimension> varDimensions = new ArrayList<Dimension>();
                varDimensions.add(timeDimension);
                varDimensions.add(latDimension);
                varDimensions.add(lonDimension);

                writer.addVariable(variableName, dataType, varDimensions);
                writer.addVariableAttribute(variableName, "units", variable.getUnit());
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
                                Double value = variable.getValue(date, latValue, lonValue);
                                variableData.set(0, lat, lon, value == null ? NULL_VALUE : value);
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
}
