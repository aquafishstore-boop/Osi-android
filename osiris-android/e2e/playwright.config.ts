import { defineConfig, devices } from '@playwright/test';

/**
 * Mobile UX suite for the experience the Android WebView loads (https://osirisai.live).
 * Pixel 7 / iPhone 14 viewports exercise OSIRIS useIsMobile layout.
 */
export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: process.env.CI ? 2 : undefined,
  reporter: [
    ['list'],
    ['json', { outputFile: 'results/playwright-results.json' }],
    ['html', { open: 'never', outputFolder: 'results/html' }],
  ],
  timeout: 90_000,
  expect: { timeout: 20_000 },
  use: {
    baseURL: 'https://osirisai.live',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    ignoreHTTPSErrors: false,
  },
  projects: [
    {
      name: 'pixel7',
      use: { ...devices['Pixel 7'] },
    },
    {
      // Chromium + iPhone viewport — WebKit optional; Android shell is the primary target
      name: 'iphone14',
      use: {
        ...devices['iPhone 14'],
        browserName: 'chromium',
        isMobile: true,
        hasTouch: true,
      },
    },
  ],
  outputDir: 'results/test-output',
});
