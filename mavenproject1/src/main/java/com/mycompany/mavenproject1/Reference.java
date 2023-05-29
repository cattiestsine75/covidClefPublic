/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mavenproject1;

import static com.mycompany.mavenproject1.Main.getHTML;
import static com.mycompany.mavenproject1.Main.grabTag;
import static com.mycompany.mavenproject1.Main.removeLike;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.time.LocalDate;

/**
 * A reference of a given SLR.
 *
 * @author ethan
 */
public class Reference {

    public static int total = 0; //how many total document references there are
    public static int found = 0; //how many documents were successfully found.
    public static int notFound = 0; //how many documents were not found
    String doi; //the doi, in format 10.PREFIX/SUFFIX
    boolean hasBeenFound;
    String title; //the title of the referenced work
    String Abstract; //the abstract of the referenced work.
    String id; // the ID of the Referenced work
    String idFormat; //the Format/Database type for which the ID (see above) is relevant.
    String foundApis;
    ArrayList<Author> authors; //Arraylist of Author objects.

    LocalDate dateAccepted; //date accepted.

    /**
     * creates a 'blank slate' reference.
     */
    public Reference() {

        this.doi = "UnknownDOI";
        this.hasBeenFound = false;
        this.id = "not found";
        this.idFormat = "N/A";
        this.title = "Unknown Title";
        this.Abstract = "Unknown Abstract";
        this.foundApis = "";

        authors = new ArrayList<Author>();
        dateAccepted = LocalDate.EPOCH;

    }

    /**
     * Returns the Reference in string format
     *
     * @return DOI, Date accepted, ID, IDFormat, Title, Abstract, and Authors
     */
    public String toString() {
        return "DOI: " + this.doi + ". Date Accepted: " + this.dateAccepted + " ID: " + this.id + " OF FORMAT: " + this.idFormat + "\nTitle:" + this.title + "\nAbstract:{" + this.Abstract + "}ABSTRACT END\nAuthors:" + this.authors;
    }

    /**
     * Populates the Reference with all relevant information based off of what
     * is provided in the input.
     *
     * @param in The input, In text, of a GetRecord OAI PMC API request for a
     * given document.
     */
    public void populate(String in) {

        // System.out.println("ATTEMPTING TO POPULATE");
        if (in.indexOf("error code=\"cannotDisseminateFormat\"") == -1) { //if we have found it
            System.out.println("PMC DOCUMENT FOUND");
            if (!this.hasBeenFound) {
                this.idFormat = "PMC";
                System.out.println("FILLING DOCMENT WITH PMC");
                found++;
                this.hasBeenFound = true;
                String title = grabTag(in, "article-title", false);
                int bad = title.indexOf("<sup");
                if (bad == -1) {
                    bad = 99999999;
                }
                bad = Math.min(bad, title.indexOf("<xref"));
                if (bad > 15) {
                    title = title.substring(0, bad + 1);
                }
                //System.out.println(title);
                this.title = title;                                                                                         //where we add the title

                //AUTHOR RELATED STUFF
                String autIn = in;
                while (autIn.indexOf("<contrib") != -1) {
                    //System.out.println("AUTHOR PROBLEM");
                    String authorBlock = grabTag(autIn, "<contrib", "</contrib>", true);
                    // System.out.println(authorBlock);
                    String surname = grabTag(authorBlock, "surname", false);
                    String firstname = grabTag(authorBlock, "given-names", false);
                    String email = grabTag(authorBlock, "email", false);
                    this.authors.add(new Author(firstname, surname, email));                                                   //where we add the authors
                    // System.out.println(firstname + " " + surname + " " + email);
                    autIn = autIn.substring(autIn.indexOf("</contrib") + 1);
                }

                //DATE RELATED STUFF:
                String dateIn = grabTag(in, "\"accepted\"", "</date>", true);
                LocalDate date = LocalDate.EPOCH;
                if (!dateIn.equals("NULL")) {
                    //System.out.println(dateIn);

                    int day = 1;
                    int month = 1;
                    int year = 1970;
                    if (dateIn.contains("day")) {
                        day = Integer.parseInt(grabTag(dateIn, "day", false));
                    }
                    if (dateIn.contains("month")) {
                        month = Integer.parseInt(grabTag(dateIn, "month", false));
                    }
                    if (dateIn.contains("year")) {
                        year = Integer.parseInt(grabTag(dateIn, "year", false));
                    }
                    date = LocalDate.of(year, month, day);
                }
                this.dateAccepted = date;

                //ABSTRACT RELATED STUFF:
                String absIn = grabTag(in, "<abstract", "</abstract>", true);
                this.Abstract = "";

                if (!absIn.contains("<sec")) {
                    if (absIn.contains("<p")) {
                        this.Abstract = grabTag(absIn, "<p", "</p", true);
                        this.Abstract = this.Abstract.substring(1);
                        formatAbstract();
                    }
                }
                while (absIn.contains("<sec")) {

                    String abstractBlock = grabTag(absIn, "<sec", "</sec>", true);
                    String tit = grabTag(abstractBlock, "title", false);
                    String info = grabTag(abstractBlock, "<p", "</p", true);
                    info = info.substring(1);
                    for (int i = 0; i < info.length(); i++) {
                        //System.out.print(in.charAt(i));
                        if (info.charAt(i) == '.' && !Character.isDigit(info.charAt(i - 1))) {
                            info = info.substring(0, i + 1) + '\n' + info.substring(i + 1);
                        }
                    }

                    this.Abstract = this.Abstract + tit + "\n" + info + "\n";                                        //where we add to the abstract
                    absIn = absIn.substring(absIn.indexOf("<sec") + 1);
                }
                formatAbstract();
            } else {
                //   System.out.println("ADDING TO FILLED APIS" + " ID:" + this.id);
                if (!this.foundApis.contains("PMC")) {
                    this.foundApis = this.foundApis + "_PMC";
                }
            }
        } else {
            this.id = "not found";
            this.idFormat = "N/A";
        }
        //  }
    }

