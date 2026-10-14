// ---------- 共通 ----------
const app = document.getElementById('app');
const MAX_LEN = 280;
const MAX_IMAGE_MB = 5;
const MAX_AUDIO_MB = 10;
const MAX_RECORD_SEC = 60;
let me = null;

// アカウントタイプ（blocked: 見られない＆コメントに使えないもの）
const TYPES = {
  mizaru: { emoji: '🙈', name: '見猿', blocked: 'image', desc: '画像が見えず、コメントに画像を貼れません' },
  iwazaru: { emoji: '🙊', name: '言わ猿', blocked: 'text', desc: '文字が見えず、コメントに文字を打てません' },
  kikazaru: { emoji: '🙉', name: '聞か猿', blocked: 'audio', desc: '音が聞こえず、コメントに音声を投稿できません' },
};
const typeOf = (u) => TYPES[u.type];

async function api(method, url, body) {
  const res = await fetch(url, {
    method,
    headers: body !== undefined || method !== 'GET' ? { 'Content-Type': 'application/json' } : {},
    body: body !== undefined ? JSON.stringify(body) : method !== 'GET' ? '{}' : undefined,
  });
  const data = await res.json().catch(() => null);
  if (!res.ok) throw new Error((data && data.error) || `エラー (${res.status})`);
  return data;
}

// DOM要素を作る小さなヘルパー（文字列は必ず textContent で入れるのでXSS安全）
function h(tag, props = {}, ...children) {
  const el = document.createElement(tag);
  for (const [k, v] of Object.entries(props)) {
    if (k.startsWith('on')) el.addEventListener(k.slice(2), v);
    else if (k === 'class') el.className = v;
    else if (v === true) el.setAttribute(k, '');
    else if (v !== false && v != null) el.setAttribute(k, v);
  }
  for (const c of children.flat()) {
    if (c == null || c === false) continue;
    el.append(c instanceof Node ? c : document.createTextNode(String(c)));
  }
  return el;
}

function toast(msg) {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.hidden = false;
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => (t.hidden = true), 2500);
}

function timeAgo(ts) {
  const s = Math.floor((Date.now() - ts) / 1000);
  if (s < 60) return 'たった今';
  if (s < 3600) return `${Math.floor(s / 60)}分前`;
  if (s < 86400) return `${Math.floor(s / 3600)}時間前`;
  if (s < 86400 * 7) return `${Math.floor(s / 86400)}日前`;
  return new Date(ts).toLocaleDateString('ja-JP');
}

function avatar(user, size = '') {
  let hash = 0;
  for (const ch of user.username) hash = (hash * 31 + ch.charCodeAt(0)) >>> 0;
  const initial = [...(user.displayName || user.username)][0].toUpperCase();
  return h('div', { class: `avatar ${size}`, style: `background:hsl(${hash % 360} 55% 48%)` }, initial);
}

function typeBadge(u) {
  const t = typeOf(u);
  return h('span', { class: `type-badge ${u.type}`, title: t.desc }, `${t.emoji} ${t.name}`);
}

// 見えないものの代わりに出すプレースホルダー
const HIDDEN_TEXT = {
  text: '🙊 ここには文字がありますが、あなたには読めません',
  image: '🙈 ここには画像がありますが、あなたには見えません',
  audio: '🙉 ここには音声がありますが、あなたには聞こえません',
};
const hiddenBox = (kind) => h('div', { class: 'hidden-media' }, HIDDEN_TEXT[kind]);

function readAsDataURL(blob) {
  return new Promise((resolve, reject) => {
    const r = new FileReader();
    r.onload = () => resolve(r.result);
    r.onerror = () => reject(r.error);
    r.readAsDataURL(blob);
  });
}

