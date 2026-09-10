package com.example.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdMobService {

    private val _isBannerVisible = MutableStateFlow(true)
    val isBannerVisible: StateFlow<Boolean> = _isBannerVisible.asStateFlow()

    private val _isInterstitialShowing = MutableStateFlow(false)
    val isInterstitialShowing: StateFlow<Boolean> = _isInterstitialShowing.asStateFlow()

    private val _isRewardedAdShowing = MutableStateFlow(false)
    val isRewardedAdShowing: StateFlow<Boolean> = _isRewardedAdShowing.asStateFlow()

    private val _isProUnlocked = MutableStateFlow(false)
    val isProUnlocked: StateFlow<Boolean> = _isProUnlocked.asStateFlow()

    private val _unlockedRewardItem = MutableStateFlow<String?>(null)
    val unlockedRewardItem: StateFlow<String?> = _unlockedRewardItem.asStateFlow()

    var onInterstitialDismissed: (() -> Unit)? = null
    var onRewardedSuccess: ((String) -> Unit)? = null

    fun showInterstitialAd(onDismiss: () -> Unit) {
        if (_isProUnlocked.value) {
            onDismiss()
            return
        }
        onInterstitialDismissed = onDismiss
        _isInterstitialShowing.value = true
    }

    fun dismissInterstitial() {
        _isInterstitialShowing.value = false
        val callback = onInterstitialDismissed
        onInterstitialDismissed = null
        callback?.invoke()
    }

    fun showRewardedAd(rewardName: String, onSuccess: (String) -> Unit) {
        _unlockedRewardItem.value = rewardName
        onRewardedSuccess = onSuccess
        _isRewardedAdShowing.value = true
    }

    fun completeRewardedAd() {
        val item = _unlockedRewardItem.value ?: "Premium Feature"
        _isRewardedAdShowing.value = false
        val callback = onRewardedSuccess
        onRewardedSuccess = null
        callback?.invoke(item)
    }

    fun dismissRewardedAd() {
        _isRewardedAdShowing.value = false
        onRewardedSuccess = null
    }

    fun toggleProStatus(unlocked: Boolean) {
        _isProUnlocked.value = unlocked
    }

    fun hideBanner() {
        _isBannerVisible.value = false
    }
}
