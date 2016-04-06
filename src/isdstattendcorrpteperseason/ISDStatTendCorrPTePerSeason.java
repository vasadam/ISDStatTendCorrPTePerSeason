/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package isdstattendcorrpteperseason;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vas_adam9
 */
public class ISDStatTendCorrPTePerSeason {  
    
    static enum Seasons {
        WINTER,
        SPRING,
        SUMMER,
        AUTUMN
    }
    
    static enum Params {
        P0,
        P1,
        P2,
        P3,
        P4,
        P5,
        TE0,
        TE1,
        TE2,
        TE3,
        TE4,
        TE5
    }

    public static void main(String[] args) {
        File rootDir = new File("C:\\Users\\EDMMVAS\\Documents\\NOAA\\ISD_stat_tend_corr_per_season");
        File destDir = new File("C:\\Users\\EDMMVAS\\Documents\\NOAA\\ISD_stat_tend_corr_PandTe_per_season");

        File[] stationDirs = rootDir.listFiles();
        HashMap<Integer, ArrayList<String[]>> corrPerHours= new HashMap<>();
        corrPerHours.put(0, new ArrayList<>());
        corrPerHours.put(12, new ArrayList<>());
        for (File stationDir : stationDirs) {
            System.out.println(stationDir.getName());
            File[] hourFiles = stationDir.listFiles();
            for (File hourFile : hourFiles) {
                System.out.println(hourFile.getName());
                int hour = Integer.parseInt(hourFile.getName().split("[.]")[0]);
                ArrayList<String[]> hourList = corrPerHours.get(hour);
                try (Scanner sc = new Scanner(hourFile)) {
                    while (sc.hasNextLine()) {                        
                        String[] values = sc.nextLine().trim().split("[,]"); // System.out.println("," + ++numLine);
                        String season = values[0];
                        String parameter = values[1];
                        String day = values[2];
                        String corr = values[3];
                        String pVal = values[4];

//                        if (!(   (season.equals("winter") && parameter.equals("Te") && day.equals("0"))
//                              || (season.equals("spring") && parameter.equals("d1Te9h") && day.equals("0"))
//                              || (season.equals("summer") && parameter.equals("Te") && day.equals("0"))
//                              || (season.equals("autumn") && parameter.equals("P") && day.equals("-1")))) {
                        if (!(parameter.equals("Te") || parameter.equals("P"))
                            || !day.equals("0")) {                            
                            continue;
                        }

                        // Add record only if p <= 0.05
                        if (Double.parseDouble(pVal) <= 0.05) {
                            hourList.add(new String[]{stationDir.getName(),
                                                      season,
                                                      parameter,
                                                      day,
                                                      corr,
                                                      pVal});
                        }
                        // Choose the strongest correlation among P and Te
                        if (hourList.size() > 1) {
                            String prevStationName = hourList.get(hourList.size()-2)[0];
                            String prevSeason = hourList.get(hourList.size()-2)[1];
                            if (stationDir.getName().equals(prevStationName) && season.equals(prevSeason)) {
                                if (Math.abs(Double.parseDouble(corr))
                                    > Math.abs(Double.parseDouble(hourList.get(hourList.size()-2)[4]))) {

                                    hourList.remove(hourList.size()-2);
                                }
                                else {
                                    hourList.remove(hourList.size()-1);
                                }                        
                            }
                        }
                    }
                    corrPerHours.put(hour, hourList);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ISDStatTendCorrPTePerSeason.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        }
        
        // Write 00h and 12h values into files
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(destDir + "\\00.txt"))) {
            ArrayList<String[]> hourList = corrPerHours.get(0);
            for (String[] record : hourList) {
                bw.append(record[0]);
                bw.append(",");
                bw.append(record[1]);
                bw.append(",");
                bw.append(record[2]);
                bw.append(",");
                bw.append(record[3]);
                bw.append(",");
                bw.append(record[4]);
                bw.append(",");
                bw.append(record[5]);
                bw.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(ISDStatTendCorrPTePerSeason.class.getName()).log(Level.SEVERE, null, ex);
        }    
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(destDir + "\\12.txt"))) {
            ArrayList<String[]> hourList = corrPerHours.get(12);
            for (String[] record : hourList) {
                bw.append(record[0]);
                bw.append(",");
                bw.append(record[1]);
                bw.append(",");
                bw.append(record[2]);
                bw.append(",");
                bw.append(record[3]);
                bw.append(",");
                bw.append(record[4]);
                bw.append(",");
                bw.append(record[5]);
                bw.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(ISDStatTendCorrPTePerSeason.class.getName()).log(Level.SEVERE, null, ex);
        }     
    }
}
