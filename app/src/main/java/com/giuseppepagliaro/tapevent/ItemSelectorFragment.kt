package com.giuseppepagliaro.tapevent

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giuseppepagliaro.tapevent.adapters.ItemSelectableAdapter
import com.giuseppepagliaro.tapevent.adapters.ItemSelectedAdapter
import com.giuseppepagliaro.tapevent.adapters.NoItemsAdapter
import com.giuseppepagliaro.tapevent.nfc.NfcAction
import com.giuseppepagliaro.tapevent.nfc.putIntoBundle
import com.giuseppepagliaro.tapevent.viewmodels.ItemSelectorViewModel


class ItemSelectorFragment<VM : ItemSelectorViewModel>(
    private val getViewModelClass: () -> Class<VM>
) : Fragment(R.layout.fragment_item_selector) {
    private lateinit var viewModel: VM

    private lateinit var noLocationsLayout: LinearLayout
    private lateinit var regularLayout: RelativeLayout

    private lateinit var tvTitle: TextView
    private lateinit var spTitle: Spinner
    private lateinit var rwItemsSelectable: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        viewModel = ViewModelProvider(this)[getViewModelClass()]

        noLocationsLayout = view.findViewById(R.id.lout_no_locations)
        regularLayout = view.findViewById(R.id.lout_regular)
        tvTitle = view.findViewById(R.id.tv_selector_title)
        spTitle = view.findViewById(R.id.sp_selector_title)
        rwItemsSelectable = view.findViewById(R.id.rw_items_selectable)

        val rwItemsSelected: RecyclerView = view.findViewById(R.id.rw_items_selected)
        val btnFinalize: ImageButton = view.findViewById(R.id.btn_selection_finalize)
        val btnClear: ImageButton = view.findViewById(R.id.btn_clear_items)

        // Configure Location Name Views
        viewModel.selectedLocationInd.observe(viewLifecycleOwner) { position ->
            spTitle.setSelection(position)

            if (spTitle.selectedItem != null)
                if (spTitle.selectedItem != viewModel.selectedLocation)
                    viewModel.setSelectedLocation(position)
        }
        viewModel.availableLocations.observe(viewLifecycleOwner) { items ->
            populateLocations(items)
        }
        spTitle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                viewModel.setSelectedLocation(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        // Configure Selectable List
        rwItemsSelectable.layoutManager = GridLayoutManager(context, getSpanCount(context))
        viewModel.selectable.observe(viewLifecycleOwner) { items ->
            if (items.isNotEmpty()) {
                rwItemsSelectable.adapter = ItemSelectableAdapter(
                    context,
                    items,
                    viewModel::selectSelectable
                )
            } else {
                rwItemsSelectable.adapter = NoItemsAdapter(context, viewModel.selectableName)
            }
        }

        // Configure Selected List
        rwItemsSelected.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        viewModel.selected.observe(viewLifecycleOwner) { items ->
            rwItemsSelected.adapter = ItemSelectedAdapter(
                context,
                items,
                viewModel::incrementSelected,
                viewModel::decrementOrRemoveSelected
            )
        }

        // Enable/Disable btnFinalize based on the selected items
        viewModel.selected.observe(viewLifecycleOwner) { items ->
            btnFinalize.isEnabled = !items.isNullOrEmpty()
        }

        btnFinalize.setOnClickListener {
            val nfcArgs = Bundle()
            putIntoBundle(nfcArgs, NfcAction.READ)

            val nfcFragment = NfcFragment()
            nfcFragment.arguments = nfcArgs

            //nfcFragment.show(parentFragmentManager, "nfc_fragment")

            Toast.makeText(context, "Il bottone è attivo", Toast.LENGTH_SHORT).show()

            /* TODO get nfc info
            val clientCode = ""

            viewModel.finalizeTransaction(clientCode)*/
        }
        btnClear.setOnClickListener {
            viewModel.clearSelected()
        }
    }

    private fun populateLocations(locations: List<String>) {
        if (locations.isEmpty()) {
            regularLayout.visibility = View.GONE
            noLocationsLayout.visibility = View.VISIBLE
        } else {
            noLocationsLayout.visibility = View.GONE
            regularLayout.visibility = View.VISIBLE
            if (locations.size > 1) {
                tvTitle.visibility = View.GONE
                spTitle.visibility = View.VISIBLE

                val adapter = ArrayAdapter<CharSequence>(
                    requireContext(),
                    R.layout.fragment_location_item,
                    locations
                )
                spTitle.adapter = adapter

                rwItemsSelectable

                rwItemsSelectable.layoutParams =
                    (rwItemsSelectable.layoutParams as RelativeLayout.LayoutParams).apply {
                        addRule(RelativeLayout.BELOW, R.id.sp_selector_title)
                    }
            } else {
                spTitle.visibility = View.GONE
                tvTitle.visibility = View.VISIBLE

                tvTitle.text = locations[0]

                rwItemsSelectable.layoutParams =
                    (rwItemsSelectable.layoutParams as RelativeLayout.LayoutParams).apply {
                        addRule(RelativeLayout.BELOW, R.id.tv_selector_title)
                    }
            }
        }
    }

    private fun getSpanCount(context: Context): Int {
        val cardWidthDp = 130

        val screenWidth = context.resources.displayMetrics.widthPixels
        val pxDensity = context.resources.displayMetrics.density

        return screenWidth / (cardWidthDp * pxDensity).toInt()
    }
}