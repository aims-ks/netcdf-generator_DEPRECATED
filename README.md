This project can be used to generate very small NetCDF files, which can be included in projects to run unit tests.

## Tools

NetCDF files can be viewed using different tools. The most convenient ones are:
- [Panoply](https://www.giss.nasa.gov/tools/panoply/)
- [Dive](http://software.cmar.csiro.au/www/en/software/dive.html)

**NOTE**: As of writing this documentation, CMAR have ceased to support the download link for **Dive**. 
If you really want to use this tool, you can still find it on the
[Web Archive](https://web.archive.org/web/20170314023923/http://software.cmar.csiro.au/www/en/software/dive.html).

## NetCDF variable naming conventions

If you want to create your own NetCDF file, you may be interested in the following resources.

- [NetCDF naming convention for `long-name` and `standard-name`](https://cfconventions.org/Data/cf-conventions/cf-conventions-1.7/build/ch02s03.html)
- [Table of common NetCDF `standard-name`](https://cfconventions.org/Data/cf-standard-names/77/build/cf-standard-name-table.html)

Some metadata information is hidden in unrelated attributes. Just have a look at the EDAL library source code
to see how `standard_name` and `long_name` can be used to infer metadata on the component variables of a vector variable.

Package: uk.ac.rdg.resc.edal.dataset.cdm  
Class: CdmDatasetFactory  
Source code:
```
private IdComponentEastNorth determineVectorIdAndComponent(String stdName) {
    if (stdName.contains("eastward_")) {
        return new IdComponentEastNorth(stdName.replaceFirst("eastward_", ""), true, true);
    } else if (stdName.contains("northward_")) {
        return new IdComponentEastNorth(stdName.replaceFirst("northward_", ""), false, true);
    } else if (stdName.matches("u-.*component")) {
        return new IdComponentEastNorth(stdName.replaceFirst("u-(.*)component", "$1"), true,
                false);
    } else if (stdName.matches("v-.*component")) {
        return new IdComponentEastNorth(stdName.replaceFirst("v-(.*)component", "$1"), false,
                false);
    } else if (stdName.matches(".*x_.*velocity")) {
        return new IdComponentEastNorth(stdName.replaceFirst("(.*)x_(.*velocity)", "$1$2"),
                true, false);
    } else if (stdName.matches(".*y_.*velocity")) {
        return new IdComponentEastNorth(stdName.replaceFirst("(.*)y_(.*velocity)", "$1$2"),
                false, false);
    }
    return null;
}
```