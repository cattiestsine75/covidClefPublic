/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mavenproject1;

import static com.mycompany.mavenproject1.Main.initialize;
import java.util.ArrayList;
import static com.mycompany.mavenproject1.Main.getHTML;
import static com.mycompany.mavenproject1.Main.grabTag;
import java.io.File;
import java.io.FileWriter;
//problems: 24, 34, 40, 41, 54, 55, 57, 62, 68, 76, 80, 84, 87, 88, 92, 94, 99, 100, 101, 

/**
 *
 * @author ethan
 */
public class Subtasks {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        ArrayList<SLR> slrs = initialize();
        int minFTRefs = 999;
        int FTforSLR = 0;
        int slrNum = 0;
        int curlow = -1;
        ArrayList<Integer> retrievals = new ArrayList<>();

        for (int i = 2; i < slrs.size(); i++) {
            File f = new File("C:\\Users\\ethan\\Desktop\\2023USRAResearch\\CovidClef2023\\covidClef2023\\Covid_19_Dataset_and_References\\AdditionalFiles\\citationGraph\\" + i + ".txt");
            try {
                FileWriter writer = new FileWriter(f);
                SLR s = slrs.get(i);
                ArrayList<String> titleList = new ArrayList<>();
                ArrayList<String> Depth2 = new ArrayList<>();
                String output = ("{\"SLR NAME\":\"" + s.name + "\",\"References\":[");
                int numRefsComplete = 0;
                int index = 0;
                System.out.println("Starting work on SLR " + i);
                if (s.references != null) {
                    while (numRefsComplete < 5 && index + 1 < s.references.size()) {
                        System.out.printf("Working on document %d of 5 in SLR %d\n", (numRefsComplete + 1), i);
                        Reference r = s.references.get(index);
                        if (r.idFormat.equals("PMC")) {
                            System.out.println(r.title);
                            titleList = genCitations(r.id);
                            if (titleList.size() > 0) {
                                numRefsComplete++;
                                output = output + "{\"title1\":\"" + r.title + "\",\"References\":[";

                                for (String title : titleList) {
                                    output = output + "{\"title2\":\"" + title + "\",\"References\":[";
                                    System.out.println("\t" + title);
                                    title = title.replace(" ", "%20");
                                    String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&retmode=xml&retmax=1&term=" + title + "&field=title";
                                    String pmcID = (grabTag(getHTML(url), "Id", false));
                                    Depth2 = genCitations(pmcID);
                                    for (String x : Depth2) {
                                        output = output + "{\"title3\":\"" + x + "\"},";
                                        System.out.println("\t\t" + x);
                                    }
                                    if (output.charAt(output.length() - 1) == ',') {
                                        output = output.substring(0, output.length() - 1);
                                    }
                                    output = output + "]},";

                                }
                                if (output.charAt(output.length() - 1) == ',') {
                                    output = output.substring(0, output.length() - 1);
                                }
                                output = output + "]},";

                            }
                        }
                        index++;
                    }
                }
                if (output.charAt(output.length() - 1) == ',') {
                    output = output.substring(0, output.length() - 1);
                }
                output = output + "]}";
                writer.write(output);
                writer.close();
                System.out.println("\n\nDONE SLR " + i + " \n\n");
            } catch (Exception e) {
                System.out.println("ERROR: \n" + e);
            }

            //grab 3, 5 random articles, citation graph depth of 2, and citation network.
        }
    }

    public static ArrayList<String> genCitations(String pmcID) {
        ArrayList<String> output = new ArrayList<>();
        String in = getHTML("https://www.ncbi.nlm.nih.gov/research/bionlp/RESTful/pmcoa.cgi/BioC_xml/" + pmcID + "/ascii?pretty");
        //  System.out.println(in);
        //  System.out.println("\n\n\n\n");
        String srch = "References";
        //System.out.println(in.indexOf(srch) + srch.length() + 1);
        in = in.substring(in.indexOf(srch) + srch.length() + 1);
        // in = in.substring(in.indexOf(srch) + srch.length() + 1);

        //System.out.println(in);
        srch = "<passage>";
        while (in.indexOf(srch) != -1) {
            String out = grabTag(in, "passage", false);
            if (out.contains(">ref<")) {
                String title = (grabTag(out, "text", false));
                if (!title.equals("NULL")) {
                    output.add(title);
                    title.replace(" ", "%20");
                    String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&retmode=xml&retmax=1&term=" + title + "&field=title";
                }
            }
            in = in.substring(in.indexOf(srch) + srch.length() + 1);

        }
        return (output);
    }

}
