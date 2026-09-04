# Aikido security backlog — OSIRIS Android (`aquafishstore-boop/aa`)

**Pulled:** 2026-09-04 via `aikido_full_scan`  
**Blocked:** `aikido_issues_list` — feature disabled for workspace `aquafishstore-boop`  
Enable: https://app.aikido.dev/settings/integrations/ide/mcp/permissions

## Open findings (SAST)

| Pri | Sev | Rule | File | Plan |
|-----|-----|------|------|------|
| P0 | 60 | `AIK_binary_integrity_check_missing` | `Dockerfile` | SHA-256 pin Gradle + cmdline-tools zips before unzip |
| P1 | 60 | `AIK_android_exported-true` | `AndroidManifest.xml` | Keep export; harden deep-link validation + fuzz tests |
| P1 | 35 | `AIK_kotlin_webview-js` | `OsirisWebViewFactory.kt` | Accept (MapLibre); document + audit bridge allowlist |

## Infra gaps

- Checkov IaC binary missing (`checkov.exe` ENOENT) — incomplete Dockerfile/IaC coverage
- Platform issue feed not available until MCP permissions enabled — may hide OSS/dep/surface findings

## Implementation order

1. **P0** Dockerfile checksum verification  
2. Enable Aikido issues feed + Checkov  
3. Deep-link / Custom Tabs hardening tests  
4. CI: scan + `assembleRelease` on PR  
5. Document accepted findings in this file when intentionally wontfix

## Already in place

Cleartext off, allowlists, SSL cancel, Safe Browsing, locked-down WebView settings, sanitized `OsirisNative` bridge, backup exclusions, UrlAllowlist on deep links.
