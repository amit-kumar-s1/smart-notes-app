package com.example.smartnotes;

import android.app.AlertDialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NoteAdapter
        extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private ArrayList<Note> noteList;

    private OnNoteClickListener listener;

    public interface OnNoteClickListener {

        void onNoteClick(int position);

        void onNoteLongClick(int position);
    }

    public NoteAdapter(
            ArrayList<Note> noteList,
            OnNoteClickListener listener) {

        this.noteList = noteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(
                parent.getContext()
        ).inflate(
                R.layout.item_note,
                parent,
                false
        );

        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull NoteViewHolder holder,
            int position) {

        Note note = noteList.get(position);

        holder.txtTitle.setText(note.title);

        holder.txtContent.setText(note.content);

        holder.txtDate.setText(note.date);

        // Different colors
        int[] colors = {

                Color.rgb(255, 236, 179),

                Color.rgb(220, 235, 255),

                Color.rgb(225, 245, 225),

                Color.rgb(245, 220, 255),

                Color.rgb(255, 225, 225)
        };

        int color =
                colors[position % colors.length];

        holder.noteCard.setCardBackgroundColor(color);

        // Normal click
        holder.itemView.setOnClickListener(v -> {

            int currentPosition =
                    holder.getAdapterPosition();

            if (currentPosition !=
                    RecyclerView.NO_POSITION) {

                listener.onNoteClick(
                        currentPosition
                );
            }
        });

        // Long click
        holder.itemView.setOnLongClickListener(v -> {

            int currentPosition =
                    holder.getAdapterPosition();

            if (currentPosition !=
                    RecyclerView.NO_POSITION) {

                listener.onNoteLongClick(
                        currentPosition
                );
            }

            return true;
        });
    }

    @Override
    public int getItemCount() {

        return noteList.size();
    }

    public static class NoteViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtTitle;
        TextView txtContent;
        TextView txtDate;
        TextView txtFavorite;

        CardView noteCard;

        public NoteViewHolder(
                @NonNull View itemView) {

            super(itemView);

            txtTitle =
                    itemView.findViewById(
                            R.id.txtNoteTitle
                    );

            txtContent =
                    itemView.findViewById(
                            R.id.txtNoteContent
                    );

            txtDate =
                    itemView.findViewById(
                            R.id.txtNoteDate
                    );

            txtFavorite =
                    itemView.findViewById(
                            R.id.txtFavorite
                    );

            noteCard =
                    itemView.findViewById(
                            R.id.noteCard
                    );
        }
    }
}