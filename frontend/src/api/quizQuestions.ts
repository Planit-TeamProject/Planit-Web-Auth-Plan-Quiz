// 퀴즈 문제 생성 — 백엔드 MockQuizQuestionGenerator 를 클라이언트로 옮긴 것.
// 실제 OpenAI 연동(박지민 담당) 전까지는 입력(과목/학습 범위)과 무관하게 항상 같은 예시 3문제를 돌려준다.
// 쉬운 문제(BASIC) 2개 + 응용 문제(APPLIED) 1개 (REQ-Q-002).
// OpenAI 연동이 정해지면 generateQuestions 만 교체하면 나머지 흐름은 그대로다.

export type QuestionType = 'BASIC' | 'APPLIED';

export interface GeneratedQuestion {
  questionType: QuestionType;
  questionText: string;
  choice1: string;
  choice2: string;
  choice3: string;
  choice4: string;
  answerNo: number; // 1~4
  explanation: string;
}

const EXAMPLE_QUESTIONS: GeneratedQuestion[] = [
  {
    questionType: 'BASIC',
    questionText: '운영체제의 주요 역할로 보기 어려운 것은?',
    choice1: 'CPU·메모리 등 시스템 자원 관리',
    choice2: '사용자와 하드웨어 사이의 인터페이스 제공',
    choice3: '프로세스 생성과 스케줄링',
    choice4: '응용 프로그램의 소스 코드 컴파일',
    answerNo: 4,
    explanation:
      '운영체제는 자원 관리, 하드웨어 추상화, 프로세스·메모리·파일 관리를 담당한다. ' +
      '소스 코드를 기계어로 바꾸는 컴파일은 컴파일러(개발 도구)의 일이지 운영체제의 기능이 아니다.',
  },
  {
    questionType: 'BASIC',
    questionText: '리눅스에 대한 설명으로 옳은 것은?',
    choice1: '리누스 토르발스가 공개한 유닉스 계열 오픈소스 운영체제이다',
    choice2: '소스 코드가 공개되지 않은 상용 전용 운영체제이다',
    choice3: '커널 없이 셸(Shell)만으로 동작한다',
    choice4: '한 번에 한 명의 사용자만 로그인할 수 있다',
    answerNo: 1,
    explanation:
      '리눅스는 1991년 리누스 토르발스가 공개한 유닉스 계열 오픈소스 운영체제로, ' +
      '커널을 중심으로 다중 사용자·다중 작업(멀티태스킹)을 지원한다.',
  },
  {
    questionType: 'APPLIED',
    questionText:
      '현재 작업 디렉터리에 있는 파일들을 권한·소유자·크기까지 한 줄씩 자세히 확인하려고 한다. 알맞은 명령은?',
    choice1: 'ls -l',
    choice2: 'cd -l',
    choice3: 'pwd -a',
    choice4: 'mkdir -l',
    answerNo: 1,
    explanation:
      'ls 는 디렉터리 내용을 보여주는 명령이고, -l 옵션을 붙이면 권한, 링크 수, 소유자, 그룹, ' +
      '크기, 수정 시각을 한 줄씩 출력한다. cd 는 디렉터리 이동, pwd 는 현재 경로 출력, ' +
      'mkdir 는 디렉터리 생성 명령이다.',
  },
];

/** 오늘 학습 범위로 퀴즈 문제 3개를 만든다 (현재는 고정 예시). */
export function generateQuestions(_subjectName: string, _todayScope: string): GeneratedQuestion[] {
  return EXAMPLE_QUESTIONS.map((q) => ({ ...q }));
}
