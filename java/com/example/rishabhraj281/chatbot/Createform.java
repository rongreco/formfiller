package com.example.rishabhraj281.chatbot;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.TextField;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Createform extends AppCompatActivity {

    ArrayList<EditText> ed = new ArrayList<EditText>();
    EditText formname;
    Button b1,b2;
    LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createform);

        ll = (LinearLayout) findViewById(R.id.ll_form);
        formname = (EditText) findViewById(R.id.form_name);
        b1 = (Button) findViewById(R.id.add_field);
        b2 = (Button) findViewById(R.id.createform);


        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText temp_edit = new EditText(Createform.this);
                ll.addView(temp_edit);

                ed.add(temp_edit);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createform();
                finish();

            }
        });
    }

    private void createform(){
        Document doc = new Document(PageSize.A4);
        try {
            PdfWriter writer =  PdfWriter.getInstance(doc, new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+formname.getText().toString()+".pdf"));
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

        for(int i=0; i<ed.size();i++){
            p.add(ed.get(i).getText().toString()+" : ");
            Chunk day = new Chunk("                                                       ");

            day.setGenericTag(ed.get(i).getText().toString());
            p.add(day);
            p.add("\n");
        }
        try {
            doc.add(p);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        doc.close();
    }
}
