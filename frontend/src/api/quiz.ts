// 퀴즈봇 데이터 — Firestore.  (백엔드 QuizService + QuizController 를 클라이언트로 옮긴 것)
//
// 컬렉션 구조:
//   quizzes/{quizId}                     { uid, studyPlanId, subjectName, todayScope,
//                                          quizDate: 'YYYY-MM-DD', createdAt,
//                                          questions: [{ questionNo, questionType, questionText,
//                                                        choice1..4, answerNo, explanation }] }
//   quizzes/{quizId}/answers/{questionNo} { selectedChoice, correct, answeredAt }
//
// REQ-Q-001~003: 오늘 학습 범위로 BASIC 2 + APPLIED 1 문제 생성·저장
// REQ-Q-004~005: 문제별 제출 → 그 자리에서 채점 + 정답/풀이
// REQ-Q-006    : 모두 제출하면 맞힌 개수 요약

import {
  addDoc,
  collection,
  doc,
  getDoc,
  getDocs,
  serverTimestamp,
  setDoc,
} from 'firebase/firestore';
import { db } from '../firebase';
import { generateQuestions, type GeneratedQuestion, type QuestionType } from './quizQuestions';

export interface QuizQuestion extends GeneratedQuestion {
  questionNo: number;
}

export interface Quiz {
  id: string;
  subjectName: string;
  todayScope: string;
  quizDate: string;
  questions: QuizQuestion[];
}

export interface SubmitResult {
  questionNo: number;
  correct: boolean;
  answerNo: number;
  explanation: string;
}

export interface QuizSummary {
  quizId: string;
  totalQuestionCount: number;
  answeredCount: number;
  correctCount: number;
}

interface StartQuizParams {
  uid: string;
  subjectName: string;
  todayScope: string;
  studyPlanId?: string | null;
}

function todayString(): string {
  return new Date().toISOString().slice(0, 10);
}

/** REQ-Q-001~003: 오늘 학습 범위로 퀴즈 1세트를 만들어 Firestore 에 저장한다. */
export async function startQuiz({
  uid,
  subjectName,
  todayScope,
  studyPlanId = null,
}: StartQuizParams): Promise<Quiz> {
  const generated = generateQuestions(subjectName, todayScope);
  const questions: QuizQuestion[] = generated.map((q, i) => ({ ...q, questionNo: i + 1 }));
  const quizDate = todayString();

  const ref = await addDoc(collection(db, 'quizzes'), {
    uid,
    studyPlanId,
    subjectName,
    todayScope,
    quizDate,
    createdAt: serverTimestamp(),
    questions,
  });

  return { id: ref.id, subjectName, todayScope, quizDate, questions };
}

/** REQ-Q-004~005: 문제 하나를 제출하고 채점 결과와 풀이를 돌려준다. 문제당 1회만 저장된다. */
export async function submitAnswer(
  quizId: string,
  question: QuizQuestion,
  selectedChoice: number,
): Promise<SubmitResult> {
  if (selectedChoice < 1 || selectedChoice > 4) {
    throw new Error('보기 번호는 1~4 중 하나여야 합니다.');
  }
  const correct = selectedChoice === question.answerNo;

  await setDoc(doc(db, 'quizzes', quizId, 'answers', String(question.questionNo)), {
    selectedChoice,
    correct,
    answeredAt: serverTimestamp(),
  });

  return {
    questionNo: question.questionNo,
    correct,
    answerNo: question.answerNo,
    explanation: question.explanation,
  };
}

/** REQ-Q-006: 제출한 답안들을 모아 맞힌 개수를 요약한다. */
export async function getQuizSummary(quizId: string): Promise<QuizSummary> {
  const quizSnap = await getDoc(doc(db, 'quizzes', quizId));
  const total = (quizSnap.data()?.questions as unknown[] | undefined)?.length ?? 0;

  const answersSnap = await getDocs(collection(db, 'quizzes', quizId, 'answers'));
  let correctCount = 0;
  answersSnap.forEach((d) => {
    if (d.data().correct) correctCount += 1;
  });

  return {
    quizId,
    totalQuestionCount: total,
    answeredCount: answersSnap.size,
    correctCount,
  };
}

export type { QuestionType };
