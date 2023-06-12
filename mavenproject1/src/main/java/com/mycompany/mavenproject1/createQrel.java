/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mavenproject1;
import static com.mycompany.mavenproject1.Main.getSLRs;
import static com.mycompany.mavenproject1.Main.refFileToDOIs;
import static com.mycompany.mavenproject1.Main.initialize;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 *
 * @author ethan
 */
public class createQrel {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       ArrayList<SLR> slrs = initialize();
        // System.out.println(slrs.get(2).references.get(3));
        int k = 2;

        File myFile;

        //Reference r = s.references.get(0);
        //  System.out.println(r.toJson());
        int topic = 1;
        int iteration = 0;
        int relevancy = 1;
        for (k = 2; k < slrs.size(); k++) {
            SLR s = slrs.get(k);
            for(Reference r: s.references){
                System.out.printf("%4d %4d %50s %4d\n", topic, iteration, r.doi, relevancy);
            }
        }
    }
    
}
