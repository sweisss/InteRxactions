package com.example.interxactions.ui

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.interxactions.R
import com.example.interxactions.data.database.SearchedDrug
import com.example.interxactions.data.database.SearchedDrugViewModel
import com.example.interxactions.utils.titleCaseWithExceptions
import com.google.android.material.snackbar.Snackbar

class RxSearchFragment : Fragment(R.layout.rx_search_fragment) {
    private lateinit var searchButton: ImageButton
    private lateinit var searchBox: EditText
    private lateinit var radioGroup: RadioGroup
    private lateinit var searchedDrugList: RecyclerView

    private val searchedDrugsViewModel: SearchedDrugViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchedDrugList = view.findViewById(R.id.searched_drug_list)
        searchBox = view.findViewById(R.id.et_search_box)
        searchButton = view.findViewById(R.id.btn_navigate)
        radioGroup = view.findViewById(R.id.radio_group_search)

        setupSearchListener()
        setupSearchButton()

        searchedDrugList.layoutManager = LinearLayoutManager(requireContext())
        searchedDrugList.setHasFixedSize(true)

        val adapter = SearchedDrugsAdapter(::onRecentlySearchedDrugClicked)
        searchedDrugList.adapter = adapter

        searchedDrugsViewModel.searchedDrugs.observe(viewLifecycleOwner) { drugs ->
            adapter.updateDrugs(drugs.toMutableList())
            searchedDrugList.scrollToPosition(0)
        }

        val itemTouchCallBack =
            object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val searchedDrug = adapter.getItemAt(viewHolder.absoluteAdapterPosition)

                    searchedDrugsViewModel.deleteDrugByName(searchedDrug.drugName)
                }
            }

        ItemTouchHelper(itemTouchCallBack).attachToRecyclerView(searchedDrugList)
    }

    private fun setupSearchListener() {
        searchBox.setOnEditorActionListener { _, actionId, event ->
            val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)

            if (isSearchAction) {
                if (getSearchDrugName().isNotEmpty()) {
                    performSearch()
                } else {
                    showEmptyQueryError()
                }
            }
            isSearchAction
        }
    }

    private fun setupSearchButton() {
        searchButton.setOnClickListener {
            if (getSearchDrugName().isNotEmpty()) {
                performSearch()
            } else {
                showEmptyQueryError()
            }
        }
    }

    private fun getSearchDrugName(): String = titleCaseWithExceptions(searchBox.text.toString().trim())

    private fun performSearch() {
        val searchDrugName = getSearchDrugName()

        // Get the selected radio button ID
        val selectedId = radioGroup.checkedRadioButtonId
        val selectedOption = when (selectedId) {
            R.id.radio_option_1 -> "brand_name"
            R.id.radio_option_2 -> "generic_name"
            else -> "Unknown"
        }
        Log.d("RxSearchFragment", "Search query: $searchDrugName, Selected option: $selectedOption")

        searchedDrugsViewModel.addSearchedDrug(SearchedDrug(searchDrugName, System.currentTimeMillis()))
        findNavController().navigate(RxSearchFragmentDirections.navigateToDrugReport())
    }

    private fun showEmptyQueryError() {
        Snackbar.make(
            searchBox,
            "Please enter or select a drug to search.",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun onRecentlySearchedDrugClicked(drug: SearchedDrug) {
        Log.d("RxSearchFragment", "Clicked on drug: $drug")

        searchedDrugsViewModel.addSearchedDrug(SearchedDrug(
            drug.drugName,
            System.currentTimeMillis()
        ))

        val directions = RxSearchFragmentDirections.navigateToDrugReport()
        findNavController().navigate(directions)
    }

    override fun onResume() {
        super.onResume()
        searchBox.text.clear()
    }
}