    /**
     * Populates a reference object given an elsevier key. This requires that
     * the reference has a doi value beginning with '10.XX' This api searches
     * the Scopus, Embase, ScienceDirect, and EngineeringVillage databases
     * hosted by elsevier.
     *
     * @param key Your personalized Elsevier key.
     */
    public void populateElsevier(String key) {
        if (this.doi.indexOf("10.") == 0) { //if this doi is valid.

            String url = "https://api.elsevier.com/content/article/doi/" + this.doi
                    + "?APIKey=" + key
                    + "&httpAccept=text/xml";
            String in = getHTML(url);
            if (in.indexOf("<service-error>") == -1 && in.indexOf("error: Some failure in") == -1) { //if there's a 
                System.out.println("Elsevier DOCUMENT FOUND");
                if (this.id.equals("not found")) { //if this document hasn't been found.
                    System.out.println("FILLING ELSEVIER DOCUMENT");
                    this.idFormat = "elsevier_pii";
                    found++;
                    this.hasBeenFound = true;
                    String coreData = grabTag(in, "coredata", false);
                    this.id = grabTag(coreData, "pii", false);
                    String title = grabTag(coreData, "dc:title", false);
                    String Abstract = grabTag(coreData, "dc:description", false);
                    String date = grabTag(coreData, "<prism:coverDate>", "</prism:coverDate>", true);
                    LocalDate theDate = LocalDate.EPOCH;
                    if (!date.equals("NULL")) {
                        theDate = LocalDate.parse(date);
                    }
                    this.dateAccepted = theDate;
                    this.Abstract = Abstract;
                    this.title = title;
                    // System.out.println(theDate);
                    // System.out.println(title);
                    //   System.out.println(Abstract);

                    String authorsData = coreData;
                    int x = 0;
                    while (authorsData.indexOf("<dc:creator>", x + 1) != -1) {
                        x = authorsData.indexOf("<dc:creator>", x + 1) + "<dc:creator>".length();
                        String authorStuff = authorsData.substring(x, authorsData.indexOf("<", x + 1));
                        StringTokenizer tk = new StringTokenizer(authorStuff, ",");
                        String lastname = tk.nextToken();
                        String firstname = tk.nextToken();

                        this.authors.add(new Author(firstname, lastname, null));

                    }
                } else {
                    //   System.out.println("ADDING TO FILLED APIS");
                    if (!this.foundApis.contains("elsevier")) {
                        this.foundApis = this.foundApis + "_elsevier";
                    }
                }
                // System.out.println(in);

            }
        }
    }

