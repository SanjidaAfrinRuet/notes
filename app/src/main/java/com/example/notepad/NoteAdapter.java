package com.example.notepad;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.Random;
import android.graphics.Color;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private final Context ctx;
    private ArrayList<Note> notes;

    public NoteAdapter(Context ctx, ArrayList<Note> notes) {
        this.ctx = ctx;
        this.notes = new ArrayList<>(notes);
    }

    public void updateList(ArrayList<Note> newList) {
        this.notes = new ArrayList<>(newList);  // Make a copy to avoid list mutation issues
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int pos) {
        Note note = notes.get(pos);
        holder.tvTitle.setText(note.getTitle());
        holder.tvDescription.setText(note.getDescription());
        holder.tvReminder.setText(
                (note.getReminderTime() != null && !note.getReminderTime().isEmpty()) ?
                        "⏰ " + note.getReminderTime() : ""
        );

        // Set a random background color for variety
        holder.itemView.setBackgroundColor(note.getColor());

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ctx, EditNoteActivity.class);
            intent.putExtra("id", note.getId());
            ctx.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            NoteDatabaseHelper db = new NoteDatabaseHelper(ctx);
            db.deleteNote(note.getId());
            notes.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());
            Toast.makeText(ctx, "Note deleted", Toast.LENGTH_SHORT).show();
        });
    }


    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvReminder;
        ImageView btnEdit, btnDelete;

        public NoteViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvReminder = itemView.findViewById(R.id.tvReminder);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
