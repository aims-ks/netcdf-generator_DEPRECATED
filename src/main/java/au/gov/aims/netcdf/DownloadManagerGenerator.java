/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.netcdf;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;

/*
 * Class used to generate small NetCDF files used with DownloadManager tests
 */

public class DownloadManagerGenerator {
    private static final DateTimeZone TIMEZONE_BRISBANE = DateTimeZone.forID("Australia/Brisbane");

    public static void main(String ... args) throws Exception {
        Generator netCDFGenerator = new Generator();

        // For the DownloadManager
        NcAnimateGenerator.generateGbr4v2(netCDFGenerator,
                new DateTime(2018, 10, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2018, 10, 2, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_simple_2018-10.nc"), false);

        NcAnimateGenerator.generateGbr4v2(netCDFGenerator,
                new DateTime(2018, 11, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2018, 11, 2, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_simple_2018-11.nc"), false);

        NcAnimateGenerator.generateGbr4v2(netCDFGenerator,
                new DateTime(2018, 12, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2018, 12, 2, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_simple_2018-12.nc"), false);

        NcAnimateGenerator.generateGbr4v2(netCDFGenerator,
                new DateTime(2018, 12, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2018, 12, 2, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_simple_2018-12_modified.nc"), false, 1000);

        NcAnimateGenerator.generateGbr4v2(netCDFGenerator,
                new DateTime(2019, 1, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2019, 1, 2, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_simple_2019-01.nc"), false);

        NcAnimateGenerator.generateGbr4v2(netCDFGenerator,
                new DateTime(2019, 2, 1, 0, 0, TIMEZONE_BRISBANE),
                new DateTime(2019, 2, 2, 0, 0, TIMEZONE_BRISBANE),
                new File("/tmp/gbr4_simple_2019-02.nc"), false);
    }
}
