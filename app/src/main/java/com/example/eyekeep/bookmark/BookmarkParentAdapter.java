package com.example.eyekeep.bookmark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eyekeep.DTO.BookMarkDTO;
import com.example.eyekeep.R;

import java.util.List;

import lombok.NonNull;

public class BookmarkParentAdapter extends RecyclerView.Adapter<BookmarkParentAdapter.BookmarkViewHolder> {

    private final List<BookMarkDTO> bookmarkList;
    private final RequestParentBookMark requestBookMark;
    private final Context context;

    public BookmarkParentAdapter(List<BookMarkDTO> bookmarkList, RequestParentBookMark requestBookMark, Context context) {
        this.bookmarkList = bookmarkList;
        this.requestBookMark = requestBookMark;
        this.context = context;
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item, parent, false);
        return new BookmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        BookMarkDTO currentBookmark = bookmarkList.get(position);
        if(currentBookmark.getAlias() != null) {
            holder.bookmarkName.setText(currentBookmark.getAlias());
        }
        else {
            holder.bookmarkName.setText(currentBookmark.getLocationName());
        }

        holder.editButton.setOnClickListener(v -> {
            showEditDialog(currentBookmark);
        });

        holder.deleteButton.setOnClickListener(v -> {
            String locationName = currentBookmark.getLocationName();
            requestBookMark.deleteBookmarkFromServer(locationName); // 북마크 삭제
        });
    }

    @Override
    public int getItemCount() {
        return bookmarkList.size();
    }   // 수정, 삭제

    public static class BookmarkViewHolder extends RecyclerView.ViewHolder {
        TextView bookmarkName;
        AppCompatButton editButton, deleteButton;

        public BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            bookmarkName = itemView.findViewById(R.id.bookmark_name);
            editButton = itemView.findViewById(R.id.btn_bookmark_edit);
            deleteButton = itemView.findViewById(R.id.btn_bookmark_delete);
        }
    }

    private void showEditDialog(BookMarkDTO bookmark) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("북마크 이름 수정");

        final EditText input = new EditText(context);
        input.setSelection(input.getText().length());
        if (bookmark.getAlias() != null) {
            input.setText(bookmark.getAlias());
        }
        else {
            input.setText(bookmark.getLocationName());  // 기존 이름을 불러와서 표시
        }
        builder.setView(input);

        builder.setPositiveButton("수정", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                bookmark.setAlias(newName);
                requestBookMark.updateBookmarkAliasFromServer(bookmark);  // 서버와 로컬 리스트에서 북마크 이름 수정
            }
            else {
                Toast.makeText(context, "별칭을 설정해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());
        builder.show();
    }


}
