
package com.eyes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DrawViewA extends SurfaceView {

    private final Paint paintBrush = new Paint();
    boolean _inFrame;
    int screenWidth = 0;
    int screenHeight = 0;
    Boolean isLeftEyeBlink,isRightEyeBlink,isLeftEyeClosed,isRightEyeClosed;
    static int alphabetsPosition = 0;
    static long previousTime = System.currentTimeMillis();
    static long currentTime = System.currentTimeMillis();
    static long whenWasLeftEyeClosedTime = System.currentTimeMillis();
    static long whenWasRightEyeClosedTime = System.currentTimeMillis();
    String[] alphabets = new String[]{"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","<-","Sp","%"};
    Boolean isAlphabetSelected = false;
    static String para = "";
    int minNoChar = 50;
    String[] suggestions  = new String[]{"How are you ?","Call my Dad.","What did you have for lunch today ?","Where are you these days ?","Who do you think will win today in football ?","Why there is so much noise here ?","Can you please hurry up ?","Do you mind if I ask you for dinner today ?","How was your day today ?","Where were you last week ?"};
    int diameter;
    int wOffset;
    int hOffset;
    static int suggestionPosition = 0;
    static Boolean isSwitchSuggestion = false;
    Boolean isSuggestionSelected = false;


    public DrawViewA(Context context,boolean inFrame, Boolean isLeftEyeBlink,Boolean isRightEyeBlink,Boolean isLeftEyeClosed,Boolean isRightEyeClosed) {
        super(context);

        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        setWillNotDraw(false);
        _inFrame = inFrame;
        this.isLeftEyeBlink = isLeftEyeBlink;
        this.isRightEyeBlink = isRightEyeBlink;
        this.isLeftEyeClosed = isLeftEyeClosed;
        this.isRightEyeClosed = isRightEyeClosed;
        diameter = screenWidth/10;
        wOffset = screenWidth/20;
        hOffset = screenHeight/20;
    }

    @Override
    protected void onDraw(final Canvas canvas) {

        coverCamera(canvas);

        displayInFrame(canvas);

        if(!isSwitchSuggestion)
            chooseAlphabet();
        else
            chooseSuggestion();

        displayKeyboard(canvas);

        displayBlinkInputs(canvas);

        displaySuggestions(canvas);

    }

    private void displaySuggestions(Canvas canvas) {
         for(int i=0;i<suggestions.length;i++){
            if(isSuggestionSelected && i == suggestionPosition && isSwitchSuggestion) {
                paintBrush.setStyle(Paint.Style.FILL);
                paintBrush.setColor(Color.GRAY);
                para = suggestions[i];
                isSwitchSuggestion = false;
            }
            else if(i==suggestionPosition && isSwitchSuggestion){
                paintBrush.setStyle(Paint.Style.FILL);
                paintBrush.setColor(Color.GRAY);
            }else {
                paintBrush.setStyle(Paint.Style.STROKE);
                paintBrush.setColor(Color.WHITE);
            }
            canvas.drawRect(-hOffset,screenHeight/2 + ((i)*hOffset),screenWidth + hOffset,screenHeight/2 + ((i+1)*hOffset),paintBrush);
            paintBrush.setTextSize(25f);
            paintBrush.setColor(Color.WHITE);
            paintBrush.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(suggestions[i], hOffset ,((screenHeight/2 + ((i)*hOffset)) + (screenHeight/2 + ((i+1)*hOffset)))/2 + 7, paintBrush);
        }
    }

    private void displayInFrame(Canvas canvas) {
        if (_inFrame)
            paintBrush.setColor(Color.GREEN);
        else
            paintBrush.setColor(Color.RED);
        canvas.drawCircle(screenWidth-wOffset,wOffset,diameter/2,paintBrush);
    }

    private void displayBlinkInputs(Canvas canvas) {
        paintBrush.setTextSize(25f);
        paintBrush.setTextAlign(Paint.Align.LEFT);
        String dummy = para;
        if(para.length() > minNoChar)
            dummy = dummy.substring(para.length()-minNoChar,para.length());
        canvas.drawText(dummy,wOffset*2,(screenHeight/2 + (-4*diameter))/2,paintBrush);
    }

    private void coverCamera(Canvas canvas){
        paintBrush.setColor(Color.BLACK);
        canvas.drawRect(0, 0, screenWidth, screenHeight, paintBrush);
    }

    private void chooseAlphabet(){
        currentTime = System.currentTimeMillis();
        isAlphabetSelected = false;
        if(isLeftEyeBlink && isRightEyeBlink && (currentTime - previousTime) > 299) {
            previousTime = currentTime;
            isAlphabetSelected = true;
        }
        else if(isLeftEyeBlink && (currentTime - previousTime) > 299) {
            previousTime = currentTime;
            alphabetsPosition--;
            if(alphabetsPosition < 0)
                alphabetsPosition = alphabets.length - 1;
        }
        else if(isRightEyeBlink && (currentTime - previousTime) > 299) {
            previousTime = currentTime;
            alphabetsPosition++;
            if(alphabetsPosition > alphabets.length - 1)
                alphabetsPosition = 0;
        }
    }

    private void chooseSuggestion(){
        currentTime = System.currentTimeMillis();
        isSuggestionSelected= false;
        if(isLeftEyeBlink && isRightEyeBlink && (currentTime - previousTime) > 299) {
            previousTime = currentTime;
            isSuggestionSelected = true;
        }
        else if(isLeftEyeBlink && (currentTime - previousTime) > 299) {
            previousTime = currentTime;
            suggestionPosition--;
            if(suggestionPosition < 0)
                suggestionPosition = suggestions.length - 1;
        }
        else if(isRightEyeBlink && (currentTime - previousTime) > 299) {
            previousTime = currentTime;
            suggestionPosition++;
            if(suggestionPosition > suggestions.length - 1)
                suggestionPosition = 0;
        }
    }

    private void displayKeyboard(Canvas canvas){
        for(int i=0;i<alphabets.length;i++)
        {
            int pos;
            int element;
            if(isAlphabetSelected && i == alphabetsPosition && !isSwitchSuggestion) {
                paintBrush.setColor(Color.GRAY);
                if(alphabets[i].equals("Sp"))
                    para += " ";
                else if(alphabets[i].equals("<-") && para.length() > 0)
                    para = para.substring(0,para.length()-1);
                else if(alphabets[i].equals("%"))
                    isSwitchSuggestion = true;
                else
                    para += alphabets[i];
            }
            else if(i == alphabetsPosition && !isSwitchSuggestion)
                paintBrush.setColor(Color.GRAY);
            else
                paintBrush.setColor(Color.WHITE);
            if(i<10) {
                pos = i;
                element = -3*diameter;
            }
            else if(i<20) {
                pos = i - 10;
                element = -2*diameter;
            }
            else {
                pos = i - 20;
                element = -diameter;
            }
            canvas.drawCircle(wOffset + (pos * diameter), screenHeight / 2 + element, diameter / 2 - 2, paintBrush);
            paintBrush.setTextSize(40f);
            paintBrush.setColor(Color.BLACK);
            paintBrush.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(alphabets[i], wOffset + (pos * diameter), screenHeight / 2 + element + 12, paintBrush);
        }

        paintBrush.setColor(Color.WHITE);
        canvas.drawLine(-wOffset, screenHeight / 2 + (-4 * diameter), screenWidth + wOffset, screenHeight / 2 + (-4 * diameter), paintBrush);
    }

    public String[] search(String input){
        try {
            URL url = new URL("http://google.com/complete/search?output=toolbar&q=" + input);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("CompleteSuggestion");
            String[] suggestions = new String[nodeList.getLength()];
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Element fstElmnt = (Element) node;
                NodeList websiteList = fstElmnt.getElementsByTagName("suggestion");
                Element websiteElement = (Element) websiteList.item(0);
                suggestions[i] = (websiteElement.getAttribute("data"));
            }
            return suggestions;
        }
        catch (Exception e) {
            return null;
        }
    }
}
