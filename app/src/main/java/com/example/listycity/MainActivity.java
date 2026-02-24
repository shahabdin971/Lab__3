package com.example.listycity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements
        AddCityFragment.AddCityDialogListener {
    private FirebaseFirestore db;
    private CollectionReference citiesRef;
    private ArrayList<City> dataList;
    private ListView cityList;
    private CityArrayAdapter cityAdapter;
    @Override
    public void addCity(City city) {

        dataList.add(city);
        cityAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.set(city);
    }

    public void deleteCity(City city) {

        // 1. Delete from Firestore
        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "City deleted"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error deleting city", e));

        // 2. (Optional) Immediately update UI – snapshot listener will also fix it
        dataList.remove(city);
        cityAdapter.notifyDataSetChanged();
    }

    @Override
    public void editCity(int position, String newCityName, String newProvinceName) {
        City city = dataList.get(position);
        city.setName(newCityName);
        city.setProvince(newProvinceName);
        cityAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_main);

        dataList = new ArrayList<>();

        cityList = findViewById(R.id.city_list);
        cityAdapter = new CityArrayAdapter(this, dataList);
        cityList.setAdapter(cityAdapter);
        cityList.setOnItemLongClickListener((parent, view, position, id) -> {
            City selectedCity = dataList.get(position);

            new AlertDialog.Builder(this)
                    .setTitle("Delete city")
                    .setMessage("Delete " + selectedCity.getName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteCity(selectedCity);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            return true; // we handled the long click
        });

        cityList.setOnItemClickListener((parent, view, position, id) -> {
            City selected = dataList.get(position);
            AddCityFragment dialog = AddCityFragment.newInstance(selected, position);
            dialog.show(getSupportFragmentManager(), "Edit City");
        });


        FloatingActionButton fab = findViewById(R.id.button_add_city);
        fab.setOnClickListener(v -> {
            new AddCityFragment().show(getSupportFragmentManager(), "Add City");
        });
        db = FirebaseFirestore.getInstance();
        citiesRef= db.collection(("cities"));
        citiesRef.addSnapshotListener((value, error) -> {

            if (error != null) {
                Log.e("Firestore", error.toString());
                return;
            }

            if (value != null && !value.isEmpty()) {

                dataList.clear();

                for (QueryDocumentSnapshot snapshot : value) {

                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");

                    dataList.add(new City(name, province));
                }

                cityAdapter.notifyDataSetChanged();
            }
        });
    }
}
