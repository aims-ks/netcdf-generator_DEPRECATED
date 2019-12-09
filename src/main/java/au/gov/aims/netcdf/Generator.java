package au.gov.aims.netcdf;

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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
public class Generator implements Closeable {
    private static final NetcdfFileWriter.Version NETCDF_VERSION = NetcdfFileWriter.Version.netcdf4;

    private static final DateTime NETCDF_EPOCH = new DateTime(1990, 1, 1, 0, 0, DateTimeZone.UTC);
    private static final DateTimeZone TIMEZONE_BRISBANE = DateTimeZone.forID("Australia/Brisbane");

    public NetcdfFileWriter writer;

    public static void main(String ... args) throws Exception {
        File outputFile = new File("/tmp/gbrRainbow.nc");
        try (Generator netCDFGenerator = new Generator(outputFile)) {
            netCDFGenerator.generateGbrRaindow(new DateTime(2019, 1, 1, 0, 0, TIMEZONE_BRISBANE));
        }
    }

    public Generator(File outputFile) throws IOException {
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

    public void generateGbrRaindow(DateTime startDate) throws IOException, InvalidRangeException {
        int timeOffset = 0;
        if (startDate != null) {
            timeOffset = Hours.hoursBetween(NETCDF_EPOCH, startDate).getHours();
        }

        Dimension latDimension = this.writer.addDimension("lat", 11);
        Dimension lonDimension = this.writer.addDimension("lon", 11);
        Dimension timeDimension = this.writer.addUnlimitedDimension("time");

        List<Dimension> latDimensions = new ArrayList<Dimension>();
        latDimensions.add(latDimension);
        Variable latVariable = this.writer.addVariable("lat", DataType.FLOAT, latDimensions);
        this.writer.addVariableAttribute("lat", "units", "degrees_north");

        List<Dimension> lonDimensions = new ArrayList<Dimension>();
        lonDimensions.add(lonDimension);
        Variable lonVariable = this.writer.addVariable("lon", DataType.FLOAT, lonDimensions);
        this.writer.addVariableAttribute("lon", "units", "degrees_east");

        List<Dimension> timeDimensions = new ArrayList<Dimension>();
        timeDimensions.add(timeDimension);
        this.writer.addVariable("time", DataType.INT, timeDimensions);
        this.writer.addVariableAttribute("time", "units", "hours since 1990-01-01");


        String tempShortName = "temperature";
        DataType tempDataType = DataType.DOUBLE;
        List<Dimension> tempDimensions = new ArrayList<Dimension>();
        tempDimensions.add(timeDimension);
        tempDimensions.add(latDimension);
        tempDimensions.add(lonDimension);

        Variable tempVariable = this.writer.addVariable(tempShortName, tempDataType, tempDimensions);
        this.writer.addVariableAttribute(tempShortName, "units", "C");

        this.writer.create();

        this.writer.write(latVariable, Array.factory(DataType.FLOAT, new int [] {11}, new float[] {-22, -20.8f, -19.6f, -18.4f, -17.2f, -16, -14.8f, -13.6f, -12.4f, -11.2f, -10}));
        this.writer.write(lonVariable, Array.factory(DataType.FLOAT, new int [] {11}, new float[] {142, 143.2f, 144.4f, 145.6f, 146.8f, 148, 149.2f, 150.4f, 151.6f, 152.8f, 154}));
        // Do not write time dimension. It will get added "frame" by "frame"

        ArrayDouble.D3 tempData = new ArrayDouble.D3(1, latDimension.getLength(), lonDimension.getLength());
        Array timeData = Array.factory(DataType.INT, new int[] {1});
        Index ima = tempData.getIndex();

        int[] origin = new int[] {0, 0, 0};
        int[] timeOrigin = new int[] {0};

        // loop over each record
        for (int time=0; time<20; time++) {
            // make up some data for this record, using different ways to fill the data arrays.
            timeData.setInt(timeData.getIndex(), time + timeOffset); // 1 hour

            for (int lat=0; lat<latDimension.getLength(); lat++) {
                for (int lon=0; lon<lonDimension.getLength(); lon++) {
                    tempData.set(0, lat, lon, Math.abs((time + lat + lon) % 40 - 20) - 10);
                }
            }

            // write the data out for one record
            // set the origin here
            timeOrigin[0] = time;
            origin[0] = time;

            this.writer.write(tempShortName, origin, tempData);
            this.writer.write("time", timeOrigin, timeData);
        }
    }

    /*
     * TODO
     * 1. Create simple data file covering GBR
     * 2. Create file with time gaps
     * 3. Create file with multiple hypercubes of data
     */
}
