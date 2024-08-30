package com.example.eyekeep;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MenuViewModel extends ViewModel {

    public enum MenuType {
        SAFETY_MAP,
        EYEKEEP,
        BOOKMARK,
        MY_INFORMATION,
        NONE  // 메뉴가 비활성화된 상태
    }

    private final MutableLiveData<MenuType> currentMenu = new MutableLiveData<>(MenuType.NONE);

    public LiveData<MenuType> getCurrentMenu() {
        return currentMenu;
    }

    public void setCurrentMenu(MenuType menuType) {
        // 현재 메뉴와 동일한 메뉴를 선택한 경우, 메뉴를 닫음
        if (currentMenu.getValue() == menuType) {
            currentMenu.setValue(MenuType.NONE);
        } else {
            currentMenu.setValue(menuType);
        }

    }
}
