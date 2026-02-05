// Chat Widget JavaScript
class ChatWidget {
    constructor(userId, userName, userEmail) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.sessionId = null;
        this.isOpen = false;
        this.pollInterval = null;
        this.lastMessageId = 0;

        this.init();
    }

    init() {
        this.createWidget();
        this.attachEventListeners();
        this.checkExistingSession();
    }

    createWidget() {
        // Create chat button
        const chatBtn = document.createElement('button');
        chatBtn.className = 'chat-widget-btn';
        chatBtn.id = 'chatWidgetBtn';
        chatBtn.innerHTML = '<i class="fas fa-comments"></i>';
        document.body.appendChild(chatBtn);

        // Create chat window
        const chatWindow = document.createElement('div');
        chatWindow.className = 'chat-widget-window';
        chatWindow.id = 'chatWidgetWindow';
        chatWindow.innerHTML = `
            <div class="chat-header">
                <div class="chat-header-info">
                    <i class="fas fa-headset"></i>
                    <div>
                        <h4>Live Support</h4>
                        <div class="chat-status">
                            <span class="status-dot"></span>
                            <span id="chatStatusText">Online</span>
                        </div>
                    </div>
                </div>
                <button class="chat-close-btn" id="chatCloseBtn">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="chat-messages" id="chatMessages">
                <div class="chat-welcome">
                    <i class="fas fa-comment-dots"></i>
                    <h4>Hi there! 👋</h4>
                    <p>How can we help you today?<br>Send us a message and we'll respond shortly.</p>
                </div>
            </div>
            <div class="chat-input-area">
                <input type="text" class="chat-input" id="chatInput" placeholder="Type your message..." maxlength="500">
                <button class="chat-send-btn" id="chatSendBtn">
                    <i class="fas fa-paper-plane"></i>
                </button>
            </div>
        `;
        document.body.appendChild(chatWindow);
    }

    attachEventListeners() {
        // Toggle chat window
        document.getElementById('chatWidgetBtn').addEventListener('click', () => this.toggleChat());
        document.getElementById('chatCloseBtn').addEventListener('click', () => this.toggleChat());

        // Send message
        document.getElementById('chatSendBtn').addEventListener('click', () => this.sendMessage());
        document.getElementById('chatInput').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.sendMessage();
        });
    }

    toggleChat() {
        this.isOpen = !this.isOpen;
        const chatWindow = document.getElementById('chatWidgetWindow');

        if (this.isOpen) {
            chatWindow.classList.add('open');
            document.getElementById('chatInput').focus();

            // Start polling for new messages if session exists
            if (this.sessionId) {
                this.startPolling();
            }
        } else {
            chatWindow.classList.remove('open');
            this.stopPolling();
        }
    }

    async checkExistingSession() {
        try {
            const response = await fetch('/api/chat/session', {
                headers: this.getHeaders()
            });
            const data = await response.json();

            if (data.hasSession) {
                // Check if session is closed
                if (data.status === 'CLOSED') {
                    // Don't use this session, let user start fresh
                    this.sessionId = null;
                    return;
                }

                this.sessionId = data.sessionId;
                await this.loadMessages();

                if (data.status === 'WAITING') {
                    this.showWaitingStatus();
                } else if (data.staffName) {
                    document.getElementById('chatStatusText').textContent = `${data.staffName} is helping you`;
                }
            }
        } catch (error) {
            console.error('Error checking session:', error);
        }
    }

    async startChat() {
        try {
            const response = await fetch('/api/chat/start', {
                method: 'POST',
                headers: this.getHeaders()
            });
            const data = await response.json();

            this.sessionId = data.sessionId;
            this.lastMessageId = 0;

            // Clear old messages and show fresh start
            const messagesContainer = document.getElementById('chatMessages');
            messagesContainer.innerHTML = '';

            this.showWaitingStatus();
            this.startPolling();

            return data;
        } catch (error) {
            console.error('Error starting chat:', error);
        }
    }

    async sendMessage() {
        const input = document.getElementById('chatInput');
        const content = input.value.trim();

        if (!content) return;

        // Start session if not exists or if closed
        if (!this.sessionId) {
            await this.startChat();
        }

        // Add message to UI immediately
        this.addMessageToUI(content, 'customer', new Date().toISOString());
        input.value = '';

        try {
            const formData = new URLSearchParams();
            formData.append('sessionId', this.sessionId);
            formData.append('content', content);
            formData.append('senderType', 'CUSTOMER');
            formData.append('senderId', this.userId);
            formData.append('senderName', this.userName);

            const response = await fetch('/api/chat/send', {
                method: 'POST',
                headers: this.getHeaders(),
                body: formData
            });

            // If session was closed, start a new one
            if (!response.ok) {
                const errorData = await response.json();
                if (errorData.error && errorData.error.includes('not found')) {
                    // Session was closed, start a new one
                    this.sessionId = null;
                    this.lastMessageId = 0;
                    await this.startChat();
                    // Resend the message
                    const formData2 = new URLSearchParams();
                    formData2.append('sessionId', this.sessionId);
                    formData2.append('content', content);
                    formData2.append('senderType', 'CUSTOMER');
                    formData2.append('senderId', this.userId);
                    formData2.append('senderName', this.userName);
                    await fetch('/api/chat/send', {
                        method: 'POST',
                        headers: this.getHeaders(),
                        body: formData2
                    });
                }
            }

            // Start polling if not already
            if (!this.pollInterval) {
                this.startPolling();
            }
        } catch (error) {
            console.error('Error sending message:', error);
        }
    }

    async loadMessages() {
        if (!this.sessionId) return;

        try {
            const response = await fetch(`/api/chat/messages/${this.sessionId}`, {
                headers: this.getHeaders()
            });
            const messages = await response.json();

            // Clear welcome message
            const messagesContainer = document.getElementById('chatMessages');
            messagesContainer.innerHTML = '';

            messages.forEach(msg => {
                this.addMessageToUI(msg.content, msg.senderType.toLowerCase(), msg.timestamp, false);
                this.lastMessageId = Math.max(this.lastMessageId, msg.id);
            });

            this.scrollToBottom();
        } catch (error) {
            console.error('Error loading messages:', error);
        }
    }

    addMessageToUI(content, type, timestamp, scroll = true) {
        const messagesContainer = document.getElementById('chatMessages');

        // Remove welcome message if present
        const welcome = messagesContainer.querySelector('.chat-welcome');
        if (welcome) welcome.remove();

        const msgDiv = document.createElement('div');
        msgDiv.className = `chat-message ${type}`;

        const time = new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        msgDiv.innerHTML = `
            ${content}
            <span class="message-time">${time}</span>
        `;

        messagesContainer.appendChild(msgDiv);

        if (scroll) this.scrollToBottom();
    }

    showWaitingStatus() {
        const messagesContainer = document.getElementById('chatMessages');

        // Check if waiting message already exists
        if (messagesContainer.querySelector('.chat-waiting')) return;

        const waitingDiv = document.createElement('div');
        waitingDiv.className = 'chat-waiting';
        waitingDiv.innerHTML = '<i class="fas fa-clock"></i> Waiting for a support agent to join...';
        messagesContainer.appendChild(waitingDiv);

        document.getElementById('chatStatusText').textContent = 'Waiting for agent...';
    }

    removeWaitingStatus() {
        const messagesContainer = document.getElementById('chatMessages');
        const waiting = messagesContainer.querySelector('.chat-waiting');
        if (waiting) waiting.remove();
    }

    startPolling() {
        if (this.pollInterval) return;

        this.pollInterval = setInterval(() => this.pollMessages(), 3000);
    }

    stopPolling() {
        if (this.pollInterval) {
            clearInterval(this.pollInterval);
            this.pollInterval = null;
        }
    }

    async pollMessages() {
        if (!this.sessionId || !this.isOpen) return;

        try {
            // First check if session still exists
            const sessionResponse = await fetch('/api/chat/session', {
                headers: this.getHeaders()
            });
            const sessionData = await sessionResponse.json();

            // If session is closed, notify user and reset
            if (!sessionData.hasSession || sessionData.status === 'CLOSED') {
                this.showSessionClosed();
                this.stopPolling();
                this.sessionId = null;
                this.lastMessageId = 0;
                return;
            }

            const response = await fetch(`/api/chat/messages/${this.sessionId}`, {
                headers: this.getHeaders()
            });
            const messages = await response.json();

            // Find new messages
            const newMessages = messages.filter(msg => msg.id > this.lastMessageId);

            newMessages.forEach(msg => {
                // Only add staff messages (customer messages are added immediately)
                if (msg.senderType === 'STAFF') {
                    this.addMessageToUI(msg.content, 'staff', msg.timestamp);
                    this.removeWaitingStatus();
                    document.getElementById('chatStatusText').textContent = `${msg.senderName} is helping you`;
                }
                this.lastMessageId = Math.max(this.lastMessageId, msg.id);
            });

            // Mark messages as read
            if (newMessages.length > 0) {
                await fetch(`/api/chat/read/${this.sessionId}?readerType=CUSTOMER`, {
                    method: 'POST',
                    headers: this.getHeaders()
                });
            }
        } catch (error) {
            console.error('Error polling messages:', error);
        }
    }

    showSessionClosed() {
        const messagesContainer = document.getElementById('chatMessages');

        // Remove waiting status if present
        this.removeWaitingStatus();

        // Add closed message
        const closedDiv = document.createElement('div');
        closedDiv.className = 'chat-waiting';
        closedDiv.style.background = '#d4edda';
        closedDiv.style.color = '#155724';
        closedDiv.innerHTML = '<i class="fas fa-check-circle"></i> This chat has been closed. Send a new message to start a new conversation.';
        messagesContainer.appendChild(closedDiv);

        document.getElementById('chatStatusText').textContent = 'Chat ended';
        this.scrollToBottom();
    }

    scrollToBottom() {
        const messagesContainer = document.getElementById('chatMessages');
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    getHeaders() {
        const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
        const csrfToken = document.querySelector('meta[name="_csrf"]');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]');

        if (csrfToken && csrfHeader) {
            headers[csrfHeader.getAttribute('content')] = csrfToken.getAttribute('content');
        }

        return headers;
    }
}

// Initialize chat widget when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Get user info from page (set by Thymeleaf)
    const userId = window.chatUserId;
    const userName = window.chatUserName;
    const userEmail = window.chatUserEmail;

    if (userId && userName) {
        new ChatWidget(userId, userName, userEmail);
    }
});
