// MySQL にデータを保存する簡易SNSサーバー
// 起動: npm install → node server.js  →  http://localhost:3000
// 接続先は環境変数 DB_HOST / DB_PORT / DB_USER / DB_PASSWORD / DB_NAME で変更できる
const http = require('http');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const mysql = require('mysql2/promise');

const PORT = process.env.PORT || 3000;
const DATA_FILE = process.env.DATA_FILE || path.join(__dirname, 'data.json');
const UPLOAD_DIR = process.env.UPLOAD_DIR || path.join(__dirname, 'uploads');
const PUBLIC_DIR = path.join(__dirname, 'public');
const DB_CONFIG = {
  host: process.env.DB_HOST || 'localhost',
  port: Number(process.env.DB_PORT) || 3306,
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  charset: 'utf8mb4',
};
const DB_NAME = process.env.DB_NAME || 'hitokoto_sns';
const MAX_POST_LENGTH = 280;
const MAX_BODY_BYTES = 16 * 1024;
const MAX_POST_BODY_BYTES = 20 * 1024 * 1024;
const MAX_IMAGE_BYTES = 5 * 1024 * 1024;
const MAX_AUDIO_BYTES = 10 * 1024 * 1024;

// アカウントタイプ: それぞれ「見られない・コメントに使えない」メディアが1つある
const ACCOUNT_TYPES = {
  mizaru: { blocked: 'image', label: '見猿' },
  iwazaru: { blocked: 'text', label: '言わ猿' },
  kikazaru: { blocked: 'audio', label: '聞か猿' },
};
const MEDIA_LABEL = { text: '文字', image: '画像', audio: '音声' };

const IMAGE_TYPES = { 'image/png': 'png', 'image/jpeg': 'jpg', 'image/gif': 'gif', 'image/webp': 'webp' };
const AUDIO_TYPES = {
  'audio/webm': 'webm', 'audio/ogg': 'ogg', 'audio/mpeg': 'mp3', 'audio/mp3': 'mp3', 'audio/mp4': 'm4a',
  'audio/x-m4a': 'm4a', 'audio/aac': 'aac', 'audio/wav': 'wav', 'audio/x-wav': 'wav', 'audio/wave': 'wav',
};

// ---------- データ保存 (MySQL) ----------
const SCHEMA = [
  `CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    display_name VARCHAR(50) NOT NULL,
    bio VARCHAR(160) NOT NULL DEFAULT '',
    type VARCHAR(16) NOT NULL,
    salt CHAR(32) NOT NULL,
    password_hash CHAR(128) NOT NULL,
    created_at BIGINT NOT NULL
  )`,
  `CREATE TABLE IF NOT EXISTS follows (
    follower_id INT NOT NULL,
    followee_id INT NOT NULL,
    PRIMARY KEY (follower_id, followee_id),
    KEY (followee_id),
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (followee_id) REFERENCES users(id) ON DELETE CASCADE
  )`,
  // reply_to には外部キーを付けない(返信の連鎖削除はアプリ側で行う)
  `CREATE TABLE IF NOT EXISTS posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    text TEXT NOT NULL,
    image_file VARCHAR(64) NULL,
    image_mime VARCHAR(32) NULL,
    audio_file VARCHAR(64) NULL,
    audio_mime VARCHAR(32) NULL,
    reply_to INT NULL,
    created_at BIGINT NOT NULL,
    KEY (user_id, created_at),
    KEY (reply_to),
    KEY (created_at),
    KEY (image_file),
    KEY (audio_file),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
  )`,
  `CREATE TABLE IF NOT EXISTS likes (
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    PRIMARY KEY (post_id, user_id),
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
  )`,
  `CREATE TABLE IF NOT EXISTS sessions (
    token CHAR(64) PRIMARY KEY,
    user_id INT NOT NULL,
    created_at BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
  )`,
];

let pool;

async function initDb() {
  const conn = await mysql.createConnection(DB_CONFIG);
  await conn.query(`CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci`);
  await conn.end();
  pool = mysql.createPool({ ...DB_CONFIG, database: DB_NAME, connectionLimit: 10 });
  for (const sql of SCHEMA) await pool.query(sql);
  await importJsonData();
}

