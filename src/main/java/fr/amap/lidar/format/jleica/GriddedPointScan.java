/*
 * Copyright (C) 2016 UMR AMAP (botAnique et Modélisation de l'Architecture des Plantes et des végétations.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.amap.lidar.format.jleica;

import fr.amap.commons.util.Statistic;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * <p>This abstract class can be used by subclasses who need to handle gridded point scan files.</p>
 * <p>The subclass should implements iterator and file opening.</p>
 * @author Julien Heurtebize
 */
public abstract class GriddedPointScan implements Iterable<LPoint>{
    
    protected PointScanHeader header;
    
    /**
     * First row index to read from the file.
     */
    protected int startRowIndex;

    /**
     * Last row index to read from the file.
     */
    protected int endRowIndex;
    
    /**
     * First column index to read from the file.
     */
    protected int startColumnIndex;

    /**
     * Last column index to read from the file.
     */
    protected int endColumnIndex;

    /**
     * Minimum azimutal angle in radians, azimuth of first non empty column.
     */
    protected double azim_min = Double.NaN;

    /**
     * Maximum azimutal angle in radians, azimuth of last non empty column.
     */
    protected double azim_max = Double.NaN;

    /**
     * Azimutal step angle in radians, offset angle between two columns.
     */
    protected double azimutalStepAngle = Double.NaN;
    
    /**
     * Zenithal step angle in radians, offset angle between two rows.
     */
    protected double elevationStepAngle = Double.NaN;
    
    /**
     * Minimum zenithal angle in radians, zenith of first non empty row.
     */
    protected double elev_min = Double.NaN;

    /**
     * Maximum zenithal angle in radians, zenith of last non empty row.
     */
    protected double elev_max = Double.NaN;
    
    //handle empty columns
    protected int colIndexAzimMin = -1;
    protected int colIndexAzimMax = -1;
    
     //handle empty rows
    protected int rowIndexElevMin = -1;
    protected int rowIndexElevMax = -1;
    
    /**
     * Is the iterator should return invalid points (missed returns of lidar shots) ?
     */
    protected boolean returnInvalidPoint;
    
    /**
     * Bonded gridded point scan file.
     */
    protected File file;
    
    protected int currentColIndex;
    
    public GriddedPointScan(){
        header = new PointScanHeader();
        returnInvalidPoint = false;
        currentColIndex = 0;
    }
    
    public abstract void openScanFile(File file) throws FileNotFoundException, IOException, Exception;
    
    @Override
    public abstract Iterator<LPoint> iterator();
    
