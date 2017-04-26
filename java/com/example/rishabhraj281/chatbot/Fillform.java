package com.example.rishabhraj281.chatbot;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.itextpdf.text.pdf.BaseField;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.TextField;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Fillform extends AppCompatActivity {
    TextToSpeech t1;
//    ArrayList <EditText> ed = new ArrayList<EditText>();
//    ArrayList <TextView> tv = new ArrayList<TextView>();

    ArrayList<String> filledValues = new ArrayList();

    Button btn_prev, btn_repeat, btn_next,btn_audio,btn_submit;
    EditText c_ed;
    TextView c_tv;
    Spinner country_spinner, state_spinner, city_spinner;
    int fields = 0;
    int q_no = 0;
    LinearLayout ll_textbox, ll_spinner;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int REQ_CODE_STATE_SPEECH_INPUT = 101;
    private final int REQ_CODE_CITY_SPEECH_INPUT = 102;
    private final int REQ_CODE_USER_SUBMIT = 103;
    private static int touch = 0 , touch2=0;
    public static String cmp="";
    private String language_selected = "en-IND";
    //customise for common questions
    final int Q_NONE = 0;
    final int Q_PHONE = 1;
    final int Q_EMAIL = 2;
    final int Q_DOB = 3;
    final int Q_ADDR = 4;
    static final Set<String> q_dob = new HashSet<String>(Arrays.asList("date of birth", "dob", "d.o.b"));
    static final Set<String> q_email = new HashSet<String>(Arrays.asList("email", "mail"));
    static final Set<String> q_phone = new HashSet<String>(Arrays.asList("telephone", "phone", "landline"));
    static final Set<String> q_address = new HashSet<String>(Arrays.asList("address"));
    ArrayList countrylist, statelist,citylist;
    ArrayList all_good,required_q;

    int q_type;
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
        btn_submit = (Button) findViewById(R.id.btn_submit);
        c_ed = (EditText) findViewById(R.id.curr_ed);
        c_tv = (TextView) findViewById(R.id.curr_tv);

        ll_textbox = (LinearLayout) findViewById(R.id.form_field);
        ll_spinner = (LinearLayout) findViewById(R.id.address_ll);
        country_spinner = (Spinner) findViewById(R.id.country_spinner);
        state_spinner = (Spinner) findViewById(R.id.state_spinner);
        city_spinner = (Spinner) findViewById(R.id.city_spinner);

        try {
            populateCountrySpinner();
        } catch (IOException e) {
            e.printStackTrace();
        }


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

        required_q = new ArrayList();
        try {
            readPDF(file_path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fields = filledValues.size();
        all_good = new ArrayList();
        for(int i=0; i<fields; i++){
            all_good.add(i,true);
        }
        Log.d("Fillform","fields" + fields);
        Log.d("Fillform","file_name" + file_name);
        Log.d("Fillform","file_path" + file_path);
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
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    saveSpecificQuestion();
                    if(sanityCheckSuccess()){
                        fillPDF(file_path,file_name);
                        finish();
                    }
                    else{
                        promptUserResponse();
                    }
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
        });


        btn_prev.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
//                String answer = c_ed.getText().toString();
//                ed.get(q_no).setText(answer);
                saveSpecificQuestion();
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
//                String answer = c_ed.getText().toString();
//                ed.get(q_no).setText(answer);
                saveSpecificQuestion();
                if(q_no < fields){
                    q_no += 1;
                }
                fillform(fields,q_no);
            }
        });

        country_spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("formfiller", "state item selected");

                if(touch == 0){
                    promptSpeechInput(REQ_CODE_SPEECH_INPUT);
                    touch++;
                }

                return false;
            }
        });

        state_spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("formfiller", "state item selected");

                    if(touch == 0){
                        touch2 =0;
                        promptSpeechInput(REQ_CODE_STATE_SPEECH_INPUT);
                        touch++;
                    }
                return false;
            }
        });

        state_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(touch2==0) {
                    try {
                        Log.e("selected item", "testing");
                        populateCitySpinner((String) parent.getItemAtPosition(position));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        city_spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("formfiller", "state item selected");

                if(touch == 0){
                    promptSpeechInput(REQ_CODE_CITY_SPEECH_INPUT);
                    touch++;
                }
                return false;
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

    private  void populateCountrySpinner() throws IOException {
        countrylist = new ArrayList();
        countrylist.add("India");

        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, countrylist);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        country_spinner.setAdapter(adapter);

        populateStateSpinner();

    }
    private void populateStateSpinner() throws IOException {

        AssetManager assetManager = getAssets();
        InputStream inputStream = assetManager.open("states");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                statelist = new ArrayList<>(Arrays.asList(row));
            }
        }
        catch (IOException ex){
            
        }

        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, statelist);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        state_spinner.setAdapter(adapter);

        populateCitySpinner((String) statelist.get(0));

    }
    private void populateCitySpinner(String stateName) throws IOException {
        Log.e("populate city for:" ,stateName);

        AssetManager assetManager = getAssets();
        InputStream inputStream = assetManager.open(stateName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                citylist = new ArrayList<>(Arrays.asList(row));
            }
        }
        catch (IOException ex){

        }

        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, citylist);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        city_spinner.setAdapter(adapter);
    }

    public static double diceCoefficient(String s1, String s2)
    {
        Set<String> nx = new HashSet<String>();
        Set<String> ny = new HashSet<String>();

        for (int i=0; i < s1.length()-1; i++) {
            char x1 = s1.charAt(i);
            char x2 = s1.charAt(i+1);
            String tmp = "" + x1 + x2;
            nx.add(tmp);
        }
        for (int j=0; j < s2.length()-1; j++) {
            char y1 = s2.charAt(j);
            char y2 = s2.charAt(j+1);
            String tmp = "" + y1 + y2;
            ny.add(tmp);
        }

        Set<String> intersection = new HashSet<String>(nx);
        intersection.retainAll(ny);
        double totcombigrams = intersection.size();

        return (2*totcombigrams) / (nx.size()+ny.size());
    }

    private void sortStates(String stateName){
        Log.e("sort states", stateName);
        cmp = stateName;
        Collections.sort(statelist, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return (int) (100*(diceCoefficient(s2,cmp.toLowerCase())-(diceCoefficient(s1,cmp.toLowerCase()))));
            }
        });

        Log.e("formfiller: After sort", (String) statelist.get(0));

        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, statelist);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        state_spinner.setAdapter(adapter);

        if(!stateName.equalsIgnoreCase((String) statelist.get(0))){
            state_spinner.performClick();
        }
    }

    private void sortCities(String cityName){
        cmp = cityName;
        Log.e("formfiller", cityName );
        Collections.sort(citylist, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return (int) (100*(diceCoefficient(s2,cmp.toLowerCase())-(diceCoefficient(s1,cmp.toLowerCase()))));
            }
        });

        Log.e("formfiller: After sort", (String) citylist.get(0));

        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, citylist);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        city_spinner.setAdapter(adapter);

        if(!cityName.equalsIgnoreCase((String) citylist.get(0))){
             city_spinner.performClick();
        }
    }

    //given the q_no, fills the appropriate question and answer to the textview and edittext
    //handles q_no < 0 and q_no = field as well
    private void fillform(int fields, int q_no){
        if(fields == 0){
            Toast.makeText(getApplicationContext(), "Trying to fill an empty form!", Toast.LENGTH_LONG).show();
            finish();
        }
        else{

            String question = filledValues.get(q_no).split(":")[0];
            c_tv.setText(question);

            String[] words = question.split(" ");
            q_type = getQuestionType(words);
            readSpecificQuestions();

            if(q_no < fields-1) {
                btn_next.setVisibility(View.VISIBLE);
                btn_submit.setEnabled(false);
                btn_submit.setBackgroundColor(this.getResources().getColor(R.color.white));
                btn_submit.setTextColor(this.getResources().getColor(R.color.black));
                if(q_no == 0) btn_prev.setVisibility(View.INVISIBLE);
                else btn_prev.setVisibility(View.VISIBLE);
            }
            else{
                btn_next.setVisibility(View.INVISIBLE);
                btn_submit.setEnabled(true);
                btn_submit.setBackgroundColor(this.getResources().getColor(R.color.submit_form_active));
                btn_submit.setTextColor(this.getResources().getColor(R.color.white));
            }
        }
    }

    void readSpecificQuestions(){
        ll_spinner.setVisibility(View.GONE);
        Log.e("formfiller",filledValues.get(q_no));
        String answer = filledValues.get(q_no).split(":")[1];
        String split[] = answer.split(",");

        c_ed.setText(split[0]);

        if(q_type == Q_NONE || q_type == Q_PHONE || q_type == Q_EMAIL){
            //these simply require validation

        }
        else if(q_type == Q_DOB){

        }
        else if(q_type == Q_ADDR){
            Log.d("formfiller", "Question type is address");
            ll_spinner.setVisibility(View.VISIBLE);
            if(split.length > 1) {
                touch2++;
                sortStates(split[2]); // split[2] has stateName
                try {
                    populateCitySpinner(split[2]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.e("read city", "back");
                sortCities(split[1]);
            }
        }
        else{}
        Log.e("testing", "testing");
    }

    void saveSpecificQuestion(){
        String answer = c_tv.getText().toString();
        answer += ":"+c_ed.getText().toString();
        Log.e("savequestion", q_no +","+ answer);
        if(q_type == Q_NONE || q_type == Q_PHONE || q_type == Q_EMAIL){
            //these simply require validation
            validateInput(c_ed.getText().toString());
        }
        else if(q_type == Q_DOB){

        }
        else if(q_type == Q_ADDR){
            Log.d("formfiller", "Question type is address");
            answer += ","+citylist.get(0)+","+statelist.get(0)+","+countrylist.get(0);
        }
        else{}
        Log.e("savequestion", q_no +","+ answer);
        filledValues.set(q_no ,answer);
    }

    int getQuestionType(String[] words){
        int result = Q_NONE;
        for(int i=0; i<words.length; i++){
            String curr = words[i].toLowerCase();
            if(q_email.contains(curr)){
                result = Q_EMAIL;
                break;
            }
            else if(q_phone.contains(curr)){
                result = Q_PHONE;
                break;
            }
            else if(q_dob.contains(curr)){
                result = Q_DOB;
                break;
            }
            else if(q_address.contains(curr)){
                result = Q_ADDR;
                break;
            }
        }
        return result;
    }

    //checks all fields have valid entry or not
    boolean sanityCheckSuccess(){
        boolean submit = true;
        for(int i=0; i<all_good.size(); i++){
            if(all_good.get(i).equals(false)){
                submit = false;
                break;
            }
        }
        return submit;
    }

    boolean sanityCheckRequiredSuccess(){
        boolean submit = true;
        for(int i=0; i<required_q.size(); i++){
            if(required_q.get(i).equals(true) && all_good.get(i).equals(false)){
                submit = false;
                break;
            }
        }
        return submit;
    }

    void promptUserResponse(){
        String toSpeak = getString(R.string.user_submit);
        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
        try {
            TimeUnit.SECONDS.sleep(10);
            promptSpeechInput(REQ_CODE_USER_SUBMIT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //return diceCoefficient(user_submit.toLowerCase(),"yes") >= diceCoefficient(user_submit.toLowerCase(),"no");
    }
    private void fillPDF(String file_path, String file_name) throws DocumentException {
        Document doc = new Document();
        try {
            PdfReader reader = new PdfReader(file_path);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/Filled_"+ file_name));
            AcroFields form  = stamper.getAcroFields();

            for(int i=0; i<filledValues.size();i++){

                String[] value = filledValues.get(i).split(":");
                Log.e("saving-> ",filledValues.get(i));
                form.setField(value[0],value[1]);
            }
            stamper.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    String modifyAndValidateInput(String s){
        if(q_type==Q_EMAIL){
            s = s.replaceAll("at the rate", "@");
            s = s.replaceAll("dot",".");
            s = s.replaceAll("underscore","_");
            s = s.replaceAll(" ","");
            s = s.toLowerCase();
        }
        else if(q_type == Q_PHONE){
            s = s.replaceAll("-","");
            s = s.replaceAll("to","2");
            s = s.replaceAll(" ","");
            boolean valid_num = s.matches("[0-9]+") && (s.length() == 10) || (s.charAt(0) == '0' && s.length() == 11 );
            if(!valid_num){
                Toast.makeText(getApplicationContext(), getString(R.string.invalid_input), Toast.LENGTH_SHORT).show();
                String toSpeak = getString(R.string.invalid_input);
                t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
        return s;
    }

    void validateInput(String s){
        if(s.length()==0){
           all_good.set(q_no,false);
        }
        else{
            boolean all_spaces = true;
            for(int i=0; i<s.length(); i++){
                if(s.charAt(i) != ' '){
                    all_spaces = false;
                    break;
                }
            }
            if(all_spaces){
               all_good.set(q_no,false);
            }
            else if(q_type==Q_PHONE){
                boolean valid = s.matches("[0-9]+") && (s.length() == 10) || (s.length() == 11 && s.charAt(0)=='0');
                if(!valid){
                  all_good.set(q_no,false);
                    Toast.makeText(getApplicationContext(), "Not a valid number!", Toast.LENGTH_LONG).show();
                }
                else{
                   all_good.set(q_no,true);
                }
            }
            else if(q_type==Q_EMAIL){
                boolean valid = Patterns.EMAIL_ADDRESS.matcher(s).matches();
                if(!valid){
                   all_good.set(q_no,false);
                    Toast.makeText(getApplicationContext(), "Not a valid email!", Toast.LENGTH_LONG).show();
                }
                else{
                   all_good.set(q_no,true);
                }
            }
            else{

            }
        }
        return;
    }


    private void readPDF(String file_path) throws IOException {
        Document doc = new Document();
        PdfReader reader = new PdfReader(file_path);
        AcroFields form  = reader.getAcroFields();
        HashMap<String,AcroFields.Item> fields = (HashMap<String, AcroFields.Item>) form.getFields();
        Set<Map.Entry<String, AcroFields.Item>> entrySet = fields.entrySet();
        int i=0;
        for (Map.Entry<String, AcroFields.Item> entry : entrySet) {
            String key = entry.getKey();
            TextView temp = new TextView(this);
            temp.setText(key);
            //ll.addView(temp);

            EditText temp_edit = new EditText(this);
            //ll.addView(temp_edit);
            key = key+": ";
            filledValues.add(key);
//            tv.add(temp);
//            ed.add(temp_edit);
            if(BaseField.REQUIRED==0){
                required_q.add(i,true);
            }
            else{
                required_q.add(i,false);
            }
            i++;
        }
        /*for(int j=0; j<i; j++){
            if(required_q.get(j).equals(true)){
                Log.d("yaay","q_no= "+j);
            }
        }*/
    }
//
//    private void saveDoc(File pdffile){
//        Document doc = new Document(PageSize.A4);
//        try {
//            PdfWriter writer =  PdfWriter.getInstance(doc, new FileOutputStream(pdffile));
//            writer.setPageEvent(new PdfPageEventHelper(){
//                @Override
//                public void onGenericTag(PdfWriter writer, Document document, Rectangle rect, String text) {
//                    TextField field = new TextField(writer, rect, text);
//                    try {
//                        writer.addAnnotation(field.getTextField());
//                    } catch (IOException ex) {
//                        throw new ExceptionConverter(ex);
//                    } catch (DocumentException ex) {
//                        throw new ExceptionConverter(ex);
//                    }
//                }
//            });
//
//        } catch (DocumentException e) {
//            e.printStackTrace();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        doc.open();
//        Paragraph p = new Paragraph();
//
//        for(int i=0; i<tv.size();i++){
//            p.add(tv.get(i).getText().toString());
//            Chunk day = new Chunk("            ");
//            day.setGenericTag(tv.get(i).getText().toString());
//            p.add(day);
//
//            p.add("\n");
//        }
//        try {
//            doc.add(p);
//        } catch (DocumentException e) {
//            e.printStackTrace();
//        }
//
////        Paragraph para = new Paragraph(tv.getText().toString()+" : " );
////        Paragraph para1 = new Paragraph(tv1.getText().toString()+" : " + ed1.getText().toString());
////        Paragraph para2 = new Paragraph(tv2.getText().toString()+" : " + ed2.getText().toString());
////        try {
////            doc.add(para);
////            doc.add(para1);
////            doc.add(para2);
////
////        } catch (DocumentException e) {
////            e.printStackTrace();
////        }
//        doc.close();
////        try {
////            readPDF();
////        } catch (DocumentException e) {
////            e.printStackTrace();
////        }
//    }

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
        touch=0;
        if(resultCode == RESULT_OK && data!=null){
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            s = result.get(0);

            if(requestCode == REQ_CODE_STATE_SPEECH_INPUT){
                sortStates(s);
                try {
                    populateCitySpinner((String) statelist.get(0));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(requestCode == REQ_CODE_CITY_SPEECH_INPUT){
                Log.e("Formfiller", s);
                sortCities(s);
            }
            else if(requestCode == REQ_CODE_USER_SUBMIT){
                boolean submit = diceCoefficient(s.toLowerCase(),"yes") >= diceCoefficient(s.toLowerCase(),"no");
                if(submit){
                    Log.d("Yahoo","response" + submit);
                    if(sanityCheckRequiredSuccess()){
                        try {
                            fillPDF(file_path,file_name);
                            finish();
                        } catch (DocumentException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        String toSpeak = getString(R.string.required_prompt);
                        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                    }

                }
            }
            else {
                c_ed.setText(modifyAndValidateInput(s));
            }

        }

    }
}
