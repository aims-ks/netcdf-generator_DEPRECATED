/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.netcdf.bean;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NetCDFDataset implements Iterable<AbstractNetCDFVariable> {
    private List<AbstractNetCDFVariable> variables;
    private List<NetCDFVectorVariable> vectorVariables;
    private Map<String, String> globalAttributes;

    // The date represented by time = 0, in NetCDF file
    private String timeUnit;
    private DateTime timeEpoch;

    public NetCDFDataset() {
        this.variables = new ArrayList<AbstractNetCDFVariable>();
        this.vectorVariables = new ArrayList<NetCDFVectorVariable>();
        this.globalAttributes = new HashMap<String, String>();
        this.timeUnit = "hours since 1990-01-01";
        this.timeEpoch = new DateTime(1990, 1, 1, 0, 0, DateTimeZone.UTC);
    }

    public String getTimeUnit() {
        return this.timeUnit;
    }

    public DateTime getTimeEpoch() {
        return this.timeEpoch;
    }

    public void setTimeUnit(String timeUnit, DateTime timeEpoch) {
        this.timeUnit = timeUnit;
        this.timeEpoch = timeEpoch;
    }

    public Map<String, String> getGlobalAttributes() {
        return this.globalAttributes;
    }

    public void setGlobalAttribute(String key, String value) {
        this.globalAttributes.put(key, value);
    }

    // Return a Dimensions instance containing all used lat, lon and heights.
    // It needs to go through all data point coordinate, which takes times,
    // but it's a small price to pay for the stability benefits.
    public Dimensions getDimensions() {
        Set<Float> latitudes = new HashSet<Float>();
        Set<Float> longitudes = new HashSet<Float>();
        Set<Double> heights = new HashSet<Double>();

        for (AbstractNetCDFVariable variable : this) {
            Map<NetCDFPointCoordinate, Double> variableData = variable.getData();
            if (variableData != null && !variableData.isEmpty()) {
                for (NetCDFPointCoordinate coordinate : variableData.keySet()) {
                    if (coordinate != null) {
                        latitudes.add(coordinate.getLat());
                        longitudes.add(coordinate.getLon());

                        Double height = coordinate.getHeight();
                        if (height != null) {
                            heights.add(height);
                        }
                    }
                }
            }
        }

        return new Dimensions(latitudes, longitudes, heights);
    }

    public List<AbstractNetCDFVariable> getVariables() {
        return this.variables;
    }

    public void setVariables(List<AbstractNetCDFVariable> variables) {
        if (variables == null) {
            this.variables.clear();
        } else {
            this.variables = variables;
        }
    }

    public void addVariable(AbstractNetCDFVariable variable) {
        this.variables.add(variable);
    }


    public List<NetCDFVectorVariable> getVectorVariables() {
        return this.vectorVariables;
    }

    public void setVectorVariables(List<NetCDFVectorVariable> vectorVariables) {
        if (vectorVariables == null) {
            this.vectorVariables.clear();
        } else {
            this.vectorVariables = vectorVariables;
        }
    }

    public void addVectorVariable(NetCDFVectorVariable vectorVariable) {
        this.vectorVariables.add(vectorVariable);
    }


    @Override
    public Iterator<AbstractNetCDFVariable> iterator() {
        List<AbstractNetCDFVariable> allVariables = new ArrayList<AbstractNetCDFVariable>(this.variables);
        for (NetCDFVectorVariable vectorVariable : this.vectorVariables) {
            allVariables.add(vectorVariable.getU());
            allVariables.add(vectorVariable.getV());
        }

        return allVariables.iterator();
    }

    public static class Dimensions {
        private float[] latitudes;
        private float[] longitudes;
        private double[] heights;

        public Dimensions(Set<Float> latitudes, Set<Float> longitudes, Set<Double> heights) {
            this(floatSetToArray(latitudes), floatSetToArray(longitudes), doubleSetToArray(heights));
        }

        public Dimensions(float[] latitudes, float[] longitudes, double[] heights) {
            this.latitudes = latitudes;
            this.longitudes = longitudes;
            this.heights = heights;

            if (this.latitudes != null) {
                Arrays.sort(this.latitudes);
            }
            if (this.longitudes != null) {
                Arrays.sort(this.longitudes);
            }
            if (this.heights != null) {
                Arrays.sort(this.heights);
            }
        }

        public float[] getLatitudes() {
            return this.latitudes;
        }

        public float[] getLongitudes() {
            return this.longitudes;
        }

        public double[] getHeights() {
            return this.heights;
        }


        private static float[] floatSetToArray(Set<Float> floats) {
            float[] floatArray = null;
            if (floats != null && !floats.isEmpty()) {
                floatArray = new float[floats.size()];
                int index = 0;
                for (Float floatValue : floats) {
                    if (floatValue != null) {
                        floatArray[index] = floatValue;
                        index++;
                    }
                }
            }
            return floatArray;
        }
        private static double[] doubleSetToArray(Set<Double> doubles) {
            double[] doubleArray = null;
            if (doubles != null && !doubles.isEmpty()) {
                doubleArray = new double[doubles.size()];
                int index = 0;
                for (Double doubleValue : doubles) {
                    if (doubleValue != null) {
                        doubleArray[index] = doubleValue;
                        index++;
                    }
                }
            }
            return doubleArray;
        }
    }
}
