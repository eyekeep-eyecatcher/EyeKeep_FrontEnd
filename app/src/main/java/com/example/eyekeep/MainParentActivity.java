package com.example.eyekeep;

import static com.example.eyekeep.repository.Utils.clearToken;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eyekeep.DTO.BookMarkDTO;
import com.example.eyekeep.DTO.FindPathDTO;
import com.example.eyekeep.DTO.RoadNodeDTO;
import com.example.eyekeep.DTO.SearchGeocodingDTO;
import com.example.eyekeep.DTO.SearchNaverDTO;
import com.example.eyekeep.activity.LoginActivity;
import com.example.eyekeep.bookmark.BookmarkParentAdapter;
import com.example.eyekeep.bookmark.RequestParentBookMark;
import com.example.eyekeep.fetchSafetyData.FetchAccidentBlackSpot;
import com.example.eyekeep.fetchSafetyData.FetchCCTV;
import com.example.eyekeep.fetchSafetyData.FetchChildrenGuardHouse;
import com.example.eyekeep.fetchSafetyData.FetchChildrenProtectionZone;
import com.example.eyekeep.fetchSafetyData.FetchPoliceOffice;
import com.example.eyekeep.fetchSafetyData.FetchSafetyEmergencyBell;
import com.example.eyekeep.fetchSafetyData.FetchSecurityLamp;
import com.example.eyekeep.repository.BookmarkViewModel;
import com.example.eyekeep.repository.Utils;
import com.example.eyekeep.request.RequestParentSignout;
import com.example.eyekeep.request.RequestRoute2;
import com.example.eyekeep.retrofit.RetrofitClient;
import com.example.eyekeep.service.ReceiveLocationFromServer;
import com.example.eyekeep.service.RequestEyeKeep;
import com.example.eyekeep.service.SearchService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.ArrowheadPathOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PolylineOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainParentActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FragmentManager fragmentManager;
    private MapFragment mapFragment;
    private AppCompatImageButton btnmapbookmarkmini;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private FusedLocationProviderClient fusedLocationClient;
    private NaverMap naverMap;
    private boolean isReceivingLocation = false; // 위치 수신 상태 (ON/OFF)
    private Handler handler = new Handler();
    private SearchView searchView;
    private ListView searchList;
    private ArrayAdapter<String> adapter;
    private List<String> suggestions = new ArrayList<>();
    private Runnable searchRunnable, locationUpdater;
    private SearchService searchService;

    private static final long DELAY = 100; // 0.1초마다 delay after user stops typing

    private FrameLayout overlayContainer;
    private AppCompatButton safeMapButton, eyekeepmenuButton, bookmarkMenuButton, myInformationButton, startDirectionButton;
    private AppCompatImageButton mapbookmarkmini;
    private View safetyMapMenu;
    private View eyekeepMenu;
    private View myInformationMenu;
    private View bookmarkMenu;
    private MenuViewModel menuViewModel;
    private View infoView;
    private EditText etEmail;

    private List<Marker> searchMarkers = new ArrayList<>(); // 검색을 통해 추가된 마커들을 관리할 리스트
    private final List<Marker> bookmarkMarkers = new ArrayList<>();  // 북마커 리스트
    private boolean isBookmarkMarkersVisible = true;

    private FetchCCTV fetchCCTV;
    private FetchPoliceOffice fetchPoliceOffice;
    private FetchSecurityLamp fetchSecurityLamp;
    private FetchAccidentBlackSpot fetchAccidentBlackSpot;
    private FetchChildrenGuardHouse fetchChildrenGuardHouse;
    private FetchSafetyEmergencyBell fetchSafetyEmergencyBell;
    private FetchChildrenProtectionZone fetchchildrenProtectionZone;

    private final RequestRoute2 requestRoute2 = new RequestRoute2(MainParentActivity.this);

    private ReceiveLocationFromServer receiveLocationFromServer;
    private RequestEyeKeep requestEyeKeep;
    private BookmarkViewModel bookmarkViewModel;
    private RequestParentBookMark requestBookMark;  // 북마크 리스트 관리 객체
    private BookmarkParentAdapter bookmarkAdapter;
    private RecyclerView recyclerView;

    private boolean isDirectionFragmentVisible = false;
    private FrameLayout fragmentContainer;
    // 기존 경로와 화살표를 클래스 멤버 변수로 저장합니다.
    private PolylineOverlay polyline;
    private ArrowheadPathOverlay arrowheadPathOverlay;


    private final BroadcastReceiver fcmMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String nickname = intent.getStringExtra("nickname");
            String email = intent.getStringExtra("email");
            showRequestDialog(nickname, email);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_parent);


        Context appContext = getApplicationContext();
        Utils.init(appContext);
        // ViewModel 초기화
        menuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        bookmarkViewModel = new ViewModelProvider(this).get(BookmarkViewModel.class);

        // 북마크 리스트를 서버에서 불러오고 지도에 표시하는 작업 수행
        requestBookMark = new RequestParentBookMark(this, bookmarkViewModel);
        requestBookMark.getBookMarkList();

        String clientId = com.example.eyekeep.BuildConfig.NAVER_MAP_CLIENT_ID;
        // NaverMapSdk 초기화
        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient(BuildConfig.NAVER_MAP_CLIENT_ID)
        );
        EdgeToEdge.enable(this);

        fragmentManager = getSupportFragmentManager();
        mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.naverMap);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.naverMap, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);



        // 브로드캐스트 수신기 등록
        LocalBroadcastManager.getInstance(this).registerReceiver(fcmMessageReceiver,
                new IntentFilter("com.example.EyeKeep.ACTION_FCM_MESSAGE"));

        overlayContainer = findViewById(R.id.overlay_container);
        safeMapButton = findViewById(R.id.btn_safety_map);
        eyekeepmenuButton = findViewById(R.id.btn_eyekeep_menu);
        bookmarkMenuButton = findViewById(R.id.btn_book_mark);
        myInformationButton = findViewById(R.id.btn_my_info); //내정봅 ㅓ튼

        fragmentContainer = findViewById(R.id.fragment_container);

        Drawable safeMapButtonDrawable = safeMapButton.getCompoundDrawables()[1]; // drawableTop이 [1]에 위치
        Drawable eyekeepMenuButtonDrawable = eyekeepmenuButton.getCompoundDrawables()[1];
        Drawable bookmarkButtonDrawable = bookmarkMenuButton.getCompoundDrawables()[1];
        Drawable myInformationButtonDrawable = myInformationButton.getCompoundDrawables()[1];

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        safetyMapMenu = inflater.inflate(R.layout.activity_safety_map_menu, null);
        eyekeepMenu = inflater.inflate(R.layout.eyekeep_menu, null);
        myInformationMenu = inflater.inflate(R.layout.my_information_menu, null);
        infoView = inflater.inflate(R.layout.location_menu, null);
        bookmarkMenu = inflater.inflate(R.layout.bookmark_menu, null);

        // ViewModel 초기화
        menuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        // ViewModel의 변경사항을 관찰하여 UI 업데이트
        menuViewModel.getCurrentMenu().observe(this, menuType -> {
            hideInfoTab(); // 정보탭 닫기
            hideBookMarkInfoTab(); // 북마크 정보탭 닫기
            overlayContainer.removeAllViews();
            int defaultColor = ContextCompat.getColor(MainParentActivity.this,R.color.black);
            // 모든 버튼과 이미지를 기본 색상으로 초기화
            safeMapButtonDrawable.setTint(defaultColor);
            safeMapButton.setTextColor(defaultColor);
            eyekeepMenuButtonDrawable.setTint(defaultColor);
            eyekeepmenuButton.setTextColor(defaultColor);
            bookmarkButtonDrawable.setTint(defaultColor);
            bookmarkMenuButton.setTextColor(defaultColor);
            myInformationButtonDrawable.setTint(defaultColor);
            myInformationButton.setTextColor(defaultColor);
            switch (menuType) {
                case SAFETY_MAP:
                    overlayContainer.addView(safetyMapMenu);
                    safeMapButtonDrawable.setTint(ContextCompat.getColor(MainParentActivity.this, R.color.blue)); // 원하는 색상으로 변경
                    safeMapButton.setTextColor(ContextCompat.getColor(this, R.color.blue));
                    break;
                case EYEKEEP:
                    overlayContainer.addView(eyekeepMenu);
                    eyekeepMenuButtonDrawable.setTint(ContextCompat.getColor(MainParentActivity.this, R.color.blue)); // 원하는 색상으로 변경
                    eyekeepmenuButton.setTextColor(ContextCompat.getColor(this, R.color.blue));
                    break;
                case MY_INFORMATION:
                    overlayContainer.addView(myInformationMenu);
                    myInformationButtonDrawable.setTint(ContextCompat.getColor(MainParentActivity.this, R.color.blue)); // 원하는 색상으로 변경
                    myInformationButton.setTextColor(ContextCompat.getColor(this, R.color.blue));
                    break;
                case BOOKMARK:
                    overlayContainer.addView(bookmarkMenu);
                    bookmarkButtonDrawable.setTint(ContextCompat.getColor(MainParentActivity.this, R.color.blue)); // 원하는 색상으로 변경
                    bookmarkMenuButton.setTextColor(ContextCompat.getColor(this, R.color.blue));
                    break;
                case NONE:
                default:
                    overlayContainer.setVisibility(View.GONE); // 메뉴가 비활성화된 경우
                    return;
            }
            overlayContainer.setVisibility(View.VISIBLE);
        });
        // 버튼 클릭 시 ViewModel의 상태 변경
        safeMapButton.setOnClickListener(v -> menuViewModel.setCurrentMenu(MenuViewModel.MenuType.SAFETY_MAP));
        eyekeepmenuButton.setOnClickListener(v -> menuViewModel.setCurrentMenu(MenuViewModel.MenuType.EYEKEEP));
        myInformationButton.setOnClickListener(v -> menuViewModel.setCurrentMenu(MenuViewModel.MenuType.MY_INFORMATION));
        bookmarkMenuButton.setOnClickListener(v -> menuViewModel.setCurrentMenu(MenuViewModel.MenuType.BOOKMARK));

        // RequestSignout 클래스의 인스턴스 생성
        RequestParentSignout requestSignout = new RequestParentSignout(MainParentActivity.this);
        AppCompatButton btnLogout = myInformationMenu.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestSignout.requestSignout();
            }
        });

        btnmapbookmarkmini = findViewById(R.id.btn_map_book_mark_mini);
        btnmapbookmarkmini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleBookmarkMarkers();
            }
        });

        AppCompatButton btneyekeeprequest = eyekeepMenu.findViewById(R.id.btn_eyekeeep_requet);
        requestEyeKeep = new RequestEyeKeep(MainParentActivity.this, eyekeepMenu);
        btneyekeeprequest.setOnClickListener(view -> requestEyeKeep.requestEyeKeep());

        // 안전지도 색칠
        TextView tvSafetyMapTitle = safetyMapMenu.findViewById(R.id.tv_safety_map_title);
        String contenttext = tvSafetyMapTitle.getText().toString(); //텍스트 가져옴.
        SpannableString spannableString = new SpannableString(contenttext); //객체 생성 SpannableString 은 String의 텍스트 일부에 스타일
        String word = "안전지도";
        int start = contenttext.indexOf(word);
        int end = start + word.length();
        spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // 범례/설명 다이로그 생성
        AppCompatButton btnLegendExplanation = safetyMapMenu.findViewById(R.id.btn_legend_explanation);
        btnLegendExplanation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTextViewLegendExplanationDialog();
            }
        });
        // CCTV 체크박스
        CheckBox cctvCheckBox = safetyMapMenu.findViewById(R.id.cb_cctv);
        cctvCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // cctv 마커 추가 로직
                    fetchCCTV.fetchCCTV();
                } else {
                    // cctv 마커 제거 로직
                    fetchCCTV.removeCCTVMarkers();
                }
            }
        });
        // 경찰서
        CheckBox policestationCheckBox = safetyMapMenu.findViewById(R.id.cb_police_station);
        policestationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // police 마커 추가 로직
                    fetchPoliceOffice.fetchPoliceStations();
                } else {
                    // police 마커 제거 로직
                    fetchPoliceOffice.removePoliceMarkers();
                }
            }
        });
        // 보안등
        CheckBox lampCheckBox = safetyMapMenu.findViewById(R.id.cb_lamp);
        lampCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // lamp 마커 추가 로직
                    fetchSecurityLamp.fetchSecurityLamps(); // 보안등 마커 추가
                    //fetchStreetLamp.fetchStreetLamps();   // 가로등 마커 추가
                } else {
                    // lamp 마커 제거 로직
                    fetchSecurityLamp.removeSecurityLampMarkers(); // 보안등 마커 제거
                    //fetchSecurityLamp.removeSecurityLampMarkers();   // 가로등 마커 제거
                }
            }
        });
        // 아동안전지킴이집
        CheckBox childsafetyzoneCheckBox = safetyMapMenu.findViewById(R.id.cb_child_safety_zone);
        childsafetyzoneCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // 마커 추가 로직
                    fetchChildrenGuardHouse.fetchChildrenGuardHouses();
                } else {
                    // 마커 제거 로직
                    fetchChildrenGuardHouse.removeChildrenGuardHouseMarkers();
                }
            }
        });
        // 안전비상벨
        CheckBox safebellCheckBox = safetyMapMenu.findViewById(R.id.cb_safe_bell);
        safebellCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // 마커 추가 로직
                    fetchSafetyEmergencyBell.fetchSafetyEmergencyBells();
                } else {
                    // 마커 제거 로직
                    fetchSafetyEmergencyBell.removeSafetyEmergencyBellMarkers();
                }
            }
        });
        // 아동보호구역F
        CheckBox childrenProtectionZoneCheckBox = safetyMapMenu.findViewById(R.id.cb_children_protection_zone);
        childrenProtectionZoneCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // 마커, 원 추가 로직
                    fetchchildrenProtectionZone.fetchChildrenProtectionZones();
                } else {
                    // 마커, 원 제거 로직
                    //fetchchildrenProtectionZone.removeChildrenProtectionZoneMarkers();
                    fetchchildrenProtectionZone.removeChildrenProtectionZoneCircles();
                }
            }
        });
        //사고다발구역
        CheckBox AccidentBlackSpotChexBox = safetyMapMenu.findViewById(R.id.cb_accident_black_spot);
        AccidentBlackSpotChexBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // 마커,원 추가 로직
                    fetchAccidentBlackSpot.fetchAccidentBlackSpot();
                } else {
                    // 마커,원 제거 로직
                    fetchAccidentBlackSpot.removeAccidentBlackSpotMarkers();
                    fetchAccidentBlackSpot.removeAccidentBlackSpot2Markers();
                    fetchAccidentBlackSpot.removeAccidentBlackSpotCircles();
                }
            }
        });


        searchView = findViewById(R.id.searchView);
        searchList = findViewById(R.id.searchList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, suggestions);
        searchList.setAdapter(adapter); // 여기서 NullPointerException이 발생하지 않도록 해야 합니다.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 검색버튼을 누를떄만 호출
                // query를 이용
                try {
                    searchLocation(query);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                return true;   //true를 반환하면 검색어 제출후 기본동작 막음
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                handler.removeCallbacks(searchRunnable);
                searchRunnable = () -> {
                    if (!newText.isEmpty()) {
                        fetchSearchSuggestions(newText);
                    } else {
                        searchList.setVisibility(View.GONE);
                    }
                };
                handler.postDelayed(searchRunnable, DELAY);

                return true;
            }
        });
        // ListView 아이템 클릭 리스너
        searchList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = suggestions.get(position);
            fetchLocationAndMoveCamera(selectedItem);
            searchList.setVisibility(View.GONE); // 리스트 숨김
        });
        //부모님이 eyekeep버튼을 ON/OFF
        AppCompatImageButton btnEyekeepLocation = findViewById(R.id.btn_eyekeep_location);
        btnEyekeepLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isReceivingLocation) {
                    stopReceivingLocation(); // 위치 정보 받는 것 중단
                } else {
                    startReceivingLocation(); // 위치 정보 받기 시작
                }
                isReceivingLocation = !isReceivingLocation; // 상태 변경
            }
        });

        // 길찾기
        startDirectionButton = findViewById(R.id.start_direction);
        startDirectionButton.setOnClickListener(v -> {
            if (!isDirectionFragmentVisible) {
                showDirectionsFragment();
                startDirectionButton.setText("길찾기");
                isDirectionFragmentVisible = true;
            } else {
                performRouteSearch();
            }
        });


    }
    // 길찾기 띄우기
    private void showDirectionsFragment() {
        fragmentContainer.setVisibility(View.VISIBLE); // FragmentContainerView를 보이게 설정

        DirectionsFragment directionsFragment = new DirectionsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, directionsFragment)
                .commit();
        startDirectionButton.setVisibility(View.GONE);
        isDirectionFragmentVisible = true;
    }

    public void hideDirectionsFragment() {
        // 기존에 그려진 경로가 있으면 지웁니다.
        clearExistingRoute();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
        }
        fragmentContainer.setVisibility(View.GONE); // FragmentContainerView를 숨김
        startDirectionButton.setVisibility(View.VISIBLE);
        isDirectionFragmentVisible = false;
    }

    private void performRouteSearch() {
        // 길찾기 로직 수행
        DirectionsFragment directionsFragment = (DirectionsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (directionsFragment != null) {
            directionsFragment.startRouteSearch();
        }
    }

    public void setBookmarkAdapter() {
        recyclerView = bookmarkMenu.findViewById(R.id.bookmark_list);
        bookmarkAdapter = new BookmarkParentAdapter(bookmarkViewModel.getBookmarkList().getValue(), requestBookMark, MainParentActivity.this);
        recyclerView.setAdapter(bookmarkAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void updateBookmarkList() {
        bookmarkAdapter = new BookmarkParentAdapter(bookmarkViewModel.getBookmarkList().getValue(), requestBookMark, MainParentActivity.this);
        recyclerView.setAdapter(bookmarkAdapter);
    }



    //아이 위치 수신 시작
    private void startReceivingLocation() {
        handler.post(locationReceiver); // 즉시 위치 수신 시작
    }
    private final Runnable locationReceiver = new Runnable() {
        @Override
        public void run() {
            receiveLocationFromServer.receiveLocationFromServer(); // 서버에서 위치 정보 받아오기

            if (isReceivingLocation) {
                handler.postDelayed(this, 3000); // 3초 후에 다시 위치 정보 받기
            }
        }
    };
    private void stopReceivingLocation() {

        handler.removeCallbacks(locationReceiver); // 위치 정보 수신 중지
    }

    // 사용자의 현재 위치를 가져오는 메서드
    private void fetchUserLocation(OnSuccessListener<Location> listener) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, listener);
    }
    // 거리 계산 메서드
    private double calculateDistance(LatLng start, LatLng end) {
        float[] results = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
        return results[0]; // 결과는 미터 단위로 반환됨
    }

    private void showRequestDialog(String nickname, String email) {

    }

        public void drawRouteOnMap(List<LatLng> routePoints) {
        // 기존에 그려진 경로가 있으면 지웁니다.
        clearExistingRoute();
        // 경로를 따라 폴리라인을 그림
        polyline = new PolylineOverlay();
        polyline.setCoords(routePoints);
        polyline.setColor(Color.BLUE); // 경로의 색상을 설정합니다.
        polyline.setMap(naverMap);  // NaverMap 객체에 Polyline을 추가하여 경로를 그립니다.

        // 경로를 따라 화살표를 표시하는 코드 추가
        addArrowsOnRoute(routePoints);
    }


    private void addArrowsOnRoute(List<LatLng> routePoints) {
        arrowheadPathOverlay = new ArrowheadPathOverlay();
        arrowheadPathOverlay.setCoords(routePoints);
        arrowheadPathOverlay.setColor(Color.BLUE); // 화살표의 색상 설정
        arrowheadPathOverlay.setMap(naverMap); // NaverMap 객체에 화살표를 추가하여 경로를 그립니다.
    }

    private void clearExistingRoute() {
        // 기존의 폴리라인과 화살표가 있으면 지도에서 제거합니다.
        if (polyline != null) {
            polyline.setMap(null);
        }
        if (arrowheadPathOverlay != null) {
            arrowheadPathOverlay.setMap(null);
        }
    }

    public void signout() {
        Intent intent = new Intent(MainParentActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); //현재 액티비티 파괴
        clearToken();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 브로드캐스트 수신기 해제
        LocalBroadcastManager.getInstance(this).unregisterReceiver(fcmMessageReceiver);
    }

    private void searchLocation(String query) throws UnsupportedEncodingException {
        SearchService service = RetrofitClient.getRetrofitInstanceSearchNaver().create(SearchService.class);
        Call<SearchNaverDTO> call = service.searchNaver(BuildConfig.NAVER_SEARCH_CLIENT_ID, BuildConfig.NAVER_SEARCH_API_KEY, query, 10, 1, "random");

        call.enqueue(new Callback<SearchNaverDTO>() {
            @Override
            public void onResponse(@NonNull Call<SearchNaverDTO> call, @NonNull Response<SearchNaverDTO> response) {
                if (!response.isSuccessful()) {
                    int statusCode = response.code();
                    String errorMessage = response.message();
                    Log.e("SearchNaverError", "Error code : Code " + statusCode + " - " + errorMessage);
                    Toast.makeText(MainParentActivity.this, "Response Error: Code " + statusCode + " - " + errorMessage, Toast.LENGTH_LONG).show();
                    return;
                }

                SearchNaverDTO data = response.body();
                List<SearchNaverDTO.Item> items = data != null ? data.getItems() : null;

                if (items == null || items.isEmpty()) {
                    Log.e("SearchNaverError", "Items list is empty or null");
                    searchAddress(query);
                    return;
                }

                LatLng firstLocation = null;
                String firstTitle = "";

                for (SearchNaverDTO.Item item : items) {
                    try {
                        double longitude = Double.parseDouble(item.getMapx()) / 10_000_000;
                        double latitude = Double.parseDouble(item.getMapy()) / 10_000_000;
                        String name = item.getTitle().replaceAll("<[^>]*>", "");

                        LatLng latLng = new LatLng(latitude, longitude);
                        String address = item.getAddress();
                        String roadAddress = item.getRoadAddress();

                        addMarkerToLocation(latLng, name, address, roadAddress); // 마커 추가 및 정보탭 설정

                        if (firstLocation == null) {
                            firstLocation = latLng;
                            firstTitle = name;
                        }
                    } catch (NumberFormatException e) {
                        Log.e("SearchNaverError", "NumberFormatException: " + e.getMessage());
                        Toast.makeText(MainParentActivity.this, "좌표 변환 오류", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                moveCameraToLocation(firstLocation);

                String firstAddress = items.get(0).getAddress();
                String firstRoadAddress = items.get(0).getRoadAddress();
                showInfoTab(firstLocation, firstTitle, firstAddress, firstRoadAddress);
            }

            @Override
            public void onFailure(@NonNull Call<SearchNaverDTO> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Search naver error occurred: " + t.getMessage());
                Toast.makeText(MainParentActivity.this, "네트워크 에러", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchAddress(String query) {
        SearchService service = RetrofitClient.getRetrofitInstanceSearchGeocoding().create(SearchService.class);
            Call<SearchGeocodingDTO> call = service.searchAddress(BuildConfig.NAVER_MAP_CLIENT_ID, BuildConfig.NAVER_MAP_API_KEY, query, 1);
        call.enqueue(new Callback<SearchGeocodingDTO>() {
            @Override
            public void onResponse(@NonNull Call<SearchGeocodingDTO> call, @NonNull Response<SearchGeocodingDTO> response) {
                SearchGeocodingDTO data = response.body();
                String status = data != null ? data.getStatus() : null;
                if (!response.isSuccessful() || !Objects.equals(status, "OK")) {
                    // 응답이 성공적이지 않을 때의 처리
                    int statusCode = response.code(); // HTTP 상태 코드
                    String errorMessage = data != null ? data.getErrorMessage() : null; // HTTP 상태 메시지
                    Log.e("SearchAddressError", "Error code : Code " + statusCode + " - " + errorMessage);
                    Toast.makeText(MainParentActivity.this, "Response Error: Code " + statusCode + " - " + errorMessage, Toast.LENGTH_LONG).show();
                    return;
                }

                List<SearchGeocodingDTO.addressList> items = data.getAddresses();

                // items 리스트가 비어있거나 null인 경우 처리
                if (items == null || items.isEmpty()) {
                    Log.e("SearchAddressError", "Items list is empty or null");
                    Toast.makeText(MainParentActivity.this, "요청 검색 결과가 없습니다.", Toast.LENGTH_LONG).show();
                    return;
                }

                SearchGeocodingDTO.addressList item = items.get(0);
                String roadAddress = item.getRoadAddress(); // "서울특별시 중랑구 면목로84길 37"
                String jibunAddress = item.getJibunAddress(); // "서울특별시 중랑구 면목동 1287-1"
                String displayAddress;
                if (Objects.equals(roadAddress, "")) {
                    displayAddress = jibunAddress;
                } else if (Objects.equals(jibunAddress, "")) {
                    displayAddress = roadAddress;
                } else { // 도로명 주소, 구주소 모두 있을 경우 도로명 주소 사용.
                    displayAddress = roadAddress;
                }

                LatLng latLng;
                try {
                    // 첫 번째 아이템의 mapx, mapy 값을 가져와서 double로 변환
                    double longitude = Double.parseDouble(item.getLongitude()); // 경도
                    double latitude = Double.parseDouble(item.getLatitude());  // 위도

                    latLng = new LatLng(latitude, longitude);
                    addMarkerToLocation(latLng, displayAddress, jibunAddress, roadAddress); // 마커 추가와 동시에 정보탭도 표시

                } catch (NumberFormatException e) {
                    // 문자열을 double로 변환하는 데 실패한 경우
                    Log.e("SearchAddressError", "NumberFormatException: " + e.getMessage());
                    Toast.makeText(MainParentActivity.this, "좌표 변환 오류", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 첫 번째 아이템의 위치로 카메라 이동
                moveCameraToLocation(latLng);

                // 정보탭 띄우기
                showInfoTab(latLng, displayAddress, jibunAddress, roadAddress);

                Toast.makeText(MainParentActivity.this, displayAddress + " 검색 결과", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<SearchGeocodingDTO> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Search address error occurred: " + t.getMessage());
                Toast.makeText(MainParentActivity.this, "네트워크 에러", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // 검색 결과를 처리하는 메서드
    private void handleSearchResponse(@NonNull Call<SearchNaverDTO> call, @NonNull Response<SearchNaverDTO> response, boolean moveCamera) {
        if (response.isSuccessful() && response.body() != null) {
            suggestions.clear();
            List<SearchNaverDTO.Item> items = response.body().getItems();

            if (items.isEmpty()) {
                return;
            }

            for (SearchNaverDTO.Item item : items) {
                suggestions.add(item.getTitle().replaceAll("<[^>]*>", ""));
            }

            if (moveCamera) {
                // 첫 번째 검색 결과로 카메라 이동
                SearchNaverDTO.Item firstItem = items.get(0);
                double longitude = Double.parseDouble(firstItem.getMapx()) / 10_000_000;
                double latitude = Double.parseDouble(firstItem.getMapy()) / 10_000_000;
                String address = firstItem.getAddress();
                String roadAddress = firstItem.getRoadAddress();
                LatLng latLng = new LatLng(latitude, longitude);

                moveCameraToLocation(latLng);
                addMarkerToLocation(latLng, firstItem.getTitle().replaceAll("<[^>]*>", ""), address, roadAddress);
                showInfoTab(latLng, firstItem.getTitle().replaceAll("<[^>]*>", ""), address, roadAddress);  // 정보탭 표시

                adapter.notifyDataSetChanged();
                searchList.setVisibility(View.GONE);  // 리스트가 보이면 숨기기
            } else {
                // 검색 제안 리스트를 갱신
                adapter.notifyDataSetChanged();
                searchList.setVisibility(View.VISIBLE);
            }
        } else {
            Log.e("SearchError", "Error: " + response.message());


        }
    }

    // 검색 제안 가져오기 메서드
    public void fetchSearchSuggestions(String query) {
        SearchService service = RetrofitClient.getRetrofitInstanceSearchNaver().create(SearchService.class);
        Call<SearchNaverDTO> call = service.searchNaver(BuildConfig.NAVER_SEARCH_API_KEY, BuildConfig.NAVER_SEARCH_CLIENT_ID, query, 5, 1, "random");

        call.enqueue(new Callback<SearchNaverDTO>() {
            @Override
            public void onResponse(@NonNull Call<SearchNaverDTO> call, @NonNull Response<SearchNaverDTO> response) {
                handleSearchResponse(call, response, false);
            }
            @Override
            public void onFailure(@NonNull Call<SearchNaverDTO> call, @NonNull Throwable t) {

            }
        });
    }

    // 콜백 인터페이스 정의
    public interface OnSuggestionsFetchedListener {
        void onSuggestionsFetched(List<FindPathDTO> suggestions);
    }

    // 새로운 메서드 오버로드
    public void fetchSearchSuggestions(String query, MainParentActivity.OnSuggestionsFetchedListener listener) {
        SearchService service = RetrofitClient.getRetrofitInstanceSearchNaver().create(SearchService.class);
        Call<SearchNaverDTO> call = service.searchNaver(BuildConfig.NAVER_SEARCH_CLIENT_ID, BuildConfig.NAVER_SEARCH_API_KEY, query, 5, 1, "random");

        call.enqueue(new Callback<SearchNaverDTO>() {
            @Override
            public void onResponse(@NonNull Call<SearchNaverDTO> call, @NonNull Response<SearchNaverDTO> response) {
                List<FindPathDTO> suggestions = new ArrayList<>();

                if (!response.isSuccessful()) {
                    int statusCode = response.code();
                    String errorMessage = response.message();
                    Log.e("SearchNaverError", "Error code : Code " + statusCode + " - " + errorMessage);
                    return;
                }

                SearchNaverDTO data = response.body();
                List<SearchNaverDTO.Item> items = data != null ? data.getItems() : null;

                if (items == null || items.isEmpty()) {
                    searchAddress(query, listener);
                    return;
                }

                for (SearchNaverDTO.Item item : items) {
                    try {
                        double longitude = Double.parseDouble(item.getMapx()) / 10_000_000;
                        double latitude = Double.parseDouble(item.getMapy()) / 10_000_000;
                        String name = item.getTitle().replaceAll("<[^>]*>", "");

                        FindPathDTO findPathDTO = new FindPathDTO();
                        findPathDTO.setLocationName(name);
                        findPathDTO.setLongitude(longitude);
                        findPathDTO.setLatitude(latitude);

                        suggestions.add(findPathDTO);

                    } catch (NumberFormatException e) {
                        Log.e("SearchNaverError", "NumberFormatException: " + e.getMessage());
                        return;
                    }
                }

                listener.onSuggestionsFetched(suggestions);
            }


            @Override
            public void onFailure(@NonNull Call<SearchNaverDTO> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Search naver error occurred: " + t.getMessage());
            }
        });
    }

    // 메서드 오버로드
    public void searchAddress(String query, MainParentActivity.OnSuggestionsFetchedListener listener) {
        SearchService service = RetrofitClient.getRetrofitInstanceSearchGeocoding().create(SearchService.class);
        Call<SearchGeocodingDTO> call = service.searchAddress(BuildConfig.NAVER_MAP_CLIENT_ID, BuildConfig.NAVER_MAP_API_KEY, query, 1);
        call.enqueue(new Callback<SearchGeocodingDTO>() {
            @Override
            public void onResponse(@NonNull Call<SearchGeocodingDTO> call, @NonNull Response<SearchGeocodingDTO> response) {
                List<FindPathDTO> suggestions = new ArrayList<>();
                SearchGeocodingDTO data = response.body();
                String status = data != null ? data.getStatus() : null;
                if (!response.isSuccessful() || !Objects.equals(status, "OK")) {
                    // 응답이 성공적이지 않을 때의 처리
                    int statusCode = response.code(); // HTTP 상태 코드
                    String errorMessage = data != null ? data.getErrorMessage() : null; // HTTP 상태 메시지
                    Log.e("SearchAddressError", "Error code : Code " + statusCode + " - " + errorMessage);
                    return;
                }

                List<SearchGeocodingDTO.addressList> items = data.getAddresses();

                // items 리스트가 비어있거나 null인 경우 처리
                if (items == null || items.isEmpty()) {
                    Log.e("SearchAddressError", "Items list is empty or null");
                    return;
                }

                SearchGeocodingDTO.addressList item = items.get(0);
                String roadAddress = item.getRoadAddress(); // "서울특별시 중랑구 면목로84길 37"
                String jibunAddress = item.getJibunAddress(); // "서울특별시 중랑구 면목동 1287-1"
                String displayAddress;
                if (Objects.equals(roadAddress, "")) {
                    displayAddress = jibunAddress;
                } else if (Objects.equals(jibunAddress, "")) {
                    displayAddress = roadAddress;
                } else { // 도로명 주소, 구주소 모두 있을 경우 도로명 주소 사용.
                    displayAddress = roadAddress;
                }

                try {
                    // 첫 번째 아이템의 mapx, mapy 값을 가져와서 double로 변환
                    double longitude = Double.parseDouble(item.getLongitude()); // 경도
                    double latitude = Double.parseDouble(item.getLatitude());  // 위도

                    FindPathDTO findPathDTO = new FindPathDTO();
                    findPathDTO.setLocationName(displayAddress);
                    findPathDTO.setLongitude(longitude);
                    findPathDTO.setLatitude(latitude);

                    suggestions.add(findPathDTO);

                } catch (NumberFormatException e) {
                    // 문자열을 double로 변환하는 데 실패한 경우
                    Log.e("SearchAddressError", "NumberFormatException: " + e.getMessage());
                    return;
                }


                listener.onSuggestionsFetched(suggestions);
            }

            @Override
            public void onFailure(@NonNull Call<SearchGeocodingDTO> call, @NonNull Throwable t) {
                Log.e("NetworkError", "Search address error occurred: " + t.getMessage());
            }
        });
    }
    // 해당 위치로 카메라 이동 메서드
    private void fetchLocationAndMoveCamera(String query) {
        SearchService service = RetrofitClient.getRetrofitInstanceSearchNaver().create(SearchService.class);
        Call<SearchNaverDTO> call = service.searchNaver(BuildConfig.NAVER_SEARCH_CLIENT_ID, BuildConfig.NAVER_SEARCH_API_KEY, query, 1, 1, "random");

        call.enqueue(new Callback<SearchNaverDTO>() {
            @Override
            public void onResponse(@NonNull Call<SearchNaverDTO> call, @NonNull Response<SearchNaverDTO> response) {
                handleSearchResponse(call, response, true);
            }
            @Override
            public void onFailure(@NonNull Call<SearchNaverDTO> call, @NonNull Throwable t) {
                Toast.makeText(MainParentActivity.this, "위치 정보 가져오기 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void requestDirection(FindPathDTO departureDTO, FindPathDTO arrivalDTO) {
        RoadNodeDTO startNode = new RoadNodeDTO();
        startNode.setNodeId("0");
        startNode.setLatitude(departureDTO.getLatitude());
        startNode.setLongitude(departureDTO.getLongitude());
        RoadNodeDTO endNode = new RoadNodeDTO();
        endNode.setNodeId("-1");
        endNode.setLatitude(arrivalDTO.getLatitude());
        endNode.setLongitude(arrivalDTO.getLongitude());

        List<RoadNodeDTO> startToEnd = new ArrayList<>();
        startToEnd.add(startNode);
        startToEnd.add(endNode);

        requestRoute2.getRouteFromServer(startToEnd);
        // 출발지와 도착지의 좌표를 사용하여 카메라를 이동하고 키보드를 숨깁니다.
        LatLng departureLatLng = new LatLng(departureDTO.getLatitude(), departureDTO.getLongitude());
        LatLng arrivalLatLng = new LatLng(arrivalDTO.getLatitude(), arrivalDTO.getLongitude());
        moveCameraToIncludeBothPoints(departureLatLng, arrivalLatLng);
    }

    private void moveCameraToIncludeBothPoints(LatLng departure, LatLng arrival) {
        // 출발지와 도착지를 포함하는 LatLngBounds 객체 생성
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(departure);
        builder.include(arrival);
        LatLngBounds bounds = builder.build();

        // 카메라 이동 애니메이션 설정
        CameraUpdate cameraUpdate = CameraUpdate.fitBounds(bounds, 100);  // 100은 패딩 값으로, 출발지와 도착지 주변에 약간의 여백을 줍니다.
        naverMap.moveCamera(cameraUpdate);

        // 키보드 숨기기
        hideKeyboard();
        hideSearchList(); // 검색 리스트 숨기기
        searchList.setVisibility(View.GONE);
    }

    private void hideSearchList() {
        if (searchList != null) {
            searchList.setVisibility(View.GONE); // 검색 리스트 숨기기
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //위치정보텝
    private void showInfoTab(LatLng latLng, String title, String address, String roadAddress) {
        adapter.notifyDataSetChanged();
        overlayContainer.removeAllViews(); // 기존의 뷰를 제거

        // Title이 도로명 주소와 같다면 구주소를 표시
        String displayAddress = (title.equals(roadAddress) && address != null && !address.isEmpty()) ? address : roadAddress;
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View infoView = inflater.inflate(R.layout.location_menu, overlayContainer, false);


        TextView titleView = infoView.findViewById(R.id.location_title);        //이름 나타내는 뷰
        TextView addressView = infoView.findViewById(R.id.location_address);    //주소 나타내는 뷰
        TextView distanceView = infoView.findViewById(R.id.location_distance);  //거리 나타내는 뷰


        titleView.setText(title);
        addressView.setText(displayAddress); // displayAddress를 주소 뷰에 설정
        // 현재 위치 가져오기 시도
        fetchUserLocation(location -> {
            if (location != null) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                double distance = calculateDistance(userLatLng, latLng); // 거리 계산

                // 거리 표시: 1000m 이상은 km 단위로, 그 외는 m 단위로 표시
                if (distance >= 1000) {
                    double distanceInKm = distance / 1000.0;
                    distanceView.setText(String.format(Locale.getDefault(), "%.1fkm", distanceInKm));
                } else {
                    int distanceInMeters = (int) distance; // 소수점 제거 및 정수 변환
                    distanceView.setText(String.format(Locale.getDefault(), "%dm", distanceInMeters));
                }
            } else {
                // 위치 정보가 없을 경우 거리를 숨김
                distanceView.setVisibility(View.GONE);
            }
        });

        overlayContainer.addView(infoView);
        overlayContainer.setVisibility(View.VISIBLE);

        // 정보탭에서 사용될 버튼 참조
        AppCompatButton bookmarkButton = infoView.findViewById(R.id.bookmark_button);

        // 버튼 초기화 및 상태 업데이트
        bookmarkButton.setText(bookmarkViewModel.isBookmarked(title) ? "북마크 저장됨" : "북마크 등록");

        // 버튼 클릭 리스너 설정
        bookmarkButton.setOnClickListener(view -> {
            if (bookmarkViewModel.isBookmarked(title)) {
                // 북마크 삭제 로직
                requestBookMark.deleteBookmarkFromServer(title);
                bookmarkButton.setText("북마크 등록");
                bookmarkButton.setBackground(getDrawable(R.drawable.button_loginn2));
                bookmarkButton.setTextColor(Color.BLACK);
            } else {
                // 북마크 저장 로직
                requestBookMark.saveBookmarkToServer(new BookMarkDTO(title, latLng.latitude, latLng.longitude));
                bookmarkButton.setText("북마크 저장됨");
                bookmarkButton.setBackground(getDrawable(R.drawable.button_loginn));
                bookmarkButton.setTextColor(Color.WHITE);
            }
        });

    }
    //정보텝숨기기
    private void hideInfoTab() {
        overlayContainer.removeAllViews();
        overlayContainer.setVisibility(View.GONE);
    }

    private void hideOverlayMenus() {
        // overlayContainer에 현재 추가된 뷰가 있는지 확인하고 제거
        if (overlayContainer.getChildCount() > 0) {
            overlayContainer.removeAllViews(); // 모든 뷰 제거
            overlayContainer.setVisibility(View.GONE); // 컨테이너를 숨김
        }
    }

    // 리스트서치마커
    private void addMarkerToLocation(LatLng latLng, String title, String address, String roadAddress) {
        if (naverMap == null) {
            return;
        }
        Marker marker = new Marker();
        marker.setPosition(latLng);
        marker.setCaptionText(title); // 마커에 이름 표시
        marker.setMap(naverMap);
        searchMarkers.add(marker); // 서치마커리스트에 추가

        // 마커의 태그에 주소 정보를 저장해 둠
        marker.setTag(address);
        // 마커 클릭 이벤트 리스너 추가
        marker.setOnClickListener(overlay -> {
            showInfoTab(latLng, title, address, roadAddress); // 정보탭 표시 메서드 호출
            return true;
        });
    }

    // 지도에 북마크 마커를 추가하는 메서드
    public void addBookmarkMarkersOnMap(List<BookMarkDTO> bookmarkList) {
        for (BookMarkDTO bookmark : bookmarkList) {
            LatLng location = new LatLng(bookmark.getLatitude(), bookmark.getLongitude());
            Marker marker = new Marker();
            marker.setPosition(location);
            marker.setWidth(60);
            marker.setHeight(60);
            marker.setIcon(OverlayImage.fromResource(R.drawable.image_bookmark_marker));
            if (bookmark.getAlias() == null) {
                marker.setCaptionText(bookmark.getLocationName());
            } else {
                marker.setCaptionText(bookmark.getAlias());
                marker.setTag(bookmark.getLocationName());  // 북마크 이름을 표시
            }
            marker.setMap(naverMap);
            bookmarkMarkers.add(marker);
        }
    }

    public void addAdditionalBookmarkMarkersOnMap(BookMarkDTO bookmark) {
        LatLng location =  new LatLng(bookmark.getLatitude(), bookmark.getLongitude());
        Marker marker = new Marker();
        marker.setPosition(location);
        marker.setWidth(60);
        marker.setHeight(60);
        marker.setIcon(OverlayImage.fromResource(R.drawable.image_bookmark_marker));
        if (bookmark.getAlias() == null) {
            marker.setCaptionText(bookmark.getLocationName());
        } else {
            marker.setCaptionText(bookmark.getAlias());
            marker.setTag(bookmark.getLocationName());  // 북마크 이름을 표시
        }
        marker.setMap(naverMap);
        bookmarkMarkers.add(marker);
        marker.setOnClickListener(overlay -> {
            // 북마크 마커 클릭 시 정보탭 표시
            showBookMarkInfoTab(marker.getPosition(), marker.getCaptionText(), marker.getTag());
            return true; // true를 반환하면 다른 이벤트는 처리되지 않음
        });
    }

    public void updateBookmarkMarkersOnMap(BookMarkDTO bookmark) {
        for (Marker marker : bookmarkMarkers) {
            if (marker.getTag() == null) {
                // 기존 alias가 없었던 경우
                if (marker.getCaptionText().equals(bookmark.getLocationName())) {
                    marker.setMap(null);
                    marker.setCaptionText(bookmark.getAlias());
                    marker.setTag(bookmark.getLocationName());
                    marker.setMap(naverMap);
                }
            } else {
                // 기존 alias가 있을 경우
                if (marker.getTag().toString().equals(bookmark.getLocationName())) {
                    marker.setMap(null);
                    marker.setCaptionText(bookmark.getAlias());
                    marker.setTag(bookmark.getLocationName());
                    marker.setMap(naverMap);
                }
            }
        }
    }

    public void deleteBookmarkMarkersOnMap(String locationName) {
        Iterator<Marker> iterator = bookmarkMarkers.iterator();

        while (iterator.hasNext()) {
            Marker marker = iterator.next();

            if (marker.getTag() == null) {
                if (marker.getCaptionText().equals(locationName)) {
                    marker.setMap(null);
                    iterator.remove(); // 리스트에서 마커 제거
                }
            } else {
                if (marker.getTag().toString().equals(locationName)) {
                    marker.setMap(null);
                    iterator.remove(); // 리스트에서 마커 제거
                }
            }
        }
    }

    private void toggleBookmarkMarkers() {
        if (isBookmarkMarkersVisible) {
            // 마커 숨기기
            for (Marker marker : bookmarkMarkers) {
                marker.setVisible(false);
            }
        } else {
            // 마커 보이기
            for (Marker marker : bookmarkMarkers) {
                marker.setVisible(true);
            }
        }
        isBookmarkMarkersVisible = !isBookmarkMarkersVisible;
    }

    private void showBookMarkInfoTab(LatLng latLng, String title, Object tag) {
        adapter.notifyDataSetChanged();
        overlayContainer.removeAllViews(); // 기존의 뷰를 제거

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View infoView = inflater.inflate(R.layout.location_menu, overlayContainer, false);


        TextView titleView = infoView.findViewById(R.id.location_title);        //이름 나타내는 뷰
        TextView addressView = infoView.findViewById(R.id.location_address);    //주소 나타내는 뷰
        TextView distanceView = infoView.findViewById(R.id.location_distance);  //거리 나타내는 뷰

        titleView.setText(title);

        String address;
        if (tag != null) {
            addressView.setText(tag.toString());
            address = tag.toString();
        }
        else {
            address = title;
        }


        // 현재 위치 가져오기 시도
        fetchUserLocation(location -> {
            if (location != null) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                double distance = calculateDistance(userLatLng, latLng); // 거리 계산

                // 거리 표시: 1000m 이상은 km 단위로, 그 외는 m 단위로 표시
                if (distance >= 1000) {
                    double distanceInKm = distance / 1000.0;
                    distanceView.setText(String.format(Locale.getDefault(), "%.1fkm", distanceInKm));
                } else {
                    int distanceInMeters = (int) distance; // 소수점 제거 및 정수 변환
                    distanceView.setText(String.format(Locale.getDefault(), "%dm", distanceInMeters));
                }
            } else {
                // 위치 정보가 없을 경우 거리를 숨김
                distanceView.setVisibility(View.GONE);
            }
        });

        overlayContainer.addView(infoView);
        overlayContainer.setVisibility(View.VISIBLE);

        // 정보탭에서 사용될 버튼 참조
        AppCompatButton bookmarkButton = infoView.findViewById(R.id.bookmark_button);

        // 버튼 초기화 및 상태 업데이트
        bookmarkButton.setText(bookmarkViewModel.isBookmarked(address) ? "북마크 저장됨" : "북마크 등록");

        // 버튼 클릭 리스너 설정
        bookmarkButton.setOnClickListener(view -> {
            if (bookmarkViewModel.isBookmarked(address)) {
                // 북마크 삭제 로직
                requestBookMark.deleteBookmarkFromServer(address);
                bookmarkButton.setText("북마크 등록");
            } else {
                // 북마크 저장 로직
                requestBookMark.saveBookmarkToServer(new BookMarkDTO(address, latLng.latitude, latLng.longitude));
                bookmarkButton.setText("북마크 저장됨");
            }
        });

    }
    private void hideBookMarkInfoTab() {
        overlayContainer.removeAllViews();
        overlayContainer.setVisibility(View.GONE);
    }


    //그냥마커
    private void addMarker(LatLng latLng, String caption) {
        Marker marker = new Marker();
        marker.setPosition(latLng);
        marker.setCaptionText(caption);
        marker.setMap(naverMap); // 마커를 지도에 추가
        searchMarkers.add(marker); // 서치마커리스트에 추가
        // 마커 클릭 이벤트 리스너 추가
        marker.setOnClickListener(overlay -> {
            //showInfoTab(caption, latLng);  // 마커 클릭 시 InfoTab 표시
            return true;
        });

    }
    //그냥 마커삭제
    private void removeMarkers() {
        // 리스트에 있는 모든 마커를 제거
        for (Marker marker : searchMarkers) {
            marker.setMap(null); // 지도에서 마커 제거
        }
        searchMarkers.clear(); // 리스트 비우기
    }

    private void moveCameraToLocation(LatLng latLng) {
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(latLng).animate(CameraAnimation.Easing);
        naverMap.moveCamera(cameraUpdate); // 카메라를 해당 위치로 이동
        // 키보드 숨기기
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        // 검색 리스트 숨기기
        searchList.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        // FusedLocationSource에서 권한 요청 결과를 처리
        locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) {// 권한 거부됨
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

    }

    private void showTextViewLegendExplanationDialog() {
        // 다이얼로그 객체 생성
        Dialog dialog = new Dialog(this);

        // 다이얼로그에 사용할 레이아웃 설정
        dialog.setContentView(R.layout.dialog_legend_explanation);

        // 다이얼로그 크기 및 위치 설정
        Window window = dialog.getWindow(); // window 객체 가져옴
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(); // LayoutParams 객체를 생성하여 다이얼로그의 크기,위치 설정
            layoutParams.copyFrom(window.getAttributes()); //  window.getAttributes()로부터 얻은 현재 Window의 속성들을 새로 만든 layoutParams 객체에 복사
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT; //다이얼로그의 너비를 내용에 맞게 설정
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; //다이얼로그의 높이를 내용에 맞게 설정
            layoutParams.gravity = Gravity.CENTER;  // 다이얼로그를 화면 가운데로 설정
            window.setAttributes(layoutParams); //설정한 다이얼로그 적용
        }
        // 다이얼로그 표시
        dialog.show();
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        fetchPoliceOffice = new FetchPoliceOffice(MainParentActivity.this, naverMap);
        fetchCCTV = new FetchCCTV(MainParentActivity.this, naverMap);
        fetchChildrenGuardHouse = new FetchChildrenGuardHouse(MainParentActivity.this, naverMap);
        fetchSecurityLamp = new FetchSecurityLamp(MainParentActivity.this, naverMap);
        fetchAccidentBlackSpot = new FetchAccidentBlackSpot(MainParentActivity.this, naverMap);
        fetchchildrenProtectionZone = new FetchChildrenProtectionZone(MainParentActivity.this, naverMap);
        fetchSafetyEmergencyBell = new FetchSafetyEmergencyBell(MainParentActivity.this, naverMap);
        receiveLocationFromServer = new ReceiveLocationFromServer(naverMap);

        bookmarkViewModel = new ViewModelProvider(this).get(BookmarkViewModel.class);


        // 지도에 북마크 마커를 표시하는 Observer
        bookmarkViewModel.getBookmarkList().observe(this, this::addBookmarkMarkersOnMap);
        /* 북마크 마커 버튼 ON/OFF
        AppCompatImageButton btnBookmarkMini = findViewById(R.id.map_book_mark_mini);
        btnBookmarkMini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBookmarkMarkersVisible) {
                    // 현재 마커가 표시되고 있다면, 마커를 제거합니다.
                    clearBookmarkMarkersFromMap();
                } else {
                    // 현재 마커가 표시되지 않고 있다면, 마커를 추가합니다.
                    addBookmarkMarkersOnMap(bookmarkViewModel.getBookmarkList().getValue());
                }
                // 상태 변수를 반전시킵니다.
                isBookmarkMarkersVisible = !isBookmarkMarkersVisible;
            }
        });

         */
        // 지도 UI 설정 (나침반 비활성화, 위치 버튼 활성화)
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setCompassEnabled(false);
        uiSettings.setLocationButtonEnabled(true);

        // 지도 클릭 이벤트 설정
        naverMap.setOnMapClickListener((point, coord) -> {
            if (searchList.getVisibility() == View.VISIBLE) {
                // 리스트가 보이는 경우 리스트만 숨김
                searchList.setVisibility(View.GONE);
            } else {
                // 리스트가 보이지 않을 때는 마커 제거 및 정보 탭 숨김
                removeMarkers();
                hideInfoTab();
                hideBookMarkInfoTab();
                hideOverlayMenus(); // SafeMapMenu와 EyeKeepMenu 닫기
            }
        });
        // 서치마커 클릭 이벤트 설정
        for (Marker marker : searchMarkers) {
            marker.setOnClickListener(overlay -> {
                // 마커 클릭 시 정보탭 표시
                showInfoTab(marker.getPosition(), marker.getCaptionText(), marker.getTag().toString(), marker.getTag().toString());
                return true; // true를 반환하면 다른 이벤트는 처리되지 않음
            });
        }
        // 북마크마커 클릭 이벤트 설정
        for (Marker marker : bookmarkMarkers) {
            marker.setOnClickListener(overlay -> {
                // 북마크 마커 클릭 시 정보탭 표시
                showBookMarkInfoTab(marker.getPosition(), marker.getCaptionText(), marker.getTag());
                return true; // true를 반환하면 다른 이벤트는 처리되지 않음
            });
        }
    }
}