    /**
     * Populates a reference object given a SpringerAPI key. This requires that
     * the reference has a doi value beginning with '10.XX'
     *
     * @param key Your personal Springer API key. For more information, see
     * https://dev.springernature.com/
     *
     */
    public void populateSpringer(String key) {
        if (this.doi.indexOf("10.") == 0) {
            String url = "https://api.springernature.com/metadata/pam?q=doi:" + this.doi
                    + "&api_key=" + key;

            String in = (getHTML(url));
            if (grabTag(in, "total", false).equals("1")) { //if we've found it
                if (this.id.equals("not found")) { //and the document hasn't been filled
                    found++; //fill it
                    this.hasBeenFound = true;
                    this.id = this.doi;
                    this.idFormat = "Springer";

                    if (in.indexOf("Abstract") != -1) {
                        String x = in.substring(in.indexOf("Abstract"));
                        x = x.substring(0, x.indexOf("</xhtml:body>"));
                        String abs = "";
                        while (x.contains("<p>")) {
                            String tag = (grabTag(x, "<p", "</p", true));
                            tag = tag.substring(tag.indexOf(">") + 1);
                            abs = abs + "\n" + tag;
                            x = x.substring(x.indexOf("</p") + 1);
                        }
                        this.Abstract = abs;
                        this.formatAbstract();
                    }
                    String date = grabTag(in, "<prism:publicationDate>", "</", true);
                    String authorsData = in;
                    String title = (grabTag(in, "<dc:title>", "</dc:title>", true));
                    this.title = title;

                    if (!date.equals("NULL")) {
                        this.dateAccepted = LocalDate.parse(date);
                    }

                    int x = 0;
                    while (authorsData.indexOf("<dc:creator>", x + 1) != -1) {
                        x = authorsData.indexOf("<dc:creator>", x + 1) + "<dc:creator>".length();
                        String authorStuff = authorsData.substring(x, authorsData.indexOf("<", x + 1));
                        StringTokenizer tk = new StringTokenizer(authorStuff, ",");
                        String lastname = tk.nextToken();
                        String firstname = tk.nextToken();
                        // System.out.println(firstname + " " + lastname);
                        this.authors.add(new Author(firstname, lastname, null));

                    }
                } else { //if the document HAS been filled already
                    if (!this.foundApis.contains("Springer")) {
                        this.foundApis = this.foundApis + "_Springer";
                    }
                }
            }
        }
    }

