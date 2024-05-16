package com.nafi.movflix.presentation.viewmore

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nafi.movflix.data.model.Movie
import com.nafi.movflix.data.repository.ViewMorePagingRepository
import kotlinx.coroutines.flow.Flow

class ViewMoreViewModel(private val extras: Bundle?, private val repository: ViewMorePagingRepository) : ViewModel() {
    val header = extras?.getString(ViewMoreActivity.HEADER)

    fun topRatedMovies(): Flow<PagingData<Movie>> = repository.getTopRatedList().cachedIn(viewModelScope)

    fun nowPlayingMovies(): Flow<PagingData<Movie>> = repository.getNowPlayingList().cachedIn(viewModelScope)

    fun popularMovies(): Flow<PagingData<Movie>> = repository.getPopularList().cachedIn(viewModelScope)

    fun upComingMovies(): Flow<PagingData<Movie>> = repository.getUpComingList().cachedIn(viewModelScope)
}
