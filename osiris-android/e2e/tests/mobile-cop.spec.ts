import { test, expect, type Page } from '@playwright/test';
import fs from 'node:fs';
import path from 'node:path';

const RESULTS_DIR = path.join(__dirname, '..', 'results');

function ensureResultsDir() {
  fs.mkdirSync(RESULTS_DIR, { recursive: true });
}

async function collectPerf(page: Page) {
  return page.evaluate(() => {
    const nav = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming | undefined;
    const paints = performance.getEntriesByType('paint');
    const fcp = paints.find((p) => p.name === 'first-contentful-paint')?.startTime ?? null;
    return {
      domContentLoaded: nav?.domContentLoadedEventEnd ?? null,
      loadEventEnd: nav?.loadEventEnd ?? null,
      transferSize: nav?.transferSize ?? null,
      fcp,
      url: location.href,
      title: document.title,
      viewport: { w: window.innerWidth, h: window.innerHeight },
    };
  });
}

test.describe('OSIRIS mobile COP (WebView proxy)', () => {
  test('health API is live', async ({ request }) => {
    const res = await request.get('https://osirisai.live/api/health');
    expect(res.ok(), `health status ${res.status()}`).toBeTruthy();
    const body = await res.text();
    expect(body.length).toBeGreaterThan(0);
  });

  test('stats API returns counters', async ({ request }) => {
    const res = await request.get('https://osirisai.live/api/stats');
    expect(res.ok()).toBeTruthy();
    const json = await res.json();
    expect(json).toBeTruthy();
  });

  test('map shell loads on mobile viewport', async ({ page }, testInfo) => {
    ensureResultsDir();
    const started = Date.now();
    await page.goto('/', { waitUntil: 'domcontentloaded' });

    // Boot / brand signal
    await expect(page).toHaveTitle(/OSIRIS/i);

    // Give MapLibre / HUD a moment to hydrate
    await page.waitForTimeout(4_000);

    const bodyText = await page.locator('body').innerText();
    expect(bodyText.length).toBeGreaterThan(10);

    // Mobile layout should expose primary controls (labels vary by version)
    const controlHints = [
      page.getByRole('button', { name: /layers|aviation|maritime|display/i }).first(),
      page.getByText(/recon|intel|search|layers|markets/i).first(),
    ];

    let foundControl = false;
    for (const locator of controlHints) {
      if (await locator.count()) {
        foundControl = true;
        break;
      }
    }
    expect(foundControl, 'expected at least one mobile/map control affordance').toBeTruthy();

    const perf = await collectPerf(page);
    const screenshotPath = path.join(
      RESULTS_DIR,
      `map-${testInfo.project.name}.png`,
    );
    await page.screenshot({ path: screenshotPath, fullPage: false });

    const metric = {
      project: testInfo.project.name,
      durationMs: Date.now() - started,
      perf,
      screenshot: screenshotPath,
      timestamp: new Date().toISOString(),
    };
    fs.writeFileSync(
      path.join(RESULTS_DIR, `metrics-${testInfo.project.name}.json`),
      JSON.stringify(metric, null, 2),
    );
  });

  test('docs page is readable on mobile', async ({ page }, testInfo) => {
    ensureResultsDir();
    await page.goto('/docs', { waitUntil: 'domcontentloaded' });
    await expect(page).toHaveTitle(/OSIRIS|Documentation|API/i);
    await expect(page.getByRole('heading', { level: 1 }).first()).toBeVisible();

    // API reference should list health endpoint somewhere
    const content = await page.locator('body').innerText();
    expect(content).toMatch(/\/api\/health|Quick Start|API/i);

    await page.screenshot({
      path: path.join(RESULTS_DIR, `docs-${testInfo.project.name}.png`),
      fullPage: false,
    });
  });

  test('external deep paths stay on origin', async ({ page }) => {
    await page.goto('/', { waitUntil: 'domcontentloaded' });
    const url = page.url();
    expect(url.startsWith('https://osirisai.live')).toBeTruthy();
  });
});