// ---------- 部品 ----------
function postCard(post, { onChange } = {}) {
  const likeBtn = h('button', { class: post.liked ? 'liked' : '', title: 'いいね' },
    `${post.liked ? '♥' : '♡'} ${post.likeCount}`);
  likeBtn.addEventListener('click', async (e) => {
    e.stopPropagation();
    try {
      const updated = await api('POST', `/api/posts/${post.id}/like`);
      card.replaceWith(postCard(updated, { onChange }));
    } catch (err) { toast(err.message); }
  });

  const replyBtn = h('button', {
    title: 'コメント',
    onclick: (e) => { e.stopPropagation(); location.hash = `#/p/${post.id}`; },
  }, `💬 ${post.replyCount}`);

  const delBtn = post.isMine && h('button', {
    class: 'del',
    title: '削除',
    onclick: async (e) => {
      e.stopPropagation();
      if (!confirm('この投稿を削除しますか？（コメントも削除されます）')) return;
      try {
        await api('DELETE', `/api/posts/${post.id}`);
        toast('削除しました');
        onChange ? onChange() : card.remove();
      } catch (err) { toast(err.message); }
    },
  }, '🗑 削除');

  const userLink = (child) => h('a', {
    href: `#/u/${post.author.username}`,
    onclick: (e) => e.stopPropagation(),
  }, child);

  const stop = (e) => e.stopPropagation();
  const content = [
    post.text && h('div', { class: 'post-text' }, post.text),
    post.textHidden && hiddenBox('text'),
    post.image && (post.image.hidden ? hiddenBox('image')
      : h('a', { href: post.image.url, target: '_blank', rel: 'noopener', onclick: stop },
        h('img', { class: 'post-image', src: post.image.url, alt: '投稿画像', loading: 'lazy' }))),
    post.audio && (post.audio.hidden ? hiddenBox('audio')
      : h('audio', { class: 'post-audio', src: post.audio.url, controls: true, preload: 'metadata', onclick: stop })),
  ];

  const card = h('article', { class: 'card post', onclick: () => (location.hash = `#/p/${post.id}`) },
    userLink(avatar(post.author)),
    h('div', { class: 'post-body' },
      h('div', { class: 'post-head' },
        userLink(h('span', { class: 'name' }, post.author.displayName)),
        typeBadge(post.author),
        h('span', { class: 'muted' }, `@${post.author.username} · ${timeAgo(post.createdAt)}`),
      ),
      post.replyTo && h('div', { class: 'muted' }, 'コメント先: ',
        h('a', { href: `#/p/${post.replyTo.id}`, onclick: stop }, `@${post.replyTo.username}`)),
      h('div', { class: 'post-content' }, content),
      h('div', { class: 'post-actions' }, replyBtn, likeBtn, delBtn),
    ),
  );
  return card;
}

// 投稿・コメント入力欄。コメント時は自分のタイプで禁止されたものが使えない
function composer({ replyTo = null, placeholder = 'いまどうしてる？', onPosted }) {
  const blocked = replyTo ? typeOf(me).blocked : null;
  let image = null; // { dataUrl }
  let audio = null; // { dataUrl }
  let recorder = null;

  const ta = h('textarea', { placeholder, maxlength: MAX_LEN * 2 });
  const counter = h('span', { class: 'counter' }, `0 / ${MAX_LEN}`);
  const btn = h('button', { disabled: true }, replyTo ? 'コメントする' : '投稿する');
  const err = h('p', { class: 'error' });
  const previews = h('div', { class: 'previews' });

  const imageInput = h('input', { type: 'file', accept: 'image/png,image/jpeg,image/gif,image/webp', hidden: true });
  const audioInput = h('input', { type: 'file', accept: 'audio/*', hidden: true });
  const imageBtn = h('button', { type: 'button', class: 'secondary small', onclick: () => imageInput.click() }, '🖼 画像');
  const audioBtn = h('button', { type: 'button', class: 'secondary small', onclick: () => audioInput.click() }, '🎵 音声ファイル');
  const recBtn = h('button', { type: 'button', class: 'secondary small' }, '🎤 録音');

  const update = () => {
    const len = [...ta.value.trim()].length;
    counter.textContent = `${len} / ${MAX_LEN}`;
    counter.classList.toggle('over', len > MAX_LEN);
    btn.disabled = (len === 0 && !image && !audio) || len > MAX_LEN || !!recorder;
  };

  // 自分が添付したものなので、タイプに関係なくプレビューする
  const renderPreviews = () => {
    const chip = (label, body, onRemove) => h('div', { class: 'preview' },
      body, h('button', { type: 'button', class: 'link', onclick: onRemove }, `${label}を外す`));
    previews.replaceChildren(
      image && chip('画像',
        h('img', { src: image.dataUrl, alt: '' }),
        () => { image = null; renderPreviews(); }),
      audio && chip('音声',
        h('audio', { src: audio.dataUrl, controls: true }),
        () => { audio = null; renderPreviews(); }),
    );
    update();
  };

  const attach = async (file, kind, maxMb) => {
    if (!file) return;
    if (file.size > maxMb * 1024 * 1024) return toast(`${kind === 'image' ? '画像' : '音声'}は${maxMb}MBまでです`);
    const dataUrl = await readAsDataURL(file);
    if (kind === 'image') image = { dataUrl };
    else audio = { dataUrl };
    renderPreviews();
  };
  imageInput.addEventListener('change', () => { attach(imageInput.files[0], 'image', MAX_IMAGE_MB); imageInput.value = ''; });
  audioInput.addEventListener('change', () => { attach(audioInput.files[0], 'audio', MAX_AUDIO_MB); audioInput.value = ''; });

  recBtn.addEventListener('click', async () => {
    if (recorder) return recorder.stop();
    let stream;
    try {
      stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    } catch {
      return toast('マイクが使えません。ブラウザでマイクを許可してください');
    }
    const chunks = [];
    recorder = new MediaRecorder(stream);
    recorder.ondataavailable = (e) => e.data.size && chunks.push(e.data);
    recorder.onstop = async () => {
      stream.getTracks().forEach((t) => t.stop());
      clearInterval(timer);
      recorder = null;
      recBtn.textContent = '🎤 録音';
      recBtn.classList.remove('recording');
      const blob = new Blob(chunks, { type: chunks[0]?.type || 'audio/webm' });
      if (blob.size) await attach(blob, 'audio', MAX_AUDIO_MB);
      update();
    };
    recorder.start();
    let sec = 0;
    recBtn.textContent = '⏹ 停止 (0秒)';
    recBtn.classList.add('recording');
    const timer = setInterval(() => {
      sec++;
      recBtn.textContent = `⏹ 停止 (${sec}秒)`;
      if (sec >= MAX_RECORD_SEC) recorder && recorder.stop();
    }, 1000);
    update();
  });

  const submit = async () => {
    if (btn.disabled) return;
    btn.disabled = true;
    try {
      await api('POST', '/api/posts', {
        text: ta.value,
        image: image && image.dataUrl,
        audio: audio && audio.dataUrl,
        replyTo,
      });
      ta.value = '';
      image = audio = null;
      err.textContent = '';
      renderPreviews();
      onPosted && onPosted();
    } catch (e) {
      err.textContent = e.message;
      update();
    }
  };
  ta.addEventListener('input', update);
  ta.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) submit();
  });
  btn.addEventListener('click', submit);

  const notice = blocked && h('div', { class: 'blocked-note' },
    `${typeOf(me).emoji} ${typeOf(me).name}アカウントなので、コメントに${{ text: '文字', image: '画像', audio: '音声' }[blocked]}は使えません`);

  return h('div', { class: 'card composer' },
    notice,
    blocked !== 'text' && ta,
    previews,
    h('div', { class: 'row wrap', style: 'margin-top:8px' },
      blocked !== 'image' && imageBtn,
      blocked !== 'audio' && [recBtn, audioBtn],
      imageInput, audioInput,
      h('div', { class: 'spacer' }),
      blocked !== 'text' && counter,
      btn),
    err,
  );
}