    /**
     * Compute minimum and maximum azimutal and elevation angles of the scan.
     */
    public void computeExtremumsAngles(){
        
        Statistic minAzimutalAngle = new Statistic();
        Statistic maxAzimutalAngle = new Statistic();
        
        resetRowLimits();
        resetColumnLimits();
        
        //compute min azimutal angle
        int i;
        
        for(i=0;i<header.getNumCols();i++){
            
            setUpColumnToRead(i);
            
            Iterator<LPoint> iterator = this.iterator();

            while(iterator.hasNext()){

                LPoint point = iterator.next();

                SphericalCoordinates sc;

                if(header.isPointInFloatFormat()){

                    LFloatPoint floatPoint = (LFloatPoint)point;
                    sc= new SphericalCoordinates(new Vector3D(floatPoint.x, floatPoint.y, floatPoint.z).normalize());
                }else{
                    LDoublePoint doublePoint = (LDoublePoint)point;
                    sc= new SphericalCoordinates(new Vector3D(doublePoint.x, doublePoint.y, doublePoint.z).normalize());
                }

                minAzimutalAngle.addValue(sc.getTheta());
            }
            
            if(minAzimutalAngle.getNbValues() > 0){
                break;
            }
        }
        
        azim_min = minAzimutalAngle.getMean();
        colIndexAzimMin = i;
        
        //compute max azimutal angle
//        for (i = colIndexAzimMin+100; i < header.getNumCols(); i+=100) {
//            
//            setUpColumnToRead(i);
//            
//            Iterator<LPoint> iterator = this.iterator();
//
//            while(iterator.hasNext()){
//
//                LPoint point = iterator.next();
//
//                SphericalCoordinates sc = new SphericalCoordinates();
//
//                if(header.isPointInFloatFormat()){
//
//                    LFloatPoint floatPoint = (LFloatPoint)point;
//                    sc.toSpherical(new Point3d(floatPoint.x, floatPoint.y, floatPoint.z));
//                }else{
//                    LDoublePoint doublePoint = (LDoublePoint)point;
//                    sc.toSpherical(new Point3d(doublePoint.x, doublePoint.y, doublePoint.z));
//                }
//
//                maxAzimutalAngle.addValue(sc.getAzimuth());
//            }
//            
//            if(maxAzimutalAngle.getNbValues() > 0){
//                break;
//            }
//        }
//        
//        azim_max = maxAzimutalAngle.getMean();
//        colIndexAzimMax = i;
        for (i = header.getNumCols()-1; i >= 0; i--) {
            
            setUpColumnToRead(i);
            
            Iterator<LPoint> iterator = this.iterator();

            while(iterator.hasNext()){

                LPoint point = iterator.next();

                SphericalCoordinates sc;

                if(header.isPointInFloatFormat()){

                    LFloatPoint floatPoint = (LFloatPoint)point;
                    sc= new SphericalCoordinates(new Vector3D(floatPoint.x, floatPoint.y, floatPoint.z).normalize());
                }else{
                    LDoublePoint doublePoint = (LDoublePoint)point;
                    sc= new SphericalCoordinates(new Vector3D(doublePoint.x, doublePoint.y, doublePoint.z).normalize());
                }

                maxAzimutalAngle.addValue(sc.getTheta());
            }
            
            if(maxAzimutalAngle.getNbValues() > 0){
                break;
            }
        }
        
        azim_max = maxAzimutalAngle.getMean();
        colIndexAzimMax = i;
        
        //compute min zenithal angle
        //compute max azimutal angle
        
        resetColumnLimits();
        
        Statistic minZenithalAngle = new Statistic();
        Statistic maxZenithalAngle = new Statistic();
            
        for (i = 0; i < header.getNumRows(); i++) {

            setUpRowToRead(i);

            Iterator<LPoint> iterator = this.iterator();

            while (iterator.hasNext()) {

                LPoint point = iterator.next();

                SphericalCoordinates sc;

                if (header.isPointInFloatFormat()) {

                    LFloatPoint floatPoint = (LFloatPoint) point;
                    sc= new SphericalCoordinates(new Vector3D(floatPoint.x, floatPoint.y, floatPoint.z).normalize());
                } else {
                    LDoublePoint doublePoint = (LDoublePoint) point;
                    sc= new SphericalCoordinates(new Vector3D(doublePoint.x, doublePoint.y, doublePoint.z).normalize());
                }

                minZenithalAngle.addValue(sc.getPhi());
            }

            if (minZenithalAngle.getNbValues() > 0) {
                break;
            }
        }
        
        elev_min = minZenithalAngle.getMean();
        rowIndexElevMin = i;
        
        for (i = header.getNumRows() - 1; i >= 0; i--) {

            setUpRowToRead(i);

            Iterator<LPoint> iterator = this.iterator();

            while (iterator.hasNext()) {

                LPoint point = iterator.next();

                SphericalCoordinates sc;

                if (header.isPointInFloatFormat()) {

                    LFloatPoint floatPoint = (LFloatPoint) point;
                    sc= new SphericalCoordinates(new Vector3D(floatPoint.x, floatPoint.y, floatPoint.z).normalize());
                } else {
                    LDoublePoint doublePoint = (LDoublePoint) point;
                    sc= new SphericalCoordinates(new Vector3D(doublePoint.x, doublePoint.y, doublePoint.z).normalize());
                }

                maxZenithalAngle.addValue(sc.getPhi());
            }

            if (maxZenithalAngle.getNbValues() > 0) {
                break;
            }

        }
        
        elev_max = maxZenithalAngle.getMean();
        rowIndexElevMax = i;
        
        resetRowLimits();
        resetColumnLimits();
    }
    
