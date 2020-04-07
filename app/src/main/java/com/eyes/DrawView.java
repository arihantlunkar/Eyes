
package com.eyes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.view.SurfaceView;
import android.content.SharedPreferences;

public class DrawView extends SurfaceView {

    private final Paint paintBrush = new Paint();
    boolean _inFrame;
    int screenWidth = 0;
    int screenHeight = 0;
    Boolean isLeftEyeBlink,isRightEyeBlink,isLeftEyeClosed,isRightEyeClosed;
    static int alphabetsPosition = 0, rectPosition = 0;
    static long previousTime = System.currentTimeMillis();
    static long currentTime = System.currentTimeMillis();
    String[] alphabets = new String[]{"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","←"," ","↕","✓"};
    Boolean isAlphabetSelected = false;
    static String para = "";
    int minNoChar = 26;
    static String[] suggestions = new String[]{"Trigger Alarm","Make a call","Send an SMS","Current Weather","Send an Email","Play Music","Switch on Lights","Close App","Live Scores","Wiki Search"};
    int diameter;
    int wOffset;
    int hOffset;
    static int suggestionPosition = 0;
    static Boolean isSwitchSuggestion = false;
    Boolean isSuggestionSelected = false;
    static Boolean isRectChosen = false;
    static SuggestionFramework suggestionFrameworkObj = null;
    private Context context;
    int mode = 0;

    public DrawView(Context context,boolean inFrame, Boolean isLeftEyeBlink,Boolean isRightEyeBlink,Boolean isLeftEyeClosed,Boolean isRightEyeClosed, int mode) {
        super(context);

        this.context = context;

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

        this.mode = mode;

        if(suggestionFrameworkObj == null)
            suggestionFrameworkObj = new SuggestionFramework();
    }

    @Override
    protected void onDraw(final Canvas canvas) {

        coverCamera(canvas);

        //settingsHelper(canvas);

        displayInFrame(canvas);

        if(!isSwitchSuggestion) {
            if(mode == 2)
                chooseAlphabetA();
            else if(mode == 1)
                chooseAlphabetB();
            else if(mode == 0)
                chooseAlphabetC();
        }
        else
            chooseSuggestionS();


        if(mode == 2)
            displayKeyboardA(canvas);
        else if(mode == 1)
            displayKeyboardB(canvas);
        else if(mode == 0)
            displayKeyboardB(canvas);


        displayBlinkInputs(canvas);

        displaySuggestions(canvas);

    }

    private void displaySuggestions(Canvas canvas) {
        if(suggestions != null) {
            for (int i = 0; i < suggestions.length; i++) {
                if (isSuggestionSelected && i == suggestionPosition && isSwitchSuggestion) {
                    paintBrush.setStyle(Paint.Style.FILL);
                    paintBrush.setColor(Color.GRAY);
                    para = suggestions[i] + " ";
                    isSwitchSuggestion = false;
                    suggestionPosition = 0;
                    isSuggestionSelected = false;
                    if(suggestionFrameworkObj.getStatus() != AsyncTask.Status.RUNNING){
                        suggestionFrameworkObj = new SuggestionFramework();
                        suggestionFrameworkObj.execute(this);
                    }
                    if(context != null)
                        new SentenceToSpeech(context,para).convertTextToSpeech();
                } else if (i == suggestionPosition && isSwitchSuggestion) {
                    paintBrush.setStyle(Paint.Style.FILL);
                    paintBrush.setColor(Color.GRAY);
                } else {
                    paintBrush.setStyle(Paint.Style.STROKE);
                    paintBrush.setColor(Color.WHITE);
                }
                canvas.drawRect(-hOffset, screenHeight / 2 + ((i) * hOffset), screenWidth + hOffset, screenHeight / 2 + ((i + 1) * hOffset), paintBrush);
                paintBrush.setTextSize(25f);
                paintBrush.setColor(Color.WHITE);
                paintBrush.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(suggestions[i], hOffset, ((screenHeight / 2 + ((i) * hOffset)) + (screenHeight / 2 + ((i + 1) * hOffset))) / 2 + 7, paintBrush);
            }
        }
    }

    private void displayInFrame(Canvas canvas) {
        if (_inFrame)
            paintBrush.setColor(Color.GREEN);
        else
            paintBrush.setColor(Color.RED);
        canvas.drawCircle(screenWidth-wOffset,wOffset,diameter/2,paintBrush);
    }

