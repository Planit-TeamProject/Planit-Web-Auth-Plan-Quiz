// Firebase 초기화.
// 안드로이드 앱이 쓰는 것과 "같은" Firebase 프로젝트에 웹 앱만 추가로 등록한 것이라,
// 웹에서 가입한 유저와 앱에서 가입한 유저가 같은 Authentication / Firestore 를 공유한다.
// (MySQL 대신 Firebase 로 전환 — member/quiz 데이터는 Firestore 에 저장한다.)
//
// 실제 config 값은 Firebase 콘솔 > 프로젝트 설정 > 내 앱 > 웹 앱 > "SDK 설정 및 구성"
// 에서 복사해 frontend/.env.local 에 채운다 (.env.local 은 커밋 금지, .env.example 참고).
// Vite 프로젝트이므로 CRA 의 process.env.REACT_APP_* 가 아니라 import.meta.env.VITE_* 를 쓴다.

import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
};

/**
 * .env.local 이 없거나 값이 비어 있으면 로그인/구글 로그인이 "아무 일도 안 일어난 것처럼" 실패한다.
 * 어떤 값이 비었는지 목록으로 남겨, auth 함수와 화면에서 명확한 안내 메시지를 띄우는 데 쓴다.
 */
export const missingFirebaseConfigKeys = Object.entries(firebaseConfig)
  .filter(([, value]) => !value)
  .map(([key]) => key);

if (missingFirebaseConfigKeys.length > 0) {
  console.error(
    `[firebase] 설정값이 비어 있습니다: ${missingFirebaseConfigKeys.join(', ')}\n` +
      'frontend/.env.local 을 만들고 Firebase 콘솔의 웹 앱 config 값을 채운 뒤 dev 서버를 재시작하세요. ' +
      '(frontend/.env.example 참고)',
  );
}

const app = initializeApp(firebaseConfig);

export const auth = getAuth(app);
export const db = getFirestore(app);
