package com.example.budgetbuddy;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ExpenseRepository {
    private final ExpenseDao expenseDao;
    private final LiveData<List<Expense>> allExpenses;

    public ExpenseRepository(Application application) {
        ExpenseDatabase database = ExpenseDatabase.getInstance(application);
        expenseDao = database.expenseDao();
        allExpenses = expenseDao.getAllExpenses();
    }

    public void insert(Expense expense) {
        ExpenseDatabase.databaseWriteExecutor.execute(() -> expenseDao.insert(expense));
    }

    public LiveData<List<Expense>> getAllExpenses() {
        return allExpenses;
    }
}