// 以前の data.json のデータを、DBが空のときだけ取り込む
async function importJsonData() {
  const [[{ n }]] = await pool.query('SELECT COUNT(*) AS n FROM users');
  if (n > 0 || !fs.existsSync(DATA_FILE)) return;
  const data = JSON.parse(fs.readFileSync(DATA_FILE, 'utf8'));
  const conn = await pool.getConnection();
  try {
    await conn.beginTransaction();
    for (const u of data.users || []) {
      // 以前のデータにはタイプがないので、ここで割り振る
      const type = ACCOUNT_TYPES[u.type] ? u.type : randomType();
      await conn.query(
        'INSERT INTO users (id, username, display_name, bio, type, salt, password_hash, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
        [u.id, u.username, u.displayName, u.bio || '', type, u.salt, u.passwordHash, u.createdAt]
      );
    }
    for (const u of data.users || []) {
      for (const f of u.following || []) await conn.query('INSERT IGNORE INTO follows VALUES (?, ?)', [u.id, f]);
    }
    for (const p of data.posts || []) {
      await conn.query(
        'INSERT INTO posts (id, user_id, text, image_file, image_mime, audio_file, audio_mime, reply_to, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)',
        [p.id, p.userId, p.text || '', p.image?.file ?? null, p.image?.mime ?? null,
          p.audio?.file ?? null, p.audio?.mime ?? null, p.replyTo || null, p.createdAt]
      );
      for (const uid of p.likes || []) await conn.query('INSERT IGNORE INTO likes VALUES (?, ?)', [p.id, uid]);
    }
    for (const [token, uid] of Object.entries(data.sessions || {})) {
      await conn.query('INSERT INTO sessions VALUES (?, ?, ?)', [token, uid, Date.now()]);
    }
    await conn.commit();
    console.log(`data.json から取り込み: ユーザー${(data.users || []).length}件, 投稿${(data.posts || []).length}件`);
  } catch (e) {
    await conn.rollback();
    throw e;
  } finally {
    conn.release();
  }
}

fs.mkdirSync(UPLOAD_DIR, { recursive: true });

function randomType() {
  const keys = Object.keys(ACCOUNT_TYPES);
  return keys[crypto.randomInt(keys.length)];
}

// 一覧表示に必要な件数などをまとめて取る SELECT (? は見ている人の id)
const USER_SELECT = `
  SELECT u.*,
    (SELECT COUNT(*) FROM follows WHERE follower_id = u.id) AS following_count,
    (SELECT COUNT(*) FROM follows WHERE followee_id = u.id) AS follower_count,
    (SELECT COUNT(*) FROM posts WHERE user_id = u.id) AS post_count,
    EXISTS(SELECT 1 FROM follows WHERE follower_id = ? AND followee_id = u.id) AS is_following
  FROM users u`;

const POST_SELECT = `
  SELECT p.*,
    a.username AS author_username, a.display_name AS author_display_name, a.type AS author_type,
    (SELECT COUNT(*) FROM likes WHERE post_id = p.id) AS like_count,
    EXISTS(SELECT 1 FROM likes WHERE post_id = p.id AND user_id = ?) AS liked,
    (SELECT COUNT(*) FROM posts r WHERE r.reply_to = p.id) AS reply_count,
    pa.username AS parent_username
  FROM posts p
  JOIN users a ON a.id = p.user_id
  LEFT JOIN posts pp ON pp.id = p.reply_to
  LEFT JOIN users pa ON pa.id = pp.user_id`;

async function findUser(viewer, where, params) {
  const [rows] = await pool.query(`${USER_SELECT} WHERE ${where}`, [viewer.id, ...params]);
  return rows[0] || null;
}

async function findPosts(viewer, rest, params = []) {
  const [rows] = await pool.query(`${POST_SELECT} ${rest}`, [viewer.id, ...params]);
  return rows;
}

// ---------- ユーティリティ ----------
function hashPassword(password, salt) {
  return crypto.scryptSync(password, salt, 64).toString('hex');
}

function verifyPassword(password, user) {
  const hash = Buffer.from(hashPassword(password, user.salt), 'hex');
  return crypto.timingSafeEqual(hash, Buffer.from(user.password_hash, 'hex'));
}

function parseCookies(req) {
  const out = {};
  (req.headers.cookie || '').split(';').forEach((pair) => {
    const i = pair.indexOf('=');
    if (i > 0) out[pair.slice(0, i).trim()] = decodeURIComponent(pair.slice(i + 1).trim());
  });
  return out;
}

