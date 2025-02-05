package com.giuseppepagliaro.tapevent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.giuseppepagliaro.tapevent.adapters.ItemSelectableAdapter
import com.giuseppepagliaro.tapevent.adapters.ItemSelectedAdapter
import com.giuseppepagliaro.tapevent.adapters.NoItemsAdapter
import com.giuseppepagliaro.tapevent.nfc.NfcAction
import com.giuseppepagliaro.tapevent.nfc.TapEventNfcProvider
import com.giuseppepagliaro.tapevent.nfc.NfcView
import com.giuseppepagliaro.tapevent.viewmodels.ItemSelectorFragmentViewModel

abstract class ItemSelectorFragment : Fragment(R.layout.fragment_item_selector), NfcView {
    private lateinit var viewModel: ItemSelectorFragmentViewModel
    private lateinit var nfcProvider: TapEventNfcProvider

    private lateinit var noLocationsLayout: LinearLayout
    private lateinit var regularLayout: ConstraintLayout

    private lateinit var tvTitle: TextView
    private lateinit var spTitle: Spinner
    private lateinit var rwItemsSelectable: RecyclerView

    protected abstract val addsNewCustomers: Boolean
    protected abstract fun getViewModelType(): Class<out ItemSelectorFragmentViewModel>

    // L'Intent viene ricevuto dalla Activity e inoltrato al Fragment.
    override fun handleNfcIntent(intent: Intent) = nfcProvider.handle(intent)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[getViewModelType()]
        nfcProvider = initNfcProvider()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        noLocationsLayout = view.findViewById(R.id.lout_no_locations)
        regularLayout = view.findViewById(R.id.lout_regular)
        tvTitle = view.findViewById(R.id.tv_selector_title)
        spTitle = view.findViewById(R.id.sp_selector_title)
        rwItemsSelectable = view.findViewById(R.id.rw_items_selectable)

        val viewRoot: ViewGroup = view.findViewById(R.id.item_selector_root)
        val rwItemsSelected: RecyclerView = view.findViewById(R.id.rw_items_selected)
        val btnFinalize: ImageButton = view.findViewById(R.id.btn_selection_finalize)
        val btnClear: ImageButton = view.findViewById(R.id.btn_clear_items)
        val btnAddCustomer: ImageButton = view.findViewById(R.id.btn_add_customer)

        val listsTransition = AutoTransition().apply {
            duration = 85 // milliseconds
        }

        // Configura le Location Views.
        viewModel.selectedLocationInd.observe(viewLifecycleOwner) { position ->
            spTitle.setSelection(position)

            if (spTitle.selectedItem != null)
                if (spTitle.selectedItem != viewModel.selectedLocation)
                    viewModel.setSelectedLocation(position)
        }
        viewModel.availableLocations.observe(viewLifecycleOwner) { items ->
            populateLocations(items)
        }
        spTitle.onItemSelectedListener = getSpinnerOnItemSelectedListener()

        // Configura la Selectable List.
        rwItemsSelectable.layoutManager = GridLayoutManager(context, getSpanCount(context))
        val selectableAdapter = ItemSelectableAdapter(context, listOf(), viewModel::selectSelectable)
        rwItemsSelectable.adapter = NoItemsAdapter(context, viewModel.selectableName)
        viewModel.selectable.observe(viewLifecycleOwner) { items ->
            TransitionManager.beginDelayedTransition(viewRoot, listsTransition)

            // Mostra gli oggetti o una Text View se non ce ne sono.
            if (items.isNotEmpty()) {
                if (rwItemsSelectable.adapter != selectableAdapter)
                    rwItemsSelectable.adapter = selectableAdapter

                (rwItemsSelectable.adapter as ItemSelectableAdapter).updateItems(items)
            } else {
                rwItemsSelectable.adapter = NoItemsAdapter(context, viewModel.selectableName)
            }

            viewRoot.requestLayout()
        }

        // Configura la Selected List.
        rwItemsSelected.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rwItemsSelected.adapter = ItemSelectedAdapter(
            context,
            listOf(),
            viewModel::incrementSelected,
            viewModel::decrementOrRemoveSelected
        )
        viewModel.selected.observe(viewLifecycleOwner) { items ->
            TransitionManager.beginDelayedTransition(viewRoot, listsTransition)

            // Se non ci sono oggetti selezionati,
            // la recycler view non deve essere mostrata.
            (rwItemsSelected.adapter as ItemSelectedAdapter).updateItems(items)

            viewRoot.requestLayout()
        }

