package com.example.financialapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Dashboard_Fragment extends Fragment {

    private ProgressBar progressBarFood, progressBarUtility, progressBarHealthCare, progressBarOthers, progressBarExpense;
    private TextView valueFood, valueUtility, valuesHealthCare, valueOthers, valueTotalexpense, valueTotalIncome;
    private Button btn_setLimit;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef, budgetdbRef;

    private int foodbev = 0;
    private int utilities = 0;
    private int healthCare = 0;
    private int others = 0;

    private int foodPres;
    private int utilityPres;
    private int healthPres;
    private int otherPres;
    private int expensePres;

    private int foodCatAmount;
    private int otherCatAmount;
    private int utilityCatAmount;
    private int healthCatAmount;


    public Dashboard_Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference().child("Cashdata").child(mAuth.getCurrentUser().getUid());
        budgetdbRef = FirebaseDatabase.getInstance().getReference().child("BudgetLimit").child(mAuth.getCurrentUser().getUid());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_, container, false);

        btn_setLimit = view.findViewById(R.id.btn_setLimit);

        progressBarFood = view.findViewById(R.id.pro_foofBrev);
        progressBarHealthCare = view.findViewById(R.id.pro_healthCare);
        progressBarUtility = view.findViewById(R.id.pro_utility);
        progressBarOthers = view.findViewById(R.id.pro_other);
        progressBarExpense = view.findViewById(R.id.pro_totalExpense);

        valueOthers = view.findViewById(R.id.otherVale);
        valueFood = view.findViewById(R.id.foodbevValue);
        valuesHealthCare = view.findViewById(R.id.healthCareValue);
        valueUtility = view.findViewById(R.id.utilityValue);
        valueTotalexpense = view.findViewById(R.id.totalExpenseValue);
        valueTotalIncome = view.findViewById(R.id.TF_totalIncome);

        btn_setLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SetlimitActivity.class);
                startActivity(i);
            }
        });

        return view;
    }
    @Override
    public void onStart() {
        super.onStart();

        MyApplication myApp = (MyApplication) getActivity().getApplicationContext();
        int totalIncome = myApp.getTotalIncome();
        int totalExpense = myApp.getTotalExpense();

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalIncome = 0;
                int totalExpense = 0;

                String type;
                String categoryType;

                foodbev = 0;
                utilities = 0;
                healthCare = 0;
                others = 0;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Datacash datacash = snap.getValue(Datacash.class);

                    type = datacash.getType();
                    categoryType = datacash.getCategoryData();

                    if (type.equals("income")) {
                        totalIncome += datacash.getAmount();
                    }
                    if (type.equals("expense")) {
                        totalExpense += datacash.getAmount();
                    }

                    if (categoryType.equals("Comida & Bebidas")) {
                        foodbev += datacash.getAmount();
                    }
                    if (categoryType.equals("Utilidades")) {
                        utilities += datacash.getAmount();
                    }
                    if (categoryType.equals("Cuidados de saúde")) {
                        healthCare += datacash.getAmount();
                    }
                    if (categoryType.equals("Outros")) {
                        others += datacash.getAmount();
                    }
                }

                if (totalExpense != 0) {
                    foodPres = foodbev * 100 / totalExpense;
                    utilityPres = utilities * 100 / totalExpense;
                    healthPres = healthCare * 100 / totalExpense;
                    otherPres = others * 100 / totalExpense;
                } else {
                    // Lógica de tratamento quando totalExpense for igual a zero
                }

                if (totalIncome != 0) {
                    expensePres = totalExpense * 100 / totalIncome;
                } else {
                    // Lógica de tratamento quando totalIncome for igual a zero
                }

                int finalTotalIncome = totalIncome;
                int finalTotalExpense = totalExpense;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        valueTotalexpense.setText(expensePres + "% | R$ " + finalTotalExpense + ".00");
                        valueTotalIncome.setText("R$ " + finalTotalIncome + ".00");

                        progressBarFood.setProgress(foodPres);
                        progressBarHealthCare.setProgress(healthPres);
                        progressBarOthers.setProgress(otherPres);
                        progressBarUtility.setProgress(utilityPres);
                        progressBarExpense.setProgress(expensePres);

                        budgetdbRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Atualizar os elementos da UI com os valores corretos
                                        String foodPresText = foodPres != 0 ? String.valueOf(foodPres) : "N/A";
                                        String otherPresText = otherPres != 0 ? String.valueOf(otherPres) : "N/A";
                                        String healthPresText = healthPres != 0 ? String.valueOf(healthPres) : "N/A";
                                        String utilityPresText = utilityPres != 0 ? String.valueOf(utilityPres) : "N/A";

                                        valueFood.setText(foodPresText + "% | R$ " + foodbev + ".00 | Limit - " + foodCatAmount);
                                        valueOthers.setText(otherPresText + "% | R$ " + others + ".00 | Limit - " + otherCatAmount);
                                        valuesHealthCare.setText(healthPresText + "% | R$ " + healthCare + ".00 | Limit - " + healthCatAmount);
                                        valueUtility.setText(utilityPresText + "% | R$ " + utilities + ".00 | Limit - " + utilityCatAmount);
                                    }
                                });


                                String categoryType;
                                int amountCategory;

                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    BudgetLimit budgetLimit = dataSnapshot.getValue(BudgetLimit.class);

                                    categoryType = budgetLimit.getCategory();

                                    if (categoryType.equals("food")) {
                                        amountCategory = budgetLimit.getAmount();
                                        valueFood.setText(foodPres + "% | R$ " + foodbev + ".00 | Limit - " + amountCategory);
                                    }
                                    if (categoryType.equals("other")) {
                                        amountCategory = budgetLimit.getAmount();
                                        valueOthers.setText(otherPres + "% | R$ " + others + ".00 | Limit - " + amountCategory);
                                    }
                                    if (categoryType.equals("health")) {
                                        amountCategory = budgetLimit.getAmount();
                                        valuesHealthCare.setText(healthPres + "% | R$ " + healthCare + ".00 | Limit - " + amountCategory);
                                    }
                                    if (categoryType.equals("utility")) {
                                        amountCategory = budgetLimit.getAmount();
                                        valueUtility.setText(utilityPres + "% | R$ " + utilities + ".00 | Limit - " + amountCategory);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Lógica de tratamento de erro
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Lógica de tratamento de erro
            }
        });
    }
}
