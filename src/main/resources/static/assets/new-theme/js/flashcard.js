// --- LOGIC FLASHCARD ---
let flashcardsData = [];
let currentCardIndex = 0;

// Hàm gọi từ nút ở Menu chính
async function startFlashcard() {
    // Ẩn menu, hiện flashcard
    document.getElementById('ui-action-step').classList.add('d-none');
    document.getElementById('ui-flashcard-step').classList.remove('d-none');
    
    // Reset
    currentCardIndex = 0;
    const cardEl = document.getElementById('flashcard-element');
    cardEl.classList.remove('flipped'); 

    try {
        // Gọi API
        const response = await fetch('/api/study/flashcards', { 
            method: 'POST', 
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ documentId: 123 }) 
        });
        const data = await response.json();
        flashcardsData = data.cards;
        
        renderCard();
        
    } catch (e) {
        alert("Lỗi tải Flashcard: " + e.message);
    }
}

function renderCard() {
    if (flashcardsData.length === 0) return;
    
    const card = flashcardsData[currentCardIndex];
    document.getElementById('fc-front-content').innerText = card.front;
    document.getElementById('fc-back-content').innerText = card.back;
    document.getElementById('fc-progress').innerText = `${currentCardIndex + 1} / ${flashcardsData.length}`;
}

// Gán các hàm vào window để HTML gọi được (QUAN TRỌNG)
window.startFlashcard = startFlashcard; // <--- BẠN CẦN THÊM DÒNG NÀY

window.flipCard = function() { 
    document.getElementById('flashcard-element').classList.toggle('flipped');
}

window.nextCard = function() {
    if (currentCardIndex < flashcardsData.length - 1) {
        currentCardIndex++;
        resetFlipAndRender();
    }
}

window.prevCard = function() {
    if (currentCardIndex > 0) {
        currentCardIndex--;
        resetFlipAndRender();
    }
}

function resetFlipAndRender() {
    const cardEl = document.getElementById('flashcard-element');
    if (cardEl.classList.contains('flipped')) {
        cardEl.classList.remove('flipped');
        setTimeout(() => renderCard(), 300); 
    } else {
        renderCard();
    }
}