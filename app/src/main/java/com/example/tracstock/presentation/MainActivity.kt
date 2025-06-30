package com.example.tracstock.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tracstock.R
import com.example.tracstock.databinding.ActivityMainBinding
import com.example.tracstock.domain.usecase.explore.GetTopGainers
import com.example.tracstock.domain.usecase.explore.GetTopLosers
import com.example.tracstock.domain.usecase.explore.SearchStocks
import com.example.tracstock.domain.usecase.productdetail.GetCompanyOverview
import com.example.tracstock.domain.usecase.productdetail.GetCompanyOverview_Factory
import com.example.tracstock.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavView.setupWithNavController(navController)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.exploreFragment, // This ID MUST match your <fragment> ID in nav_graph.xml and <item> ID in bottom_nav_menu.xml
                R.id.watchlistFragment // This ID MUST match your <fragment> ID in nav_graph.xml and <item> ID in bottom_nav_menu.xml
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}