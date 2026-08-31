import { useState } from 'react';
import { Link, Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import { signOutUser } from './api/auth';
import { useCurrentUser } from './auth/useCurrentUser';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import QuizPage from './pages/QuizPage';
import StudyPlanPage from './pages/StudyPlanPage';

// 김동호 담당: 회원가입 / 로그인 / 로그아웃(REQ-A-012) / 학습계획입력 / 퀴즈봇.
// 저장소는 전부 Firebase(Auth / Firestore / Storage).
// 아래 nav 는 개발 중 화면을 오가기 위한 임시 링크이며, 팀원 웹과 합칠 때 제거하면 된다.

function NavBar() {
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
    <nav>
      <Link to="/login">로그인</Link> · <Link to="/signup">회원가입</Link> ·{' '}
      <Link to="/plan">플랜 생성</Link> · <Link to="/quiz">퀴즈봇</Link>
      {user && (
        <>
          {'  |  '}
          <span>{user.displayName ?? user.email}</span>{' '}
          <button type="button" onClick={handleLogout} disabled={loggingOut}>
            {loggingOut ? '로그아웃 중…' : '로그아웃'}
          </button>
        </>
      )}
    </nav>
  );
}

export default function App() {
  return (
    <>
      <NavBar />

      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/plan" element={<StudyPlanPage />} />
        <Route path="/quiz" element={<QuizPage />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </>
  );
}
