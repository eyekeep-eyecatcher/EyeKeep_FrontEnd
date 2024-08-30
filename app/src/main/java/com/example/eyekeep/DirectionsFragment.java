package com.example.eyekeep;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eyekeep.DTO.FindPathDTO;

import java.util.ArrayList;
import java.util.List;

public class DirectionsFragment extends Fragment {

    private EditText etDeparture;
    private EditText etArrival;
    private ListView searchList;
    private Button findDirectionButton2;
    private ArrayAdapter<String> adapter;
    private final List<String> suggestions = new ArrayList<>();
    private final List<FindPathDTO> searchDepartureResult = new ArrayList<>();
    private final List<FindPathDTO> searchArrivalResult = new ArrayList<>();
    private EditText currentFocusedEditText; // 현재 포커스된 EditText를 추적

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // directions.xml 레이아웃을 프래그먼트로 인플레이트
        View view = inflater.inflate(R.layout.directions, container, false);

        etDeparture = view.findViewById(R.id.et_departure);
        etArrival = view.findViewById(R.id.et_arrival);
        searchList = view.findViewById(R.id.search_list);
        findDirectionButton2 = view.findViewById(R.id.start_direction2);


        // 닫기 버튼 설정
        ImageButton closeDirectionButton = view.findViewById(R.id.btn_colse_directions);
        closeDirectionButton.setOnClickListener(v -> {
            // MainActivity로 가서 Fragment를 숨기도록 합니다.
            if (getActivity() instanceof MainChildActivity) {
                ((MainChildActivity) getActivity()).hideDirectionsFragment();
            } else if (getActivity() instanceof MainParentActivity) {
                ((MainParentActivity) getActivity()).hideDirectionsFragment();
            }
        });

        // 어댑터 설정
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, suggestions);
        searchList.setAdapter(adapter);

        // TextWatcher를 사용하여 입력 감지 및 버튼 활성화
        etDeparture.addTextChangedListener(textWatcher);
        etArrival.addTextChangedListener(textWatcher);

        // 출발지 삭제 버튼 설정
        ImageButton btnDeleteDeparture = view.findViewById(R.id.btn_delete_departure);
        btnDeleteDeparture.setOnClickListener(v -> etDeparture.setText(""));  // 클릭 시 출발지 EditText 비움

        // 도착지 삭제 버튼 설정
        ImageButton btnDeleteArrival = view.findViewById(R.id.btn_delete_arrival);
        btnDeleteArrival.setOnClickListener(v -> etArrival.setText(""));  // 클릭 시 도착지 EditText 비움

        // 리스트뷰 아이템 선택 시 EditText에 반영
        searchList.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedSuggestion = adapter.getItem(position);
            if (selectedSuggestion != null && currentFocusedEditText != null) {
                currentFocusedEditText.setText(selectedSuggestion);
                currentFocusedEditText.clearFocus();
            }
            searchList.setVisibility(View.GONE); // 선택 후 리스트 숨기기
        });


        findDirectionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DirectionsFragment", "길찾기 버튼이 눌렸습니다.");
                startRouteSearch();
            }
        });

        return view;
    }


    public void startRouteSearch() {
        // 출발지와 도착지 정보를 가져와서 길찾기 로직을 수행
        String departure = etDeparture.getText().toString();
        String arrival = etArrival.getText().toString();

        FindPathDTO departureDTO = findLocationInDepartureList(departure);
        FindPathDTO arrivalDTO = findLocationInArrivalList(arrival);

        if (departureDTO != null && arrivalDTO != null && getActivity() instanceof MainChildActivity) {
            ((MainChildActivity) getActivity()).requestDirection(departureDTO, arrivalDTO);

        } else if (departureDTO != null && arrivalDTO != null && getActivity() instanceof MainParentActivity) {
            ((MainParentActivity) getActivity()).requestDirection(departureDTO, arrivalDTO);
        }
        searchList.setVisibility(View.GONE);
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (s.length() > 0) {
                if (etDeparture.hasFocus()) {
                    currentFocusedEditText = etDeparture;
                } else if (etArrival.hasFocus()) {
                    currentFocusedEditText = etArrival;
                }
                searchForSuggestions(s.toString());
            } else {
                searchList.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    // 출발지와 도착지가 모두 입력된 경우에만 버튼 활성화
    private void checkInputsAndEnableButton() {
        boolean isDepartureFilled = !etDeparture.getText().toString().trim().isEmpty();
        boolean isArrivalFilled = !etArrival.getText().toString().trim().isEmpty();
        findDirectionButton2.setEnabled(isDepartureFilled && isArrivalFilled); // 버튼 활성화 여부 설정
    }

    // 검색 결과에서 위치 정보를 찾는 메서드
    private FindPathDTO findLocationInDepartureList(String locationName) {
        for (FindPathDTO dto : searchDepartureResult) {
            if (dto.getLocationName().equals(locationName)) {
                return dto;
            }
        }
        return null;
    }

    private FindPathDTO findLocationInArrivalList(String locationName) {
        for (FindPathDTO dto : searchArrivalResult) {
            if (dto.getLocationName().equals(locationName)) {
                return dto;
            }
        }
        return null;
    }

    // 기존 메서드 활용하여 검색어 제안 불러오기
    private void searchForSuggestions(String query) {
        if (getActivity() instanceof MainChildActivity) {
            ((MainChildActivity) getActivity()).fetchSearchSuggestions(query, suggestions -> {
                List<String> suggestionList = new ArrayList<>();
                for (FindPathDTO findPathDTO : suggestions) {
                    suggestionList.add(findPathDTO.getLocationName());
                }
                if (currentFocusedEditText == etDeparture) {
                    searchDepartureResult.clear();
                    searchDepartureResult.addAll(suggestions);
                }
                else {
                    searchArrivalResult.clear();
                    searchArrivalResult.addAll(suggestions);
                }

                adapter.clear();
                adapter.addAll(suggestionList);
                adapter.notifyDataSetChanged(); // 어댑터에 변경 사항을 알려줍니다.
                searchList.setVisibility(View.VISIBLE);
            });
        } else if (getActivity() instanceof MainParentActivity) {
            ((MainParentActivity) getActivity()).fetchSearchSuggestions(query, suggestions -> {
                List<String> suggestionList = new ArrayList<>();
                for (FindPathDTO findPathDTO : suggestions) {
                    suggestionList.add(findPathDTO.getLocationName());
                }
                if (currentFocusedEditText == etDeparture) {
                    searchDepartureResult.clear();
                    searchDepartureResult.addAll(suggestions);
                }
                else {
                    searchArrivalResult.clear();
                    searchArrivalResult.addAll(suggestions);
                }

                adapter.clear();
                adapter.addAll(suggestionList);
                adapter.notifyDataSetChanged(); // 어댑터에 변경 사항을 알려줍니다.
                searchList.setVisibility(View.VISIBLE);
            });
        }
    }
}