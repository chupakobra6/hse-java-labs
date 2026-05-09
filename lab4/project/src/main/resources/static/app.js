let currentGame = null;
const answerLetters = ["A", "B", "C", "D"];
const moneyLadder = [
    500, 1000, 2000, 3000, 5000,
    10000, 15000, 25000, 50000, 100000,
    200000, 400000, 800000, 1500000, 3000000
];

const startForm = document.querySelector("#startForm");
const statusText = document.querySelector("#statusText");
const levelText = document.querySelector("#levelText");
const amountText = document.querySelector("#amountText");
const questionText = document.querySelector("#questionText");
const answers = document.querySelector("#answers");
const ladder = document.querySelector("#ladder");
const records = document.querySelector("#records");
const hintOutput = document.querySelector("#hintOutput");
const stopGame = document.querySelector("#stopGame");

startForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const body = {
        playerName: document.querySelector("#playerName").value,
        safeLevel: Number(document.querySelector("#safeLevel").value)
    };
    const game = await postJson("/api/game/start", body);
    render(game);
});

document.querySelectorAll("[data-hint]").forEach((button) => {
    button.addEventListener("click", async () => {
        if (!currentGame || currentGame.status !== "ACTIVE") return;
        const hint = button.dataset.hint;
        const game = await postJson(`/api/game/${currentGame.gameId}/hint/${hint}`, {});
        render(game);
    });
});

stopGame.addEventListener("click", async () => {
    if (!currentGame || currentGame.status !== "ACTIVE") return;
    const game = await postJson(`/api/game/${currentGame.gameId}/stop`, {});
    render(game);
});

async function postJson(url, body) {
    const response = await fetch(url, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(body)
    });
    const data = await response.json();
    if (!response.ok) {
        hintOutput.textContent = data.error || "Ошибка";
        throw new Error(data.error || "Ошибка");
    }
    return data;
}

async function loadRecords() {
    const response = await fetch("/api/records");
    renderRecords(await response.json());
}

async function answer(number) {
    if (!currentGame || currentGame.status !== "ACTIVE") return;
    const game = await postJson(`/api/game/${currentGame.gameId}/answer`, {answer: number});
    render(game);
}

function render(game) {
    currentGame = game;
    statusText.textContent = `${game.playerName}: ${game.message}`;
    hintOutput.innerHTML = "";
    if (game.audienceVotes) {
        hintOutput.innerHTML = Object.entries(game.audienceVotes)
            .map(([answer, percent]) => `<div>${answer}: ${percent}%</div>`)
            .join("");
    } else if (game.phoneMessage) {
        hintOutput.textContent = game.phoneMessage;
    } else {
        hintOutput.textContent = game.message;
    }

    if (game.status === "ACTIVE") {
        levelText.textContent = `Вопрос ${game.level}`;
        amountText.textContent = formatMoney(game.levelAmount);
        questionText.textContent = game.question.text;
        answers.innerHTML = "";
        game.question.answers.forEach((text, index) => {
            const number = index + 1;
            const button = document.createElement("button");
            button.className = "answer";
            button.type = "button";
            button.dataset.testid = `answer-${number}`;
            button.innerHTML = `<strong>${answerLetters[index]}</strong><span>${escapeHtml(text)}</span>`;
            button.disabled = game.disabledAnswers.includes(number);
            if (game.disabledAnswers.includes(number)) {
                button.classList.add("hidden-answer");
            }
            button.addEventListener("click", () => answer(number));
            answers.appendChild(button);
        });
    } else {
        levelText.textContent = "Игра завершена";
        amountText.textContent = formatMoney(game.currentPrize);
        questionText.textContent = game.message;
        answers.innerHTML = "";
    }

    document.querySelectorAll("[data-hint]").forEach((button) => {
        const normalized = button.dataset.hint === "fifty-fifty" ? "fifty_fifty" : button.dataset.hint.replace("-", "_");
        button.disabled = game.status !== "ACTIVE" || game.usedHints.includes(normalized) || game.hintsUsed >= 4;
        if (button.dataset.hint === "double-dip" && game.usedHints.includes("double_dip")) {
            button.disabled = true;
        }
    });
    stopGame.disabled = game.status !== "ACTIVE" || game.currentPrize === 0;
    renderLadder(game);
    renderRecords(game.topRecords || []);
}

function renderLadder(game) {
    ladder.innerHTML = "";
    const source = game ? game.moneyLadder : moneyLadder;
    const levels = [...source].map((amount, index) => ({level: index + 1, amount})).reverse();
    for (const item of levels) {
        const li = document.createElement("li");
        if (game && item.level === game.level && game.status === "ACTIVE") li.classList.add("active");
        if (game && item.level === game.safeLevel) li.classList.add("safe");
        li.innerHTML = `<span>${item.level}</span><span>${formatMoney(item.amount)}</span>`;
        ladder.appendChild(li);
    }
}

function renderRecords(items) {
    records.innerHTML = "";
    if (!items.length) {
        records.innerHTML = "<li>Рекордов пока нет</li>";
        return;
    }
    for (const item of items) {
        const li = document.createElement("li");
        li.innerHTML = `<strong>${escapeHtml(item.playerName)} — ${formatMoney(item.prize)}</strong><span>${item.finishedAt}</span>`;
        records.appendChild(li);
    }
}

function formatMoney(value) {
    return new Intl.NumberFormat("ru-RU").format(value || 0);
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;");
}

document.querySelectorAll("[data-hint]").forEach((button) => button.disabled = true);
stopGame.disabled = true;
renderLadder(null);
loadRecords();
