// --- LOGIC LUYỆN VIẾT (ESSAY) ---
let currentEssayQuestion = "";

// 1. Bắt đầu làm bài
window.startEssay = async function() {
    console.log("Starting Essay Mode...");

    // Chuyển màn hình
    const actionStep = document.getElementById('ui-action-step');
    const essayStep = document.getElementById('ui-essay-step');
    
    if(actionStep) actionStep.classList.add('d-none');
    if(essayStep) essayStep.classList.remove('d-none');
    
    // Reset giao diện & Hiển thị Loading
    const questionEl = document.getElementById('essay-question-text');
    questionEl.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i> Đang đọc tài liệu và tạo đề bài...';
    questionEl.style.color = "#6c757d"; // Màu xám
    questionEl.style.textAlign = "center"; // Loading thì căn giữa cho đẹp
    
    document.getElementById('essay-answer-input').value = "";
    document.getElementById('essay-result-area').classList.add('d-none');
    document.getElementById('btn-submit-essay').disabled = false;

    try {
        // Gọi API (Backend tự lấy text từ RAM)
        const response = await fetch('/api/study/essay/generate', { 
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({}) 
        });
        
        if (!response.ok) {
            const errData = await response.json();
            throw new Error(errData.error || "Lỗi API");
        }

        const data = await response.json();
        currentEssayQuestion = data.question;
        
        // 👇 HIỂN THỊ KẾT QUẢ (Ngay lập tức)
        questionEl.innerText = currentEssayQuestion;
        
        // Style cho văn bản hiển thị đẹp
        questionEl.style.color = "#212529"; // Màu đen
        questionEl.style.lineHeight = "1.6";
        questionEl.style.textAlign = "justify"; // Căn đều 2 bên

    } catch (e) {
        console.error(e);
        questionEl.innerText = "⚠️ Lỗi: " + e.message;
        questionEl.style.color = "#dc3545"; // Màu đỏ báo lỗi
        
        if (e.message.includes("Upload PDF") || e.message.includes("chưa có tài liệu")) {
            alert("Bạn chưa tải tài liệu lên! Vui lòng quay lại và Upload PDF trước.");
            if(window.resetQuizUI) window.resetQuizUI();
        }
    }
}

// 2. Nộp bài & Chấm điểm
window.submitEssay = async function() {
    const answer = document.getElementById('essay-answer-input').value.trim();
    if (answer.length < 10) {
        alert("Bài làm quá ngắn! Hãy viết ít nhất 1 câu hoàn chỉnh.");
        return;
    }

    const btn = document.getElementById('btn-submit-essay');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i> AI đang chấm...';
    btn.disabled = true;

    try {
        const response = await fetch('/api/study/essay/grade', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ 
                question: currentEssayQuestion,
                answer: answer
            })
        });
        
        if (!response.ok) {
             const errData = await response.json();
             throw new Error(errData.error || "Lỗi chấm điểm");
        }

        const result = await response.json();

        // Hiển thị kết quả
        document.getElementById('essay-score-badge').innerText = result.score + "/10";
        document.getElementById('essay-feedback').innerText = result.feedback;
        document.getElementById('essay-suggestion').innerText = result.suggestion;
        
        document.getElementById('essay-result-area').classList.remove('d-none');
        document.getElementById('essay-result-area').scrollIntoView({ behavior: 'smooth' });

    } catch (e) {
        alert("Lỗi: " + e.message);
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}