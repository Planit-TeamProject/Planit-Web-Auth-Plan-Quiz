import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useCurrentUser } from '../auth/useCurrentUser';
import { useLang } from '../i18n/lang';
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

type LoadError = { key: string; params?: Record<string, string | number> } | null;

export default function QuizPage() {
  const { t } = useLang();
  const { user, loading } = useCurrentUser();

  const [dayMeta, setDayMeta] = useState<{ date: string; minutes: number } | null>(null);
  const [dayItems, setDayItems] = useState<DayItem[]>([]);
  const [todayScope, setTodayScope] = useState('');
  const [loadError, setLoadError] = useState<LoadError>(null);

  const [startBusy, setStartBusy] = useState(false);
  const [quiz, setQuiz] = useState<Quiz | null>(null);
  const [results, setResults] = useState<Record<number, SubmitResult>>({});
  const [error, setError] = useState('');
  const [summary, setSummary] = useState<QuizSummary | null>(null);

  useEffect(() => {
    loadDayOne();
  }, []);

  useEffect(() => {
    if (quiz && Object.keys(results).length === quiz.questions.length) {
      getQuizSummary(quiz.id)
        .then(setSummary)
        .catch((e) => setError(e instanceof Error ? e.message : String(e)));
    }
  }, [quiz, results]);

  async function loadDayOne() {
    setLoadError(null);
    try {
      const res = await fetch('/study_plan.json');
      if (!res.ok) {
        setLoadError({ key: 'quiz.err.loadFail', params: { status: res.status } });
        setTodayScope('');
        return;
      }
      const plan = await res.json();
      const day1 = plan.days?.[0];
      if (!day1 || !Array.isArray(day1.items) || day1.items.length === 0) {
        setTodayScope('');
        setDayItems([]);
        setLoadError({ key: 'quiz.err.noItems' });
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
      setLoadError({ key: 'quiz.err.loadFail', params: { status: e instanceof Error ? e.message : String(e) } });
    }
  }

  async function handleStart() {
    if (!user || !todayScope) {
      if (!todayScope) setError(t('quiz.err.noScope'));
      return;
    }
    setError('');
    setSummary(null);
    setResults({});
    setStartBusy(true);
    try {
      setQuiz(await startQuiz({ uid: user.uid, subjectName: 'quiz', todayScope }));
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
        <p>{t('common.loading')}</p>
      </main>
    );

  if (!user) {
    return (
      <main>
        <h1>{t('quiz.title')}</h1>
        <div className="card">
          <p className="msg-error">
            {t('quiz.needLogin')} <Link to="/login">{t('quiz.toLogin')}</Link>
          </p>
        </div>
      </main>
    );
  }

  return (
    <main className="wide">
      <h1>{t('quiz.title')}</h1>
      <p className="quiz-lead">{t('quiz.lead')}</p>

      <div className="card">
        <h2>{t('quiz.todayHeading')}</h2>
        {loadError && <p className="msg-error">{t(loadError.key, loadError.params)}</p>}
        {dayMeta && (
          <p className="sub">
            {t('quiz.dayMeta', { date: dayMeta.date, minutes: dayMeta.minutes })}
          </p>
        )}
        <ul className="dayone">
          {dayItems.map((it, i) => (
            <li key={i}>
              <span>{it.title}</span>
              <span className="meta">
                {it.pageRange ? `${it.pageRange} · ` : ''}
                {it.status}
              </span>
            </li>
          ))}
        </ul>
        <div className="btn-row">
          <button type="button" className="btn btn-ghost" onClick={loadDayOne}>
            {t('quiz.reload')}
          </button>
          <button
            type="button"
            className="btn btn-gold"
            onClick={handleStart}
            disabled={startBusy || !todayScope}
          >
            {startBusy ? t('quiz.starting') : t('quiz.start')}
          </button>
        </div>
        {error && <p className="msg-error">{error}</p>}
      </div>

      {quiz &&
        quiz.questions.map((q) => (
          <QuestionCard
            key={q.questionNo}
            question={q}
            total={quiz.questions.length}
            result={results[q.questionNo] ?? null}
            onSubmit={handleSubmit}
          />
        ))}

      {summary && (
        <div className="quiz-score">
          <div className="n">
            {summary.correctCount} / {summary.totalQuestionCount}
          </div>
          <div className="l">
            {t('quiz.summary', {
              answered: summary.answeredCount,
              correct: summary.correctCount,
            })}
          </div>
        </div>
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
  const { t } = useLang();
  const [selected, setSelected] = useState<number | null>(null);
  const choices = [question.choice1, question.choice2, question.choice3, question.choice4];
  const applied = question.questionType === 'APPLIED';
  const answered = result != null;

  return (
    <div className="card">
      <div>
        <span className={`q-badge ${applied ? 'applied' : 'basic'}`}>
          {applied ? t('quiz.badge.applied') : t('quiz.badge.basic')}
        </span>
        <span className="q-no">
          {t('quiz.qNo', { no: question.questionNo, total })}
        </span>
      </div>
      <p className="q-text">{question.questionText}</p>
      <ul className="q-choices">
        {choices.map((c, i) => {
          const no = i + 1;
          const mark = answered
            ? no === result.answerNo
              ? ' ✅'
              : no === selected
                ? ' ❌'
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
                />
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
          className="btn btn-primary"
          onClick={() => selected != null && onSubmit(question, selected)}
          disabled={selected == null}
        >
          {t('quiz.submit')}
        </button>
      )}
      {answered && (
        <p className={`q-result ${result.correct ? 'ok' : 'no'}`}>
          <strong>
            {result.correct ? t('quiz.correct') : t('quiz.wrong', { no: result.answerNo })}
          </strong>
          <br />
          {result.explanation}
        </p>
      )}
    </div>
  );
}
