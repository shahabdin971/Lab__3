package com.example.listycity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AddCityFragment extends DialogFragment {

    private City editingCity = null;
    private int editingPos = -1;

    private static final String ARG_CITY = "arg_city";
    private static final String ARG_POSITION = "arg_position";

    interface AddCityDialogListener {
        void addCity(City city);
        void editCity(int position, String newCityName, String newProvinceName);
    }

    private AddCityDialogListener listener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AddCityDialogListener) {
            listener = (AddCityDialogListener) context;
        } else {
            throw new RuntimeException(context + " must implement AddCityDialogListener");
        }
    }
    public static AddCityFragment newInstance(City city, int position) {
        AddCityFragment fragment = new AddCityFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CITY, city);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view =
                LayoutInflater.from(getContext()).inflate(R.layout.fragment_add_city, null);
        EditText editCityName = view.findViewById(R.id.edit_text_city_text);
        EditText editProvinceName = view.findViewById(R.id.edit_text_province_text);


        Bundle args = getArguments();
        if (args != null) {
            editingCity = (City) args.getSerializable(ARG_CITY);
            editingPos = args.getInt(ARG_POSITION, -1);
        }

        if (editingCity != null) {
            editCityName.setText(editingCity.getName());
            editProvinceName.setText(editingCity.getProvince());
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        boolean isEditing = (editingCity != null && editingPos != -1);

        return builder
                .setView(view)
                .setTitle("Add/edit city")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", (dialog, which) -> {
                    String cityName = editCityName.getText().toString().trim();
                    String provinceName = editProvinceName.getText().toString().trim();

                    if (isEditing) {
                        listener.editCity(editingPos, cityName, provinceName);
                    } else {
                        listener.addCity(new City(cityName, provinceName));
                    }
                })
                .create();

    }
}