function postList(posts, emptyText, onChange) {
  if (!posts.length) return h('div', { class: 'card empty' }, emptyText);
  return h('div', {}, posts.map((p) => postCard(p, { onChange })));
}

// ---------- 画面 ----------
async function homePage() {
  const list = h('div');
  const load = async () => {
    const posts = await api('GET', '/api/timeline?type=home');
    list.replaceChildren(postList(posts, 'まだ投稿がありません。最初の投稿をしたり、「ユーザー」から誰かをフォローしてみましょう。', load));
  };
  app.replaceChildren(h('h1', {}, 'ホーム'), composer({ onPosted: load }), list);
  await load();
}

async function explorePage() {
  const list = h('div');
  const load = async () => {
    const posts = await api('GET', '/api/timeline?type=all');
    list.replaceChildren(postList(posts, 'まだ誰も投稿していません。', load));
  };
  app.replaceChildren(h('h1', {}, 'みんなの投稿'), composer({ onPosted: load }), list);
  await load();
}

async function usersPage() {
  const users = await api('GET', '/api/users');
  app.replaceChildren(
    h('h1', {}, 'ユーザー'),
    users.length ? users.map((u) => userRow(u)) : h('div', { class: 'card empty' }, 'ユーザーがいません'),
  );
}

function bioView(u, style) {
  if (u.bioHidden) return h('div', { class: 'muted', style }, '🙊 自己紹介は読めません');
  return u.bio && h('div', { style }, u.bio);
}

function userRow(u) {
  const row = h('div', { class: 'card row' },
    h('a', { href: `#/u/${u.username}` }, avatar(u)),
    h('div', { class: 'spacer', style: 'min-width:0' },
      h('div', { class: 'row', style: 'gap:6px;flex-wrap:wrap' },
        h('a', { href: `#/u/${u.username}`, style: 'font-weight:700;color:var(--text)' }, u.displayName),
        typeBadge(u)),
      h('div', { class: 'muted' }, `@${u.username}`),
      bioView(u, 'font-size:14px;overflow-wrap:anywhere'),
    ),
    followButton(u, (updated) => row.replaceWith(userRow(updated))),
  );
  return row;
}

