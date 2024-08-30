package com.example.eyekeep.bookmark;

import android.util.Log;

import com.example.eyekeep.DTO.BookMarkDTO;
import com.example.eyekeep.MainChildActivity;
import com.example.eyekeep.repository.BookmarkViewModel;
import com.example.eyekeep.request.RequestAccessIsActive;
import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.service.BookMarkService;
import com.example.eyekeep.repository.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestChildBookMark {
    private final BookmarkViewModel bookmarkViewModel;
    private final MainChildActivity mainActivity;

    public RequestChildBookMark(MainChildActivity mainActivity, BookmarkViewModel bookmarkViewModel) {
        this.mainActivity = mainActivity;
        this.bookmarkViewModel = bookmarkViewModel;
    }

    public void getBookMarkList() {
        RequestAccessIsActive.checkAccessToken();
        String accessToken = Utils.getAccessToken(null);
        BookMarkService service = RetrofitClient.getRetrofitInstance().create(BookMarkService.class);
        Call<List<BookMarkDTO>> call = service.getBookMarkList("Bearer " + accessToken);

        call.enqueue(new Callback<List<BookMarkDTO>>() {
            @Override
            public void onResponse(Call<List<BookMarkDTO>> call, Response<List<BookMarkDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("GetBookMarkList", "북마크 리스트 요청 성공, 데이터 수신: " + response.body().size() + " 개");
                    List<BookMarkDTO> bookmarkList = response.body();
                    bookmarkViewModel.setBookmarkList(bookmarkList);  // ViewModel에 데이터 설정
                    mainActivity.addBookmarkMarkersOnMap(bookmarkList);  // MainActivity의 메서드 호출
                    mainActivity.setBookmarkAdapter();
                } else {
                    // 에러 처리
                    int statusCode = response.code();
                    if (statusCode == 400) {
                        List<BookMarkDTO> emptyList = new ArrayList<>();
                        bookmarkViewModel.setBookmarkList(emptyList);
                        mainActivity.setBookmarkAdapter();
                        return;
                    }
                    Log.e("GetBookMarkList", "북마크 리스트 요청 실패: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<BookMarkDTO>> call, Throwable t) {
                // 네트워크 에러 처리
                Log.e("GetBookMarkList", "북마크 리스트 요청 실패 (네트워크 오류): " + t.getMessage());
            }
        });
    }

    // 북마크 서버에 저장
    public void saveBookmarkToServer(BookMarkDTO bookmark) {
        RequestAccessIsActive.checkAccessToken();
        String accessToken = Utils.getAccessToken(null);
        BookMarkService service = RetrofitClient.getRetrofitInstance().create(BookMarkService.class);
        Call<BookMarkDTO> call = service.saveBookMark("Bearer " + accessToken, bookmark);
        call.enqueue(new Callback<BookMarkDTO>() {
            @Override
            public void onResponse(Call<BookMarkDTO> call, Response<BookMarkDTO> response) {
                if (response.isSuccessful()) {
                    // 성공적으로 저장됨
                    BookMarkDTO responseBookmark = response.body();
                    if (responseBookmark == null) {
                        Log.e("saveBookmarkToServer", "Body is null.");
                    }
                    bookmarkViewModel.addBookmark(responseBookmark);
                    mainActivity.updateBookmarkList();
                    mainActivity.addAdditionalBookmarkMarkersOnMap(responseBookmark);
                    Log.d("saveBookmarkToServer", "success");
                } else {
                    // 에러 처리
                    Log.d("saveBookmarkToServer", "fail");
                }
            }

            @Override
            public void onFailure(Call<BookMarkDTO> call, Throwable t) {
                // 네트워크 에러 처리
                Log.d("saveBookmarkToServer", "네트워크 오류");
            }
        });
    }

    // 서버에서 북마크 삭제
    public void deleteBookmarkFromServer(String locationName) {
        RequestAccessIsActive.checkAccessToken();
        String accessToken = Utils.getAccessToken(null); // 실제 토큰 가져오기
        BookMarkService service = RetrofitClient.getRetrofitInstance().create(BookMarkService.class);
        Call<Void> call = service.deleteBookMark("Bearer " + accessToken, new BookMarkDTO(locationName, 0, 0)); // 생성자 호출에 필요한 파라미터를 맞추기 위해 임시로 0, 0 사용
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // 성공적으로 삭제됨
                    bookmarkViewModel.removeBookmark(locationName);
                    mainActivity.updateBookmarkList();
                    mainActivity.deleteBookmarkMarkersOnMap(locationName);
                    Log.d("deleteBookmarkFromServer", "success");
                } else {
                    // 에러 처리
                    Log.d("deleteBookmarkFromServer", "fail");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // 네트워크 에러 처리
                Log.d("deleteBookmarkFromServer", "네트워크 오류");
            }
        });
    }

    public void updateBookmarkAliasFromServer(BookMarkDTO bookmark) {
        RequestAccessIsActive.checkAccessToken();
        String accessToken = Utils.getAccessToken(null); // 실제 토큰 가져오기
        BookMarkService service = RetrofitClient.getRetrofitInstance().create(BookMarkService.class);
        Call<BookMarkDTO> call = service.setAlias("Bearer " + accessToken, bookmark);
        call.enqueue(new Callback<BookMarkDTO>() {
            @Override
            public void onResponse(Call<BookMarkDTO> call, Response<BookMarkDTO> response) {
                if (response.isSuccessful()) {
                    // 성공적으로 수정된 경우 로컬 리스트에도 반영
                    BookMarkDTO responseBookmark = response.body();
                    if (responseBookmark == null) {
                        Log.e("updateBookmarkAliasFromServer", "Body is null.");
                    }
                    String locationName = responseBookmark.getLocationName();
                    List<BookMarkDTO> currentList = bookmarkViewModel.getBookmarkList().getValue();
                    for (BookMarkDTO item : currentList) {
                        if (item.getLocationName().equals(locationName)) {  // locationName으로 식별하여 수정
                            item.setLocationName(locationName);
                            break;
                        }
                    }
                    bookmarkViewModel.setBookmarkList(currentList); // 업데이트된 리스트를 LiveData에 설정
                    mainActivity.updateBookmarkList();
                    mainActivity.updateBookmarkMarkersOnMap(responseBookmark);
                } else {
                    // 에러 처리
                    Log.e("updateBookmarkAliasFromServer", "Failed.");
                }
            }

            @Override
            public void onFailure(Call<BookMarkDTO> call, Throwable t) {
                // 네트워크 에러 처리
                Log.e("updateBookmarkAliasFromServer", "Network error.");
            }
        });
    }
}
