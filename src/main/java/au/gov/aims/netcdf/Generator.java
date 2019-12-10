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
 *
 * Source:
 *     https://github.com/Unidata/netcdf-java/tree/master/cdm/core/src/main/java/ucar
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
     * Generate a NetCDF file containing at least one data hypercube
     * @param outputFile Where the NetCDF file will be saved.
     * @param datasets Data to save in the file. Only specify more than one when creating multiple data hypercubes.
     * @throws IOException
     * @throws InvalidRangeException
     */
    public void generate(File outputFile, NetCDFDataset ... datasets) throws IOException, InvalidRangeException {
        // Validate arguments
        if (outputFile == null) {
            throw new IllegalArgumentException("No output file provided");
        }
        if (datasets == null || datasets.length < 1) {
            throw new IllegalArgumentException("No dataset provided");
        }

        // Instanciate the UCAR NetCDF writer (with a try-with-resource to ensure it get closed)
        try (NetcdfFileWriter writer = NetcdfFileWriter.createNew(NETCDF_VERSION, outputFile.getAbsolutePath())) {

            List<Bundle> bundleList = new ArrayList<Bundle>();

            // Initialise the NetCDF header
            // - Declare UCAR Dimensions
            // - Declare UCAR Variables
            int datasetCount = 0;
            for (NetCDFDataset dataset : datasets) {
                Bundle bundle = new Bundle(dataset);
                bundleList.add(bundle);

                // Create a unique name for the dimensions / variables (to prevent clashes between hypercubes)
                bundle.latVariableName = "lat";
                bundle.lonVariableName = "lon";
                bundle.timeVariableName = "time";
                if (datasetCount > 0) {
                    bundle.latVariableName += datasetCount;
                    bundle.lonVariableName += datasetCount;
                    bundle.timeVariableName += datasetCount;
                }

                float[] lats = dataset.getLatitudes();
                float[] lons = dataset.getLongitudes();

                // Declare lat / lon / time dimensions
                bundle.latDimension = writer.addDimension(bundle.latVariableName, lats.length);
                bundle.lonDimension = writer.addDimension(bundle.lonVariableName, lons.length);
                Dimension timeDimension = writer.addUnlimitedDimension(bundle.timeVariableName);

                // Declare dimension variables (seams redundant, but it's required)
                List<Dimension> latDimensions = new ArrayList<Dimension>();
                latDimensions.add(bundle.latDimension);
                bundle.latVariable = writer.addVariable(bundle.latVariableName, DataType.FLOAT, latDimensions);
                writer.addVariableAttribute(bundle.latVariableName, "units", "degrees_north");

                List<Dimension> lonDimensions = new ArrayList<Dimension>();
                lonDimensions.add(bundle.lonDimension);
                bundle.lonVariable = writer.addVariable(bundle.lonVariableName, DataType.FLOAT, lonDimensions);
                writer.addVariableAttribute(bundle.lonVariableName, "units", "degrees_east");

                List<Dimension> timeDimensions = new ArrayList<Dimension>();
                timeDimensions.add(timeDimension);
                writer.addVariable(bundle.timeVariableName, DataType.INT, timeDimensions);
                writer.addVariableAttribute(bundle.timeVariableName, "units", "hours since 1990-01-01");

                // Declare data variables (such as temp, salt, current, etc)
                // NOTE: This is the declaration only. The data will be added later.
                for (NetCDFVariable variable : dataset) {
                    String variableName = variable.getName();
                    DataType dataType = DataType.DOUBLE;
                    List<Dimension> varDimensions = new ArrayList<Dimension>();
                    varDimensions.add(timeDimension);
                    varDimensions.add(bundle.latDimension);
                    varDimensions.add(bundle.lonDimension);

                    writer.addVariable(variableName, dataType, varDimensions);
                    writer.addVariableAttribute(variableName, "units", variable.getUnit());
                }

                datasetCount++;
            }

            // Create the file and switch off "define mode":
            //     It's no longer possible to define dimensions / variables pass this point.
            writer.create();

            for (Bundle bundle : bundleList) {
                float[] lats = bundle.dataset.getLatitudes();
                float[] lons = bundle.dataset.getLongitudes();

                // Write all the lat / lon values that will be used with the data.
                writer.write(bundle.latVariable, Array.factory(DataType.FLOAT, new int [] {lats.length}, lats));
                writer.write(bundle.lonVariable, Array.factory(DataType.FLOAT, new int [] {lons.length}, lons));
                // NOTE: The time dimension data is created on the fly, while adding data records bellow

                // Create a map of all the data for each variable.
                // The data is written to the NetCDF file, one variable at the time.
                Map<String, ArrayDouble.D3> dataMap = new HashMap<String, ArrayDouble.D3>();
                // Create a list dates used across all variables (some variables might have time gap)
                Set<DateTime> allDateTime = new TreeSet<DateTime>();
                for (NetCDFVariable variable : bundle.dataset) {
                    dataMap.put(
                            variable.getName(),
                            new ArrayDouble.D3(1, bundle.latDimension.getLength(), bundle.lonDimension.getLength())
                    );
                    allDateTime.addAll(variable.getDates());
                }

                // Initialise the data array for the time variable
                Array timeData = Array.factory(DataType.INT, new int[] {1});

                int[] origin = new int[] {0, 0, 0};
                int[] timeOrigin = new int[] {0};

                // Create each record
                Index timeIndex = timeData.getIndex();
                int recordIndex = 0;
                for (DateTime date : allDateTime) {
                    // Calculate the number of hours that elapsed since NetCDF epoch and the provided date
                    // (that's how dates are recorded in NetCDF files)
                    int timeOffset = Hours.hoursBetween(NETCDF_EPOCH, date).getHours();
                    // Set the time data for the current record
                    timeData.setInt(timeIndex, timeOffset);

                    // Set the data for each coordinate (lon / lat),
                    //     for each variable (temp, salt, current, etc),
                    //     for the specified record time.
                    for (int lat=0; lat<bundle.latDimension.getLength(); lat++) {
                        float latValue = lats[lat];
                        for (int lon=0; lon<bundle.lonDimension.getLength(); lon++) {
                            float lonValue = lons[lon];
                            for (NetCDFVariable variable : bundle.dataset) {
                                ArrayDouble.D3 variableData = dataMap.get(variable.getName());
                                if (variableData != null) {
                                    Double value = variable.getValue(date, latValue, lonValue);
                                    variableData.set(0, lat, lon, value == null ? NULL_VALUE : value);
                                }
                            }
                        }
                    }

                    // Set the record origin
                    // (the record index?)
                    timeOrigin[0] = recordIndex;
                    origin[0] = recordIndex;

                    // Write the data out for the current record
                    for (Map.Entry<String, ArrayDouble.D3> dataEntry : dataMap.entrySet()) {
                        writer.write(dataEntry.getKey(), origin, dataEntry.getValue());
                    }
                    writer.write(bundle.timeVariableName, timeOrigin, timeData);

                    // Increase the record index count
                    recordIndex++;
                }
            }

            // Flush the writer, to be sure all the data is written in the file, before closing it.
            writer.flush();
        }
    }

    // Simple class to keep generation variables together
    private static class Bundle {
        public NetCDFDataset dataset;

        public String latVariableName;
        public Dimension latDimension;
        public Variable latVariable;

        public String lonVariableName;
        public Dimension lonDimension;
        public Variable lonVariable;

        public String timeVariableName;

        public Bundle(NetCDFDataset dataset) {
            this.dataset = dataset;
        }
    }
}
