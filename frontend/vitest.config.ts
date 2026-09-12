import { fileURLToPath } from 'node:url';

import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    environment: 'jsdom',
    // Ainda nao ha teste nenhum: o codigo de aplicacao e escrito pela equipe.
    // Sem esta flag o vitest sai com codigo 1 em "No test files found" e derruba o
    // CI por ausencia de teste, e nao por teste quebrado. Remover quando o primeiro
    // teste da equipe existir.
    passWithNoTests: true,
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
});
