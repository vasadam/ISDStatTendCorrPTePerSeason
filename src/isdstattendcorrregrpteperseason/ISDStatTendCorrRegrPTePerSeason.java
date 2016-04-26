/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package isdstattendcorrregrpteperseason;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vas_adam9
 */
public class ISDStatTendCorrRegrPTePerSeason {  

    public static void main(String[] args) {
        File rootDir = new File("D:\\NOAA\\ISD_stat_tend_corr_per_season");
        File destDir = new File("D:\\NOAA\\ISD_stat_tend_corr_regr_PandTe_per_season");

        File[] stationDirs = rootDir.listFiles();
        HashMap<Integer, ArrayList<String[]>> corrPerHours= new HashMap<>();
        corrPerHours.put(0, new ArrayList<>());
        corrPerHours.put(12, new ArrayList<>());
        for (File corrStationDir : stationDirs) {
            System.out.println(corrStationDir.getName());
            File[] corrHourFiles = corrStationDir.listFiles((hourFile) -> hourFile.getName().endsWith(".txt"));
            for (File corrHourFile : corrHourFiles) {
                System.out.println(corrHourFile.getName());
                File regrHourFile = new File(corrHourFile.getAbsolutePath().replace("corr", "regr"));                
                int hour = Integer.parseInt(corrHourFile.getName().split("[.]")[0]);
                ArrayList<String[]> hourList = corrPerHours.get(hour);
                try (Scanner corrSc = new Scanner(corrHourFile);
                     Scanner regrSc = new Scanner(regrHourFile)) {     
                    while (corrSc.hasNextLine()) {                        
                        String[] corrValues = corrSc.nextLine().trim().split("[,]");
                        String season = corrValues[0];
                        String parameter = corrValues[1];
                        String day = corrValues[2];
                        String corr = corrValues[3];
                        String pVal = corrValues[4];
                        
                        String[] regrValues = regrSc.nextLine().trim().split("[,]");
                        String a = regrValues[3];
                        String b = regrValues[4];
                        String sigmaA = regrValues[5];
                        String sigmaB = regrValues[6];                        

                        if (!(parameter.equals("Te") || parameter.equals("P"))
                            || !day.equals("0")) {                            
                            continue;
                        }

                        // Add record only if p <= 0.05
                        if (Double.parseDouble(pVal) <= 0.05) {
                            hourList.add(new String[]{corrStationDir.getName(),
                                                      season,
                                                      parameter,
                                                      day,
                                                      corr,
                                                      pVal,
                                                      a,
                                                      b,
                                                      sigmaA,
                                                      sigmaB});
                        }
                        // Choose the strongest correlation among P and Te
                        if (hourList.size() > 1) {
                            String prevStationName = hourList.get(hourList.size()-2)[0];
                            String prevSeason = hourList.get(hourList.size()-2)[1];
                            if (corrStationDir.getName().equals(prevStationName) && season.equals(prevSeason)) {
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
                    Logger.getLogger(ISDStatTendCorrRegrPTePerSeason.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        }
        
        // Write 00h and 12h values into files
        try {
            Files.createDirectories(destDir.toPath());
        } catch (IOException ex) {
            Logger.getLogger(ISDStatTendCorrRegrPTePerSeason.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                bw.append(",");
                bw.append(record[6]);
                bw.append(",");
                bw.append(record[7]);
                bw.append(",");
                bw.append(record[8]);
                bw.append(",");
                bw.append(record[9]);
                bw.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(ISDStatTendCorrRegrPTePerSeason.class.getName()).log(Level.SEVERE, null, ex);
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
                bw.append(",");
                bw.append(record[6]);
                bw.append(",");
                bw.append(record[7]);
                bw.append(",");
                bw.append(record[8]);
                bw.append(",");
                bw.append(record[9]);                
                bw.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(ISDStatTendCorrRegrPTePerSeason.class.getName()).log(Level.SEVERE, null, ex);
        }     
    }
}
