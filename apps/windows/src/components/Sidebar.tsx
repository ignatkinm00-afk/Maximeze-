import { useState } from "react";
import type { ReactNode } from "react";
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
  const { settings, updateSettings, clearHistory } = useBrowserStore();
  const [confirmClear, setConfirmClear] = useState(false);

  return (
    <div className={styles.settingsScroll}>

      <SettingsSection title="Внешний вид">
        <SettingsRow label="Тема оформления">
          <select
            className={styles.select}
            value={settings.theme}
            onChange={(e) => updateSettings({ theme: e.target.value as "light" | "dark" | "system" })}
          >
            <option value="system">Системная</option>
            <option value="light">Светлая</option>
            <option value="dark">Тёмная</option>
          </select>
        </SettingsRow>
        <SettingsRow label={`Размер текста — ${Math.round(settings.fontSize * 100)}%`}>
          <input
            type="range"
            className={styles.slider}
            min={75} max={200} step={25}
            value={Math.round(settings.fontSize * 100)}
            onChange={(e) => updateSettings({ fontSize: Number(e.target.value) / 100 })}
          />
        </SettingsRow>
      </SettingsSection>

      <SettingsSection title="Поиск">
        <SettingsRow label="Поисковая система">
          <select
            className={styles.select}
            value={settings.defaultSearchEngine}
            onChange={(e) => updateSettings({ defaultSearchEngine: e.target.value as "google" | "bing" | "duckduckgo" })}
          >
            <option value="google">Google</option>
            <option value="bing">Bing</option>
            <option value="duckduckgo">DuckDuckGo</option>
          </select>
        </SettingsRow>
        <ToggleRow
          label="Поисковые подсказки"
          subtitle="Показывать предложения при вводе"
          checked={settings.showSearchSuggestions}
          onChange={(v) => updateSettings({ showSearchSuggestions: v })}
        />
        <ToggleRow
          label="Безопасный поиск"
          subtitle="Фильтровать результаты для взрослых"
          checked={settings.safeSearch}
          onChange={(v) => updateSettings({ safeSearch: v })}
        />
      </SettingsSection>

      <SettingsSection title="Конфиденциальность">
        <ToggleRow
          label="Блокировать рекламу"
          subtitle="Убирать рекламные блоки"
          checked={settings.blockAds}
          onChange={(v) => updateSettings({ blockAds: v })}
        />
        <ToggleRow
          label="Блокировать трекеры"
          subtitle="Защита от слежки сторонних сервисов"
          checked={settings.blockTrackers}
          onChange={(v) => updateSettings({ blockTrackers: v })}
        />
        <ToggleRow
          label="Не отслеживать (DNT)"
          subtitle="Отправлять запрос об отказе от слежки"
          checked={settings.doNotTrack}
          onChange={(v) => updateSettings({ doNotTrack: v })}
        />
        <ToggleRow
          label="Принудительный HTTPS"
          subtitle="Переключать сайты на безопасное соединение"
          checked={settings.httpsOnly}
          onChange={(v) => updateSettings({ httpsOnly: v })}
        />
        <SettingsRow label="Куки">
          <select
            className={styles.select}
            value={settings.cookiePolicy}
            onChange={(e) => updateSettings({ cookiePolicy: e.target.value as "allow-all" | "block-third-party" | "block-all" })}
          >
            <option value="allow-all">Разрешить все</option>
            <option value="block-third-party">Блокировать сторонние</option>
            <option value="block-all">Блокировать все</option>
          </select>
        </SettingsRow>
      </SettingsSection>

      <SettingsSection title="Содержимое страниц">
        <ToggleRow
          label="JavaScript"
          subtitle="Разрешить выполнение скриптов"
          checked={settings.javaScriptEnabled}
          onChange={(v) => updateSettings({ javaScriptEnabled: v })}
        />
        <ToggleRow
          label="Панель закладок"
          subtitle="Показывать под адресной строкой"
          checked={settings.showBookmarksBar}
          onChange={(v) => updateSettings({ showBookmarksBar: v })}
        />
      </SettingsSection>

      <SettingsSection title="Загрузки">
        <ToggleRow
          label="Спрашивать место сохранения"
          subtitle="Запрашивать папку для каждой загрузки"
          checked={settings.askDownloadLocation}
          onChange={(v) => updateSettings({ askDownloadLocation: v })}
        />
      </SettingsSection>

      <SettingsSection title="Дополнительно">
        <ToggleRow
          label="Открывать ссылки в фоне"
          subtitle="Не переключаться при открытии новых ссылок"
          checked={settings.openLinksInBackground}
          onChange={(v) => updateSettings({ openLinksInBackground: v })}
        />
        <ToggleRow
          label="Очищать данные при выходе"
          subtitle="Удалять историю и куки при закрытии"
          checked={settings.clearDataOnExit}
          onChange={(v) => updateSettings({ clearDataOnExit: v })}
        />
      </SettingsSection>

      <SettingsSection title="Данные браузера">
        {confirmClear ? (
          <div className={styles.confirmRow}>
            <span className={styles.confirmText}>Удалить всю историю?</span>
            <button className={styles.dangerBtn} onClick={() => { clearHistory(); setConfirmClear(false); }}>Удалить</button>
            <button className={styles.cancelBtn} onClick={() => setConfirmClear(false)}>Отмена</button>
          </div>
        ) : (
          <button className={styles.dangerOutlineBtn} onClick={() => setConfirmClear(true)}>
            Очистить историю
          </button>
        )}
      </SettingsSection>

      <SettingsSection title="О браузере">
        <div className={styles.about}>
          <span className={styles.aboutName}>Maximeze Browser</span>
          <span className={styles.aboutVersion}>Версия 0.1.0 · Открытый исходный код</span>
        </div>
      </SettingsSection>

    </div>
  );
}

function SettingsSection({ title, children }: { title: string; children: ReactNode }) {
  return (
    <div className={styles.section}>
      <div className={styles.sectionTitle}>{title}</div>
      <div className={styles.sectionContent}>{children}</div>
    </div>
  );
}

function SettingsRow({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className={styles.settingRow}>
      <span className={styles.settingLabel}>{label}</span>
      {children}
    </div>
  );
}

function ToggleRow({ label, subtitle, checked, onChange }: {
  label: string;
  subtitle?: string;
  checked: boolean;
  onChange: (v: boolean) => void;
}) {
  return (
    <div className={styles.toggleRow} onClick={() => onChange(!checked)}>
      <div className={styles.toggleInfo}>
        <span className={styles.toggleLabel}>{label}</span>
        {subtitle && <span className={styles.toggleSubtitle}>{subtitle}</span>}
      </div>
      <div className={`${styles.toggle} ${checked ? styles.toggleOn : ""}`}>
        <div className={styles.toggleThumb} />
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
