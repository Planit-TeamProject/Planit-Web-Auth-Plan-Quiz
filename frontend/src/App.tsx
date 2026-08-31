import { useState } from 'react';
import { Link, Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import { signOutUser } from './api/auth';
import { useCurrentUser } from './auth/useCurrentUser';
import { LangSwitcher, useLang } from './i18n/lang';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import QuizPage from './pages/QuizPage';

// 김동호 담당: 회원가입 / 로그인 / 로그아웃(REQ-A-012) / 퀴즈봇. 저장소는 Firebase(Auth / Firestore).
// 상단 nav 는 개발 중 화면을 오가기 위한 임시 바이며, 팀원 웹과 합칠 때 제거하면 된다.

function TopNav() {
  const { t } = useLang();
  const { user } = useCurrentUser();
  const navigate = useNavigate();
  const [loggingOut, setLoggingOut] = useState(false);

  // REQ-A-012: 로그아웃 → Firebase 세션 종료 후 로그인 화면으로 이동.
  async function handleLogout() {
    setLoggingOut(true);
    try {
      await signOutUser();
      navigate('/login');
    } finally {
      setLoggingOut(false);
    }
  }

  return (
    <nav className="topnav">
      <Link to="/login">{t('nav.login')}</Link>
      <Link to="/signup">{t('nav.signup')}</Link>
      <Link to="/quiz">{t('nav.quiz')}</Link>
      <span className="spacer" />
      <LangSwitcher />
      {user && (
        <>
          <span className="who">{user.displayName ?? user.email}</span>
          <button type="button" onClick={handleLogout} disabled={loggingOut}>
            {loggingOut ? t('nav.loggingOut') : t('nav.logout')}
          </button>
        </>
      )}
    </nav>
  );
}

export default function App() {
  return (
    <>
      <TopNav />

      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/quiz" element={<QuizPage />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </>
  );
}
