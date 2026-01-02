package com.zanjaprogrammer.warungku.ads;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.zanjaprogrammer.warungku.R;

/**
 * Utility class for smooth ad container animations
 */
public class AdAnimationUtils {
    private static final int ANIMATION_DURATION_MS = 300;
    
    /**
     * Animate ad container showing with smooth slide-up effect
     */
    public static void showAdContainer(ViewGroup adContainer) {
        if (adContainer == null || adContainer.getVisibility() == View.VISIBLE) {
            return;
        }
        
        // Measure the container height
        adContainer.measure(
            View.MeasureSpec.makeMeasureSpec(adContainer.getWidth(), View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        final int targetHeight = adContainer.getMeasuredHeight();
        
        // Start with height 0
        adContainer.getLayoutParams().height = 0;
        adContainer.setVisibility(View.VISIBLE);
        adContainer.setAlpha(0f);
        
        // Animate height and alpha
        ValueAnimator heightAnimator = ValueAnimator.ofInt(0, targetHeight);
        heightAnimator.setDuration(ANIMATION_DURATION_MS);
        heightAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        heightAnimator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = adContainer.getLayoutParams();
            layoutParams.height = animatedValue;
            adContainer.setLayoutParams(layoutParams);
            
            // Fade in alpha
            float progress = animation.getAnimatedFraction();
            adContainer.setAlpha(progress);
        });
        
        heightAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Ensure final state
                ViewGroup.LayoutParams layoutParams = adContainer.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                adContainer.setLayoutParams(layoutParams);
                adContainer.setAlpha(1f);
            }
        });
        
        heightAnimator.start();
    }
    
    /**
     * Animate ad container hiding with smooth slide-down effect
     */
    public static void hideAdContainer(ViewGroup adContainer) {
        if (adContainer == null || adContainer.getVisibility() != View.VISIBLE) {
            return;
        }
        
        final int initialHeight = adContainer.getHeight();
        
        // Animate height and alpha
        ValueAnimator heightAnimator = ValueAnimator.ofInt(initialHeight, 0);
        heightAnimator.setDuration(ANIMATION_DURATION_MS);
        heightAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        heightAnimator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = adContainer.getLayoutParams();
            layoutParams.height = animatedValue;
            adContainer.setLayoutParams(layoutParams);
            
            // Fade out alpha
            float progress = 1f - animation.getAnimatedFraction();
            adContainer.setAlpha(progress);
        });
        
        heightAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                adContainer.setVisibility(View.GONE);
                adContainer.setAlpha(1f);
                
                // Reset layout params
                ViewGroup.LayoutParams layoutParams = adContainer.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                adContainer.setLayoutParams(layoutParams);
            }
        });
        
        heightAnimator.start();
    }
    
    /**
     * Animate ad loading state with subtle pulse effect
     */
    public static void showLoadingAnimation(ViewGroup adContainer) {
        if (adContainer == null) {
            return;
        }
        
        // Create a subtle pulse animation
        ValueAnimator pulseAnimator = ValueAnimator.ofFloat(0.8f, 1.0f);
        pulseAnimator.setDuration(1000);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        pulseAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            adContainer.setScaleX(scale);
            adContainer.setScaleY(scale);
            adContainer.setAlpha(scale);
        });
        
        // Store animator in tag for later cancellation
        adContainer.setTag(R.id.ad_loading_animator, pulseAnimator);
        pulseAnimator.start();
    }
    
    /**
     * Stop loading animation and restore normal state
     */
    public static void stopLoadingAnimation(ViewGroup adContainer) {
        if (adContainer == null) {
            return;
        }
        
        // Cancel existing animation
        Object animatorTag = adContainer.getTag(R.id.ad_loading_animator);
        if (animatorTag instanceof ValueAnimator) {
            ((ValueAnimator) animatorTag).cancel();
            adContainer.setTag(R.id.ad_loading_animator, null);
        }
        
        // Restore normal state
        adContainer.setScaleX(1.0f);
        adContainer.setScaleY(1.0f);
        adContainer.setAlpha(1.0f);
    }
    
    /**
     * Animate ad refresh with subtle fade effect
     */
    public static void animateAdRefresh(ViewGroup adContainer, Runnable onRefreshComplete) {
        if (adContainer == null) {
            return;
        }
        
        // Fade out
        ValueAnimator fadeOut = ValueAnimator.ofFloat(1.0f, 0.3f);
        fadeOut.setDuration(150);
        fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());
        
        fadeOut.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            adContainer.setAlpha(alpha);
        });
        
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Trigger refresh
                if (onRefreshComplete != null) {
                    onRefreshComplete.run();
                }
                
                // Fade back in
                ValueAnimator fadeIn = ValueAnimator.ofFloat(0.3f, 1.0f);
                fadeIn.setDuration(150);
                fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
                
                fadeIn.addUpdateListener(anim -> {
                    float alpha = (float) anim.getAnimatedValue();
                    adContainer.setAlpha(alpha);
                });
                
                fadeIn.start();
            }
        });
        
        fadeOut.start();
    }
}