package com.eyes;

import android.os.AsyncTask;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Arihant on 20-04-2016.
 */
public class SuggestionFramework extends AsyncTask<DrawView, Void, String[]> {

    private DrawView drawViewObj = null;
    private String[] suggestions;

    @Override
    protected String[] doInBackground(DrawView... params) {
        if(drawViewObj == null) {
            drawViewObj = params[0];
            //drawViewObj.suggestions = null;
        }
        search(drawViewObj.para);
        return suggestions;
    }

    @Override
    protected void onPostExecute(String[] a) {
        super.onPostExecute(a);
        if (drawViewObj != null) {
            if(suggestions != null)
                drawViewObj.suggestions = suggestions;
        }
    }
    public void search(String input){
        try {
            input = input.replace(" ","%20");
            URL url = new URL("http://google.com/complete/search?output=toolbar&q=" + input);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("CompleteSuggestion");
            suggestions = new String[nodeList.getLength()];
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Element fstElmnt = (Element) node;
                NodeList websiteList = fstElmnt.getElementsByTagName("suggestion");
                Element websiteElement = (Element) websiteList.item(0);
                suggestions[i] = (websiteElement.getAttribute("data"));
            }
        }
        catch (Exception e) {
        }
    }
}
