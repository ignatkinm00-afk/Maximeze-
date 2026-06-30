import { useEffect, useRef } from "react";
import { useBrowserStore } from "../store/browserStore";
import { NewTabPage } from "./NewTabPage";
import styles from "./WebContent.module.css";

export function WebContent() {
  const { tabs, updateTab, recordVisit } = useBrowserStore();

  return (
    <div className={styles.container}>
      {tabs.map((tab) => (
        <div
          key={tab.id}
          className={`${styles.tabContent} ${tab.isActive ? styles.visible : styles.hidden}`}
          role="tabpanel"
          aria-hidden={!tab.isActive}
        >
          {tab.url === "about:newtab" ? (
            <NewTabPage />
          ) : tab.isSuspended ? (
            <SuspendedPage tabId={tab.id} url={tab.url} />
          ) : (
            <WebView
              tabId={tab.id}
              url={tab.url}
              onTitleChange={(title) => updateTab(tab.id, { title })}
              onFaviconChange={(favicon) => updateTab(tab.id, { favicon })}
              onLoadStart={() => updateTab(tab.id, { isLoading: true })}
              onLoadEnd={(url, title) => {
                updateTab(tab.id, { isLoading: false, url, title });
                recordVisit(url, title);
              }}
            />
          )}
        </div>
      ))}
    </div>
  );
}

interface WebViewProps {
  tabId: string;
  url: string;
  onTitleChange: (title: string) => void;
  onFaviconChange: (favicon: string) => void;
  onLoadStart: () => void;
  onLoadEnd: (url: string, title: string) => void;
}

function WebView({ tabId, url, onTitleChange, onFaviconChange, onLoadStart, onLoadEnd }: WebViewProps) {
  // In Tauri, the actual web content is rendered by the native WebView2.
  // This component represents the placeholder in the React tree; in production
  // Tauri's webview is layered underneath the UI overlay.
  // For the development preview, we render an iframe as a stand-in.
  const iframeRef = useRef<HTMLIFrameElement>(null);

  useEffect(() => {
    onLoadStart();
    // Simulate load completion after a tick (in production this comes from Tauri events)
    const timer = setTimeout(() => {
      onLoadEnd(url, url);
    }, 500);
    return () => clearTimeout(timer);
  }, [url]);

  return (
    <div className={styles.webview} data-tab-id={tabId}>
      {/* In production Tauri build, the WebView2 window is positioned here */}
      <div className={styles.webviewPlaceholder}>
        <p className={styles.webviewNote}>
          WebView2 renders here in the production Tauri build.
        </p>
        <p className={styles.webviewUrl}>{url}</p>
      </div>
    </div>
  );
}

interface SuspendedPageProps {
  tabId: string;
  url: string;
}

function SuspendedPage({ tabId, url }: SuspendedPageProps) {
  const { unsuspendTab } = useBrowserStore();

  return (
    <div className={styles.suspended}>
      <div className={styles.suspendedContent}>
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" className={styles.suspendedIcon}>
          <path d="M18.5 2.5a2.5 2.5 0 0 1 3 3L5 20l-4 1 1-4Z" />
          <path d="m15 5 3 3" />
        </svg>
        <p className={styles.suspendedTitle}>Tab suspended</p>
        <p className={styles.suspendedUrl}>{url}</p>
        <button
          className={styles.resumeBtn}
          onClick={() => unsuspendTab(tabId)}
        >
          Resume tab
        </button>
      </div>
    </div>
  );
}
