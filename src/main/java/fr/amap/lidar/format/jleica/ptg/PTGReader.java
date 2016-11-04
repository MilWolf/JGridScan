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
package fr.amap.lidar.format.jleica.ptg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>This class is dedicated to handle PTG files, a Leica gridded point format 
 * (see <a href= "http://www.xdesy.de/freeware/PTG-DLL/PTG-1.0.pdf"> specification</a>)</p>
 * <p>You can use this class to get the scan list from the ascii ptg file or use it to determines 
 * if the input file is the ascii file or a binary file.
 * If you already know that the type of the input file is a binary scan file, you can use the class {@link PTGScan PTGScan}.</p>
 *
 * @author Julien Heurtebize
 */
public class PTGReader{
    
    private List<File> scanList;
    private boolean binaryFile;
    private boolean asciiFile;
    
    /**
     *
     */
    public PTGReader() {
    }
    
    /**
     * <p>Open a ptg file from the specified file.</p>
     * <p>The input file can be either ascii or binary.</p>
     * <p>PTG specification defines the ascii file as the file containing the list of ptg binary scan files
     * and binaries files as the actual scans.</p>
     * <p>When a file is opened, use methods {@link isBinaryFile() isBinaryFile()} or {@link isAsciiFile() isAsciiFile()}</p>
     * <p>If file is the ascii file, then you can use method {@link getScanList() getScanList()} to get the ptg file scan list.
     * Else if scan is a binary file, you can use class {@link PTGScan PTGScan} to open it.</p>
     * @param file The input file
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void openPTGFile(File file) throws IOException, FileNotFoundException{
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            
            //searching for ascii header
            String header = reader.readLine();
            if(header.equals("PTG index file")){
                
                //file is an ascii PTG file, reading binary file list
                
                //skipping useless line
                String uselessLine = reader.readLine();
                
                if(uselessLine != null){
                    
                    scanList = new ArrayList<>();
                    
                    String fileLine;
                    
                    while((fileLine = reader.readLine()) != null){
                        fileLine = fileLine.replace('\\', File.separatorChar);
                        scanList.add(new File(file.getParentFile().getAbsolutePath()+File.separator+fileLine));
                    }
                    
                    asciiFile = true;
                }
                
            }else{
                
                //file must be a binary scan file
                binaryFile = true;
            }
        }
    }

    /**
     * Check if opened file is a binary file or not
     * @return true if file is a binary file, false otherwise
     */
    public boolean isBinaryFile() {
        return binaryFile;
    }

    /**
     * Check if opened file is an ascii file or not
     * @return true if file is an ascii file, false otherwise
     */
    public boolean isAsciiFile() {
        return asciiFile;
    }

