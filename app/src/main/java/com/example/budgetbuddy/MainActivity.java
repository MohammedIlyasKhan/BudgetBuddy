package com.example.budgetbuddy;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private EditText expenseAmount;
    private EditText expenseNote;
    private Button addExpenseButton, exportCsvBtn, exportPdfBtn;
    private RecyclerView expenseRecyclerView;
    private TextView totalText;
    private Spinner filterSpinner;
    private BarChart barChart;

    private ExpenseAdapter adapter;
    private ExpenseViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views
        expenseAmount = findViewById(R.id.expenseAmount);
        expenseNote = findViewById(R.id.expenseNote);
        addExpenseButton = findViewById(R.id.addExpenseButton);
        expenseRecyclerView = findViewById(R.id.expenseRecyclerView);
        totalText = findViewById(R.id.totalText);
        filterSpinner = findViewById(R.id.filterSpinner);
        barChart = findViewById(R.id.barChart);

        // Export buttons in your XML
        exportCsvBtn = findViewById(R.id.exportCsvBtn);
        exportPdfBtn = findViewById(R.id.exportPdfBtn);

        // RecyclerView
        adapter = new ExpenseAdapter();
        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseRecyclerView.setAdapter(adapter);

        // ViewModel + data
        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        viewModel.getAllExpenses().observe(this, expenses -> {
            adapter.submitList(expenses);
            updateTotal(expenses);
            updateChart(expenses);
        });

        // Add expense
        addExpenseButton.setOnClickListener(v -> {
            String amountStr = expenseAmount.getText().toString().trim();
            String noteStr = expenseNote.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(MainActivity.this, "Enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                Expense expense = new Expense(amount, noteStr, System.currentTimeMillis());
                viewModel.insert(expense);

                expenseAmount.setText("");
                expenseNote.setText("");
                hideKeyboard();
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Invalid number", Toast.LENGTH_SHORT).show();
            }
        });

        // Filter spinner
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // just trigger chart refresh using current LiveData
                List<Expense> current = adapter.getCurrentList();
                updateTotal(current);
                updateChart(current);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Exports
        exportCsvBtn.setOnClickListener(v ->
                ExportUtils.exportToCSV(this, adapter.getCurrentList())
        );
        exportPdfBtn.setOnClickListener(v ->
                ExportUtils.exportToPDF(this, adapter.getCurrentList())
        );
    }

    private void updateTotal(List<Expense> expenses) {
        double total = 0;
        for (Expense e : expenses) total += e.getAmount();
        totalText.setText("Total: ₹" + total);
    }

    private void updateChart(List<Expense> expenses) {
        if (expenses == null) return;

        String filter = filterSpinner.getSelectedItem() != null
                ? filterSpinner.getSelectedItem().toString()
                : "Daily";

        Map<String, Double> groupedData = new TreeMap<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df;

        for (Expense e : expenses) {
            cal.setTimeInMillis(e.getTimestamp());
            String key;
            switch (filter) {
                case "Weekly":
                    key = "Week " + cal.get(Calendar.WEEK_OF_YEAR) + ", " + cal.get(Calendar.YEAR);
                    break;
                case "Monthly":
                    df = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
                    key = df.format(cal.getTime());
                    break;
                case "Daily":
                default:
                    df = new SimpleDateFormat("dd MMM", Locale.getDefault());
                    key = df.format(cal.getTime());
                    break;
            }
            groupedData.put(key, groupedData.getOrDefault(key, 0.0) + e.getAmount());
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Double> it : groupedData.entrySet()) {
            entries.add(new BarEntry(i, it.getValue().floatValue()));
            labels.add(it.getKey());
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, filter + " Expenses");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        // Use the simplest, version-safe formatter (1-arg)
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "₹" + Math.round(value);
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        barChart.setData(data);
        barChart.animateY(800);
        barChart.getDescription().setEnabled(false);

        XAxis x = barChart.getXAxis();
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setGranularity(1f);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setLabelRotationAngle(-30f);

        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);

        Legend legend = barChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);

        barChart.invalidate();
    }

    private void hideKeyboard() {
        View v = getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}

