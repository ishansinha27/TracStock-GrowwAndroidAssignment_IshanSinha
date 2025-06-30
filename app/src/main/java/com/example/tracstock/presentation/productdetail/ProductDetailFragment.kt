package com.example.tracstock.presentation.productdetail

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels

import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.tracstock.R
import com.example.tracstock.databinding.FragmentProductDetailBinding
import com.example.tracstock.domain.model.HistoricalPrice
import com.example.tracstock.domain.model.Stock
import com.example.tracstock.util.DateUtils
import com.example.tracstock.util.Resource
import com.example.tracstock.util.showSnackbar
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductDetailViewModel by viewModels()
    private val args: ProductDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title = args.symbol

        setupChart()
        observeViewModel()
        setupClickListeners()

        if (viewModel.stockSymbol.isBlank()) {
            viewModel.fetchStockDetails(args.symbol)
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("stockAddedToWatchlist")
            ?.observe(viewLifecycleOwner) { isAdded ->
                if (isAdded) {
                    viewModel.onStockAddedToWatchlist(viewModel.stockSymbol)
                }
            }



    }

    private fun setupChart() {
        binding.priceLineChart.apply {
            description.isEnabled = false // No description text
            setTouchEnabled(true) // Enable touch gestures
            isDragEnabled = true // Enable dragging
            setScaleEnabled(true) // Enable scaling
            setPinchZoom(true) // Enable pinch zoom

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = ContextCompat.getColor(context, R.color.colorTextPrimary)
                // Use a custom ValueFormatter for date/time
                valueFormatter = DateAxisValueFormatter() // Set custom formatter
                setLabelCount(4, true)
            }

            // Left Y-Axis (Prices) configuration
            axisLeft.apply {
                setDrawGridLines(true) // Horizontal grid lines
                textColor = ContextCompat.getColor(context, R.color.colorTextPrimary) // Text color
            }

            // Right Y-Axis (Disabled)
            axisRight.isEnabled = false // No right Y-axis

            legend.isEnabled = false // No legend
        }
    }
    inner class DateAxisValueFormatter : com.github.mikephil.charting.formatter.ValueFormatter() {
        private val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        private val outputFormat = SimpleDateFormat("HH:mm\nMMM dd", Locale.getDefault())

        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            val historicalData = viewModel.historicalData.value?.data
            if (index >= 0 && index < historicalData?.size ?: 0) {
                val dateTimeString = historicalData?.get(index)?.date ?: ""
                return try {
                    val date: Date? = inputFormat.parse(dateTimeString)
                    if (date != null) {
                        outputFormat.format(date)
                    } else {
                        dateTimeString // Fallback if parsing fails
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    dateTimeString // Fallback on parsing error
                }
            }
            return ""
        }
    }







    private fun observeViewModel() {
        // Observe Company Overview
        viewModel.companyOverview.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.detailProgressBar.visibility = View.VISIBLE
                    binding.detailErrorTextView.visibility = View.GONE
                    binding.detailsContainer.visibility = View.GONE // Hide details while loading
                    binding.companyDescriptionTextView.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.detailProgressBar.visibility = View.GONE
                    binding.detailErrorTextView.visibility = View.GONE
                    binding.detailsContainer.visibility = View.VISIBLE
                    binding.companyDescriptionTextView.visibility = View.VISIBLE
                    resource.data?.let { overview ->
                        // Update UI with company overview data
                        binding.companyNameTextView.text = overview.name
                        binding.companySymbolTextView.text = overview.symbol
                        binding.companyDescriptionTextView.text = overview.description

                        // Populate overview details
                        binding.sectorTextView.text = overview.sector
                        binding.industryTextView.text = overview.industry
                        binding.marketCapTextView.text = overview.marketCapitalization
                        binding.peRatioTextView.text = overview.peRatio
                        binding.dividendYieldTextView.text = overview.dividendYield
                        binding.weekHighTextView.text = overview.high52Week
                        binding.weekLowTextView.text = overview.low52Week
                    }
                }
                is Resource.Error -> {
                    binding.detailProgressBar.visibility = View.GONE
                    binding.detailsContainer.visibility = View.GONE
                    binding.companyDescriptionTextView.visibility = View.GONE
                    binding.detailErrorTextView.visibility = View.VISIBLE
                    binding.detailErrorTextView.text = resource.message ?: getString(R.string.error_loading_details)
                    binding.root.showSnackbar(resource.message ?: getString(R.string.error_loading_details))
                }
            }
        }
        viewModel.currentPrice.observe(viewLifecycleOwner) { priceString ->
            binding.currentPriceTextView.text = priceString
        }

        // Observe Historical Data for Chart
        viewModel.historicalData.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.chartProgressBar.visibility = View.VISIBLE
                    binding.chartErrorTextView.visibility = View.GONE
                    binding.priceLineChart.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.chartProgressBar.visibility = View.GONE
                    binding.chartErrorTextView.visibility = View.GONE
                    binding.priceLineChart.visibility = View.VISIBLE

                    resource.data?.let { historicalPrices ->
                        if (historicalPrices.isNotEmpty()) {
                            displayChart(historicalPrices)
                        } else {
                            binding.priceLineChart.visibility = View.GONE
                            binding.chartErrorTextView.visibility = View.VISIBLE
                            binding.chartErrorTextView.text = getString(R.string.error_loading_chart_data) + "\nNo data available."
                        }
                    }
                }
                is Resource.Error -> {
                    binding.chartProgressBar.visibility = View.GONE
                    binding.priceLineChart.visibility = View.GONE
                    binding.chartErrorTextView.visibility = View.VISIBLE
                    binding.chartErrorTextView.text = resource.message ?: getString(R.string.error_loading_chart_data)
                    binding.root.showSnackbar(resource.message ?: getString(R.string.error_loading_chart_data))
                }
            }
        }

        // Observe watchlist status to update the button/icon
        viewModel.isStockInAnyWatchlist.observe(viewLifecycleOwner) { isInWatchlist ->
            if (isInWatchlist) {
                binding.addToWatchlistButton.setText(R.string.remove_from_watchlist)
                binding.addToWatchlistButton.setIconResource(R.drawable.ic_watchlist_remove) // You need to create this icon
                binding.addToWatchlistButton.setIconTintResource(R.color.colorLoss) // Use red for remove
                binding.addToWatchlistButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorLoss))
            } else {
                binding.addToWatchlistButton.setText(R.string.add_to_watchlist)
                binding.addToWatchlistButton.setIconResource(R.drawable.ic_watchlist_add)
                binding.addToWatchlistButton.setIconTintResource(R.color.colorPrimary) // Use primary color for add
                binding.addToWatchlistButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            }
        }

        viewModel.watchlistEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ProductDetailViewModel.WatchlistOperationEvent.StockRemoved -> {
                    binding.root.showSnackbar(getString(R.string.stock_removed_from_watchlist, event.stockSymbol))
                    // No explicit checkWatchlistStatus call here as it's done in ViewModel
                }
                is ProductDetailViewModel.WatchlistOperationEvent.StockAdded -> { // <<<< NEW EVENT HANDLER
                    binding.root.showSnackbar(getString(R.string.stock_added_to_watchlist_success, event.stockSymbol))
                    // No explicit checkWatchlistStatus call here as it's done in ViewModel
                }
                is ProductDetailViewModel.WatchlistOperationEvent.ShowMessage -> {
                    binding.root.showSnackbar(event.message)
                }
            }
        }
    }



    /**
     * Populates and updates the LineChart with historical price data.
     */
    private fun displayChart(historicalPrices: List<HistoricalPrice>) {
        val entries = ArrayList<Entry>()
        val dates = ArrayList<String>()

        // Ensure data is sorted by date ascending for correct chart plotting
        val sortedPrices = historicalPrices.sortedBy { DateUtils.dateStringToTimestamp(it.date) }

        sortedPrices.forEachIndexed { index, price ->
            val adjustedClose = price.adjustedClose.toFloatOrNull() ?: 0f
            entries.add(Entry(index.toFloat(), adjustedClose))
            dates.add(DateUtils.formatChartDate(price.date)) // Store formatted date for X-axis labels
        }

        val dataSet = LineDataSet(entries, "Adjusted Close Price").apply {
            color = ContextCompat.getColor(requireContext(), R.color.colorPrimary) // Line color
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.colorTextSecondary) // Value text color
            setDrawCircles(false) // Don't draw circles on data points
            setDrawValues(false) // Don't draw value text on data points
            lineWidth = 2f
            mode = LineDataSet.Mode.LINEAR // Straight lines between points
            setDrawFilled(true) // Fill area below the line
            fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.chart_gradient_fill) // Custom gradient fill drawable
            highLightColor = Color.rgb(244, 117, 117) // Color for highlighted points
        }

        val lineData = LineData(dataSet)
        binding.priceLineChart.data = lineData

        // Update X-axis labels with formatted dates
        binding.priceLineChart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(dates)
        binding.priceLineChart.invalidate() // Refresh the chart
    }

    /**
     * Sets up click listeners for UI elements.
     */
    private fun setupClickListeners() {
        binding.addToWatchlistButton.setOnClickListener {
            val isCurrentlyInWatchlist = viewModel.isStockInAnyWatchlist.value ?: false
            if (isCurrentlyInWatchlist) {
                // If currently in watchlist, remove it
                val currentOverview = viewModel.companyOverview.value?.data
                val currentSymbol = viewModel.stockSymbol
                if (currentOverview != null && currentSymbol.isNotBlank()) {
                    val stock = Stock(
                        symbol = currentSymbol,
                        name = currentOverview.name,
                        price = viewModel.currentPrice.value?.split(" ")?.get(0) ?: "0.0", // Use current price from LiveData
                        currency = viewModel.currentPrice.value?.split(" ")?.getOrElse(1) { "USD" } ?: "USD"
                    )
                    viewModel.removeStockFromAnyWatchlist(stock)
                } else {
                    binding.root.showSnackbar("Stock details not fully loaded yet for removal.")
                }
            } else {
                // If not in watchlist, add it (navigate to dialog)
                val currentOverview = viewModel.companyOverview.value?.data
                val currentPriceText = viewModel.currentPrice.value ?: "0.0 USD"
                val currentPriceValue = currentPriceText.split(" ")[0]
                val currentCurrency = currentPriceText.split(" ").getOrElse(1) { "USD" }

                if (currentOverview != null && viewModel.stockSymbol.isNotBlank()) {
                    val stockName = currentOverview.name
                    val action = ProductDetailFragmentDirections.actionProductDetailFragmentToAddWatchlistDialogFragment(
                        stockSymbol = viewModel.stockSymbol,
                        stockName = stockName,
                        stockPrice = currentPriceValue,
                        stockCurrency = currentCurrency,
                        isAddToWatchlist = true
                    )
                    findNavController().navigate(action)
                } else {
                    binding.root.showSnackbar("Stock details not fully loaded yet for adding.")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        // Re-check watchlist status when fragment resumes (e.g., after dialog closes)
        viewModel.checkWatchlistStatus(viewModel.stockSymbol)
    }
}