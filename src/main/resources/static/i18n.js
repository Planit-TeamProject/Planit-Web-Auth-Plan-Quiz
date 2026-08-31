/* 아주 가벼운 i18n. data-i18n / data-i18n-ph 속성으로 정적 문구를 바꾸고,
   동적 문구는 t('key', {params}) 로 만든다. 언어 선택은 localStorage 에 저장. */
(function () {
  var I18N = {
    ko: {
      'nav.login': '로그인', 'nav.quiz': '퀴즈봇', 'nav.logout': '로그아웃',
      'login.title': '로그인',
      'login.sub': '이어서 계획을 확인하려면 로그인하세요.',
      'login.field.email': '이메일', 'login.field.password': '비밀번호',
      'login.remember': '아이디 저장하기',
      'login.submit': '로그인', 'login.submitting': '로그인 중…',
      'login.google': '구글로 로그인하기', 'login.googleBusy': '구글 로그인 중…',
      'login.or': '또는',
      'login.noAccount': '계정이 없으신가요?', 'login.toSignup': '회원가입',
      'login.err.empty': '이메일과 비밀번호를 입력해 주세요.',
      'signup.title': '회원가입',
      'signup.sub': '1분이면 충분해요. 가입하면 로그인 화면으로 이동합니다.',
      'signup.field.name': '이름', 'signup.ph.name': '홍길동',
      'signup.field.passwordConfirm': '비밀번호 확인', 'signup.ph.passwordConfirm': '다시 입력',
      'signup.submit': '회원가입 완료', 'signup.submitting': '처리 중…',
      'signup.haveAccount': '이미 계정이 있으신가요?', 'signup.toLogin': '로그인',
      'signup.notice': '가입 완료! 로그인해 주세요.',
      'signup.err.name': '이름을 입력해 주세요.',
      'signup.err.passwordShort': '비밀번호는 8자 이상이어야 합니다.',
      'signup.err.passwordMismatch': '비밀번호가 일치하지 않습니다.',
      'auth.welcome': '로그인 성공! 환영합니다, {name}님',
      'auth.err.invalidEmail': '이메일 형식이 올바르지 않습니다.',
      'auth.err.emailInUse': '이미 사용 중인 이메일입니다.',
      'auth.err.weakPassword': '비밀번호는 6자 이상이어야 합니다.',
      'auth.err.badCredential': '이메일 또는 비밀번호가 올바르지 않습니다.',
      'auth.err.tooManyRequests': '로그인 시도가 너무 많습니다. 잠시 후 다시 시도해 주세요.',
      'auth.err.network': '네트워크 오류가 발생했습니다. 연결 상태를 확인해 주세요.',
      'auth.err.popupBlocked': '브라우저가 팝업을 차단했습니다. 팝업을 허용한 뒤 다시 시도해 주세요.',
      'auth.err.server': '서버 인증에 실패했습니다. 잠시 후 다시 시도해 주세요.',
      'auth.err.config': 'Firebase 설정이 채워지지 않았습니다. login.html 의 firebaseConfig 를 확인하세요.',
      'quiz.title': '오늘의 퀴즈',
      'quiz.lead': 'study_plan.json 의 1일차 학습 범위로 4지선다 3문제(쉬운 문제 2 + 응용 1)를 만듭니다.',
      'quiz.needLogin': '로그인이 필요합니다.', 'quiz.toLogin': '로그인하러 가기',
      'quiz.todayHeading': '오늘의 일과 (study_plan.json 1일차)',
      'quiz.dayMeta': '1일차 · {date} · {minutes}분',
      'quiz.reload': '다시 불러오기', 'quiz.start': '퀴즈 시작', 'quiz.starting': '문제 만드는 중…',
      'quiz.badge.basic': '기본', 'quiz.badge.applied': '응용',
      'quiz.qNo': '{no} / {total}', 'quiz.submit': '제출',
      'quiz.correct': '⭕ 정답이에요!', 'quiz.wrong': '❌ 오답이에요. 정답은 {no}번입니다.',
      'quiz.summary': '{answered}문제 제출 · {correct}문제 정답',
      'quiz.err.load': '오늘의 일과를 불러오지 못했습니다.'
    },
    ja: {
      'nav.login': 'ログイン', 'nav.quiz': 'クイズ', 'nav.logout': 'ログアウト',
      'login.title': 'ログイン',
      'login.sub': 'ログインすると、前回の続きから計画を確認できます。',
      'login.field.email': 'メールアドレス', 'login.field.password': 'パスワード',
      'login.remember': 'メールアドレスを保存する',
      'login.submit': 'ログイン', 'login.submitting': 'ログイン中…',
      'login.google': 'Googleでログイン', 'login.googleBusy': 'Googleでログイン中…',
      'login.or': 'または',
      'login.noAccount': 'アカウントをお持ちでない方は', 'login.toSignup': '新規登録',
      'login.err.empty': 'メールアドレスとパスワードを入力してください。',
      'signup.title': '新規登録',
      'signup.sub': '1分ほどで完了します。登録後、ログイン画面に移動します。',
      'signup.field.name': '名前', 'signup.ph.name': '山田 太郎',
      'signup.field.passwordConfirm': 'パスワード（確認）', 'signup.ph.passwordConfirm': 'もう一度入力',
      'signup.submit': '登録する', 'signup.submitting': '処理中…',
      'signup.haveAccount': 'すでにアカウントをお持ちの方は', 'signup.toLogin': 'ログイン',
      'signup.notice': '登録が完了しました。ログインしてください。',
      'signup.err.name': '名前を入力してください。',
      'signup.err.passwordShort': 'パスワードは8文字以上で入力してください。',
      'signup.err.passwordMismatch': 'パスワードが一致しません。',
      'auth.welcome': 'ログインしました。ようこそ、{name}さん！',
      'auth.err.invalidEmail': 'メールアドレスの形式が正しくありません。',
      'auth.err.emailInUse': 'このメールアドレスは既に使われています。',
      'auth.err.weakPassword': 'パスワードは6文字以上にしてください。',
      'auth.err.badCredential': 'メールアドレスまたはパスワードが正しくありません。',
      'auth.err.tooManyRequests': 'ログインの試行回数が多すぎます。しばらく待ってからお試しください。',
      'auth.err.network': '通信エラーが発生しました。接続状況を確認してください。',
      'auth.err.popupBlocked': 'ポップアップがブロックされました。許可してからもう一度お試しください。',
      'auth.err.server': 'サーバー認証に失敗しました。しばらくしてからお試しください。',
      'auth.err.config': 'Firebaseの設定が未入力です。login.html の firebaseConfig を確認してください。',
      'quiz.title': '今日のクイズ',
      'quiz.lead': 'study_plan.json の1日目の学習範囲から、4択問題を3問（基本2問・応用1問）作成します。',
      'quiz.needLogin': 'ログインが必要です。', 'quiz.toLogin': 'ログインする',
      'quiz.todayHeading': '今日の予定（study_plan.json 1日目）',
      'quiz.dayMeta': '1日目 · {date} · {minutes}分',
      'quiz.reload': '再読み込み', 'quiz.start': 'クイズ開始', 'quiz.starting': '問題を作成中…',
      'quiz.badge.basic': '基本', 'quiz.badge.applied': '応用',
      'quiz.qNo': '{no} / {total}', 'quiz.submit': '回答する',
      'quiz.correct': '⭕ 正解です！', 'quiz.wrong': '❌ 不正解です。正解は{no}番です。',
      'quiz.summary': '{answered}問回答・{correct}問正解',
      'quiz.err.load': '今日の予定を読み込めませんでした。'
    },
    en: {
      'nav.login': 'Log in', 'nav.quiz': 'Quiz', 'nav.logout': 'Log out',
      'login.title': 'Log in',
      'login.sub': 'Log in to pick up where you left off.',
      'login.field.email': 'Email', 'login.field.password': 'Password',
      'login.remember': 'Remember my email',
      'login.submit': 'Log in', 'login.submitting': 'Logging in…',
      'login.google': 'Continue with Google', 'login.googleBusy': 'Signing in with Google…',
      'login.or': 'or',
      'login.noAccount': "Don't have an account?", 'login.toSignup': 'Sign up',
      'login.err.empty': 'Enter your email and password.',
      'signup.title': 'Sign up',
      'signup.sub': "Only takes a minute. You'll go to the login screen once you're done.",
      'signup.field.name': 'Name', 'signup.ph.name': 'Jane Doe',
      'signup.field.passwordConfirm': 'Confirm password', 'signup.ph.passwordConfirm': 'Re-enter it',
      'signup.submit': 'Create account', 'signup.submitting': 'Working…',
      'signup.haveAccount': 'Already have an account?', 'signup.toLogin': 'Log in',
      'signup.notice': 'Account created — go ahead and log in.',
      'signup.err.name': 'Enter your name.',
      'signup.err.passwordShort': 'Password must be at least 8 characters.',
      'signup.err.passwordMismatch': "Passwords don't match.",
      'auth.welcome': "You're in — welcome, {name}!",
      'auth.err.invalidEmail': "That email address doesn't look right.",
      'auth.err.emailInUse': 'That email is already registered.',
      'auth.err.weakPassword': 'Password must be at least 6 characters.',
      'auth.err.badCredential': 'Email or password is incorrect.',
      'auth.err.tooManyRequests': 'Too many attempts. Try again in a bit.',
      'auth.err.network': 'Network error. Check your connection.',
      'auth.err.popupBlocked': 'Your browser blocked the popup. Allow it and try again.',
      'auth.err.server': 'Server sign-in failed. Try again in a moment.',
      'auth.err.config': 'Firebase config is empty. Check firebaseConfig in login.html.',
      'quiz.title': "Today's quiz",
      'quiz.lead': 'Makes 3 multiple-choice questions (2 basic, 1 applied) from day 1 of study_plan.json.',
      'quiz.needLogin': 'You need to be logged in.', 'quiz.toLogin': 'Go to login',
      'quiz.todayHeading': "Today's plan (study_plan.json, day 1)",
      'quiz.dayMeta': 'Day 1 · {date} · {minutes} min',
      'quiz.reload': 'Reload', 'quiz.start': 'Start quiz', 'quiz.starting': 'Building questions…',
      'quiz.badge.basic': 'Basic', 'quiz.badge.applied': 'Applied',
      'quiz.qNo': '{no} / {total}', 'quiz.submit': 'Submit',
      'quiz.correct': '⭕ Correct!', 'quiz.wrong': '❌ Not quite — the answer is #{no}.',
      'quiz.summary': '{answered} answered · {correct} correct',
      'quiz.err.load': "Couldn't load today's plan."
    }
  };

  var LABELS = { ko: '한국어', ja: '日本語', en: 'English' };
  var LANG = 'ko';
  try {
    var saved = localStorage.getItem('planit.lang');
    if (saved && I18N[saved]) LANG = saved;
    else {
      var nav = (navigator.language || 'ko').slice(0, 2);
      if (I18N[nav]) LANG = nav;
    }
  } catch (e) {}

  window.t = function (key, params) {
    var s = (I18N[LANG] && I18N[LANG][key]) || I18N.ko[key] || key;
    if (params) for (var k in params) s = s.replace('{' + k + '}', params[k]);
    return s;
  };

  window.applyI18n = function (root) {
    root = root || document;
    root.querySelectorAll('[data-i18n]').forEach(function (el) {
      el.textContent = window.t(el.getAttribute('data-i18n'));
    });
    root.querySelectorAll('[data-i18n-ph]').forEach(function (el) {
      el.setAttribute('placeholder', window.t(el.getAttribute('data-i18n-ph')));
    });
    document.documentElement.lang = LANG;
  };

  window.setLang = function (l) {
    if (!I18N[l]) return;
    LANG = l;
    try { localStorage.setItem('planit.lang', l); } catch (e) {}
    window.applyI18n();
    document.dispatchEvent(new Event('langchange'));
    renderSwitch();
  };

  function renderSwitch() {
    var box = document.querySelector('[data-langswitch]');
    if (!box) return;
    box.innerHTML = ['ko', 'ja', 'en'].map(function (l) {
      return '<button type="button" data-lang="' + l + '"' + (l === LANG ? ' class="active"' : '') + '>' + LABELS[l] + '</button>';
    }).join('');
    box.querySelectorAll('button').forEach(function (b) {
      b.onclick = function () { window.setLang(b.getAttribute('data-lang')); };
    });
  }

  document.addEventListener('DOMContentLoaded', function () {
    window.applyI18n();
    renderSwitch();
  });
})();
