// 학습계획입력 데이터 — Firestore + Firebase Storage.
// (백엔드 StudyPlanService + StudyPlanController 를 클라이언트로 옮긴 것)
//
// 컬렉션 구조:
//   studyPlans/{planId}  { uid, subjectName, tocFileUrl, startDate: 'YYYY-MM-DD', endDate,
//                          dailyAvailableMinutes, timeSlots: TimeSlotType[], createdAt, updatedAt }
//   목차 파일: Storage  toc/{uid}/{timestamp}_{파일명}
//
// REQ-B-002 / REQ-NF-017: 목차 파일은 PDF/JPG/PNG, 최대 10MB
// REQ-B-003: 과목명과 목차 파일이 모두 있어야 저장
// REQ-B-005: 종료일(시험일)은 시작일보다 빠를 수 없다
// REQ-B-006: 선호 학습 시간대 복수 선택
// REQ-NF-019: 본인이 등록한 학습 계획만 조회

import {
  addDoc,
  collection,
  getDoc,
  getDocs,
  orderBy,
  query,
  serverTimestamp,
  where,
  doc,
} from 'firebase/firestore';
import { getDownloadURL, ref as storageRef, uploadBytes } from 'firebase/storage';
import { db, storage } from '../firebase';

export const TIME_SLOTS = [
  { value: 'EARLY_MORNING', label: '아침(6~9시)' },
  { value: 'MORNING', label: '오전(9~12시)' },
  { value: 'AFTERNOON', label: '오후(1~6시)' },
  { value: 'EVENING', label: '저녁(6~10시)' },
  { value: 'LATE_NIGHT', label: '심야(10~12시)' },
  { value: 'WEEKEND_MORNING', label: '주말 오전' },
  { value: 'WEEKEND_AFTERNOON', label: '주말 오후' },
  { value: 'WEEKDAY_ONLY', label: '평일만' },
] as const;

export type TimeSlotType = (typeof TIME_SLOTS)[number]['value'];

export interface StudyPlan {
  id: string;
  subjectName: string;
  tocFileUrl: string;
  startDate: string;
  endDate: string;
  dailyAvailableMinutes: number | null;
  timeSlots: TimeSlotType[];
}

interface CreateStudyPlanParams {
  uid: string;
  subjectName: string;
  tocFileUrl: string;
  startDate: string;
  endDate: string;
  dailyAvailableMinutes?: number | null;
  timeSlots?: TimeSlotType[];
}

const MAX_TOC_BYTES = 10 * 1024 * 1024;
const ALLOWED_TOC_EXT = ['pdf', 'jpg', 'jpeg', 'png'];

/** REQ-B-002: 목차 파일을 Storage 에 올리고 다운로드 URL 을 돌려준다. */
export async function uploadTocFile(uid: string, file: File): Promise<string> {
  const ext = file.name.split('.').pop()?.toLowerCase() ?? '';
  if (!ALLOWED_TOC_EXT.includes(ext)) {
    throw new Error('목차 파일은 PDF, JPG, PNG 파일만 업로드할 수 있습니다.');
  }
  if (file.size > MAX_TOC_BYTES) {
    throw new Error('목차 파일은 최대 10MB까지 업로드할 수 있습니다.');
  }

  const path = `toc/${uid}/${Date.now()}_${file.name}`;
  const snap = await uploadBytes(storageRef(storage, path), file);
  return getDownloadURL(snap.ref);
}

/** REQ-B-001~009: 학습 계획을 저장한다. */
export async function createStudyPlan({
  uid,
  subjectName,
  tocFileUrl,
  startDate,
  endDate,
  dailyAvailableMinutes = null,
  timeSlots = [],
}: CreateStudyPlanParams): Promise<StudyPlan> {
  if (!subjectName.trim() || !tocFileUrl.trim()) {
    throw new Error('과목명과 목차 파일을 모두 입력해 주세요.');
  }
  if (endDate < startDate) {
    throw new Error('종료일(시험일)은 시작일보다 빠를 수 없습니다.');
  }

  const ref = await addDoc(collection(db, 'studyPlans'), {
    uid,
    subjectName: subjectName.trim(),
    tocFileUrl,
    startDate,
    endDate,
    dailyAvailableMinutes,
    timeSlots,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  });

  return {
    id: ref.id,
    subjectName: subjectName.trim(),
    tocFileUrl,
    startDate,
    endDate,
    dailyAvailableMinutes,
    timeSlots,
  };
}

function toStudyPlan(id: string, data: Record<string, unknown>): StudyPlan {
  return {
    id,
    subjectName: (data.subjectName as string) ?? '',
    tocFileUrl: (data.tocFileUrl as string) ?? '',
    startDate: (data.startDate as string) ?? '',
    endDate: (data.endDate as string) ?? '',
    dailyAvailableMinutes: (data.dailyAvailableMinutes as number | null) ?? null,
    timeSlots: (data.timeSlots as TimeSlotType[]) ?? [],
  };
}

/** 내 학습 계획 목록 (최신순). */
export async function getMyStudyPlans(uid: string): Promise<StudyPlan[]> {
  const q = query(
    collection(db, 'studyPlans'),
    where('uid', '==', uid),
    orderBy('createdAt', 'desc'),
  );
  const snap = await getDocs(q);
  return snap.docs.map((d) => toStudyPlan(d.id, d.data()));
}

/** REQ-NF-019: 본인 학습 계획만 조회할 수 있다. */
export async function getStudyPlan(uid: string, planId: string): Promise<StudyPlan> {
  const snap = await getDoc(doc(db, 'studyPlans', planId));
  if (!snap.exists()) throw new Error('학습 계획을 찾을 수 없습니다.');
  if (snap.data().uid !== uid) throw new Error('본인이 등록한 학습 계획만 조회할 수 있습니다.');
  return toStudyPlan(snap.id, snap.data());
}
