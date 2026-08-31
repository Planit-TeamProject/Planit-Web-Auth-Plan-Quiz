import { FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authErrorMessage, consumeRedirectResult, signInWithGoogle, signUp } from '../api/auth';
import { missingFirebaseConfigKeys } from '../firebase';

// 화면 3. 회원가입 (김동호 담당 - 회원가입/로그인). 인증은 Firebase Authentication 사용.
// 이메일/비밀번호 가입과 구글 가입(=구글 로그인) 둘 다 지원한다.
// 이메일 형식 / 이메일 중복은 Firebase 가 검증하고(authErrorMessage 로 한국어 변환),
// 비밀번호 확인 일치(REQ-A-002)와 최소 8자(REQ-A-004)는 전송 전 화면에서 먼저 검사한다.
// 이메일 가입 성공 시 로그인 화면으로 이동한다. 구글 가입은 그 자리에서 바로 로그인된 상태가 된다.

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

  const configMissing = missingFirebaseConfigKeys.length > 0;
  const busy = submitting || googleBusy;

  // 구글 "리다이렉트" 로그인에서 되돌아온 경우 결과를 받는다.
  useEffect(() => {
    consumeRedirectResult()
      .then((user) => {
        if (user) {
          setWelcome(`구글 계정으로 가입되었습니다. 환영합니다, ${user.name ?? user.email}님`);
        }
      })
      .catch((err) => setError(authErrorMessage(err)));
  }, []);

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
      // 로그인 화면에 이메일을 미리 채우지 않는다. 안내 문구만 전달.
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

      {configMissing && (
        <p role="alert">
          Firebase 설정이 없습니다. <code>frontend/.env.local</code> 에 웹 앱 config 값을 채우고
          dev 서버를 재시작하세요. (누락: {missingFirebaseConfigKeys.join(', ')})
        </p>
      )}

      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="signup-name">이름</label>
          <input
            id="signup-name"
            type="text"
            autoComplete="name"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
        </div>

        <div>
          <label htmlFor="signup-email">이메일</label>
          <input
            id="signup-email"
            type="email"
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>

        <div>
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

        <div>
          <label htmlFor="signup-password-confirm">비밀번호 확인</label>
          <input
            id="signup-password-confirm"
            type="password"
            autoComplete="new-password"
            value={passwordConfirm}
            onChange={(e) => setPasswordConfirm(e.target.value)}
          />
        </div>

        {error && <p role="alert">{error}</p>}
        {welcome && <p role="status">{welcome}</p>}

        <button type="submit" disabled={busy}>
          {submitting ? '처리 중…' : '회원가입 완료'}
        </button>
      </form>

      <button type="button" onClick={handleGoogle} disabled={busy}>
        {googleBusy ? '구글 로그인 중…' : '구글로 로그인하기'}
      </button>

      <p>
        이미 계정이 있으신가요? <Link to="/login">로그인</Link>
      </p>
    </main>
  );
}
