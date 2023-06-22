/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mavenproject1;

import static com.mycompany.mavenproject1.Main.getSLRs;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author ethan
 */
public class ProvideMoreSLRInfo {

    static ArrayList<String> strList;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ArrayList<SLR> slrs;
        slrs = getSLRs();
        System.out.println(slrs.get(21).abs);
    }

    public static String getHTML(String urlToRead) {
        String result = "error: Some failure in getHTML line\n" + urlToRead;
        try {
            URL url = new URL(urlToRead);
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            result = IOUtils.toString(is, StandardCharsets.UTF_8);

        } catch (Exception e) {
            System.out.println(e);
        }
        return result;
    }

    public static String BioC_PMC(String format, String ID, String encoding) {
        String base = "https://www.ncbi.nlm.nih.gov/research/bionlp/RESTful/pmcoa.cgi/BioC_";

        encoding = encoding.toLowerCase();
        String url = base + format + "/" + ID + "/" + encoding;
        return getHTML(url);

    }

    public static String ESearch(String db, String query, boolean exact, String date) {
        String base = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";

        if (exact) {
            // getHTML()
        } else {
            //parse the string query manually.

            // System.out.println(query);
        }
        query = query.replaceAll(" ", "%20");
        query = query.replaceAll("\n", "");
        String url = base + "esearch.fcgi?db=" + db + "&term=\"" + query + "\"&usehistory=y&retmax=1&sort=relevance";
        query = query.replaceAll("%5B", "[");
        query = query.replaceAll("%5D", "]");
        query = query.replaceAll("%20", " ");
        query = query.replaceAll("\\+", " ");
        System.out.println("Query:\n" + query);
        System.out.println("\n\n" + url + "\n\n");
        //System.out.println("\n\nTHEURL:" + url + "\n\n");
        //System.out.println(getHTML(url));
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            System.out.println(e);
        }
        return getHTML(url);
    }

    public static String SearchToIds(String input) {
        String id;

        String output = "";
        String added = "";
        int curIndex = 0;
        while (input.indexOf("</Id>") != -1) {
            curIndex = input.indexOf("</Id>") + 5; //9 = length of <Id> + length of </Id>
            // System.out.println("curIndex: " + curIndex);
            added = grabTag(input, "Id", false);

            output = output + added + ",";
            // System.out.println(grabTag(input, "Id"));
            if (curIndex <= input.length()) {
                input = input.substring(curIndex);
            } else {

                curIndex = -1;
            }
            //System.out.println(" input after grabbing tag " + input + "    \n");
        }
        if (output.length() > 0) {
            output = output.substring(0, output.length() - 1);

        }
        return output;

    }

    public static String grabTag(String input, String tag, boolean exact) {
        return grabTag(input, tag, tag, exact);
    }

    public static String grabTag(String input, String opentag, String closetag, boolean exact) {
        if (!exact) {
            opentag = "<" + opentag + ">";
            closetag = "</" + closetag + ">";
        }
        int x = input.indexOf(opentag);
        int y = input.indexOf((closetag), x + opentag.length());
        String output = "NULL";
        if (x != -1 && y != -1) {
            output = input.substring(x + (opentag).length(), y);
        } else {
        }
        if (x == -1 || y == -1) {
        }
        return output;
    }

    public static String PMCtoDOI(String pmc) {
        String base = "https://www.ncbi.nlm.nih.gov/pmc/utils/idconv/v1.0/";
        String in = getHTML(base + "?ids=" + pmc);
        // System.out.println(in);
        return grabTag(in, "doi=\"", "\"", true);
    }

    public static String doiToPMC(String doi) {
        String base = "https://www.ncbi.nlm.nih.gov/pmc/utils/idconv/v1.0/";
        String in = getHTML(base + "?ids=" + doi);
        return grabTag(in, "pmcid=\"", "\"", true);
    }

}
