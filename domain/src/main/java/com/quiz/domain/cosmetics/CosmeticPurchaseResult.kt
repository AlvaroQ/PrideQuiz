package com.quiz.domain.cosmetics

sealed class CosmeticPurchaseResult {
    data class Success(val item: Unlockable, val newBalance: CurrencyBalance) : CosmeticPurchaseResult()
    data class InsufficientFunds(val needed: Int, val have: Int, val currency: String) : CosmeticPurchaseResult()
    data class AlreadyOwned(val itemId: String) : CosmeticPurchaseResult()
    data class ConditionNotMet(val condition: UnlockCondition) : CosmeticPurchaseResult()
}
