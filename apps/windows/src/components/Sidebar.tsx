import { useBrowserStore } from "../store/browserStore";
import styles from "./Sidebar.module.css";

export function Sidebar() {
  const { sidebarOpen, sidebarTab, setSidebarOpen } = useBrowserStore();

  if (!sidebarOpen) return null;

  return (
    <div className={styles.sidebar} role="complementary">
      <div className={styles.header}>
        <h2 className={styles.title}>
          {sidebarTab === "bookmarks" && "Закладки"}
          {sidebarTab === "history" && "История"}
          {sidebarTab === "downloads" && "Загрузки"}
          {sidebarTab === "settings" && "Настройки"}
        </h2>
        <button
          className={styles.closeBtn}
          onClick={() => setSidebarOpen(false)}
          aria-label="Закрыть панель"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
            <path d="M18 6 6 18M6 6l12 12" />
          </svg>
        </button>
      </div>
      <div className={styles.content}>
        {sidebarTab === "bookmarks" && <BookmarksPanel />}
        {sidebarTab === "history" && <HistoryPanel />}
        {sidebarTab === "downloads" && <DownloadsPanel />}
        {sidebarTab === "settings" && <SettingsPanel />}
      </div>
    </div>
  );
}

function BookmarksPanel() {
  const { bookmarks, getActiveTab, navigate, removeBookmark } = useBrowserStore();

  if (bookmarks.length === 0) {
    return <Empty message="Нет закладок" />;
  }

  return (
    <div className={styles.list}>
      {bookmarks.map((bm) => (
        <div key={bm.id} className={styles.listItem}>
          <div className={styles.itemInfo} onClick={() => {
            const tab = getActiveTab();
            if (tab) navigate(tab.id, bm.url);
          }}>
            <span className={styles.itemTitle}>{bm.title}</span>
            <span className={styles.itemMeta}>{bm.url}</span>
          </div>
          <button className={styles.removeBtn} onClick={() => removeBookmark(bm.id)} aria-label="Удалить закладку">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
              <path d="M18 6 6 18M6 6l12 12" />
            </svg>
          </button>
        </div>
      ))}
    </div>
  );
}

function HistoryPanel() {
  const { history, clearHistory, getActiveTab, navigate } = useBrowserStore();

  if (history.length === 0) {
    return <Empty message="История пуста" />;
  }

  return (
    <>
      <div className={styles.list}>
        {history.slice(0, 50).map((entry) => (
          <div key={entry.id} className={styles.listItem} onClick={() => {
            const tab = getActiveTab();
            if (tab) navigate(tab.id, entry.url);
          }}>
            <div className={styles.itemInfo}>
              <span className={styles.itemTitle}>{entry.title || entry.url}</span>
              <span className={styles.itemMeta}>{new Date(entry.visitedAt).toLocaleString()}</span>
            </div>
          </div>
        ))}
      </div>
      <div className={styles.actions}>
        <button className={styles.clearBtn} onClick={clearHistory}>Очистить историю</button>
      </div>
    </>
  );
}

function DownloadsPanel() {
  const { downloads, pauseDownload, resumeDownload, cancelDownload } = useBrowserStore();

  if (downloads.length === 0) {
    return <Empty message="Нет загрузок" />;
  }

  return (
    <div className={styles.list}>
      {downloads.map((dl) => (
        <div key={dl.id} className={styles.downloadItem}>
          <div className={styles.itemInfo}>
            <span className={styles.itemTitle}>{dl.filename}</span>
            <span className={styles.itemMeta}>{dl.status}</span>
          </div>
          {dl.totalBytes > 0 && (
            <div className={styles.progressBar}>
              <div
                className={styles.progressFill}
                style={{ width: `${(dl.receivedBytes / dl.totalBytes) * 100}%` }}
              />
            </div>
          )}
          <div className={styles.downloadActions}>
            {dl.status === "downloading" && (
              <button onClick={() => pauseDownload(dl.id)}>Пауза</button>
            )}
            {dl.status === "paused" && (
              <button onClick={() => resumeDownload(dl.id)}>Возобновить</button>
            )}
            {(dl.status === "downloading" || dl.status === "paused") && (
              <button onClick={() => cancelDownload(dl.id)}>Отмена</button>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}

function SettingsPanel() {
  const { settings, updateSettings } = useBrowserStore();

  return (
    <div className={styles.settings}>
      <div className={styles.settingRow}>
        <label className={styles.settingLabel}>Тема</label>
        <select
          className={styles.settingSelect}
          value={settings.theme}
          onChange={(e) => updateSettings({ theme: e.target.value as "light" | "dark" | "system" })}
        >
          <option value="system">Системная</option>
          <option value="light">Светлая</option>
          <option value="dark">Тёмная</option>
        </select>
      </div>

      <div className={styles.settingRow}>
        <label className={styles.settingLabel}>Поисковик</label>
        <select
          className={styles.settingSelect}
          value={settings.defaultSearchEngine}
          onChange={(e) => updateSettings({ defaultSearchEngine: e.target.value as "google" | "bing" | "duckduckgo" })}
        >
          <option value="google">Google</option>
          <option value="bing">Bing</option>
          <option value="duckduckgo">DuckDuckGo</option>
        </select>
      </div>

      <div className={styles.settingRow}>
        <label className={styles.settingLabel}>Блокировать трекеры</label>
        <input
          type="checkbox"
          checked={settings.blockTrackers}
          onChange={(e) => updateSettings({ blockTrackers: e.target.checked })}
        />
      </div>

      <div className={styles.settingRow}>
        <label className={styles.settingLabel}>Блокировать рекламу</label>
        <input
          type="checkbox"
          checked={settings.blockAds}
          onChange={(e) => updateSettings({ blockAds: e.target.checked })}
        />
      </div>

      <div className={styles.settingRow}>
        <label className={styles.settingLabel}>Не отслеживать</label>
        <input
          type="checkbox"
          checked={settings.doNotTrack}
          onChange={(e) => updateSettings({ doNotTrack: e.target.checked })}
        />
      </div>
    </div>
  );
}

function Empty({ message }: { message: string }) {
  return (
    <div className={styles.empty}>
      <p>{message}</p>
    </div>
  );
}
