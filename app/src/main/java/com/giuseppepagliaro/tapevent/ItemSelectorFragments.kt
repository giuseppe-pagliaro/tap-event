package com.giuseppepagliaro.tapevent

import com.giuseppepagliaro.tapevent.viewmodels.DummyItemSelectorViewModel
import com.giuseppepagliaro.tapevent.viewmodels.ItemSelectorViewModel

class DummyItemSelectorFragmentWithCustomerCreation : ItemSelectorFragment() {
    override val addsNewCustomers: Boolean = true

    override fun getViewModelType(): Class<out ItemSelectorViewModel> {
        return DummyItemSelectorViewModel::class.java
    }
}

class DummyItemSelectorFragmentNoCustomerCreation : ItemSelectorFragment() {
    override val addsNewCustomers: Boolean = false

    override fun getViewModelType(): Class<out ItemSelectorViewModel> {
        return DummyItemSelectorViewModel::class.java
    }
}