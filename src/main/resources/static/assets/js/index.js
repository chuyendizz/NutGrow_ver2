import Vapi from "https://esm.sh/@vapi-ai/web";

const vapi = new Vapi('0b0c37f4-1f4c-4b23-acff-c5a121a9720c');
let currentBubble = null;
let currentRole = null;
let lastTranscript = "";


document.getElementById("startTutor").onclick = async () => {
  await vapi.start("39f8a27a-d6ac-41a0-aa7b-270fa15ce764", {
    variableValues: {
      lesson_content: documentContext
    }
  });
};

document.getElementById("stopTutor").onclick = () => {
  vapi.stop();
  finalizeTranscript();
};


function updateTranscript(text, role) {
  if (currentRole !== role) {
    finalizeTranscript();
  }

  currentRole = role;

  if (text === lastTranscript) return;

  lastTranscript = text;

  if (!currentBubble) {
    const msg = document.createElement("div");
    msg.classList.add(
      "transcript-msg",
      role === "assistant" ? "transcript-ai" : "transcript-user"
    );

    msg.innerHTML = `
      <div class="transcript-text"></div>
      <div class="transcript-time">${new Date().toLocaleTimeString()}</div>
    `;

    transcriptContent.appendChild(msg);
    currentBubble = msg;
  }

  currentBubble.querySelector(".transcript-text").innerText = text;
  transcriptContent.scrollTop = transcriptContent.scrollHeight;
}

function finalizeTranscript() {
  currentBubble = null;
  currentRole = null;
  lastTranscript = "";
}


vapi.on("message", (message) => {
  if (
    message.type !== "transcript" ||
    !message.transcript ||
    !message.role
  ) return;

  updateTranscript(message.transcript, message.role);
});