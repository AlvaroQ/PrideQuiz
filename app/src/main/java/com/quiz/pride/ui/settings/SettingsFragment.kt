package com.quiz.pride.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.android.billingclient.api.*
import com.google.common.collect.ImmutableList
import com.quiz.pride.BuildConfig
import com.quiz.pride.R
import com.quiz.pride.common.startActivity
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.ui.moreApps.MoreAppsActivity
import com.quiz.pride.utils.rateApp
import com.quiz.pride.utils.shareApp
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : PreferenceFragmentCompat(), PurchasesUpdatedListener {
    private val settingsViewModel: SettingsViewModel by viewModel()
    private lateinit var billingClient: BillingClient

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBillingClient()

        // rate_app
        val rateApp: Preference? = findPreference("rate_app")
        rateApp?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            rateApp(requireContext())
            false
        }

        // share
        val share: Preference? = findPreference("share")
        share?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            shareApp(-1, requireContext())
            false
        }

        // Version
        val version: Preference? = findPreference("version")
        version?.summary = "${getString(R.string.settings_version)} ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})"

        // more_apps
        val moreApps: Preference? = findPreference("more_apps")
        moreApps?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            activity?.startActivity<MoreAppsActivity> {}
            false
        }

        val deleteAds: Preference? = findPreference("delete_ads")
        deleteAds?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            loadProductParams()
            false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsViewModel.showingAds.observe(viewLifecycleOwner, Observer(::loadAd))
    }

    private fun loadAd(model: SettingsViewModel.UiModel) {
        if (model is SettingsViewModel.UiModel.ShowAd)
            (activity as SettingsActivity).showAd(model.show)
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(requireContext())
            .enablePendingPurchases()
            .setListener(this)
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    AnalyticsManager.analyticsScreenViewed("Billing setup finished")
                }
            }

            override fun onBillingServiceDisconnected() {
                AnalyticsManager.analyticsScreenViewed("Billing Service Disconnected")
            }
        })
    }

    private fun loadProductParams() = if (billingClient.isReady) {
        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    ImmutableList.of(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(REMOVE_AD)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()))
                .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                for (productDetails in productDetailsList) {
                    if (productDetails.productId == REMOVE_AD) {
                        val productDetailsParamsList = listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                        )

                        val productDetailsParams = BillingFlowParams
                            .newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build()
                        billingClient.launchBillingFlow(requireActivity(), productDetailsParams)
                    }
                }
            }
        }

    } else {
        Toast.makeText(requireContext(), "Billing Client not ready", Toast.LENGTH_SHORT).show()
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            AnalyticsManager.analyticsScreenViewed("billing_purchase_ok")
            settingsViewModel.savePaymentDone()
            for (purchase in purchases) {
                acknowledgePurchase(purchase.purchaseToken)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Toast.makeText(requireContext(), "Billing USER_CANCELED", Toast.LENGTH_SHORT).show()
            AnalyticsManager.analyticsScreenViewed("billing_purchase_canceled")

        } else if(billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            AnalyticsManager.analyticsScreenViewed("billing_already_purchase")
            settingsViewModel.savePaymentDone()
        } else {
            // Handle any other error codes.
            Toast.makeText(requireContext(), "Billing errors", Toast.LENGTH_SHORT).show()
            AnalyticsManager.analyticsScreenViewed("billing_purchase_error")
        }
    }


    private fun acknowledgePurchase(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { billingResult ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage

            AnalyticsManager.analyticsScreenViewed("acknowledgePurchase responseCode=$responseCode")
            AnalyticsManager.analyticsScreenViewed("acknowledgePurchase debugMessage=$debugMessage")
        }
    }

    companion object {
        const val REMOVE_AD = "remove_ad"
    }
}