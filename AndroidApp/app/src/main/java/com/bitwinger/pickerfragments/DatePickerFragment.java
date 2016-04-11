package com.bitwinger.pickerfragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Nimesh on 03-02-2016.
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener
        , DatePickerDialog.OnClickListener{

    private DatePickerDialog datePickerDialog=null;
    private CallbackHandler callbackHandler = null;

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE){

        }
    }

    public interface CallbackHandler{
        void setDate(DatePicker view, int year, int month, int day);
    }

    public void setCallbackHandler(CallbackHandler callbackHandler){
        this.callbackHandler = callbackHandler;
    }

    public DatePickerDialog getDatePickerDialog(){
        return datePickerDialog;
    }

    public void createPickerDialog(Activity activity){
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        // Create a new instance of DatePickerDialog and return it
        datePickerDialog = new DatePickerDialog(activity, this, year, month, day);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return datePickerDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        if (callbackHandler!=null){
            callbackHandler.setDate(view, year, month, day);
        }
    }
}