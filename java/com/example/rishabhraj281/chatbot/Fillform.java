package com.example.rishabhraj281.chatbot;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.TextField;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Fillform extends AppCompatActivity {
    TextToSpeech t1;
    ArrayList <EditText> ed = new ArrayList<EditText>();
    ArrayList <TextView> tv = new ArrayList<TextView>();
    Button btn_prev, btn_repeat, btn_next,btn_audio;
    EditText c_ed;
    TextView c_tv;
    int fields = 0;
    int q_no = 0;
    //LinearLayout ll;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private String language_selected = "en-IND";
    //customise for common questions
    /*final String Q_PHONE = "phone";
    final String Q_DOB = "dob";
    final String Q_EMAIL = "email";
    final String Q_NONE = "none";
    static final Set<String> q_phone = new HashSet<String>(Arrays.asList("telephone", "phone", "landline"));
    static final Set<String> q_dob = new HashSet<String>(Arrays.asList("date of birth", "dob", "d.o.b"));
    static final Set<String> q_email = new HashSet<String>(Arrays.asList("email", "mail"));
*/
    String file_name;
    String file_path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fillform);
        Intent intent = getIntent();

        file_name = intent.getStringExtra("file_path");
        file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+intent.getStringExtra("file_path");
//        final File pdffile = new File(intent.getStringExtra("file_path"));
        btn_prev = (Button) findViewById(R.id.btn_prev);
        btn_repeat = (Button) findViewById(R.id.btn_repeat);
        btn_next = (Button) findViewById(R.id.btn_next);
        btn_audio = (Button) findViewById(R.id.btn_audio);
        c_ed = (EditText) findViewById(R.id.curr_ed);
        c_tv = (TextView) findViewById(R.id.curr_tv);
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    Locale local = new Locale("hi-IND");
                    t1.setLanguage(local);
                }
            }
        });

        t1.setSpeechRate(1);

        //ll = (LinearLayout) findViewById(R.id.linearlayout);

        try {
            readPDF(file_path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fields = tv.size();
        //Log.d("Fillform","fields" + fields);
        fillform(fields,q_no);

        btn_audio.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String toSpeak = c_tv.getText().toString();
                //Log.d("FillForm", toSpeak);
                t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                try {
                    TimeUnit.SECONDS.sleep(1);
                    promptSpeechInput(REQ_CODE_SPEECH_INPUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


        /*c_ed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSpeak = c_tv.getText().toString();
                Log.d("FillForm", toSpeak);
                t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                try {
                    TimeUnit.SECONDS.sleep(1);
                    promptSpeechInput(REQ_CODE_SPEECH_INPUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });*/

        btn_prev.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String answer = c_ed.getText().toString();
                ed.get(q_no).setText(answer);
                if(q_no>0) q_no -= 1;
                fillform(fields,q_no);

            }
        });

        btn_repeat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String toSpeak = c_tv.getText().toString();
                t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                try {
                    TimeUnit.SECONDS.sleep(1);
                    promptSpeechInput(REQ_CODE_SPEECH_INPUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String answer = c_ed.getText().toString();
                ed.get(q_no).setText(answer);
                q_no += 1;
                fillform(fields,q_no);
            }
        });



        //fillform(fields,q_no);



        /*for(int i=0; i<tv.size();i++){
            final int finalI = i;
            ed.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String toSpeak = tv.get(finalI).getText().toString();
                    t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                        promptSpeechInput(REQ_CODE_SPEECH_INPUT+ finalI);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });
        }*/
    }

    //given the q_no, fills the appropriate question and answer to the textview and edittext
    //handles q_no < 0 and q_no = field as well
    private void fillform(int fields, int q_no){
        if(fields == 0){
            Toast.makeText(getApplicationContext(), "Trying to fill an empty form!", Toast.LENGTH_LONG).show();
        }
        else{
            if(q_no < fields) {
                String question = tv.get(q_no).getText().toString();
                c_tv.setText(question);
                String[] words = question.split(" ");
                //String q_type = getQuestionType(words);
                String answer = ed.get(q_no).getText().toString();
                c_ed.setText(answer);
            }
            else{
                try {
                    fillPDF(file_path,file_name);
                    finish();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*String getQuestionType(String[] words){
        String result = Q_NONE;
        for(int i=0; i<words.length; i++){
            if(q_email.contains(words[i])){
                result = Q_EMAIL;
            }
            else if(q_phone.contains(words[i])){
                result = Q_PHONE;
            }
            else if(q_dob.contains(words[i])){
                result = Q_DOB;
            }
            else{

            }
        }
        return result;
    }*/
    private void fillPDF(String file_path, String file_name) throws DocumentException {
        Document doc = new Document();
        try {
            PdfReader reader = new PdfReader(file_path);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/Filled_"+ file_name));
            AcroFields form  = stamper.getAcroFields();

            for(int i=0; i<tv.size();i++){
                form.setField(tv.get(i).getText().toString(),ed.get(i).getText().toString());
            }
            stamper.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void readPDF(String file_path) throws IOException {
        Document doc = new Document();
        PdfReader reader = new PdfReader(file_path);
        AcroFields form  = reader.getAcroFields();
        HashMap<String,AcroFields.Item> fields = (HashMap<String, AcroFields.Item>) form.getFields();
        Set<Map.Entry<String, AcroFields.Item>> entrySet = fields.entrySet();
        for (Map.Entry<String, AcroFields.Item> entry : entrySet) {
            String key = entry.getKey();
            TextView temp = new TextView(this);
            temp.setText(key);
            //ll.addView(temp);

            EditText temp_edit = new EditText(this);
            //ll.addView(temp_edit);

            tv.add(temp);
            ed.add(temp_edit);
        }
    }

    private void saveDoc(File pdffile){
        Document doc = new Document(PageSize.A4);
        try {
            PdfWriter writer =  PdfWriter.getInstance(doc, new FileOutputStream(pdffile));
            writer.setPageEvent(new PdfPageEventHelper(){
                @Override
                public void onGenericTag(PdfWriter writer, Document document, Rectangle rect, String text) {
                    TextField field = new TextField(writer, rect, text);
                    try {
                        writer.addAnnotation(field.getTextField());
                    } catch (IOException ex) {
                        throw new ExceptionConverter(ex);
                    } catch (DocumentException ex) {
                        throw new ExceptionConverter(ex);
                    }
                }
            });

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        doc.open();
        Paragraph p = new Paragraph();

        for(int i=0; i<tv.size();i++){
            p.add(tv.get(i).getText().toString());
            Chunk day = new Chunk("            ");
            day.setGenericTag(tv.get(i).getText().toString());
            p.add(day);

            p.add("\n");
        }
        try {
            doc.add(p);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

//        Paragraph para = new Paragraph(tv.getText().toString()+" : " );
//        Paragraph para1 = new Paragraph(tv1.getText().toString()+" : " + ed1.getText().toString());
//        Paragraph para2 = new Paragraph(tv2.getText().toString()+" : " + ed2.getText().toString());
//        try {
//            doc.add(para);
//            doc.add(para1);
//            doc.add(para2);
//
//        } catch (DocumentException e) {
//            e.printStackTrace();
//        }
        doc.close();
//        try {
//            readPDF();
//        } catch (DocumentException e) {
//            e.printStackTrace();
//        }
    }

    private void promptSpeechInput(int req_code){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language_selected);
        //Locale.getDefault()
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,getString(R.string.speech_prompt));

        try{
            startActivityForResult(intent,req_code);

        }
        catch (ActivityNotFoundException a){
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        String s;
        if(resultCode == RESULT_OK && data!=null){
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            s = result.get(0);
            c_ed.setText(s);
        }

    }
}