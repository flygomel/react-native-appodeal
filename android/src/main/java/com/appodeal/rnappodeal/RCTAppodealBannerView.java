package com.appodeal.rnappodeal;

import android.app.Activity;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.appodeal.ads.AdType;
import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.BannerView;
import com.appodeal.ads.MrecCallbacks;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;


public class RCTAppodealBannerView extends ReactViewGroup implements BannerCallbacks, MrecCallbacks {
    private enum BannerSize {
        PHONE,
        TABLET,
        MREC
    }

    private View adView;
    private BannerSize size;
    private String placement;

    private final Runnable measureAndLayout = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.measure(
                        MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY)
                );
                child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
            }
        }
    };

    public RCTAppodealBannerView(ThemedReactContext context) {
        super(context);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        post(measureAndLayout);
    }

    public void setAdSize(String adSize) {
        int adType;

        if (adSize.equals("tablet")) {
            size = BannerSize.TABLET;
            adType = Appodeal.BANNER_VIEW;
        } else if (adSize.equals("mrec")) {
            size = BannerSize.MREC;
            adType = Appodeal.MREC;
        } else {
            size = BannerSize.PHONE;
            adType = Appodeal.BANNER_VIEW;
        }

        if (!Appodeal.isAutoCacheEnabled(adType)) {
            Activity activity = getReactContext().getCurrentActivity();
            if (activity != null) {
                Appodeal.cache(activity, adType);
            }
        }

        hideBannerView();
        setupAdView();
    }

    public void setPlacement(String placement) {
        this.placement = placement;
    }

    private void setupAdView() {
        Activity activity = getReactContext().getCurrentActivity();
        if (activity == null || this.adView != null) {
            return;
        }

        View adView;

        switch (size) {
            case MREC:
                adView = Appodeal.getMrecView(activity);
                break;
            case TABLET:
                Appodeal.set728x90Banners(true);
                adView = Appodeal.getBannerView(activity);
                break;
            default:
                Appodeal.set728x90Banners(false);
                adView = Appodeal.getBannerView(activity);
                break;
        }

        addView(adView);
        this.adView = adView;
    }

    public void hideBannerView() {
        if (adView != null) {
            removeView(adView);
            adView = null;
        }
    }

    private void showBannerView() {
        Activity activity = getReactContext().getCurrentActivity();
        if (activity == null) {
            return;
        }

        int height;
        int adType;

        Resources r = getReactContext().getResources();
        DisplayMetrics dm = r.getDisplayMetrics();

        switch (size) {
            case MREC:
                adType = Appodeal.MREC;
                height = 250;
                break;
            case TABLET:
                adType = Appodeal.BANNER_VIEW;
                height = 90;
                Appodeal.set728x90Banners(true);
                break;
            default:
                adType = Appodeal.BANNER_VIEW;
                height = 50;
                break;
        }

        setupAdView();

        if (adView == null) {
            return;
        }

        int pxW = r.getDisplayMetrics().widthPixels;
        int pxH = dp2px(height, dm);

        adView.setVisibility(VISIBLE);
        adView.setLayoutParams(new BannerView.LayoutParams(pxW, pxH));

        if (this.placement != null) {
            Appodeal.show(activity, adType, placement);
        } else {
            Appodeal.show(activity, adType);
        }
    }

    private ReactContext getReactContext() {
        return (ReactContext)getContext();
    }

    private RCTEventEmitter getEmitter() {
        return getReactContext().getJSModule(RCTEventEmitter.class);
    }

    private int dp2px(int dp, DisplayMetrics dm) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        showBannerView();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        hideBannerView();
    }

    @Override
    public void onBannerLoaded(int height, boolean isPrecache) {
        WritableMap params = Arguments.createMap();
        params.putInt("height", height);
        params.putBoolean("isPrecache", isPrecache);
        getEmitter().receiveEvent(getId(), "onBannerLoaded", params);
    }

    @Override
    public void onBannerFailedToLoad() {
        getEmitter().receiveEvent(getId(), "onBannerFailedToLoad", null);
    }

    @Override
    public void onBannerClicked() {
        getEmitter().receiveEvent(getId(), "onBannerClicked", null);
    }

    @Override
    public void onBannerExpired() {
        getEmitter().receiveEvent(getId(), "onBannerExpired", null);
    }

    @Override
    public void onBannerShowFailed() { }

    @Override
    public void onBannerShown() { }

    @Override
    public void onMrecLoaded(boolean isPrecache) {
        WritableMap params = Arguments.createMap();
        params.putInt("height", 250);
        params.putBoolean("isPrecache", isPrecache);
        getEmitter().receiveEvent(getId(), "onBannerLoaded", params);
    }

    @Override
    public void onMrecFailedToLoad() {
        getEmitter().receiveEvent(getId(), "onBannerFailedToLoad", null);
    }

    @Override
    public void onMrecClicked() {
        getEmitter().receiveEvent(getId(), "onBannerClicked", null);
    }

    @Override
    public void onMrecExpired() {
        getEmitter().receiveEvent(getId(), "onBannerExpired", null);
    }

    @Override
    public void onMrecShowFailed() { }

    @Override
    public void onMrecShown() { }
}
