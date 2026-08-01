package com.michaldrabik.ui_base.events

sealed class Event

object ReloadData : Event()

// Trakt Sync

object TraktSyncAuthError : Event()

// Floppy Sync

object FloppySyncStart : Event()

object FloppySyncProgress : Event()

object FloppySyncSuccess : Event()

object FloppySyncError : Event()

// Trakt Instant Sync

data class TraktQuickSyncSuccess(
  val count: Int,
) : Event()

object TraktListQuickSyncSuccess : Event()

// Shows, Movies Sync

data class ShowsMoviesSyncComplete(
  val count: Int,
) : Event()