async function currentUser(req) {
  const token = parseCookies(req).session;
  if (!token) return null;
  const [rows] = await pool.query('SELECT u.* FROM sessions s JOIN users u ON u.id = s.user_id WHERE s.token = ?', [token]);
  return rows[0] || null;
}

function canSee(viewer, kind) {
  return ACCOUNT_TYPES[viewer.type].blocked !== kind;
}

function send(res, status, data, headers = {}) {
  res.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8', ...headers });
  res.end(JSON.stringify(data));
}

function readBody(req, limit = MAX_BODY_BYTES) {
  return new Promise((resolve, reject) => {
    let size = 0;
    const chunks = [];
    const onData = (c) => {
      size += c.length;
      if (size > limit) {
        req.off('data', onData);
        req.resume();
        reject(new HttpError(413, 'ファイルが大きすぎます'));
      } else chunks.push(c);
    };
    req.on('data', onData);
    req.on('end', () => {
      if (size > limit) return;
      if (!chunks.length) return resolve({});
      try {
        resolve(JSON.parse(Buffer.concat(chunks).toString('utf8')));
      } catch {
        reject(new HttpError(400, 'JSONが不正です'));
      }
    });
    req.on('error', reject);
  });
}

class HttpError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

async function requireUser(req) {
  const user = await currentUser(req);
  if (!user) throw new HttpError(401, 'ログインが必要です');
  return user;
}

// data URL を検証して uploads/ に保存する
function saveMedia(dataUrl, allowed, maxBytes, label) {
  const m = /^data:([^;,]+)[^,]*;base64,([A-Za-z0-9+/=]+)$/.exec(String(dataUrl));
  if (!m) throw new HttpError(400, `${label}のデータが不正です`);
  const mime = m[1].toLowerCase();
  const ext = allowed[mime];
  if (!ext) throw new HttpError(400, `この形式の${label}は使えません (${mime})`);
  const buf = Buffer.from(m[2], 'base64');
  if (!buf.length) throw new HttpError(400, `${label}が空です`);
  if (buf.length > maxBytes) throw new HttpError(413, `${label}は${maxBytes / 1024 / 1024}MBまでです`);
  const file = `${crypto.randomBytes(16).toString('hex')}.${ext}`;
  fs.writeFileSync(path.join(UPLOAD_DIR, file), buf);
  return { file, mime };
}

// u は USER_SELECT で取った行。自分の自己紹介はタイプに関係なく見える
function publicUser(u, viewer) {
  const seeBio = viewer.id === u.id || canSee(viewer, 'text');
  return {
    username: u.username,
    displayName: u.display_name,
    bio: seeBio ? u.bio : null,
    bioHidden: !seeBio && !!u.bio,
    type: u.type,
    createdAt: u.created_at,
    followingCount: u.following_count,
    followerCount: u.follower_count,
    postCount: u.post_count,
    isFollowing: !!u.is_following,
    isMe: viewer.id === u.id,
  };
}

// 見る人のタイプに応じて、見えない内容はサーバーから送らない (p は POST_SELECT で取った行)
// 自分の投稿はタイプに関係なく全部見える
function publicPost(p, viewer) {
  const see = (kind) => viewer.id === p.user_id || canSee(viewer, kind);
  const media = (kind, file) => (!file ? null : see(kind) ? { url: `/media/${file}` } : { hidden: true });
  return {
    id: p.id,
    text: p.text && see('text') ? p.text : null,
    textHidden: !!p.text && !see('text'),
    image: media('image', p.image_file),
    audio: media('audio', p.audio_file),
    createdAt: p.created_at,
    author: { username: p.author_username, displayName: p.author_display_name, type: p.author_type },
    likeCount: p.like_count,
    liked: !!p.liked,
    replyCount: p.reply_count,
    replyTo: p.parent_username ? { id: p.reply_to, username: p.parent_username } : null,
    isMine: viewer.id === p.user_id,
  };
}

async function postResponse(id, viewer) {
  const [post] = await findPosts(viewer, 'WHERE p.id = ?', [id]);
  return post ? publicPost(post, viewer) : null;
}

async function userResponse(id, viewer) {
  return publicUser(await findUser(viewer, 'u.id = ?', [id]), viewer);
}

