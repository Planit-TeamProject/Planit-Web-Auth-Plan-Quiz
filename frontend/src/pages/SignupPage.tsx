import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authErrorMessage, signInWithGoogle, signUp } from '../api/auth';

// 화면 3. 회원가입 (김동호 담당 - 회원가입/로그인). 인증은 Firebase Authentication 사용.
// 이메일/비밀번호 가입과 구글 가입(=구글 로그인) 둘 다 지원한다.
// 이메일 형식 / 이메일 중복은 Firebase 가 검증하고(authErrorMessage 로 한국어 변환),
// 비밀번호 확인 일치(REQ-A-002)와 최소 8자(REQ-A-004)는 전송 전 화면에서 먼저 검사한다.
// 이메일 가입 성공 시 로그인 화면으로 이동한다(이메일은 넘기지 않음). 구글 가입은 그 자리에서 로그인됨.

export default function SignupPage() {
  const navigate = useNavigate();

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [googleBusy, setGoogleBusy] = useState(false);
  const [welcome, setWelcome] = useState('');

  const busy = submitting || googleBusy;

  function validate(): string | null {
    if (!name.trim()) return '이름을 입력해 주세요.';
    if (!email.trim()) return '이메일을 입력해 주세요.';
    if (!password) return '비밀번호를 입력해 주세요.';
    if (password.length < 8) return '비밀번호는 8자 이상이어야 합니다.';
    if (password !== passwordConfirm) return '비밀번호가 일치하지 않습니다.';
    return null;
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setWelcome('');
    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }
    setSubmitting(true);
    try {
      await signUp({ name: name.trim(), email: email.trim(), password });
      navigate('/login', { state: { notice: '가입 완료! 로그인해 주세요.' } });
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
      setWelcome(`구글 계정으로 가입되었습니다. 환영합니다, ${user.name ?? user.email}님`);
    } catch (err) {
      const message = authErrorMessage(err);
      if (message) setError(message);
    } finally {
      setGoogleBusy(false);
    }
  }

  return (
    <main>
      <h1>회원가입</h1>

      <div className="card">
        <p className="sub">1분이면 충분해요. 가입하면 로그인 화면으로 이동합니다.</p>

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="signup-name">이름</label>
            <input
              id="signup-name"
              type="text"
              autoComplete="name"
              placeholder="홍길동"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </div>

          <div className="field">
            <label htmlFor="signup-email">이메일</label>
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
            <label htmlFor="signup-password">비밀번호</label>
            <input
              id="signup-password"
              type="password"
              autoComplete="new-password"
              placeholder="8자 이상"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          <div className="field">
            <label htmlFor="signup-password-confirm">비밀번호 확인</label>
            <input
              id="signup-password-confirm"
              type="password"
              autoComplete="new-password"
              placeholder="다시 입력"
              value={passwordConfirm}
              onChange={(e) => setPasswordConfirm(e.target.value)}
            />
          </div>

          {error && <p className="msg-error">{error}</p>}
          {welcome && <p className="msg-ok">{welcome}</p>}

          <button type="submit" className="btn btn-gold btn-block" disabled={busy}>
            {submitting ? '처리 중…' : '회원가입 완료'}
          </button>
        </form>

        <div className="divider">또는</div>

        <button
          type="button"
          className="btn btn-ghost btn-block"
          onClick={handleGoogle}
          disabled={busy}
        >
          {googleBusy ? '구글 로그인 중…' : '구글로 가입/로그인'}
        </button>

        <p className="switch-line">
          이미 계정이 있으신가요? <Link to="/login">로그인</Link>
        </p>
      </div>
    </main>
  );
}
