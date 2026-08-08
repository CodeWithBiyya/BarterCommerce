//package com.example.signuplogina
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.View
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.navigation.fragment.NavHostFragment
//import com.example.signuplogina.databinding.ActivityMainBinding
//import com.example.signuplogina.databinding.ActivityRegisterBinding
//import com.google.android.material.bottomnavigation.BottomNavigationView
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMainBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Initialize ViewBinding
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//
//        // Set up the NavHostFragment and Navigation Controller
//        val navHostFragment = supportFragmentManager
//            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        val navController = navHostFragment.navController
//
//        // Set up the BottomNavigationView with the NavController
//        val bottomNavigationView: BottomNavigationView = binding.bottomNavigation
//        NavigationUI.setupWithNavController(bottomNavigationView, navController)
//
//        // Floating Action Button click listener to navigate to AddItemFragment
//
//        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.nav_home -> {
//                    navController.navigate(R.id.)
//                    true
//                }
//
//                R.id.nav_search -> {
//                    navController.navigate(R.id.searchFragment)
//                    true
//                }
//                R.id.fab -> {
//                    navController.navigate(R.id.addItemFragment)
//                    true
//                }
//
//                else -> false
//            }
//        }
////        binding.buttonLogin.setOnClickListener {
////            val intent= Intent(this, LoginActivity::class.java)
////            startActivity(intent)
////        }
////
////        binding.buttonRegister.setOnClickListener {
////            val intent= Intent(this, RegisterActivity::class.java)
////            startActivity(intent)
////        }
//        // Set up FAB click listener
//        binding.fab.setOnClickListener {
//            // Hide FAB when navigating to the AddItemFragment
//            binding.fab.visibility = View.GONE
//
//            // Navigate to the AddItemFragment
//            navController.navigate(R.id.action_homeFragment_to_addItemFragment)
////            navController.navigate(R.id.action_searchFragment_to_addItemFragment)
//        }
//    }
//    override fun onResume() {
//        super.onResume()
//        // Make FAB visible again when returning to the HomeFragment
//        binding.fab.visibility = View.VISIBLE
//        }
//}