function followButton(u, onUpdated) {
  if (u.isMe) return null;
  return h('button', {
    class: u.isFollowing ? 'secondary' : '',
    onclick: async () => {
      try {
        onUpdated(await api('POST', `/api/users/${encodeURIComponent(u.username)}/follow`));
      } catch (e) { toast(e.message); }
    },
  }, u.isFollowing ? 'フォロー中' : 'フォロー');
}

async function profilePage(username) {
  const load = async () => {
    const { user, posts } = await api('GET', `/api/users/${encodeURIComponent(username)}`);
    const editArea = h('div');
    const editBtn = user.isMe && h('button', { class: 'secondary', onclick: () => {
      editArea.replaceChildren(profileEditor(user, load));
      editBtn.remove();
    } }, 'プロフィール編集');

    app.replaceChildren(
      h('div', { class: 'card' },
        h('div', { class: 'profile-head' },
          avatar(user, 'lg'),
          h('div', { class: 'spacer', style: 'min-width:0' },
            h('h1', { style: 'margin:0' }, user.displayName),
            h('div', { class: 'muted' }, `@${user.username} · ${new Date(user.createdAt).toLocaleDateString('ja-JP')}に登録`),
            h('div', { style: 'margin-top:6px' }, typeBadge(user), ' ', h('span', { class: 'muted' }, typeOf(user).desc)),
            bioView(user, 'margin:8px 0 0;white-space:pre-wrap;overflow-wrap:anywhere'),
            h('div', { class: 'profile-stats' },
              h('span', {}, h('b', {}, user.postCount), ' 投稿'),
              h('span', {}, h('b', {}, user.followingCount), ' フォロー'),
              h('span', {}, h('b', {}, user.followerCount), ' フォロワー'),
            ),
          ),
          followButton(user, load) || editBtn,
        ),
        editArea,
      ),
      postList(posts, 'まだ投稿がありません。', load),
    );
  };
  await load();
}

function profileEditor(user, onSaved) {
  const name = h('input', { value: user.displayName, maxlength: 50 });
  const bio = h('textarea', { maxlength: 160 });
  bio.value = user.bio || '';
  const err = h('p', { class: 'error' });
  return h('div', { style: 'margin-top:12px;border-top:1px solid var(--border);padding-top:4px' },
    h('label', {}, '表示名'), name,
    h('label', {}, '自己紹介（160文字まで）'), bio,
    err,
    h('div', { class: 'row' },
      h('button', { onclick: async () => {
        try {
          me = await api('PATCH', '/api/me', { displayName: name.value, bio: bio.value });
          renderSession();
          toast('保存しました');
          onSaved();
        } catch (e) { err.textContent = e.message; }
      } }, '保存'),
      h('button', { class: 'secondary', onclick: onSaved }, 'キャンセル'),
    ),
  );
}

async function postPage(id) {
  const load = async () => {
    let data;
    try {
      data = await api('GET', `/api/posts/${id}`);
    } catch (e) {
      app.replaceChildren(h('div', { class: 'card empty' }, e.message));
      return;
    }
    app.replaceChildren(
      h('div', { class: 'row', style: 'margin-bottom:12px' },
        h('button', { class: 'secondary', onclick: () => history.back() }, '← 戻る'),
        h('h1', { style: 'margin:0' }, '投稿')),
      postCard(data.post, { onChange: () => history.back() }),
      composer({ replyTo: data.post.id, placeholder: `@${data.post.author.username} にコメント`, onPosted: load }),
      h('h1', { style: 'font-size:16px' }, `コメント ${data.replies.length}件`),
      postList(data.replies, 'まだコメントはありません。', load),
    );
  };
  await load();
}

