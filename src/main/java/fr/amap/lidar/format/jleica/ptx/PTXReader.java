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
package fr.amap.lidar.format.jleica.ptx;

import fr.amap.lidar.format.jleica.LDoublePoint;
import fr.amap.lidar.format.jleica.LPoint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

/**
 *
 * @author Julien Heurtebize
 */
public class PTXReader {
    
    private File file;
    private long currentLineIndex;
    private final List<PTXScan> singlesScans;

    public PTXReader() {
        singlesScans = new ArrayList<>();
    }
    
    
    public void openPTXFile(File file) throws IOException, FileNotFoundException{
        
        this.file = file;
        currentLineIndex = 0;
        
        //read all scan headers
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            
            PTXScan singleScan = readSingleScanHeader(reader);
            
            while(singleScan != null){
                singlesScans.add(singleScan);
                
                long nbPoints = singleScan.getHeader().getNumCols() * singleScan.getHeader().getNumRows();
                skipLines(reader, nbPoints);
                
                currentLineIndex += nbPoints;
                singleScan = readSingleScanHeader(reader);
            }
        }
    }
    
    private PTXScan readSingleScanHeader(BufferedReader reader){
        
        try {
            PTXHeader header = new PTXHeader();
            
            header.setNumRows(Integer.valueOf(getNextLine(reader)));
            header.setNumCols(Integer.valueOf(getNextLine(reader)));
            header.setPointInDoubleFormat(true);
            
            String[] registeredPos = getNextLine(reader).split(" ");
            header.setScannerRegisteredPosition(new Point3d(Double.valueOf(registeredPos[0]),
                    Double.valueOf(registeredPos[1]),
                    Double.valueOf(registeredPos[2])));
            
            String[] registeredAxisX = getNextLine(reader).split(" ");
            header.setScannerRegisteredAxisX(new Point3d(Double.valueOf(registeredAxisX[0]),
                    Double.valueOf(registeredAxisX[1]),
                    Double.valueOf(registeredAxisX[2])));
            
            String[] registeredAxisY = getNextLine(reader).split(" ");
            header.setScannerRegisteredAxisY(new Point3d(Double.valueOf(registeredAxisY[0]),
                    Double.valueOf(registeredAxisY[1]),
                    Double.valueOf(registeredAxisY[2])));
            
            String[] registeredAxisZ = getNextLine(reader).split(" ");
            header.setScannerRegisteredAxisZ(new Point3d(Double.valueOf(registeredAxisZ[0]),
                    Double.valueOf(registeredAxisZ[1]),
                    Double.valueOf(registeredAxisZ[2])));
            
            String[] transfMatrixRow0 = getNextLine(reader).split(" ");
            String[] transfMatrixRow1 = getNextLine(reader).split(" ");
            String[] transfMatrixRow2 = getNextLine(reader).split(" ");
            String[] transfMatrixRow3 = getNextLine(reader).split(" ");
            
            double m00 = Double.valueOf(transfMatrixRow0[0]);
            double m01 = Double.valueOf(transfMatrixRow1[0]);
            double m02 = Double.valueOf(transfMatrixRow2[0]);
            double m03 = Double.valueOf(transfMatrixRow3[0]);
            double m10 = Double.valueOf(transfMatrixRow0[1]);
            double m11 = Double.valueOf(transfMatrixRow1[1]);
            double m12 = Double.valueOf(transfMatrixRow2[1]);
            double m13 = Double.valueOf(transfMatrixRow3[1]);
            double m20 = Double.valueOf(transfMatrixRow0[2]);
            double m21 = Double.valueOf(transfMatrixRow1[2]);
            double m22 = Double.valueOf(transfMatrixRow2[2]);
            double m23 = Double.valueOf(transfMatrixRow3[2]);
            double m30 = Double.valueOf(transfMatrixRow0[3]);
            double m31 = Double.valueOf(transfMatrixRow1[3]);
            double m32 = Double.valueOf(transfMatrixRow2[3]);
            double m33 = Double.valueOf(transfMatrixRow3[3]);
            
            Matrix4d transfMatrix  = new Matrix4d();
            transfMatrix.set(new double[]{
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33
            });
            
            header.setTransfMatrix(transfMatrix);
            
            if(header.getNumCols() * header.getNumRows() != 0){
                
                //read first point
                reader.mark(1000);

                String firstPoint = reader.readLine();
                if(firstPoint != null){
                    String[] split = firstPoint.split(" ");
                    
                    if(split.length > 3){
                            
                        header.setPointContainsIntensity(true);

                        if(split.length > 6){
                            header.setPointContainsRGB(true);
                        }
                    }
                }

                reader.reset();
            }
            
            
            return new PTXScan(file, header, currentLineIndex);
            
        }catch (IOException | NumberFormatException ex ) {
            return null;
        }
    }
    
    private void skipLines(BufferedReader reader, long nbLinesToSkip) throws IOException{
        
        int nbLinesSkipped = 0;

        while(nbLinesSkipped < nbLinesToSkip){

            reader.readLine();
            nbLinesSkipped++;
        }
    }
    
    private String getNextLine(BufferedReader reader) throws IOException{
        
        String line = reader.readLine();
        
        if(line != null){
            currentLineIndex++;
        }
        
        return line;
    }
    
    /**
     * Get the list of scans contained in the ptx file
     * @return A list of scans
     */
    public List<PTXScan> getSinglesScans() {
        return singlesScans;
    }    
    
    /**
     * Get the number of scans contained in the ptx file
     * @return The number of scans
     */
    public int getNbScans(){
        
        if(singlesScans == null){
            return -1;
        }
        
        return singlesScans.size();
    }
    
    public static void main(String[] args){
        
        int count = 0;
        try{
            PTXReader reader = new PTXReader();
            reader.openPTXFile(new File("/home/calcul/Documents/Julien/AMAPVox/trunk/AMAPVox/JLeica/src/test/resources/scan_test.ptx"));
            
            long startTime = System.currentTimeMillis();
            
            double time = (System.currentTimeMillis() - startTime)/1000.0;
            System.out.println("Time to read scans header : "+time+" s");
            
            List<PTXScan> singlesScans = reader.getSinglesScans();
            for(PTXScan scan : singlesScans){
                System.out.println(scan.getHeader().toString()+"\n\n");
            }
            
            startTime = System.currentTimeMillis();
            
            for(PTXScan scan : singlesScans){
                
                scan.setReturnInvalidPoint(true);
                scan.setUpColumnToRead(1);
                scan.setUpRowToRead(3);
                
                Iterator<LPoint> iterator = scan.iterator();
            
                while(iterator.hasNext()){

                    LPoint point = iterator.next();
                    if(point.valid){
                        LDoublePoint dPoint = (LDoublePoint)point;
                        System.out.println(dPoint.x+" "+dPoint.y+" "+dPoint.z);
                    }

                    count++;

                }
            }
            
            time = (System.currentTimeMillis() - startTime)/1000.0;
            System.out.println("Time to read file : "+time+" s");
            
        }catch(Exception e){
            System.err.println("Error occured");
        }finally{
            System.out.println("Nb points: "+count);
        }
        
    }
}
