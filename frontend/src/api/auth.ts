// 인증 — Firebase Authentication 기반.
// 안드로이드 앱과 같은 Firebase 프로젝트를 쓰므로, 여기서 가입/로그인한 계정은
// 앱에서도 그대로 쓰이고 Firestore `users` 컬렉션도 공유된다.
//
// (기존 Spring `/api/auth/*` 호출은 팀 방침 변경으로 Firebase 로 대체됨.)

import { FirebaseError } from 'firebase/app';
import {
  GoogleAuthProvider,
  createUserWithEmailAndPassword,
  getAdditionalUserInfo,
  getRedirectResult,
  signInWithEmailAndPassword,
  signInWithPopup,
  signInWithRedirect,
  signOut,
  updateProfile,
  type User,
  type UserCredential,
} from 'firebase/auth';
import { doc, serverTimestamp, setDoc } from 'firebase/firestore';
import { auth, db, missingFirebaseConfigKeys } from '../firebase';

/**
 * Firebase config(.env.local)가 채워져 있지 않으면 여기서 먼저 막아
 * 화면에 명확한 안내가 뜨게 한다 (설정 누락 시 signInWithPopup 등은 조용히 실패함).
 */
function assertFirebaseConfigured(): void {
  if (missingFirebaseConfigKeys.length > 0) {
    throw new Error(
      'Firebase 설정이 없습니다. frontend/.env.local 을 만들어 Firebase 콘솔의 웹 앱 config 값을 ' +
        '채운 뒤 dev 서버를 재시작하세요. (누락: ' +
        missingFirebaseConfigKeys.join(', ') +
        ')',
    );
  }
}

export interface AuthUser {
  uid: string;
  email: string | null;
  name: string | null;
  emailVerified: boolean;
}

export interface SignUpParams {
  name: string;
  email: string;
  password: string;
}

export interface SignInParams {
  email: string;
  password: string;
}

function toAuthUser(user: User): AuthUser {
  return {
    uid: user.uid,
    email: user.email,
    name: user.displayName,
    emailVerified: user.emailVerified,
  };
}

/**
 * Firestore `users/{uid}` 문서를 만들거나 갱신한다.
 * `users` 컬렉션은 안드로이드 앱과 공유하므로 필드명을 앱과 맞춰야 한다(현재: name, email).
 * createdAt 은 신규 가입일 때만 기록한다.
 */
async function upsertUserDoc(user: User, isNew: boolean): Promise<void> {
  try {
    await setDoc(
      doc(db, 'users', user.uid),
      {
        name: user.displayName,
        email: user.email,
        ...(isNew ? { createdAt: serverTimestamp() } : {}),
      },
      { merge: true },
    );
  } catch (e) {
    // Firestore 보안 규칙이 아직 안 잡혀 있으면 여기서 막힐 수 있다.
    // 계정 자체는 이미 만들어졌으므로 실패로 처리하지 않고 경고만 남긴다.
    console.warn('[auth] users 문서 저장 실패:', e);
  }
}

/** Firebase 오류 코드를 화면에 그대로 보여줄 한국어 문구로 바꾼다 (REQ-NF-015). */
export function authErrorMessage(err: unknown): string {
  if (err instanceof FirebaseError) {
    switch (err.code) {
      case 'auth/invalid-email':
        return '이메일 형식이 올바르지 않습니다.';
      case 'auth/email-already-in-use':
        return '이미 사용 중인 이메일입니다.';
      case 'auth/weak-password':
        return '비밀번호는 6자 이상이어야 합니다.';
      case 'auth/user-not-found':
      case 'auth/wrong-password':
      case 'auth/invalid-credential':
        return '이메일 또는 비밀번호가 올바르지 않습니다.';
      case 'auth/user-disabled':
        return '비활성화된 계정입니다. 관리자에게 문의해 주세요.';
      case 'auth/too-many-requests':
        return '로그인 시도가 너무 많습니다. 잠시 후 다시 시도해 주세요.';
      case 'auth/network-request-failed':
        return '네트워크 오류가 발생했습니다. 연결 상태를 확인해 주세요.';
      case 'auth/popup-closed-by-user':
      case 'auth/cancelled-popup-request':
        return ''; // 사용자가 구글 로그인 팝업을 닫은 것 — 오류로 표시하지 않는다
      case 'auth/popup-blocked':
        return '브라우저가 팝업을 차단했습니다. 팝업을 허용한 뒤 다시 시도해 주세요.';
      case 'auth/account-exists-with-different-credential':
        return '이미 다른 방법으로 가입된 이메일입니다. 기존 방식으로 로그인해 주세요.';
      default:
        return err.message;
    }
  }
  return err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.';
}