    /**
     * Compute azimutal step angle from the extremums angles.
     * <p>If the extremums are unknown, then the method
     * {@link #computeExtremumsAngles() computeExtremumsAngles()} will be called.</p>
     */
    protected void computeAzimutalStepAngle(){
        
        if(Double.isNaN(azim_min) || Double.isNaN(azim_max)){
            computeExtremumsAngles();
        }
        
        
        //azimutalStepAngle = (Math.abs(azim_min)-Math.abs(azim_max))/(double)(colIndexAzimMax - colIndexAzimMin);
        
        double fov = azim_min - azim_max;
        if(Math.abs(azim_max-azim_min) < 0.1){
            fov += (Math.PI * 2);
        }
        
        azimutalStepAngle = fov/(double)(colIndexAzimMax - colIndexAzimMin);
    }
    
    /**
     * Compute zenithal step angle from the extremums angles.
     * <p>If the extremums are unknown, then the method
     * {@link #computeExtremumsAngles() computeExtremumsAngles()} will be called.</p>
     */
    protected void computeZenithalStepAngle(){
        
        if(Double.isNaN(elev_min) || Double.isNaN(elev_max)){
            computeExtremumsAngles();
        }
        
        double fov = elev_min - elev_max;
        elevationStepAngle = fov/((double)rowIndexElevMax - rowIndexElevMin);
    }
    
    /**
     * Reset the row range limits to default values.
     * <p>The values can have been modified by a call to the following methods :</p>
     * <ul>
     * <li>{@link #setUpRowToRead(int) setUpRowToRead(int)}</li>
     * <li>{@link #setUpRowsToRead(int, int) setUpRowsToRead(int, int)}</li>
     * </ul>
     */
    public void resetRowLimits(){
        setUpRowsToRead(0, header.getNumRows()-1);
    }
    
    /**
     * Reset the columns range limits to default values.
     * <p>The values can have been modified by a call to the following methods :</p>
     * <ul>
     * <li>{@link #setUpColumnToRead(int) setUpColumnToRead(int)}</li>
     * <li>{@link #setUpColumnsToRead(int, int) setUpColumnsToRead(int, int)}</li>
     * </ul>
     */
    public void resetColumnLimits(){
        
        setUpColumnsToRead(0, header.getNumCols()-1);
    }
    
    /**
     * Set the column index to read from the file, others columns will be ignored.
     * <p>You should invoke this method before you get the iterator.</p>
     * @param columnIndex The column index to read
     */
    public void setUpColumnToRead(int columnIndex){
        this.startColumnIndex = columnIndex;
        this.endColumnIndex = columnIndex;
    }
    
    /**
     * Set the range (inclusion) of column indices to read from the file, columns outside the range will be ignored.
     * <p>You should invoke this method before you get the iterator.</p>
     * @param startColumnIndex The first column index of the range
     * @param endColumnIndex The last column index of the range
     */
    public void setUpColumnsToRead(int startColumnIndex, int endColumnIndex){
        this.startColumnIndex = startColumnIndex;
        this.endColumnIndex = endColumnIndex;
    }
    
    /**
     * Set the row index to read from the file, others rows will be ignored.
     * <p>You should invoke this method before you get the iterator.</p>
     * @param rowIndex The row index to read
     */
    public void setUpRowToRead(int rowIndex){
        this.startRowIndex = rowIndex;
        this.endRowIndex = rowIndex;
    }
    
    /**
     * Set the range (inclusion) of row indices to read from the file, rows outside the range will be ignored.
     * <p>You should invoke this method before you get the iterator.</p>
     * @param startRowIndex The first row index of the range
     * @param endRowIndex The last row index of the range
     */
    public void setUpRowsToRead(int startRowIndex, int endRowIndex){
        this.startRowIndex = startRowIndex;
        this.endRowIndex = endRowIndex;
    }
    
    /**
     * Are invalid points returned ? 
     * <p>An invalid point is a point without a position because the laser shot didn't get a return.</p>
     * @return true if invalid points are returned, false otherwise
     */
    public boolean isReturnInvalidPoint() {
        return returnInvalidPoint;
    }

    /**
     * Set if invalid points are returned or not.
     * <p>An invalid point is a point without a position because the laser shot didn't get a return.</p>
     * @param returnInvalidPoint true if invalid point should be returned, false otherwise
     */
    public void setReturnInvalidPoint(boolean returnInvalidPoint) {
        this.returnInvalidPoint = returnInvalidPoint;
    }

    /**
     * Get column index corresponding to the mimimum azimutal angle.
     * <p>The minimum azimutal angle is obtained from the first non empty column
     * starting from column 0.</p>
     *
     * @return a column index
     */
    public int getColIndexAzimMin() {
        return colIndexAzimMin;
    }
    
