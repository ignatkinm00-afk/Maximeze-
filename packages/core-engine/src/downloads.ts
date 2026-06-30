import type { Download, DownloadStatus } from "./types";

export function createDownload(
  url: string,
  filename: string,
  savePath: string,
  totalBytes = 0
): Download {
  return {
    id: generateId(),
    url,
    filename,
    savePath,
    totalBytes,
    receivedBytes: 0,
    status: "pending",
    startedAt: Date.now(),
  };
}

export function updateDownloadProgress(
  downloads: Download[],
  id: string,
  receivedBytes: number,
  totalBytes?: number
): Download[] {
  return downloads.map((d) =>
    d.id === id
      ? {
          ...d,
          receivedBytes,
          totalBytes: totalBytes ?? d.totalBytes,
          status: "downloading" as DownloadStatus,
        }
      : d
  );
}

export function completeDownload(downloads: Download[], id: string): Download[] {
  return downloads.map((d) =>
    d.id === id
      ? { ...d, status: "completed" as DownloadStatus, completedAt: Date.now(), receivedBytes: d.totalBytes }
      : d
  );
}

export function pauseDownload(downloads: Download[], id: string): Download[] {
  return downloads.map((d) =>
    d.id === id && d.status === "downloading"
      ? { ...d, status: "paused" as DownloadStatus }
      : d
  );
}

export function resumeDownload(downloads: Download[], id: string): Download[] {
  return downloads.map((d) =>
    d.id === id && d.status === "paused"
      ? { ...d, status: "downloading" as DownloadStatus }
      : d
  );
}

export function cancelDownload(downloads: Download[], id: string): Download[] {
  return downloads.map((d) =>
    d.id === id && (d.status === "downloading" || d.status === "paused")
      ? { ...d, status: "cancelled" as DownloadStatus }
      : d
  );
}

export function getDownloadProgress(download: Download): number {
  if (download.totalBytes === 0) return 0;
  return Math.min(download.receivedBytes / download.totalBytes, 1);
}

export function formatFileSize(bytes: number): string {
  if (bytes === 0) return "0 B";
  const units = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  return `${(bytes / Math.pow(1024, i)).toFixed(i === 0 ? 0 : 1)} ${units[i]}`;
}

function generateId(): string {
  return `dl-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 9)}`;
}
