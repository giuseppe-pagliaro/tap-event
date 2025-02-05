package com.giuseppepagliaro.tapevent

import com.giuseppepagliaro.tapevent.viewmodels.DummyItemSelectorFragmentViewModel
import com.giuseppepagliaro.tapevent.viewmodels.ItemSelectorFragmentViewModel

class DummyItemSelectorFragmentWithCustomerCreation : ItemSelectorFragment() {
    override val addsNewCustomers: Boolean = true

    override fun getViewModelType(): Class<out ItemSelectorFragmentViewModel> {
        return DummyItemSelectorFragmentViewModel::class.java
    }
}

class DummyItemSelectorFragmentNoCustomerCreation : ItemSelectorFragment() {
    override val addsNewCustomers: Boolean = false

    override fun getViewModelType(): Class<out ItemSelectorFragmentViewModel> {
        return DummyItemSelectorFragmentViewModel::class.java
    }
}