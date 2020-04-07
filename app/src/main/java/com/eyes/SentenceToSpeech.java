package com.eyes;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * Created by Arihant on 25-04-2016.
 */
public class SentenceToSpeech implements
        TextToSpeech.OnInitListener {
    private TextToSpeech textToSpeech;
    private String text = null;

    SentenceToSpeech(Context context,String text){
        textToSpeech = new TextToSpeech(context, this);
        this.text = text;
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            } else {
                convertTextToSpeech();
            }
        }
    }

    public void convertTextToSpeech() {
        if (null == text || text.equals(""))
            text = "No results";
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
}
