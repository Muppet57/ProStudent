package com.student.pro.prostudent.Comparators;

import android.util.Log;

import com.student.pro.prostudent.Objects.Notes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by jonnh on 3/27/2018.
 */

public class CustomCompareNotes implements Comparator<Notes> {
    @Override
    public int compare(Notes notes, Notes t1) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        Date x1 = null;
        Date x2 = null;

        try {
            x1 = sdf.parse(notes.getDate().toString());
            x2 = sdf.parse(t1.getDate().toString());
            Log.d("cnc", "compare: deu");
            return x1.compareTo(x2);
        } catch (ParseException e) {

            e.printStackTrace();
            Log.d("cnc", "compare: 0");
            return 0;
        }


    }
}
