import { Link, Navigate, Route, Routes } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import QuizPage from './pages/QuizPage';
import StudyPlanPage from './pages/StudyPlanPage';

// 김동호 담당: 회원가입/로그인 + 퀴즈봇 + 학습계획입력. 저장소는 전부 Firebase(Auth/Firestore/Storage).
// 아래 nav 는 개발 중 화면을 오가기 위한 임시 링크이며, 팀원 웹과 합칠 때 제거하면 된다.

export default function App() {
  return (
    <>
      <nav>
        <Link to="/login">로그인</Link> · <Link to="/signup">회원가입</Link> ·{' '}
        <Link to="/plan">플랜 생성</Link> · <Link to="/quiz">퀴즈봇</Link>
      </nav>

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