function loginPage() {
  if (me) return (location.hash = '#/');
  let mode = 'login';
  const username = h('input', { autocomplete: 'username', placeholder: 'yamada_taro' });
  const displayName = h('input', { placeholder: '山田太郎' });
  const password = h('input', { type: 'password', autocomplete: 'current-password' });
  const err = h('p', { class: 'error' });
  const nameField = h('div', { hidden: true }, h('label', {}, '表示名（任意）'), displayName);
  const regNote = h('p', { class: 'muted', hidden: true },
    '登録すると 🙈見猿・🙊言わ猿・🙉聞か猿 のどれかがランダムで割り振られます。');
  const title = h('h1');
  const submitBtn = h('button', { type: 'submit' });
  const switchBtn = h('button', { type: 'button', class: 'link' });

  const render = () => {
    const reg = mode === 'register';
    title.textContent = reg ? '新規登録' : 'ログイン';
    submitBtn.textContent = reg ? '登録する' : 'ログイン';
    switchBtn.textContent = reg ? 'アカウントをお持ちの方はログイン' : 'アカウントを作成する';
    nameField.hidden = !reg;
    regNote.hidden = !reg;
    password.autocomplete = reg ? 'new-password' : 'current-password';
    err.textContent = '';
  };
  switchBtn.addEventListener('click', () => { mode = mode === 'login' ? 'register' : 'login'; render(); });

  const form = h('form', { class: 'card' },
    title,
    regNote,
    h('label', {}, 'ユーザー名（英数字と_、3〜20文字）'), username,
    nameField,
    h('label', {}, 'パスワード（8文字以上）'), password,
    err,
    h('div', { class: 'row', style: 'margin-top:8px' }, submitBtn, h('div', { class: 'spacer' }), switchBtn),
  );
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    submitBtn.disabled = true;
    try {
      me = await api('POST', mode === 'login' ? '/api/login' : '/api/register', {
        username: username.value.trim(),
        password: password.value,
        displayName: displayName.value,
      });
      renderSession();
      if (mode === 'register') showTypeReveal();
      else toast('ログインしました');
      location.hash = '#/';
    } catch (e2) {
      err.textContent = e2.message;
    } finally {
      submitBtn.disabled = false;
    }
  });
  render();
  app.replaceChildren(
    h('div', { class: 'card intro' },
      h('h1', { style: 'margin:0 0 4px' }, '三猿SNS'),
      h('p', { style: 'margin:0' }, 'このSNSのアカウントは3種類。投稿は誰でも自由にできますが、見られないものとコメントで使えないものがあります。'),
      h('ul', {}, Object.values(TYPES).map((t) => h('li', {}, h('b', {}, `${t.emoji} ${t.name}`), `：${t.desc}`))),
    ),
    form,
  );
  username.focus();
}

// 登録直後にどのタイプになったか発表する
function showTypeReveal() {
  const t = typeOf(me);
  const dlg = h('dialog', { class: 'reveal' },
    h('div', { class: 'reveal-emoji' }, t.emoji),
    h('p', { class: 'muted', style: 'margin:0' }, 'あなたのアカウントは…'),
    h('h2', {}, `${t.name}アカウント`),
    h('p', {}, t.desc),
    h('p', { class: 'muted' }, '投稿はどのタイプでも自由にできます。'),
    h('button', { onclick: () => dlg.close() }, 'はじめる'),
  );
  dlg.addEventListener('close', () => dlg.remove());
  document.body.append(dlg);
  dlg.showModal();
}

// ---------- セッション表示・ルーティング ----------
function renderSession() {
  const box = document.getElementById('session-box');
  document.querySelectorAll('[data-auth]').forEach((a) => (a.hidden = !me));
  if (me) {
    const t = typeOf(me);
    box.replaceChildren(
      h('div', {}, `${me.displayName} (@${me.username})`),
      h('div', { class: 'my-type' }, typeBadge(me), h('div', { class: 'muted' }, t.desc)),
      h('button', { class: 'link', onclick: async () => {
        await api('POST', '/api/logout');
        me = null;
        renderSession();
        toast('ログアウトしました');
        location.hash = '#/login';
      } }, 'ログアウト'),
    );
  } else {
    box.replaceChildren(h('a', { href: '#/login' }, 'ログイン / 新規登録'));
  }
}

async function router() {
  const hash = location.hash.slice(1) || '/';
  const [, section, arg] = hash.split('/');
  // 投稿を見るにはログインが必要
  if (!me && section !== 'login') return (location.hash = '#/login');
  document.querySelectorAll('[data-nav]').forEach((a) => {
    const nav = a.dataset.nav;
    a.classList.toggle('active', (section || 'home') === nav || (nav === 'me' && section === 'u' && me && arg === me.username));
  });
  window.scrollTo(0, 0);
  try {
    if (!section) await homePage();
    else if (section === 'explore') await explorePage();
    else if (section === 'users') await usersPage();
    else if (section === 'login') loginPage();
    else if (section === 'me') location.hash = `#/u/${me.username}`;
    else if (section === 'u') await profilePage(decodeURIComponent(arg));
    else if (section === 'p') await postPage(arg);
    else app.replaceChildren(h('div', { class: 'card empty' }, 'ページが見つかりません'));
  } catch (e) {
    app.replaceChildren(h('div', { class: 'card empty' }, e.message));
  }
}

window.addEventListener('hashchange', router);
(async () => {
  me = await api('GET', '/api/me').catch(() => null);
  renderSession();
  router();
})();
