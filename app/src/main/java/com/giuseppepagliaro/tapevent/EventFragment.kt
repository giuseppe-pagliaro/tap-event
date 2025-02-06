package com.giuseppepagliaro.tapevent

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.giuseppepagliaro.tapevent.adapters.ItemDisplayableAdapter
import com.giuseppepagliaro.tapevent.nfc.NfcAction
import com.giuseppepagliaro.tapevent.nfc.TapEventNfcProvider
import com.giuseppepagliaro.tapevent.nfc.NfcView
import com.giuseppepagliaro.tapevent.viewmodels.EventFragmentViewModel

abstract class EventFragment : Fragment(R.layout.fragment_event), NfcView {
    private lateinit var viewModel: EventFragmentViewModel
    private lateinit var nfcProvider: TapEventNfcProvider

    abstract fun getViewModelFactory(): EventFragmentViewModel.Factory

    override fun handleNfcIntent(intent: Intent) = nfcProvider.handle(intent)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            getViewModelFactory()
        )[EventFragmentViewModel::class.java]

        nfcProvider = TapEventNfcProvider(
            requireContext(),
            parentFragmentManager,
            this::onNfcReadResult,
            { _, _ -> throw IllegalStateException("This view does not add customers") },
            viewModel.getCustomerIdCipherPassphrase,
            { throw IllegalStateException("This view does not add customers") }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        val listsTransition = AutoTransition().apply {
            duration = 85 // milliseconds
        }

        val tvName: TextView = view.findViewById(R.id.tv_event_name)
        val tvDate: TextView = view.findViewById(R.id.tv_event_date)
        val btnCheckBalance: Button = view.findViewById(R.id.btn_check_balance)
        val cardViewTickets: CardView = view.findViewById(R.id.card_view_tickets)
        val cardViewProducts: CardView = view.findViewById(R.id.card_view_products)
        val rwViewTickets: RecyclerView = view.findViewById(R.id.rw_event_tickets)
        val rwViewProducts: RecyclerView = view.findViewById(R.id.rw_event_products)

        fun redrawCardView() {
            if (view.height == 0) return

            // Riconfiguro l'altezza delle Card View
            val cardViewsHeight = calculateCardViewHeight(
                view.height,
                tvName.height,
                tvDate.height,
                btnCheckBalance.height
            )
            cardViewTickets.layoutParams.height = cardViewsHeight
            cardViewTickets.requestLayout()
            cardViewProducts.layoutParams.height = cardViewsHeight
            cardViewProducts.requestLayout()
        }

        // Assicura che le CardView siano dimensionate dopo l'Inflate
        // (prima, view.height è 0).
        view.post {
            redrawCardView()
        }

        viewModel.name.observe(viewLifecycleOwner) { name ->
            if (name.isNullOrEmpty()) return@observe

            tvName.text = name
            redrawCardView()
        }

        viewModel.date.observe(viewLifecycleOwner) { date ->
            if (date == null) return@observe

            tvDate.text = date.toString()
            redrawCardView()
        }

        rwViewTickets.layoutManager = LinearLayoutManager(context)
        rwViewTickets.adapter = ItemDisplayableAdapter(
            context,
            listOf()
        )
        viewModel.tickets.observe(viewLifecycleOwner) { tickets ->
            if (tickets.isNullOrEmpty()) return@observe

            val viewGroup = cardViewTickets as ViewGroup
            TransitionManager.beginDelayedTransition(viewGroup, listsTransition)

            (rwViewTickets.adapter as ItemDisplayableAdapter).updateItems(tickets)

            viewGroup.requestLayout()
        }

        rwViewProducts.layoutManager = LinearLayoutManager(context)
        rwViewProducts.adapter = ItemDisplayableAdapter(
            context,
            listOf()
        )
        viewModel.products.observe(viewLifecycleOwner) { products ->
            if (products.isNullOrEmpty()) return@observe

            val viewGroup = cardViewProducts as ViewGroup
            TransitionManager.beginDelayedTransition(viewGroup, listsTransition)

            (rwViewProducts.adapter as ItemDisplayableAdapter).updateItems(products)

            viewGroup.requestLayout()
        }

        btnCheckBalance.setOnClickListener {
            nfcProvider.request(NfcAction.READ)
        }
    }

    private fun calculateCardViewHeight(
        fragmentHeight: Int,
        eventNameHeight: Int,
        eventDateHeight: Int,
        balanceBtnHeight: Int
    ): Int {
        val context = requireContext()

        val singleMarginHeightDp = 20
        val marginCount = 6

        val pixelDensity = context.resources.displayMetrics.density

        val margins = singleMarginHeightDp * pixelDensity * marginCount

        return ((fragmentHeight - margins - eventNameHeight - eventDateHeight - balanceBtnHeight) / 2).toInt()
    }

    private fun onNfcReadResult(clientCod: String) {
        val intent = Intent(requireContext(), TicketsListActivity::class.java)
        intent.putExtra("client_cod", clientCod)

        startActivity(intent)
    }
}