    /**
     * Get the list of scan files in case opened file was an ascii file
     * @return A list of files
     */
    public List<File> getScanList() {
        return scanList;
    }
    
//    public static void main(String[] args)  throws IOException, Exception{
//                
//        
//        //mainTest();
//        //test1();
//        //test2();
//        //test3();
//        //shotTest();
//        //test4();
//        test5();
//    }
//    
//    public static void test5() throws Exception{
//        
//        PTGScan scan2 = new PTGScan();
//        scan2.openScanFile(new File("/media/forestview01/BDLidar/TLS/Test_scanners/Vezenobres/Leica C10/registered_scans/registered_scans-00.PTG"));
//        PTGHeader header = scan2.getHeader();
//        Iterator<LPoint> iterator2 = scan2.iterator();
//
//        while(iterator2.hasNext()){
//            LPoint point = iterator2.next();
//            System.out.println(point.intensity);
//        }
//        
//        File ptgFile = new File("/media/calcul/6653-C343/Projet_Nicolas/projet2015-11-16.ptg");
//        PTGReader reader = new PTGReader();
//        reader.openPTGFile(ptgFile);
//        
//        List<File> scanList = reader.getScanList();
//        int count = 0;
//        
//        for(File file : scanList){
//
//            //try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsolutePath()+".txt"))) {
//                
//                PTGScan scan = new PTGScan();
//                scan.openScanFile(file);
//                
//                Iterator<LPoint> iterator = scan.iterator();
//
//                while(iterator.hasNext()){
//                    LPoint point = iterator.next();
//                    count++;
//                }
//            
//            /*LPointShotExtractor shots = new LPointShotExtractor(scan);
//            Iterator<LShot> iterator = shots.iterator();
//            while(iterator.hasNext()){
//            LShot shot = iterator.next();
//            shot.direction.normalize();
//            shot.direction.scale(50);
//            //writer.write(shot.direction.x+" "+shot.direction.y+" "+shot.direction.z+" "+shot.nbEchos+"\n");
//            }*/
//            //}
//        }
//        
//        System.out.println("Nb points: "+count);
//        
//    }
//    
//    public static void test4() throws Exception{
//        
//        File ptgFile = new File("/media/calcul/6653-C343/ptg/leica/leica_scans/leica_scans-2.PTG");
//        
//        BufferedWriter writer1 = new BufferedWriter(new FileWriter(new File("/home/calcul/Documents/Julien/slope_test.obj")));
//        BufferedWriter writer2 = new BufferedWriter(new FileWriter(new File("/home/calcul/Documents/Julien/slope_test_computed.obj")));
//        
//        PTGScan scan = new PTGScan();
//        scan.openScanFile(ptgFile);
//        
//        LPointShotExtractor shots = new LPointShotExtractor(scan);
//        
//        Iterator<LShot> iterator = shots.iterator();
//        
//        SimpleRegression regressionXZ = new SimpleRegression(false);
//        SimpleRegression regressionYZ = new SimpleRegression(false);
//        
//        SimpleRegression regression2 = new SimpleRegression();
//        
//        List<Vector3d> computedPoints = new ArrayList<>();
//        
//        while(iterator.hasNext()){
//            
//            LShot shot = iterator.next();
//            
//            
//            shot.direction.add(shot.origin);
//            shot.direction.scale(50);
//            
//            if(shot.nbEchos == 0){
//                writer2.write("v "+shot.direction.x+" "+shot.direction.y+" "+shot.direction.z+"\n");
//                regression2.addData((float)shot.direction.y, (float)shot.direction.z);
//                computedPoints.add(shot.direction);
//            }else{
//                writer1.write("v "+shot.direction.x+" "+shot.direction.y+" "+shot.direction.z+"\n");
//                regressionXZ.addData((float)shot.direction.x, (float)shot.direction.z);
//                regressionYZ.addData((float)shot.direction.y, (float)shot.direction.z);
//            }
//        }
//        
//        double slopeX = regressionXZ.getSlope();
//        System.out.println("slope X ~ Z (read): "+slopeX);
//        double intercept = regressionXZ.getIntercept();
//        System.out.println("Leica data regression line : "+"Y = "+slopeX+"x"+" + "+intercept);
//        
//        //System.out.println("\n");
//        
//        double slopeY = regressionYZ.getSlope();
//        System.out.println("slope Y ~ Z (read): "+slopeY);
//        intercept = regressionYZ.getIntercept();
//        System.out.println("Regression line : "+"Y = "+slopeY+"x"+" + "+intercept);
//        
//        //angle, pour y ~ z
//        double angleYZ = -Math.atan(slopeY);
//        
//        //angle, pour x ~ z
//        double angleXZ = -Math.atan(slopeX);
//                
//        //pour une pente dans le plan x;z il faut effectuer une rotation autour de l'axe y
//        //pour une pente dans le plan y;z il faut effectuer une rotation autour de l'axe x
//        
//        Mat4D rotationY = new Mat4D();
//        rotationY.mat = new double[]{
//            Math.cos(angleXZ), 0.0, Math.sin(angleXZ), 0.0,
//            0.0, 1.0, 0.0, 0.0,
//            -Math.sin(angleXZ), 0.0, Math.cos(angleXZ), 0.0,
//            0.0, 0.0, 0.0, 1.0};
//        
//        Mat4D rotationX = new Mat4D();
//        rotationX.mat = new double[]{
//            1.0, 0.0, 0.0, 0.0,
//            0.0, Math.cos(angleYZ), -Math.sin(angleYZ), 0.0,
//            0.0, Math.sin(angleYZ), Math.cos(angleYZ), 0.0,
//            0.0, 0.0, 0.0, 1.0
//        };
//        
//        //Mat4D correctionMatrix = Mat4D.inverse(Mat4D.multiply(rotationY, rotationX));
//        
//        
//        BufferedWriter writer3 = new BufferedWriter(new FileWriter(new File("/home/calcul/Documents/Julien/slope_test_computed_corrected.obj")));
//        SimpleRegression newRegression = new SimpleRegression();
//        
//        for(Vector3d point : computedPoints){
//            
//            Vec4D transformedPoint = Mat4D.multiply(rotationX, new Vec4D((float)point.x, (float)point.y, (float)point.z, 0));
//            transformedPoint = Mat4D.multiply(rotationY, new Vec4D((float)transformedPoint.x, (float)transformedPoint.y, (float)transformedPoint.z, 0));
//            Vec3D vec = Vec3D.multiply(new Vec3D((float)transformedPoint.x, (float)transformedPoint.y, (float)transformedPoint.z), 1);
//            newRegression.addData((float)vec.x, (float)vec.z);
//            //newRegression.addData((float)(point.x), (float)(point.z));
//            
//            writer3.write("v "+vec.x+" "+vec.y+" "+vec.z+"\n");
//        }
//        
//        System.out.println("New computed regression line (after transform) : "+"Y = "+newRegression.getSlope()+"x"+" + "+newRegression.getIntercept());
//        System.out.println("Difference : "+(newRegression.getSlope() - regressionXZ.getSlope()));
//        
//        writer3.close();
//        
//        //double slope2 = regression2.getSlope();
//        //System.out.println("slope of computed: "+slope2);
//        
//        writer1.close();
//        writer2.close();
//    }
//    
//    public static void mainTest()  throws IOException, Exception{
//        
//        //File ptgFile = new File("E:\\HolzturmSpitze-0.ptg");
//        File ptgFile = new File("/media/calcul/6653-C343/ptg/leica/leica_scans/leica_scans-0.PTG");
//        PTGReader reader = new PTGReader();
//        reader.openPTGFile(ptgFile);
//        
//        if(reader.isAsciiFile()){
//            
//            List<File> scanList = reader.getScanList();
//            
//            for(File file : scanList){
//                PTGScan scan = new PTGScan();
//                scan.openScanFile(file);
//                PTGHeader header = scan.getHeader();
//                System.out.println(header.toString());
//                
//                Iterator<LPoint> iterator = scan.iterator();
//                
//                int count = 0;
//                while(iterator.hasNext()){
//                    
//                    LPoint point = iterator.next();
//                    
//                    if(header.isPointInFloatFormat()){
//                        
//                        //System.out.println(((LFloatPoint)point).x+ " "+((LFloatPoint)point).y+ " "+((LFloatPoint)point).z);
//                    }
//                    
//                    count++;
//                    
//                    if(point == null){
//                        System.out.println("test");
//                    }
//                }
//                
//                System.out.println("Nombre de points : "+count);
//                //scan.printPoints();
//            }
//            
//        }else{
//            PTGScan scan = new PTGScan();
//            scan.openScanFile(ptgFile);
//            PTGHeader header = scan.getHeader();
//            System.out.println(header.toString());
//            
//            //scan.printPoints();
//            
//            //scan.setUpRowToRead(0);
//            //scan.setUpRowsToRead(200, 201);
//            //scan.setUpRowsToRead(39, 59);
//            scan.setUpRowToRead(header.getNumRows()-1);
//            scan.setReturnInvalidPoint(true);
//            
//            Iterator<LPoint> iterator = scan.iterator();
//            
//            //BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/home/calcul/Documents/Julien/ptg_0_b.obj")));
//            Statistic intensityStatistic = new Statistic();
//            Statistic rangeStatistic = new Statistic();
//            
//            int count = 0;
//            int nbPointsValid = 0;
//            int nbPointsInvalid = 0;
//            
//            while(iterator.hasNext()){
//
//                LPoint point = iterator.next();
//                
//                if(point.valid){
//                    nbPointsValid++;
//                }else{
//                    nbPointsInvalid++;
//                }
//
////                if(header.isPointInFloatFormat()){
////                    
////                    LFloatPoint floatPoint = (LFloatPoint)point;
////                    //writer.write("v "+floatPoint.x+" "+floatPoint.y+" "+floatPoint.z+"\n");
////                    intensityStatistic.addValue(floatPoint.intensity);
////                    double distance = Math.sqrt((floatPoint.x*floatPoint.x) + (floatPoint.y*floatPoint.y) + (floatPoint.z*floatPoint.z));
////                    rangeStatistic.addValue(distance);
////                    //System.out.println(((LFloatPoint)point).x+ " "+((LFloatPoint)point).y+ " "+((LFloatPoint)point).z);
////                }
//
//                count++;
//            }
//            
//            //writer.close();
//            
//
//            System.out.println("Nombre de points : "+count);
//            
//            /*System.out.println("Min/max intensity: "+intensityStatistic.getMinValue()+" -> "+intensityStatistic.getMaxValue());
//            System.out.println("Min/max distance: "+rangeStatistic.getMinValue()+" -> "+rangeStatistic.getMaxValue());
//            
//            //System.out.println("Min/max azimutal angle : "+Math.toDegrees(scan.getAzim_min())+" -> "+Math.toDegrees(scan.getAzim_max()));
//            System.out.println("Azimutal step angle : "+Math.toDegrees(scan.getAzimutalStepAngle()));
//            
//            System.out.println("min zenithal angle : "+Math.toDegrees(scan.getElev_min()));
//            System.out.println("max zenithal angle : "+Math.toDegrees(scan.getElev_max()));
//            System.out.println("zenithal step angle : "+Math.toDegrees(scan.getElevationStepAngle()));*/
//        
//            //scan.printPoints();
//        }
//    }
//    
//    public static void test2() throws IOException, Exception{
//        
//        File ptgFile = new File("/media/calcul/6653-C343/ptg/leica/leica_scans/leica_scans-0.PTG");
//        
//        PTGScan scan = new PTGScan();
//        scan.openScanFile(ptgFile);
//        PTGHeader header = scan.getHeader();
//        System.out.println(header.toString());
//        
//        System.out.println("min azimutal angle : "+Math.toDegrees(scan.getAzim_min()));
//        //System.out.println("max azimutal angle : "+Math.toDegrees(scan.getAzim_max()));
//        System.out.println("azimutal step angle : "+Math.toDegrees(scan.getAzimutalStepAngle()));
//        
//        System.out.println("min zenithal angle : "+Math.toDegrees(scan.getElev_min()));
//        System.out.println("max zenithal angle : "+Math.toDegrees(scan.getElev_max()));
//        System.out.println("zenithal step angle : "+Math.toDegrees(scan.getElevationStepAngle()));
//    }
//    
//    public static void test3() throws IOException, Exception{
//        
//        File ptgFile = new File("/media/calcul/6653-C343/ptg/leica/leica_scans/leica_scans-0.PTG");
//        
//        PTGScan scan = new PTGScan();
//        scan.openScanFile(ptgFile);
//        PTGHeader header = scan.getHeader();
//        System.out.println(header.toString());
//        
//        scan.setUpRowToRead(0);
//            
//        Iterator<LPoint> iterator = scan.iterator();
//        Statistic azimutalAngle1 = new Statistic();
//        Statistic zenitalAngle1 = new Statistic();
//
//        while(iterator.hasNext()){
//
//            LPoint point = iterator.next();
//
//            if(header.isPointInFloatFormat()){
//
//                LFloatPoint floatPoint = (LFloatPoint)point;
//                SphericalCoordinates sc = new SphericalCoordinates();
//                sc.toSpherical(new Point3d(floatPoint.x, floatPoint.y, floatPoint.z));
//                azimutalAngle1.addValue(sc.getAzimut());
//                zenitalAngle1.addValue(sc.getZenith());
//            }
//        }
//        
//        
//        double min1 = azimutalAngle1.getMinValue();
//        double max1 = azimutalAngle1.getMaxValue();
//        double mean1 = azimutalAngle1.getMean();
//        
//        Statistic azimutalAngle2 = new Statistic();
//        Statistic zenitalAngle2 = new Statistic();
//        
//        scan.setUpRowToRead(1);
//            
//        iterator = scan.iterator();
//
//        while(iterator.hasNext()){
//
//            LPoint point = iterator.next();
//
//            if(header.isPointInFloatFormat()){
//
//                LFloatPoint floatPoint = (LFloatPoint)point;
//                SphericalCoordinates sc = new SphericalCoordinates();
//                sc.toSpherical(new Point3d(floatPoint.x, floatPoint.y, floatPoint.z));
//                azimutalAngle2.addValue(sc.getAzimut());
//                zenitalAngle2.addValue(sc.getZenith());
//            }
//        }
//        
//        double min2 = azimutalAngle2.getMinValue();
//        double max2 = azimutalAngle2.getMaxValue();
//        double mean2 = azimutalAngle2.getMean();
//        
//        double step = zenitalAngle1.getMean() - zenitalAngle2.getMean();
//        
//        System.out.println(step);
//            
//    }
//    
//    
//    
//    public static void shotTest() throws Exception{
//        
//        File ptgFile = new File("/media/calcul/6653-C343/test_indoor/test_indoor-8.PTG");
//        
//        PTGScan scan = new PTGScan();
//        scan.openScanFile(ptgFile);
//        
//        LPointShotExtractor pTGShots = new LPointShotExtractor(scan);
//        //scan.setUpRowsToRead(1, 9);
//        //scan.setUpRowsToRead(scan.getHeader().getNumRows()-11, scan.getHeader().getNumRows()-1);
//        Iterator<LShot> iterator = pTGShots.iterator();
//        
//        int count = 0;
//        int count2 = 0;
//        
//        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/home/calcul/Documents/Julien/test_shots_indoor.obj")));
//        BufferedWriter writer2 = new BufferedWriter(new FileWriter(new File("/home/calcul/Documents/Julien/test_shots_0_b.txt")));
//        
//        while(iterator.hasNext()){
//            LShot shot = iterator.next();
//            
//            shot.direction.add(shot.origin);
//            shot.direction.scale(10);
//            
//            
//            if(shot.nbEchos == 0){
//                
//                //correction de la pente, ajustement du modèle simulé avec le modèle réel
//                /*Vec4D transformedPoint = Mat4D.multiply(rotation, new Vec4D(shot.direction.x, shot.direction.y, shot.direction.z, 1));
//                transformedPoint = Mat4D.multiply(rotationX, transformedPoint);
//                
//                shot.direction.x = transformedPoint.x;
//                shot.direction.y = transformedPoint.y;
//                shot.direction.z = transformedPoint.z;*/
//                
////                SphericalCoordinates sc = new SphericalCoordinates();
////                sc.toSpherical(new Point3d(shot.direction.x, shot.direction.y, shot.direction.z));
////                double elevation = sc.getElevation();
////                double azimuth = sc.getAzimuth();
////                
////                /*décalage en élevation dû à la rotation, ici je corrige d'un offset approximatif 
////                mais il faudrait corriger du décalage entre les deux droites*/
////                sc = new SphericalCoordinates(azimuth, elevation/*+(scan.getElevationStepAngle()*2.0)*/, 50);
////                Point3d cartesian = sc.toCartesian();
////                
////                shot.direction.x = cartesian.x;
////                shot.direction.y = cartesian.y;
////                shot.direction.z = cartesian.z;
//                
//                //writer.write("v "+shot.direction.x+" "+shot.direction.y+" "+shot.direction.z+" "+shot.nbEchos+" "+shot.point.columnIndex+"\n");
//            }else{
//                
//                //writer2.write("v "+shot.direction.x+" "+shot.direction.y+" "+shot.direction.z+" "+shot.nbEchos+" "+shot.point.columnIndex+"\n");
//            }
//            
//            writer.write("v "+shot.direction.x+" "+shot.direction.y+" "+shot.direction.z+" "+shot.nbEchos/*+" "+shot.point.columnIndex+" "+shot.point.rowIndex*/+"\n");
//            
//            
//            //writer.write("v "+shot.direction.x+" "+shot.direction.y+" "+shot.direction.z+" "+shot.nbEchos+" "+shot.point.columnIndex+"\n");
//            //writer.write("v "+shot.direction.x+" "+shot.direction.y+" "+shot.direction.z+" "+shot.nbEchos+"\n");
//            /*System.out.println(shot.nbEchos+ "\t" +shot.origin.x+ "\t" +shot.origin.y+ "\t" +shot.origin.z+
//                                            "\t" +shot.direction.x+ "\t" +shot.direction.y+ "\t" +shot.direction.z);*/
//            
//            count++;
//            count2++;
//            
//            if(count2 == 1000000){
//                System.out.println("Shots processed: "+count);
//                count2 = 0;
//            }
//            
//        }
//        writer.close();
//        writer2.close();
//        System.out.println("nb shots : "+count);
//    }
    
    
}
