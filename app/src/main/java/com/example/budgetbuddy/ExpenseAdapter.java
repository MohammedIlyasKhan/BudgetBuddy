package com.example.budgetbuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;

public class ExpenseAdapter extends ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder> {

    public ExpenseAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Expense> DIFF_CALLBACK = new DiffUtil.ItemCallback<Expense>() {
        @Override
        public boolean areItemsTheSame(@NonNull Expense oldItem, @NonNull Expense newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Expense oldItem, @NonNull Expense newItem) {
            return oldItem.getAmount() == newItem.getAmount()
                    && oldItem.getNote().equals(newItem.getNote())
                    && oldItem.getTimestamp() == newItem.getTimestamp();
        }
    };

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = getItem(position);
        holder.amountView.setText("₹" + expense.getAmount());
        holder.noteView.setText(expense.getNote());
        holder.dateView.setText(DateFormat.getDateInstance().format(new Date(expense.getTimestamp())));
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private TextView amountView;
        private TextView noteView;
        private TextView dateView;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            amountView = itemView.findViewById(R.id.amountText);
            noteView = itemView.findViewById(R.id.noteText);
            dateView = itemView.findViewById(R.id.dateTextView);
        }
    }
}
