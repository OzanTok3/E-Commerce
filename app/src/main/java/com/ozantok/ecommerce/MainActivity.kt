package com.ozantok.ecommerce

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ozantok.ecommerce.ui.cart.CartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val cartVm: CartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setupWithNavController(navController)

        val cartMenuId = R.id.cartFragment
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cartVm.count.collect { c ->
                    if (c > 0) {
                        val badge = bottomNav.getOrCreateBadge(cartMenuId)
                        badge.isVisible = true
                        badge.number = c
                        badge.backgroundColor =
                            ContextCompat.getColor(this@MainActivity, R.color.red)
                        badge.badgeTextColor =
                            ContextCompat.getColor(this@MainActivity, android.R.color.white)
                        badge.maxCharacterCount = 2
                    } else {
                        bottomNav.removeBadge(cartMenuId)
                    }
                }
            }
        }
    }
}
