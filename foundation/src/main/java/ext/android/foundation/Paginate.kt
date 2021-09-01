package ext.android.foundation

import androidx.paging.PagingSource
import androidx.paging.PagingState

abstract class PaginateData<T> {
    abstract val hasPrev: Boolean
    abstract val prevNum: Int
    abstract val hasNext: Boolean
    abstract val nextNum: Int
    abstract val list: List<T>
}

/**
 *
 *  fun getPager(): Flow<PagingData<HealthData>> {
 *      return Pager(
 *          config = PagingConfig(pageSize = 30, enablePlaceholders = false),
 *          pagingSourceFactory = {
 *          IntPaginateSource.paginate {
 *              healthDataAPI.getPaginateList("", "", "")
 *          }
 *      }
 *  ).flow
 * }
 *
 */
abstract class IntPaginateSource<V : Any> : PagingSource<Int, V>() {
    companion object {
        inline fun <V : Any> paginate(crossinline loader: suspend (LoadParams<Int>) -> PaginateData<V>) =
            object : IntPaginateSource<V>() {
                override suspend fun loadPaginate(params: LoadParams<Int>): PaginateData<V> =
                    loader(params)
            }
    }

    override fun getRefreshKey(state: PagingState<Int, V>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, V> {
        return try {
            val result = loadPaginate(params)
            LoadResult.Page(
                data = result.list,
                prevKey = if (result.hasPrev) result.prevNum else null,
                nextKey = if (result.hasNext) result.nextNum else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    abstract suspend fun loadPaginate(params: LoadParams<Int>): PaginateData<V>
}