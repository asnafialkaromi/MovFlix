package com.nafi.movflix.presentation.viewmore

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nafi.movflix.R
import com.nafi.movflix.data.model.Movie
import com.nafi.movflix.databinding.ActivityViewMoreBinding
import com.nafi.movflix.databinding.SheetShareBinding
import com.nafi.movflix.databinding.SheetViewBinding
import com.nafi.movflix.presentation.viewmore.adapter.ViewMoreAdapter
import com.nafi.movflix.utils.proceedWhen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ViewMoreActivity : AppCompatActivity() {
    private val binding: ActivityViewMoreBinding by lazy {
        ActivityViewMoreBinding.inflate(layoutInflater)
    }

    private val viewMoreViewModel: ViewMoreViewModel by viewModel {
        parametersOf(intent.extras)
    }

    private val viewMoreAdapter: ViewMoreAdapter by lazy {
        ViewMoreAdapter { movie ->
            showBottomSheetDialog(movie)
        }
    }

    companion object {
        const val HEADER = "HEADER"

        fun startActivity(
            context: Context,
            header: String,
        ) {
            val intent = Intent(context, ViewMoreActivity::class.java)
            intent.putExtra(HEADER, header)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupMovieTopRatedList()
        observeData()
        setClickListener()
    }

    private fun observeData() {
        viewMoreViewModel.header.let {
            binding.tvHeaderViewMore.text = it
            when (it) {
                "Top Rated" -> getTopRated()
                "Popular" -> getPopular()
                "Upcoming" -> getUpcoming()
                "Now Playing" -> getNowPlaying()
            }
        }
    }

    private fun setClickListener() {
        binding.ivArrowBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupMovieTopRatedList() {
        binding.rvMovieList.apply {
            adapter = viewMoreAdapter
        }
    }

    private fun getTopRated() {
        lifecycleScope.launch {
            viewMoreViewModel.topRatedMovies().collectLatest { pagingData ->
                viewMoreAdapter.submitData(pagingData)
            }
        }
    }

    private fun getUpcoming() {
        lifecycleScope.launch {
            viewMoreViewModel.upComingMovies().collectLatest { pagingData ->
                viewMoreAdapter.submitData(pagingData)
            }
        }
    }

    private fun getNowPlaying() {
        lifecycleScope.launch {
            viewMoreViewModel.nowPlayingMovies().collectLatest { pagingData ->
                viewMoreAdapter.submitData(pagingData)
            }
        }
    }

    private fun getPopular() {
        lifecycleScope.launch {
            viewMoreViewModel.popularMovies().collectLatest { pagingData ->
                viewMoreAdapter.submitData(pagingData)
            }
        }
    }

    private fun showBottomSheetDialog(movie: Movie) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetBinding = SheetViewBinding.inflate(layoutInflater)
        bottomSheetBinding.apply {
            ivBannerFilm.load("${getString(R.string.text_link_image)}${movie.backdropPath}")
            ivPoster.load("${getString(R.string.text_link_image)}${movie.posterPath}")
            tvTitleFilm.text = movie.title
            tvDescFilm.text = movie.desc
            tvRelease.text = movie.releaseDate
            tvRating.text = movie.voteAverage.toString()
        }

        bottomSheetBinding.btnShared.setOnClickListener {
            bottomSheetDialog.dismiss()
            showBottomSheetShare(movie)
        }
        checkMovieIsList(movie, bottomSheetBinding)

        bottomSheetDialog.setContentView(bottomSheetBinding.root)
        bottomSheetDialog.show()
    }

    private fun showBottomSheetShare(movie: Movie) {
        val shareBottomSheetDialog = BottomSheetDialog(this)
        val shareBottomSheetBinding = SheetShareBinding.inflate(layoutInflater)
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        shareBottomSheetBinding.apply {
            tvTitleFilm.text = movie.title
            tvUrlFilm.text = (getString(R.string.text_url_poster, movie.posterPath))
            btnCopyUrl.setOnClickListener {
                val clip = ClipData.newPlainText(getString(R.string.text_label_url), tvUrlFilm.text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(
                    this@ViewMoreActivity,
                    getString(R.string.text_url_incopy),
                    Toast.LENGTH_SHORT,
                ).show()
            }
            btnQuickShare.setOnClickListener {
                val posterUrl = "${getString(R.string.text_link_image)}${movie.posterPath}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(posterUrl))
                startActivity(intent)
            }
        }
        shareBottomSheetDialog.setContentView(shareBottomSheetBinding.root)
        shareBottomSheetDialog.show()
    }

    private fun setClickAddList(
        detail: Movie,
        bottomSheetBinding: SheetViewBinding,
    ) {
        bottomSheetBinding.btnList.setOnClickListener {
            addToList(detail)
        }
    }

    private fun setClickRemoveList(
        movieId: Int?,
        bottomSheetBinding: SheetViewBinding,
    ) {
        bottomSheetBinding.btnList.setOnClickListener {
            removeFromList(movieId)
        }
    }

    private fun checkMovieIsList(
        data: Movie,
        bottomSheetBinding: SheetViewBinding,
    ) {
        viewMoreViewModel.checkMovieList(data.id).observe(
            this,
        ) { isList ->
            if (isList.isEmpty()) {
                bottomSheetBinding.btnList.setIconResource(R.drawable.ic_add)
                setClickAddList(data, bottomSheetBinding)
            } else {
                bottomSheetBinding.btnList.setIconResource(R.drawable.ic_plus)
                setClickRemoveList(data.id, bottomSheetBinding)
            }
        }
    }

    private fun removeFromList(movieId: Int?) {
        viewMoreViewModel.removeFromList(movieId).observe(this) {
            it.proceedWhen(
                doOnSuccess = {
                    Toast.makeText(
                        this@ViewMoreActivity,
                        getString(R.string.text_done_delete_to_list),
                        Toast.LENGTH_SHORT,
                    ).show()
                },
                doOnError = {
                    Toast.makeText(
                        this@ViewMoreActivity,
                        getString(R.string.text_failed_delete_to_list),
                        Toast.LENGTH_SHORT,
                    ).show()
                },
            )
        }
    }

    private fun addToList(detail: Movie) {
        viewMoreViewModel.addToList(detail).observe(this) {
            it.proceedWhen(
                doOnSuccess = {
                    Toast.makeText(
                        this@ViewMoreActivity,
                        getString(R.string.text_done_add_to_list),
                        Toast.LENGTH_SHORT,
                    ).show()
                },
                doOnError = {
                    Toast.makeText(
                        this@ViewMoreActivity,
                        getString(R.string.text_failed_add_to_list),
                        Toast.LENGTH_SHORT,
                    ).show()
                },
            )
        }
    }
}