    private void settingsHelper(Canvas canvas) {

        Bitmap settingsBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                        R.drawable.settings),
                diameter/2 + 5 , diameter/2 + 5, true);
        paintBrush.setColor(Color.WHITE);
        canvas.drawCircle(screenWidth-4*wOffset,wOffset,diameter/2,paintBrush);
        canvas.drawBitmap(settingsBitmap,screenWidth-4*wOffset - 15,17,paintBrush);
    }

    private void displayBlinkInputs(Canvas canvas) {
        paintBrush.setTextSize(40f);
        paintBrush.setTextAlign(Paint.Align.LEFT);
        String dummy = para;
        if(para.length() > minNoChar)
            dummy = dummy.substring(para.length()-minNoChar,para.length());
        canvas.drawText(dummy.toUpperCase(),wOffset*2,(screenHeight/2 + (-4*diameter))/2,paintBrush);
    }

    private void coverCamera(Canvas canvas){
        paintBrush.setColor(Color.BLACK);
        canvas.drawRect(0, 0, screenWidth, screenHeight, paintBrush);
    }

    private void chooseAlphabetB(){
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

    private void chooseAlphabetC(){
        currentTime = System.currentTimeMillis();
        isAlphabetSelected = false;
        if(isLeftEyeBlink && isRightEyeBlink && (currentTime - previousTime) > 299) {
            previousTime = currentTime;
            isAlphabetSelected = true;
        }
        else {
            alphabetsPosition = (int) (currentTime - previousTime) / 2000;
            if (alphabetsPosition > alphabets.length - 1) {
                alphabetsPosition = 0;
                previousTime = currentTime;
            }
        }

    }

    private void chooseAlphabetA(){
        currentTime = System.currentTimeMillis();
        isAlphabetSelected = false;
        if(isLeftEyeBlink && isRightEyeBlink && (currentTime - previousTime) > 0) {
            previousTime = currentTime;
            if (isRectChosen) {
                isAlphabetSelected = true;
                isRectChosen = false;
            }
            else
                isRectChosen = true;
        }
        else if(isLeftEyeBlink && (currentTime - previousTime) > 0) {
            previousTime = currentTime;
            if (!isRectChosen) {
                rectPosition -= 5;
                alphabetsPosition = rectPosition;
                if (alphabetsPosition < 0)
                    alphabetsPosition = 25;
                if (rectPosition < 0)
                    rectPosition = 25;
            }
            else {
                int rectBlock = alphabetsPosition/5;
                int position = alphabetsPosition%5;
                position --;
                if (position < 0)
                    alphabetsPosition = (rectBlock*5) + 4;
                else
                    alphabetsPosition = (rectBlock*5) + position;
            }
        }
        else if(isRightEyeBlink && (currentTime - previousTime) > 0) {
            previousTime = currentTime;
            if (!isRectChosen) {
                rectPosition += 5;
                alphabetsPosition = rectPosition;
                if (rectPosition > 25)
                    rectPosition = 0;
                if (alphabetsPosition > 25)
                    alphabetsPosition = 0;
            }
            else {
                int rectBlock = alphabetsPosition/5;
                int position = alphabetsPosition%5;
                position ++;
                if (position >= 5)
                    alphabetsPosition = (rectBlock*5);
                else
                    alphabetsPosition = (rectBlock*5) + position;
            }
        }
    }

    private void chooseSuggestionA(){
        currentTime = System.currentTimeMillis();
        isSuggestionSelected= false;
        if(isLeftEyeBlink && isRightEyeBlink && (currentTime - previousTime) > 0) {
            previousTime = currentTime;
            isSuggestionSelected = true;
        }
        else if(isLeftEyeBlink && (currentTime - previousTime) > 0) {
            previousTime = currentTime;
            suggestionPosition--;
            if(suggestionPosition < 0)
                suggestionPosition = suggestions.length - 1;
        }
        else if(isRightEyeBlink && (currentTime - previousTime) > 0) {
            previousTime = currentTime;
            suggestionPosition++;
            if(suggestionPosition > suggestions.length - 1)
                suggestionPosition = 0;
        }
    }

    private void chooseSuggestionS(){
        currentTime = System.currentTimeMillis();
        isSuggestionSelected= false;
        if(isLeftEyeBlink && isRightEyeBlink && (currentTime - previousTime) > 0) {
            previousTime = currentTime;
            isSuggestionSelected = true;
        }
        else {
            suggestionPosition = (int) (currentTime - previousTime) / 2000;
            if (suggestionPosition > suggestions.length - 1) {
                suggestionPosition = 0;
                previousTime = currentTime;
            }
        }
    }

    private void displayKeyboardA(Canvas canvas){

        for(int i=0;i<alphabets.length;i++)
        {
            int pos;
            int element;
            if(isAlphabetSelected && i == alphabetsPosition && !isSwitchSuggestion) {
                System.out.println(alphabets[i]);
                paintBrush.setColor(Color.GRAY);
                if(alphabets[i].equals(" ")) {
                    para += " ";
                    if(suggestionFrameworkObj.getStatus() != AsyncTask.Status.RUNNING){
                        suggestionFrameworkObj = new SuggestionFramework();
                        suggestionFrameworkObj.execute(this);
                    }
                }
                else if(alphabets[i].equals("←") && para.length() > 0) {
                    para = para.substring(0, para.length() - 1);
                    if(suggestionFrameworkObj.getStatus() != AsyncTask.Status.RUNNING){
                        suggestionFrameworkObj = new SuggestionFramework();
                        suggestionFrameworkObj.execute(this);
                    }
                }
                else if(alphabets[i].equals("↕"))
                    isSwitchSuggestion = true;
                else if(alphabets[i].equals("✓")) {
                    if(context != null)
                        new SentenceToSpeech(context,para).convertTextToSpeech();
                    para = "";
                }
                else if(!alphabets[i].equals("←")) {
                    para += alphabets[i];
                    if(suggestionFrameworkObj.getStatus() != AsyncTask.Status.RUNNING){
                        suggestionFrameworkObj = new SuggestionFramework();
                        suggestionFrameworkObj.execute(this);
                    }
                }
            }
            else if(i == alphabetsPosition && !isSwitchSuggestion) {
                System.out.println(alphabets[i]);
                paintBrush.setColor(Color.GRAY);
            }
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

        if(isRectChosen) {
            paintBrush.setStrokeWidth(2f);
            paintBrush.setStyle(Paint.Style.STROKE);
            paintBrush.setColor(Color.GREEN);
        }
        else {
            paintBrush.setStrokeWidth(2f);
            paintBrush.setStyle(Paint.Style.STROKE);
            paintBrush.setColor(Color.RED);
        }
        if (rectPosition < 1) {
            canvas.drawRect(0, screenHeight / 2 + (-4 * diameter) + wOffset, (5 * diameter), screenHeight / 2 + (-3 * diameter) + wOffset, paintBrush);
        } else if (rectPosition < 6) {
            canvas.drawRect((rectPosition * diameter), screenHeight / 2 + (-4 * diameter) + wOffset, (5 * diameter) + (rectPosition * diameter), screenHeight / 2 + (-3 * diameter) + wOffset, paintBrush);
        } else if (rectPosition < 11) {
            canvas.drawRect(0, screenHeight / 2 + (-3 * diameter) + wOffset, (5 * diameter), screenHeight / 2 + (-2 * diameter) + wOffset, paintBrush);
        } else if (rectPosition < 16) {
            canvas.drawRect(((rectPosition - 10) * diameter), screenHeight / 2 + (-3 * diameter) + wOffset, (5 * diameter) + (rectPosition * diameter), screenHeight / 2 + (-2 * diameter) + wOffset, paintBrush);
        } else if (rectPosition < 21) {
            canvas.drawRect(0, screenHeight / 2 + (-2 * diameter) + wOffset, (5 * diameter), screenHeight / 2 + (-1 * diameter) + wOffset, paintBrush);
        } else if (rectPosition < 26) {
            canvas.drawRect(((rectPosition - 20) * diameter), screenHeight / 2 + (-2 * diameter) + wOffset, (5 * diameter) + (rectPosition * diameter), screenHeight / 2 + (-1 * diameter) + wOffset, paintBrush);
        }

        paintBrush.setStyle(Paint.Style.FILL);
        paintBrush.setColor(Color.WHITE);
        canvas.drawLine(-wOffset, screenHeight / 2 + (-4 * diameter), screenWidth + wOffset, screenHeight / 2 + (-4 * diameter), paintBrush);
    }

    private void displayKeyboardB(Canvas canvas){
        for(int i=0;i<alphabets.length;i++)
        {
            int pos;
            int element;
            if(isAlphabetSelected && i == alphabetsPosition && !isSwitchSuggestion) {
                System.out.println(alphabets[i]);
                paintBrush.setColor(Color.GRAY);
                if(alphabets[i].equals(" ")) {
                    para += " ";
                    if(suggestionFrameworkObj.getStatus() != AsyncTask.Status.RUNNING){
                        suggestionFrameworkObj = new SuggestionFramework();
                        suggestionFrameworkObj.execute(this);
                    }
                }
                else if(alphabets[i].equals("←") && para.length() > 0) {
                    para = para.substring(0, para.length() - 1);
                    if(suggestionFrameworkObj.getStatus() != AsyncTask.Status.RUNNING){
                        suggestionFrameworkObj = new SuggestionFramework();
                        suggestionFrameworkObj.execute(this);
                    }
                }
                else if(alphabets[i].equals("↕"))
                    isSwitchSuggestion = true;
                else if(alphabets[i].equals("✓")) {
                    if(context != null)
                        new SentenceToSpeech(context,para).convertTextToSpeech();
                    para = "";
                }
                else if(!alphabets[i].equals("←")) {
                    para += alphabets[i];
                    if(suggestionFrameworkObj.getStatus() != AsyncTask.Status.RUNNING){
                        suggestionFrameworkObj = new SuggestionFramework();
                        suggestionFrameworkObj.execute(this);
                    }
                }
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
}

