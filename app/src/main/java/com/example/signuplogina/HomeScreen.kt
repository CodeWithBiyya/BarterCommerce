package com.example.signuplogina

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.signuplogina.databinding.ActivityHomeScreenBinding

class HomeScreen : AppCompatActivity() {
    private var _binding: ActivityHomeScreenBinding? = null
    private val binding get() = _binding!!

    // Handler to schedule auto-scroll tasks
    private val handler = Handler(Looper.getMainLooper())
    private var autoScrollPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        _binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView with a GridLayoutManager (for main products)
        binding.productGrid.layoutManager = GridLayoutManager(this, 2) // 2 columns
        binding.button3.setOnClickListener {
            val intent= Intent(this@HomeScreen,AddItem::class.java)
            startActivity(intent)
        }
        // Sample data for the RecyclerView (main product grid)
        val products = listOf(
            Product("Computers", R.drawable.ic_cart),
            Product("Furniture", R.drawable.handshake),
            Product("Headphones", R.drawable.ic_mobile),
            Product("Bags", R.drawable.box),
            Product("Furniture", R.drawable.handshake),
            Product("Headphones", R.drawable.ic_mobile),
            Product("Bags", R.drawable.box)
        )

        val productAdapter = ProductAdapter(products)
        binding.productGrid.adapter = productAdapter

        // Setup RecyclerView for Latest Items (Horizontal Layout)
        binding.latestItemsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Sample data for latest items (you can modify this list as needed)
        val latestItems = listOf(
            Product("New Phone", R.drawable.box),
            Product("New Laptop", R.drawable.ic_toysandgames),
            Product("New Watch", R.drawable.handshake),
            Product("New Shoes", R.drawable.buying)
        )

        val latestItemsAdapter = ProductAdapter(latestItems)
        binding.latestItemsRecyclerView.adapter = latestItemsAdapter

        // Start auto-scrolling the latest items
        startAutoScroll(latestItems.size)
    }

    // Method to start auto-scrolling
    private fun startAutoScroll(itemCount: Int) {
        val runnable = object : Runnable {
            override fun run() {
                // Check if binding is not null
                if (_binding != null) {
                    // Scroll to the next item position
                    binding.latestItemsRecyclerView.smoothScrollToPosition(autoScrollPosition)

                    // Increment position and reset to 0 if it exceeds itemCount
                    autoScrollPosition = (autoScrollPosition + 1) % itemCount

                    // Schedule the next scroll after 2 seconds
                    handler.postDelayed(this, 2000)
                }
            }
        }

        // Start the scrolling task
        handler.post(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove any pending posts of the runnable to prevent memory leaks or crashes
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}
