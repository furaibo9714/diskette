package io.github.furaibo9714.diskette.ui_base.viewmodel

import io.github.furaibo9714.diskette.ui_base.utilities.events.Event
import io.github.furaibo9714.diskette.ui_base.utilities.events.MessageEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class DefaultChannelsDelegate : ChannelsDelegate {

  override val messageChannel = Channel<MessageEvent>(Channel.BUFFERED)
  override val messageFlow = messageChannel.receiveAsFlow()

  override val eventChannel = Channel<Event<*>>(Channel.BUFFERED)
  override val eventFlow = eventChannel.receiveAsFlow()
}
