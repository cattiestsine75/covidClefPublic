/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mavenproject1;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//72 is dead
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.commons.io.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.time.LocalDate;
import java.time.Month;
import java.util.StringTokenizer;

/**
 *
 * @author ethan zelmer email: ethan.zelmer@torontomu.ca
 */
public class Main {

    public static String ElsevierApiKey = "";
    public static String SpringerApiKey = "";
    public static String CoreApiKey = "";
    public static int searchOffset = 3;// starts at 0
    public static int searchAmt = Math.min(searchOffset + 3, 113);//ends at 5
    //CORE OFFSET 110, CORE MISSED 6
    //doc 12 of SLR 53

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            Scanner coreIn = new Scanner(new File("C:\\Users\\ethan\\Desktop\\2023USRAResearch\\CovidClef2023\\keys\\CoreApiKey.txt"));
            CoreApiKey = coreIn.nextLine();
            Scanner elsIn = new Scanner(new File("C:\\Users\\ethan\\Desktop\\2023USRAResearch\\CovidClef2023\\keys\\ElsevierApiKey.txt"));
            ElsevierApiKey = elsIn.nextLine();
            Scanner springerIn = new Scanner(new File("C:\\Users\\ethan\\Desktop\\2023USRAResearch\\CovidClef2023\\keys\\SpringerApiKey.txt"));
            SpringerApiKey = springerIn.nextLine();
        } catch (FileNotFoundException f) {
            System.out.println("ERROR READING APIS:\n" + f);
        }
        /*
        keep track of where references can be found, and where they aren't, even if it's already been found.
        more biomed datasets
        all information available in excel
         */
        // TODO code application logic here

        ArrayList<SLR> slrs = initialize();
        //   pmcPopulate(slrs, searchAmt, searchOffset); //PMC gets 74 in 45 sec
        // elsevierPopulate(slrs, searchAmt, searchOffset); //ELSEVIER gets 20 in 29.8 sec
        //springerPopulate(slrs, searchAmt, searchOffset); //springer gets 10, 50 sec
        //  medxrivPopulate(slrs, searchAmt, searchOffset);
        //  corePopulate(slrs, searchAmt, searchOffset);

       // int i = 0;
       // int j = 0;
        int ct = 0;
       

        for (int i = 2; i < slrs.size(); i++) {  //specific slr id. Exact.
            for (int j = 0; j < slrs.get(i).references.size(); j++) { //row of Spreadsheet. For specific rowid, take j + 1
                Reference r = slrs.get(i).references.get(j);
                if (r.Abstract.contains("id=\"Par")) {
                    System.out.println("\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    System.out.println("SLR:" + i + " REF: " + j + " ABS: " + r.Abstract + "\n");
                 //   r.Abstract = removeLike(r.Abstract,"id=\"Par" );
                 //   r.dumpData(j+1, i);
                    ct++;
                }
            }
        }
        
        
        
        System.out.println("COUNT: " + ct);
        
        
        
        
        System.out.println("\n\n");
        for (int k = 2 + searchOffset; k < searchAmt; k++) {
            //   slrs.get(k).dumpData(k);
        }
       // System.out.println(slrs.get(2).references.get(0).doi);
//                                  same               +1
        //slrs.get(2).references.get(0).idFormat = "test";
       // slrs.get(2).references.get(0).dumpData(1, 2);

        
        
      
        
        
         
        System.out.println("\n\nDONE WITH THAT\n\n");
        System.out.println("{" + Reference.found + "}" + "out of " + Reference.total);
        System.out.println(searchOffset);

    }
    
    
    public static String removeLike(String txt, String srch){
        
        int x = txt.indexOf(srch) + srch.length();
        //System.out.println(txt.substring(x));
        int ct = 0;
        while (Character.isDigit(txt.charAt(x + ct))) {
            ct++;
        }
       // System.out.println(ct);
     //   System.out.println(txt.substring(0, x - srch.length()) + txt.substring(x + ct + 2));
      //  System.out.println(Character.isDigit(txt.charAt(x + ct)));
        txt = (txt.substring(0, x - srch.length()) + txt.substring(x + ct + 2));
        
        return txt;
        
         
    }

    /**
     * reads SLR document file and creates both SLRs and references accordingly.
     * If data is already present in the slr file, the document is added, and
     * relevant fields are filled in.
     *
     * @return arraylist of SLRS, with their respective arraylists of
     * references. Contains information such as their DOI.
     */
    public static ArrayList<SLR> initialize() {
        ArrayList<SLR> slrs = new ArrayList<SLR>();
        slrs = getSLRs();
        for (int j = 2; j < slrs.size(); j++) {
            System.out.println("creating reference object " + j + " of " + slrs.size());
            slrs.get(j).references = refFileToDOIs(j);
        }
        return slrs;
    }

    /**
     * Populates the current list of SLRs utilizing the PMC database including:
     * Pubmed, PMC
     *
     * @param slrs arraylist of SLRS you wish to populate
     * @param k quantity of documents you wish to populate
     */
    public static void pmcPopulate(ArrayList<SLR> slrs, int k, int offset) {
        for (int i = 2 + offset; i < k; i++) {
            System.out.println("Searching for PMCIDs of documents in SLR" + i + " of SLR" + k);
            searchPMCForRefs(slrs, i);
            for (int j = 0; j < slrs.get(i).references.size(); j++) {
                if (slrs.get(i).references.get(j).idFormat.equals("PMC") && !slrs.get(i).references.get(j).hasBeenFound) {
                    System.out.println("Searching PMC for document " + j + " of SLR " + i);
                    String pmcid = slrs.get(i).references.get(j).id.substring(3);
                    if (pmcid.contains(".")) {
                        pmcid = pmcid.substring(0, pmcid.indexOf("."));
                    }
                    String base = "https://www.ncbi.nlm.nih.gov/pmc/oai/oai.cgi?verb=GetRecord&identifier=oai:pubmedcentral.nih.gov:" + pmcid
                            + "&metadataPrefix=pmc";
                    String in = getHTML(base);
                    slrs.get(i).references.get(j).populate(in);//populate
                }
            }
        }
    }

    /**
     * Populates the SLRs arraylist's of References using the Elsevier
     * database.This includes Scopus, ScienceDirect, SciVal, Engineering
     * Village, Embase, Reaxys, PharmaPendium.
     *
     * @param slrs ArrayList of SLR objects. It is assumed that this arraylist
     * was created using the Initialize code to ensure that they are created
     * properly.
     * @param offset the offset used to determine where to start querying.
     * @see initialize
     * @param k the last index of the SLR that should be populated. This is done
     * to prevent excessive compile times, as well as adhere to rate limits from
     * various api providers.
     */
    public static void elsevierPopulate(ArrayList<SLR> slrs, int k, int offset) {
        for (int i = 2 + offset; i < k; i++) {
            for (int j = 0; j < slrs.get(i).references.size(); j++) {
                if (!slrs.get(i).references.get(j).hasBeenFound) {
                    System.out.println("Searching Elsevier for document " + j + " of SLR " + i);
                    slrs.get(i).references.get(j).populateElsevier(ElsevierApiKey);
                }
            }
        }
    }

    /**
     * Populates the SLRs arraylist's of References using the Elsevier
     * database.This includes Scopus, ScienceDirect, SciVal, Engineering
     * Village, Embase, Reaxys, PharmaPendium.
     *
     * @param slrs
     * @param k
     * @param offset
     */
    public static void medxrivPopulate(ArrayList<SLR> slrs, int k, int offset) {
        for (int i = 2 + offset; i < k; i++) {
            for (int j = 0; j < slrs.get(i).references.size(); j++) {
                if (!slrs.get(i).references.get(j).hasBeenFound) {
                    System.out.println("Searching MEDXRIV for document " + j + " of SLR " + i);
                    slrs.get(i).references.get(j).populateMedrxiv();
                }
            }
        }
    }

    /**
     * Populates the SLRs arraylist's of References using the CORE database.
     *
     * @param slrs ArrayList of SLR objects. It is assumed that this arraylist
     * was created using the Initialize code to ensure that they are created
     * properly.
     * @see initialize
     * @param k the last index of the SLR that should be populated. This is done
     * to prevent excessive compile times, as well as adhere to rate limits from
     * various api providers.
     */
    public static void corePopulate(ArrayList<SLR> slrs, int k, int offset) {
        for (int i = 2 + offset; i < k; i++) {
            for (int j = 0; j < slrs.get(i).references.size(); j++) {
                if (!slrs.get(i).references.get(j).hasBeenFound) {
                    System.out.println("Searching Core for document " + j + " of SLR " + i);
                    slrs.get(i).references.get(j).populateCore(CoreApiKey);
                }
            }
        }
    }

    /**
     * Populates the SLRs arraylist's of References using the Springer database.
     *
     * @param slrs ArrayList of SLR objects. It is assumed that this arraylist
     * was created using the Initialize code to ensure that they are created
     * properly.
     * @see initialize
     * @param k the last index of the SLR that should be populated. This is done
     * to prevent excessive compile times, as well as adhere to rate limits from
     * various api providers.
     */
    public static void springerPopulate(ArrayList<SLR> slrs, int k, int offset) {
        for (int i = 2 + offset; i < k; i++) {
            for (int j = 0; j < slrs.get(i).references.size(); j++) {
                if (!slrs.get(i).references.get(j).hasBeenFound) {
                    System.out.println("Searching Springer for document " + j + " of SLR " + i);
                    slrs.get(i).references.get(j).populateSpringer(SpringerApiKey);
                }
            }
        }
    }

    /**
     * Populates the SLRs arraylist's of References using the MedXRIV and
     * BioXRIV database.
     *
     * @param slrs ArrayList of SLR objects. It is assumed that this arraylist
     * was created using the Initialize code to ensure that they are created
     * properly.
     * @see initialize
     */
    public static void medxrivPopulate(ArrayList<SLR> slrs) {
        medxrivPopulate(slrs, slrs.size(), 0);
    }

    /**
     * Populates the SLRs arraylist's of References using the Springer database.
     *
     * @param slrs ArrayList of SLR objects. It is assumed that this arraylist
     * was created using the Initialize code to ensure that they are created
     * properly.
     * @see initialize
     */
    public static void springerPopulate(ArrayList<SLR> slrs) {
        springerPopulate(slrs, slrs.size(), 0);
    }

    /**
     * Populates the SLRs arraylist's of References using the CORE database.
     *
     * @param slrs ArrayList of SLR objects. It is assumed that this arraylist
     * was created using the Initialize code to ensure that they are created
     * properly.
     * @see initialize
     */
    public static void corePopulate(ArrayList<SLR> slrs) {
        corePopulate(slrs, slrs.size(), 0);
    }

    /**
     * Populates all SLRS using PMC. Hopefully will be revised to behave
     * similarly to the other servicePopulate methods.
     *
     * @param slrs list of slrs to be populated.
     */
    public static void pmcPopulate(ArrayList<SLR> slrs) {
        pmcPopulate(slrs, slrs.size(), 0);
    }

    /**
     * Utility method used by various other methods. Example "HellBADDATAo"
     * removeLike("HellBADDATAo", "BAD", 7) returns "Hello"
     *
     * @param in The string you'd like to remove something from
     * @param del The first few characters matching the deletion
     * @param length the length of characters being deleted.
     * @return String with removals applied
     */
    public static String removeLike(String in, String del, int length) {
        while (in.indexOf(del) != -1) {
            int loc = in.indexOf(del);
            in = in.substring(0, loc) + in.substring(loc + length);
        }
        return in;
    }

    /**
     * Converts all DOIs of the j'th SLR's References to PMCIDs, if applicable.
     *
     * @param slrs
     * @param j
     */
    public static void searchPMCForRefs(ArrayList<SLR> slrs, int j) {
        for (int i = 0; i < slrs.get(j).references.size(); i++) {
            if (slrs.get(j).references.get(i).id.equals("not found")) {
                String doi = slrs.get(j).references.get(i).doi;
                String PMCID = "NULL";
                if (doi.indexOf("10") == 0) {
                    PMCID = doiToPMC(slrs.get(j).references.get(i).doi);
                }
                if (!PMCID.equals("NULL")) {
                    slrs.get(j).references.get(i).id = PMCID;
                    slrs.get(j).references.get(i).idFormat = "PMC";
                }
            }
        }
    }

    public static ArrayList<SLR> getSLRs() {
        ArrayList<SLR> slrs = new ArrayList<SLR>();
        SLR dummy = new SLR();
        dummy.references = new ArrayList<Reference>();
        slrs.add(dummy);//add a dummy value so that the indices of slrs and the index on the excel sheet line up (ease of use)
        try {
            File myFile = new File("C:\\Users\\ethan\\Desktop\\2023USRAResearch\\CovidClef2023\\covidClef2023\\Covid_19_Dataset_and_References\\Covid_SLR_Dataset.xlsx");
            FileInputStream file = new FileInputStream(myFile);
            Workbook workbook = new XSSFWorkbook(file);
            DataFormatter df = new DataFormatter();
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = sheet.iterator();
            int rowTerator = 0;
            while (iterator.hasNext()) {
                Row row = iterator.next();
                Iterator<Cell> cellIterator = row.iterator();
                SLR added = new SLR();
                while (cellIterator.hasNext()) {
                    rowTerator++;
                    Cell cell = cellIterator.next();
                    String cellValue = df.formatCellValue(cell);
                    parseSLRInfo(added, cell, rowTerator);
                }
                rowTerator = 0;
                slrs.add(added);
            }
            workbook.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return slrs;
    }

    /**
     * Creates references for a given SLR given a SLR XLS file.
     *
     * @param fileID the id of the file, as a number. Files must be stored in
     * this format (#.xlsx)
     * @return an arrayList of references, filled with preliminary information,
     * such as DOI, and Date. NOTE: Date is currently not carried over, this
     * information is instead determined from the PMC entry we retrieve.
     */
    public static ArrayList<Reference> refFileToDOIs(int fileID) {
        ArrayList<Reference> References = new ArrayList<Reference>();
        try {
            File referenceFile = new File("C:\\Users\\ethan\\Desktop\\2023USRAResearch\\CovidClef2023\\covidClef2023\\Covid_19_Dataset_and_References\\References\\" + fileID + ".xlsx");
            Workbook workbook = new XSSFWorkbook(referenceFile);
            Sheet sheet = workbook.getSheetAt(0); //only dealing with sheet 0 (main sheet)
            int colTerator = 0;
            int rowTerator = 0;
            DataFormatter df = new DataFormatter();
            Iterator<Row> iterator = sheet.iterator();
            Row row = iterator.next();
            while (iterator.hasNext()) {
                Reference added = new Reference();
                row = iterator.next();
                rowTerator++;
                Iterator<Cell> cellIterator = row.iterator();
                while (cellIterator.hasNext()) {
                    colTerator++;
                    Cell cell = cellIterator.next();
                    String cellValue = df.formatCellValue(cell);
                 //   System.out.println(cellValue +"   :" +  colTerator);
                    try{
                    switch (colTerator) {
                        case 1:
                            added.doi = cellValue;
                            break;
                        case 2:
                            //parse the spreadsheet date and store that as our date (if wanted)
                            break;
                        case 3:
                            if (!cellValue.equals("Unknown Title") && cellValue.length() > 5) {
                                //  System.out.println(fileID + ", " + (rowTerator + 1) + " HAS BEEN FOUND");
                                added.hasBeenFound = true;
                                Reference.found++;
                                added.title = cellValue;
                              
                            } else {
                                // System.out.println(fileID + ", " + rowTerator + " HAS NOT BEEN FOUND");
                            }
                            break;
                        case 4:
                            if (added.hasBeenFound) {
                                added.Abstract = cellValue;
                            }
                            break;
                        case 5:
                            if (added.hasBeenFound) {
                                if (cellValue.length() > 2 && cellValue.contains("[") && cellValue.contains("]")) {
                                    StringTokenizer auths = new StringTokenizer(cellValue.substring(cellValue.lastIndexOf("[") + 1, cellValue.indexOf("]")), ",");
                                    while (auths.hasMoreTokens()) {
                                        StringTokenizer bits = new StringTokenizer(auths.nextToken(), "%");
                                        String fn = bits.nextToken();
                                        String ln = bits.nextToken();
                                        String email = bits.nextToken();
                                        Author x = new Author(fn, ln, email);
                                        added.authors.add(x);
                                    }
                                }
                            }
                            break;
                        case 6:
                            if (added.hasBeenFound) {
                                added.id = cellValue;
                            }
                            break;
                        case 7:
                            if (added.hasBeenFound) {
                                added.idFormat = cellValue;
                            }
                            break;
                        case 8:
                            if (added.hasBeenFound && cellValue.length() > 4) {
                            //    System.out.println(cellValue);
                                added.dateAccepted = LocalDate.parse(cellValue);
                            }
                            break;
                    }
                }catch(Exception e){
                        System.out.println(e +" innerError " + fileID);
                }
                }
               
                colTerator = 0;
                if (added.doi.length() > 3 && added.doi.indexOf("10.") == 0) {
                    References.add(added);
                    Reference.total++;
                }
            }
            workbook.close();
        } catch (Exception e) {
            System.out.println(e + " on file " + fileID);
        }
        return References;
    }

    /**
     *
     * @param input the input for which we will be grabbing info from
     * @param tag the searched tag
     * @param exact whether or not the tag is 'exact' or needs to be
     * supplemented with &lt, &gt, and /
     * @return information found in the tag.
     */
    public static String grabTag(String input, String tag, boolean exact) {
        return grabTag(input, tag, tag, exact);
    }

    /**
     *
     * @param doi the doi value in format 10.PREFIX/SUFFIX
     * @return the PMC id of the DOI
     */
    public static String doiToPMC(String doi) {
        String in = pubmedConvertDOI(doi);
        return grabTag(in, "pmcid=\"", "\"", true);
    }

    /**
     *
     * @param input the input for which we will be grabbing info from
     * @param opentag the opening tag, With or without {@literal < or >}
     * @param closetag the closing tag, With or without {@literal </ or >}
     * @param exact Specify true to indicate you have put in the exact
     * open/closetag with opentag/closetag character
     * ({@literal<example> / </example>}) otherwise, the tag open/close
     * characters are added.
     * @return the found value.
     *
     */
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

    /**
     * Allows access to the converter api for DOI to PMCs, PMIDs, and other
     * formats.
     *
     * @param doi the doi value in format 10.PREFIX/SUFFIX
     * @return the html output of this text.
     */
    public static String pubmedConvertDOI(String doi) {
        String base = "https://www.ncbi.nlm.nih.gov/pmc/utils/idconv/v1.0/";
        return getHTML(base + "?ids=" + doi);
    }

    /**
     * Basic HTML input method for use with RESTful APIs.
     *
     * @param urlToRead The URL of the API.
     * @return the HTML of the output, in text form
     */
    public static String getHTML(String urlToRead) {
        String result = "error: Some failure in getHTML line\n" + urlToRead;
        String header = "";
        try {
            URL url = new URL(urlToRead);
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            result = IOUtils.toString(is, StandardCharsets.UTF_8);
            header = conn.getHeaderFields().toString();
        } catch (Exception e) {
            if (!e.toString().contains("elsevier")) {
                System.out.println(header);
                System.out.println(e);
            }
        }
        return result;
    }

    /**
     * Parse SLR information as specified in the attached Covid_SLR_Dataset
     * file. This method only works with that document's formatting of data.
     *
     * @param s the given SLR
     * @param c the given cell
     * @param r the column position of the cell.
     */
    public static void parseSLRInfo(SLR s, Cell c, int r) {
        DataFormatter df = new DataFormatter();
        String cellValue = df.formatCellValue(c);
        switch (r) {
            case 1:
                s.name = cellValue;
                break;
            case 2:
                s.link = cellValue;
                break;
            case 3:
             try {
                s.dbSearches = Integer.parseInt(cellValue) + "";
            } catch (Exception e) {
                s.dbSearches = "non int value";
                System.out.println(e);
            }
            break;
            case 4:
                s.refs = cellValue;
                break;
            default:
                break;
        }

    }

}
