package com.nla.bookstoreapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> bookList;
    private OnBookActionListener actionListener;
    public interface OnBookActionListener {
        void onDeleteClick(Book book);
        void onEditClick(Book book);
    }

    public BookAdapter(List<Book> bookList, OnBookActionListener actionListener) {
        this.bookList = bookList;
        this.actionListener = actionListener;
    }

    public void setFilteredList(List<Book> filteredList) {
        this.bookList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        String currentUid = FirebaseAuth.getInstance().getUid();

        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText("By: " + book.getAuthor());
        holder.tvISBN.setText("Serial: " + book.getIsbn() + " | Year: " + book.getYear());

        // ONLY SHOW BUTTONS IF LOGGED-IN USER IS THE OWNER
        if (book.getOwnerId() != null && book.getOwnerId().equals(currentUid)) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnEdit.setVisibility(View.VISIBLE);
        } else {
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.GONE);
        }

        holder.btnDelete.setOnClickListener(v -> actionListener.onDeleteClick(book));
        holder.btnEdit.setOnClickListener(v -> actionListener.onEditClick(book));
    }
    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvID, tvISBN, tvAuthor;
        ImageButton btnDelete, btnEdit;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBookTitle);
            tvID = itemView.findViewById(R.id.tvBookID);
            tvISBN = itemView.findViewById(R.id.tvBookISBN);
            tvAuthor = itemView.findViewById(R.id.tvBookAuthor);
            btnDelete = itemView.findViewById(R.id.btnDeleteBook);
            btnEdit = itemView.findViewById(R.id.btnEditBook);
        }
    }
}