    /**
     * Populates a reference object given a CoreAPI key. This requires that the
     * reference has a doi value beginning with '10.XX'
     *
     * @param key Your personal CoreAPI key. For more information, see
     * https://core.ac.uk/services/api
     */
    public void populateCore(String key) {
        //use of this method is generally unfavourable due to the rate limits imposed. 
        if (this.doi.indexOf("10.") == 0) {
            String url = "https://api.core.ac.uk/v3/search/works/?q=doi:" + this.doi + "&api_key=" + key;
            String in = getHTML(url);
            String th = grabTag(in, "\"totalHits\":", ",", true);
            if (!th.equals("0")) { //if we found it
                if (this.id.equals("not found")) { //and the document hasn't been found
                    System.out.println("ADDING CORE INFO TO DOC");
                    found++;
                    this.hasBeenFound = true;
                    this.id = this.doi;
                    this.idFormat = "CORE";
                    int x = 0;
                    if (in.contains("\"authors\":[")) {
                        String authorsData = grabTag(in, "\"authors\":[", "]", true);
                        // System.out.println(authorsData);
                        authorsData = removeLike(authorsData, ",", 1);
                        // System.out.println("\n\nAFTER REMOVAL:\n" + authorsData);

                        while (authorsData.indexOf("\"name\":\"", x + 1) != -1) {
                            x = authorsData.indexOf("\"name\":\"", x + 1) + "\"name\":\"".length();
                            String authorStuff = authorsData.substring(x, authorsData.indexOf("\"", x + 1));

                            if (authorStuff.indexOf(" ") != -1) {
                                String fn = authorStuff.substring(0, authorStuff.indexOf(" "));
                                String ln = authorStuff.substring(authorStuff.indexOf(" ") + 1);
                                this.authors.add(new Author(fn, ln, "coreGivesNoEmail"));

                            } else {
                                //add it regardless?
                            }
                        }
                    }
                    if (in.contains("\"title\":\"")) {
                        String title = grabTag(in, "\"title\":\"", "\"", true);
                        this.title = title;

                    }
                    if (in.contains("\"abstract\":\"")) {
                        String Abstract = grabTag(in, "\"abstract\":\"", "\"", true);
                        for (int i = 0; i < Abstract.length(); i++) {
                            if (Abstract.charAt(i) == '.' && !Character.isDigit(Abstract.charAt(i - 1))) {
                                Abstract = Abstract.substring(0, i + 1) + '\n' + Abstract.substring(i + 1);
                            }
                        }
                        this.Abstract = Abstract;

                    }
                    try {
                        Thread.sleep(2000); //this is done to counter rate limiting. That being said, It's not perfect.
                        //At it's worst, if core has received no calls, and then receieves the current rate limit (150) then this adds an additional 5 minutes to runtime.
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                } else { //the document has been found
                    if (!this.foundApis.contains("Core")) {
                        this.foundApis = this.foundApis + "_Core";
                    }
                }
            }
        }

    }

    /**
     * Populates a reference object using the Medrxiv api. This requires that
     * the reference has a doi value beginning with '10.XX'
     *
     *
     */
    public void populateMedrxiv() {

        String base = "https://api.medrxiv.org/details/medrxiv/"
                + this.doi + "/na/xml";
        String in = (getHTML(base));
        if (!in.contains("<record")) {
            in = getHTML("https://api.biorxiv.org/details/biorxiv/" + this.doi + "/na/xml");//if we don't have record, check biorxiv to see if it's there
        }
        if (in.contains("<record")) { //if we found it.
            if (!this.id.equals("not found")) { //and the document is not found
                found++;
                this.id = this.doi;
                this.idFormat = "medrxiv/biorxiv doi";
                this.hasBeenFound = true;
                in = in.substring(in.lastIndexOf("<record")); //get the latest versioned record.
                String Abstract = "";
                if (in.contains("<abstract")) {
                    Abstract = grabTag(in, "<abstract", "</abstract", true);
                    Abstract = grabTag(Abstract, "<![CDATA[", "]]>", true);
                }
                this.Abstract = Abstract;
                String tBlock = grabTag(in, "<title", "</title", true);
                String Title = grabTag(tBlock, "<![CDATA[", "]]>", true);
                this.title = Title;
                System.out.println(Title);
                String date = grabTag(in, "<date>", "</date", true);

                this.dateAccepted = LocalDate.parse(date);

                String aBlock = grabTag(in, "<authors>", "</authors", true);
                aBlock = grabTag(aBlock, "<![CDATA[", "]]>", true);

                StringTokenizer tk = new StringTokenizer(aBlock, ";");
                while (tk.hasMoreTokens()) {
                    String fn = "couldn't find firstname";
                    String ln = "couldn't find lastname";
                    StringTokenizer tk2 = new StringTokenizer(tk.nextToken(), ",");
                    if (tk2.hasMoreTokens()) {
                        ln = tk2.nextToken();
                    }
                    if (tk2.hasMoreTokens()) {
                        fn = tk2.nextToken();
                    }
                    Author a = new Author(fn, ln, null);
                    this.authors.add(a);
                }

            } else {
                if (!this.foundApis.contains("MedBiorxiv")) {
                    this.foundApis = this.foundApis + "_MedBiorxiv";
                }
            }
        }
    }

    /**
     * Populates a reference object using the Crossref api. This requires that
     * the reference has a doi value beginning with '10.XX'
     *
     *
     */
    public void populateCrossref() {
        if (this.doi.indexOf("10.") == 0) {
            String url = "https://api.crossref.org/works/" + this.doi;
            String in = getHTML(url);
            if (!in.contains("error: Some failure in getHTML line")) {//if we found it

                if (this.id.equals("not found")) {
                    this.idFormat = "CROSSREF";
                    this.id = this.doi;
                    found++;
                    this.hasBeenFound = true;
                    String abs = "";
                    String title = "";
                    String auths = "";
                    String date = "";
                    String srch = "abstract\":";
                    if (in.contains(srch)) {
                        int x = in.indexOf(srch) + srch.length();
                        abs = in.substring(x, in.indexOf("\",", x));
                        this.Abstract = abs;
                        //  System.out.println(abs);
                    }
                    srch = "\"title\":[";
                    if (in.contains(srch)) {
                        int x = in.indexOf(srch) + srch.length();
                        title = in.substring(x, in.indexOf("],", x));
                        this.title = title;
                        //  System.out.println(title);
                    }
                    srch = "\"author\":[";
                    if (in.contains(srch)) {
                        int x = 0;

                        x = in.indexOf(srch, x) + srch.length();
                        auths = in.substring(x, in.indexOf("],", x));
                        x = 0;
                        srch = "\"given\":\"";
                        while (auths.indexOf(srch, x) != -1) {
                            srch = "\"given\":\"";
                            int y = auths.indexOf(srch, x) + srch.length();
                            String fn = auths.substring(y, auths.indexOf("\"", y));
                            srch = "\"family\":\"";
                            y = auths.indexOf(srch, y) + srch.length();
                            String ln = auths.substring(y, auths.indexOf("\"", y));
                            // System.out.println(fn + ", " + ln);
                            this.authors.add(new Author(fn, ln, "xref no email"));
                            x = y + 1;

                        }
                        // System.out.println(auths);
                    }
                    srch = "\"date-time\":\"";
                    if (in.contains(srch)) {
                        int x = in.indexOf(srch) + srch.length();
                        date = in.substring(x, in.indexOf("\",", x));
                        if (Character.isDigit(date.charAt(0)) && Character.isLetter(date.charAt(10))) {
                            date = date.substring(0, 10);
                            this.dateAccepted = LocalDate.parse(date);
                        }

                    }
                    formatAbstract();
                } else {
                    if (!this.foundApis.contains("CROSSREF")) {
                        this.foundApis = this.foundApis + "_CROSSREF";
                    }
                }
            }
        }
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Reformats the abstract, adding newlines after sentences being ended, and
     * removing any possible tags.
     */
    public void formatAbstract() {
        for (int i = 0; i < Abstract.length(); i++) {
            if (Abstract.charAt(i) == '.' && !Character.isDigit(Abstract.charAt(i - 1))) {
                Abstract = Abstract.substring(0, i + 1) + '\n' + Abstract.substring(i + 1);
            }
        }

        while (Abstract.contains("<")) {

            //  System.out.println(Abstract);
            int x = Abstract.indexOf("<");
            int y = Abstract.indexOf(">", x) + 1;
            if (x != -1 && y != 0) {
                Abstract = Abstract.substring(0, x) + Abstract.substring(y);
            }
        }
        
       

    }
    public void removeParIdTag(){
         String srch = "id=\"Par";
        while(Abstract.contains(srch)){
            int loc = Abstract.indexOf(srch) +srch.length();
            while(Character.isDigit(Abstract.charAt(loc))){
              
                loc++;
            }
            loc = loc+ 2;
            int y= Abstract.indexOf(srch);
            Abstract = Abstract.substring(0, y) + Abstract.substring(loc);
        }
    }

    public void clear() {
       // this.doi = "UnknownDOI";
        this.hasBeenFound = false;
        this.id = "not found";
        this.idFormat = "N/A";
        this.title = "Unknown Title";
        this.Abstract = "Unknown Abstract";
        this.foundApis = "";

        authors = new ArrayList<Author>();
        dateAccepted = LocalDate.EPOCH;
    }
}
