package com.example.eyekeep.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eyekeep.DTO.BookMarkDTO;
import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.service.BookMarkService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookmarkViewModel extends ViewModel {
    private final MutableLiveData<List<BookMarkDTO>> bookmarkList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isProcessing = new MutableLiveData<>(false);

    // 북마크 리스트를 LiveData로 반환
    public LiveData<List<BookMarkDTO>> getBookmarkList() {
        return bookmarkList;
    }


    // 북마크 리스트를 설정하는 메서드
    public void setBookmarkList(List<BookMarkDTO> bookmarks) {
        bookmarkList.setValue(bookmarks);
    }

    // 현재 장소가 북마크에 등록되어 있는지 확인하는 메서드
    public boolean isBookmarked(String locationName) {
        if (bookmarkList.getValue() != null) {
            for (BookMarkDTO bookmark : bookmarkList.getValue()) {
                if (bookmark.getLocationName().equals(locationName)) {
                    return true;
                }
            }
        }
        return false;
    }

    // 현재 북마크 처리 중인지 상태를 반환하는 메서드
    public LiveData<Boolean> getIsProcessing() {
        return isProcessing;
    }

    // 서버에서 북마크 리스트를 가져오는 메서드
    public void loadBookmarksFromServer() {
        String accessToken = Utils.getAccessToken(null); // 실제 토큰 가져오기
        BookMarkService service = RetrofitClient.getRetrofitInstance().create(BookMarkService.class);
        Call<List<BookMarkDTO>> call = service.getBookMarkList("Bearer " + accessToken);
        call.enqueue(new Callback<List<BookMarkDTO>>() {
            @Override
            public void onResponse(Call<List<BookMarkDTO>> call, Response<List<BookMarkDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bookmarkList.setValue(response.body());
                } else {
                    // 에러 처리
                }
            }

            @Override
            public void onFailure(Call<List<BookMarkDTO>> call, Throwable t) {
                // 네트워크 에러 처리
            }
        });
    }

    public void addBookmark(BookMarkDTO newBookmark) {
        List<BookMarkDTO> currentList = bookmarkList.getValue();
        if (currentList != null) {
            currentList.add(newBookmark);
            bookmarkList.setValue(currentList);
        }
    }

    // 북마크를 제거하는 메서드
    public void removeBookmark(String locationName) {
        if (bookmarkList.getValue() != null) {
            List<BookMarkDTO> currentList = new ArrayList<>(bookmarkList.getValue());
            currentList.removeIf(mark -> mark.getLocationName().equals(locationName));
            bookmarkList.setValue(currentList);
        }
    }
}