// ---------- API ----------
const routes = [];
function route(method, pattern, handler) {
  const keys = [];
  const re = new RegExp('^' + pattern.replace(/:(\w+)/g, (_, k) => (keys.push(k), '([^/]+)')) + '$');
  routes.push({ method, re, keys, handler });
}

route('POST', '/api/register', async (req) => {
  const { username = '', password = '', displayName = '' } = await readBody(req);
  if (!/^[a-zA-Z0-9_]{3,20}$/.test(username))
    throw new HttpError(400, 'ユーザー名は英数字と_で3〜20文字にしてください');
  if (typeof password !== 'string' || password.length < 8)
    throw new HttpError(400, 'パスワードは8文字以上にしてください');
  const salt = crypto.randomBytes(16).toString('hex');
  let result;
  try {
    // username 列は大文字小文字を区別しない照合順序なので、UNIQUE で重複を防げる
    [result] = await pool.query(
      'INSERT INTO users (username, display_name, bio, type, salt, password_hash, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)',
      [username, String(displayName).trim().slice(0, 50) || username, '', randomType(), salt, hashPassword(password, salt), Date.now()]
    );
  } catch (e) {
    if (e.code === 'ER_DUP_ENTRY') throw new HttpError(409, 'そのユーザー名は既に使われています');
    throw e;
  }
  return login(result.insertId);
});

route('POST', '/api/login', async (req) => {
  const { username = '', password = '' } = await readBody(req);
  const [[user]] = await pool.query('SELECT * FROM users WHERE username = ?', [String(username)]);
  if (!user || !verifyPassword(String(password), user))
    throw new HttpError(401, 'ユーザー名またはパスワードが違います');
  return login(user.id);
});

async function login(userId) {
  const token = crypto.randomBytes(32).toString('hex');
  await pool.query('INSERT INTO sessions (token, user_id, created_at) VALUES (?, ?, ?)', [token, userId, Date.now()]);
  const user = await findUser({ id: userId }, 'u.id = ?', [userId]);
  return {
    body: publicUser(user, user),
    headers: { 'Set-Cookie': `session=${token}; HttpOnly; SameSite=Lax; Path=/; Max-Age=2592000` },
  };
}

route('POST', '/api/logout', async (req) => {
  const token = parseCookies(req).session;
  if (token) await pool.query('DELETE FROM sessions WHERE token = ?', [token]);
  return { body: { ok: true }, headers: { 'Set-Cookie': 'session=; HttpOnly; Path=/; Max-Age=0' } };
});

route('GET', '/api/me', async (req) => {
  const user = await currentUser(req);
  return user ? userResponse(user.id, user) : null;
});

route('PATCH', '/api/me', async (req) => {
  const user = await requireUser(req);
  const { displayName, bio } = await readBody(req);
  if (typeof displayName === 'string' && displayName.trim())
    await pool.query('UPDATE users SET display_name = ? WHERE id = ?', [displayName.trim().slice(0, 50), user.id]);
  if (typeof bio === 'string') await pool.query('UPDATE users SET bio = ? WHERE id = ?', [bio.slice(0, 160), user.id]);
  return userResponse(user.id, user);
});

route('GET', '/api/timeline', async (req, params, query) => {
  const viewer = await requireUser(req);
  const posts =
    query.get('type') === 'home'
      ? await findPosts(
          viewer,
          `WHERE p.user_id = ? OR p.user_id IN (SELECT followee_id FROM follows WHERE follower_id = ?)
           ORDER BY p.created_at DESC, p.id DESC LIMIT 100`,
          [viewer.id, viewer.id]
        )
      : await findPosts(viewer, 'ORDER BY p.created_at DESC, p.id DESC LIMIT 100');
  return posts.map((p) => publicPost(p, viewer));
});

