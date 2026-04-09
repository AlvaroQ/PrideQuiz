package com.quiz.pride.managers

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

private const val REMOVE_AD = "remove_ad"

/**
 * Resultado de una operacion de compra.
 */
sealed class PurchaseResult {
    data object Success : PurchaseResult()
    data object AlreadyOwned : PurchaseResult()
    data object Canceled : PurchaseResult()
    data object Pending : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}

/**
 * Gestiona el ciclo de vida del BillingClient de Google Play.
 * Es un singleton que se registra en Koin como `single`.
 */
class BillingManager(private val context: Context) {

    private var billingClient: BillingClient? = null
    private var isReady = false

    private val _purchaseResult = MutableSharedFlow<PurchaseResult>(
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val purchaseResult = _purchaseResult.asSharedFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when {
            billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null -> {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            }
            billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED -> {
                emitResult(PurchaseResult.Canceled)
            }
            billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                emitResult(PurchaseResult.AlreadyOwned)
            }
            else -> {
                emitResult(PurchaseResult.Error("BillingResponseCode: ${billingResult.responseCode}"))
            }
        }
    }

    /**
     * Inicializa y conecta el BillingClient. Debe llamarse cuando la pantalla
     * que necesita billing entra en composicion.
     */
    fun initBilling() {
        if (billingClient?.isReady == true) return

        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .setListener(purchasesUpdatedListener)
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                isReady = billingResult.responseCode == BillingClient.BillingResponseCode.OK
            }

            override fun onBillingServiceDisconnected() {
                isReady = false
            }
        })
    }

    /**
     * Lanza el flujo de compra de "remove_ad" para la Activity dada.
     * Si el cliente no esta listo, emite [PurchaseResult.Error].
     */
    fun launchBillingFlow(activity: Activity) {
        val client = billingClient
        if (!isReady || client == null) {
            emitResult(PurchaseResult.Error("BillingClient no esta listo"))
            return
        }

        val productList = ImmutableList.of(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(REMOVE_AD)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(queryProductDetailsParams) { _, result ->
            val productDetailsList = result.productDetailsList
            for (productDetails in productDetailsList) {
                if (productDetails.productId == REMOVE_AD) {
                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                    val flowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()
                    client.launchBillingFlow(activity, flowParams)
                    break
                }
            }
        }
    }

    /**
     * Consulta las compras existentes del usuario para restaurarlas.
     * Si el usuario ya compro "remove_ad", emite [PurchaseResult.AlreadyOwned].
     */
    fun restorePurchases() {
        val client = billingClient
        if (!isReady || client == null) {
            emitResult(PurchaseResult.Error("BillingClient no esta listo"))
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasRemoveAd = purchases.any {
                    it.products.contains(REMOVE_AD) &&
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (hasRemoveAd) {
                    emitResult(PurchaseResult.AlreadyOwned)
                } else {
                    emitResult(PurchaseResult.Error("NO_PURCHASES_FOUND"))
                }
            } else {
                emitResult(PurchaseResult.Error("QueryPurchases failed: ${billingResult.responseCode}"))
            }
        }
    }

    /**
     * Libera los recursos del BillingClient. Debe llamarse cuando la pantalla
     * que usa billing sale de composicion.
     */
    fun release() {
        billingClient?.endConnection()
        billingClient = null
        isReady = false
    }

    private fun handlePurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                acknowledgePurchase(purchase)
                emitResult(PurchaseResult.Success)
            }
            Purchase.PurchaseState.PENDING -> {
                emitResult(PurchaseResult.Pending)
            }
            else -> {
                emitResult(PurchaseResult.Error("Estado de compra no reconocido: ${purchase.purchaseState}"))
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient?.acknowledgePurchase(params) { _ -> }
    }

    private fun emitResult(result: PurchaseResult) {
        // tryEmit es non-suspending; funciona para SharedFlow con replay=0
        _purchaseResult.tryEmit(result)
    }
}
