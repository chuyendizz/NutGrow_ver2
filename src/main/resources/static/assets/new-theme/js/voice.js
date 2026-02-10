import Vapi from "https://esm.sh/@vapi-ai/web";

// Khởi tạo Vapi
const vapi = new Vapi('0b0c37f4-1f4c-4b23-acff-c5a121a9720c'); // Public Key

// Biến toàn cục trong module
let currentBubble = null;
let currentRole = null;
let committedText = "";

// Lấy các element (Sẽ chạy khi file được load)
const transcriptBox = document.getElementById("transcript-box");
const statusText = document.getElementById("voice-status-text");
const avatarGlow = document.getElementById("voice-avatar");
const btnStart = document.getElementById("startTutor");
const btnStop = document.getElementById("stopTutor");

// --- 1. HÀM MỞ GIAO DIỆN (Nối với Menu chính) ---
// Phải gán vào window vì đây là module, nếu không HTML không gọi được
window.openVoiceTutor = function() {
    console.log("Opening Voice Tutor...");
    
    // Ẩn Menu, hiện Voice UI
    document.getElementById('ui-action-step').classList.add('d-none');
    const voiceStep = document.getElementById('ui-voice-step');
    if (voiceStep) voiceStep.classList.remove('d-none');
    
    // Reset trạng thái
    updateUI("inactive");
    if(transcriptBox) transcriptBox.innerHTML = '<div class="text-center text-muted small my-2">-- Bắt đầu cuộc trò chuyện --</div>';
}

// Hàm dừng khi bấm nút Quay lại (Back)
window.stopVoiceTutor = function() {
    vapi.stop();
    updateUI("inactive");
}

// --- 2. XỬ LÝ SỰ KIỆN NÚT BẤM ---
if (btnStart) {
    btnStart.onclick = async () => {
        // Lấy ngữ cảnh từ file PDF đã upload (nếu có)
        // Lưu ý: Biến uploadedDocumentContext cần được set ở file upload logic
        const context = window.uploadedDocumentContext || "Bạn là gia sư AI.";
        
        try {
            updateUI("connecting");
            // Assistant ID của bạn
            await vapi.start("39f8a27a-d6ac-41a0-aa7b-270fa15ce764", { 
                variableValues: { lesson_content: context } 
            });
            updateUI("active");
        } catch (err) { 
            console.error(err); 
            if(statusText) statusText.innerText = "Lỗi kết nối!"; 
            updateUI("inactive"); 
        }
    };
}

if (btnStop) {
    btnStop.onclick = () => {
        window.stopVoiceTutor();
    };
}

// --- 3. EVENT LISTENERS CỦA VAPI ---
vapi.on("message", (msg) => {
    if (msg.type === "transcript" && msg.transcript) {
        handleTranscript(msg.transcript, msg.role, msg.transcriptType);
    }
});

vapi.on("call-end", () => { 
    updateUI("inactive"); 
});

vapi.on("error", (e) => {
    console.error(e);
    updateUI("inactive");
});

// --- 4. CÁC HÀM XỬ LÝ LOGIC ---
function handleTranscript(text, role, type) {
    if (currentRole !== role) {
        currentBubble = null;
        currentRole = role;
        committedText = "";
    }

    if (!currentBubble) {
        const div = document.createElement("div");
        div.className = `v-msg ${role === "assistant" ? "ai" : "user"} partial`;
        if(transcriptBox) transcriptBox.appendChild(div);
        currentBubble = div;
    }

    const separator = committedText ? " " : ""; 
    currentBubble.innerText = committedText + separator + text;
    
    if (type === "final") {
        committedText += separator + text;
        currentBubble.classList.remove("partial");
    } else {
        currentBubble.classList.add("partial");
    }

    if(transcriptBox) transcriptBox.scrollTop = transcriptBox.scrollHeight;
}

function updateUI(state) {
    if (!statusText || !btnStart || !btnStop || !avatarGlow) return;

    if (state === "connecting") {
        statusText.innerText = "Đang kết nối...";
        btnStart.disabled = true; 
        btnStop.disabled = true;
        avatarGlow.classList.add("active");
    } else if (state === "active") {
        statusText.innerText = "Đang nghe...";
        btnStart.classList.add("d-none");
        btnStop.classList.remove("d-none");
        btnStop.disabled = false;
        avatarGlow.classList.add("active");
    } else {
        statusText.innerText = "Sẵn sàng"; // Hoặc "Đã dừng"
        btnStart.classList.remove("d-none");
        btnStop.classList.add("d-none");
        btnStart.disabled = false;
        avatarGlow.classList.remove("active");
    }
}