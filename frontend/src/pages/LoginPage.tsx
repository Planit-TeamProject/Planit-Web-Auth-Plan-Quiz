import { FormEvent, useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { authErrorMessage, consumeRedirectResult, signIn, signInWithGoogle } from '../api/auth';
import { missingFirebaseConfigKeys } from '../firebase';
import { useLang } from '../i18n/lang';

// 화면 2. 로그인 (김동호 담당 - 회원가입/로그인). 인증은 Firebase Authentication 사용.
// 이메일/비밀번호 로그인 + 구글 로그인 + "아이디 저장하기".
//
// "아이디 저장하기" 동작:
//  - 처음(저장된 값 없음): 이메일칸 빈칸(placeholder만), 체크박스 꺼짐.
//  - 사용자가 직접 체크 + 로그인 성공 → 이메일을 localStorage 에 저장.
//  - 그 뒤 로그아웃하고 다시 로그인 화면에 오면: 이메일칸 자동 채움 + 체크박스 켜진 상태.
//  - 체크 해제하고 로그인하면 저장값 삭제.

interface LoginRouteState {
  notice?: string;
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
  const { t } = useLang();
  const routeState = (useLocation().state as LoginRouteState | null) ?? {};
  const rememberedEmail = loadRememberedEmail();

  const [email, setEmail] = useState(rememberedEmail ?? '');
  const [password, setPassword] = useState('');
  const [rememberId, setRememberId] = useState(rememberedEmail !== null);
  const [errorKey, setErrorKey] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [googleBusy, setGoogleBusy] = useState(false);
  const [welcomeName, setWelcomeName] = useState('');

  const configMissing = missingFirebaseConfigKeys.length > 0;
  const busy = submitting || googleBusy;

  useEffect(() => {
    consumeRedirectResult()
      .then((user) => {
        if (user) setWelcomeName(user.name ?? user.email ?? '');
      })
      .catch((err) => setErrorKey(authErrorMessage(err)));
  }, []);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErrorKey('');
    setWelcomeName('');
    if (!email.trim() || !password) {
      setErrorKey('login.err.empty');
      return;
    }
    setSubmitting(true);
    try {
      const user = await signIn({ email: email.trim(), password });
      saveRememberedEmail(rememberId ? email.trim() : null);
      setWelcomeName(user.name ?? user.email ?? '');
    } catch (err) {
      setErrorKey(authErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleGoogle() {
    setErrorKey('');
    setWelcomeName('');
    setGoogleBusy(true);
    try {
      const user = await signInWithGoogle();
      setWelcomeName(user.name ?? user.email ?? '');
    } catch (err) {
      setErrorKey(authErrorMessage(err));
    } finally {
      setGoogleBusy(false);
    }
  }

  return (
    <main>
      <h1>{t('login.title')}</h1>

      {configMissing && (
        <p className="msg-error">
          {t('login.configMissing', { keys: missingFirebaseConfigKeys.join(', ') })}
        </p>
      )}

      <div className="card">
        <p className="sub">{t('login.sub')}</p>

        <form onSubmit={handleSubmit}>
          {routeState.notice === 'signup' && <p className="msg-ok">{t('login.notice.signup')}</p>}

          <div className="field">
            <label htmlFor="login-email">{t('login.field.email')}</label>
            <input
              id="login-email"
              type="email"
              autoComplete="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          <div className="field">
            <label htmlFor="login-password">{t('login.field.password')}</label>
            <input
              id="login-password"
              type="password"
              autoComplete="current-password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          <label className="check">
            <input
              type="checkbox"
              checked={rememberId}
              onChange={(e) => setRememberId(e.target.checked)}
            />
            {t('login.remember')}
          </label>

          {errorKey && <p className="msg-error">{t(errorKey)}</p>}
          {welcomeName && <p className="msg-ok">{t('login.welcome', { name: welcomeName })}</p>}

          <button type="submit" className="btn btn-primary btn-block" disabled={busy}>
            {submitting ? t('login.submitting') : t('login.submit')}
          </button>
        </form>

        <div className="divider">{t('common.or')}</div>

        <button
          type="button"
          className="btn btn-ghost btn-block"
          onClick={handleGoogle}
          disabled={busy}
        >
          {googleBusy ? t('login.googleBusy') : t('login.google')}
        </button>

        <p className="switch-line">
          {t('login.noAccount')} <Link to="/signup">{t('login.toSignup')}</Link>
        </p>
      </div>
    </main>
  );
}