    /**
     * Get row index corresponding to the mimimum zenithal angle.
     * <p>The minimum zenithal angle is obtained from the first non empty row
     * starting from row 0.</p>
     *
     * @return a row index
     */
    public int getRowIndexElevMin() {
        return rowIndexElevMin;
    }

    /**
     * Get row index corresponding to the maximum zenithal angle.
     * <p>The maximum zenithal angle is obtained from the first non empty row
     * starting from the last row.</p>
     *
     * @return a row index
     */
    public int getRowIndexElevMax() {
        return rowIndexElevMax;
    }
    
    /**
     * Get column index corresponding to the maximum azimutal angle.
     * <p>The maximum azimutal angle is obtained from the first non empty column
     * starting from the last column.</p>
     *
     * @return a column index
     */
    public int getColIndexAzimMax() {
        return colIndexAzimMax;
    }

    /**
     * Get the first row index to read from the file.
     * The default value is 0.
     * @return the row index
     */
    public int getStartRowIndex() {
        return startRowIndex;
    }

    /**
     * Get the first column index to read from the file.
     * The default value is 0.
     * @return the column index
     */
    public int getStartColumnIndex() {
        return startColumnIndex;
    }

    /**
     * Get the last row index to read from the file.
     * The default value is (number of rows) - 1.
     * @return the row index
     */
    public int getEndRowIndex() {
        return endRowIndex;
    }

    /**
     * Get the last column index to read from the file.
     * The default value is (number of columns) - 1.
     * @return the column index
     */
    public int getEndColumnIndex() {
        return endColumnIndex;
    }

    /**
     * Get the bonded scan file.
     * @return The bonded scan file.
     */
    public File getFile() {
        return file;
    }
    
    public PointScanHeader getHeader() {
        return header;
    }
    
    /**
     * Get the minimum azimutal angle.
     * <p>If the extremums are unknown, then the method
     * {@link #computeExtremumsAngles() computeExtremumsAngles()} will be called.</p>
     * @return The minimum azimutal angle in radians
     */
    public double getAzim_min() {
        
        if(Double.isNaN(azim_min)){
            computeExtremumsAngles();
        }
        
        return azim_min;
    }

    /**
     * Get the maximum azimutal angle.
     * <p>If the extremums are unknown, then the method
     * {@link #computeExtremumsAngles() computeExtremumsAngles()} will be called.</p>
     * @return The maximum azimutal angle in radians
     */
    public double getAzim_max() {
        
        if(Double.isNaN(azim_max)){
            computeExtremumsAngles();
        }
        
        return azim_max;
    }

    /**
     * Get the minimum zenithal angle.
     * <p>If the extremums are unknown, then the method
     * {@link #computeExtremumsAngles() computeExtremumsAngles()} will be called.</p>
     * @return The minimum zenithal angle in radians
     */
    public double getElev_min() {
        
        if(Double.isNaN(elev_min)){
            computeExtremumsAngles();
        }
        return elev_min;
    }

    /**
     * Get the maximum zenithal angle.
     * <p>If the extremums are unknown, then the method
     * {@link #computeExtremumsAngles() computeExtremumsAngles()} will be called.</p>
     * @return The maximum zenithal angle in radians
     */
    public double getElev_max() {
        return elev_max;
    }    

    /**
     * Get the azimuthal step angle.
     * <p>If the extremums are unknown, then the method
     * {@link #computeExtremumsAngles() computeExtremumsAngles()} will be called.</p>
     * @return The azimuthal step angle in radians
     */
    public double getAzimutalStepAngle() {
        
        if(Double.isNaN(azimutalStepAngle)){
            computeAzimutalStepAngle();
        }
        
        return azimutalStepAngle;
    }

    /**
     * Get the zenithal step angle.
     * <p>If the extremums are unknown, then the method
     * {@link #computeExtremumsAngles() computeExtremumsAngles()} will be called.</p>
     * @return The zenithal step angle in radians
     */
    public double getElevationStepAngle() {
        
        if(Double.isNaN(elevationStepAngle)){
            computeZenithalStepAngle();
        }
        
        return elevationStepAngle;
    }

    public int getCurrentColIndex() {
        return currentColIndex;
    }
    
}
