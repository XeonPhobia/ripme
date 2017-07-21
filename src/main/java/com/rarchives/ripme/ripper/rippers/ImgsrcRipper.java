package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Ting jeg har lagt til
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;
import java.util.Scanner;
import java.util.Collections;
import java.io.*;
import java.util.*;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class ImgsrcRipper extends AbstractHTMLRipper {
	public List<URL> numberofAlbumIdentifiersList = new ArrayList<URL>(); 	

    // Current HTML document
	public Document doc = null;
    private Document albumDoc = null;
    private Document siteDoc = null;
	URL oldUrl = null;
	int indexNominus = 0;
	public static Integer maxCounterAlbumPages;
	
    public ImgsrcRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "imgsrc";
    }
    @Override
    public String getDomain() {
        return "imgsrc.ru";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[wm.]*imgsrc\\.ru/([a-zA-Z0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected imgsrc.ru URL format: " +
                        "imgsrc.ru/albumid - got " + url + "instead");
    }

   
    
    @Override
    public Document getFirstPage() throws IOException {

    	
    	
    	
   		if (numberofAlbumIdentifiersList.size() == 0) {
			doc = Http.url(url).get();
   			for (Element thumb : doc.select("td > a")) {
        		String imageSearch = thumb.attr("target");
        		if (imageSearch.equals("_blank")) {
        			imageSearch = thumb.attr("href");
         			Pattern p = Pattern.compile("a\\d{7}");
        			Matcher m = p.matcher(imageSearch);    				

        			if(m.find()) {    			
        				String totalPageString;
        				totalPageString = m.group();    					
        				Scanner inSearch = new Scanner(totalPageString).useDelimiter("[^0-9]+");
        				int integerSearch = inSearch.nextInt();
        				URL urlSearch = new URL("http://imgsrc.ru/main/tape.php?ad=" + integerSearch + "&pwd=");
        				numberofAlbumIdentifiersList.add(urlSearch);
        			} 
        		} 		 
            } 
		} 
//		Get the first document from the main search lists URLs.    		
		if (numberofAlbumIdentifiersList.size() > 0) {
			doc = Http.url(numberofAlbumIdentifiersList.get(0)).get();    			
			numberofAlbumIdentifiersList.remove(0);
//			throw new IOException("The max pages reached in album, albumlist is currently this long:" + numberofAlbumIdentifiersList.size());
		} else {
//			throw new IOException("No more Albums to download");
		}
          
	List<Integer> numberofAlbumPagesList = new ArrayList<Integer>();						
	for (Element thumb : doc.select("a")) {
		String urlExtender = thumb.attr("href");
		if(urlExtender.indexOf("&skp=") > 0){    		   		
			String urlstrings = urlExtender.toString();
 			Pattern p = Pattern.compile("\\&skp=(.*?)\\&pwd=");
			Matcher m = p.matcher(urlstrings);    				

			if(m.find()) {    			
				String totalPageString;
				totalPageString = m.group();    					
				Scanner in = new Scanner(totalPageString).useDelimiter("[^0-9]+");
				int integer = in.nextInt();
//				throw new IOException("Found a number of siignificance" + integer);
				numberofAlbumPagesList.add(integer);
			}
		} 		 
    } 
	maxCounterAlbumPages = Collections.max(numberofAlbumPagesList);
    return doc;
}
    
    @Override
    public List<String> getURLsFromPage(Document doc) {
    List<String> imageURLs = new ArrayList<String>();
		for (Element thumb : doc.select("img")) {
    		String image = thumb.attr("class");
    		if (image.equals("big")) {
    			image = thumb.attr("src");
        		if(image.length() > 0){
        			imageURLs.add(image);
        		}
    		}
        }

    	for (Element thumb : doc.select("img")) {
    		String image = thumb.attr("data-src");
    		if(image.length() > 0){
    			imageURLs.add(image);
    		}
        }
    	
//    	if (imageURLs.size() == 0){
//    		Document getNextPage(Document doc);
//    	}
        return imageURLs;
    }
    
    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
    
    @Override
    public Document getNextPage(Document albumDoc) throws IOException {	
    	indexNominus = indexNominus + 12;
    	if (indexNominus > maxCounterAlbumPages) {
    		indexNominus = 0;
    		if (numberofAlbumIdentifiersList.size() > 0) {
    			albumDoc = Http.url(numberofAlbumIdentifiersList.get(0)).get();    			
    			numberofAlbumIdentifiersList.remove(0);
//    			throw new IOException("The max pages reached in album, albumlist is currently this long:" + numberofAlbumIdentifiersList.size());
    		}
    		
//    		albumDoc = getFirstPage();
    	} else {
    		URL oldUrl = new URL(albumDoc.location());	
        	URL url = new URL(oldUrl + "&skp=" + indexNominus);
            albumDoc = Http.url(url).get();	
    	}
    	
    	if (indexNominus > maxCounterAlbumPages) {
    		throw new IOException("Found last page at" + this.url);
    	}	
    return albumDoc;
    } 
   
    
    
}