route('POST', '/api/posts', async (req) => {
  const user = await requireUser(req);
  const { text = '', image = null, audio = null, replyTo = null } = await readBody(req, MAX_POST_BODY_BYTES);
  const body = String(text).trim();
  if (!body && !image && !audio) throw new HttpError(400, '文字・画像・音声のどれかを入れてください');
  if ([...body].length > MAX_POST_LENGTH) throw new HttpError(400, `${MAX_POST_LENGTH}文字以内にしてください`);
  if (replyTo) {
    const [[parent]] = await pool.query('SELECT id FROM posts WHERE id = ?', [Number(replyTo)]);
    if (!parent) throw new HttpError(404, '返信先の投稿がありません');
    // コメントではアカウントタイプで禁止されたメディアは使えない
    const blocked = ACCOUNT_TYPES[user.type].blocked;
    const used = { text: !!body, image: !!image, audio: !!audio };
    if (used[blocked])
      throw new HttpError(403, `${ACCOUNT_TYPES[user.type].label}アカウントはコメントに${MEDIA_LABEL[blocked]}を使えません`);
  }
  const img = image ? saveMedia(image, IMAGE_TYPES, MAX_IMAGE_BYTES, '画像') : null;
  const aud = audio ? saveMedia(audio, AUDIO_TYPES, MAX_AUDIO_BYTES, '音声') : null;
  const [result] = await pool.query(
    'INSERT INTO posts (user_id, text, image_file, image_mime, audio_file, audio_mime, reply_to, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
    [user.id, body, img?.file ?? null, img?.mime ?? null, aud?.file ?? null, aud?.mime ?? null, replyTo ? Number(replyTo) : null, Date.now()]
  );
  return postResponse(result.insertId, user);
});

route('GET', '/api/posts/:id', async (req, { id }) => {
  const viewer = await requireUser(req);
  const [post] = await findPosts(viewer, 'WHERE p.id = ?', [Number(id)]);
  if (!post) throw new HttpError(404, '投稿が見つかりません');
  const replies = await findPosts(viewer, 'WHERE p.reply_to = ? ORDER BY p.created_at, p.id', [post.id]);
  return { post: publicPost(post, viewer), replies: replies.map((p) => publicPost(p, viewer)) };
});

route('DELETE', '/api/posts/:id', async (req, { id }) => {
  const user = await requireUser(req);
  const [[post]] = await pool.query('SELECT id, user_id FROM posts WHERE id = ?', [Number(id)]);
  if (!post) throw new HttpError(404, '投稿が見つかりません');
  if (post.user_id !== user.id) throw new HttpError(403, '自分の投稿のみ削除できます');
  // 返信(返信の返信も含む)も一緒に削除
  const [targets] = await pool.query(
    `WITH RECURSIVE tree AS (
       SELECT id FROM posts WHERE id = ?
       UNION ALL
       SELECT p.id FROM posts p JOIN tree t ON p.reply_to = t.id
     )
     SELECT p.id, p.image_file, p.audio_file FROM posts p JOIN tree t ON t.id = p.id`,
    [post.id]
  );
  await pool.query('DELETE FROM posts WHERE id IN (?)', [targets.map((p) => p.id)]);
  for (const p of targets) {
    for (const file of [p.image_file, p.audio_file]) if (file) fs.rm(path.join(UPLOAD_DIR, file), () => {});
  }
  return { ok: true };
});

route('POST', '/api/posts/:id/like', async (req, { id }) => {
  const user = await requireUser(req);
  const [[post]] = await pool.query('SELECT id FROM posts WHERE id = ?', [Number(id)]);
  if (!post) throw new HttpError(404, '投稿が見つかりません');
  const [del] = await pool.query('DELETE FROM likes WHERE post_id = ? AND user_id = ?', [post.id, user.id]);
  if (!del.affectedRows) await pool.query('INSERT IGNORE INTO likes (post_id, user_id) VALUES (?, ?)', [post.id, user.id]);
  return postResponse(post.id, user);
});

route('GET', '/api/users/:username', async (req, { username }) => {
  const viewer = await requireUser(req);
  const user = await findUser(viewer, 'u.username = ?', [decodeURIComponent(username)]);
  if (!user) throw new HttpError(404, 'ユーザーが見つかりません');
  const posts = await findPosts(viewer, 'WHERE p.user_id = ? ORDER BY p.created_at DESC, p.id DESC', [user.id]);
  return { user: publicUser(user, viewer), posts: posts.map((p) => publicPost(p, viewer)) };
});

route('POST', '/api/users/:username/follow', async (req, { username }) => {
  const viewer = await requireUser(req);
  const [[target]] = await pool.query('SELECT id FROM users WHERE username = ?', [decodeURIComponent(username)]);
  if (!target) throw new HttpError(404, 'ユーザーが見つかりません');
  if (target.id === viewer.id) throw new HttpError(400, '自分はフォローできません');
  const [del] = await pool.query('DELETE FROM follows WHERE follower_id = ? AND followee_id = ?', [viewer.id, target.id]);
  if (!del.affectedRows)
    await pool.query('INSERT IGNORE INTO follows (follower_id, followee_id) VALUES (?, ?)', [viewer.id, target.id]);
  return userResponse(target.id, viewer);
});

