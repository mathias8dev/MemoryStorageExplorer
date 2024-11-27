package com.mathias8dev.memoriesstoragexplorer.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import timber.log.Timber


@Composable
@Destination
@RootNavGraph
fun PageScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {

        val pageState = rememberPagerState { 100 }
        var swipeDirection by rememberSaveable { mutableStateOf("Swipe left or right") }

        var previousPage by rememberSaveable {
            mutableIntStateOf(pageState.currentPage)
        }
        LaunchedEffect(pageState) {
            snapshotFlow { pageState.currentPage }.collect {
                previousPage = it
            }
        }

        HorizontalPager(
            modifier = Modifier
                .fillMaxSize(),
            state = pageState,
            userScrollEnabled = false
        ) { page ->


            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Page ${System.currentTimeMillis()}")
                Text(swipeDirection)

            }
        }
    }
}

@Composable
fun InfiniteHorizontalPager(
    pageCount: Int,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
    content: @Composable PagerScope.(page: Int) -> Unit,
) {
    val max = Short.MAX_VALUE.toInt()
    val half = max / 2

    val pagerPositionIndex = initialPage + half - half % pageCount
    val pagerState = rememberPagerState(pageCount = { max }, initialPage = pagerPositionIndex)

    LaunchedEffect(pagerPositionIndex, pageCount) {
        pagerState.scrollToPage(pagerPositionIndex)
        Timber.d("pagerPositionIndex: $pagerPositionIndex")
    }

    HorizontalPager(
        state = pagerState,
        userScrollEnabled = pageCount > 1,
        modifier = modifier,
    ) { index ->
        Timber.d("The current index is $index and the page count is $pageCount and the index % pageCount is ${index % pageCount}")
        val page = index % pageCount
        this.content(page)
    }
}