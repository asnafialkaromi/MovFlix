package com.nafi.movflix.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nafi.movflix.data.source.network.model.movie.MovieListResponse
import com.nafi.movflix.data.source.network.service.MovFlixApiService

class NowPlayingPagingSource(private val service: MovFlixApiService) :
    PagingSource<Int, MovieListResponse>() {
    override fun getRefreshKey(state: PagingState<Int, MovieListResponse>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieListResponse> {
        val page = params.key ?: 1
        return try {
            val response =
                service.getNowPlayingMovies(
                    false,
                    false,
                    "en-US",
                    1,
                    "popularity.desc",
                    "2|3",
                    minDate = String(),
                    maxDate = String(),
                )
            LoadResult.Page(
                data = response.data,
                prevKey = if (page == 1) null else page.minus(1),
                nextKey = if (response.data.isEmpty()) null else page.plus(1),
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }
}
