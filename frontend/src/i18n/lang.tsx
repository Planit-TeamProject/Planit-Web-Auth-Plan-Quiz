import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { LANGS, translations, type Lang } from './translations';

type TFn = (key: string, params?: Record<string, string | number>) => string;

interface LangCtx {
  lang: Lang;
  setLang: (l: Lang) => void;
  t: TFn;
}

const Ctx = createContext<LangCtx | null>(null);

const STORAGE_KEY = 'planit.lang';

function loadLang(): Lang {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved === 'ko' || saved === 'ja' || saved === 'en') return saved;
  } catch {
    /* localStorage 접근 불가 환경 */
  }
  // 저장값이 없으면 브라우저 언어에서 추정, 아니면 한국어.
  const nav = typeof navigator !== 'undefined' ? navigator.language.slice(0, 2) : 'ko';
  return nav === 'ja' || nav === 'en' ? (nav as Lang) : 'ko';
}

export function LangProvider({ children }: { children: ReactNode }) {
  const [lang, setLangState] = useState<Lang>(loadLang);

  useEffect(() => {
    document.documentElement.lang = lang;
  }, [lang]);

  const setLang = useCallback((l: Lang) => {
    setLangState(l);
    try {
      localStorage.setItem(STORAGE_KEY, l);
    } catch {
      /* 무시 */
    }
  }, []);

  const t = useCallback<TFn>(
    (key, params) => {
      const dict = translations[lang] ?? translations.ko;
      let s = dict[key] ?? translations.ko[key] ?? key;
      if (params) {
        for (const [k, v] of Object.entries(params)) {
          s = s.replace(`{${k}}`, String(v));
        }
      }
      return s;
    },
    [lang],
  );

  const value = useMemo(() => ({ lang, setLang, t }), [lang, setLang, t]);
  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useLang(): LangCtx {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error('useLang must be used within <LangProvider>');
  return ctx;
}

/** 언어 전환 버튼 (한국어 / 日本語 / English). */
export function LangSwitcher() {
  const { lang, setLang } = useLang();
  return (
    <span className="langswitch">
      {LANGS.map(({ code, label }) => (
        <button
          key={code}
          type="button"
          className={code === lang ? 'active' : ''}
          onClick={() => setLang(code)}
        >
          {label}
        </button>
      ))}
    </span>
  );
}
