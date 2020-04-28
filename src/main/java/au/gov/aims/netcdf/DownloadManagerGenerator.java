/*
 *  Copyright (C) 2020 Australian Institute of Marine Science
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
