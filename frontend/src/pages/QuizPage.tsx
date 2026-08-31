import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useCurrentUser } from '../auth/useCurrentUser';
import {
  getQuizSummary,
  startQuiz,
  submitAnswer,
  type Quiz,
  type QuizQuestion,
  type QuizSummary,
  type SubmitResult,
} from '../api/quiz';

// 화면 6. 오늘의 퀴즈 (김동호 담당 - 퀴즈봇). 저장소는 Firestore.
// study_plan.json 1일차 학습 범위로 4지선다 3문제(BASIC 2 + APPLIED 1)를 만든다 (REQ-Q-001~003).
// 문제별 제출 → 그 자리에서 채점·풀이 (REQ-Q-004~005) → 3문제 다 풀면 요약 (REQ-Q-006).

interface DayItem {
  title: string;
  pageRange: string | null;
  status: string;
}

export default function QuizPage() {
  const { user, loading } = useCurrentUser();

  const [dayMeta, setDayMeta] = useState<{ date: string; minutes: number } | null>(null);
  const [dayItems, setDayItems] = useState<DayItem[]>([]);
  const [todayScope, setTodayScope] = useState('');
  const [loadError, setLoadError] = useState('');

  const [startBusy, setStartBusy] = useState(false);
  const [quiz, setQuiz] = useState<Quiz | null>(null);
  const [results, setResults] = useState<Record<number, SubmitResult>>({});
  const [error, setError] = useState('');
  const [summary, setSummary] = useState<QuizSummary | null>(null);

  useEffect(() => {
    loadDayOne();
  }, []);

  // 모든 문제를 제출하면 요약을 불러온다 (REQ-Q-006).
  useEffect(() => {
    if (quiz && Object.keys(results).length === quiz.questions.length) {
      getQuizSummary(quiz.id)
        .then(setSummary)
        .catch((e) => setError(e instanceof Error ? e.message : String(e)));
    }
  }, [quiz, results]);

  async function loadDayOne() {
    setLoadError('');
    try {
      const res = await fetch('/study_plan.json');
      if (!res.ok) throw new Error(`study_plan.json 을 불러오지 못했습니다 (${res.status})`);
      const plan = await res.json();
      const day1 = plan.days?.[0];
      if (!day1 || !Array.isArray(day1.items) || day1.items.length === 0) {
        setTodayScope('');
        setDayItems([]);
        setLoadError('1일차 학습 항목이 없습니다.');
        return;
      }
      setDayMeta({ date: day1.date, minutes: day1.minutes });
      setDayItems(day1.items);
      setTodayScope(
        day1.items
          .map((it: DayItem) => (it.pageRange ? `${it.title}(${it.pageRange})` : it.title))
          .join(', '),
      );
    } catch (e) {
      setTodayScope('');
      setLoadError(e instanceof Error ? e.message : String(e));
    }
  }

  async function handleStart() {
    if (!user || !todayScope) {
      if (!todayScope) setError('오늘의 일과를 먼저 불러와 주세요.');
      return;
    }
    setError('');
    setSummary(null);
    setResults({});
    setStartBusy(true);
    try {
      setQuiz(await startQuiz({ uid: user.uid, subjectName: '테스트', todayScope }));
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally {
      setStartBusy(false);
    }
  }

  async function handleSubmit(question: QuizQuestion, selectedChoice: number) {
    if (!quiz || results[question.questionNo]) return;
    try {
      const r = await submitAnswer(quiz.id, question, selectedChoice);
      setResults((prev) => ({ ...prev, [question.questionNo]: r }));
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    }
  }

  if (loading)
    return (
      <main>
        <p>불러오는 중…</p>
      </main>
    );

  if (!user) {
    return (
      <main>
        <h1>오늘의 퀴즈</h1>
        <p role="alert">
          로그인이 필요합니다. <Link to="/login">로그인하러 가기</Link>
        </p>
      </main>
    );
  }

  return (
    <main>
      <h1>오늘의 퀴즈</h1>
      <p>
        study_plan.json 의 <strong>1일차</strong> 학습 범위로 4지선다 3문제(쉬운 문제 2 + 응용 1)를
        만듭니다. 아래 오늘의 일과를 확인하고 &lsquo;퀴즈 시작&rsquo;을 누르세요.
      </p>

      <section>
        <h2>오늘의 일과 (study_plan.json 1일차)</h2>
        {loadError && <p role="alert">{loadError}</p>}
        {dayMeta && (
          <p>
            1일차 · {dayMeta.date} · {dayMeta.minutes}분
          </p>
        )}
        <ul>
          {dayItems.map((it, i) => (
            <li key={i}>
              {it.title} — {it.pageRange ? `${it.pageRange} · ` : ''}
              {it.status}
            </li>
          ))}
        </ul>
        <button type="button" onClick={loadDayOne}>
          다시 불러오기
        </button>{' '}
        <button type="button" onClick={handleStart} disabled={startBusy || !todayScope}>
          {startBusy ? '문제 만드는 중…' : '퀴즈 시작'}
        </button>
        {error && <p role="alert">{error}</p>}
      </section>

      {quiz && (
        <section>
          <h2>문제</h2>
          {quiz.questions.map((q) => (
            <QuestionCard
              key={q.questionNo}
              question={q}
              total={quiz.questions.length}
              result={results[q.questionNo] ?? null}
              onSubmit={handleSubmit}
            />
          ))}
        </section>
      )}

      {summary && (
        <section>
          <h2>결과</h2>
          <p>
            <strong>
              {summary.correctCount} / {summary.totalQuestionCount}
            </strong>{' '}
            · {summary.answeredCount}문제 제출 · {summary.correctCount}문제 정답
          </p>
        </section>
      )}
    </main>
  );
}

interface QuestionCardProps {
  question: QuizQuestion;
  total: number;
  result: SubmitResult | null;
  onSubmit: (question: QuizQuestion, selectedChoice: number) => void;
}

function QuestionCard({ question, total, result, onSubmit }: QuestionCardProps) {
  const [selected, setSelected] = useState<number | null>(null);
  const choices = [question.choice1, question.choice2, question.choice3, question.choice4];
  const applied = question.questionType === 'APPLIED';
  const answered = result != null;

  return (
    <article>
      <p>
        [{applied ? '응용' : '기본'}] {question.questionNo} / {total}
      </p>
      <p>
        <strong>{question.questionText}</strong>
      </p>
      <ul>
        {choices.map((c, i) => {
          const no = i + 1;
          const mark = answered
            ? no === result.answerNo
              ? ' ✅ (정답)'
              : no === selected
                ? ' ❌ (내 선택)'
                : ''
            : '';
          return (
            <li key={no}>
              <label>
                <input
                  type="radio"
                  name={`q-${question.questionNo}`}
                  value={no}
                  checked={selected === no}
                  disabled={answered}
                  onChange={() => setSelected(no)}
                />{' '}
                {no}. {c}
                {mark}
              </label>
            </li>
          );
        })}
      </ul>
      {!answered && (
        <button
          type="button"
          onClick={() => selected != null && onSubmit(question, selected)}
          disabled={selected == null}
        >
          제출
        </button>
      )}
      {answered && (
        <p>
          {result.correct ? '⭕ 정답이에요!' : `❌ 오답이에요. 정답은 ${result.answerNo}번입니다.`}{' '}
          {result.explanation}
        </p>
      )}
    </article>
  );
}
