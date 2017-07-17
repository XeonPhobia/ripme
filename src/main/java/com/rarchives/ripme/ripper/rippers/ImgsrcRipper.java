package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Ting jeg har lagt til
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;
import java.util.Scanner;
import java.util.Collections;


import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class ImgsrcRipper extends AbstractHTMLRipper {
    // Current HTML document
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
        if (albumDoc == null) {
            albumDoc = Http.url(url).get();
        }
        
		List<Integer> numberofAlbumPagesList = new ArrayList<Integer>();						
    	List<String> counting = new ArrayList<String>();
    	for (Element thumb : albumDoc.select("a")) {
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
//    				throw new IOException("Found a number of siignificance" + integer);
    				numberofAlbumPagesList.add(integer);
    			}
    		} 		 
        } 
    	maxCounterAlbumPages = Collections.max(numberofAlbumPagesList);
        return albumDoc;
    }
    
    @Override
    public List<String> getURLsFromPage(Document doc) {
    List<String> imageURLs = new ArrayList<String>();
        for (Element thumb : doc.select("img")) {
//    		String image = thumb.attr("src");
    		String image = thumb.attr("data-src");
    		if(image.length() > 0){
    			imageURLs.add(image);
    		}
        }
        return imageURLs;
    }
    
    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
    
    @Override
    public Document getNextPage(Document albumDoc) throws IOException {	
//    	int maxCounterAlbumPages = 48;
    	indexNominus = indexNominus + 12;
    	if (indexNominus > maxCounterAlbumPages) {
    		throw new IOException("Found last page at" + this.url);
    	}
    	URL oldUrl = new URL(albumDoc.location());	
    	URL url = new URL(oldUrl + "&skp=" + indexNominus);
        albumDoc = Http.url(url).get();	

    	
    return albumDoc;
    } 
   
    
    
}
