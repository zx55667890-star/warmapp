package com.example.myapplication.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ChatMessage
import com.example.myapplication.domain.chat.FetchOpponentUseCase
import com.example.myapplication.domain.chat.ObserveMessagesUseCase
import com.example.myapplication.domain.chat.RecallMessageUseCase
import com.example.myapplication.domain.chat.SendMediaUseCase
import com.example.myapplication.domain.chat.SendTextMessageUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(
    private val observeMessagesUseCase: ObserveMessagesUseCase,
    private val sendTextMessageUseCase: SendTextMessageUseCase,
    private val sendMediaUseCase: SendMediaUseCase,
    private val recallUseCase: RecallMessageUseCase,
    private val fetchOpponentUseCase: FetchOpponentUseCase,
) : ViewModel() {
    companion object {
        private const val PENDING_MATCH_TIMEOUT_WINDOW_MS = 86_400_000L // 24h
    }

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ChatEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<ChatEvent> = _events.asSharedFlow()

    private val _chatroomId = MutableStateFlow<String?>(null)
    private val _userId = MutableStateFlow<String?>(null)
    private var myRole = ""

    val mediaSender = ChatMediaSender(sendMediaUseCase, viewModelScope).also {
        it.onPendingAdded = { pendingMsg ->
            _uiState.update { state -> state.copy(pendingMessages = state.pendingMessages + pendingMsg) }
        }
        it.onPendingRemoved = { id ->
            _uiState.update { state -> state.copy(pendingMessages = state.pendingMessages.filter { p -> p.id != id }) }
        }
        it.onMessageAdded = { msg ->
            _uiState.update { state -> state.copy(messages = state.messages + msg) }
        }
        it.onScrollToBottom = { _events.tryEmit(ChatEvent.ScrollToBottom) }
        it.onShowSnackbar = { _events.tryEmit(ChatEvent.ShowSnackbar(it)) }
    }

    val globalImageUrls: StateFlow<List<String>> = _uiState
        .map { state ->
            (state.messages + state.pendingMessages)
                .flatMap { it.imageUrls.ifEmpty { listOf(it.imageUrl) } }
                .filter { it.isNotBlank() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        observeChatDataStreams()
    }

    fun initChat(chatroomId: String, userId: String, role: String) {
        if (_chatroomId.value == chatroomId) return
        _chatroomId.value = chatroomId
        _userId.value = userId
        myRole = role
        _uiState.update {
            it.copy(
                messages = emptyList(), pendingMessages = emptyList(),
                isInitialLoading = true, isChatActive = true, chatEndedByOther = false
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeChatDataStreams() {
        viewModelScope.launch {
            combine(
                _chatroomId.filterNotNull(),
                _userId.filterNotNull()
            ) { roomId, uid -> roomId to uid }
                .flatMapLatest { (roomId, uid) ->
                    combine(
                        observeMessagesUseCase.observeMessages(roomId),
                        observeMessagesUseCase.observeTypingStatus(roomId, uid),
                        observeMessagesUseCase.observeChatStatus(roomId)
                    ) { messagesResult, isTyping, isEnded ->
                        Triple(messagesResult, isTyping, isEnded)
                    }
                }.collect { (messagesResult, isTyping, isEnded) ->
                    val prevSize = _uiState.value.messages.size
                    _uiState.update { state ->
                        val uid = _userId.value ?: ""
                        
                        // Strip temporary uploaded_ placeholders; real Firebase data is authoritative
                        val filteredMessages = state.messages.filter { !it.id.startsWith("uploaded_") }
                        
                        // 找出這次更新中「新增」的、且是我發送的多媒體訊息
                        val newConfirmedMediaMsgs = messagesResult.messages.filter { newMsg ->
                            filteredMessages.none { oldMsg -> oldMsg.id == newMsg.id } &&
                            newMsg.senderId == uid && 
                            (newMsg.imageUrl.isNotBlank() || newMsg.videoUrl.isNotBlank() || newMsg.voiceUrl.isNotBlank() || newMsg.imageUrls.isNotEmpty() || newMsg.text.isBlank())
                        }.toMutableList()
                        
                        // 先將伺服器傳來的訊息繼承我們原本已經綁定好 localId 與 localImageUrls
                        var mappedMessages = messagesResult.messages.map { newMsg ->
                            val existingMsg = filteredMessages.find { it.id == newMsg.id }
                            if (existingMsg != null && existingMsg.localId.isNotBlank()) {
                                newMsg.copy(
                                    localId = existingMsg.localId,
                                    localImageUrls = existingMsg.localImageUrls
                                )
                            } else {
                                newMsg
                            }
                        }
                        
                        val newPending = state.pendingMessages.filter { pending ->
                            val match = newConfirmedMediaMsgs
                                .filter { kotlin.math.abs(it.timestamp - pending.timestamp) < PENDING_MATCH_TIMEOUT_WINDOW_MS }
                                .minByOrNull { kotlin.math.abs(it.timestamp - pending.timestamp) }
                                
                            if (match != null) {
                                newConfirmedMediaMsgs.remove(match)
                                mappedMessages = mappedMessages.map { 
                                    if (it.id == match.id) {
                                        it.copy(
                                            localId = pending.localId.takeIf { id -> id.isNotBlank() } ?: pending.id,
                                            localImageUrls = if (pending.videoUrl.isNotBlank()) emptyList() else pending.localImageUrls,
                                            isCameraCapture = pending.isCameraCapture
                                        )
                                    } else it 
                                }
                                false
                            } else {
                                true
                            }
                        }

                        state.copy(
                            messages = mappedMessages,
                            pendingMessages = newPending,
                            hasMoreMessages = messagesResult.hasMore,
                            isInitialLoading = false,
                            isOtherTyping = isTyping,
                            chatEndedByOther = if (state.isChatActive && isEnded) true else state.chatEndedByOther,
                            isChatActive = !isEnded
                        )
                    }
                    if (isEnded) _events.tryEmit(ChatEvent.ChatEndedByOther)
                    if (messagesResult.messages.size != prevSize && !_uiState.value.isLoadingMore) _events.tryEmit(ChatEvent.ScrollToBottom)
                }
        }
    }

    fun sendMessage(text: String) {
        val id = _chatroomId.value ?: return
        val uid = _userId.value ?: return
        val s = _uiState.value
        if (!s.isChatActive || text.isBlank()) return
        sendTextMessageUseCase(
            chatroomId = id, userId = uid, myRole = myRole,
            text = text,
            replyToId = s.replyToMessage?.id,
            replyToText = s.replyToMessage?.text
        ) { e -> _events.tryEmit(ChatEvent.ShowSnackbar(e)) }
        _events.tryEmit(ChatEvent.ScrollToBottom)
        _uiState.update { it.copy(replyToMessage = null) }
    }

    fun loadMoreMessages() {
        val id = _chatroomId.value ?: return
        val s = _uiState.value
        if (!s.hasMoreMessages || s.isLoadingMore) return
        _uiState.update { it.copy(isLoadingMore = true) }
        val oldestTimestamp = s.messages.firstOrNull()?.timestamp
        observeMessagesUseCase.loadMore(id, oldestTimestamp) {
            _uiState.update { it.copy(isLoadingMore = false) }
        }
    }

    fun recallMessage(msg: ChatMessage) {
        val id = _chatroomId.value ?: return
        val isPending = _uiState.value.pendingMessages.any { it.id == msg.id }
        if (isPending) {
            mediaSender.cancelPending(msg.id)
            _uiState.update { it.copy(pendingMessages = it.pendingMessages.filter { p -> p.id != msg.id }) }
        } else {
            recallUseCase.recallConfirmed(id, msg, viewModelScope) { _events.tryEmit(ChatEvent.ScrollToBottom) }
        }
    }

    fun fetchOpponentProfile(opponentId: String) {
        viewModelScope.launch {
            val profile = fetchOpponentUseCase(opponentId)
            _uiState.update {
                it.copy(
                    opponentRating = profile.rating,
                    opponentHelpCount = profile.helpCount,
                    opponentNickname = profile.nickname,
                    showOpponentProfile = true
                )
            }
        }
    }

    fun updateTypingStatus(isTyping: Boolean) {
        val id = _chatroomId.value ?: return
        val uid = _userId.value ?: return
        if (!_uiState.value.isChatActive) return
        observeMessagesUseCase.updateMyTypingStatus(id, uid, isTyping)
    }

    fun markAllRead() {
        val myId = _userId.value ?: return
        val unreadIds = _uiState.value.messages
            .filter { it.senderId != myId && !it.readBy.containsKey(myId) }
            .map { it.id }
        observeMessagesUseCase.markAsRead(_chatroomId.value ?: return, unreadIds, myId)
    }

    fun markChatEnded(onSuccess: () -> Unit, onError: (String) -> Unit) {
        observeMessagesUseCase.markChatEnded(_chatroomId.value ?: return, onSuccess, onError)
    }

    fun onChatEndedByOther(myRole: String, seekerExpertId: String, onBack: () -> Unit) {
        if (myRole == "user" && _userId.value != seekerExpertId) {
            _uiState.update { it.copy(showRatingDialog = true) }
        } else onBack()
    }

    fun updateUiState(update: (ChatUiState) -> ChatUiState) { _uiState.update { update(it) } }
    fun openCamera() { _events.tryEmit(ChatEvent.OpenCamera) }
    fun openVoiceRecorder() { _events.tryEmit(ChatEvent.OpenVoiceRecorder) }
}
