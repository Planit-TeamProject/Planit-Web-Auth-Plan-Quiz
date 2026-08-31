import { FormEvent, useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { authErrorMessage, consumeRedirectResult, signIn, signInWithGoogle } from '../api/auth';
import { missingFirebaseConfigKeys } from '../firebase';

// 화면 2. 로그인 (김동호 담당 - 회원가입/로그인). 인증은 Firebase Authentication 사용.
// 이메일/비밀번호 로그인 + 구글 로그인 + "아이디 저장하기"(이메일을 localStorage 에 기억).
// 로그인 성공 후 이동할 메인 화면은 팀원 웹이 담당하므로, 여기서는 성공 메시지만 표시한다.
// 연결 지점: handleSubmit / handleGoogle / 리다이렉트 결과 처리 useEffect 의 성공 분기.

interface LoginRouteState {
  notice?: string;
  email?: string;
}

const REMEMBERED_EMAIL_KEY = 'planit.rememberedEmail';

function loadRememberedEmail(): string | null {
  try {
    return localStorage.getItem(REMEMBERED_EMAIL_KEY);
  } catch {
    return null;
  }
}

function saveRememberedEmail(email: string | null) {
  try {
    if (email) localStorage.setItem(REMEMBERED_EMAIL_KEY, email);
    else localStorage.removeItem(REMEMBERED_EMAIL_KEY);
  } catch {
    /* 저장이 막힌 환경(시크릿 모드 등)은 무시 */
  }
}

export default function LoginPage() {
  const routeState = (useLocation().state as LoginRouteState | null) ?? {};
  const rememberedEmail = loadRememberedEmail();

  // 회원가입 직후 넘어온 이메일 > 저장된 이메일 > 빈 값
  const [email, setEmail] = useState(routeState.email ?? rememberedEmail ?? '');
  const [password, setPassword] = useState('');
  const [rememberId, setRememberId] = useState(rememberedEmail !== null);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [googleBusy, setGoogleBusy] = useState(false);
  const [welcome, setWelcome] = useState('');

  const configMissing = missingFirebaseConfigKeys.length > 0;
  const busy = submitting || googleBusy;

  // 구글 "리다이렉트" 로그인에서 되돌아온 경우 결과를 받는다.
  useEffect(() => {
    consumeRedirectResult()
      .then((user) => {
        if (user) setWelcome(`로그인 성공! 환영합니다, ${user.name ?? user.email}님`);
      })
      .catch((err) => setError(authErrorMessage(err)));
  }, []);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setWelcome('');

    if (!email.trim() || !password) {
      setError('이메일과 비밀번호를 입력해 주세요.');
      return;
    }

    setSubmitting(true);
    try {
      const user = await signIn({ email: email.trim(), password });
      // REQ: "아이디 저장하기" 체크 시 다음 로그인부터 이메일이 자동으로 채워지도록 저장.
      saveRememberedEmail(rememberId ? email.trim() : null);
      setWelcome(`로그인 성공! 환영합니다, ${user.name ?? user.email}님`);
    } catch (err) {
      setError(authErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleGoogle() {
    setError('');
    setWelcome('');
    setGoogleBusy(true);
    try {
      const user = await signInWithGoogle();
      setWelcome(`로그인 성공! 환영합니다, ${user.name ?? user.email}님`);
    } catch (err) {
      const message = authErrorMessage(err);
      if (message) setError(message);
    } finally {
      setGoogleBusy(false);
    }
  }

  return (
    <main>
      <h1>로그인</h1>

      {configMissing && (
        <p role="alert">
          Firebase 설정이 없습니다. <code>frontend/.env.local</code> 에 웹 앱 config 값을 채우고
          dev 서버를 재시작하세요. (누락: {missingFirebaseConfigKeys.join(', ')})
        </p>
      )}

      <form onSubmit={handleSubmit}>
        {routeState.notice && <p role="status">{routeState.notice}</p>}

        <div>
          <label htmlFor="login-email">이메일</label>
          <input
            id="login-email"
            type="email"
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>

        <div>
          <label htmlFor="login-password">비밀번호</label>
          <input
            id="login-password"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>

        <label>
          <input
            type="checkbox"
            checked={rememberId}
            onChange={(e) => setRememberId(e.target.checked)}
          />{' '}
          아이디 저장하기
        </label>

        {error && <p role="alert">{error}</p>}
        {welcome && <p role="status">{welcome}</p>}

        <button type="submit" disabled={busy}>
          {submitting ? '로그인 중…' : '로그인'}
        </button>
      </form>

      <button type="button" onClick={handleGoogle} disabled={busy}>
        {googleBusy ? '구글 로그인 중…' : '구글로 로그인하기'}
      </button>

      <p>
        계정이 없으신가요? <Link to="/signup">회원가입</Link>
      </p>
    </main>
  );
}
