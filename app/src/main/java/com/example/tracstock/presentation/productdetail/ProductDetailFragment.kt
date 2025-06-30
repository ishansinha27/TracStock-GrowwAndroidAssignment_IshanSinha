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
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = ContextCompat.getColor(context, R.color.colorTextPrimary)
                valueFormatter = DateAxisValueFormatter()
                setLabelCount(4, true)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                textColor = ContextCompat.getColor(context, R.color.colorTextPrimary)
            }

            axisRight.isEnabled = false
            legend.isEnabled = false
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
                        dateTimeString
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    dateTimeString
                }
            }
            return ""
        }
    }

    private fun observeViewModel() {
        viewModel.companyOverview.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.detailProgressBar.visibility = View.VISIBLE
                    binding.detailErrorTextView.visibility = View.GONE
                    binding.detailsContainer.visibility = View.GONE
                    binding.companyDescriptionTextView.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.detailProgressBar.visibility = View.GONE
                    binding.detailErrorTextView.visibility = View.GONE
                    binding.detailsContainer.visibility = View.VISIBLE
                    binding.companyDescriptionTextView.visibility = View.VISIBLE
                    resource.data?.let { overview ->
                        binding.companyNameTextView.text = overview.name
                        binding.companySymbolTextView.text = overview.symbol
                        binding.companyDescriptionTextView.text = overview.description
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

        viewModel.isStockInAnyWatchlist.observe(viewLifecycleOwner) { isInWatchlist ->
            if (isInWatchlist) {
                binding.addToWatchlistButton.setText(R.string.remove_from_watchlist)
                binding.addToWatchlistButton.setIconResource(R.drawable.ic_watchlist_remove)
                binding.addToWatchlistButton.setIconTintResource(R.color.colorLoss)
                binding.addToWatchlistButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorLoss))
            } else {
                binding.addToWatchlistButton.setText(R.string.add_to_watchlist)
                binding.addToWatchlistButton.setIconResource(R.drawable.ic_watchlist_add)
                binding.addToWatchlistButton.setIconTintResource(R.color.colorPrimary)
                binding.addToWatchlistButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            }
        }

        viewModel.watchlistEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ProductDetailViewModel.WatchlistOperationEvent.StockRemoved -> {
                    binding.root.showSnackbar(getString(R.string.stock_removed_from_watchlist, event.stockSymbol))
                }
                is ProductDetailViewModel.WatchlistOperationEvent.StockAdded -> {
                    binding.root.showSnackbar(getString(R.string.stock_added_to_watchlist_success, event.stockSymbol))
                }
                is ProductDetailViewModel.WatchlistOperationEvent.ShowMessage -> {
                    binding.root.showSnackbar(event.message)
                }
            }
        }
    }

    private fun displayChart(historicalPrices: List<HistoricalPrice>) {
        val entries = ArrayList<Entry>()
        val dates = ArrayList<String>()

        val sortedPrices = historicalPrices.sortedBy { DateUtils.dateStringToTimestamp(it.date) }

        sortedPrices.forEachIndexed { index, price ->
            val adjustedClose = price.adjustedClose.toFloatOrNull() ?: 0f
            entries.add(Entry(index.toFloat(), adjustedClose))
            dates.add(DateUtils.formatChartDate(price.date))
        }

        val dataSet = LineDataSet(entries, "Adjusted Close Price").apply {
            color = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.colorTextSecondary)
            setDrawCircles(false)
            setDrawValues(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.LINEAR
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.chart_gradient_fill)
            highLightColor = Color.rgb(244, 117, 117)
        }

        val lineData = LineData(dataSet)
        binding.priceLineChart.data = lineData
        binding.priceLineChart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(dates)
        binding.priceLineChart.invalidate()
    }

    private fun setupClickListeners() {
        binding.addToWatchlistButton.setOnClickListener {
            val isCurrentlyInWatchlist = viewModel.isStockInAnyWatchlist.value ?: false
            if (isCurrentlyInWatchlist) {
                val currentOverview = viewModel.companyOverview.value?.data
                val currentSymbol = viewModel.stockSymbol
                if (currentOverview != null && currentSymbol.isNotBlank()) {
                    val stock = Stock(
                        symbol = currentSymbol,
                        name = currentOverview.name,
                        price = viewModel.currentPrice.value?.split(" ")?.get(0) ?: "0.0",
                        currency = viewModel.currentPrice.value?.split(" ")?.getOrElse(1) { "USD" } ?: "USD"
                    )
                    viewModel.removeStockFromAnyWatchlist(stock)
                } else {
                    binding.root.showSnackbar("Stock details not fully loaded yet for removal.")
                }
            } else {
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
        viewModel.checkWatchlistStatus(viewModel.stockSymbol)
    }
}
