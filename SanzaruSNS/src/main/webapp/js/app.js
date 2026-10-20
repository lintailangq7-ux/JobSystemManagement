// 画面の補助 (JSP が作った HTML に動きを付ける)
const MAX_LEN = 280;
const MAX_IMAGE_MB = 5;
const MAX_AUDIO_MB = 10;
const MAX_RECORD_SEC = 60;

function toast(msg, isError) {
  const t = document.createElement('div');
  t.className = 'toast' + (isError ? ' error' : '');
  t.textContent = msg;
  document.body.append(t);
  setTimeout(() => t.remove(), 2500);
}

// サーバーから届いたメッセージは数秒で消す
document.querySelectorAll('[data-toast]').forEach((t) => setTimeout(() => t.remove(), 2500));

// 登録直後のタイプ発表
const reveal = document.querySelector('[data-reveal]');
if (reveal) reveal.showModal();

// 削除などの確認
document.querySelectorAll('form[data-confirm]').forEach((form) => {
  form.addEventListener('submit', (e) => {
    if (!confirm(form.dataset.confirm)) e.preventDefault();
  });
});

// 投稿カードのどこかを押したら詳細へ (リンク・ボタン・音声などは除く)
document.querySelectorAll('article.post[data-href]').forEach((card) => {
  card.addEventListener('click', (e) => {
    if (e.target.closest('a, button, form, audio, input, label')) return;
    location.href = card.dataset.href;
  });
});

// 投稿・コメント入力欄
document.querySelectorAll('form.composer').forEach((form) => {
  const ta = form.querySelector('textarea');
  const counter = form.querySelector('.counter');
  const submit = form.querySelector('button[type=submit]');
  const previews = form.querySelector('.previews');
  const imageInput = form.querySelector('input[name=image]');
  const audioInput = form.querySelector('input[name=audio]');
  const recBtn = form.querySelector('[data-record]');
  let recorder = null;

  const update = () => {
    const len = ta ? [...ta.value.trim()].length : 0;
    if (counter) {
      counter.textContent = `${len} / ${MAX_LEN}`;
      counter.classList.toggle('over', len > MAX_LEN);
    }
    const hasFile = (imageInput && imageInput.files.length) || (audioInput && audioInput.files.length);
    submit.disabled = (len === 0 && !hasFile) || len > MAX_LEN || !!recorder;
  };

  // 自分が添付したものなので、タイプに関係なくプレビューする
  const renderPreviews = () => {
    previews.replaceChildren();
    for (const [kind, input, label] of [['image', imageInput, '画像'], ['audio', audioInput, '音声']]) {
      if (!input || !input.files.length) continue;
      const url = URL.createObjectURL(input.files[0]);
      let body;
      if (kind === 'image') {
        body = document.createElement('img');
        body.src = url;
        body.alt = '';
      } else {
        body = document.createElement('audio');
        body.src = url;
        body.controls = true;
      }
      const remove = document.createElement('button');
      remove.type = 'button';
      remove.className = 'link';
      remove.textContent = `${label}を外す`;
      remove.addEventListener('click', () => {
        input.value = '';
        renderPreviews();
      });
      const chip = document.createElement('div');
      chip.className = 'preview';
      chip.append(body, remove);
      previews.append(chip);
    }
    update();
  };

  const checkSize = (input, maxMb, label) => {
    if (input.files.length && input.files[0].size > maxMb * 1024 * 1024) {
      toast(`${label}は${maxMb}MBまでです`, true);
      input.value = '';
    }
    renderPreviews();
  };
  if (imageInput) imageInput.addEventListener('change', () => checkSize(imageInput, MAX_IMAGE_MB, '画像'));
  if (audioInput) audioInput.addEventListener('change', () => checkSize(audioInput, MAX_AUDIO_MB, '音声'));

  // 録音した音声は「音声ファイル」欄に入れて、普通のファイルと同じように送る
  if (recBtn) {
    recBtn.addEventListener('click', async () => {
      if (recorder) return recorder.stop();
      let stream;
      try {
        stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      } catch {
        return toast('マイクが使えません。ブラウザでマイクを許可してください', true);
      }
      const chunks = [];
      let sec = 0;
      recorder = new MediaRecorder(stream);
      recorder.ondataavailable = (e) => e.data.size && chunks.push(e.data);
      recorder.onstop = () => {
        stream.getTracks().forEach((t) => t.stop());
        clearInterval(timer);
        recorder = null;
        recBtn.textContent = '🎤 録音';
        recBtn.classList.remove('recording');
        const type = (chunks[0] && chunks[0].type) || 'audio/webm';
        const blob = new Blob(chunks, { type });
        if (blob.size) {
          const dt = new DataTransfer();
          dt.items.add(new File([blob], `recording.${type.includes('mp4') ? 'm4a' : 'webm'}`, { type }));
          audioInput.files = dt.files;
          checkSize(audioInput, MAX_AUDIO_MB, '音声');
        }
        update();
      };
      recorder.start();
      recBtn.textContent = '⏹ 停止 (0秒)';
      recBtn.classList.add('recording');
      const timer = setInterval(() => {
        sec++;
        recBtn.textContent = `⏹ 停止 (${sec}秒)`;
        if (sec >= MAX_RECORD_SEC && recorder) recorder.stop();
      }, 1000);
      update();
    });
  }

  if (ta) {
    ta.addEventListener('input', update);
    ta.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' && (e.ctrlKey || e.metaKey) && !submit.disabled) form.requestSubmit();
    });
  }
  form.addEventListener('submit', () => {
    // 二重送信を防ぐ
    setTimeout(() => (submit.disabled = true));
  });
  update();
});
