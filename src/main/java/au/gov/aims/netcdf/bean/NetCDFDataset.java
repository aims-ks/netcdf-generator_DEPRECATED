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
package au.gov.aims.netcdf.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NetCDFDataset implements Iterable<AbstractNetCDFVariable> {
    private List<AbstractNetCDFVariable> variables;
    private List<NetCDFVectorVariable> vectorVariables;

    public NetCDFDataset() {
        this.variables = new ArrayList<AbstractNetCDFVariable>();
        this.vectorVariables = new ArrayList<NetCDFVectorVariable>();
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
