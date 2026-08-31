import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authErrorMessage, signInWithGoogle, signUp } from '../api/auth';
import { useLang } from '../i18n/lang';

// 화면 3. 회원가입 (김동호 담당 - 회원가입/로그인). 인증은 Firebase Authentication 사용.
// 이메일/비밀번호 가입과 구글 가입(=구글 로그인) 둘 다 지원한다.
// 이메일 형식 / 이메일 중복은 Firebase 가 검증하고, 비밀번호 확인 일치(REQ-A-002)와
// 최소 8자(REQ-A-004)는 전송 전 화면에서 먼저 검사한다.
// 이메일 가입 성공 시 로그인 화면으로 이동한다(이메일은 넘기지 않음). 구글 가입은 그 자리에서 로그인됨.

export default function SignupPage() {
  const { t } = useLang();
  const navigate = useNavigate();

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [errorKey, setErrorKey] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [googleBusy, setGoogleBusy] = useState(false);
  const [welcomeName, setWelcomeName] = useState('');

  const busy = submitting || googleBusy;

  function validate(): string | null {
    if (!name.trim()) return 'signup.err.name';
    if (!email.trim()) return 'signup.err.email';
    if (!password) return 'signup.err.password';
    if (password.length < 8) return 'signup.err.passwordShort';
    if (password !== passwordConfirm) return 'signup.err.passwordMismatch';
    return null;
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErrorKey('');
    setWelcomeName('');
    const v = validate();
    if (v) {
      setErrorKey(v);
      return;
    }
    setSubmitting(true);
    try {
      await signUp({ name: name.trim(), email: email.trim(), password });
      navigate('/login', { state: { notice: 'signup' } });
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
      <h1>{t('signup.title')}</h1>

      <div className="card">
        <p className="sub">{t('signup.sub')}</p>

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="signup-name">{t('signup.field.name')}</label>
            <input
              id="signup-name"
              type="text"
              autoComplete="name"
              placeholder={t('signup.ph.name')}
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </div>

          <div className="field">
            <label htmlFor="signup-email">{t('signup.field.email')}</label>
            <input
              id="signup-email"
              type="email"
              autoComplete="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          <div className="field">
            <label htmlFor="signup-password">{t('signup.field.password')}</label>
            <input
              id="signup-password"
              type="password"
              autoComplete="new-password"
              placeholder={t('signup.ph.password')}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          <div className="field">
            <label htmlFor="signup-password-confirm">{t('signup.field.passwordConfirm')}</label>
            <input
              id="signup-password-confirm"
              type="password"
              autoComplete="new-password"
              placeholder={t('signup.ph.passwordConfirm')}
              value={passwordConfirm}
              onChange={(e) => setPasswordConfirm(e.target.value)}
            />
          </div>

          {errorKey && <p className="msg-error">{t(errorKey)}</p>}
          {welcomeName && (
            <p className="msg-ok">{t('signup.googleWelcome', { name: welcomeName })}</p>
          )}

          <button type="submit" className="btn btn-gold btn-block" disabled={busy}>
            {submitting ? t('signup.submitting') : t('signup.submit')}
          </button>
        </form>

        <div className="divider">{t('common.or')}</div>

        <button
          type="button"
          className="btn btn-ghost btn-block"
          onClick={handleGoogle}
          disabled={busy}
        >
          {googleBusy ? t('login.googleBusy') : t('signup.google')}
        </button>

        <p className="switch-line">
          {t('signup.haveAccount')} <Link to="/login">{t('signup.toLogin')}</Link>
        </p>
      </div>
    </main>
  );
}
