package com.bkapps.news.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import com.bkapps.news.R
import com.bkapps.news.api.PostService
import com.bkapps.news.api.PostDataSource
import com.bkapps.news.data.repository.PostRepository
import com.bkapps.news.databinding.ActivityMainBinding
import com.bkapps.news.ui.adapter.CustomLoadStateAdapter
import com.bkapps.news.ui.adapter.PostAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    companion object {
        const val ANGRY = "\uD83D\uDE28 "
    }

    private lateinit var adapter: PostAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: PostsViewModel
    private var selectPosts: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(PostRepository(PostDataSource(PostService.create())))
        )
            .get(PostsViewModel::class.java)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(viewModel = viewModel)
        binding.postRecycler.adapter = adapter

        //add RecyclerView's row
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.postRecycler.addItemDecoration(decoration)

        selectPosts?.cancel()
        selectPosts = lifecycleScope.launch {
            viewModel.posts.collectLatest {
                adapter.submitData(it)
            }
        }

        binding.postRecycler.adapter = adapter.withLoadStateFooter(
            footer = CustomLoadStateAdapter {
                adapter.retry()
            }
        )

        lifecycleScope.launch {
            adapter.loadStateFlow.collect { loadState ->
                val refreshState = loadState.refresh

                binding.postRecycler.isVisible = refreshState is LoadState.NotLoading
                binding.progressBar.isVisible = refreshState is LoadState.Loading
                binding.layoutError.isVisible = refreshState is LoadState.Error

            }
        }

        binding.reloadPostsBtn.setOnClickListener {
            adapter.refresh()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            adapter.refresh()
        }
    }
}