import { ChangeEvent, useState } from 'react';
import { Link } from 'react-router-dom';
import { useCurrentUser } from '../auth/useCurrentUser';
import {
  createStudyPlan,
  uploadTocFile,
  TIME_SLOTS,
  type StudyPlan,
  type TimeSlotType,
} from '../api/studyPlan';

// 화면 4. 플랜 생성 마법사 (김동호 담당 - 학습계획입력). 저장소는 Firestore + Storage.
// STEP1 과목/목차 → STEP2 기간/시간대 → STEP3 확인·저장 (REQ-B-001~009).
// REQ-B-003: 과목명 + 목차 파일 필수 / REQ-B-005: 종료일 ≥ 시작일 / REQ-B-006: 시간대 복수 선택.

export default function StudyPlanPage() {
  const { user, loading } = useCurrentUser();

  const [step, setStep] = useState<1 | 2 | 3>(1);

  const [subjectName, setSubjectName] = useState('');
  const [tocFileUrl, setTocFileUrl] = useState('');
  const [tocFileName, setTocFileName] = useState('');
  const [uploading, setUploading] = useState(false);

  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [dailyMinutes, setDailyMinutes] = useState('');
  const [timeSlots, setTimeSlots] = useState<TimeSlotType[]>([]);

  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState<StudyPlan | null>(null);

  async function handleFile(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file || !user) return;
    setError('');
    setUploading(true);
    try {
      const url = await uploadTocFile(user.uid, file);
      setTocFileUrl(url);
      setTocFileName(file.name);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setUploading(false);
    }
  }

  function toggleSlot(value: TimeSlotType) {
    setTimeSlots((prev) =>
      prev.includes(value) ? prev.filter((v) => v !== value) : [...prev, value],
    );
  }

  function goStep2() {
    setError('');
    if (!subjectName.trim()) return setError('과목 / 자격증명을 입력해 주세요.');
    if (!tocFileUrl) return setError('목차 파일을 업로드해 주세요.');
    setStep(2);
  }

  function goStep3() {
    setError('');
    if (!startDate || !endDate) return setError('시작일과 종료일을 입력해 주세요.');
    if (endDate < startDate) return setError('종료일(시험일)은 시작일보다 빠를 수 없습니다.');
    setStep(3);
  }

  async function handleSave() {
    if (!user) return;
    setError('');
    setSaving(true);
    try {
      const plan = await createStudyPlan({
        uid: user.uid,
        subjectName,
        tocFileUrl,
        startDate,
        endDate,
        dailyAvailableMinutes: dailyMinutes ? Number(dailyMinutes) : null,
        timeSlots,
      });
      setSaved(plan);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setSaving(false);
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
        <h1>플랜 생성</h1>
        <p role="alert">
          로그인이 필요합니다. <Link to="/login">로그인하러 가기</Link>
        </p>
      </main>
    );
  }

  if (saved) {
    return (
      <main>
        <h1>플랜 생성 완료</h1>
        <p role="status">학습 계획이 저장되었습니다. (id: {saved.id})</p>
        <ul>
          <li>과목: {saved.subjectName}</li>
          <li>
            기간: {saved.startDate} ~ {saved.endDate}
          </li>
          <li>하루 가용 시간: {saved.dailyAvailableMinutes ?? '-'}분</li>
          <li>
            선호 시간대:{' '}
            {saved.timeSlots.length
              ? saved.timeSlots
                  .map((v) => TIME_SLOTS.find((s) => s.value === v)?.label ?? v)
                  .join(', ')
              : '-'}
          </li>
        </ul>
      </main>
    );
  }

  return (
    <main>
      <h1>플랜 생성 (STEP {step} / 3)</h1>
      {error && <p role="alert">{error}</p>}

      {step === 1 && (
        <section>
          <h2>무엇을 공부하나요?</h2>
          <div>
            <label htmlFor="subject">과목 / 자격증명</label>
            <input
              id="subject"
              type="text"
              placeholder="예: 정보처리기사, 토익, 선형대수학"
              value={subjectName}
              onChange={(e) => setSubjectName(e.target.value)}
            />
          </div>
          <div>
            <label htmlFor="toc">목차 업로드 (PDF, JPG, PNG · 최대 10MB)</label>
            <input id="toc" type="file" accept=".pdf,.jpg,.jpeg,.png" onChange={handleFile} />
            {uploading && <p>업로드 중…</p>}
            {tocFileUrl && (
              <p role="status">
                업로드됨: {tocFileName} (<a href={tocFileUrl}>파일 보기</a>)
              </p>
            )}
          </div>
          <button type="button" onClick={goStep2}>
            다음
          </button>
        </section>
      )}

      {step === 2 && (
        <section>
          <h2>일정을 알려주세요</h2>
          <div>
            <label htmlFor="start">시작일</label>
            <input
              id="start"
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
            />
          </div>
          <div>
            <label htmlFor="end">종료일 (목표일)</label>
            <input
              id="end"
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
            />
          </div>
          <div>
            <label htmlFor="daily">하루 가용 학습 시간(분, 선택)</label>
            <input
              id="daily"
              type="number"
              min={0}
              value={dailyMinutes}
              onChange={(e) => setDailyMinutes(e.target.value)}
            />
          </div>
          <fieldset>
            <legend>선호 학습 시간대 (복수 선택)</legend>
            {TIME_SLOTS.map((s) => (
              <label key={s.value}>
                <input
                  type="checkbox"
                  checked={timeSlots.includes(s.value)}
                  onChange={() => toggleSlot(s.value)}
                />{' '}
                {s.label}
              </label>
            ))}
          </fieldset>
          <button type="button" onClick={() => setStep(1)}>
            이전
          </button>{' '}
          <button type="button" onClick={goStep3}>
            다음
          </button>
        </section>
      )}

      {step === 3 && (
        <section>
          <h2>입력한 내용을 확인하세요</h2>
          <ul>
            <li>과목: {subjectName}</li>
            <li>목차 파일: {tocFileName || '(없음)'}</li>
            <li>
              기간: {startDate} ~ {endDate}
            </li>
            <li>하루 가용 시간: {dailyMinutes || '-'}분</li>
            <li>
              선호 시간대:{' '}
              {timeSlots.length
                ? timeSlots.map((v) => TIME_SLOTS.find((s) => s.value === v)?.label).join(', ')
                : '-'}
            </li>
          </ul>
          <button type="button" onClick={() => setStep(2)}>
            이전
          </button>{' '}
          <button type="button" onClick={handleSave} disabled={saving}>
            {saving ? '저장 중…' : '학습 계획 저장'}
          </button>
        </section>
      )}
    </main>
  );
}
