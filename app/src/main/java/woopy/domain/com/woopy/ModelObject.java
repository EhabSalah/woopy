package woopy.domain.com.woopy;

/*-------------------------------

    - woopy -

    Created by cubycode @2017
    All Rights reserved

-------------------------------*/

public enum ModelObject {

    RED(R.string.page1, R.layout.wizard_page1),
    BLUE(R.string.page2, R.layout.wizard_page2),
    GREEN(R.string.page3, R.layout.wizard_page3);


    private int mTitleResId;
    private int mLayoutResId;

    ModelObject(int titleResId, int layoutResId) {
        mTitleResId = titleResId;
        mLayoutResId = layoutResId;
    }

    public int getTitleResId() {
        return mTitleResId;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }

}