/**
 * 회원가입: Firebase Auth 계정 생성 + 표시 이름 저장 + Firestore `users/{uid}` 문서 생성.
 */
export async function signUp({ name, email, password }: SignUpParams): Promise<AuthUser> {
  assertFirebaseConfigured();
  const cred = await createUserWithEmailAndPassword(auth, email, password);
  await updateProfile(cred.user, { displayName: name });
  await upsertUserDoc(cred.user, true);
  return toAuthUser(cred.user);
}

/** 이메일/비밀번호 로그인 */
export async function signIn({ email, password }: SignInParams): Promise<AuthUser> {
  assertFirebaseConfigured();
  const cred = await signInWithEmailAndPassword(auth, email, password);
  return toAuthUser(cred.user);
}

async function finishGoogleSignIn(result: UserCredential): Promise<AuthUser> {
  const isNew = getAdditionalUserInfo(result)?.isNewUser ?? false;
  await upsertUserDoc(result.user, isNew);
  return toAuthUser(result.user);
}

// 팝업이 뜨지 않거나 브라우저가 막는 상황 → 전체 페이지 리다이렉트 방식으로 자동 전환한다.
const POPUP_FALLBACK_CODES = new Set([
  'auth/popup-blocked',
  'auth/cancelled-popup-request',
  'auth/popup-closed-by-user',
  'auth/operation-not-supported-in-this-environment',
]);

/**
 * 구글 계정으로 로그인/회원가입.
 * 1) 먼저 팝업(signInWithPopup)을 시도한다.
 * 2) 팝업이 막히거나 뜨지 않으면 리다이렉트(signInWithRedirect)로 전환한다.
 *    → 이 경우 페이지가 구글로 이동했다가 되돌아오며, 돌아온 뒤 consumeRedirectResult() 가 결과를 받는다.
 * 처음 보는 계정이면 그대로 가입 처리되고, Firestore `users/{uid}` 문서도 함께 만든다.
 * 콘솔에서 Authentication > 로그인 방법 > Google 공급업체를 사용 설정해야 동작한다.
 */
export async function signInWithGoogle(): Promise<AuthUser> {
  assertFirebaseConfigured();
  const provider = new GoogleAuthProvider();
  try {
    const result = await signInWithPopup(auth, provider);
    return await finishGoogleSignIn(result);
  } catch (err) {
    if (err instanceof FirebaseError && POPUP_FALLBACK_CODES.has(err.code)) {
      await signInWithRedirect(auth, provider);
      // 리다이렉트가 시작되면 아래로 내려오지 않는다. 타입을 맞추기 위한 대기.
      return new Promise<AuthUser>(() => {});
    }
    throw err;
  }
}

/**
 * 구글 리다이렉트 로그인에서 되돌아왔을 때 결과를 받는다.
 * 로그인/회원가입 화면이 처음 뜰 때 한 번 호출한다. 결과가 없으면 null.
 */
export async function consumeRedirectResult(): Promise<AuthUser | null> {
  if (missingFirebaseConfigKeys.length > 0) return null;
  const result = await getRedirectResult(auth);
  if (!result) return null;
  return finishGoogleSignIn(result);
}

/** 로그아웃 */
export function signOutUser(): Promise<void> {
  return signOut(auth);
}
