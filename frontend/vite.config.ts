import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 로그인/회원가입은 Firebase Authentication 을 직접 호출하므로 별도 백엔드 프록시가 필요 없다.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
  },
});
