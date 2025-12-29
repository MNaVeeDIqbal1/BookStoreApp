package com.nla.bookstoreapp;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // Added TextView import

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // Added FirebaseUser import
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private List<Book> allBooksList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // --- ADDED: HEADER VIEW LOGIC TO SHOW USERNAME ---
        View headerView = navigationView.getHeaderView(0);
        TextView tvNavUsername = headerView.findViewById(R.id.tvNavUsername);
        TextView tvNavEmail = headerView.findViewById(R.id.tvNavEmail);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            tvNavEmail.setText(email);
            // Extracts name from email (e.g., john@gmail.com -> john)
            String name = (email != null && email.contains("@")) ? email.split("@")[0] : "User";
            // Capitalize first letter
            String capitalizedName = name.substring(0, 1).toUpperCase() + name.substring(1);
            tvNavUsername.setText(capitalizedName);
        }
        // ------------------------------------------------

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter(allBooksList, new BookAdapter.OnBookActionListener() {
            @Override
            public void onDeleteClick(Book book) { hideSingleBook(book); }

            @Override
            public void onEditClick(Book book) { showBookDialog(book); }
        });
        recyclerView.setAdapter(adapter);

        loadBooksFromFirebase();
        setupSearch();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_add_book) showBookDialog(null);
            else if (id == R.id.nav_delete_account) confirmAndDeleteAccount();
            else if (id == R.id.nav_logout) {
                mAuth.signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void setupSearch() {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) { return false; }
            @Override
            public boolean onQueryTextChange(String q) { filter(q); return true; }
        });
    }

    private void loadBooksFromFirebase() {
        db.collection("books").whereEqualTo("isPublic", true)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        allBooksList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            allBooksList.add(new Book(
                                    doc.getId(),
                                    doc.getString("title"),
                                    doc.getString("author"),
                                    doc.getString("isbn"),
                                    doc.getString("year"),
                                    doc.getString("ownerId")
                            ));
                        }
                        adapter.setFilteredList(new ArrayList<>(allBooksList));
                    }
                });
    }

    private void showBookDialog(Book bookToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_book, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        EditText etTitle = view.findViewById(R.id.etDialogTitle);
        EditText etISBN = view.findViewById(R.id.etDialogISBN);
        EditText etYear = view.findViewById(R.id.etDialogYear);
        Button btnSave = view.findViewById(R.id.btnSaveBook);
        Button btnPaste = view.findViewById(R.id.btnPaste);

        if (bookToEdit != null) {
            etTitle.setText(bookToEdit.getTitle());
            etISBN.setText(bookToEdit.getIsbn());
            etYear.setText(bookToEdit.getYear());
            btnSave.setText("Update Book");
        }

        btnPaste.setOnClickListener(v -> {
            ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cb != null && cb.hasPrimaryClip()) etTitle.setText(cb.getPrimaryClip().getItemAt(0).getText().toString());
        });

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) return;

            Map<String, Object> data = new HashMap<>();
            data.put("title", title);
            data.put("author", mAuth.getCurrentUser().getEmail());
            data.put("isbn", etISBN.getText().toString().isEmpty() ? "AUTO-"+new Random().nextInt(9999) : etISBN.getText().toString());
            data.put("year", etYear.getText().toString());
            data.put("isPublic", true);
            data.put("ownerId", mAuth.getUid());

            if (bookToEdit == null) {
                db.collection("books").add(data);
            } else {
                db.collection("books").document(bookToEdit.getId()).update(data);
            }
            dialog.dismiss();
        });
        dialog.show();
    }

    private void hideSingleBook(Book book) {
        new AlertDialog.Builder(this).setTitle("Delete?").setPositiveButton("Yes", (d,w) ->
                db.collection("books").document(book.getId()).update("isPublic", false)).show();
    }

    private void confirmAndDeleteAccount() {
        new AlertDialog.Builder(this).setTitle("Delete Account?").setPositiveButton("Yes", (d,w) -> {
            String uid = mAuth.getUid();
            db.collection("books").whereEqualTo("ownerId", uid).get().addOnSuccessListener(shots -> {
                for (DocumentSnapshot doc : shots) doc.getReference().update("isPublic", false);
                mAuth.getCurrentUser().delete().addOnCompleteListener(t -> {
                    startActivity(new Intent(this, MainActivity.class)); finish();
                });
            });
        }).show();
    }

    private void filter(String text) {
        List<Book> filtered = new ArrayList<>();
        for (Book b : allBooksList) {
            if (b.getTitle().toLowerCase().contains(text.toLowerCase())) filtered.add(b);
        }
        adapter.setFilteredList(filtered);
    }
}