        // Abilita o disabilita il finalize button a seconda della presenza di
        // elementi selezionati.
        viewModel.selected.observe(viewLifecycleOwner) { items ->
            if (!items.isNullOrEmpty())
                btnFinalize.setOnClickListener {
                    nfcProvider.request(NfcAction.READ)
                }
            else
                btnFinalize.setOnClickListener {
                    Toast.makeText(
                        context,
                        context.getString(R.string.item_selector_select_one_item),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        // Configura il bottone Clear
        btnClear.setOnClickListener {
            viewModel.clearSelected()
        }

        // Configura il bottone Add Customer
        if (addsNewCustomers) {
            btnAddCustomer.setOnClickListener {
                nfcProvider.request(NfcAction.WRITE)
            }
        }
        else {
            btnAddCustomer.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        nfcProvider.dispose()
    }

    private fun initNfcProvider(): TapEventNfcProvider {
        if (addsNewCustomers)
            return TapEventNfcProvider(
                requireContext(),
                parentFragmentManager,
                this::onNfcReadResult,
                this::onNfcWriteResult,
                viewModel::getCustomerIdCipherPassphrase,
                viewModel::requestNewCustomerId
            )
        else
            return TapEventNfcProvider(
                requireContext(),
                parentFragmentManager,
                this::onNfcReadResult,
                { _, _ -> throw IllegalStateException("This view does not add customers.") },
                viewModel::getCustomerIdCipherPassphrase
            ) { throw IllegalStateException("This view does not add customers.") }
    }

    // Nfc Read Result Business Logic.
    private fun onNfcReadResult(customerId: String) {
        viewModel.finalizeTransaction(customerId)
        viewModel.clearSelected()
    }

    // Nfc Write Business Logic.
    private fun onNfcWriteResult(wasSuccessful: Boolean, customerId: String) {
        if (wasSuccessful)
            viewModel.confirmCustomerId(customerId)
        else
            viewModel.cancelCustomerId(customerId)
    }

    private fun populateLocations(locations: List<String>) {

        // Se non ci sono Locations, mostra una view con un del testo che
        // lo comunica; altrimenti, mostra il layout vero e proprio.
        if (locations.isEmpty()) {
            regularLayout.visibility = View.GONE
            noLocationsLayout.visibility = View.VISIBLE
        } else {
            noLocationsLayout.visibility = View.GONE
            regularLayout.visibility = View.VISIBLE

            // Se c'è una sola Location, mostra un text view;
            // altrimenti, mostra un dropdown.
            if (locations.size > 1) {
                tvTitle.visibility = View.GONE
                spTitle.visibility = View.VISIBLE

                spTitle.adapter = getSpinnerAdapter(locations)

                // Riapplica i constraint che vengono sovrascritti quando la view è cambiata.
                rwItemsSelectable.layoutParams =
                    (rwItemsSelectable.layoutParams as ConstraintLayout.LayoutParams).apply {
                        topToBottom = R.id.sp_selector_title
                    }
            } else {
                spTitle.visibility = View.GONE
                tvTitle.visibility = View.VISIBLE

                tvTitle.text = locations[0]

                // Riapplica i constraint che vengono sovrascritti quando la view è cambiata.
                rwItemsSelectable.layoutParams =
                    (rwItemsSelectable.layoutParams as ConstraintLayout.LayoutParams).apply {
                        topToBottom = R.id.tv_selector_title
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

    private fun getSpinnerAdapter(locations: List<String>): ArrayAdapter<CharSequence> {
        val adapter = object : ArrayAdapter<CharSequence>(
            requireContext(),
            R.layout.fragment_location_item,
            locations
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)

                // Rimuovi il padding aggiuntivo per l'elemento selezionato.
                view.setPadding(0, 0, 0, 0)
                return view
            }
        }

        // Il padding è applicato solo agli elementi nel dropdown.
        adapter.setDropDownViewResource(R.layout.fragment_location_item)

        return adapter
    }

    private fun getSpinnerOnItemSelectedListener(): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {

            // Aggiorna i Selectable quando una nuova Location è selezionata.
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                viewModel.setSelectedLocation(position)
            }

            // Non fare niente quando la selezione è annullata.
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
    }
}