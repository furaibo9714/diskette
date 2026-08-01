package com.michaldrabik.ui_base.events

sealed class Event

object ReloadData : Event()

// Floppy Sync

object FloppySyncStart : Event()

object FloppySyncProgress : Event()

object FloppySyncSuccess : Event()

object FloppySyncError : Event()

// Shows, Movies Sync

data class ShowsMoviesSyncComplete(
  val count: Int,
) : Event()