route('GET', '/api/users', async (req) => {
  const viewer = await requireUser(req);
  const [users] = await pool.query(`${USER_SELECT} ORDER BY u.created_at DESC, u.id DESC`, [viewer.id]);
  return users.map((u) => publicUser(u, viewer));
});

// ---------- アップロードされた画像・音声 ----------
// 見る権限がないタイプのアカウントにはファイル自体を渡さない (自分の投稿は除く)
async function serveMedia(req, res, name) {
  const viewer = await currentUser(req);
  const [[p]] = await pool.query(
    'SELECT user_id, image_file, image_mime, audio_file, audio_mime FROM posts WHERE image_file = ? OR audio_file = ? LIMIT 1',
    [name, name]
  );
  let kind = null;
  let mime = null;
  if (p && p.image_file === name) [kind, mime] = ['image', p.image_mime];
  else if (p) [kind, mime] = ['audio', p.audio_mime];
  if (!viewer || !kind || (viewer.id !== p.user_id && !canSee(viewer, kind))) {
    res.writeHead(404, { 'Content-Type': 'text/plain' });
    return res.end('Not found');
  }
  fs.readFile(path.join(UPLOAD_DIR, name), (err, data) => {
    if (err) {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      return res.end('Not found');
    }
    res.writeHead(200, {
      'Content-Type': mime,
      'Content-Length': data.length,
      'X-Content-Type-Options': 'nosniff',
      'Cache-Control': 'private, no-store',
    });
    res.end(data);
  });
}

// ---------- 静的ファイル ----------
const MIME = { '.html': 'text/html', '.css': 'text/css', '.js': 'text/javascript', '.svg': 'image/svg+xml', '.png': 'image/png' };

function serveStatic(req, res, pathname) {
  const file = path.normalize(path.join(PUBLIC_DIR, pathname === '/' ? 'index.html' : pathname));
  if (!file.startsWith(PUBLIC_DIR + path.sep)) {
    res.writeHead(403);
    return res.end();
  }
  fs.readFile(file, (err, data) => {
    if (err) {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      return res.end('Not found');
    }
    res.writeHead(200, { 'Content-Type': (MIME[path.extname(file)] || 'application/octet-stream') + '; charset=utf-8' });
    res.end(data);
  });
}

// ---------- サーバー ----------
const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, 'http://localhost');
  const mediaMatch = /^\/media\/([a-f0-9]{32}\.[a-z0-9]+)$/.exec(url.pathname);
  if (mediaMatch) {
    return serveMedia(req, res, mediaMatch[1]).catch((e) => {
      console.error(e);
      res.writeHead(500, { 'Content-Type': 'text/plain' });
      res.end('Server error');
    });
  }
  if (!url.pathname.startsWith('/api/')) return serveStatic(req, res, decodeURIComponent(url.pathname));

  // 変更系リクエストはJSONのみ受け付ける(簡易CSRF対策)
  if (req.method !== 'GET' && !(req.headers['content-type'] || '').startsWith('application/json')) {
    return send(res, 415, { error: 'Content-Type は application/json にしてください' });
  }

  for (const r of routes) {
    const m = r.method === req.method && url.pathname.match(r.re);
    if (!m) continue;
    const params = Object.fromEntries(r.keys.map((k, i) => [k, m[i + 1]]));
    try {
      const result = await r.handler(req, params, url.searchParams);
      if (result && result.headers && 'body' in result) return send(res, 200, result.body, result.headers);
      return send(res, 200, result);
    } catch (e) {
      if (e instanceof HttpError) return send(res, e.status, { error: e.message });
      console.error(e);
      return send(res, 500, { error: 'サーバーエラー' });
    }
  }
  send(res, 404, { error: 'Not found' });
});

initDb()
  .then(() => server.listen(PORT, () => console.log(`SNS起動: http://localhost:${PORT} (MySQL: ${DB_CONFIG.host}/${DB_NAME})`)))
  .catch((e) => {
    console.error('MySQL に接続できませんでした:', e.message);
    console.error('DB_HOST / DB_PORT / DB_USER / DB_PASSWORD を確認してください');
    process.exit(1);
  });
