package com.zanjaprogrammer.warungku;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.zanjaprogrammer.warungku.adapters.ShoppingAdapter;
import com.zanjaprogrammer.warungku.databinding.ActivityShoppingListBinding;
import com.zanjaprogrammer.warungku.viewmodel.AppViewModel;

public class ShoppingListActivity extends AppCompatActivity {

    private ActivityShoppingListBinding binding;
    private AppViewModel viewModel;
    private ShoppingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityShoppingListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Use singleton instance untuk persist cart across activities
        viewModel = AppViewModel.getInstance(getApplication());
        setupRecyclerView();

        viewModel.getShoppingList().observe(this, products -> {
            adapter.setProducts(products);
            
            // Show/hide empty state
            if (products == null || products.isEmpty()) {
                binding.rvShopping.setVisibility(android.view.View.GONE);
                binding.layoutEmptyState.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.rvShopping.setVisibility(android.view.View.VISIBLE);
                binding.layoutEmptyState.setVisibility(android.view.View.GONE);
            }
        });

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        // Empty state button
        binding.btnAddProducts.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, StockActivity.class));
            finish();
        });
    }

    private void setupRecyclerView() {
        adapter = new ShoppingAdapter();
        binding.rvShopping.setLayoutManager(new LinearLayoutManager(this));
        binding.rvShopping.setAdapter(adapter);
    }
}
