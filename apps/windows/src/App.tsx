import { useEffect } from "react";
import { useBrowserStore } from "./store/browserStore";
import { TabBar } from "./components/TabBar";
import { Toolbar } from "./components/Toolbar";
import { WebContent } from "./components/WebContent";
import { Sidebar } from "./components/Sidebar";
import styles from "./App.module.css";

export default function App() {
  const { settings, updateSettings } = useBrowserStore();

  useEffect(() => {
    const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
    const applyTheme = (dark: boolean) => {
      document.documentElement.dataset.theme = dark ? "dark" : "light";
    };

    if (settings.theme === "system") {
      applyTheme(mediaQuery.matches);
      const handler = (e: MediaQueryListEvent) => applyTheme(e.matches);
      mediaQuery.addEventListener("change", handler);
      return () => mediaQuery.removeEventListener("change", handler);
    } else {
      applyTheme(settings.theme === "dark");
    }
  }, [settings.theme]);

  return (
    <div className={styles.root}>
      <TabBar />
      <Toolbar />
      <div className={styles.content}>
        <WebContent />
        <Sidebar />
      </div>
    </div>
  );
}
